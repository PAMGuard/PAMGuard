package deepWhistle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepWhistle.DeepWhistleTest.DeepWhistleInfo;
import org.jamdev.jdl4pam.deepWhistle.SpectrumTranslator;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.JamArr;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.TranslateException;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;


/**
 *
 * Implements the DeepWhistle model as a PAMGuard FFT mask. The model is downloaded from the PAMGuard deeplearningmodels GitHub repository
 *
 * @author Jamie Macaulay
 */
public class DeepWhistleMask extends AbstractFFTMask {

	/**
	 * Lower clip bound applied to the log-magnitude before normalisation
	 * (min_clip in the MATLAB silbido code).
	 */
	private static final double MIN_CLIP = 0.0;

	/**
	 * Upper clip bound applied to the log-magnitude before normalisation
	 * (max_clip in the MATLAB silbido code).
	 */
	private static final double MAX_CLIP = 6.0;

	/**
	 * The MATLAB silbido reference analysis window length in seconds (~2 ms).
	 * Used to compute the FFT-length term of the log-magnitude offset.
	 */
	private static final double MATLAB_WINDOW_SECONDS = 0.002;

	/**
	 * Bit scale (bits minus the sign bit) of the integer audio the model was
	 * trained on. MATLAB {@code dtDeepWhistle.m} scales all audio to a 16-bit
	 * integer equivalent, so the full-scale value is 2<sup>15</sup>.
	 */
	private static final int REFERENCE_BIT_SCALE = 15;

	/**
	 * Optional manual fine-tune (in log10 units) added to the computed
	 * log-magnitude offset. Leave at 0 unless re-validating the model input
	 * against the MATLAB reference (compare_java_model_input.m) shows the bulk
	 * of the distribution is consistently shifted.
	 */
	private static final double LOG_OFFSET_ADJUST = 0.0;

	Predictor<float[][], float[]> specPredictor;

	/**
	 * URL to the downloadable DeepWhistle model. The model is hosted as a release
	 * asset (a zip containing the model file and a README) in the PAMGuard
	 * deeplearningmodels repository on GitHub.
	 */
	public static final String MODEL_URL = "https://github.com/PAMGuard/deeplearningmodels/releases/download/1.0/deep_whistle_1.zip";

	/**
	 * Name of the model file to locate inside the downloaded archive.
	 */
	public static final String MODEL_FILE_NAME = "DWC-I.pt";

	//For saving debug info
	private DeepWhistleMatFile deepWhistleMatFile;

	public String matFilePath = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/silbido/pamguard_input_example.mat";

	int count = 0;


	public DeepWhistleMask(MaskedFFTProcess maksedFFTProcess) {
		super(maksedFFTProcess);
	}

	@Override
	public boolean initMask() {

		if (specPredictor != null) {
			//already initialised
			specPredictor.close();
		}

		MaskedFFTParamters fftParams = this.maksedFFTProcess.getMaskFFTParams();

		String modelPath = fftParams.modelPath;
		if (modelPath == null) {
			System.err.println("DeepWhistleMask: no model has been downloaded - model path is null. "
					+ "Open the settings and select a mask to download the model.");
			return false;
		}

		//define some model info and create the transforms
		initTransforms();

		//we need to work out the size of the input to the model - this is defined by the number of FFTs buffered
		long[] modelShape = getModelShape();
		System.out.println("DeepWhistleMask: model input shape: " + modelShape[0] + " x " + modelShape[1] + " x " + modelShape[2] + " x " + modelShape[3]);

		specPredictor = loadPyTorchdeepWhistleModel(modelPath, modelShape[3], maksedFFTProcess.getUnitsToBuffer());

		if (specPredictor == null) {
			System.err.println("DeepWhistleMask: failed to load deepWhistle model from " + modelPath);
			return false;
		}

		return true;
	}

