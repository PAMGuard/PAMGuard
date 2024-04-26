package rawDeepLearningClassifier.dlClassification.delphinID;


import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.segmenter.GroupedRawData;


public class DelphinIDWorker extends  ArchiveModelWorker {

	
	@Override
	public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){
		
		ArrayList<DLTransform> modelTransforms = getModelTransforms();

		@SuppressWarnings("unchecked")
		ArrayList<GroupedRawData> whistleGroups = (ArrayList<GroupedRawData>) dataUnits;
		 
		//the number of chunks. 
		int numChunks = whistleGroups.size(); 

		//data input into the model - a stack of spectrogram images. 
		float[][][] transformedDataStack = new float[numChunks][][]; 
//		
//		//generate the spectrogram stack. 
//		AudioData soundData; 
//		double[][] transformedData2; //spec data
//		double[] transformedData1;  //waveform data
//		for (int j=0; j<numChunks; j++) {
//		
//			soundData  = new AudioData(rawDataUnits.get(j).getRawData()[iChan], sampleRate); 
//			
//			//			for (int i=0; i<modelTransforms.size(); i++) {
//			//				System.out.println("Transfrom type: " + modelTransforms.get(i).getDLTransformType()); 
//			//			}
//			//set the sound in the first transform. 
//			((WaveTransform) modelTransforms.get(0)).setWaveData(soundData); 
//
////			System.out.println("Model transforms:no. " + modelTransforms.size()+ "  input sounds len: " + soundData.getLengthInSeconds() 
////			+ " Decimate Params: " + ((WaveTransform) modelTransforms.get(0)).getParams()[0] + "max amplitude sound: " + PamArrayUtils.max(soundData.samples));
//
//			DLTransform transform = modelTransforms.get(0); 
//			for (int i =0; i<modelTransforms.size(); i++) {
//				transform = modelTransforms.get(i).transformData(transform); 
////				//TEMP
////				if (transform instanceof FreqTransform) {
////					transformedData = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
////					System.out.println("DLModelWorker: transform : " + modelTransforms.get(i).getDLTransformType() + " "+ i + transformedData.length + "  " + transformedData[0].length + " minmax: " + PamArrayUtils.minmax(transformedData)[0] + " " + PamArrayUtils.minmax(transformedData)[1]);
////				}
//			}
//
//			if (transform instanceof FreqTransform) {
//				//add a spectrogram to the stacl
//				transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 
//				transformedDataStack[j] = DLUtils.toFloatArray(transformedData2); 
//			}
//			else {
//				//add wavefrom to the stack = we make the 2nd dimesnion 1. 
//				transformedData1 = ((WaveTransform) transform).getWaveData().getScaledSampleAmplitudes(); 
//				transformedDataStack[j] = new float[1][transformedData1.length];
//				transformedDataStack[j][0] = DLUtils.toFloatArray(transformedData1); 
//			}
//		}
		
		
		return transformedDataStack;
	} 

	
}
