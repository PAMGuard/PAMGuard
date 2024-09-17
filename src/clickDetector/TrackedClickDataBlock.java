package clickDetector;

import PamguardMVC.PamProcess;

public class TrackedClickDataBlock extends ClickDataBlock {

	private ClickControl clickControl;
		
	public TrackedClickDataBlock(ClickControl clickControl, PamProcess parentProcess, int channelMap) {
		super(clickControl, parentProcess, channelMap);
		setDataName(clickControl.getDataBlockPrefix() + "Tracked Clicks");
		this.clickControl = clickControl;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public void addPamData(ClickDetection pamDataUnit) {
		super.addPamData(pamDataUnit, pamDataUnit.getUID());
	}
}
