package annotation.dataselect;

import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;

public class SNRDataSelCreator extends ScalarDataSelCreator {

	public SNRDataSelCreator(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	@Override
	public DataSelector createDataSelector(DataAnnotationType dataAnnotationType, String selectorName,
			boolean allowScores, String selectorType) {
		return new SNRDataSelector(dataAnnotationType, null, selectorName, allowScores, ScalarDataSelector.USE_MINIMUM);
	}

}
