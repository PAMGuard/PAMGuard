package deepWhistle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jamdev.jdl4pam.deepWhistle.DeepWhistleTest.DeepWhistleInfo;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.JamArr;

import PamUtils.PamUtils;
import ai.djl.Device;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.translate.TranslateException;
import fftManager.FFTDataUnit;

/**
 * Implements the SAM-Whistle model as a PAMGuard FFT mask.
 * <p>
 * SAM-Whistle ("Adapting Foundation Models for Automated Dolphin Whistle
 * Detection", <a href="https://github.com/Paul-LiPu/DeepWhistle">based on
 * DeepWhistle</a> / silbido) is a Segment-Anything based network. It is used
 * here in exactly the same way as {@link DeepWhistleMask} - a confidence
 * surface is produced from a buffered spectrogram and multiplied back into the
 * FFT data - but the pre-processing and the model input format differ.
 * <p>
 * <b>Pre-processing.</b> Reproduces the DCLDE inference pipeline in the
 * sam_whistle repository ({@code tonal_tracker._load_spectrogram} with
 * {@code use_conf}):
 * <pre>
 *   crop to [5, 50] kHz  -&gt;  20*log10(|FFT|) (dB)  -&gt;  z-score normalise
 * </pre>
 * The Python code crops the spectrogram to 5-50 kHz and then z-score normalises
 * (subtract the mean, divide by the standard deviation) over the <i>cropped</i>
 * region. Because the model input is z-scored, the absolute magnitude scaling of
 * PAMGuard's FFT versus librosa's STFT does not matter (a constant scale factor
 * becomes a constant dB offset which the z-score removes) - so, unlike the
 * DeepWhistle mask, no window/bit-depth magnitude offset is required. The model
 * was trained with a Hamming window and an ~8 ms FFT length / ~2 ms hop; see
 * {@link SamWhistleMaskPane} which checks the FFT source is configured to match.
 * <p>
 * <b>Model input.</b> The network expects a three-channel image
 * <code>[1, 3, H, W]</code> with the spectrogram replicated across the channels
 * (see {@link SamSpectrumTranslator}). During training the spectrogram is
 * flipped so the highest frequency is at the top (row 0); {@link #FLIP_FREQ}
 * reproduces this. The flip is applied to the model input and undone on the model
 * output, so it only affects the orientation the network sees - the mapping of
 * the returned mask back onto FFT bins is unaffected.
 *
 * @author Jamie Macaulay
 */
public class SamWhistleMask extends AbstractFFTMask {

	/**
	 * URL to the downloadable SAM-Whistle model (trained on the DCLDE 2011
	 * dataset). The model is hosted as a release asset (a zip containing the model
	 * file and a README) in the PAMGuard deeplearningmodels repository on GitHub.
	 * <p>
	 * NOTE: this asset does not exist yet - it must be added to the
	 * deeplearningmodels repository. Update the release/version in this URL to
	 * match once it has been uploaded.
	 */
	public static final String MODEL_URL = "https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/sam_whistle.zip";

	/**
	 * Name of the traced model file to locate inside the downloaded archive. This
	 * must match the file name used when the model is exported (traced/scripted)
	 * and uploaded to GitHub.
	 */
	public static final String MODEL_FILE_NAME = "sam_whistle.pt";

	/**
	 * Frequency bins the exported model expects (its traced input height). Because
	 * the traced model bakes in its input size (it resizes internally to the SAM
	 * 1024 grid and interpolates the mask back to the input size), the spectrogram
	 * block is resized to {@link #MODEL_INPUT_FREQ_BINS} x {@link #MODEL_INPUT_TIME_STEPS}
	 * before inference and the mask is resized back afterwards. This makes the mask
	 * robust to the exact FFT length / buffer PAMGuard is using.
	 * <p>
	 * These MUST match the {@code --height} / {@code --width} used when exporting
	 * the model (see {@code scripts/export_torchscript.py}). The default export is
	 * 361 (5-50 kHz at ~125 Hz/bin) x 1500 (3 s at a 2 ms hop).
	 */
	private static final int MODEL_INPUT_FREQ_BINS = 361;

