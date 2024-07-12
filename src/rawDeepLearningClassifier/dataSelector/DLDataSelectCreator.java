package rawDeepLearningClassifier.dataSelector;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;
import annotation.dataselect.AnnotationDataSelCreator;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;

/**
 * Creates a data selector for the deep learning module. 
 * 
 * @author Jamie Macaulay
 */
public class DLDataSelectCreator extends AnnotationDataSelCreator<DLAnnotation>  {

	private DLControl dlcontrol;

	public DLDataSelectCreator(DLControl dlcontrol, DLAnnotationType type) {
		super(type); 
		this.dlcontrol = dlcontrol; 
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new DLDataSelectorParams();
	}

	@Override
	public DataSelector createDataSelector(DataAnnotationType<DLAnnotation> dataAnnotationType, String selectorName,
			boolean allowScores, String selectorType) {
		// TODO Auto-generated method stub
		return new DLDataSelector(dlcontrol, dataAnnotationType, null,
				selectorName, allowScores);
	}



}
