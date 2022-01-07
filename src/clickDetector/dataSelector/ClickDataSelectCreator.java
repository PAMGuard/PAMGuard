package clickDetector.dataSelector;

import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.alarm.ClickAlarmParameters;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

public class ClickDataSelectCreator extends DataSelectorCreator {

	private ClickControl clickControl;
	private ClickDataBlock clickDataBlock;

	public ClickDataSelectCreator(ClickControl clickControl, ClickDataBlock clickDataBlock) {
		super(clickDataBlock);
		this.clickControl = clickControl;
		this.clickDataBlock = clickDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selType) {
		return new ClickDataSelector(clickControl, clickDataBlock, selectorName, allowScores, true);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new ClickAlarmParameters();
	}

	@Override
	protected DataSelector addSuperDetectionOptions(DataSelector ds, String selectorName, boolean allowScores,
			String selectorType) {
		return ds;
	}
	
	

}
