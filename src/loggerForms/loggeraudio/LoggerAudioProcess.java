package loggerForms.loggeraudio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import Acquisition.SoundCardSystem;

import javax.sound.sampled.Mixer.Info;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import loggerForms.loggeraudio.logging.LoggerAudioDataBlock;
import loggerForms.loggeraudio.logging.LoggerAudioLogging;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkMessage;
import loggerForms.network.LoggerNetworkReceiver;
import loggerForms.network.LoggerNetworkSystem;
import wavFiles.ByteConverter;

public class LoggerAudioProcess extends PamProcess {

	private LoggerAudioControl loggerAudioControl;

	/**
	 * @return the loggerAudioControl
	 */
	public LoggerAudioControl getLoggerAudioControl() {
		return loggerAudioControl;
	}

	private boolean listening = false;

	public static final int appSampleRate = 8000;

	static private final String listenTopic = "AudioData/#";

	private Map<String, PlatformAudio> platformAudios;

	private AudioFormat outputFormat = new AudioFormat(appSampleRate, 16, 2, true, true);

	private ByteConverter inputByteConverter = ByteConverter.createByteConverter(2, false, Encoding.PCM_SIGNED);
	private ByteConverter outputByteConverter = ByteConverter.createByteConverter(2, true, Encoding.PCM_SIGNED);

	private int lineBuffSize;

	private boolean running;

	private Mixer currentMixer;

	private Timer queueTimer; 
	
	private LoggerAudioDataBlock audioDataBlock;

