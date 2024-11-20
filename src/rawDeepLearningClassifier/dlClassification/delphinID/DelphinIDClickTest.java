package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamArrayUtils;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.WhistleGroup;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDWhistleTest.DelphinIDWorkerTest;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import whistlesAndMoans.AbstractWhistleDataUnit;

public class DelphinIDClickTest {

	
	
	/****---------------------1D Whistle Spectrum Tests---------------------****/
	/*
	/*
	/*
	/****------------------------------------------------------------------****/
	
	/**
	 * Test a model including JSON transforms etc. This function tests a 2D Image model. 
	 * @param matImageSave - the MATLAB image
	 * @return true of the test is passed
	 */
	private static boolean testDelphinIDSpectrumModel(String matImageSave) {
		double segLen = 4000.;
		double segHop = 1000.0;
		double startSeconds = 9.898656; //seconds to start segments (so we can compare to Python)
		//unix time from sound file
		long dataStartMillis = 1340212413000L;

		//path to the .mat containing whistle contours. 
		String whistleContourPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/whistle_contours_20200918_123234.mat";

		//the path to the model
		//String modelPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/whistleclassifier.zip";
		//		String modelPath =  "./src/test/resources/rawDeepLearningClassifier/DelphinID/whistle_4s_415_model.zip";

		boolean whistleOK = runClickModel( modelPath,  whistleContourPath,  matImageSave,  startSeconds,  segLen,  segHop);
		
		return whistleOK;
	}

	private static boolean runClickModel(String modelPath, String whistleContourPath, String matImageSave,
			double startSeconds, double segLen, double segHop) {
		//create MatFile for saving the image data to. 
		MatFile matFile = Mat5.newMatFile();

		//get the whislte contours form a .mat file. 
		WhistleGroup whistlegroup = DelphinIDUtils.getWhistleContoursMAT(whistleContourPath);
		ArrayList<AbstractWhistleDataUnit> whistleContours = whistlegroup.getWhistle();
		float sampleRate = (float) whistlegroup.getSampleRate();
		long dataStartMillis = whistlegroup.getFileDataStart();

		//segment the whistle detections
		//Note, delphinID starts from the first whistle and NOT the first file. 
		ArrayList<SegmenterDetectionGroup> segments =  DelphinIDUtils.segmentWhsitleData(whistleContours,  (long) (dataStartMillis+(startSeconds*1000.)), 
				segLen,  segHop, (float) whistlegroup.getSampleRate());

		for (int i=0; i<segments.size(); i++) {
			System.out.println("Segment " + i + " contains " + segments.get(i).getSubDetectionsCount() + " whistles"); 
		}

		//prepare the model - this loads the zip file and loads the correct transforms. 
		Path path = Paths.get(modelPath);
		DelphinIDWorkerTest model = DelphinIDUtils.prepDelphinIDModel(path.toAbsolutePath().toString());
		model.setEnableSoftMax(false);


		//initialise strcuture for image data
		Struct imageStruct = Mat5.newStruct(segments.size(), 1);

		for (int i=0; i<segments.size(); i++) {

			//remember that the input is a stack of detections to be run by the model at once - Here we want to do each one individually. 
			ArrayList<SegmenterDetectionGroup> aSegment = new  ArrayList<SegmenterDetectionGroup>();
			aSegment.add(segments.get(i)); 

			if (segments.get(i).getSubDetectionsCount()>0) {
			//the prediction. 
			ArrayList<StandardPrediction> predicition = model.runModel(aSegment, sampleRate, 1);		

			float[] output =  predicition.get(0).getPrediction();

			System.out.println();
			System.out.print(String.format("Segment: %d %.4f s" , i ,((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.)));
			for (int j=0; j<output.length; j++) {
				System.out.print(String.format( " %.4f" , output[j])); 
			}

			Matrix modelinput = DLMatFile.array2Matrix(PamArrayUtils.float2Double(model.getLastModelInput()[0]));
			imageStruct.set("modelinput", i, modelinput);
			imageStruct.set("startmillis", i, Mat5.newScalar(aSegment.get(0).getSegmentStartMillis()));
			imageStruct.set("startseconds", i, Mat5.newScalar((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.));
			imageStruct.set("prediction", i, DLMatFile.array2Matrix(PamArrayUtils.float2Double(output)));
			}

		}

		matFile.addArray("whistle_model_inputs", imageStruct);

		if (matImageSave!=null) {
			// Serialize to disk using default configurations
			try {
				Mat5.writeToFile(matFile,matImageSave);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//		for (int i=0; i<whistleContours.size(); i++) {
		//			System.out.println("Whislte: " + i);
		//			PamArrayUtils.printArray(whistleContours.get(i).getFreqsHz());
		//		}
		return true;
	}
	
	
}
