package loggerForms.loggeraudio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.Timer;

import PamDetection.RawDataUnit;
import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import loggerForms.loggeraudio.logging.LoggerAudioDataUnit;
import wavFiles.WavFileWriter;

/**
 * Store for audio data coming from one of the platform apps. This contains two queues, the
 * first is a simple list that receives data when they first arrive over the network. This frees
 * the network. This first queue is then emptied on a time, which puts the data into a more
 * persistent RawDataBlock OR writes to a wav file, as part of a buffered recording system. 
 */
public class PlatformAudio {

	private String platform;
	
	private String initials;
	
	private LoggerAudioProcess loggerAudioProcess;
	private List<RawDataUnit> audioQueue;
	private int maxQueueBytes = 8000*2;
	public long totalSamples;
	
	private Mixer currentMixer;
	private SourceDataLine sourceDataLine;
	private LoggerRawAudioDataBlock rawOutDataBlock;
	
	private WavFileWriter wavFileWriter;
	private long fileEndTime;
	private Timer fileEndTimer; // only needed in case data collection stops. Otherwise will happen when new data are added. 
	private Object writeLock = new Object();

	private long recordingStart;
	

	public PlatformAudio(LoggerAudioProcess loggerAudioProcess, String platform) {
		this.loggerAudioProcess = loggerAudioProcess;
		this.platform = platform;
		audioQueue = new LinkedList<>();
		rawOutDataBlock = new LoggerRawAudioDataBlock(platform, loggerAudioProcess, 1, loggerAudioProcess.appSampleRate);
		loggerAudioProcess.addOutputDataBlock(rawOutDataBlock);
		makeInitials();
		fileEndTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long now = System.currentTimeMillis();
				checkFileEndTime(now);
			}
		});
	}
	/**
	 * @return the totalSamples
	 */
	public long getTotalSamples() {
		return totalSamples;
	}

	/**
	 * @param totalSamples the totalSamples to set
	 */
	public void setTotalSamples(long totalSamples) {
		this.totalSamples = totalSamples;
	}

	/**
	 * @return the platform
	 */
	public String getPlatform() {
		return platform;
	}

	/**
	 * @return the currentMixer
	 */
	public Mixer getCurrentMixer() {
		return currentMixer;
	}

	/**
	 * @return the sourceDataLine
	 */
	public SourceDataLine getSourceDataLine() {
		return sourceDataLine;
	}

	protected void checkFileEndTime(long now) {
		if (now < fileEndTime) {
			return;
		}
		LoggerAudioDataUnit ladu = null;
		synchronized (writeLock) {
			if (wavFileWriter == null) {
				return;
			}
			wavFileWriter.close();
			long frames = wavFileWriter.getFileFrames();
			int seconds = (int) (frames / LoggerAudioProcess.appSampleRate);
			ladu = new LoggerAudioDataUnit(recordingStart, platform, wavFileWriter.getFileName(), seconds);
			
			wavFileWriter = null;
			fileEndTimer.stop();
		}
		if (ladu != null) {
			loggerAudioProcess.getAudioDataBlock().addPamData(ladu);
		}
	}

	private void makeInitials() {
		String[] split = platform.split(" ");
		initials = new String(split[0].substring(0, 1));
		for (int i = 1; i < split.length; i++) {
			initials += new String(split[i].substring(0, 1));
		}
	}

	/**
	 * Add audio data when it comes from the source. Should 
	 * probably change this to RawDataUnits so that they get time stamped asap. 
	 * @param data
	 */
	public void addAudioData(RawDataUnit data) {
		synchronized (audioQueue) {
			audioQueue.add(data);
		}
		trimQueue();
	}
	
	/**
	 * Called again when data have been read from the initial queue and 
	 * will be written to a datablock, or direct to a file. 
	 * @param rdu
	 */
	public void storeDataUnit(RawDataUnit rdu) {
		checkFileEndTime(rdu.getTimeMilliseconds());
		synchronized (writeLock) {
			if (wavFileWriter != null) {
				// add the data to the writer. 
				double[][] wData = {rdu.getRawData()};
				wavFileWriter.append(wData);
			}
			else {
				// store the data for the next write
				synchronized(rawOutDataBlock.getSynchLock()) {
					// onl write to the datablock if we're not writing a file. 
					rawOutDataBlock.addPamData(rdu);
				}
			}
		}
		
	}

	/**
	 * Trim queue to a maximum number of bytes. 
	 * @return number of blocks removed. 
	 */
	private int trimQueue() {
		int n = 0;
		synchronized (audioQueue) {
			while (getQueueSize() > maxQueueBytes) {
				audioQueue.remove(0);
				n++;
			}		
		}
		return n;
	}

	public int getQueueSize() {
		int sz = 0;
		synchronized (audioQueue) {
			for (RawDataUnit aData : audioQueue) {
				sz += aData.getRawData().length;
			}
		}
		return sz;
	}
	
	public RawDataUnit getBlock() {
		trimQueue();
		synchronized (audioQueue) {
			if (audioQueue.size() > 0) {
				return audioQueue.remove(0);
			}
		}
		return null;
	}

	public SourceDataLine getSourceDataLine(Mixer mixer, AudioFormat outputFormat) {
		if (mixer == null) {
			return null;
		}
		if (sourceDataLine != null && mixer == currentMixer) {
			return sourceDataLine;
		}
		if (sourceDataLine != null) {
			sourceDataLine.close();
			sourceDataLine = null;
		}
		try {
			sourceDataLine =  (SourceDataLine) mixer.getLine(mixer.getSourceLineInfo()[0]);
			sourceDataLine.open(outputFormat);
			sourceDataLine.start();
		} catch (LineUnavailableException e) {
			System.err.println("Error opening logger audio output line");
		}
		currentMixer = mixer;
		return sourceDataLine;
	}

	public void clearLine() {
		if (sourceDataLine == null) {
			return;
		}
		try {
			sourceDataLine.stop();
			sourceDataLine.flush();
			sourceDataLine.drain();
			sourceDataLine.close();
			sourceDataLine = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void clearQueue() {
		synchronized (audioQueue) {
			System.out.printf("Removing %d items from line %s\n", audioQueue.size(), platform);
			audioQueue.clear();
		}
		
	}

	public LoggerRawAudioDataBlock getRawOutDataBlock() {
		return rawOutDataBlock;
	}

	/**
	 * Make a recording, or continue a recording, as appropriate. 
	 * @return true if it seems OK. false if a detectable problem. 
	 */
	public boolean makeRecording() {
		LoggerAudioSettings settings = loggerAudioProcess.getLoggerAudioControl().getLoggerAudioSettings();
		// get the settings. These shouldn't be null, but you never know
		PlatformSettings platformSettings = settings.getStreamSettings(platform);
		if (platformSettings == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		fileEndTime = now + settings.recordSeconds * 1000;
		
		synchronized (writeLock) {
			if (wavFileWriter != null) {
				return true; // no need to do anything, record end has already been pushed back
			}
			// otherwise need to start a recording, grabbing buffer as we go. 
			synchronized (rawOutDataBlock.getSynchLock()) {
				recordingStart = now;
				ArrayList<RawDataUnit> bufferedData = rawOutDataBlock.getDataCopy();
				rawOutDataBlock.clearAll(); // nothing will ever be written a second time. 
				if (bufferedData.size() > 0) {
					recordingStart = bufferedData.get(0).getTimeMilliseconds();
				}		
				File outFolder = FileFunctions.getStorageFileFolder(settings.outputFolder,
						recordingStart, settings.outputSubFolders, true);
				String fileName = PamCalendar.createFileNameMillis(recordingStart, outFolder.getAbsolutePath(), 
						initials+"_", ".wav");
				AudioFormat af = new AudioFormat(LoggerAudioProcess.appSampleRate, 16, 1, true, false);
				wavFileWriter = new WavFileWriter(fileName, af);
				for (RawDataUnit rdu : bufferedData) {
					double[][] wDat = {rdu.getRawData()};
					wavFileWriter.append(wDat);
				}
				fileEndTimer.start(); // this is only needed in case data collection stops. 
			}
		}
		
		return false;
	}
	

}
