package Acquisition;

import PamguardMVC.ChannelListManager;

/**
 * Class for converting channel indexes and numbers to hydrophones
 * for the main PAMGAURD DAQ. 
 * <p>
 * All these functions are passed through to the AcquisitionParameters class
 * but these ones are better organised and therefore much easier to use than
 * the total mess in the AcquisitionParameters class
 * @author Doug Gillespie
 * @see AcquisitionParameters
 *
 */
public class DAQChannelListManager extends ChannelListManager {

	private AcquisitionControl acquisitionControl;
	
	public DAQChannelListManager(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
	}

//	@Override
//	public int channelIndexToNumber(int channelIndex) {
//
//		return acquisitionControl.acquisitionParameters.getChannelList(channelIndex);
//		
//	}

	@Override
	public int channelIndexToPhone(int channelIndex) {
		int[] phoneList = acquisitionControl.acquisitionParameters.getHydrophoneList();
		if (channelIndex >= phoneList.length) {
			return -1;
		}
		else {
			return phoneList[channelIndex];
		}
	}

//	@Override
//	public int channelNumberToIndex(int channelNumber) {
//		return acquisitionControl.acquisitionParameters.getChannelListIndexes(channelNumber);
//	}
//
//	@Override
//	public int channelNumberToPhone(int channelNumber) {
//		return acquisitionControl.acquisitionParameters.getHydrophone(channelNumber);
//	}

}
