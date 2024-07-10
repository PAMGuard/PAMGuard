package rawDeepLearningClassifier.dataSelector;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import clickDetector.dataSelector.ClickDataSelector;
import rawDeepLearningClassifier.DLControl;

/**
 * Creates a data selector for the deep learning module. 
 * 
 * @author Jamie Macaulay
 */
public class DLDataSelectCreator extends DataSelectorCreator  {

	private DLControl dlcontrol;

	public DLDataSelectCreator(DLControl dlcontrol, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.dlcontrol = dlcontrol; 
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new DLDataSelectorParams();
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new DLDataSelector(dlcontrol, this.getPamDataBlock(), selectorName, allowScores, selectorType);
	}

}
