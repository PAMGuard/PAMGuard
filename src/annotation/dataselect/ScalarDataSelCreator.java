package annotation.dataselect;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;

public abstract class ScalarDataSelCreator extends AnnotationDataSelCreator {

	public ScalarDataSelCreator(DataAnnotationType dataAnnotationType) {
		super(dataAnnotationType);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new ScalarDataParams();
	}

}
