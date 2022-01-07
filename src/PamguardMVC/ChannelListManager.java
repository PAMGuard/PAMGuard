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

//	/**
//	 * Convert a channel number to a channel Index
//	 * @param channelNumber channel Number
//	 * @return channelIndex
//	 */
//	public abstract int channelNumberToIndex(int channelNumber);
	
//	/**
//	 * Convert a channel index to a channel number
//	 * @param channelIndex channel Index
//	 * @return channelNumber
//	 */
//	public abstract int channelIndexToNumber(int channelIndex);
//	
//	/**
//	 * Convert a channel number to a hydrophone number
//	 * @param channelNumber channel Number
//	 * @return HydrophoneNumber
//	 */
//	public abstract int channelNumberToPhone(int channelNumber);
//
//	/**
//	 * Convert a channel index to a hydrophone number
//	 * @param channelIndex channel Index
//	 * @return HydrophoneNumber
//	 */
	public abstract int channelIndexToPhone(int channelIndex);
	
//	/**
//	 * Convert a bitmap of channel numbers to a bitmap of chanel indexes
//	 * @param channelNumbers channel Numbers
//	 * @return channelIndexes
//	 */
//	public int channelNumbersToIndexes(int channelNumbers) {
//		int inds = 0;
//		int nChan = PamUtils.getNumChannels(channelNumbers);
//		for (int i = 0; i < nChan; i++) {
//			inds |= 1<<channelNumberToIndex(PamUtils.getNthChannel(i, channelNumbers));
//		}
//		return inds;
//	}
	
//	/**
//	 * Convert a bitmap of channel Indexes to channel Numbers
//	 * @param chanelIndexes chanel Indexes
//	 * @return channelNumbers
//	 */
//	public int channelIndexesToNumbers(int chanelIndexes) {
//		int numbs = 0;
//		int nChan = PamUtils.getNumChannels(chanelIndexes);
//		for (int i = 0; i < nChan; i++) {
//			numbs |= 1<<channelIndexToNumber(PamUtils.getNthChannel(i, chanelIndexes));
//		}
//		return numbs;
//	}
	
//	/**
//	 * Convert a bitmap of channel Numbers to a bitmap of phones
//	 * @param channelNumbers channel Numbers
//	 * @return phone numbers
//	 */
//	public int channelNumbersToPhones(int channelNumbers) {
//		int phones = 0;
//		int nChan = PamUtils.getNumChannels(channelNumbers);
//		for (int i = 0; i < nChan; i++) {
//			phones |= 1<<channelNumbersToPhones(PamUtils.getNthChannel(i, channelNumbers));
//		}
//		return phones;
//	}
	
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
	
//	/**
//	 * Convert an array of channel numbers into an array of channel indexes
//	 * @param channelNumbers channel Numbers
//	 * @return channel Indexes
//	 */
//	public int[] channelNumbersToIndexes(int[] channelNumbers) {
//		int[] inds = new int[channelNumbers.length];
//		for (int i = 0; i < channelNumbers.length; i++) {
//			inds[i] = channelNumberToIndex(channelNumbers[i]);
//		}
//		return inds;
//	}
	
//	/**
//	 * convert an array of channel indexes into an array of channel numbers
//	 * @param channelIndexes channel Indexes
//	 * @return channel numbers
//	 */
//	public int[] channelIndexesToNumbers(int[] channelIndexes) {
//		int[] numbs = new int[channelIndexes.length];
//		for (int i = 0; i < channelIndexes.length; i++) {
//			numbs[i] = channelIndexToNumber(channelIndexes[i]);
//		}
//		return numbs;
//		
//	}

//	/**
//	 * Convert an array of channel numbers into an array of hydrophone numbers
//	 * @param channelNumbers channel Numbers
//	 * @return hydrophone numbers
//	 */
//	public int[] channelNumbersToPhones(int[] channelNumbers) {
//		int[] phones = new int[channelNumbers.length];
//		for (int i = 0; i < channelNumbers.length; i++) {
//			phones[i] = channelNumberToPhone(channelNumbers[i]);
//		}
//		return phones;
//	}

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