	/**
	 * Time steps the exported model expects (its traced input width). See
	 * {@link #MODEL_INPUT_FREQ_BINS}. For best fidelity set the PAMGuard SAM buffer
	 * so the number of buffered FFTs is close to this (3 s at a 2 ms hop = 1500),
	 * which keeps the time resize close to identity.
	 */
	private static final int MODEL_INPUT_TIME_STEPS = 1500;

	/**
	 * Whether to flip the spectrogram in frequency (highest frequency at row 0)
	 * before passing it to the model, matching the {@code np.flip(..., axis=0)}
	 * step in the sam_whistle training/inference code. Applied symmetrically (the
	 * model output is flipped back) so it only changes the orientation the network
	 * sees. If validation against the reference model shows the mask is upside
	 * down, toggle this flag.
	 */
	private static final boolean FLIP_FREQ = true;

	/**
	 * Small constant added to the standard deviation before dividing, matching the
	 * {@code + 1e-8} in the reference {@code normalize_spect} z-score.
	 */
	private static final double ZSCORE_EPS = 1e-8;

	private Predictor<float[][][], float[]> specPredictor;

	/**
	 * The device the model was actually loaded on (e.g. mps or cpu), for logging.
	 */
	private Device modelDevice;

	/**
	 * Per-channel running (global) statistics of the dB spectrogram, accumulated
	 * across all processed blocks with Welford's algorithm. The reference
	 * implementation z-score normalises over the whole recording, so we approximate
	 * that here with statistics that accumulate over the whole run rather than
	 * normalising each block on its own. Kept per channel (each channel is a
	 * separate "recording"). Reset in {@link #initMask()}.
	 */
	private final Map<Integer, RunningStats> channelStats = new HashMap<>();

	/**
	 * Welford online mean/variance accumulator for one channel.
	 */
	private static class RunningStats {
		long count = 0;
		double mean = 0.0;
		double m2 = 0.0;
	}

	public SamWhistleMask(MaskedFFTProcess maksedFFTProcess) {
		super(maksedFFTProcess);
	}

	@Override
	public boolean initMask() {

		if (specPredictor != null) {
			//already initialised
			specPredictor.close();
			specPredictor = null;
		}

		//reset the per-channel running global normalisation statistics for the new run.
		channelStats.clear();

		MaskedFFTParamters fftParams = this.maksedFFTProcess.getMaskFFTParams();

		String modelPath = fftParams.modelPath;
		if (modelPath == null) {
			System.err.println("SamWhistleMask: no model has been downloaded - model path is null. "
					+ "Open the settings and select the SAM-Whistle mask to download the model.");
			return false;
		}

		//define some model info and create the transforms
		initTransforms();

		long[] modelShape = getModelShape();
		System.out.println("SamWhistleMask: model input shape: 1 x 3 x " + modelShape[2] + " x " + modelShape[3]);

		specPredictor = loadSamWhistleModel(modelPath);

		if (specPredictor == null) {
			System.err.println("SamWhistleMask: failed to load SAM-Whistle model from " + modelPath);
			return false;
		}

		System.out.println("SamWhistleMask: model running on device " + deviceName());

		return true;
	}

