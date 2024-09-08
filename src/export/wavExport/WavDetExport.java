package export.wavExport;

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

public class WavDetExport {
	
	/**
	 * Successful writing of .wav file. 
	 */
//	public static final int SUCCESS_WAV=0; 

	/**
	 * Successful writing of .wav file from data contained in detections. 
	 */
//	public static final int SUCCESS_DET_WAV=1; 

	/**
	 * General failure in wav file writing 
	 */
//	public static final int FAILURE_WAV=2; 


//	public static final int LOADING_WAV=2;

	/**
	 * The maximum allowed size
	 */
	private static final double MAX_ZEROPAD_SIZE_MEGABYTES = 1024; //MB

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
	
	private WavExportOptions wavFileoptions = new WavExportOptions(); 

	private WavOptionsPanel wavOptionsPanel; 

	public WavDetExport() {
		wavDataUnitExports.add(new RawHolderWavExport());	

		defaultPath=FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		defaultPath=defaultPath + File.separator + "Pamguard Manual Export";

		currentFolder=defaultPath;
	}



	/**
	 * Get a list of exporters that can export wav files from a data unit. 
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
		currentPath = currentFolder + File.separator + currentPath;

		return currentPath;

	}

	/**
	 * Convert the mark/dataunits to a .wav file. makes a decision as to what to
	 * save
	 * 
	 * 1) Data units with no raw data in which case we want to export a wav clip
	 * from raw data <br> 2) Data units which all have raw data in which case we want to
	 * export data unit clips as a zero padded wav file (this counts for a single
	 * data unit too) <b> 3) Mixed data units in which case we want to export a wav clip
	 * from raw data.
	 * 
	 * @param foundDataUnits - found data units.
	 * @param selectedIndex  - the currently selected data unit.
	 * @param mark           - overlay mark.
	 */
	public int writeOverlayMarkWav(DetectionGroupSummary foundDataUnits, int selectedIndex, OverlayMark mark) {
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
			hasAllWavClips = (n ==  foundDataUnits.getNumDataUnits() && n!=0); //make sure to do a zero check here or raw wav data won't save
			System.out.println("N raw data units: " + n + " N found data units: " + foundDataUnits.getNumDataUnits()); 
		}

		int flag=-1; 

		String currentFileS = createFileName(foundDataUnits.getFirstTimeMillis()); 
		File file = new File(currentFileS);

		if (hasAllWavClips) {
			flag = writeDetGroupWav(foundDataUnits, file, false); 
		}
		else if (!hasAllWavClips && haveRawData) {
			//no raw data
			flag = saveRawWav(start, end, rawDataBlock); 
		}
		else {
			flag = writeDetGroupWav(foundDataUnits, file, false); 
		}

