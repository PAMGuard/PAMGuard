package clickDetector.dataSelector;

import Map.MapPanel;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.OfflineEventDataBlock;

public class ClickTrainDataSelectorCreator extends DataSelectorCreator {

	private ClickControl clickControl;
	private OfflineEventDataBlock offlineEventDataBlock;
	/**
	 * @param pamDataBlock
	 * @param clickControl
	 * @param offlineEventDataBlock
	 */
	public ClickTrainDataSelectorCreator(ClickControl clickControl,
			OfflineEventDataBlock offlineEventDataBlock) {
		super(offlineEventDataBlock);
		this.clickControl = clickControl;
		this.offlineEventDataBlock = offlineEventDataBlock;
	}
	
	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		if (selectorType == null) selectorType = "";
		switch (selectorType) {
		case MapPanel.DATASELECTNAME:
			return new ClickTrainDataSelector(clickControl, offlineEventDataBlock, selectorName, allowScores);
		default:
			return new ClickTrainDataSelector2(clickControl, offlineEventDataBlock, selectorName, allowScores);
		}
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new ClickTrainSelectParameters();
	}

}
