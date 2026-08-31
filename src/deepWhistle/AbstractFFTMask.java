package deepWhistle;

import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepWhistle.DeepWhistleTest.DeepWhistleInfo;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.spectrogram.SpecTransform;
import org.jamdev.jpamutils.spectrogram.Spectrogram;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * Shared base class for {@link PamFFTMask} implementations that generate a
 * time-frequency confidence mask from a deep learning model and apply it to a
 * buffered batch of {@link FFTDataUnit}s.
 * <p>
 * The class holds all the model-independent plumbing:
 * <ul>
 * <li>converting PAMGuard {@link ComplexArray} FFT slices into a jpamutils
 * {@link Spectrogram} and running the pre-processing transform chain,</li>
 * <li>working out the model input shape from the transforms, and</li>
 * <li>mapping the (frequency-trimmed) mask returned by the model back onto the
 * full set of FFT bins and multiplying it into the FFT data.</li>
 * </ul>
 * Concrete subclasses (e.g. {@link DeepWhistleMask}, {@link SamWhistleMask})
 * provide the model-specific pieces: the pre-processing transform list
 * ({@link #createTransforms(DeepWhistleInfo)}), model loading
 * ({@link #initMask()}) and running the model over a batch
 * ({@link #applyMask(List)}).
 *
 * @author Jamie Macaulay
 */
public abstract class AbstractFFTMask implements PamFFTMask {

	/**
	 * Default mask value for frequencies outside the trimmed range.
	 */
	protected static final double DEFAULT_MASK_VALUE = 0;

	/**
	 * The masked FFT process the mask belongs to.
	 */
	protected final MaskedFFTProcess maksedFFTProcess;

	/**
	 * Model info (FFT length / hop, frequency range and chunk length).
	 */
	protected DeepWhistleInfo modelInfo;

	/**
	 * List of the transform parameters used in pre-processing.
	 */
	protected ArrayList<DLTransfromParams> transformParams;

	/**
	 * The instantiated pre-processing transforms.
	 */
	protected ArrayList<DLTransform> transforms;

	public AbstractFFTMask(MaskedFFTProcess maksedFFTProcess) {
		this.maksedFFTProcess = maksedFFTProcess;
	}

	/**
	 * Create the pre-processing transform parameters for this model.
	 *
	 * @param modelInfo - the model info (frequency range, FFT settings).
	 * @return the ordered list of transform parameters.
	 */
	protected abstract ArrayList<DLTransfromParams> createTransforms(DeepWhistleInfo modelInfo);

	/**
	 * The lowest frequency (Hz) the model was trained on. The spectrogram is
	 * trimmed to [{@link #getMinFreq()}, {@link #getMaxFreq()}] before being passed
	 * to the model.
	 *
	 * @return the minimum model frequency in Hz.
	 */
	protected float getMinFreq() {
		return 5000.0f;
	}

	/**
	 * The highest frequency (Hz) the model was trained on.
	 *
	 * @return the maximum model frequency in Hz.
	 */
	protected float getMaxFreq() {
		return 50000.0f;
	}

	/**
	 * Build {@link #modelInfo} and instantiate the pre-processing transforms. This
	 * should be called by a subclass's {@link #initMask()} before it loads the
	 * model (the model input shape depends on the transforms - see
	 * {@link #getModelShape()}).
	 */
	protected void initTransforms() {
		FFTDataBlock fftDataBlock = maksedFFTProcess.getInputFFTData();
		int fftLen = fftDataBlock.getFftLength();
		int fftHop = fftDataBlock.getFftHop();

		MaskedFFTParamters fftParams = this.maksedFFTProcess.getMaskFFTParams();

		modelInfo = new DeepWhistleInfo(fftLen, fftHop, getMinFreq(), getMaxFreq(), (float) fftParams.bufferSeconds);

		transformParams = createTransforms(modelInfo);
		transforms = DLTransformsFactory.makeDLTransforms(transformParams);
	}

	/**
	 * Get the DeepWhistle parameters from the process. Both the DeepWhistle and SAM
	 * masks share the same parameter object (they both only need the confidence
	 * threshold).
	 *
	 * @return the DeepWhistle parameters.
	 */
	protected DeepWhistleParamters getDeepWhistleParameters() {
		return (DeepWhistleParamters) maksedFFTProcess.getMaskFFTParams();
	}

	/**
	 * Get the model input shape. The model expects input of shape
	 * [1, channels, freqBins, timeSteps] but the number of frequency bins depends
	 * on the frequency trimming step, sample rate etc. so we work it out here by
	 * running the transforms over a dummy spectrogram.
	 *
	 * @return the model input shape as [1, 1, freqBins, timeSteps].
	 */
	protected long[] getModelShape() {

		//get the number of FFT's
		int numFFT = maksedFFTProcess.getUnitsToBuffer();

		//create a spectrogram with dummy data to work out the size after transforms
		org.jamdev.jpamutils.spectrogram.ComplexArray[] fftDataArr = new org.jamdev.jpamutils.spectrogram.ComplexArray[numFFT];

		for (int i = 0; i < numFFT; i++) {
			fftDataArr[i] = dummyComplexArray(getFFTLength());
		}

		FreqTransform freqTransform = transformFFTBatch(fftDataArr);

		float[][] dataF = DLUtils.toFloatArray(freqTransform.getSpecTransfrom().getTransformedData());

		return new long[] { 1, 1, dataF.length, dataF[0].length };
	}

	/**
	 * Create a dummy complex array with all values set to 1.0 + 0.0j.
	 *
	 * @param fftLength - the length of the FFT.
	 * @return the dummy complex array.
	 */
	protected org.jamdev.jpamutils.spectrogram.ComplexArray dummyComplexArray(int fftLength) {
		org.jamdev.jpamutils.spectrogram.ComplexArray complexArr = new org.jamdev.jpamutils.spectrogram.ComplexArray(fftLength);
		for (int i = 0; i < fftLength; i++) {
			complexArr.setReal(i, 1.0);
			complexArr.setImag(i, 0.0);
		}
		return complexArr;
	}

	/**
	 * Transform a batch of FFT data units into the model input format.
	 *
	 * @param batch - the batch of FFT data units.
	 * @return the frequency transform containing the transformed data.
	 */
	protected FreqTransform transformFFTBatch(List<FFTDataUnit> batch) {
		//this is a bit clunky - we need to convert from PamUtils complex array to jpamutils complex array. We cannot use a PamUtils complex
		//array in jpam because it is independent of PAMGuard and so cannot reference PamGuard classes.
		org.jamdev.jpamutils.spectrogram.ComplexArray[] fftDataArr = new org.jamdev.jpamutils.spectrogram.ComplexArray[batch.size()];

		for (int i = 0; i < batch.size(); i++) {
			fftDataArr[i] = convertComplexArray(batch.get(i).getFftData());
		}

		return transformFFTBatch(fftDataArr);
	}

	/**
	 * Transform a batch of FFT data units into the model input format.
	 *
	 * @param fftDataArr - the batch of FFT data units as jpamutils complex arrays.
	 * @return the frequency transform containing the transformed data.
	 */
	protected FreqTransform transformFFTBatch(org.jamdev.jpamutils.spectrogram.ComplexArray[] fftDataArr) {

		Spectrogram spectrogram = new Spectrogram(fftDataArr, getFFTLength(), getFFTHop(), getSampleRate());

		((FreqTransform) transforms.get(0)).setSpecTransfrom(new SpecTransform(spectrogram));
		//set the frequency limits
		((FreqTransform) transforms.get(0)).setFreqlims(new double[] { 0.0, maksedFFTProcess.getInputFFTData().getSampleRate() / 2.0 });

		DLTransform transform = transforms.get(0);
		for (int i = 0; i < transforms.size(); i++) {
			transform = transforms.get(i).transformData(transform);
		}

		return ((FreqTransform) transform);
	}

	/**
	 * Multiply a (frequency-trimmed) mask into a batch of FFT data units. The mask
	 * is indexed as [timeUnit][trimmedFreqBin] and is mapped onto the full set of
	 * FFT bins by {@link #getMaskValueForBin}.
	 *
	 * @param batch - the batch of FFT data units (modified in place).
	 * @param mask - the mask [timeUnit][trimmedFreqBin], values in [0, 1].
	 * @param freqLims - the frequency limits (Hz) the mask spans.
	 * @return the batch with the mask applied.
	 */
	protected List<FFTDataUnit> applyMask(List<FFTDataUnit> batch, double[][] mask, double[] freqLims) {

		double sampleRate = maksedFFTProcess.getInputFFTData().getSampleRate();

		double confidenceThreshold = getDeepWhistleParameters().confidenceThreshold;

		//now apply the mask to each unit
		ComplexArray out;
		for (int i = 0; i < batch.size(); i++) {
			out = batch.get(i).getFftData();
			if (out == null) {
				System.err.println("MaskedFFTProcess: no FFT data in unit " + i + " of batch");
				continue;
			}
			for (int j = 0; j < out.length(); j++) {

				double maskVal = getMaskValueForBin(j, out.length(), sampleRate / 2.0, mask[i], freqLims);

				if (maskVal < confidenceThreshold) {
					maskVal = 0.0;
				}

				//to apply a mask must multiply both real and imaginary parts by the mask value
				double re = out.getReal(j) * maskVal;
				out.setReal(j, re);
				double im = out.getImag(j) * maskVal;
				out.setImag(j, im);
			}

		}

		return batch;
	}

	/**
	 * Get the mask value for a given FFT bin by mapping to the trimmed frequency
	 * mask.
	 *
	 * @param j - the FFT bin index.
	 * @param len - the total number of FFT bins.
	 * @param nyquist - the nyquist frequency (sample rate / 2).
	 * @param mask - the trimmed frequency mask for this time unit.
	 * @param freqLims - the frequency limits used in trimming.
	 * @return the mask value for this bin.
	 */
	protected double getMaskValueForBin(int j, int len, double nyquist, double[] mask, double[] freqLims) {

		//get the frequency for this bin
		double binFreq = ((double) j) / len * (nyquist);
		double centerFreq = binFreq + (nyquist / len) / 2.0;

		//check if this frequency is within the trimmed limits
		if (centerFreq < freqLims[0] || centerFreq > freqLims[1]) {
			return getDefaultMaskValue();
		}

		//now we need to map this frequency to the index in the trimmed mask
		//the frequency lies between two frequency bins in the trimmed mask - find those bins

		//the fraction along the trimmed frequency range
		double percent = (centerFreq - freqLims[0]) / (freqLims[1] - freqLims[0]);

		int minIndex = (int) Math.floor(percent * (mask.length - 1));
		int maxIndex = (int) Math.ceil(percent * (mask.length - 1));

		//now we have two indices - do a linear interpolation between them
		if (minIndex == maxIndex) {
			return mask[minIndex];
		} else {

			//weight the masks based on distance between the two mask frequency bins and the actual bin frequency
			double freqMin = freqLims[0] + ((double) minIndex) / (mask.length - 1) * (freqLims[1] - freqLims[0]);
			double freqMax = freqLims[0] + ((double) maxIndex) / (mask.length - 1) * (freqLims[1] - freqLims[0]);

			double weightMax = (centerFreq - freqMin) / (freqMax - freqMin);
			double weightMin = 1.0 - weightMax;

			return weightMin * mask[minIndex] + weightMax * mask[maxIndex];
		}

	}

	/**
	 * Get the default mask value for frequencies outside the trimmed range.
	 *
	 * @return the default mask value.
	 */
	protected double getDefaultMaskValue() {
		return DEFAULT_MASK_VALUE;
	}

	/**
	 * Convert from PamUtils complex array to jpamutils complex array.
	 *
	 * @param fftData - the PamUtils complex array.
	 * @return the jpamutils complex array.
	 */
	protected org.jamdev.jpamutils.spectrogram.ComplexArray convertComplexArray(ComplexArray fftData) {
		org.jamdev.jpamutils.spectrogram.ComplexArray complexArr = new org.jamdev.jpamutils.spectrogram.ComplexArray(fftData.length() * 2);
		for (int i = 0; i < fftData.length(); i++) {
			complexArr.setReal(i, fftData.getReal(i));
			complexArr.setImag(i, fftData.getImag(i));
		}

		return complexArr;
	}

	protected float getSampleRate() {
		return maksedFFTProcess.getInputFFTData().getSampleRate();
	}

	protected int getFFTHop() {
		return maksedFFTProcess.getInputFFTData().getFftHop();
	}

	protected int getFFTLength() {
		return maksedFFTProcess.getInputFFTData().getFftLength();
	}

}
