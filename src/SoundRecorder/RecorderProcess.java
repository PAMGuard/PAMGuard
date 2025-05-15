package SoundRecorder;

import java.util.Arrays;
import java.util.ListIterator;

import javax.sound.sampled.AudioFileFormat;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.CheckStorageFolder;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

/**
 * Process raw audio data prior to storage. Raw data blocks
 * only contain one channel of data each. RecorderProcess stacks
 * up the data from all channels before passing it on the the 
 * RecorderStorage
 * @author Doug
 * @see SoundRecorder.RecorderStorage
 * @see SoundRecorder.RecorderControl
 */
public class RecorderProcess extends PamProcess {

	private RecorderControl recorderControl;

	private int collectedChannels;

	private long sampleStartTime;

	private long lastRecordedSample;

	private double[][] soundData;

	private RecordingInfo recordingInfo;

	private PamDataBlock<RecorderDataUnit> recordingData;

	/**
	 * Data flowing in from source (i.e. Pam Started)
	 */
	private boolean dataComing;

	/**
	 * Flag that when recording starts the buffer should be grabbed
	 * and inserted at the start of the recording. If recording is 
	 * already running, then this flag will have no effect. It is always
	 * cleared at the end of a recording. 
	 * It's an integer value, so that different recorder triggers can demand 
	 * different amounts of buffer. 
	 */
	protected double grabBuffer = 0;

	private String actionTrigger;

