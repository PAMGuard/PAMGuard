package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.IOException;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import whistlesAndMoans.AbstractWhistleDataUnit;


/**
 * A delphinID test suite. 
 * 
 * @author Jamie Macaulay
 * 
 */
public class DelphinIDTest {


	public static class DelphinIDWorkerTest extends DelphinIDWorker {
		
		private float[][][] lastModelInput;


		public float[][][] dataUnits2ModelInput(ArrayList<? extends PamDataUnit> dataUnits, float sampleRate, int iChan){
			
			float[][][] data = super.dataUnits2ModelInput(dataUnits, sampleRate, iChan);
			
			this.lastModelInput = data;
		
			
			return data;
		}
		
		public float[][][] getLastModelInput() {
			return lastModelInput;
		}
		
	}

	/**
	 * Main class for running the test. 
	 * @param args - the arguments
	 */
	public static void main(String args[]) {

		double segLen = 4000.;
		double segHop = 1000.0;
		float sampleRate =96000;
		//unix time from sound file
		long dataStartMillis = 1340212413000L;

		//path to the .mat containing whistle contours. 
		String whistleContourPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_contours.mat";

		//the path to the model
//		String modelPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Dde415/whistle_4s_415_model.zip";

		
		//the path to the model
		String matImageSave = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/whistleimages_4s_415.mat";
		
		//create MatFile for saving the image data to. 
		MatFile matFile = Mat5.newMatFile();

		//get the whislte contours form a .mat file. 
		ArrayList<AbstractWhistleDataUnit> whistleContours = DelphinIDUtils.getWhistleContoursMAT(whistleContourPath);

		//segment the whistle detections
		//Note, delphinID starts from the first whislte and NOT the first file. 
		ArrayList<SegmenterDetectionGroup> segments =  DelphinIDUtils.segmentWhsitleData(whistleContours,  (long) (dataStartMillis+(9.565*1000.)), 
				segLen,  segHop);

		for (int i=0; i<segments.size(); i++) {
			System.out.println("Segment " + i + " contains " + segments.get(i).getSubDetectionsCount() + " whistles"); 
		}

		//prepare the model - this loads the zip file and loads the correct transforms. 
		DelphinIDWorkerTest model = DelphinIDUtils.prepDelphinIDModel(modelPath);
		model.setEnableSoftMax(false);

		
		//initialise strcuture for image data
		Struct imageStruct = Mat5.newStruct(segments.size(), 1);

		for (int i=0; i<segments.size(); i++) {

			//remember that the input is a stack of detections to be run by thge model at once - Here we want to do each one individually. 
			ArrayList<SegmenterDetectionGroup> aSegment = new  ArrayList<SegmenterDetectionGroup>();
			aSegment.add(segments.get(i)); 

			//the prediciton. 
			ArrayList<StandardPrediction> predicition = model.runModel(aSegment, sampleRate, 1);		

			float[] output =  predicition.get(0).getPrediction();
		
			System.out.println();
			System.out.print("Segment: " + i + " " + (aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000. + "s ");
			for (int j=0; j<output.length; j++) {
				System.out.print("  " + output[j]); 
			}
			
			Matrix image = DLMatFile.array2Matrix(PamArrayUtils.float2Double(model.getLastModelInput()[0]));
			imageStruct.set("image", i, image);
			imageStruct.set("startmillis", i, Mat5.newScalar(aSegment.get(0).getSegmentStartMillis()));
			imageStruct.set("startseconds", i, Mat5.newScalar((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.));
			imageStruct.set("prediction", i, DLMatFile.array2Matrix(PamArrayUtils.float2Double(output)));

		}

		matFile.addArray("whistle_images", imageStruct);
		// Serialize to disk using default configurations
		try {
			Mat5.writeToFile(matFile,matImageSave);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//		for (int i=0; i<whistleContours.size(); i++) {
		//			System.out.println("Whislte: " + i);
		//			PamArrayUtils.printArray(whistleContours.get(i).getFreqsHz());
		//		}

	}

}
