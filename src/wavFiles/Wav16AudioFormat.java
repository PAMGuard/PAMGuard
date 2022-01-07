package wavFiles;

import javax.sound.sampled.AudioFormat;

/**
 * Quick constructor for 16 bit wav file audio formats. 
 * @author dg50
 *
 */
public class Wav16AudioFormat extends AudioFormat {

	/**
	 * @param sampleRate Sample rate, Hz
	 * @param nChannels Number of channels. 
	 */
	public Wav16AudioFormat(float sampleRate, int nChannels) {
		super(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, nChannels, nChannels*2, sampleRate, false);
	}

}
