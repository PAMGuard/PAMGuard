package patchPanel;

import PamguardMVC.ChannelListManager;
import PamguardMVC.DefaultChannelListManager;
import PamguardMVC.PamRawDataBlock;

public class PatchPanelChannelList extends ChannelListManager {

	private PatchPanelControl patchPanelControl;
	
	private ChannelListManager parentListManager;
	
	public PatchPanelChannelList(PatchPanelControl patchPanelControl) {
		super();
		this.patchPanelControl = patchPanelControl;
	}

//	@Override
//	public int channelIndexToNumber(int channelIndex) {
//		int outputChans = patchPanelControl.patchPanelParameters.getOutputChannels();
//		return PamUtils.getNthChannel(channelIndex, outputChans);
//	}

	@Override
	public int channelIndexToPhone(int channelIndex) {
//		int chanNum = channelIndexToNumber(channelIndex);
		if (channelIndex < 0) {
			return -1;
		}
		channelIndex = patchPanelControl.patchPanelParameters.getFirstChannels()[channelIndex];
		return parentListManager.channelIndexToPhone(channelIndex);
	}

//	@Override
//	public int channelNumberToIndex(int channelNumber) {
//		int outputChans = patchPanelControl.patchPanelParameters.getOutputChannels();
//		return PamUtils.getChannelPos(channelNumber, outputChans);
//	}
//
//	@Override
//	public int channelNumberToPhone(int channelNumber) {
//		int chanNum = patchPanelControl.patchPanelParameters.getFirstChannels()[channelNumber];
//		return parentListManager.channelNumberToPhone(chanNum);
//	}

	public void findParentListManager(PamRawDataBlock parentDataBlock) {

		if (parentDataBlock == null) {
			parentListManager = new DefaultChannelListManager();
			return;
		}
		parentListManager = parentDataBlock.getChannelListManager();
	}
}