	public RecorderProcess(RecorderControl recorderControl) {

		super(recorderControl, null);

		this.recorderControl = recorderControl;

		recordingData = new PamDataBlock<RecorderDataUnit>(RecorderDataUnit.class, "Recordings", this, 0);

		recordingData.SetLogging(new RecorderLogger(recorderControl, recordingData));

		addOutputDataBlock(recordingData);

	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		long history = 0;
		if (recorderControl.recorderSettings.enableBuffer) {
			history = (recorderControl.recorderSettings.bufferLength + 1) * 1000;
		}
		history = Math.max(history, 
				(long) (recorderControl.recorderSettings.getLongestHistory() * 1000.));
		return history;
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {

		recorderControl.newData((PamDataBlock) o, arg);

		RawDataUnit rawDataUnit = (RawDataUnit) arg;

		if (recorderControl.getRecorderStatus() == RecorderControl.IDLE) return;

		/*
		 * Checks on file size no longer take place here, but in recordData()
		 * so all this needs to do is to check to see if a new file needs
		 * opening and if it does, to write the buffer to it if necessary. 
		 */

		/*
		 * at this point, we may need to start a recording - the controller
		 * tells us we want one - if there isn't one, start one, getting the buffer
		 * if necessary before starting to throw new data in. This is all done here so
		 * ensure that any data in the buffer is synchronised with anything here. 
		 */ 
		if (recorderControl.recorderStorage.getFileName() == null) {
			/* need to set up and start a recording, and to use all the data in 
			 *  the buffer if desired. 
			 *  the file name is based on time, so the first thing to do is to work
			 *  this out based on the time of the first data block that will actually be used. 
			 *  This may be the current one, or it may be earlier in the buffered data.   
			 */
			PamRawDataBlock b = (PamRawDataBlock) o;
			RawDataUnit dataUnit;
			long timeNow = arg.getTimeMilliseconds();
			long recordingStart = timeNow;
			if (recorderControl.recorderSettings.enableBuffer && grabBuffer > 0) {
				synchronized (b.getSynchLock()) {
					ListIterator<RawDataUnit> rawIterator = b.getListIterator(0);
					dataUnit = b.getFirstUnit();
					if (dataUnit != null) {
						recordingStart -= (grabBuffer * 1000.);
						recordingStart = Math.max(recordingStart, dataUnit.getTimeMilliseconds());
						// also, since the buffer isn't emptied, check that we don't ever overlap
						// recordings. 
						recordingStart = Math.max(recordingStart, lastRecordedSample);
					}

				}
			}
			if (recorderControl.recorderSettings.getFileType() == null) {
				recorderControl.recorderSettings.setFileType(AudioFileFormat.Type.WAVE);
			}
			//			if (true) {
			//			System.out.println("Opening storage at sample " + rawDataUnit.getStartSample() +
			//					" Samples since last opening = " + (rawDataUnit.getStartSample() - lastOpening));
			//			lastOpening = rawDataUnit.getStartSample();
			//			}
			int chanMap = recorderControl.recorderSettings.getChannelBitmap(b.getChannelMap());
			recorderControl.recorderStorage.openStorage(recorderControl.recorderSettings.getFileType(),
					recordingStart, getSampleRate(), PamUtils.getNumChannels(chanMap), recorderControl.recorderSettings.bitDepth);
			recordingInfo = new RecordingInfo(recorderControl.recorderStorage.getFileName(),
					getSampleRate(), chanMap, recorderControl.recorderSettings.bitDepth, recordingStart, 
					rawDataUnit.getStartSample(), actionTrigger);
			synchronized (b.getSynchLock()) {
				ListIterator<RawDataUnit> rawIterator = b.getListIterator(0);
				while (rawIterator.hasNext()) {
					dataUnit = rawIterator.next();
					if (dataUnit == null) break;
					if (dataUnit.getTimeMilliseconds() < recordingStart) continue;

					recordData(o, dataUnit);
//					System.out.println("REcord sample " + dataUnit.getStartSample());
					/**
					 * Aha ! When processing fast, e.g. decimating a load of files offline, by the time this function has
					 * actually locked the datablock, several more data units have arrived in the queue. these will 
					 * appear in this function as subsequent calls and get recorded below. So while this loop is good 
					 * for recording existing DU's at the start, it needs to get out when it's caught up so that 
					 * units don't record twice. 
					 */
					if (dataUnit == arg) {
						break;
					}
				}
			}
			recorderControl.sayRecorderStatus();
		}
		else {
			/*
			 * If this file is already open, then can just get on with it.
			 * If it just went through the file opening process, then this unit
			 * will already have been recorded at the end of the loop above which
			 * is why this is contained within the else{}.
			 */
			recordData(o, rawDataUnit);
//			System.out.println("REcord sample " + rawDataUnit.getStartSample());
		}
	}

	/**
	 * See how many more samples we can record into the current file based on 
	 * both file length options and file size options. 
	 * @return number of frames that can still fit in this file or -1
	 * if no file is open. 
	 */
	private long getRemainingRecordFrames() {
		if (recorderControl.recorderStorage.getFileName() == null) {
			return -1;
		}
		RecorderSettings recSet = recorderControl.recorderSettings;
		long frameSize = recSet.bitDepth/8 * recSet.getNumChannels();
		
		long maxFileBytes = recorderControl.recorderStorage.getMaxFileSizeBytes(); // absolute limit for all files whatever the user wants !

		if (recSet.limitLengthSeconds && recSet.maxLengthSeconds > 0) {
			long maxBytes =  (long) (recSet.maxLengthSeconds * frameSize * (double) getSampleRate());
			maxFileBytes = Math.min(maxFileBytes, maxBytes);
			if (recSet.isRoundFileStarts()) {
				// be a bit more clever and work out how many more samples we can make to get onto 
				// then next "nice" time. 
				long fileStart = recorderControl.recorderStorage.getFileStartTime();
				long fileEnd = fileStart/(recSet.maxLengthSeconds*1000);
				fileEnd = (fileEnd+1) * recSet.maxLengthSeconds*1000;
				long fileMillis = fileEnd-fileStart;
				if (fileMillis < 2000) fileMillis += recSet.maxLengthSeconds*1000;
				maxFileBytes = (long) (fileMillis * frameSize * (double) getSampleRate() / 1000.);
			}
		}		
		if (recSet.limitLengthMegaBytes && recSet.maxLengthMegaBytes > 0){
			maxFileBytes = Math.min(maxFileBytes, ((long) recSet.maxLengthMegaBytes)<<20);
		}
		
		return maxFileBytes / frameSize - recorderControl.recorderStorage.getFileFrames();
	}
	
	int bCount = 0;
	/**
	 * RecordData has to stack up all the channels so that we've a full 
	 * set, then sends on to recordSoundData. 
	 * @param o
	 * @param rawDataUnit
	 */
	private void recordData(PamObservable o, RawDataUnit rawDataUnit) {		

		int wantedChannels = recorderControl.recorderSettings.getChannelBitmap(0xFFFFFFFF);

		/*
		 * Is it one of the channels we want ? If not get out straight away.  
		 */
		if ((rawDataUnit.getChannelBitmap() & wantedChannels) == 0) return;

		int nChannels = PamUtils.getNumChannels(wantedChannels);
		if (soundData == null || soundData.length != nChannels) {
			soundData = new double[nChannels][];
		}
		/*
		 * there is a mapping of channel numbers if not all are being used
		 * which needs to be sorted out here. For instance, if there were 
		 * two channels 0 and 1, and we're only recording channel 1, then 
		 * the data from channel one needs to go into position 0 in the soundData
		 */
		int thisChannel = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		int channelPos = PamUtils.getChannelPos(thisChannel, wantedChannels);
		if (channelPos < 0) return;
		if (collectedChannels > 0 && sampleStartTime != rawDataUnit.getStartSample()) {
			// data coming from a different sample start time - reset
			collectedChannels = 0;
		}
		sampleStartTime = rawDataUnit.getStartSample();
		collectedChannels = PamUtils.SetBit(collectedChannels, thisChannel, true);
		soundData[channelPos] = rawDataUnit.getRawData();

		if (collectedChannels == wantedChannels) {
			lastRecordedSample = rawDataUnit.getTimeMilliseconds();
			recordSoundData(lastRecordedSample, soundData);
			collectedChannels = 0;
			recordingInfo.endTimeMillis = lastRecordedSample;
			soundData = new double[soundData.length][];
		}
	}
	
	/**
	 * Writes sound data to file or files - makes checks to see that the 
	 * file size limits have not been exceeded, opening new files as necessary
	 * and splitting the data as necessary. 
	 * @param dataTimeMillis
	 * @param soundData
	 * @return
	 */
	private boolean recordSoundData(long dataTimeMillis, double[][] soundData){

		if (soundData == null) return false;
		long availableFrames = getRemainingRecordFrames();
//		long currentFileFrames = recorderControl.recorderStorage.getFileFrames();
		long newFrames = soundData[0].length;
		int firstWrite = (int) Math.min(newFrames, availableFrames);
		int secondWrite = (int) (newFrames - firstWrite);
		if (secondWrite == 0) {
			return recorderControl.recorderStorage.addData(dataTimeMillis, soundData);
		}		
		else {
			double[][] sp = getSoundPart(soundData, 0, firstWrite);
			boolean ok1 = recorderControl.recorderStorage.addData(dataTimeMillis, sp);
			long newMillis = dataTimeMillis + (long) (firstWrite * 1000./(double)getSampleRate());
//			System.out.printf("Wrote %d frames to file %s\n", recorderControl.recorderStorage.getFileFrames(),recorderControl.recorderStorage.getFileName());
			recorderControl.recorderStorage.reOpenStorage(newMillis);
			sp = getSoundPart(soundData, firstWrite, secondWrite);
			boolean ok2 = recorderControl.recorderStorage.addData(newMillis, sp);
			return ok1 & ok2;
		}

	}
	
	/**
	 * Get just part of the sound to write, copied into new arrays. 
	 * @param soundData2
	 * @param i
	 * @param firstWrite
	 */
	private double[][] getSoundPart(double[][] soundData, int startSample, int nSamples) {
		int nChan = soundData.length;
		double[][] sp = new double[nChan][];
		for (int i = 0; i < nChan; i++) {
			sp[i] = Arrays.copyOfRange(soundData[i], startSample, startSample+nSamples);
		}
		return sp;
	}

	protected void setRecordStatus(int status, String actionTrigger) {
		this.actionTrigger = actionTrigger;
		if (status == RecorderControl.RECORDING) {
			startRecording(false);
		}
		else {
			stopRecording();
		}
	}

	private boolean startRecording(boolean forceStart) {
		// if it's already recording, then there is nothing to do.
		if (forceStart) {
			stopRecording();
		}
		return true;
	}

	protected boolean stopRecording() {
		grabBuffer = 0;
		recorderControl.recorderStorage.closeStorage();
		return true;	
	}

	protected void storageClosed() {
		RecorderDataUnit newDataUnit = new RecorderDataUnit(recordingInfo.startTimeMillis, recordingInfo);
		recordingData.addPamData(newDataUnit);		
	}


	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		recorderControl.setSampleRate(sampleRate);
	}

