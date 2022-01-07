package cpod.dataSelector;

import PamController.PamControlledUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clickDetector.alarm.ClickAlarmParameters;
import cpod.CPODClickDataBlock;
import cpod.CPODControl;

/**
 * Creates CPOD data selectors. 
 * @author Jamie Macaulay 
 *
 */
public class CPODDataSelectorCreator extends DataSelectorCreator {

	private PamControlledUnit cpodDataSelector;
	private CPODClickDataBlock cpodDataBlock;

	public CPODDataSelectorCreator(PamControlledUnit clickControl, CPODClickDataBlock clickDataBlock) {
		super(clickDataBlock);
		this.cpodDataSelector = clickControl;
		this.cpodDataBlock = clickDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selType) {
		return new CPODDataSelector(cpodDataSelector, cpodDataBlock, selectorName, allowScores);
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
