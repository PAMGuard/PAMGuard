package soundPlayback;

import Acquisition.SoundCardSystem;
import PamDetection.RawDataUnit;
import soundPlayback.fx.PlaybackSettingsPane;
import soundPlayback.fx.SoundCardPlaybackPane;
import soundPlayback.swing.PlaybackDialogComponent;
import soundPlayback.swing.soundCardDialogComponent;

public class SoundCardPlayback extends PlaybackSystem {
	
	private soundCardDialogComponent soundCardDialogComponent;
	
	private SoundCardPlaybackBase soundCardPlaybackBase;
	
	/**
	 * The SoundCard playback pane. 
	 */
	private SoundCardPlaybackPane soundCardPlaybackPane;
	
	public SoundCardPlayback(SoundCardSystem soundCardSystem) {
		soundCardDialogComponent = new soundCardDialogComponent(soundCardSystem);
		soundCardPlaybackBase = new SoundCardPlaybackBase();
	}

	@Override
	public PlaybackDialogComponent getDialogComponent() {
		return soundCardDialogComponent;
	}

	@Override
	public int getMaxChannels() {
		return soundCardPlaybackBase.getMaxChannels();
	}

	@Override
	public String getName() {
		return soundCardPlaybackBase.getName();
	}

	@Override
	public String getDeviceName() {
		return soundCardPlaybackBase.getDeviceName();
	}

	@Override
	synchronized public boolean prepareSystem(PlaybackControl playbackControl,
			int nChannels, float playbackRate) {
		
		return soundCardPlaybackBase.preparePlayback(playbackControl.playbackParameters.deviceNumber, 
				nChannels, playbackRate, true);
//		Debug.out.println("Preparing sound card for output");
//
//		unPrepareSystem();
//		
//		if (nChannels <= 0 || nChannels > getMaxChannels()) return false;
//		
//		audioFormat = new AudioFormat(sampleRate, 16, nChannels, true, true);
//		
//		int selDeviceNumber = playbackControl.playbackParameters.deviceNumber;
//
//		ArrayList<Mixer.Info> mixerinfos = SoundCardSystem.getOutputMixerList();
//		if (mixerinfos == null || mixerinfos.size() == 0) {
//			currentMixer = null;
//			return false;
//		}
//		else if (selDeviceNumber >= mixerinfos.size()) {
//			selDeviceNumber = 0;
//		}
//		Mixer.Info thisMixerInfo = mixerinfos.get(selDeviceNumber);
//		currentMixer = AudioSystem.getMixer(thisMixerInfo);
//		if (currentMixer.getSourceLineInfo().length <= 0){
//			currentMixer.getLineInfo();
//			return false;
//		}
//		
//		try {
//			// try to get the device of choice ...
//			boolean supported = currentMixer.isLineSupported(currentMixer.getSourceLineInfo()[0]);
//			if (!supported) {
//				Debug.out.println("Line not supported: " + currentMixer.getSourceLineInfo()[0]);
//				sourceDataLine = null;
//				return false;
//			}
//			sourceDataLine = (SourceDataLine) currentMixer.getLine(currentMixer.getSourceLineInfo()[0]);
//			
//			sourceDataLine.open(audioFormat);
//			sourceDataLine.start();
//			sourceDataLine.addLineListener(new SourceLineListener());
//		} catch (Exception Ex) {
////			Ex.printStackTrace();
//			System.err.println(Ex.getLocalizedMessage());
//			unPrepareSystem();
//			sourceDataLine = null;
//			return false;
//		}
//		
//		return true;
	}
	
//	private class SourceLineListener implements LineListener {
//
//		@Override
//		public void update(LineEvent event) {
//			Debug.out.println("Audio line event " + event.getType());
//		}
//		
//	}
	
	@Override
	public boolean unPrepareSystem() {
		return soundCardPlaybackBase.unPrepareSystem();
	}

//	
//	private int lastAvailable = 0;
//	private boolean lastActive = false;
//	private boolean lastRunning = false;
	
	public boolean playData(RawDataUnit[] data, double gain) {

		return soundCardPlaybackBase.playData(data);
//		if (sourceDataLine == null) return false;
//		/*
//		 * need to check the buffer size - will be wrong the first time, but can then write easily
//		 * to it. then write data into the buffer then write buffer to dataline
//		 */
//		int nChan = data.length;
//		int sampleSize = 2;
//		RawDataUnit dataUnit = data[0];
//		int nSamples = dataUnit.getSampleDuration().intValue();
//		int bufferSize = nSamples * sampleSize * nChan; 
//		if (rawAudio == null || rawAudio.length != bufferSize) {
//			rawAudio = new byte[bufferSize];
//		}
//		// now write the data to the buffer, packing as we go.
//		int byteNo;
//		double[] rawData;
//		short int16Data;
//		for (int iChan = 0; iChan < nChan; iChan++) {
//			byteNo = iChan * sampleSize;
//			rawData = data[iChan].getRawData();
//			for (int i = 0; i < rawData.length; i++) {
//				int16Data = (short) (rawData[i] * 32768 * gain);
//				rawAudio[byteNo+1] = (byte) (int16Data & 0xFF);
//				rawAudio[byteNo] = (byte) (int16Data>>>8 & 0xFF);
//				byteNo += nChan * sampleSize;
//			}
//		}
//		
//		int available = sourceDataLine.available();
//		boolean running = sourceDataLine.isRunning();
//		boolean active = sourceDataLine.isActive();
//		if (running != lastRunning) {
//			Debug.out.println("Sound Out running status changed to " + running);
//			lastRunning = running;
//		}
//		if (active != lastActive) {
//			Debug.out.println("Sound Out active status changed to " + active);
//			lastActive = active;
//		}
//		
//		int wrote = sourceDataLine.write(rawAudio, 0, bufferSize);
////		if (wrote != bufferSize) {
////			prepareSystem();
////		}
//			
//		return wrote == bufferSize;
	}

	@Override
	public PlaybackSettingsPane getSettingsPane() {
		if (soundCardPlaybackPane==null) {
			soundCardPlaybackPane = new SoundCardPlaybackPane(this); 
		}
		return soundCardPlaybackPane;
	}

}
