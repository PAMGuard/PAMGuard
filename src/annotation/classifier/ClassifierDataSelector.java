package annotation.classifier;

import PamguardMVC.PamDataBlock;
import annotation.dataselect.ScalarDataSelector;

public class ClassifierDataSelector extends ScalarDataSelector<BaseClassificationAnnotation> {

	public ClassifierDataSelector(BaseClassificationAnnotationType annotationType, PamDataBlock pamDataBlock, String selectorName,
			boolean allowScores) {
		super(annotationType, pamDataBlock, selectorName, allowScores, ScalarDataSelector.USE_MINIMUM);
	}

	@Override
	public double getScalarValue(BaseClassificationAnnotation annotation) {
		return annotation.getScore();
	}

}
