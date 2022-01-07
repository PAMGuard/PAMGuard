package annotation.classifier;

import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;
import annotation.dataselect.ScalarDataSelCreator;

public class ClassifierDataSelCreator extends ScalarDataSelCreator {

	private BaseClassificationAnnotationType clasAnnotationType;

	public ClassifierDataSelCreator(BaseClassificationAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
		clasAnnotationType = dataAnnotationType;
	}

	@Override
	public DataSelector createDataSelector(DataAnnotationType dataAnnotationType2, String selectorName,
			boolean allowScores, String selectorType) {
		// TODO Auto-generated method stub
		return new ClassifierDataSelector(clasAnnotationType, null, selectorName, allowScores);
	}

}
