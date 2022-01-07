package PamguardMVC.dataSelector;

import PamguardMVC.PamDataBlock;

/**
 * Null data selector that doesn't actually create a data selector. This can be used
 * by datablocks which still might get annotations which have data selectors. 
 * @author Douglas Gillespie
 *
 */
public class NullDataSelectorCreator extends DataSelectorCreator {

	public NullDataSelectorCreator(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return null;
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return null;
	}

}
