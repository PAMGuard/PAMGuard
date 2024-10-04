package soundPlayback;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;

import Acquisition.SoundCardSystem;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import warnings.PamWarning;
import warnings.QuickWarning;
import warnings.WarningSystem;

/**
 * Base sound card playback to use in the SoundCardPlayback and 
 * the SoundCardFilePlayback classes. 
 * @author dg50
 *
 */
public class SoundCardPlaybackBase {

	private AudioFormat audioFormat;

	private volatile SourceDataLine sourceDataLine;

//	private byte[] rawAudio;
	
	private PamWarning soundWarning= new PamWarning("Sound Card Output", "", 0);
	
	private int lineBuffSize;

	private QuickWarning soundCardWarning;

	private boolean isRealTime;
	
	private PlayDeviceState deviceState = new PlayDeviceState();

	private Mixer currentMixer;

	private int currentDeviceNumber;
	
	private Timer checkTimer;
	
	/**
	 * 
	 */
	public SoundCardPlaybackBase() {
		super();
		soundCardWarning = new QuickWarning("Sound card playback");
	}

	public int getMaxChannels() {
		return 2;
	}

	public String getName() {
		return "Sound Card Playback";
	}
	
	public String getDeviceName() {
		if (currentMixer == null) {
			return getName();
		}
		String name = currentMixer.getMixerInfo().getName();
		if (currentDeviceNumber == 0) {
			// then it's actually using device 1, since 0 is just the default line
			ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
			if (mixers.size() >= 2) {
				name += ": " + mixers.get(1).getName();
			}
		}
		return name;
	}
	
	public synchronized boolean preparePlayback(int deviceNumber, int nChannels, float playbackRate, boolean isRealTime) {

		unPrepareSystem();
		
//		Debug.out.println("Prepare Soundcard  system " + PamCalendar.formatTime(System.currentTimeMillis()));
		
		audioFormat = new AudioFormat(playbackRate, 16, nChannels, true, true);

		ArrayList<Mixer.Info> mixerinfos = SoundCardSystem.getOutputMixerList();
		if (mixerinfos == null || mixerinfos.size() == 0) {
			// give up if there are no mixers on the system
			return false;
		}
		if (deviceNumber >= mixerinfos.size() || deviceNumber < 0) {
			deviceNumber = 0;// reset to default device. 
		}
		Mixer.Info thisMixerInfo = mixerinfos.get(deviceNumber);
		currentMixer = AudioSystem.getMixer(thisMixerInfo);
		if (currentMixer.getSourceLineInfo().length <= 0){
			currentMixer.getLineInfo();
			return false;
		}
		
		currentDeviceNumber = deviceNumber;
		
		this.isRealTime = isRealTime;
		deviceState.reset();

		try {
			// try to get the device of choice ...
			sourceDataLine = (SourceDataLine) currentMixer.getLine(currentMixer.getSourceLineInfo()[0]);
			sourceDataLine.addLineListener(new SCLineListener());
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
			lineBuffSize = sourceDataLine.getBufferSize();
//			System.out.printf("Sound card output buffer = %d bytes = %3.2fs\n", lineBuffSize, 
//					(double) lineBuffSize / (double) audioFormat.getFrameRate() / audioFormat.getFrameSize());
		} catch (Exception Ex) {
			System.err.println(Ex.getMessage());
			sourceDataLine = null;
			return false;
		}
//		startPlayback = System.currentTimeMillis();

		checkTimer = new Timer();
		checkTimer.scheduleAtFixedRate(new CheckTimer(), 100, 100);

		return true;
	}

	synchronized public boolean unPrepareSystem() {
		
		if (checkTimer != null) {
			checkTimer.cancel();
		}
		
		if (sourceDataLine == null) return false;
		sourceDataLine.stop();
		sourceDataLine.close();
//		sourceDataLine = null;
		return true;
	}

