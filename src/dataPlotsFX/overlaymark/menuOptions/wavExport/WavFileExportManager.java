package dataPlotsFX.overlaymark.menuOptions.wavExport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.swing.filechooser.FileSystemView;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataMap.OfflineDataMapPoint;
import detectiongrouplocaliser.DetectionGroupSummary;
import wavFiles.Wav16AudioFormat;
import wavFiles.WavFileWriter;

/**
 * Manages .wav file writing based on what type of data unit is selected and whether raw data is available. 
 * @author Jamie Macaulay
 *
 */
public class WavFileExportManager {

	/**
	 * Successful writing of .wav file. 
	 */
	public static final int SUCCESS_WAV=0; 

	/**
	 * Successful writing of .wav file from data contained in detections. 
	 */
	public static final int SUCCESS_DET_WAV=1; 

	/**
	 * General failure in wav file writing 
	 */
	public static final int FAILURE_WAV=2; 


	public static final int LOADING_WAV=2;

	/**
	 * The default path to write files to
	 */
	private String defaultPath;

	/*
	 * The current file to write wav files to. 
	 */
	private String currentFolder;


	private ArrayList<WavDataUnitExport> wavDataUnitExports = new ArrayList<WavDataUnitExport>();

	private WavSaveCallback saveCallback; 

	public WavFileExportManager() {
		wavDataUnitExports.add(new RawHolderWavExport());	


		defaultPath=FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		defaultPath=defaultPath +  "/Pamguard Manual Export";

		currentFolder=defaultPath;
	}



	/**
	 * Get a list of ecporters that can export wav files from a data unit. 
	 * @return the wav file exporters. 
	 */
	public ArrayList<WavDataUnitExport> getWavDataUnitExporters() {
		return wavDataUnitExports;
	}


	/**
	 * Check whether raw data is available. 
	 * @return true if raw data for the specified period is available. 
	 */
	public static boolean haveRawData(PamRawDataBlock rawDataBlock, long start, long end) {
		//System.out.println("Is raw available? : " +  PamCalendar.formatDateTime2(start) +  " to " + PamCalendar.formatDateTime2(end)); 

		if (rawDataBlock==null) {
			System.out.println("The raw data block is null");
			return false;
		}

		//check if there is data in the raw data block available to be loaded. 
		if (rawDataBlock.getPrimaryDataMap()==null || rawDataBlock.getPrimaryDataMap().getMapPoints().size()==0) {
			//			System.out.println("There is no raw data in the time period");
			return false; 
		}

		OfflineDataMapPoint mapPoint = (OfflineDataMapPoint) rawDataBlock.getPrimaryDataMap().getMapPoints().get(0); 		

		List<OfflineDataMapPoint> rawDataPoints = rawDataBlock.getPrimaryDataMap().getMapPoints(start, end); 

		if (rawDataPoints==null || rawDataPoints.size()==0) {
			//			System.out.println("There is no raw data in the time period 2");
			return false;
		}
		else {
			return true; 
		}
	}


