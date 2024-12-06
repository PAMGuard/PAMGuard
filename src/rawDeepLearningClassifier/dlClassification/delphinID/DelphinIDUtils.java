package rawDeepLearningClassifier.dlClassification.delphinID;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jamdev.jdl4pam.utils.DLMatFile;
import org.jamdev.jdl4pam.utils.DLUtils;

import PamUtils.FileList;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;
import rawDeepLearningClassifier.dlClassification.delphinID.DelphinIDWhistleTest.DelphinIDWorkerTest;
import rawDeepLearningClassifier.dlClassification.delphinID.Whistles2Image.Whistle2ImageParams;
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

		StandardModelParams params = new DelphinIDParams();
		params.modelPath = modelPath;

		//prepare the model
		delphinIDWorker.prepModel(params, null);

		return delphinIDWorker;
	}
	
	
	
	//record WhistleGroup(ArrayList<AbstractWhistleDataUnit> whistle, double sampleRate, double fftLen, double fftHop, long fileDataStart) { }

	
	
	/**
	 * Holds a whistle group and some extra information on sample rate, fft length and hop and the start of the processed file. 
	 * Keep Java 11 compliant so do not use record. 
	 *
	 */
	public static class DetectionGroupMAT<T extends PamDataUnit> {
		
		public DetectionGroupMAT(ArrayList<T> whistle, double sampleRate, long fileDataStart) { 
			this.whistle=whistle;
			this.sampleRate=sampleRate;
			this.fileDataStart=fileDataStart;
		}
		
		public DetectionGroupMAT(ArrayList<T> whistle, double sampleRate, double fftLen, double fftHop, long fileDataStart) { 
			this.whistle=whistle;
			this.sampleRate=sampleRate;
			this.fftLen=fftLen;
			this.fftHop=fftHop;
			this.fileDataStart=fileDataStart;
		}
		
		public ArrayList<T> getDetections() {
			return whistle;
		}

		public void setWhistle(ArrayList<T> whistle) {
			this.whistle = whistle;
		}

		public double getSampleRate() {
			return sampleRate;
		}

		public void setSampleRate(double sampleRate) {
			this.sampleRate = sampleRate;
		}

		public double getFftLen() {
			return fftLen;
		}

		public void setFftLen(double fftLen) {
			this.fftLen = fftLen;
		}

		public double getFftHop() {
			return fftHop;
		}

		public void setFftHop(double fftHop) {
			this.fftHop = fftHop;
		}

		public long getFileDataStart() {
			return fileDataStart;
		}

		public void setFileDataStart(long fileDataStart) {
			this.fileDataStart = fileDataStart;
		}

		public ArrayList<T> whistle;
		
		public double sampleRate; 
		
		public double fftLen;
		
		public double fftHop;
		
		public long fileDataStart;
		
	}
	
	
	/**
	 * Get base data for a data unit from a MATLAB struct. 
	 * @param detectionsStruct - the struct array containing acoustic detections. 
	 * @param i - the index of the unit from the struct.
	 * @param sampleRate - the sample rate in samples per second. 
	 * @return the base data for the data unit
	 */
	public static DataUnitBaseData getBaseData(Struct detectionsStruct, int i, double sampleRate){
		
		DataUnitBaseData basicData = new DataUnitBaseData();

		long timeMillis = ((Matrix)detectionsStruct.get("millis", i)).getLong(0);
		basicData.setTimeMilliseconds(timeMillis);

		long sampleDuration = ((Matrix)detectionsStruct.get("sampleDuration", i)).getLong(0);
		basicData.setSampleDuration(sampleDuration);

		basicData.setMillisecondDuration(1000.*(sampleDuration/sampleRate));

		int channelMap = ((Matrix)detectionsStruct.get("channelMap", i)).getInt(0);
		basicData.setChannelBitmap(channelMap);

		long uid = ((Matrix)detectionsStruct.get("UID", i)).getLong(0);
		basicData.setUID(uid);

		long startSample = ((Matrix)detectionsStruct.get("startSample", i)).getLong(0);
		basicData.setStartSample(startSample);
		
		return basicData;
	}

	
	

	/**
	 * Load whistle contours from a MAT file. ()
	 * 
	 * @param filePath - the file path. 
	 * 
	 * @return a list of whistle contour objects from the mat file. 
	 */
	public static DetectionGroupMAT getClicksMAT(String filePath){

		//		SegmenterDetectionGroup segmenterDetectionGroup = new SegmenterDetectionGroup(0, 0, 0, 0);

		// Read scalar from nested struct
		try {
			Mat5File matFile = Mat5.readFromFile(filePath);

			Struct whistlesStruct = matFile.getStruct("clicks");

			double sampleRate = matFile.getMatrix("samplerate").getDouble(0);
			
			long fileDataStart = PamCalendar.dateNumtoMillis(matFile.getMatrix("filedate").getDouble(0));

			return new DetectionGroupMAT<ClickDetectionMAT>(getClicksMAT(whistlesStruct, sampleRate), sampleRate ,fileDataStart);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null; 
	}

	
	
	/**
	 * Load clicks from a MATLAB struct

	 * @param clicksStruct - a struct containing a list of whistle contours
	 * @param fftLen- the fft length in samples
	 * @param fftHop - the fft hop in samples. 
	 * @param sampleRate - the sample rate in samples per second. 
	 * @return a list of whistle contour objects from the struct. 
	 */
	public static ArrayList<ClickDetectionMAT> getClicksMAT(Struct clicksStruct, double sampleRate){

		ArrayList<ClickDetectionMAT> clicks = new ArrayList<ClickDetectionMAT>();

		for (int i=0; i< clicksStruct.getNumElements(); i++) {
			DataUnitBaseData basicData = getBaseData(clicksStruct,  i,  sampleRate);
			
			double[][] waveform = null;
			Matrix waveformM = clicksStruct.getMatrix("wave", i);
			waveform = DLMatFile.matrix2array(waveformM);
			

			clicks.add(new ClickDetectionMAT(basicData,PamArrayUtils.transposeMatrix(waveform), (float) sampleRate)); 
		}

		return clicks;
	}
	
	


	/**
	 * Load whistle contours from a MAT file. ()
	 * 
	 * @param filePath - the file path. 
	 * 
	 * @return a list of whistle contour objects from the mat file. 
	 */
	public static DetectionGroupMAT getWhistleContoursMAT(String filePath){

		ArrayList<AbstractWhistleDataUnit> contours = new ArrayList<AbstractWhistleDataUnit>();

		//		SegmenterDetectionGroup segmenterDetectionGroup = new SegmenterDetectionGroup(0, 0, 0, 0);

		// Read scalar from nested struct
		try {
			Mat5File matFile = Mat5.readFromFile(filePath);

			Struct whistlesStruct = matFile.getStruct("whistles");

			double fftLen = matFile.getMatrix("fftlen").getDouble(0);
			double fftHop = matFile.getMatrix("ffthop").getDouble(0);
			double sampleRate = matFile.getMatrix("samplerate").getDouble(0);
			
			long fileDataStart = PamCalendar.dateNumtoMillis(matFile.getMatrix("filedate").getDouble(0));

			return new DetectionGroupMAT(getWhistleContoursMAT(whistlesStruct,  fftLen,  fftHop,  sampleRate), sampleRate, fftLen, fftHop, fileDataStart);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null; 
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
			DataUnitBaseData basicData = getBaseData(whistlesStruct,  i,  sampleRate);

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
	public static ArrayList<SegmenterDetectionGroup> segmentDetectionData(ArrayList<? extends PamDataUnit> whistles, long dataStartMillis, 
			double segLen, double segHop){
		return segmentDetectionData(whistles,  dataStartMillis, 
				 segLen,  segHop, null);
	}


	/**
	 * Segment the detections into groups. Note that segments are overlaps so each whistle may belong to  multiple segments. 
	 * @param whistles - a list of whistles - not necessarily sorted by time. 
	 * @param dataStartMillis - the start time of the data in millis i.e. where the first segment starts. 
	 * @param segLen - the segment size in milliseconds. 
	 * @param segHop - the segment hop in milliseconds. 
	 * @param sampleRate - the sample rate to set. 
	 * @return groups of data units within each segment. 
	 */
	public static ArrayList<SegmenterDetectionGroup> segmentDetectionData(ArrayList<? extends PamDataUnit> whistles, long dataStartMillis, 
			double segLen, double segHop, Float sampleRate){

		ArrayList<SegmenterDetectionGroup> group = new ArrayList<SegmenterDetectionGroup>(); 

		//find the maximum whistle time
		long maxTime = Long.MIN_VALUE;
		long endTime = 0; 
		for (PamDataUnit whislte: whistles) {
			endTime = (long) (whislte.getTimeMilliseconds()+whislte.getDurationInMilliseconds()); 
			if (endTime>maxTime) maxTime=endTime;
		}

		long segStart = dataStartMillis;
		long segEnd = (long) (segStart+segLen);

		long whistleStart; 
		long whistleEnd;
		AcousticDetectionGroup whistleGroup;
		while (segStart<endTime){

			whistleGroup = new AcousticDetectionGroup(segStart, 1, segEnd, segLen);
			whistleGroup.setHardSampleRate(sampleRate);

			for (PamDataUnit whislte: whistles) {
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
	
	
	private static class AcousticDetectionGroup extends SegmenterDetectionGroup {
		
		public Float hardSampleRate;

		public AcousticDetectionGroup(long timeMilliseconds, int channelBitmap, long startSample,
				double duration) {
			super(timeMilliseconds, channelBitmap, startSample, duration);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public float getSampleRate() {
			if (super.getParentDataBlock()==null) {
				return hardSampleRate;
			}
			else {
				return super.getSampleRate();
			}
			
		}

		public Float getHardSampleRate() {
			return hardSampleRate;
		}

		public void setHardSampleRate(Float hardSampleRate) {
			this.hardSampleRate = hardSampleRate;
		}
		
	}
	

	/**
	 * Subclass of Abstract whsitle data unit for loading whistle contours from .mat files. 
	 */
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
	
	/**
	 * Subclass of Abstract whsitle data unit for loading whistle contours from .mat files. 
	 */
	public static class ClickDetectionMAT extends PamDataUnit implements RawDataHolder {

		private double[][] waveform;
		
		
		private RawDataTransforms rawTransforms;

		public ClickDetectionMAT(DataUnitBaseData baseData, double[][] waveform, float sampleRate) {
			super(baseData);
			this.waveform= waveform;
			this.rawTransforms = new RawDataTransforms(this) {	
				@Override
				public float getSampleRate() {
					return sampleRate;
				}
			};
			
		}


		@Override
		public double[][] getWaveData() {
			return waveform;
		}

		@Override
		public RawDataTransforms getDataTransforms() {
			return rawTransforms;
		}

	}



	private static void generateImages(Struct whistlesStruct, String outName, DelphinIDWorker worker, double fftLen, double fftHop, double sampleRate) {

		double segLen = 4000.;
		double segHop = 1000.0;

		//		ArrayList<ArrayList<Double>> contourData =  TxtFileUtils.importCSVData(csvFile);
		ArrayList<AbstractWhistleDataUnit> whistles = getWhistleContoursMAT(whistlesStruct,  fftLen,  fftHop,  sampleRate);

		//segment the whistle detections
		ArrayList<SegmenterDetectionGroup> segments =  DelphinIDUtils.segmentDetectionData(whistles,  whistles.get(0).getTimeMilliseconds(), segLen, segHop);
		
		float[][][] images = worker.dataUnits2ModelInput(segments,  (float) sampleRate,  0);

		float[][] image;
		BufferedImage bfImage;
		double density;
		for (int k=0; k<images.length; k++) {
			
			if (segments.get(k).getSubDetectionsCount()<1) {
				continue;
			}
			image = images[k];

			bfImage = new BufferedImage(image[0].length, image.length, BufferedImage.TYPE_INT_RGB);

			//			System.out.println("Max of image: " + PamArrayUtils.minmax(image)[1]);

			for(int i = 0; i < image.length; i++) {
				for(int j = 0; j < image[0].length; j++) {
					Color myRGB = new Color(image[i][j], image[i][j], image[i][j]);
					int rgb = myRGB.getRGB();
					bfImage.setRGB(j,i, rgb);
				}
			}

			density = getDensity(segments.get(k)); 
			//now save the image 
			String outputPath = String.format("%s_d%.2f_%d.png", outName, density, k);

			File outputfile = new File(outputPath);

			try {
				ImageIO.write(bfImage, "png", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Calculate the density of whistles for a segmenter group in the absence of a known fft length and hop. 
	 * @param group - the group
	 * @return
	 */
	public static double getDensity(SegmenterDetectionGroup group) {
		//number of whistle bins/number of time bins
		ArrayList<double[][]> contour = Whistles2Image.whistContours2Points(group);
		
		//time bin length from the first contour
		double[] times = new double[contour.get(0).length-1];
		for (int i=0; i<times.length; i++) {
			times[i]=1000.*(contour.get(0)[i+1][0] - contour.get(0)[i][0]);
		}
		
		double timebinMillis = PamArrayUtils.mean(times);
		
		double nBins = group.getSegmentDuration()/timebinMillis;
		
		double nwhistleBins = 0;
		for (int i=0; i<contour.size(); i++) {
			nwhistleBins+=contour.get(i).length;
		}
		
//		System.out.println("nwhistleBins: " +nwhistleBins + "nBins: " + nBins + " timebinMillis: " + timebinMillis);
			
		return nwhistleBins/nBins;
	}

	/**
	 * Generate training images for DelphinID
	 * @param modelPath
	 * @param whistlefolder
	 * @param imageFolder
	 * @param lineWidth - the line width in pixels to use
	 */
	private static void generateTrainingData(String modelPath, String whistlefolder, String imageFolder, double lineWidth) {
		DelphinIDWorkerTest model = DelphinIDUtils.prepDelphinIDModel(modelPath);
		model.setEnableSoftMax(false);
		
		model.getWhistleTransform().getTransformParams();

		((Whistle2ImageParams) model.getWhistleTransform().getTransformParams()).lineWidth=lineWidth;

		FileList filelist = new FileList();

		File folder = new File(whistlefolder);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isDirectory()) {
					//get a list of csv files
					//					ArrayList<File> csvFiles = filelist.getFileList(listOfFiles[i].getAbsolutePath(), ".mat" , true);

					System.out.println("Directory " + listOfFiles[i].getName());



					try {

						File file  = new File(listOfFiles[i].getPath() + File.separator + "whistles.mat");

						if (!file.exists()) {
							System.out.println("No whistles.mat for " + listOfFiles[i].getName()); 
							continue;
						}

						Mat5File matFile = Mat5.readFromFile(file);

						Struct whistlesStruct = matFile.getStruct("whistles");

						double fftLen = matFile.getMatrix("fftlen").getDouble(0);
						double fftHop = matFile.getMatrix("ffthop").getDouble(0);
						double sampleRate = matFile.getMatrix("samplerate").getDouble(0);

						List<String> fieldNames = whistlesStruct.getFieldNames();

						File outFolder = new File(imageFolder + File.separator + listOfFiles[i].getName());
						outFolder.mkdir();//make the out folder directory

						Struct whistecontours;
						for (String name: fieldNames) {
							System.out.println("Generating images for recording " + name + " from " + listOfFiles[i].getName() + " " + lineWidth);
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
		
	public static void main(String[] args) {

		//		double[] density = new double[] {0.15 - 1.5}; 

		//number of whistle bins/number of time bins; either 16 or 21
		//the e contours as csv files.
		//		String whistlefolder = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD";
//		String whistlefolder = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD_examples/contours";
		String whistlefolder = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD/contours";

		//the image folder to save to.
		//		String imageFolder = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD_Images";
//		String imageFolder = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/training/WMD_examples/images";
		String imageFolder = "C:/Users/Jamie Macaulay/Desktop/Tristan_training_images/contour_images";

		//the path to the model
		//		String modelPath = "/Users/au671271/Library/CloudStorage/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";
		String modelPath = "D:/Dropbox/PAMGuard_dev/Deep_Learning/delphinID/testencounter415/whistle_model_2/whistle_4s_415.zip";

		//line widths in pixels
		double[] lineWidths = new double[] {6, 7, 10, 15, 20};

		for (double lineWidth:lineWidths) {
			String imageFolderWidth = (imageFolder + "_"+ String.format("%d",(int)lineWidth));
			new File(imageFolderWidth).mkdir();
			generateTrainingData( modelPath,  whistlefolder,  imageFolderWidth, lineWidth);
		}
	}

}
