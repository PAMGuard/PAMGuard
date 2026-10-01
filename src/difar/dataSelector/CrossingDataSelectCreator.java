package difar.dataSelector;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

/**
 * Makes crossing data selectors for the crossing block.
 */
public class CrossingDataSelectCreator extends DataSelectorCreator {

	public CrossingDataSelectCreator(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new CrossingDataSelector(getPamDataBlock(), selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new CrossingSelectParams();
	}
}
