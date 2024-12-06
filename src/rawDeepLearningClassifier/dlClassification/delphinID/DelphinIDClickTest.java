package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jpamutils.spectrum.Spectrum;

import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.ClickDetectionMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.DetectionGroupMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDWhistleTest.DelphinIDWorkerTest;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

public class DelphinIDClickTest {

	public static void main(String args[]) {

//		String clicksMatPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";
//		String modelFile = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clickclassifier.zip";

		String clicksMatPath = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";
		String modelFile = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clickclassifier.zip";
		String matclickSave = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/click_spectrums.mat";

		DetectionGroupMAT<ClickDetectionMAT> clicks = DelphinIDUtils.getClicksMAT(clicksMatPath);

//		-----basic tests to check transforms----
//		test a single segment. 
				String matFileout = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/clickspectrum.mat";
				float[][] output = testDelphinIDClickSegment(matFileout, clicks); 
		
				//run the model
		
//				String matFileout2 = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/clickspectrum_results.mat";
//		
//				testDelphinIDModelRaw(modelFile, output, matFileout2); 


		//---complete test on the model and transforms------
		//float[][] results = testDelphinIDClickModel( modelFile,	clicks, matclickSave ); 

	}


	/****---------------------1D Click Spectrum Tests---------------------****/
	/*
	/*
	/*
	/****------------------------------------------------------------------****/


