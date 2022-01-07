package patchPanel;

import PamguardMVC.ChannelListManager;
import PamguardMVC.PamRawDataBlock;

public class PatchPanelDataBlock extends PamRawDataBlock {

	private PatchPanelProcess patchPanelProcess;
	
	private PatchPanelControl patchPanelControl;
	
	private PatchPanelChannelList patchPanelChannelList;
	
	public PatchPanelDataBlock(String name, PatchPanelProcess parentProcess,
			int channelMap, float sampleRate) {
		super(name, parentProcess, channelMap, sampleRate);
		this.patchPanelProcess = parentProcess;
		this.patchPanelControl = patchPanelProcess.patchPanelControl;
		patchPanelChannelList = new PatchPanelChannelList(patchPanelControl);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamRawDataBlock#getChannelListManager()
	 */
	@Override
	public ChannelListManager getChannelListManager() {
		return patchPanelChannelList;
	}
	
	public PatchPanelChannelList getPatchPanelChannelListManager() {
		return patchPanelChannelList;
	}

}
