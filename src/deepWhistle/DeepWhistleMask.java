package deepWhistle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jamdev.jdl4pam.deepWhistle.DeepWhistleTest.DeepWhistleInfo;
import org.jamdev.jdl4pam.deepWhistle.SpectrumTranslator;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;

import PamUtils.complex.ComplexArray;

import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransformParams;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.JamArr;
import org.jamdev.jpamutils.spectrogram.SpecTransform;
import org.jamdev.jpamutils.spectrogram.Spectrogram;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.TranslateException;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;

public class DeepWhistleMask implements PamFFTMask {

	/**
	 * FFT length used in the model
	 */
	private DeepWhistleProcess maksedFFTProcess;

	public DeepWhistleMask(DeepWhistleProcess maksedFFTProcess) {
		this.maksedFFTProcess = maksedFFTProcess;
	}

	Predictor<float[][], float[]> specPredictor;

	//TEMP
	String modelPath = "/Users/jdjm/Dropbox/PAMGuard_dev/Deep_Learning/deepWhistle/DWC-I.pt";

	/**
	 * Model info
	 */
	private DeepWhistleInfo modelInfo;

	/**
	 * List of the transform parameters used in pre-processing
	 */
	private ArrayList<DLTransfromParams> transformParams;

	private ArrayList<DLTransform> transforms;

	//For saving debug info
	private DeepWhistleMatFile deepWhistleMatFile;

	public String matFilePath = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/silbido/pamguard_input_example.mat";

	int count = 0;

	@Override
	public boolean initMask() {

		if (specPredictor!=null) {
			//already initialised
			specPredictor.close();
		}

//		deepWhistleMatFile = new DeepWhistleMatFile();
//		deepWhistleMatFile.initMatFile();
//		count = 0;

		FFTDataBlock fftDataBlock = maksedFFTProcess.getInputFFTData();
		int fftLen = fftDataBlock.getFftLength();
		int fftHop = fftDataBlock.getFftHop();

		MaskedFFTParamters fftParams = this.maksedFFTProcess.getMaskFFTParams();
		specPredictor =  loadPyTorchdeepWhistleModel(modelPath,  88,  maksedFFTProcess.getUnitsToBuffer());

		if (specPredictor == null) {
			System.err.println("DeepWhistleMask: failed to load deepWhistle model from "+modelPath);
			return false;
		}

		//define some model info
		modelInfo = new DeepWhistleInfo(fftLen, fftHop, 5000.0f , 50000.0f , (float) fftParams.bufferSeconds);		


		//create the transforms
		transformParams = createTrasnforms( modelInfo);
		transforms =	DLTransformsFactory.makeDLTransforms(transformParams); 

		//now work out what the input shape for the model is going to be. 

		return true;
	}


