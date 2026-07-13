package loggerForms.loggeraudio;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Mixer.Info;

import PamController.PamController;
import PamguardMVC.PamProcess;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkMessage;
import loggerForms.network.LoggerNetworkReceiver;
import loggerForms.network.LoggerNetworkSystem;
import wavFiles.ByteConverter;

public class LoggerAudioProcess extends PamProcess {

	private LoggerAudioControl loggerAudioControl;
	
	private boolean listening = false;
	
	private int sampleRate = 8000;
	
	static private final String listenTopic = "AudioData/#";
	
	private Map<String, PlatformAudio> platformAudios;

	private AudioFormat outputFormat = new AudioFormat(sampleRate, 16, 1, true, true);
	
	private ByteConverter inputByteConverter = ByteConverter.createByteConverter(2, true, Encoding.PCM_SIGNED);
	private ByteConverter outputByteConverter = ByteConverter.createByteConverter(2, true, Encoding.PCM_SIGNED);

	private SourceDataLine sourceDataLine;

	private int lineBuffSize;

	public LoggerAudioProcess(LoggerAudioControl loggerAudioControl) {
		super(loggerAudioControl, null);
		this.loggerAudioControl = loggerAudioControl;
		platformAudios = new TreeMap<>(); 
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			prepareOutput();
			setupListener();
		}
	}
	
	
	private boolean prepareOutput() {
		Info[] mixers = AudioSystem.getMixerInfo();
		if (mixers == null || mixers.length == 0) {
			return false;
		}
		loggerAudioControl.getLoggerAudioSettings().outputDevice = 6;
		int iMix = Math.min(mixers.length-1, loggerAudioControl.getLoggerAudioSettings().outputDevice);
		Info mixer = mixers[iMix];
		Mixer currentMixer = AudioSystem.getMixer(mixer);
		try {
			// try to get the device of choice ...
			sourceDataLine = (SourceDataLine) currentMixer.getLine(currentMixer.getSourceLineInfo()[0]);
			sourceDataLine.addLineListener(new SCLineListener());
			sourceDataLine.open(outputFormat);
			sourceDataLine.start();
			lineBuffSize = sourceDataLine.getBufferSize();
			System.out.printf("Sound card output buffer = %d bytes = %3.2fs\n", lineBuffSize, 
					(double) lineBuffSize / (double) outputFormat.getFrameRate() / outputFormat.getFrameSize());
		} catch (Exception Ex) {
			System.err.println(Ex.getMessage());
			sourceDataLine = null;
			return false;
		}
		
		
		return true;
	}

	private class SCLineListener implements LineListener {

		@Override
		public void update(LineEvent event) {
//			System.out.println(event.getClass().getName());
			Type type = event.getType();
			if (type == LineEvent.Type.START) {
//				deviceState.setStarted(true);
			}
			else if (type == LineEvent.Type.STOP) {
//				deviceState.setStarted(false);
			}
		}
		
	}

	public PlatformAudio getPlatformAudio(String key) {
		PlatformAudio p = platformAudios.get(key);
		if (p == null) {
			p = new PlatformAudio(this, key);
			platformAudios.put(key, p);
		}
		return p;
	}


	private void setupListener() {
		LoggerNetworkManager netManager = LoggerNetworkSystem.getManager();
		if (netManager != null && listening == false) {
			netManager.subsribeTopic(listenTopic, new LoggerNetworkReceiver() {
				@Override
				public boolean newMessage(LoggerNetworkMessage message) {
					audioReceived(message);
					return true;
				}
			});
			listening = true;
		}
	}

	private long lastOut = 0;
	protected void audioReceived(LoggerNetworkMessage message) {
		// get last topic id which is the sending platform
		int lastSlash = message.getTopic().lastIndexOf('/');
		if (lastSlash < 0) {
			return; // should never happen
		}
		String sender = message.getTopic().substring(lastSlash+1);
		// need to see if it's a new channel, because if it is, then we need
		// to notify a few things. 
		boolean isNew = false;
		PlatformAudio pfa = platformAudios.get(sender);
		if (pfa == null) {
			pfa = new PlatformAudio(this, sender);
			isNew = true;
			platformAudios.put(sender, pfa);
		}
		if (isNew) {
			
		}

//		 byteConverter = ByteConverter.createByteConverter(2, false, Encoding.PCM_SIGNED);
		// it's int16, so need half the number of samples. 
		// app is sending 1280 bytes, but we're receiving 1285. Wha'ts happening ? 
		byte[] audioBytes = Arrays.copyOfRange(message.getData(), 0, message.getData().length);
		int nBytes = audioBytes.length;
		int nSamples = nBytes/2;
		double[][] audio = new double[1][nSamples]; 
		inputByteConverter.bytesToDouble(audioBytes, audio, nBytes);
		double max = 0;
		for (int i = 0; i < 10; i++) {
			max = Math.max(Math.abs(audio[0][i]), max);
		}
		// what's in those first five bytes ? [0 8 -2 0 5]
		
		long now = System.currentTimeMillis();
		if (now - lastOut > 1000) {
			System.out.printf("%d(%d) bytes %d-%d received from %s - max level is %5.4f\n", 
					audioBytes.length, message.getData().length, audioBytes[0], audioBytes[1], sender, max);
			lastOut = now;
		}
		
		pfa.addAudioData(audio[0]);
		double[] outAudio = pfa.getBlock();
		if (outAudio != null) {
			double[][] out = {outAudio};
			byte[] outBytes = new byte[outAudio.length*2];
			outputByteConverter.doubleToBytes(out, outBytes, outAudio.length);
			sourceDataLine.write(outBytes, 0, outBytes.length);
		}
	}


	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}


}