	/**
	 * Create the transform params used in SAM-Whistle pre-processing:
	 * crop to [5, 50] kHz, convert to dB (20*log10), z-score normalise.
	 * <p>
	 * The frequency trim is first (index 0) so it receives the raw spectrogram and
	 * reports the frequency limits used when mapping the mask back onto FFT bins.
	 * Converting to dB is a per-element operation so it is order-independent with
	 * respect to the trim; the z-score is computed over the trimmed region, which
	 * matches the reference inference (crop then normalise).
	 */
	@Override
	protected ArrayList<DLTransfromParams> createTransforms(DeepWhistleInfo modelInfo) {

		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//1) trim to the model frequency range (5-50 kHz)
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECFREQTRIM, modelInfo.minFreq, modelInfo.maxFreq));
		//2) amplitude to dB: 20*log10(|FFT|) (librosa amplitude_to_db, ref=1.0)
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC2DB));

		//NOTE: the z-score normalisation is deliberately NOT part of the per-block
		//transform chain. The reference implementation (tonal_tracker._load_spectrogram)
		//z-score normalises over the WHOLE recording before it is split into blocks, so
		//the model expects globally-normalised input. Normalising each ~3 s block on its
		//own (using SPECNORMALISESTD) stretches noise-only blocks to unit variance and
		//makes the model fire on noise. We instead accumulate running global statistics
		//and apply the z-score ourselves - see normaliseRunning().

		return dlTransformParamsArr;
	}

	@Override
	public double getPreferredBufferSeconds() {
		//Process in blocks of MODEL_INPUT_TIME_STEPS FFT slices so the buffered block
		//matches the width the model was trained on. This keeps the time-axis resize in
		//runSamWhistle close to identity (avoiding time-stretch distortion) regardless of
		//the user-configured buffer. Derived from the actual hop so it is exact at any
		//sample rate (MODEL_INPUT_TIME_STEPS * hopSeconds; 1500 * 2 ms = 3 s).
		double sampleRate = getSampleRate();
		int fftHop = getFFTHop();
		if (sampleRate <= 0 || fftHop <= 0) {
			return -1;
		}
		return MODEL_INPUT_TIME_STEPS * (fftHop / sampleRate);
	}

	@Override
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {
		//single-channel convenience: process as a one-channel batch.
		List<List<FFTDataUnit>> single = new ArrayList<List<FFTDataUnit>>(1);
		single.add(batch);
		return applyMaskChannels(single).get(0);
	}

	/**
	 * Apply the mask to one block from each channel in a single batched inference.
	 * All channels' blocks cover the same time window, so batching them into one
	 * <code>[nChannels, 3, H, W]</code> model call greatly improves throughput
	 * (especially on the GPU) without adding latency.
	 */
	@Override
	public List<List<FFTDataUnit>> applyMaskChannels(List<List<FFTDataUnit>> channelBatches) {

		int nChan = channelBatches.size();
		if (nChan == 0) {
			return channelBatches;
		}

		double nyquist = maksedFFTProcess.getInputFFTData().getSampleRate() / 2.0;

		//--- per channel: transform, normalise and build the model input block ---
		float[][][] modelInputs = new float[nChan][][];   //[chan][MODEL_FREQ][MODEL_TIME]
		double[][] freqLimsArr = new double[nChan][];
		int[] freqBinsArr = new int[nChan];
		int[] timeStepsArr = new int[nChan];

		for (int c = 0; c < nChan; c++) {
			List<FFTDataUnit> batch = channelBatches.get(c);

			FreqTransform freqTransform = transformFFTBatch(batch);

			double[] freqLimits = freqTransform.getFreqlims();
			freqLimsArr[c] = new double[] { freqLimits[0], Math.min(freqLimits[1], nyquist) };

			//transformed dB spectrogram [timeUnit][freqBin] (not yet normalised)
			float[][] dataF = DLUtils.toFloatArray(freqTransform.getSpecTransfrom().getTransformedData());

			//z-score using this channel's running global statistics.
			int channel = PamUtils.getSingleChannel(batch.get(0).getChannelBitmap());
			float[][] dataFNorm = normaliseRunning(channel, dataF);

			//[timeUnit][freqBin] -> [freqBin][timeUnit] (H=freq, W=time)
			float[][] specFreqTime = JamArr.transposeMatrix(dataFNorm);
			freqBinsArr[c] = specFreqTime.length;
			timeStepsArr[c] = specFreqTime[0].length;

			//resize to the model's traced input size; flip so the highest frequency is at the top.
			float[][] resized = resize(specFreqTime, MODEL_INPUT_FREQ_BINS, MODEL_INPUT_TIME_STEPS);
			modelInputs[c] = FLIP_FREQ ? reverseRows(resized) : resized;
		}

		//--- one batched inference across all channels ---
		float[] output;
		try {
			long startTime = System.nanoTime();
			output = specPredictor.predict(modelInputs);   //[nChan, 1, MODEL_FREQ, MODEL_TIME] flattened
			double seconds = (System.nanoTime() - startTime) / 1.0e9;
			System.out.println("SamWhistleMask: " + nChan + " channel(s) processed in " + seconds + " s on " + deviceName());
		} catch (TranslateException e) {
			e.printStackTrace();
			return channelBatches;
		}

		//--- per channel: slice the output, map it back and apply it ---
		int blockLen = MODEL_INPUT_FREQ_BINS * MODEL_INPUT_TIME_STEPS;
		for (int c = 0; c < nChan; c++) {
			float[] slice = Arrays.copyOfRange(output, c * blockLen, (c + 1) * blockLen);

			//[MODEL_FREQ][MODEL_TIME]
			float[][] maskFreqTime = JamArr.to2D(slice, MODEL_INPUT_TIME_STEPS);
			if (FLIP_FREQ) {
				maskFreqTime = reverseRows(maskFreqTime);
			}
			//resize back to this channel's block size, then to [timeUnit][freqBin].
			float[][] maskResized = resize(maskFreqTime, freqBinsArr[c], timeStepsArr[c]);
			double[][] mask = JamArr.floatToDouble(JamArr.transposeMatrix(maskResized));

			applyMask(channelBatches.get(c), mask, freqLimsArr[c]);
		}

		return channelBatches;
	}

	/**
	 * Z-score normalise a dB spectrogram block using running global statistics.
	 * <p>
	 * The reference implementation z-score normalises over the whole recording
	 * before splitting it into blocks. To reproduce that in a streaming context the
	 * running mean and variance of every dB value seen so far are accumulated (with
	 * Welford's online algorithm), the current block is folded in, and the block is
	 * then normalised with the updated global statistics: {@code (x - mean)/(std + eps)}.
	 * This avoids the per-block normalisation which stretches noise-only blocks to
	 * unit variance and causes false positives.
	 * <p>
	 * Statistics accumulate per channel over the whole run (reset in
	 * {@link #initMask()}), which matches the "whole recording" reference for a
	 * stationary noise field. For a long, non-stationary deployment a rolling /
	 * exponentially-weighted estimate could be substituted here.
	 *
	 * @param channel - the audio channel the block belongs to.
	 * @param dB - the dB spectrogram block [timeUnit][freqBin].
	 * @return a new, z-score normalised block of the same shape.
	 */
	private float[][] normaliseRunning(int channel, float[][] dB) {

		RunningStats stats = channelStats.computeIfAbsent(channel, k -> new RunningStats());

		//update the running statistics with every value in this block.
		for (int i = 0; i < dB.length; i++) {
			for (int j = 0; j < dB[i].length; j++) {
				double v = dB[i][j];
				stats.count++;
				double delta = v - stats.mean;
				stats.mean += delta / stats.count;
				stats.m2 += delta * (v - stats.mean);
			}
		}

		//population standard deviation (matches numpy std, ddof=0).
		double variance = stats.count > 0 ? stats.m2 / stats.count : 0.0;
		double denom = Math.sqrt(variance) + ZSCORE_EPS;

		float[][] out = new float[dB.length][dB[0].length];
		for (int i = 0; i < dB.length; i++) {
			for (int j = 0; j < dB[i].length; j++) {
				out[i][j] = (float) ((dB[i][j] - stats.mean) / denom);
			}
		}
		return out;
	}

	/**
	 * Reverse the order of the outer (frequency) dimension of a 2D array.
	 *
	 * @param array - the array [freqBin][timeUnit].
	 * @return a new array with the frequency rows reversed.
	 */
	private static float[][] reverseRows(float[][] array) {
		float[][] out = new float[array.length][];
		for (int i = 0; i < array.length; i++) {
			out[i] = array[array.length - 1 - i];
		}
		return out;
	}

	/**
	 * Bilinearly resize a 2D array to a new number of rows and columns. Used to map
	 * the spectrogram block onto the model's fixed input size and to map the mask
	 * back onto the block size.
	 *
	 * @param src - the source array [rows][cols].
	 * @param outRows - the required number of rows.
	 * @param outCols - the required number of columns.
	 * @return the resized array [outRows][outCols].
	 */
	private static float[][] resize(float[][] src, int outRows, int outCols) {
		int inRows = src.length;
		int inCols = src[0].length;

		if (inRows == outRows && inCols == outCols) {
			return src;
		}

		float[][] out = new float[outRows][outCols];
		for (int r = 0; r < outRows; r++) {
			double fr = (outRows == 1) ? 0 : ((double) r) * (inRows - 1) / (outRows - 1);
			int r0 = (int) Math.floor(fr);
			int r1 = Math.min(r0 + 1, inRows - 1);
			double wr = fr - r0;
			for (int c = 0; c < outCols; c++) {
				double fc = (outCols == 1) ? 0 : ((double) c) * (inCols - 1) / (outCols - 1);
				int c0 = (int) Math.floor(fc);
				int c1 = Math.min(c0 + 1, inCols - 1);
				double wc = fc - c0;

				double top = src[r0][c0] * (1 - wc) + src[r0][c1] * wc;
				double bot = src[r1][c0] * (1 - wc) + src[r1][c1] * wc;
				out[r][c] = (float) (top * (1 - wr) + bot * wr);
			}
		}
		return out;
	}

	/**
	 * Load the traced SAM-Whistle model into memory and create a predictor.
	 * <p>
	 * The model is a large Segment-Anything network, so it is loaded onto the GPU
	 * where possible - on Apple Silicon this is the Metal (MPS) backend, which gives
	 * roughly a 3-5x speed-up over the CPU. Each candidate device is validated with a
	 * warm-up inference (which also pays the one-off Metal shader-compilation cost up
	 * front); if the GPU is unavailable or the model cannot run on it - for example an
	 * older, frozen export whose constants cannot move off the CPU - the loader falls
	 * back to the CPU. A model that is to run on the GPU must be exported WITHOUT
	 * {@code torch.jit.freeze} (see scripts/export_torchscript.py, which now leaves
	 * freezing off by default).
	 *
	 * @param modelPathS - the path to the traced PyTorch (.pt) model file.
	 * @return the predictor which returns a flattened confidence surface, or null
	 *         if the model could not be loaded on any device.
	 */
	public Predictor<float[][][], float[]> loadSamWhistleModel(String modelPathS) {

		Path modelPath = Paths.get(modelPathS);
		Path modelDirectory = modelPath.getParent();
		String modelName = modelPath.getFileName().toString();

		modelDevice = null;

		//a dummy single-channel batch to warm up / validate the device.
		float[][][] warmup = new float[1][MODEL_INPUT_FREQ_BINS][MODEL_INPUT_TIME_STEPS];

		for (Device device : getCandidateDevices()) {
			Model loadedModel = null;
			try {
				loadedModel = Model.newInstance("SamWhistle", device);
				loadedModel.load(modelDirectory, modelName);

				Predictor<float[][][], float[]> predictor = loadedModel.newPredictor(new SamSpectrumTranslator());

				//run one inference to confirm the model actually runs on this device.
				predictor.predict(warmup);

				modelDevice = device;
				return predictor;

			} catch (Throwable e) {
				System.err.println("SamWhistleMask: could not run model on device " + device
						+ " (" + e.getMessage() + ") - trying the next device.");
				if (loadedModel != null) {
					loadedModel.close();
				}
			}
		}

		System.err.println("SamWhistleMask: failed to load the model on any device.");
		return null;
	}

	/**
	 * Human-readable name of the device the model is running on (e.g. "MPS", "CPU").
	 *
	 * @return the device name, or "unknown" if the model is not loaded.
	 */
	private String deviceName() {
		return modelDevice == null ? "unknown" : modelDevice.getDeviceType().toUpperCase();
	}

	/**
	 * The devices to try loading the model on, in order of preference. On Apple
	 * Silicon the Metal (MPS) GPU is tried first, then the CPU; on other platforms
	 * the CPU is used (the bundled PyTorch native library is CPU-only aside from
	 * Apple's integrated GPU).
	 *
	 * @return the ordered list of candidate devices.
	 */
	private static Device[] getCandidateDevices() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String arch = System.getProperty("os.arch", "").toLowerCase();
		boolean appleSilicon = os.contains("mac") && (arch.equals("aarch64") || arch.contains("arm"));

		if (appleSilicon) {
			return new Device[] { Device.of("mps", 0), Device.cpu() };
		}
		return new Device[] { Device.cpu() };
	}

}