	@Override
	public void prepareProcess() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			CheckStorageFolder scf = new CheckStorageFolder(recorderControl.getUnitName());
			boolean ok = scf.checkPath(recorderControl.recorderSettings.outputFolder, true);
			recorderControl.setFolderStatus(ok);
		}
	}

	@Override
	public void pamStart() {
		
		// if the recorder file type is set to X3 by accident, change it here to wav
		if (recorderControl.recorderSettings.getFileType() == RecorderControl.X3) {
			String warnTxt = "<html>You have selected X3 as the output format for the Sound Recorder module.  This format has " +
					"not been implemented in the standard PAMGuard application, and should only be used when generating an XML " +
					"settings file for a Decimus unit.  The format will be changed to WAV.";
			WarnOnce.showWarning(PamController.getMainFrame(), "Sound Recorder Output Format", warnTxt, WarnOnce.OK_OPTION);
			recorderControl.recorderSettings.setFileType(AudioFileFormat.Type.WAVE);
			recorderControl.selectRecorderStorage();
		}

		dataComing = true;
		if (getParentDataBlock() != null) {
			recorderControl.recorderSettings.getChannelBitmap(getParentDataBlock().getChannelMap());
		}
		
		collectedChannels = 0;

		recorderControl.enableRecording();

		// press whichever button was last pressed...
		if (recorderControl.recorderSettings.autoStart) {
			recorderControl.buttonCommand(recorderControl.recorderSettings.oldStatus);
		}
		else {
			recorderControl.buttonCommand(recorderControl.recorderSettings.startStatus);
		}

	}

	@Override
	public void pamStop() {
		/*
		 * If it's set to autoStart, get the current status
		 */
		//		if (recorderControl.recorderSettings.autoStart &&
		//				PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING) {
		//			recorderControl.recorderSettings.oldStatus = recorderControl.pressedButton;
		//		}
		//		else {
		//			recorderControl.recorderSettings.oldStatus = RecorderView.BUTTON_OFF;
		//		}
		// Close off any current file.
		recorderControl.buttonCommand(RecorderView.BUTTON_OFF);
		//recorderControl.recorderStorage.closeStorage();
		dataComing = false;
		recorderControl.enableRecording();
		// don't do this, it creates a condition that occasionally crashed PAMGuard if this gets called while it's writing. 
//		soundData = null;
	}


	public boolean isDataComing() {
		return dataComing;
	}


}
