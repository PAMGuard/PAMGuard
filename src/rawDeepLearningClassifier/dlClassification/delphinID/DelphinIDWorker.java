package rawDeepLearningClassifier.dlClassification.delphinID;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SpectrumTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.JamArr;
import org.json.JSONArray;
import org.json.JSONObject;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import ai.djl.Model;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image.Whistle2ImageParams;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * The delphinID worker. This worker has two additional tasks compared to it's
 * parent class.
 * <ul>
 * <li>Read the the JSON parameters that transform that converts a group of clicks or whistles to a transform</li>
 * <li>Insert the custon transform at the start of the transforms list before data are transformed</li>
 * </ul>
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDWorker extends ArchiveModelWorker {

	/**
	 * Parameters for the whistle to image transform. 
	 */
	private DelphinIDTransform groupDataTransform = new DelphinIDTransform();

	/**
	 * Get the whislte to image parameters. 
	 * 
	 * @return
	 */
	public DelphinIDTransform getWhistleTransform() {
		return groupDataTransform;
	}


	@Override
	public void prepModel(StandardModelParams dlParams, DLControl dlControl) {
		//most of the model prep is done in the perent class. 
		super.prepModel(dlParams, dlControl);
				
		//now have to read the whsitle2image transform to get correct parameters for that. 
		String jsonString  = DLTransformsParser.readJSONString(new File(this.getModel().getAudioReprFile()));
		
		JSONObject jsonObject = new JSONObject(jsonString); 
		boolean transformOK = groupDataTransform.setJSONData(jsonObject); 
		
		((DelphinIDParams) dlParams).dataType = groupDataTransform.getDataType(); 

		if (!transformOK) {
			System.err.println("Error: could not find whsitle or click group transform in DelphinID JSON file. Model will not work.");
			this.setModel(null); // set model to null to make sure nothing works and errors are thrown
		}
		
		dlParams.binaryClassification = new boolean[dlParams.numClasses];
		for (int i=0; i<dlParams.numClasses; i++) {
			dlParams.binaryClassification[i]=true;
		}
	
	}

	@Override
	public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){
		
		
		/**
		 * Need to override this as the standard function assumes dataUnits are GroupedAudioData and 
		 * adds AudioData to first transforms and runs the transforms. We need to take whsiltes and do something
		 * completely different here. 
		 */
		
		//Get a list of of the model transforms. 
		ArrayList<DLTransform> modelTransforms = getModelTransforms();

		@SuppressWarnings("unchecked")
		ArrayList<SegmenterDetectionGroup> detectionGroups = (ArrayList<SegmenterDetectionGroup>) dataUnits;

		//the number of chunks. 
		int numChunks = detectionGroups.size(); 

		//data input into the model - a stack of spectrogram images. 
		float[][][] transformedDataStack = new float[numChunks][][]; 

		double[][] transformedData2; //spectrogram data
		
		
		for (int j=0; j<numChunks; j++) {
			
		
			groupDataTransform.setGroupDetectionData(detectionGroups.get(j), modelTransforms.get(0));

			//process all the transforms. 
			DLTransform transform = modelTransforms.get(0); 
			for (int i =0; i<modelTransforms.size(); i++) {
				transform = modelTransforms.get(i).transformData(transform); 
			}
			
			if (transform instanceof FreqTransform) {
				//Process whistle or click segment images
				//add a spectrogram to the stacl
				transformedData2 = ((FreqTransform) transform).getSpecTransfrom().getTransformedData(); 

				//a bit ugly but works - it is very important we tranpose the matrix!!
				transformedData2 = JamArr.transposeMatrix(transformedData2);
				transformedDataStack[j] = DLUtils.toFloatArray(transformedData2);
			}
			else {
				//process whistle or click segment spectra
				//add wavefrom to the stack = we make the 2nd dimesnion 1. 
				double[] spectrum = ((SpectrumTransform) transform).getSpectrum().getRealSpectrum();
				transformedDataStack[j] = new float[1][spectrum.length];
				transformedDataStack[j][0] = DLUtils.toFloatArray(spectrum); 
			}
			
		}


		return transformedDataStack;
	} 
	
	
//	private Struct imageStruct;
//	int count = 0;
//	/**
//	 * Tets by exporting results to a .mat file. 
//	 * @param data
//	 * @param aSegment
//	 */
//	private void addIMage2MatFile(double[][] data, SegmenterDetectionGroup aSegment) {
//		long dataStartMillis = 1340212413000L;
//
//		if (imageStruct==null) {
//			 imageStruct = Mat5.newStruct(100,1);
//		}
//		Matrix image = DLMatFile.array2Matrix(data);
//		imageStruct.set("image", count, image);
//		imageStruct.set("startmillis", count, Mat5.newScalar(aSegment.getSegmentStartMillis()));
//		imageStruct.set("startseconds", count, Mat5.newScalar((aSegment.getSegmentStartMillis()-dataStartMillis)/1000.));
//
//		count++;
//		
//		System.out.println("SAVED " +count + " TO MAT FILE");
//		
//		if (count==10) {
//			//create MatFile for saving the image data to. 
//			MatFile matFile = Mat5.newMatFile();
//			matFile.addArray("whistle_images", imageStruct);
//			//the path to the model
//			String matImageSave = "C:/Users/Jamie Macaulay/MATLAB Drive/MATLAB/PAMGUARD/deep_learning/delphinID/whistleimages_pg.mat";
//			try {
//				Mat5.writeToFile(matFile,matImageSave);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}


}
