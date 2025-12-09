package soundPlayback;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.SwingWorker;


/**
 * Some static functions to playback sound clips. 
 * Should eventually amalgamate this a bit with the main SoundPlayback module 
 * used by the viewer!
 * @author doug
 *
 */
public class ClipPlayback {

	private static ClipPlayback singleInstance;
	
	private ClipPlayback() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get the singleton clip playback object. 
	 * @return clip playback class. 
	 */
	public static ClipPlayback getInstance() {
		if (singleInstance == null) {
			singleInstance = new ClipPlayback();
		}
		return singleInstance;
	}
	
	/**
	 * Play one or two channels of audio data back through the default sound card. 
	 * @param clipData one or two channels of audio data. 
	 * @param sampleRate playback sample rate
	 * @param autoScale if true, will autoscale the data to full range of system (i.e. max val will be 32767). 
	 * @return true if played OK, false otherwise. 
	 */
	public boolean playClip(double[][] clipData, float sampleRate, boolean autoScale) {
		// will really have to rethread this, since we don't want this to block. 
		if (clipData.length > 2) {
			clipData = Arrays.copyOf(clipData, 2);
		}
		PlayWorker playWorker = new PlayWorker(clipData, sampleRate, autoScale);
		playWorker.execute();
		
		return true;
	}
	
	protected class PlayWorker extends SwingWorker<Integer, PlayClipProgress> {

		private double[][] clipData;
		private float sampleRate;
		private boolean autoScale;
		private Clip currentClip;
		private AudioFormat audioFormat;

		public PlayWorker(double[][] clipData, float sampleRate, boolean autoScale) {
			this.clipData = clipData;
			this.sampleRate = sampleRate;
			this.autoScale = autoScale;
		}
		
		@Override
		protected Integer doInBackground() throws Exception {
			int nChan = clipData.length;
			int nSamps = clipData[0].length;
			currentClip = AudioSystem.getClip();
			audioFormat = new AudioFormat(sampleRate, 16, nChan, true, true);
			ByteBuffer byteBuffer = ByteBuffer.allocate(nChan*nSamps*2).order(ByteOrder.BIG_ENDIAN);
			ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
			double scale = 32767.;
			double maxVal = 0;
			if (autoScale) {
				for (int iC = 0; iC < nChan; iC++) {
					for (int iS = 0; iS < nSamps; iS++) {
						maxVal = Math.max(maxVal, Math.abs(clipData[iC][iS]));
					}
				}
				scale /= maxVal;
			}
			for (int iS = 0; iS < nSamps; iS++) {
				for (int iC = 0; iC < nChan; iC++) {
					shortBuffer.put((short) (clipData[iC][iS] * scale));
				}
			}
			try { 
				currentClip.open(audioFormat, byteBuffer.array(), 0, nChan*nSamps*2);
				currentClip.start();
			}
			catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
