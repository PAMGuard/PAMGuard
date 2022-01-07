package hfDaqCard;

import java.io.Serializable;

import Acquisition.DaqSystemXMLManager;
import Acquisition.SoundCardParameters;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.PamConstants;

public class SmruDaqParameters extends SoundCardParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 1L;

	public static final int NCHANNELS = 4;
	
	public static final int MAX_DEVICES = 8;
	
	public static final int[] sampleRates = {62500, 250000, 500000, 1000000};
	public static final double[] lineargains = {0, 1, 2, 4, 8, 16, 32, 64};
	private static double[] gains;
	public static final double[] filters = {10., 100., 2000., 20000., 0.};

	public static final int MAXSAMPLERATE = 1000000;

	public static final double VPEAKTOPEAK = 5.64;
	
	public int sampleRateIndex = 1;
	
	public int channelMask;
//	public int[] deviceIndex = new int[PamConstants.MAX_CHANNELS];
	public int[] channelGainIndex = new int[PamConstants.MAX_CHANNELS];
	public int[] channelFilterIndex = new int[PamConstants.MAX_CHANNELS];
	
	/**
	 * @param systemType
	 */
	public SmruDaqParameters(String systemType) {
		super(systemType);
	}

	/**
	 * Get the total number of boards that are actually used by the system. 
	 * @param installedBoards number of installed boards. 
	 * @return the number of used boards. 
	 */
	public int getNumUsedBoards(int installedBoards) {
		int nUsed = 0;
		int boardMask;
		for (int i = 0; i < installedBoards; i++) {
			boardMask = getChannelMask(i);
			if (boardMask != 0) {
				nUsed++;
			}
		}
		return nUsed;
	}
	
	/**
	 * Get the channel mask for a single board. 
	 * @param board board number
	 * @return channel mask
	 */
	public int getChannelMask(int board) {
		int maskOffset = board * NCHANNELS;
		int mask = channelMask>>maskOffset;
		return (mask & 0xF);
	}
	/**
	 * Get the gain index for a specified board and channel
	 * @param board board number
	 * @param channel channel number
	 * @return gain index to pass to device
	 */
	public int getGainIndex(int board, int channel) {
		return channelGainIndex[channel+board * NCHANNELS];
	}
	
	/**
	 * Get the filter index for a specified board and channel
	 * @param board board number
	 * @param channel channel number
	 * @return filter index to pass to device
	 */
	public int getFilterIndex(int board, int channel) {
		return channelFilterIndex[channel+board * NCHANNELS];
	}
	
	public static double[] getGains() {
		if (gains == null) {
			gains = new double[lineargains.length];
			for (int i = 0; i < lineargains.length; i++) {
				gains[i] = 20.*Math.log10(lineargains[i]);
			}
		}
		return gains;
	}
	
	public float getSampleRate() {
		return sampleRates[sampleRateIndex];
	}
	
	public double getChannelGain(int channel) {
		getGains();
		return gains[channelGainIndex[channel]];
	}
	
	public double getChannelFilter(int channel) {
		return filters[channelFilterIndex[channel]];
	}
	
	@Override
	public SmruDaqParameters clone() {
		SmruDaqParameters newParams = (SmruDaqParameters) super.clone();
		if (newParams.channelFilterIndex == null) {
			newParams = new SmruDaqParameters(null);
		}
//		if (newParams.deviceIndex == null) {
//			newParams.deviceIndex = new int[PamConstants.MAX_CHANNELS];
//		}
		return newParams;
	}
	
	public int getSampleRateIndex() {
		return sampleRateIndex;
	}
	
	public void setSampleRateIndex(int rateIndex) {
		sampleRateIndex = rateIndex;
	}

	public int getGainIndex(int channel) {
		return channelGainIndex[channel];
	}
	
	public void setGainIndex(int channel, int gainIndex) {
		channelGainIndex[channel] = gainIndex;
	}
	
	public int getFilterIndex(int channel) {
		return channelFilterIndex[channel];
	}
	
	public void setFilterIndex(int channel, int filterIndex) {
		channelFilterIndex[channel] = filterIndex;
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected a SMRU DAQ card, just return null
		if (!DaqSystemXMLManager.isSelected(SmruDaqSystem.newCardName)) {
			return null;
		}
		return super.getParameterSet();
	}
}
