package clickTrainDetector.dataselector;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clickDetector.alarm.ClickAlarmParameters;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;

/**
 * The click train data selector. Used primarily for map drawing. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class CTDataSelectCreator extends DataSelectorCreator {
	
	/**
	 * Reference to the click train control
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Reference to a click train data block. Can be the unconfirmed, temporary or classified data block. 
	 */
	private ClickTrainDataBlock clickTrainDataBlock;

	public CTDataSelectCreator(ClickTrainControl clickTrainControl, ClickTrainDataBlock  clickTrainDataBlock) {
		super(clickTrainDataBlock);
		
		this.clickTrainControl = clickTrainControl;
		this.clickTrainDataBlock = clickTrainDataBlock;
		
		
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selType) {
		return new CTDataSelector(clickTrainControl, clickTrainDataBlock, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new ClickAlarmParameters();
	}
}
