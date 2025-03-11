package whistleClassifier.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import whistleClassifier.WhistleClassificationDataBlock;
import whistleClassifier.WhistleClassifierControl;

public class WslClsDataSelectCreator extends DataSelectorCreator {

	private WhistleClassifierControl wslClassifierControl;
	private WhistleClassificationDataBlock wslClassifierDataBlock;

	public WslClsDataSelectCreator(WhistleClassifierControl wslClassifierControl, WhistleClassificationDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.wslClassifierControl = wslClassifierControl;
		this.wslClassifierDataBlock = pamDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new WslClsDataSelector(wslClassifierControl, wslClassifierDataBlock, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new WslClsSelectorParams();
	}

}