	public static float[][] testDelphinIDClickModel(String modelPath,	DetectionGroupMAT<ClickDetectionMAT> clicks, String matClickSave) {

		Path path = Paths.get(modelPath);


		double segLen = 4000.;
		double segHop = 1000.0;
		int minclicks = 5; 

		//load the model
		try {

			//now feed segments directly into model to test. 

			float sampleRate = (float) clicks.getSampleRate();
			long dataStartMillis = clicks.getFileDataStart();

			
			Float hardSampleRate = Float.valueOf((float) clicks.sampleRate); 
			//segment the whistle detections
			//Note, delphinID starts from the first whistle and NOT the first file. 
			ArrayList<SegmenterDetectionGroup> segments  = DelphinIDUtils.segmentDetectionData(clicks.getDetections(), dataStartMillis, segLen,  segHop, hardSampleRate);

			for (int i=0; i<segments.size(); i++) {
				System.out.println("Segment " + i + " contains " + segments.get(i).getSubDetectionsCount() + " clicks"); 
			}

			//prepare the model - this loads the zip file and loads the correct transforms. 
			DelphinIDWorkerTest model = DelphinIDUtils.prepDelphinIDModel(path.toAbsolutePath().toString());
			model.setEnableSoftMax(false);


			//initialise strcuture for image data
			Struct imageStruct = Mat5.newStruct(segments.size(), 1);

			for (int i=0; i<segments.size(); i++) {

				//remember that the input is a stack of detections to be run by the model at once - Here we want to do each one individually. 
				ArrayList<SegmenterDetectionGroup> aSegment = new  ArrayList<SegmenterDetectionGroup>();
				
				aSegment.add(segments.get(i)); 

				if (segments.get(i).getSubDetectionsCount()>minclicks) {
					//the prediction. 
					ArrayList<StandardPrediction> predicition = model.runModel(aSegment, sampleRate, 1);		

					float[] output =  predicition.get(0).getPrediction();

					System.out.print(String.format("Segment: %d no. clicks %d %.4f s" , i , segments.get(i).getSubDetectionsCount() ,((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.)));
					for (int j=0; j<output.length; j++) {
						System.out.print(String.format( " %.4f" , output[j])); 
					}
					System.out.println();

					Matrix modelinput = DLMatFile.array2Matrix(PamArrayUtils.float2Double(model.getLastModelInput()[0]));
					imageStruct.set("modelinput", i, modelinput);
					imageStruct.set("startmillis", i, Mat5.newScalar(aSegment.get(0).getSegmentStartMillis()));
					imageStruct.set("startseconds", i, Mat5.newScalar((aSegment.get(0).getSegmentStartMillis()-dataStartMillis)/1000.));
					imageStruct.set("prediction", i, DLMatFile.array2Matrix(PamArrayUtils.float2Double(output)));
				}
				else {
					System.out.println(String.format("Segment: %d no. clicks %d %s" , i , segments.get(i).getSubDetectionsCount(), "-------------------------"));
				}

			}
			
			//create MatFile for saving the image data to. 
			MatFile matFile = Mat5.newMatFile();
			matFile.addArray("click_model_inputs", imageStruct);

			if (matClickSave!=null) {
				// Serialize to disk using default configurations
				try {
					Mat5.writeToFile(matFile,matClickSave);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;

	}





	private static void testDelphinIDModelRaw(String modelPath, float[][] rawModelInput, String matFileout) {
		// TODO Auto-generated method stub

		Path path = Paths.get(modelPath);

		//load the model
		SimpleArchiveModel model;
		try {
			model = new SimpleArchiveModel(new File(path.toString()));

			float[] outputJava;
			for (int i=0; i<rawModelInput.length ; i++) {
				float[][] input = new float[1][];
				input[0] = rawModelInput[i];
				outputJava = model.runModel(input);

				System.out.print(i+" :"); 

				PamArrayUtils.printArray(outputJava, true);
				System.out.println("");
			}



		} catch (MalformedModelException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * This test runs delphinID on one 4 second window from whistle contours saved
	 * in a mat file. 
	 * 
	 * @return true if the test is passed. 
	 */
	public static float[][] testDelphinIDClickSegment(String matFileout, DetectionGroupMAT<ClickDetectionMAT> clicks ) {


		//		String clicksMatPath = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";

		double segLen = 4000.;
		double segHop = 1000.0;
		double startSeconds = 1.50355; //seconds to start segments (so we can compare to Python)
		double[] freqLimits = new double[] {10000., 40188.};
		int fftLen = 512; 

		// Create MAT file with a scalar in a nested struct
		MatFile matFile;
		int sum = 0;
		try {

			System.out.println("Total clicks: " + clicks.getDetections().size() + " First click: " + (clicks.getDetections().get(0).getStartSample()/clicks.getSampleRate()) + "s");


			long dataStartMillis = clicks.getFileDataStart();

			dataStartMillis = (long) (dataStartMillis+(startSeconds*1000.));

			//split the clicks into segments
			ArrayList<SegmenterDetectionGroup> segments  = DelphinIDUtils.segmentDetectionData(clicks.getDetections(), dataStartMillis, segLen,  segHop);


			Struct clkStruct = Mat5.newStruct(1, segments.size());

			float[][] output = new float[segments.size()][];
			for (int i=0; i<segments.size(); i++) {

				sum += segments.get(i).getSubDetectionsCount() ;

				ArrayList<PamDataUnit<?, ?>> clicksSeg = segments.get(i).getSubDetections();
				Spectrum spectrum  = Clicks2Spectrum.clicks2Spectrum(clicksSeg, (float) clicks.getSampleRate(), fftLen); 
				clkStruct.set("averagespectrum", i, DLMatFile.array2Matrix(spectrum.getRealSpectrum())); 
				clkStruct.set("nclks", i, Mat5.newScalar(clicksSeg.size())); 

				//System.out.println("Segment average" + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum + "  spectrum " + spectrum.length() + "  " + spectrum.getRealSpectrum()[0]);

				//convert to dB
				spectrum = spectrum.spectrumdB(true);

				//System.out.println("Segment dB" + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum + "  spectrum " + spectrum.length() + "  " + spectrum.getRealSpectrum()[0]);

				//smooth spectrum
				spectrum = spectrum.smoothSpectrum(3);

				//System.out.println("Segment smooth" + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum + "  spectrum " + spectrum.length() + "  " + spectrum.getRealSpectrum()[0]);

				//trim spectrum
				spectrum = spectrum.trimSpectrum(freqLimits);

				//System.out.println("Segment trim" + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum + "  spectrum " + spectrum.length() + "  " + spectrum.getRealSpectrum()[0]);

				//normalise spectrum row sum
				spectrum = spectrum.normaliseSpectrumSum();

				//down sample the spectrum.
				spectrum.downSampleSpectrumMean(2);

				System.out.println("Segment " + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum + "  spectrum " + spectrum.length() + "  " + spectrum.getRealSpectrum()[0]);

				clkStruct.set("transformedspectrum", i, DLMatFile.array2Matrix(spectrum.getRealSpectrum())); 

				//run the model
				float[] whistleSpectrumF = PamArrayUtils.double2Float(spectrum.getRealSpectrum());

				output[i] = whistleSpectrumF;

			}


			//write some output data for plotting if there is an output file set. 
			if (matFileout!=null){
				MatFile matFileWrite = Mat5.newMatFile()
						.addArray("spectrumJava",clkStruct)
						.addArray("filedate", Mat5.newScalar(PamCalendar.millistoDateNum(clicks.getFileDataStart()))); 

				Mat5.writeToFile(matFileWrite, matFileout);
			}

			return output;


		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}


}
