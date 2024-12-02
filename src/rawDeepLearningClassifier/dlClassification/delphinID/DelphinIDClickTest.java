package rawDeepLearningClassifier.dlClassification.delphinID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jpamutils.spectrum.Spectrum;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import ai.djl.MalformedModelException;
import rawDeepLearningClassifier.dlClassification.archiveModel.SimpleArchiveModel;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.ClickDetectionMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.DetectionGroupMAT;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.MatFile;
import us.hebi.matlab.mat.types.Struct;

public class DelphinIDClickTest {

	public static void main(String args[]) {

		//test a single segment. 
		String matFileout = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/clickspectrum.mat";

		//
		float[][] output = testDelphinIDClickSegment(matFileout); 

		//run the model
		String modelFile = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clickclassifier.zip";

		String matFileout2 = "/Users/jdjm/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/clickspectrum_results.mat";

		testDelphinIDModelRaw(modelFile, output, matFileout2); 

	}


	/****---------------------1D Click Spectrum Tests---------------------****/
	/*
	/*
	/*
	/****------------------------------------------------------------------****/



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
				
				System.out.println("Click spectrum output Java: "); 
				PamArrayUtils.printArray(outputJava);
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
	public static float[][] testDelphinIDClickSegment(String matFileout) {


		//		String clicksMatPath = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";
		String clicksMatPath = "/Users/jdjm/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";

		double segLen = 4000.;
		double segHop = 1000.0;
		double startSeconds = 1.50355; //seconds to start segments (so we can compare to Python)
		double[] freqLimits = new double[] {10000., 40188.};
		int fftLen = 512; 

		// Create MAT file with a scalar in a nested struct
		MatFile matFile;
		int sum = 0;
		try {

			DetectionGroupMAT<ClickDetectionMAT> clicks = DelphinIDUtils.getClicksMAT(clicksMatPath);
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
						.addArray("file", Mat5.newString(clicksMatPath)); 

				Mat5.writeToFile(matFileWrite, matFileout);
			}

			return output;


		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}


}
