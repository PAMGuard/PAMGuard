package RightWhaleEdgeDetector.datasel;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

public class RWDataSelCreator extends DataSelectorCreator {

	public RWDataSelCreator(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new RWDataSelector(getPamDataBlock(), selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new RWDataSelParams();
	}

}
