package annotation.dataselect;

import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public abstract class AnnotationDataSelCreator<TDataAnnotation extends DataAnnotation> extends DataSelectorCreator {

	private DataAnnotationType<TDataAnnotation> dataAnnotationType;
	
	private static final String unitType = "Data Annotation Data Selector";

	public AnnotationDataSelCreator(DataAnnotationType dataAnnotationType) {
		super(null);
		this.dataAnnotationType = dataAnnotationType;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return createDataSelector(dataAnnotationType, selectorName, allowScores, selectorType);
	}

	public abstract DataSelector createDataSelector(DataAnnotationType<TDataAnnotation> dataAnnotationType,
			String selectorName, boolean allowScores, String selectorType);

	/**
	 * Need to make this abstract again so that individual classes
	 * are forced to override it. 
	 */
	@Override
	public String getUnitName() {
		return this.getClass().getName();
	}

	@Override
	public String getUnitType() {
		return unitType;
	}

}
