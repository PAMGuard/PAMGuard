package Spectrogram;

import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class SpectrumBackgrounds {

	private FFTDataBlock fftDataBlock;
	/**
	 * @return the fftDataBlock
	 */
	public FFTDataBlock getFftDataBlock() {
		return fftDataBlock;
	}

	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}

	private int channelMap;
	private SpectrumBackground[] backgrounds = new SpectrumBackground[PamConstants.MAX_CHANNELS];

	public SpectrumBackgrounds(FFTDataBlock fftDataBlock, int channelMap) {
		this.fftDataBlock = fftDataBlock;
		this.channelMap = channelMap;		
	}
	
	/**
	 * Prepare a set of background measurements for the given channel map
	 * @param channelMap new channel map
	 * @param timeConstSecs Background time constant in seconds
	 * @return
	 */
	public boolean prepareS(int channelMap, double timeConstSecs) {
		this.channelMap = channelMap;
		return prepareS(timeConstSecs);
	}

	/**
	 * Prepare a set of background measurements using the set channel map
	 * @param timeConstSecs Background time constant in seconds
	 * @return true
	 */
	public boolean prepareS(double timeConstSecs) {
		boolean ok = true;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & channelMap) == 0) {
				continue;
			}
			if (backgrounds[i] == null) {
				backgrounds[i] = new SpectrumBackground(fftDataBlock, i);
			}
			ok |= backgrounds[i].prepareS(timeConstSecs);
		}
		return ok;
	}
	
	/**
	 * Process an FFT data unit. Find the correct background for
	 * that channel and update. 
	 * @param fftDataUnit FFT Data
	 */
	public void process(FFTDataUnit fftDataUnit) {
		int singleChan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		if (backgrounds[singleChan] != null) {
			backgrounds[singleChan].process(fftDataUnit);
		}
	}

	/**
	 * Get the background for a channel, or null if that 
	 * channel is not measured. 
	 * @param channel Channel index
	 * @return background data (magnitude squared, no corrections for FFT length, neg frequencies, etc.) 
	 */
	public double[] copyBackground(int channel) {
		if (backgrounds[channel] != null) {
			return backgrounds[channel].copyBackground();
		}
		else {
			return null;
		}
	}
	
}
