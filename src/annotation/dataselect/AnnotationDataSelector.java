package annotation.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public abstract class AnnotationDataSelector<TDataAnnotation extends DataAnnotation<?>> extends DataSelector {

	private DataAnnotationType<TDataAnnotation> annotationType;

	/**
	 * @return the annotationType
	 */
	public DataAnnotationType<TDataAnnotation> getAnnotationType() {
		return annotationType;
	}

	public AnnotationDataSelector(DataAnnotationType<TDataAnnotation> annotationType, PamDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.annotationType = annotationType;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
			return 1;
		}
		TDataAnnotation annotation = (TDataAnnotation) pamDataUnit.findDataAnnotation(annotationType.getAnnotationClass());
		return scoreData(pamDataUnit, annotation);
	}

	/**
	 * Score the data based on the annotaiton. 
	 * @param pamDataUnit
	 * @param annotation might be null
	 * @return 
	 */
	protected abstract double scoreData(PamDataUnit pamDataUnit, TDataAnnotation annotation);

	@Override
	public String getLongSelectorName() {
		return annotationType.getAnnotationName() + ":" + getSelectorName();
	}
	
}