	/**
	 * Create the transform params used in deepWhistle pre-processing
	 * @param modelInfo - the model info
	 * @return
	 */
	private ArrayList<DLTransfromParams> createTrasnforms(DeepWhistleInfo modelInfo){


		ArrayList<DLTransfromParams> dlTransformParamsArr = new ArrayList<DLTransfromParams>();

		//so that transforms are
		// spectrogram - create a spectrogram - note silbido does not use a window function weirdly...
		//then trim the frequency
		//multiply by log10. 
		///clamp the values between mac_clip and min_clip - this is between 0 and 6 in Silbido
		//transforms
		//Then do a min mac normalisation
		//	dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECTROGRAM, 1024,512)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECFREQTRIM, modelInfo.minFreq, modelInfo.maxFreq)); 
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_LOG10));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPEC_ADD, 2.5));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECCLAMP, 0, 6.));
		dlTransformParamsArr.add(new SimpleTransformParams(DLTransformType.SPECNORMALISE_MINIMAX, 0, 6)); 


		return dlTransformParamsArr; 
	}

	/**
	 * Set the FFT data into the transforms. 
	 * @return
	 */
	public ArrayList<DLTransfromParams> setFFTData(List<FFTDataUnit> batch, ArrayList<DLTransfromParams> transforms){
		return null;
	}




	@Override
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {
		//System.out.println("MaskedFFTProcess: processing batch of size START "+batch.get(0).getFftData().getReal(0));
		org.jamdev.jpamutils.spectrogram.ComplexArray[] fftDataArr = new org.jamdev.jpamutils.spectrogram.ComplexArray[batch.size()];

		for (int i = 0; i < batch.size(); i++) {
			fftDataArr[i] = convertComplexArray(batch.get(i).getFftData());
		}

		Spectrogram spectrogram = new Spectrogram(fftDataArr, 193 , 768, 96000);

		((FreqTransform) transforms.get(0)).setSpecTransfrom(new SpecTransform(spectrogram));
		//set the frequency limits
		((FreqTransform) transforms.get(0)).setFreqlims(new double[] {0.0, maksedFFTProcess.getInputFFTData().getSampleRate()/2.0});

		double[][] dataT3 = ((FreqTransform) transforms.get(0)).getSpecTransfrom().getTransformedData();

		//		System.out.println(" Spectrogram size: " + dataT3.length + " x " + dataT3[0].length);

		DLTransform transform = transforms.get(0); 
		for (int i=0; i<transforms.size(); i++) {
			//System.out.println("Transform " + i + ": " + transforms.get(i).getDLTransformType().getJSONString());

			double[][] dataT = ((FreqTransform) transform).getSpecTransfrom().getTransformedData();
			//System.out.println("Data shape: " + dataT.length + " x " + dataT[0].length + " min: " + JamArr.min(dataT) + " max: " + JamArr.max(dataT));

			transform = transforms.get(i).transformData(transform); 
		}

		double[] freqLimits=((FreqTransform) transform).getFreqlims();

		float[][] dataF =  DLUtils.toFloatArray(((FreqTransform) transform).getSpecTransfrom().getTransformedData());

//		/*** Temporarily saving stuff to mat file for debugging ***/
//		if (count<deepWhistleMatFile.getMaxNumStruct()) {
//			deepWhistleMatFile.addArray(dataF, "model_input", count, batch.get(0).getChannelBitmap());
//			count++;
//		}
//		else {
//			System.out.println("DeepWhistleMask: SAVE THE MAT FILE");
//			deepWhistleMatFile.saveMatFile(matFilePath);
//		}
//		/*********************************************************/


		//the model results - the model should return a mask of the same size as the input data - note - this is not the same 
		//as the FFT length - it is the trimmed length after frequency trimming.
		float[][] modelResults = null;

		modelResults = runPyTorchDeepWhislte(specPredictor, dataF); 

		//get the mask from the model results
		double[][] mask = JamArr.floatToDouble(modelResults);

		// delegate to helper that maps trimmed-frequency mask to full FFT bins
		return applyMask(batch, mask, freqLimits);

		//System.out.println("MaskedFFTProcess: processing batch of size DONE "+batch.get(0).getFftData().getReal(0));
	}

	private List<FFTDataUnit> applyMask(List<FFTDataUnit> batch, double[][] mask, double[] freqLims) {

		double sampleRate =  maksedFFTProcess.getInputFFTData().getSampleRate();

		//now apply the mask to each unit
		ComplexArray out;
		for (int i = 0; i < batch.size(); i++) {
			out = batch.get(i).getFftData();
			if (out == null) {
				System.err.println("MaskedFFTProcess: no FFT data in unit "+i+" of batch");
				continue;
			}
			for (int j = 0; j < out.length(); j++) {

				double maskVal = getMaskValueForBin(j, out.length(), sampleRate/2.0, mask[i], freqLims);

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
	 * Get the mask value for a given FFT bin by mapping to the frequency trimmed mask
	 * @param j - the FFT bin index
	 * @param sampleRate - the sample rate of the data
	 * @param mask - the frequency trimmed mask for the current time slice
	 * @param freqLims - the frequency limits used in trimming
	 */
	private double getMaskValueForBin(int j, int len, double nyquist, double[] mask, double[] freqLims) {

		//get the frequency for this bin
		double binFreq = ((double) j)/len * (nyquist);

		//System.out.println("Bin " + j + " freq: " + binFreq + " freqLims: " + freqLims[0] + " - " + freqLims[1]);

		//check if this frequency is within the trimmed limits
		if (binFreq < freqLims[0] || binFreq > freqLims[1]) {
			return getDefaultMaskValue();
		}

		//now we need to map this frequency to the index in the trimmed mask
		//the frequency lies between two frequent bins in the trimmed mask - find those bins

		//the fraction along the trimmed frequency range
		double percent = (binFreq - freqLims[0])/(freqLims[1]-freqLims[0]);

		int minIndex = (int) Math.floor(percent * (mask.length-1));
		int maxIndex = (int) Math.ceil(percent * (mask.length-1));

		//	System.out.println("MinIndex: " + minIndex + " MaxIndex: " + maxIndex);

		//now we have two indices - do a linear interpolation between them
		if (minIndex == maxIndex) {
			return mask[minIndex];
		} else {

			//weight the masks based on distance between the two mask frequency bins and the actual bin frequency
			double freqMin = freqLims[0] + ((double) minIndex)/(mask.length-1) * (freqLims[1]-freqLims[0]);
			double freqMax = freqLims[0] + ((double) maxIndex)/(mask.length-1) * (freqLims[1]-freqLims[0]);

			double weightMax = (binFreq - freqMin)/(freqMax - freqMin);
			double weightMin = 1.0 - weightMax;

			//			return weightMin * mask [minIndex] + weightMax * mask [maxIndex];
			return mask[minIndex];
		}

	}

	/**
	 * Get the default mask value for frequencies outside the trimmed range
	 * @return the default mask value
	 */
	private double getDefaultMaskValue() {
		//TODO
		return 1;
	}


	/**
	 * Convert from PamUtils complex array to jpamutils complex array
	 * @param fftData - the PamUtils complex array
	 * @return the jpamutils complex array
	 */
	private org.jamdev.jpamutils.spectrogram.ComplexArray convertComplexArray(ComplexArray fftData) {
		org.jamdev.jpamutils.spectrogram.ComplexArray complexArr = new org.jamdev.jpamutils.spectrogram.ComplexArray(fftData.length()*2);
		//System.out.println("DeepWhistleMask: convertComplexArray: length = " + fftData.length());
		for (int i=0; i<fftData.length(); i++) {
			complexArr.setReal(i, fftData.getReal(i));
			complexArr.setImag(i, fftData.getImag(i));
		}

		return complexArr;
	}


	/**
	 * Load the deepWhistle into memory and create a predictor that can be called to run the model. 
	 * @param modelPathS - the path to the PyTorch model file
	 * @param fftLen - the fft length used in the model
	 * @param fftNum - the number of runs (i.e. number of FFT
	 * @return the predictor which returns a flattened confidence surface. 
	 */
	public static Predictor<float[][], float[]>  loadPyTorchdeepWhistleModel(String modelPathS, long fftLen, long fftNum) {

		//System.out.println("Loading deepWhistle model: " + fftLen + " FFT length, " + fftNum + " no. FFT");

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

		System.out.println("Begin deepWhistle prediction: " + spectrogram.length + " x " +  spectrogram[0].length + " no. FFT");
		float[] output;
		try {

			float[][] input =JamArr.transposeMatrix(spectrogram);

			long start = System.currentTimeMillis();

			output = specPredictor.predict(input);

			float[][] confMap = JamArr.to2D(output,  input[0].length);

			long end = System.currentTimeMillis();

			System.out.println("End deepWhistle prediction: " + spectrogram.length + " in " + (end-start) + " millis");

			return JamArr.transposeMatrix(confMap);

		} catch (TranslateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null; 
	}


}
