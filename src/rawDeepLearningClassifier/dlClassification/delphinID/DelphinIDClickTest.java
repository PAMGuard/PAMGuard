package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;

import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.ClickDetectionMAT;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDUtils.DetectionGroupMAT;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.types.MatFile;

public class DelphinIDClickTest {
	
	public static void main(String args[]) {
		
		//test a single segment. 
		String matFileout = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/deep_learning/delphinID/click1D/clickspectrum.mat";
		
		//
		testDelphinIDClickSegment(matFileout); 
	}

	
	/****---------------------1D Click Spectrum Tests---------------------****/
	/*
	/*
	/*
	/****------------------------------------------------------------------****/


	/**
	 * This test runs delphinID on one 4 second window from whistle contours saved
	 * in a mat file. 
	 * 
	 * @return true if the test is passed. 
	 */
	public static boolean testDelphinIDClickSegment(String matFileout) {
		
		
		String clicksMatPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/delphinIDmodels/Ggr242/clicks_20200918_123234.mat";
		
		double segLen = 4000.;
		double segHop = 1000.0;
		double startSeconds = 1.50355; //seconds to start segments (so we can compare to Python)
		
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
			
			
			for (int i=0; i<segments.size(); i++) {
				sum += segments.get(i).getSubDetectionsCount() ;
				System.out.println("Segment" + i + " time:  " +  (double)(segments.get(i).getSegmentStartMillis()-dataStartMillis)/1000.  + "s no. clicks " + segments.get(i).getSubDetectionsCount() + " total: " + sum);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;

	}
	
	
}
