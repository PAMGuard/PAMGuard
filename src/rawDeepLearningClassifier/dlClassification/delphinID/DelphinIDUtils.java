package rawDeepLearningClassifier.dlClassification.delphinID;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import PamUtils.FileList;
import PamUtils.PamArrayUtils;
import PamUtils.TxtFileUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDTest.DelphinIDWorkerTest;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;
import whistlesAndMoans.AbstractWhistleDataUnit;

/**
 * A bunch of utility functions that a re useful for testing and running
 * DelphinID models
 */
public class DelphinIDUtils {

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

			return getWhistleContoursMAT(whistlesStruct,  fftLen,  fftHop,  sampleRate);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contours; 
	}


	/**
	 * Load whistle contours from a MATLAB struct

	 * @param whistlesStruct - a struct containing a list of whistle contours
	 * @param fftLen- the fft length in samples
	 * @param fftHop - the fft hop in samples. 
	 * @param sampleRate - the sample rate in samples per second. 
	 * @return a list of whistle contour objects from the struct. 
	 */
	public static ArrayList<AbstractWhistleDataUnit> getWhistleContoursMAT(Struct whistlesStruct, double fftLen, double fftHop, double sampleRate){

		ArrayList<AbstractWhistleDataUnit> contours = new ArrayList<AbstractWhistleDataUnit>();


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


	private static void generateImages(Struct whistlesStruct, String outName, DelphinIDWorker worker, double fftLen, double fftHop, double sampleRate) {

		double segLen = 4000.;
		double segHop = 1000.0;

		//		ArrayList<ArrayList<Double>> contourData =  TxtFileUtils.importCSVData(csvFile);
		ArrayList<AbstractWhistleDataUnit> whistles = getWhistleContoursMAT(whistlesStruct,  fftLen,  fftHop,  sampleRate);

		//segment the whistle detections
		ArrayList<SegmenterDetectionGroup> segments =  DelphinIDUtils.segmentWhsitleData(whistles,  whistles.get(0).getTimeMilliseconds(), segLen, segHop);

		float[][][] images = worker.dataUnits2ModelInput(segments,  (float) sampleRate,  0);

		float[][] image;
		BufferedImage bfImage;
		for (int k=0; k<images.length; k++) {
			image = images[k];

			bfImage = new BufferedImage(image.length, image[0].length, BufferedImage.TYPE_INT_RGB);
			
//			System.out.println("Max of image: " + PamArrayUtils.minmax(image)[1]);

			for(int i = 0; i < image.length; i++) {
				for(int j = 0; j < image[0].length; j++) {
					Color myRGB = new Color(image[i][j], image[i][j], image[i][j]);
					int rgb = myRGB.getRGB();
					bfImage.setRGB(i, j, rgb);
				}
			}

			//now save the image 
			String outputPath = outName + "_" + k + ".png";

			File outputfile = new File(outputPath);

			try {
				ImageIO.write(bfImage, "png", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



	}

	public static void main(String[] args) {

		//the whsitle contours as csv files.
		String whistlefolder = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD";

		//the image folder to save to.
		String imageFolder = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD_Images";

		//the path to the model
		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";

		//prepare the model - this loads the zip file and loads the correct transforms. 
		DelphinIDWorkerTest model;
		
		model = DelphinIDUtils.prepDelphinIDModel(modelPath);
		model.setEnableSoftMax(false);

		FileList filelist = new FileList();

		File folder = new File(whistlefolder);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isDirectory()) {
					//get a list of csv files
					//					ArrayList<File> csvFiles = filelist.getFileList(listOfFiles[i].getAbsolutePath(), ".mat" , true);

					System.out.println("Directory " + listOfFiles[i].getName());

					File outFolder = new File(imageFolder + File.separator + listOfFiles[i].getName());
					outFolder.mkdir();//make the out folder directory

					try {

						File file  = new File(listOfFiles[i].getPath() + File.separator + "whistles.mat");
						Mat5File matFile = Mat5.readFromFile(file);

						Struct whistlesStruct = matFile.getStruct("whistles");

						double fftLen = matFile.getMatrix("fftlen").getDouble(0);
						double fftHop = matFile.getMatrix("ffthop").getDouble(0);
						double sampleRate = matFile.getMatrix("samplerate").getDouble(0);

						List<String> fieldNames = whistlesStruct.getFieldNames();

						Struct whistecontours;
						for (String name: fieldNames) {
							System.out.println("Generating images for recording " + name + " from " + listOfFiles[i].getName());
							if (!name.equals("fftlen") && !name.equals("ffthop") && !name.equals("samplerate")) {
								whistecontours = whistlesStruct.get(name);
								generateImages( whistecontours,  (outFolder + File.separator + name) , model, fftLen, fftHop, sampleRate);
							}
						}


					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

}