	/**
	 * Create the transform params used in deepWhistle pre-processing.
	 * <p>
	 * This reproduces the MATLAB silbido pre-processing in <code>dtDeepWhistle.m</code>:
	 * <pre>
	 *   freq trim -&gt; log10(|FFT|) -&gt; clip to [0,6] -&gt; (x-0)/(6-0)
	 * </pre>
	 * The MATLAB code runs on <b>unscaled int16-equivalent</b> audio with <b>no window</b>,
	 * so its log-magnitudes naturally fall in [0,6]. PAMGuard instead supplies FFTs of
	 * <b>amplitude-normalised (&plusmn;1)</b> audio with a window applied, so the magnitudes
	 * are smaller by a fixed factor. We correct for this with a single additive offset in
	 * log space (see {@link #computeLogMagnitudeOffset()}) which is derived from the actual
	 * FFT settings (window gain, FFT length, sample rate) and therefore adjusts
	 * automatically if those settings change - unlike the previous hard-coded
	 * SPEC_ADD(2.1)/SPEC_PRODUCT(2) calibration which was tuned for one configuration.
	 *
	 * @param modelInfo - the model info
	 * @return the list of transform parameters.
	 */
	@Override
	protected ArrayList<DLTransfromParams> createTransforms(DeepWhistleInfo modelInfo){


		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//additive log-space offset which maps PAMGuard's amplitude-normalised, windowed FFT
		//magnitude onto the unscaled, un-windowed magnitude scale the model was trained on.
		double logOffset = computeLogMagnitudeOffset();

		System.out.println("DeepWhistleMask: log-magnitude offset = " + logOffset);

		//1) trim to the model frequency range
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECFREQTRIM, modelInfo.minFreq, modelInfo.maxFreq));
		//2) log10 of the magnitude spectrogram
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_LOG10));
		//3) shift onto the int16/no-window magnitude scale used by the MATLAB model
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_ADD, logOffset));
		//4) clamp to [min_clip, max_clip] (0 and 6 in silbido)
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECCLAMP, MIN_CLIP, MAX_CLIP));
		//5) min-max normalise using the same fixed bounds (NOT per-block) i.e. (x-0)/(6-0)
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECNORMALISE_MINIMAX, MIN_CLIP, MAX_CLIP));


		return dlTransformParamsArr;
	}

	/**
	 * Compute the additive offset (in log10 units) which maps PAMGuard's FFT magnitude
	 * onto the magnitude scale the MATLAB silbido model was trained on.
	 * <p>
	 * MATLAB ({@code dtDeepWhistle.m}) computes {@code log10(abs(fft(audio)))} where:
	 * <ul>
	 * <li>{@code audio} is unscaled and scaled to a 16-bit-integer equivalent (full scale
	 *     2<sup>15</sup>) for any bit depth, and</li>
	 * <li>no window function is applied, and</li>
	 * <li>the FFT length corresponds to a ~2 ms analysis window.</li>
	 * </ul>
	 * PAMGuard supplies FFTs of amplitude-normalised (&plusmn;1) audio with a window applied
	 * and a (possibly different) FFT length. Using broadband/noise statistics - which set the
	 * [0,6] clip range - the expected magnitudes relate as:
	 * <pre>
	 *   |X_matlab|   ~ &sigma;&middot;2^15 &middot; sqrt(N_matlab)                 (no window)
	 *   |X_pamguard| ~ &sigma;        &middot; sqrt(N_pamguard) &middot; windowGain  (windowed)
	 * </pre>
	 * so the required offset is
	 * <pre>
	 *   log10(|X_matlab|/|X_pamguard|) =
	 *       15&middot;log10(2) + 0.5&middot;log10(N_matlab/N_pamguard) - log10(windowGain)
	 * </pre>
	 * where {@code windowGain} is the RMS window gain reported by the FFT data block
	 * (1.0 for a rectangular window, ~0.61 for Hann). Because every term is read from the
	 * live FFT settings, the offset self-adjusts when the FFT length or window changes.
	 *
	 * @return the additive log10 offset.
	 */
	private double computeLogMagnitudeOffset() {

		FFTDataBlock fftDataBlock = maksedFFTProcess.getInputFFTData();

		int fftLen = fftDataBlock.getFftLength();
		double sampleRate = fftDataBlock.getSampleRate();

		//RMS gain of the window PAMGuard applied (1.0 = rectangular, ~0.61 = Hann).
		double windowGain = fftDataBlock.getDataGain(0);
		if (!(windowGain > 0)) {
			windowGain = 1.0;
		}

		//MATLAB analyses ~2 ms windows: N = round(fs*0.002)+1
		int matlabFFTLen = (int) Math.round(sampleRate * MATLAB_WINDOW_SECONDS) + 1;

		double offset = REFERENCE_BIT_SCALE * Math.log10(2.0)
				+ 0.5 * Math.log10(((double) matlabFFTLen) / ((double) fftLen))
				- Math.log10(windowGain)
				+ LOG_OFFSET_ADJUST;

		return offset;
	}

	@Override
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {

		//transform the batch of FFT data units into the model input format
		FreqTransform freqTransform = transformFFTBatch(batch);

		double[] freqLimits = freqTransform.getFreqlims();

		//The SPECFREQTRIM transform reports the *requested* frequency range (the model's
		//nominal 5-50 kHz) rather than the range actually kept. When the Nyquist frequency
		//is below the requested maximum (e.g. a 48 kHz file, Nyquist 24 kHz) the spectrogram
		//- and therefore the mask - only spans up to Nyquist. If we don't clamp the upper
		//limit here the mask is mapped to the wrong FFT bins (the whistles end up outside the
		//masked regions). See getMaskValueForBin which uses these limits.
		double nyquist = maksedFFTProcess.getInputFFTData().getSampleRate() / 2.0;
		freqLimits = new double[] { freqLimits[0], Math.min(freqLimits[1], nyquist) };

		float[][] dataF = DLUtils.toFloatArray(freqTransform.getSpecTransfrom().getTransformedData());

		//the model results - the model should return a mask of the same size as the input data - note - this is not the same
		//as the FFT length - it is the trimmed length after frequency trimming.
		float[][] modelResults = runPyTorchDeepWhislte(specPredictor, dataF);

		//get the mask from the model results
		double[][] mask = JamArr.floatToDouble(modelResults);

		// delegate to helper that maps trimmed-frequency mask to full FFT bins
		return applyMask(batch, mask, freqLimits);
	}


	/**
	 * Load the deepWhistle into memory and create a predictor that can be called to run the model.
	 * @param modelPathS - the path to the PyTorch model file
	 * @param fftLen - the fft length used in the model
	 * @param fftNum - the number of runs (i.e. number of FFT
	 * @return the predictor which returns a flattened confidence surface.
	 */
	public static Predictor<float[][], float[]>  loadPyTorchdeepWhistleModel(String modelPathS, long fftLen, long fftNum) {

		Path modelPath = Paths.get(modelPathS);

		//get the parent
		Path modelDirectory = modelPath.getParent();
		// Define the name of your ONNX model file
		String modelName = modelPath.getFileName().toString();

		Model loadedModel = Model.newInstance("DeepWhistle");
		try {

			loadedModel.load(modelDirectory, modelName);

			System.out.println("Model input description: " + loadedModel.describeInput());

			if (loadedModel.describeInput()!=null) {
				for (int i=0; i<loadedModel.describeInput().size() ;i++) {
					System.out.println(loadedModel.describeInput().get(i).getValue());
				}
			}

			System.out.println("Model properties: " + loadedModel.getProperties());

			SpectrumTranslator spectrumTranslator = new SpectrumTranslator(new Shape(new long[] {1, 1,fftLen, fftNum}), new Shape(new long[] {fftLen}));

			//predictor for the model if using images as input
			Predictor<float[][], float[]> specPredictor = loadedModel.newPredictor(spectrumTranslator);
			return specPredictor;

		} catch (MalformedModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}



	/**
	 * Simple function which loads up the deep PyTorch whistle model and then runs it on completely random data
	 */
	public static float[][] runPyTorchDeepWhislte(Predictor<float[][], float[]> specPredictor, float[][] spectrogram) {

		float[] output;
		try {

			float[][] input =JamArr.transposeMatrix(spectrogram);

			output = specPredictor.predict(input);

			float[][] confMap = JamArr.to2D(output,  input[0].length);

			return JamArr.transposeMatrix(confMap);

		} catch (TranslateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


}
