package loggerForms.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import loggerForms.controlDescriptions.ControlDescription;

/**
 * 
 * Data select creator for a single logger control. 
 * @author Dougl
 *
 */
public abstract class ControlDataSelCreator extends DataSelectorCreator {

	private ControlDescription controlDescription;

	public ControlDataSelCreator(ControlDescription controlDescription, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.controlDescription = controlDescription;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return createDataSelector(selectorName, allowScores, selectorType, controlDescription);
	}

	public abstract ControlDataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType,
			ControlDescription controlDescription);

	@Override
	public synchronized ControlDataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return (ControlDataSelector) super.getDataSelector(selectorName, allowScores, selectorType);
	}


}
