package rawDeepLearningClassifier.dlClassification.genericModel;

import java.util.ArrayList;
import java.util.Arrays;

import org.jamdev.jdl4pam.animalSpot.*;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;

import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.segmenter.GroupedRawData;


/**
 * 
 * Runs the deep learning model and performs feature extraction.
 * <p>
 * 
 * @author Jamie Macaulay 
 *
 */
public abstract class DLModelWorker<T> {

	/**
	 * The maximum allowed queue size;
	 */
	public final static int MAX_QUEUE_SIZE = 10 ; 

	/**
	 * The model transforms for the data. 
	 */
	private ArrayList<DLTransform> modelTransforms;

	/**
	 * True to enable normalisation of results using softmax; 
	 */
	private boolean enableSoftMax = true;
	
	
	/**
	 * Convert a list of data units to a stack if images. 
	 * @param dataUnits - the data units. 
	 * @param sampleRate - the sample rate
	 * @param iChan - the channels
	 * @return a stack of images for input into a deep learning model. 
	 */
	public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){
		
		@SuppressWarnings("unchecked")
		ArrayList<GroupedRawData> rawDataUnits = (ArrayList<GroupedRawData>) dataUnits;
		 
		//the number of chunks. 
		int numChunks = rawDataUnits.size(); 

		//data input into the model - a stack of spectrogram images. 
		float[][][] transformedDataStack = new float[numChunks][][]; 
		
		//generate the spectrogram stack. 
		AudioData soundData; 
		double[][] transformedData2; //spec data
		double[] transformedData1;  //waveform data
		for (int j=0; j<numChunks; j++) {
		
			soundData  = new AudioData(rawDataUnits.get(j).getRawData()[iChan], sampleRate); 
			
			//			for (int i=0; i<modelTransforms.size(); i++) {
			//				System.out.println("Transfrom type: " + modelTransforms.get(i).getDLTransformType()); 
			//			}
			//set the sound in the first transform. 
			((WaveTransform) modelTransforms.get(0)).setWaveData(soundData); 

//			System.out.println("Model transforms:no. " + modelTransforms.size()+ "  input sounds len: " + soundData.getLengthInSeconds() 
//			+ " Decimate Params: " + ((WaveTransform) modelTransforms.get(0)).getParams()[0] + "max amplitude sound: " + PamArrayUtils.max(soundData.samples));

			DLTransform transform = modelTransforms.get(0); 
			for (int i =0; i<modelTransforms.size(); i++) {
				transform = modelTransforms.get(i).transformData(transform); 

//				//TEMP
//				if (transform instanceof FreqTransform) {
//					transformedData = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
//					System.out.println("DLModelWorker: transform : " + modelTransforms.get(i).getDLTransformType() + " "+ i + transformedData.length + "  " + transformedData[0].length + " minmax: " + PamArrayUtils.minmax(transformedData)[0] + " " + PamArrayUtils.minmax(transformedData)[1]);
//				}
				
//				//TEMP
//				if (transform instanceof WaveTransform) {
//					transformedData1 = ((WaveTransform) transform).getWaveData().getScaledSampleAmplitudes();
//					System.out.println("DLModelWorker: transform : " + modelTransforms.get(i).getDLTransformType() + " "+ i + "  " + transformedData1.length + "  " +  PamArrayUtils.minmax(transformedData1)[0] + " " + PamArrayUtils.minmax(transformedData1)[1]);
//				}
			}

			if (transform instanceof FreqTransform) {
				//add a spectrogram to the stacl
				transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
				transformedDataStack[j] = DLUtils.toFloatArray(transformedData2); 
			}
			else {
				//add wavefrom to the stack = we make the 2nd dimesnion 1. 
				transformedData1 = ((WaveTransform) transform).getWaveData().getScaledSampleAmplitudes(); 
				transformedDataStack[j] = new float[1][transformedData1.length];
				transformedDataStack[j][0] = DLUtils.toFloatArray(transformedData1); 
			}
		}
		return transformedDataStack;
	} 


	/**
	 * Run the initial data feature extraction and the model
	 * @param rawDataUnit - the raw data unit. This is a stack of data units to be classified either together or separately.  
	 * @param iChan - the channel to run the data on. 
	 * @return the model to run. 
	 */
	public synchronized ArrayList<T> runModel(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan) {

		try {
			//PamCalendar.isSoundFile(); 
			//create an audio data object from the raw data chunk
			long timeStart = System.nanoTime(); 
			
			float[][][] transformedDataStack  = dataUnits2ModelInput(dataUnits,  sampleRate,  iChan);

			//run the model. 
			float[] output = null; 
			long time1 = System.currentTimeMillis();
			
			output = runModel(transformedDataStack); 
//			System.out.println("Model out: " + PamArrayUtils.array2String(output, 2, ","));
			long time2 = System.currentTimeMillis();

			int numclasses = (int) (output.length/transformedDataStack.length); 

			//			System.out.println(PamCalendar.formatDBDateTime(rawDataUnits.get(0).getTimeMilliseconds(), true) + 
			//					" Time to run model: " + (time2-time1) + " ms for spec of len: " + transformedDataStack.length + 
			//					"output: " + output.length + " " + numclasses); 

			ArrayList<T> modelResults = new ArrayList<T>(); 
			float[] prob; 
			float[] classOut; 
			for (int i=0; i<transformedDataStack.length; i++) {
				
				/**
				 * This is super weird. Reading the documentation for copyOfRange the index from and index to are inclusive. So 
				 * to copy the first two elements indexfrom =0 and indexto = 1. But actually it seems that this should be indexfrom =0 and indexto =2. 
				 * So do not minus one form (i+1)*numclasses. This works but I'm confused as to why?
				 */
				classOut = Arrays.copyOfRange(output, i*numclasses, (i+1)*numclasses); 

//								System.out.println("The copyOfRange is: " + i*numclasses + " to " + ((i+1)*numclasses-1) + " class out len: " + classOut.length); 

				if (enableSoftMax) {
					prob = new float[classOut.length]; 
					for (int j=0; j<classOut.length; j++) {
						//python code for this. 
						//prob = torch.nn.functional.softmax(out).numpy()[n, 1]
						//pred = int(prob >= ARGS.threshold)		    	
						//softmax function
						prob[j] = (float) DLUtils.softmax(classOut[j], classOut); 
						//System.out.println("The probability is: " + j + ": " + prob[j]); 
					}
				}
				else {
					prob = classOut; 
				}
				
				//does this pass binary classification
				long timeEnd = System.nanoTime(); 

				T modelResult = makeModelResult(prob, (timeEnd-timeStart)/1000/1000/1000); 
				//soundSpotResult.setAnalysisTime((timeEnd-timeStart)/1000/1000/1000);

				modelResults.add(modelResult);
			}

			return modelResults;

		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Run the model on a stack of transformed data.
	 * @param transformedDataStack - the input data for the model where the outer array is the number of input images or wavforms.
	 * @return the prediction as a flattened array of probabilities for each class.
	 */
	public abstract float[] runModel(float[][][] transformedDataStack);
	
	/**
	 * Check whether a model is null or not. 
	 * @return true of the model is null. 
	 */
	public abstract boolean isModelNull();

	/**
	 * Make a model result from the probabilities and the time it took to run the model.
	 * @param prob - the probabilities for each class.
	 * @param time - the time taken to run the model.
	 * @return a model result object. 
	 */
	public abstract T makeModelResult(float[] prob, double time);

	/**
	 * Prepare the model for running. 
	 * @param standardModelParams - the parameters for the sound spot model.
	 * @param dlControl - the control object for the deep learning process.
	 * @return a status of the preparation of the model. 
	 */
	public abstract DLStatus prepModel(StandardModelParams standardModelParams, DLControl dlControl);


	/**
	 * Destroy the model. 
	 */
	public abstract void closeModel();

	//	public SoundSpotResult makeModelResult(Tensor output) {
	//
	//		//grab the results. 
	//		float[] prob = new float[(int) output.shape()[1]]; 
	//
	//
	//		for (int j=0; j<output.shape()[1]; j++) {
	//			//python code for this. 
	//			//	    	prob = torch.nn.functional.softmax(out).numpy()[n, 1]
	//			//                    pred = int(prob >= ARGS.threshold)		    	
	//			//softmax function
	//			prob[j] = (float) DLUtils.softmax(output.getDataAsFloatArray()[j], output.getDataAsFloatArray()); 
	//			System.out.println("The probability is: " + prob[j]); 
	//		}
	//
	//		SoundSpotResult soundSpotResult = new SoundSpotResult(prob); 
	//
	//		return soundSpotResult; 
	//	}

	/**
	 * Get the model transforms for the data. These are the transforms that are applied to the data before it is input into the model.
	 * @return the model transforms.
	 */
	public ArrayList<DLTransform> getModelTransforms() {
		return modelTransforms;
	}

	/**
	 * Set the model transforms for the data. These are the transforms that are applied to the data before it is input into the model.
	 * @param modelTransforms - the model transforms.
	 */
	public void setModelTransforms(ArrayList<DLTransform> modelTransforms) {
		this.modelTransforms = modelTransforms;
	}

	/**
	 * Convert the parameters saved in the sound spot model to DLtransform parameters. 
	 * @return the DLTransform parameters. 
	 */
	public ArrayList<DLTransform> model2DLTransforms(AnimalSpotParams dlParams) {

		ArrayList<DLTransform> transforms = DLTransformsFactory.makeDLTransforms(dlParams.dlTransforms); 

		//		//waveform transforms. 
		//		transforms.add(new WaveTransform(DLTransformType.DECIMATE, dlParams.sR)); 
		//		transforms.add(new WaveTransform(DLTransformType.PREEMPHSIS, dlParams.preemphases)); 
		//		//transforms.add(new WaveTransform(soundData, DLTransformType.TRIM, samplesChunk[0], samplesChunk[1])); 
		//
		//		//frequency transforms. 
		//		transforms.add(new FreqTransform(DLTransformType.SPECTROGRAM, dlParams.n_fft, dlParams.hop_length)); 
		//		transforms.add(new FreqTransform(DLTransformType.SPECCROPINTERP, dlParams.fmin, dlParams.fmax, dlParams.n_freq_bins)); 
		//		transforms.add(new FreqTransform(DLTransformType.SPEC2DB)); 
		//		transforms.add(new FreqTransform(DLTransformType.SPECNORMALISE, dlParams.min_level_dB, dlParams.ref_level_dB)); 
		//		transforms.add(new FreqTransform(DLTransformType.SPECCLAMP, dlParams.clampMin, dlParams.clampMax)); 


		return transforms; 

	}
	
	

	/**
	 * Check whether the results are normalised with a softmax function. 
	 * @return true if results are normalised using a softmax function
	 */
	public boolean isEnableSoftMax() {
		return enableSoftMax;
	}

	/**
	 * Set whether the results are normalised with a softmax function. 
	 * @param set to true if results are normalised using a softmax function
	 */
	public void setEnableSoftMax(boolean enableSoftMax) {
		this.enableSoftMax = enableSoftMax;
	}


}
