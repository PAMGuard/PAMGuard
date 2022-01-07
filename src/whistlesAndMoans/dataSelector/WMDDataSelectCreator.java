package whistlesAndMoans.dataSelector;

import whistlesAndMoans.WhistleMoanControl;
import whistlesAndMoans.alarm.WMAlarmParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

/**
 * Default data selector for the whsitle and moan detector. 
 * @author Doug Gillespie
 *
 */
public class WMDDataSelectCreator extends DataSelectorCreator {

	private WhistleMoanControl wmControl;

	public WMDDataSelectCreator(WhistleMoanControl wmControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.wmControl = wmControl;
	}

	@Override
	public DataSelector createDataSelector(String selectorName,
			boolean allowScores, String selectorType) {
		return new WMDDataSelector(wmControl, getPamDataBlock(), selectorName, allowScores);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelectorCreator#createNewParams(java.lang.String)
	 */
	@Override
	public DataSelectParams createNewParams(String name) {
		return new WMAlarmParameters();
	}

}