		return flag; 
	}

	/**
	 * Save a clip of wav data from a pre existing .wav or other audio file within in PG. 
	 * 
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
	 * Save wav data from a data unit instead of from the raw file store. 
	 * @param foundDataUnits - the list of found data units. 
	 * @param currentFile - path to current file to save to. 
	 * @param zeroPad - if true will zeroPad detections. 
	 * @return the number of data units that were saved. 
	 */
	public int writeDetGroupWav(DetectionGroupSummary foundDataUnits, File filename, boolean zeroPad) {
		return writeDataUnitWav(foundDataUnits.getDataList(), filename, zeroPad);
	}
	
	
	/**
	 * Save data units which contain raw data to individual wav files within a folder. 
	 * @param foundDataUnits - list of data units to save.
	 * @param currentFile - the current folder. If this is a file name the parent directory will be used. 
	 * @return the number of data units saved
	 */
	public int writeDataUnitWavs(List<PamDataUnit> foundDataUnits, File currentFile) {
		int n=0; 
		WavFileWriter wavWrite = null; 
		for (PamDataUnit fnDataUnit: foundDataUnits){
			
			AudioFormat audioFormat = new Wav16AudioFormat(fnDataUnit.getParentDataBlock().getSampleRate(), PamUtils.getNumChannels(fnDataUnit.getChannelBitmap()));

			String currentFileS = createFileName(fnDataUnit.getTimeMilliseconds()); 

			//System.out.println("Save detection wav." + foundDataUnits.size());
			for (int i=0; i<wavDataUnitExports.size(); i++) {
				if (wavDataUnitExports.get(i).getUnitClass().isAssignableFrom(fnDataUnit.getClass())) {
					
					wavWrite= new WavFileWriter(currentFileS, audioFormat); 

					System.out.println("Write individual wav." + foundDataUnits.size());
					//save the wav file of detection
					wavWrite.append(wavDataUnitExports.get(i).getWavClip(fnDataUnit)); 
					n++; 
					
					wavWrite.close();
					
					break; 
				}
			}
		}
		
		return n;
	}

	/**
	 * Save data units which contain raw data to a wav file. Note that this assumed
	 * the data units all contain raw data, the raw data is at the same sample rate and number of channels 
	 * and the data units are in order. This will APPEND to the wav file if it currently exists. 
	 * 
	 * @param foundDataUnits - data units containing raw data.
	 * @param currentFile - path to current file to save to. 
	 * @param zeroPad - if true will zeroPad detections. 
	 * @return the number of data units saved - this should be the same as the size
	 *         of the data unit list.
	 */
	public int writeDataUnitWav(List<PamDataUnit> foundDataUnits, File currentFile, boolean zeroPad) {
		int n=0; 
		WavFileWriter wavWrite = null; 
		PamDataUnit lastfnDataUnit = null;
		
		if (foundDataUnits==null || foundDataUnits.size()<=0) {
			return 0;
		}
		
		//System.out.println("Save detection wav." + foundDataUnits.size());
		AudioFormat audioFormat = new Wav16AudioFormat(foundDataUnits.get(0).getParentDataBlock().getSampleRate(), PamUtils.getNumChannels(foundDataUnits.get(0).getChannelBitmap())); 
		
		wavWrite= new WavFileWriter(currentFile.getAbsolutePath(), audioFormat);
				
		for (PamDataUnit fnDataUnit: foundDataUnits){
			
			for (int i=0; i<wavDataUnitExports.size(); i++) {
				if (wavDataUnitExports.get(i).getUnitClass().isAssignableFrom(fnDataUnit.getClass())) {
					
					//System.out.println("Append wav. data unit: " + n + " samples: " + wavDataUnitExports.get(i).getWavClip(fnDataUnit)[0].length + " zeroPad: " + zeroPad);
					
					if (zeroPad && lastfnDataUnit!=null) {
						//we need to append zero samples between the detections. 
						long timeMillisDiff = fnDataUnit.getTimeMilliseconds() - lastfnDataUnit.getTimeMilliseconds();
						
						long sampleDiff =  fnDataUnit.getStartSample()  - lastfnDataUnit.getStartSample();
						
						int samplesPad;
						//are the two similar? - are they within 5 milliseconds
						if (Math.abs(((sampleDiff/audioFormat.getSampleRate())*1000.) - timeMillisDiff)<5){
							//use the sample diff
							samplesPad =  (int) sampleDiff;
						}
						else {
							//use time millis for padding. May indicate that data units are from different wav files. 
							samplesPad =  (int) ((((double) timeMillisDiff)/1000.)*audioFormat.getSampleRate());
						}
						
						//now safety check - is this more than one GB of data. Each sample is 16bits but the input double array is 64 bits each. 
						double size = samplesPad*16*audioFormat.getChannels()/1024/1024;
						
						//System.out.println("Append wav. zero pad" + samplesPad);
						//
						if (size>MAX_ZEROPAD_SIZE_MEGABYTES) {
							wavWrite.close();
							System.err.println(String.format("WavDetExport: A zero padding of %.2f MB was requested. The maximum allowed size is %.2f - "
									+ "the .wav file was closed and any additional data units have not been written %s", size, MAX_ZEROPAD_SIZE_MEGABYTES, currentFile));
							return n;
						}
						
						wavWrite.append(new double[audioFormat.getChannels()][samplesPad]);
					}
					
					//save the wav file of detection
					if (wavWrite.append(wavDataUnitExports.get(i).getWavClip(fnDataUnit))) { 
						n++; 
					}					
					lastfnDataUnit = fnDataUnit;
					break; 
				}
			}
		}
		
		wavWrite.close();

		
		//send a message that the wav file has saved
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
//						System.out.println("Write wav data: Time millis:   "+ dataUnit.getTimeMilliseconds()+ " channel: " + PamUtils.getSingleChannel(dataUnit.getChannelBitmap()) );
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


}
