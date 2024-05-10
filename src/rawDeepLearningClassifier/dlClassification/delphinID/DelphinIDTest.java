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

	public static DelphinIDWorkerTest prepDelphinIDModel(String modelPath) {

		//create the delphinID worker. 
		DelphinIDWorkerTest delphinIDWorker = new DelphinIDWorkerTest();

		StandardModelParams params = new StandardModelParams();
		params.modelPath = modelPath;

		//prepare the model
		delphinIDWorker.prepModel(params, null);

		return delphinIDWorker;
	}


	/**
	 * Load whistle contours from a MAT file. ()
	 * 
	 * @param filePath - the file path. 
	 * 
	 * @return a list of whistle contour objects from the mat file. 
	 */
	public static ArrayList<AbstractWhistleDataUnit> getWhistleContoursMAT(String filePath){

		ArrayList<AbstractWhistleDataUnit> contours = new ArrayList<AbstractWhistleDataUnit>();

		//		SegmenterDetectionGroup segmenterDetectionGroup = new SegmenterDetectionGroup(0, 0, 0, 0);

		// Read scalar from nested struct
		try {
			Mat5File matFile = Mat5.readFromFile(filePath);
			Struct whistlesStruct = matFile.getStruct("whistles");

			double fftLen = matFile.getMatrix("fftlen").getDouble(0);
			double fftHop = matFile.getMatrix("ffthop").getDouble(0);
			double sampleRate = matFile.getMatrix("samplerate").getDouble(0);

			for (int i=0; i< whistlesStruct.getNumElements(); i++) {
				DataUnitBaseData basicData = new DataUnitBaseData();

				long timeMillis = ((Matrix)whistlesStruct.get("millis", i)).getLong(0);
				basicData.setTimeMilliseconds(timeMillis);

				long sampleDuration = ((Matrix)whistlesStruct.get("sampleDuration", i)).getLong(0);
				basicData.setSampleDuration(sampleDuration);

				basicData.setMillisecondDuration(1000.*(sampleDuration/sampleRate));

				int channelMap = ((Matrix)whistlesStruct.get("channelMap", i)).getInt(0);
				basicData.setChannelBitmap(channelMap);

				long uid = ((Matrix)whistlesStruct.get("UID", i)).getLong(0);
				basicData.setUID(uid);

				long startSample = ((Matrix)whistlesStruct.get("startSample", i)).getLong(0);
				basicData.setStartSample(startSample);

				int nSlices = ((Matrix)whistlesStruct.get("nSlices", i)).getInt(0);

				double[] freq = new double[nSlices];
				double[] times = new double[nSlices];

				Matrix contourStruct = whistlesStruct.getMatrix("contour", i); 
				for (int j=0; j<nSlices; j++) {
					freq[j] = contourStruct.getDouble(j)*sampleRate/fftLen; 
					times[j]  = j * fftHop /sampleRate;
				}

				contours.add(new WhistleContourMAT(basicData, freq, times)); 
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contours; 
	}



	/**
	 * Segment the detections into groups. Note that segments are overlaps so each whistle may belong to  multiple segments. 
	 * @param whistles - a list of whistles - not necessarily sorted by time. 
	 * @param dataStartMillis - the start time of the data in millis i.e. where the first segment starts. 
	 * @param segLen - the segment size in milliseconds. 
	 * @param segHop - the segment hop in milliseconds. 
	 * @return groups of data units within each segment. 
	 */
	public static ArrayList<SegmenterDetectionGroup> segmentWhsitleData(ArrayList<AbstractWhistleDataUnit> whistles, long dataStartMillis, 
			double segLen, double segHop){

		ArrayList<SegmenterDetectionGroup> group = new ArrayList<SegmenterDetectionGroup>(); 

		//find the maximum whistle time
		long maxTime = Long.MIN_VALUE;
		long endTime = 0; 
		for (AbstractWhistleDataUnit whislte: whistles) {
			endTime = (long) (whislte.getTimeMilliseconds()+whislte.getDurationInMilliseconds()); 
			if (endTime>maxTime) maxTime=endTime;
		}

		long segStart = dataStartMillis;
		long segEnd = (long) (segStart+segLen);

		long whistleStart; 
		long whistleEnd;
		SegmenterDetectionGroup whistleGroup;
		while (segStart<endTime){

			whistleGroup = new SegmenterDetectionGroup(segStart, 1, segEnd, segLen);

			for (AbstractWhistleDataUnit whislte: whistles) {
				whistleStart = whislte.getTimeMilliseconds();
				whistleEnd = (long) (whislte.getTimeMilliseconds() + whislte.getDurationInMilliseconds());

				if ((whistleStart>=segStart && whistleStart<segEnd) || ((whistleEnd>=segStart && whistleEnd<segEnd))){
					//some part of the whistle is in the segment. 
					whistleGroup.addSubDetection(whislte);
				}

			}

			group.add(whistleGroup);
			
//			System.out.println("SegStart: " + (segStart - dataStartMillis));

			segStart = (long) (segStart+segHop);
			segEnd = (long) (segStart+segLen);
		}

		return group;

	}

	public static class WhistleContourMAT extends AbstractWhistleDataUnit {

		private double[] freq;
		private double[] times;

		public WhistleContourMAT(DataUnitBaseData basicData, double[] freq, double[] times) {
			super(basicData);
			this.freq=freq;
			this.times=times;
		}

		@Override
		public int getSliceCount() {
			return freq.length;
		}

		@Override
		public double[] getTimesInSeconds() {
			return times;
		}

		@Override
		public double[] getFreqsHz() {
			return freq;
		}


	}
	
	
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
		String whistleContourPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_contours.mat";

		//the path to the model
		String modelPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";
		
		//the path to the model
		String matImageSave = "C:/Users/Jamie Macaulay/MATLAB Drive/MATLAB/PAMGUARD/deep_learning/delphinID/whistleimages.mat";
		
		//create MatFile for saving the image data to. 
		MatFile matFile = Mat5.newMatFile();

		//get the whislte contours form a .mat file. 
		ArrayList<AbstractWhistleDataUnit> whistleContours = getWhistleContoursMAT(whistleContourPath);

		//segment the whistle detections
		ArrayList<SegmenterDetectionGroup> segments =  segmentWhsitleData(whistleContours,  (long) (dataStartMillis+(9.565*1000.)), 
				segLen,  segHop);

		for (int i=0; i<segments.size(); i++) {
			System.out.println("Segment " + i + " contains " + segments.get(i).getSubDetectionsCount() + " whistles"); 
		}

		//prepare the model - this loads the zip file and loads the correct transforms. 
		DelphinIDWorkerTest model = prepDelphinIDModel(modelPath);
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
