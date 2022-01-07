package loggerForms.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import loggerForms.controlDescriptions.ControlDescription;

public class LookupDataSelCreator extends ControlDataSelCreator {

	public LookupDataSelCreator(ControlDescription controlDescription, PamDataBlock pamDataBlock) {
		super(controlDescription, pamDataBlock);
	}

	@Override
	public ControlDataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType,
			ControlDescription controlDescription) {
		return new LookupDataSelector(getPamDataBlock(), selectorName, allowScores, controlDescription);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new LookupDataSelParams();
	}


}