	/**
	 * Create the wav file name 
	 * @param timeStart
	 * @return
	 */
	private String createFileName(long timeStart) {

		File folder = new File(currentFolder); 

		try {
			//save a .wav file clip. 
			if (!folder.exists()){
				if (!folder.mkdir()){
					System.err.println("Could not create a folder for the .wav file");
					return null;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}

		String currentPath = PamCalendar.formatFileDateTime(timeStart, false);

		//add correct file type.	
		currentPath = currentPath + ".wav";
		currentPath = currentFolder+"/"+currentPath;

		return currentPath;

	}

	/**
	 * Convert the mark/dataunits to a .wav file. 
	 * @param foundDataUnits - found data units. 
	 * @param selectedIndex - the currently selected data unit. 
	 * @param mark - overlay mark. 
	 */
	public int dataUnits2Wav(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
		System.out.println("Data units 2 wav");
		//this order for .wav files. 
		//if there is a mark then save limits of mark. If there is one data unit then set limits of data units
		long start;
		long end; 
		if (mark!=null) {
			start=(long) mark.getLimits()[0]; 
			end=(long) mark.getLimits()[1]; 
		}
		else {
			start=foundDataUnits.getFirstTimeMillis(); 
			end=foundDataUnits.getLastTimeMillis(); 
		}


		//now search for the raw data block 
		PamRawDataBlock rawDataBlock;
		if (foundDataUnits==null || foundDataUnits.getDataList().size()<=0) {
			rawDataBlock=PamController.getInstance().getRawDataBlock(0);
		}
		else {
			rawDataBlock=foundDataUnits.getDataList().get(0).getParentDataBlock().getRawSourceDataBlock();
		}

		//have a bit decision to make here. 

		// 1) Data units with no raw data in which case we want to export a wav clip from raw data
		// 2) Data units which all have raw data in which case we want to export data unit clips as a zero padded wav file
		//		(this counts for a single data unit too)
		// 3) Mixed data units in which case we want to export a wav clip from raw data. 

		//check whether raw .wav data is available. 
		boolean haveRawData = haveRawData(rawDataBlock, start, end); 

		boolean hasAllWavClips = false; 
		if (foundDataUnits!=null) {
			//check whether the wav file has all data raw data units. 
			int n =  getNWavDataUnits(foundDataUnits); 
			hasAllWavClips = n ==  foundDataUnits.getNumDataUnits(); 
			System.out.println("N raw data units: " + n + " N found data units: " + foundDataUnits.getNumDataUnits()); 
		}

		int flag=-1; 

		if (hasAllWavClips) {
			flag = saveDataUnitWav(foundDataUnits); 
		}
		else if (!hasAllWavClips && haveRawData) {
			//no raw data
			flag = saveRawWav(start, end, rawDataBlock); 
		}
		else {
			flag = saveDataUnitWav(foundDataUnits); 
		}

		return flag; 
	}

	/**
	 * Save a clip of wav data from a pre existing .wav or other audio file within in PG. 
	 * @param start - the start of the wav file. 
	 * @param end - the end of the wac file. 
	 */
	private int saveRawWav(long start, long end, PamRawDataBlock rawDataBlock) {
		//System.out.println("Save raw wav. from " + PamCalendar.formatDateTime2(start) +  " to " + PamCalendar.formatDateTime2(end) ) ;
		String currentFile = createFileName(start); 

		AudioFormat audioFormat = new Wav16AudioFormat(rawDataBlock.getSampleRate(), PamUtils.getNumChannels(rawDataBlock.getChannelMap()));
		WavFileWriter wavWrite= new WavFileWriter(currentFile, audioFormat); 

		//now need to go into a new thread to load the offline data. 

		rawDataBlock.orderOfflineData(new RawObserver(wavWrite, rawDataBlock.getChannelMap()), new RawLoadObserver(wavWrite),
				start, end, 1, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);


		return 0; 
	}

	/**
	 * Observes incoming raw data and saves to a wav file 
	 *
	 * @author Jamie Macaulay
	 *
	 */
	class RawObserver extends PamObserverAdapter {

		/**
		 * The wav write
		 */
		private WavFileWriter wavWriter;

		/**
		 * The channel map 
		 */
		private int channelMap;

		/**
		 * List of channels 
		 */
		private int[] channels;

		/**
		 * Current time in millis. 
		 */
		long currentTimeMillis;

		/**
		 * The arrays
		 */
		double[][] wavArray; 

		/**
		 * 
		 * @param wavWriter
		 */
		RawObserver(WavFileWriter wavWriter, int channelMap){
			this.channelMap=channelMap; 
			channels= PamUtils.getChannelArray(channelMap); 
			this.wavWriter=wavWriter;
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			newRawData((RawDataUnit) dataUnit); //do not put in FX thread!
		}

		/***
		 * Called whenever new raw data is acquired. Each raw data unit is only one channel. Therefore msut wait for a raw data unit to 
		 * fill up a double[][] wav array and then append to file. 
		 * @param dataUnit
		 */
		private void newRawData(RawDataUnit dataUnit) {
			//			System.out.println(" Time millis:   "+ dataUnit.getTimeMilliseconds()+ " channel: " + PamUtils.getSingleChannel(dataUnit.getChannelBitmap()) );
			if (currentTimeMillis!=dataUnit.getTimeMilliseconds()) {
				currentTimeMillis=dataUnit.getTimeMilliseconds(); 
				if (wavArray!=null) {
					//					for (int i=0; i<wavArray.length; i++) {
					//						System.out.println("Wav data: " + wavArray[i] + " channel " +channels[i] );
					//					}
					wavWriter.append(wavArray); 
				}
				wavArray= new double[channels.length][]; 
			}

			//this must always be 1-1 but you never know
			int index=Arrays.binarySearch(channels, PamUtils.getSingleChannel(dataUnit.getChannelBitmap())); 
			wavArray[index]=dataUnit.getRawData();
		}


		@Override
		public String getObserverName() {
			return "Wav File Export Observer";
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

	}


	/**
	 * Observe the success of the data load
	 * @author Jamie Macaulay
	 *
	 */
	public class RawLoadObserver implements LoadObserver {


		private WavFileWriter wavWriter;

		public RawLoadObserver(WavFileWriter wavWriter){
			this.wavWriter=wavWriter;
		}

		@Override
		public void setLoadStatus(int loadState) {
			//TODO- doesn;t do anything yet. 
			//				System.out.println("FINISHED WITH THE .WAV FILE "); 
			wavWriter.close();
			if (saveCallback!=null) saveCallback.wavSaved(wavWriter.getFileName(), 0);
		}
	}


	/**
	 * Save wav data from a data unit instead of from the raw file store. 
	 * @param foundDataUnits - the list of found data units. 
	 * @return
	 */
	private int saveDataUnitWav(DetectionGroupSummary foundDataUnits) {
		//TODO - need to pad the detections...with zeros.
		
		
		//System.out.println("Save data unit wav: " + foundDataUnits.getNumDataUnits()); 

		int n=0; 
		WavFileWriter wavWrite = null; 
		for (PamDataUnit fnDataUnit: foundDataUnits.getDataList()){

			String currentFile = createFileName(fnDataUnit.getTimeMilliseconds()); 

			AudioFormat audioFormat = new Wav16AudioFormat(fnDataUnit.getParentDataBlock().getSampleRate(), PamUtils.getNumChannels(fnDataUnit.getChannelBitmap()));

			System.out.println("Save detection wav." + foundDataUnits.getNumDataUnits());
			
			for (int i=0; i<wavDataUnitExports.size(); i++) {
				if (wavDataUnitExports.get(i).getUnitClass().isAssignableFrom(fnDataUnit.getClass())) {
					
					wavWrite= new WavFileWriter(currentFile, audioFormat); 

					System.out.println("Append wav." + foundDataUnits.getNumDataUnits());

					//save the wav file of detection
					wavWrite.append(wavDataUnitExports.get(i).getWavClip(fnDataUnit)); 
					n++; 
					
					wavWrite.close();
					break; 
				}
			}
		}
		
		
		if (wavWrite!=null && saveCallback!=null) saveCallback.wavSaved(wavWrite.getFileName(), 0);
		
		return n; 
	}


	/**
	 * Save wav data from a data unit instead of from the raw file store. 
	 * @param foundDataUnits - the list of found data units. 
	 * @return
	 */
	private int getNWavDataUnits(DetectionGroupSummary foundDataUnits) {
		int n=0; 
		for (PamDataUnit fnDataUnit: foundDataUnits.getDataList()){
			//System.out.println("Save detection wav." + foundDataUnits.getNumDataUnits());
			for (int i=0; i<wavDataUnitExports.size(); i++) {
				if (wavDataUnitExports.get(i).getUnitClass().isAssignableFrom(fnDataUnit.getClass())) {
					n++; 
					break; 
				}
			}
		}
		return n; 
	}

	/**
	 * Set a callback for saving .wav data. 
	 * @param saveCallback - the callback
	 */
	public void setOnWavSaved(WavSaveCallback saveCallback) {
		this.saveCallback=saveCallback; 
	}





	//	hello(){
	//
	//
	//		if (mark==null) {
	//			start= foundDataUnits.getFirstTimeMillis();
	//			end= foundDataUnits.getLastTimeMillis(); 
	//		}
	//
	//		File folder = new File(currentFolder); 
	//
	//		//save a .wav file clip. 
	//		if (!folder.exists()){
	//			if (!folder.mkdir()){
	//				//TODO- warning message. 
	//				return;
	//			}
	//		}
	//
	//		String currentPath = PamCalendar.formatFileDateTime();
	//		//add data types to the filen,ae
	//		for (int i=0 ;i<mlData.size(); i++ ){
	//			currentPath=currentPath + "_" + mlData.get(i).getName(); 
	//		}
	//		//add correct file type.	
	//		currentPath = currentPath + ".mat";
	//		currentPath = currentFolder+"\\"+currentPath;
	//
	//
	//		if (append && clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
	//			wavFile.append(rawData);
	//			lastClipDataUnit.setSampleDuration(rawEnd-lastClipDataUnit.getStartSample());
	//			clipDataBlock.updatePamData(lastClipDataUnit, dataUnit.getTimeMilliseconds());
	//			//			System.out.println(String.format("%d samples added to file", rawData[0].length));
	//		}
	//		else {
	//			ClipDataUnit clipDataUnit;
	//			long startMillis = dataUnit.getTimeMilliseconds() - (long) (clipGenSetting.preSeconds*1000.);
	//			if (clipControl.clipSettings.storageOption == ClipSettings.STORE_WAVFILES) {
	//				String folderName = getClipFileFolder(dataUnit.getTimeMilliseconds(), true);
	//				String fileName = getClipFileName(startMillis);
	//				AudioFormat af = new Wav16AudioFormat(getSampleRate(), rawData.length);
	//				wavFile = new WavFileWriter(folderName+fileName, af);
	//				wavFile.write(rawData);
	//				wavFile.close();
	//				// make a data unit to go with it. 
	//				clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
	//						(int)(rawEnd-rawStart), channelMap, fileName, dataBlock.getDataName(), rawData, getSampleRate());
	//			}
	//			else {
	//				clipDataUnit = new ClipDataUnit(startMillis, dataUnit.getTimeMilliseconds(), rawStart,
	//						(int)(rawEnd-rawStart), channelMap, "", dataBlock.getDataName(), rawData, getSampleRate());
	//			}
	//			clipDataUnit.setFrequency(dataUnit.getFrequency());
	//			lastClipDataUnit = clipDataUnit;
	//			if (bearingLocaliser != null) {
	//				localiseClip(clipDataUnit, bearingLocaliser, hydrophoneMap);
	//			}				
	//			clipDataBlock.addPamData(clipDataUnit);
	//		}
	//
	//		return 0; // no error. 
	//	}
	//
	//	}
	//	}

}