	public boolean playData(RawDataUnit[] data) {
//		Debug.out.println("Playing data on line " + sourceDataLine + " From " + this);
		if (sourceDataLine == null) {
			return false;
		}
//		callCount++;
//		if (callCount % 100 == 0) {
//			System.out.printf("%s, bytes available = %d of %d\n", PamCalendar.formatTime(PamCalendar.getTimeInMillis()), 
//					sourceDataLine.available(), lineBuffSize);
//		}
		/*
		 * need to check the buffer size - will be wrong the first time, but can then write easily
		 * to it. then write data into the buffer then write buffer to dataline
		 */
		int nChan = data.length;		
		
		int sampleSize = 2;
		RawDataUnit dataUnit = data[0];
		int nSamples = dataUnit.getSampleDuration().intValue();
		int bufferSize = nSamples * sampleSize * nChan; 
//		if (rawAudio == null || rawAudio.length != bufferSize) {
			byte[] rawAudio = new byte[bufferSize];
//		}
		// now write the data to the buffer, packing as we go.
		int byteNo;
		double[] rawData;
		short int16Data;
		for (int iChan = 0; iChan < nChan; iChan++) {
			byteNo = iChan * sampleSize;
			rawData = data[iChan].getRawData();
			for (int i = 0; i < rawData.length; i++) {
				int16Data = (short) (rawData[i] * 32767);
				rawAudio[byteNo+1] = (byte) (int16Data & 0xFF);
				rawAudio[byteNo] = (byte) (int16Data>>>8 & 0xFF);
				byteNo += nChan * sampleSize;
			}
		}
		
		boolean writeOK = writeToCard(rawAudio);
		if (!writeOK) {
//			String msg = "Unable to write data to sound card";
//			System.out.println(msg);			
			soundWarning.setWarnignLevel(1);
			soundWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			WarningSystem.getWarningSystem().addWarning(soundWarning);
		}
		return writeOK;
	}

	private boolean writeToCard(byte[] rawAudio) {
		int bufferSize = rawAudio.length;
		int chunkSize = Math.min(1024, bufferSize);
		int toWrite = bufferSize;
		int wrote = 0;
		while (toWrite > 0) {
			int thisWrite = Math.min(toWrite, chunkSize); 
			thisWrite = writeToCard(rawAudio, wrote, thisWrite);
			if (thisWrite == 0) {
//				System.out.println("Zero write");
				return false;
			}
			wrote += thisWrite;
			toWrite -= thisWrite;
		}
		return wrote == rawAudio.length;
	}
	