	public LoggerAudioProcess(LoggerAudioControl loggerAudioControl) {
		super(loggerAudioControl, null);
		this.loggerAudioControl = loggerAudioControl;
		platformAudios = new TreeMap<>(); 
		audioDataBlock = new LoggerAudioDataBlock(this);
		audioDataBlock.SetLogging(new LoggerAudioLogging(audioDataBlock));
		addOutputDataBlock(audioDataBlock);
		
		queueTimer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				queueTimerAction();
			}
		});
		if (loggerAudioControl.isViewer() == false) {
			queueTimer.start();
		}
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

		closeOutput();

		ArrayList<Info> mixers = SoundCardSystem.getOutputMixerList();
		if (mixers == null || mixers.size() == 0) {
			return false;
		}
		Info mixer = loggerAudioControl.getLoggerAudioSettings().findMixer();
		currentMixer = AudioSystem.getMixer(mixer);

		return true;
	}

	private void closeOutput() {
		try {
			// clear audio lines. 
			Set<String> keys = platformAudios.keySet();
			for (String key : keys) {
				PlatformAudio pfa = platformAudios.get(key);
				pfa.clearLine();
			}

			Line[] sls = currentMixer.getSourceLines();
			if (sls != null) {
				for (int i = 0; i < sls.length; i++) {
					//					sls[i].
				}
			}
			if (currentMixer != null) {
				//				currentMixer.
				currentMixer.close();
				currentMixer = null;
			}
		}
		catch (Exception e) {}
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
			else if (type == LineEvent.Type.OPEN) {

			}
		}

	}

	/**
	 * Get plaform audio. Always create if not there. 
	 * @param key
	 * @return
	 */
	public PlatformAudio getPlatformAudio(String key) {
		PlatformAudio p = platformAudios.get(key);
		if (p == null) {
			p = new PlatformAudio(this, key);
			platformAudios.put(key, p);
		}
		return p;
	}
	
	/**
	 * Get platform audio. Don't create, so return null if it's not there. 
	 * @param key
	 * @return
	 */
	public PlatformAudio findPlatformAudio(String key) {
		return platformAudios.get(key);
	}


	private void setupListener() {
		LoggerNetworkManager netManager = LoggerNetworkSystem.getManager();
		if (netManager != null && listening == false) {
			netManager.subsribeTopic(listenTopic, new LoggerNetworkReceiver() {
				@Override
				public boolean newMessage(LoggerNetworkMessage message) {
					try {
						audioReceived(message);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			});
			listening = true;
		}
	}

	private long lastOut = 0;

	private long startTime;
	
	/**
	 * Network callback. repacks the data as double and dumps it into a queue for each platform. 
	 * A different thread will read the queues and use the data so that this is never blocked
	 * by writing to the output device. 
	 * @param message
	 */
	protected void audioReceived(LoggerNetworkMessage message) {
		// get last topic id which is the sending platform
		long now = System.currentTimeMillis();
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
			loggerAudioControl.checkActionsMap(sender);
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
		RawDataUnit rdu = new RawDataUnit(now, 1, pfa.totalSamples, audio[0].length);
		rdu.setRawData(audio[0]);
		
		pfa.addAudioData(rdu);
		pfa.totalSamples += audio[0].length;
		// what's in those first five bytes ? [0 8 -2 0 5]
		//		if (now - lastOut > 1000) {
		//			System.out.printf("%d(%d) bytes %d-%d received from %s - tot samples %d max level is %5.4f\n", 
		//					audioBytes.length, message.getData().length, audioBytes[0], audioBytes[1], sender, pfa.totalSamples, max);
		//			lastOut = now;
		//		}


	}

	/**
	 *  timer action to empty queues, done here so that the callback
	 *  from MQTT can always return immediately. Will also do each queue in a 
	 *  separate thread in the hope that they interleave better
	 */	
	protected void queueTimerAction() {
		Set<String> keys = platformAudios.keySet();
		Thread[] threads = new Thread[keys.size()];
		int i = 0;
		for (String key : keys) {
			PlatformAudio pfa = platformAudios.get(key);
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					emptyQueue(pfa);
				}
			});
			threads[i].start();
			i++;
		}
		// wait for all to finish so that we can never have two threads writing to the same line
		for (i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Empties network data que for each platform. Data are put into 
	 * a datablock, and also sent to the output device. 
	 * Note that the data may not be correctly interleaved in the 
	 * datablock - may want to change this to a block per platform ?  
	 * @param pfa
	 */
	private void emptyQueue(PlatformAudio pfa) {

		RawDataUnit rdu;
		LoggerRawAudioDataBlock rawOutDataBlock = pfa.getRawOutDataBlock();
		rawOutDataBlock.setNaturalLifetime(loggerAudioControl.getLoggerAudioSettings().bufferSeconds);
		long now = System.currentTimeMillis();
		
		while ((rdu = pfa.getUnit()) != null) {
			
			pfa.storeDataUnit(rdu);

			PlatformSettings platSettings = loggerAudioControl.getLoggerAudioSettings().getStreamSettings(pfa.getPlatform());

			double[] interleaved = interleaveAudio(rdu.getRawData(), platSettings.outputChannel);
			double[][] out = {interleaved};
			byte[] outBytes = new byte[interleaved.length*2];
			outputByteConverter.doubleToBytes(out, outBytes, interleaved.length);
			SourceDataLine dataLine = pfa.getSourceDataLine(currentMixer, outputFormat);
			if (dataLine != null) {
				long tic = System.currentTimeMillis();
				dataLine.write(outBytes, 0, outBytes.length);
				long toc = System.currentTimeMillis();
				int bs = dataLine.getBufferSize();
				if (toc-tic >= 2) {
					pfa.clearQueue();
					dataLine.flush();
					dataLine.drain();
					int aa = dataLine.available();
					//				if (now - lastOut > 1000 && platSettings.outputChannel == 1) {
					System.out.printf("Line buffer size chan %d is %d, avail %d, write took %d millis\n", platSettings.outputChannel, bs, aa, toc-tic);
					//				System.out.printf("%d(%d) bytes %d-%d received from %s - tot samples %d max level is %5.4f\n", 
					//						audioBytes.length, message.getData().length, audioBytes[0], audioBytes[1], sender, pfa.totalSamples, max);
//					lastOut = now;
				}
			}
			// and clear up old data. Since PAMGuard probably isn't running the clearup won't get called, so do it here. 
			synchronized(rawOutDataBlock.getSynchLock()) {
				rawOutDataBlock.clearold(System.currentTimeMillis() - loggerAudioControl.getLoggerAudioSettings().bufferSeconds*1000);
			}
		}
	}

	/**
	 * Interleave data with zeros so that it can be sent to a single channel 
	 * of a stereo output line. 
	 * @param data
	 * @param offset
	 * @return
	 */
	private double[] interleaveAudio(double[] data, int offset) {
		int n = data.length;
		double[] out = new double[data.length * 2];
		for (int i = 0, j = offset; i < data.length; i++, j += 2) {
			out[j] = data[i];
		}
		return out;
	}


	@Override
	public float getSampleRate() {
		return appSampleRate;
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		sampleRate = this.appSampleRate;
		super.setSampleRate(sampleRate, notify);
	}

	/**
	 * @return the audioDataBlock
	 */
	public LoggerAudioDataBlock getAudioDataBlock() {
		return audioDataBlock;
	}


	@Override
	public void pamStart() {
		startTime = PamCalendar.getTimeInMillis();
		running = true;
	}

	@Override
	public void pamStop() {
		running = false;
	}


}
