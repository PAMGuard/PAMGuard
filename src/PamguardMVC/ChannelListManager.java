package PamguardMVC;

import PamUtils.PamUtils;

/**
 * Functions to convert between 
 * <p>Channel lists - hardware channels which may not start at zero
 * <p>Channel indexes - software lists which are always 0,1,2, etc.
 * <p>Hydrophone numbers which are always 0,1,2, etc.
 * @author Doug
 *
 */
public abstract class ChannelListManager {

	/**
	 * Convert a channel index to a hydrophone number
	 * @param channelIndex channel Index, 0 - 31
	 * @return HydrophoneNumber
	 */
	public abstract int channelIndexToPhone(int channelIndex);
	

	
	/**
	 * Convert a bitmap of channel indexes to a bitmap of phones
	 * @param channelIndexes channel Indexes
	 * @return phone numbers
	 */
	public int channelIndexesToPhones(int channelIndexes) {
		int phones = 0;
		int newPhone;
		int nChan = PamUtils.getNumChannels(channelIndexes);
		for (int i = 0; i < nChan; i++) {
			newPhone = channelIndexToPhone(PamUtils.getNthChannel(i, channelIndexes));
			if (newPhone >= 0) { // avoid indexes where phone < 0 i.e. does not exist. 
				phones |= 1<<newPhone;
			}
		}
		return phones;
	}
	
	/**
	 * Convert a channel map to a list of hydrophones. Note
	 * that phones may not be 1:1 with channels, so while a channelBitmap of
	 * 3 would normally return {0,1}, if the hydrophones are remapped in the 
	 * array manager, then it might return {1,0}.
	 * @param channelBitmap
	 * @return
	 */
	public int[] channelMapToPhonesList(int channelBitmap) {
		int nChan = Integer.bitCount(channelBitmap);
		int[] phones = new int[nChan];
		for (int i = 0; i < nChan; i++) {
			int chan = PamUtils.getNthChannel(i, channelBitmap);
			int phone = channelIndexToPhone(chan);
			phones[i] = phone;
		}
		return phones;
	}
	
	/**
	 * Convert an array of channel indexes into an array of hydrophone numbers
	 * @param channelNumbers channel Numbers
	 * @return hydrophone numbers
	 */
	public int[] channelIndexesToPhones(int[] channelIndexes) {
		int[] phones = new int[channelIndexes.length];
		for (int i = 0; i < channelIndexes.length; i++) {
			phones[i] = channelIndexToPhone(channelIndexes[i]);
		}
		return phones;
	}
}