	private int writeToCard(byte[] rawAudio, int offset, int toWrite) {
//		playCount++;
		int waitStep = 5;
		int availale1 = sourceDataLine.available();
		int weeWaits = 0;
		int maxWaits = isRealTime ? 20 : 100;
		while (availale1 <= lineBuffSize/2 && weeWaits < maxWaits) {
			try {
				Thread.sleep(waitStep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			weeWaits++;
			availale1 = sourceDataLine.available();
		}
		if (availale1 <= lineBuffSize/2 && isRealTime) { // still no space to write data so give up
//			System.out.println("Skip data " + availale1);
			deviceState.addDataDumpled(toWrite);
					return 0;
		}
		if (weeWaits >= maxWaits && isRealTime) {
			String msg = String.format("%s Waited %d millis to write %d bytes of sound data, %d now available",
					PamCalendar.formatTime(PamCalendar.getTimeInMillis(), true),
					weeWaits*waitStep, toWrite, availale1);
//			System.out.println(msg);
			soundWarning.setWarningMessage(msg);
			soundWarning.setWarnignLevel(1);
			soundWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			WarningSystem.getWarningSystem().addWarning(soundWarning);
		}
//		long bN = System.currentTimeMillis();
		int written = 0;
		try {
			written = sourceDataLine.write(rawAudio, offset, toWrite);
			deviceState.addDataPlayed(written);
		}
		catch (Exception e) {
			soundWarning.setWarningMessage("Write Error: " + e.getMessage());
			soundWarning.setWarnignLevel(2);
			soundWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			WarningSystem.getWarningSystem().addWarning(soundWarning);
			return 0;
		}
		if (written != toWrite) {
			soundWarning.setWarningMessage(String.format("Sound out wrote only %d of %d bytes", written, toWrite));
			soundWarning.setWarnignLevel(2);
			soundWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			WarningSystem.getWarningSystem().addWarning(soundWarning);
			return written;
		}
//		else if (availale1 == lineBuffSize || playCount%1000 < 0) {
//			int availale2 = sourceDataLine.available();
//			soundWarning.setWarningMessage(String.format("Sound out wrote %d availableBytes of %d changed from %d to %d (%d)", 
//					written, lineBuffSize, availale1, availale2, availale2-availale1));
//			soundWarning.setWarnignLevel(1);
//			soundWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
////			System.out.println(soundWarning.getWarningMessage());
//			WarningSystem.getWarningSystem().addWarning(soundWarning);
//		}
		else {
//			WarningSystem.getWarningSystem().removeWarning(soundWarning);
		}
//		long aN = System.currentTimeMillis() - bN;
//		System.out.println(String.format("T %3.2f Wrote %d of %d into buffer with %d available in %d millis", 
//				(double)(bN-startPlayback)/1000., written, bufferSize, available, aN));
		
		return written;
	}

	public boolean stopPlayback() {
		if (sourceDataLine == null) return false;
		sourceDataLine.stop();
		sourceDataLine.close();
		sourceDataLine = null;
		return true;
	}

	private class CheckTimer extends TimerTask {

		private int zeroCount = 0;
		private int callCount = 0;
		
		@Override
		public void run() {
			callCount++;
			// copy so it's not overwritten to null while this operates. 
			SourceDataLine line = sourceDataLine;
			if (line == null) {
				return;
			}

//			Debug.out.println("Checking audioline in "   + SoundCardPlaybackBase.this);
			int available = line.available();
			Boolean running = line.isRunning();
			Boolean active = line.isActive();
			if (available == 0) {
				zeroCount++;
				
			}
			else {
				zeroCount = 0;
			}
			if (zeroCount > 0 || callCount%50 == 0) {
				String devName = "Unknown";
				try {
					Mixer mixer = AudioSystem.getMixer(currentMixer.getMixerInfo());
					devName = mixer.getMixerInfo().getName();
				}
				catch (IllegalArgumentException e) {
					devName = e.getMessage(); 
					zeroCount = 10;
				}
//				Debug.out.printf("Playing line %s avail %d, running %s, active %s, nZ = %d %s\n", "", available, 
//						running.toString(), active.toString(), zeroCount, devName);
				Info[] mixerInfos = AudioSystem.getMixerInfo();
				ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
//				Boolean supported = AudioSystem.isLineSupported(AudioSystem.getLine(mixerInfos[1].));
//				Debug.out.printf("Mixer 1 is \"%s\" [%s]\n", mixerInfos[1].getName(), mixerInfos[1].getDescription());
			}
			if (zeroCount > 5  & line.isOpen()) {
//				soundCardWarning.clearWarning();
				soundCardWarning.setWarning("Emergency shut down of audio output line since no data being written", 2);
				line.close();
			}
			else {
				soundCardWarning.clearWarning();
			}
		}
		
	}

	private class SCLineListener implements LineListener {

		@Override
		public void update(LineEvent event) {
//			System.out.println(event.getClass().getName());
			Type type = event.getType();
			if (type == LineEvent.Type.START) {
				deviceState.setStarted(true);
			}
			else if (type == LineEvent.Type.STOP) {
				deviceState.setStarted(false);
			}
		}
		
	}

	public PlayDeviceState getDeviceState() {
		return deviceState;
	}
}
