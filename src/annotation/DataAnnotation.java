package annotation;

import PamguardMVC.PamDataUnit;

/**
 * Some kind of data annotation, i.e. data, such as a comment
 * added to a data unit. 
 * @author Doug Gillespie
 *
 */
public abstract class DataAnnotation<TDataAnnotationType extends DataAnnotationType> {

	private TDataAnnotationType dataAnnotationType;

	public DataAnnotation(TDataAnnotationType dataAnnotationType) {
		super();
		this.dataAnnotationType = dataAnnotationType;
	}

	public TDataAnnotationType getDataAnnotationType() {
		return dataAnnotationType;
	}
	
	/**
	 * Add annotation to a data unit. This is called from BinaryStore.unpackAnnotationData
	 * <br>It needs called here so that annotations can do a few other things to the data unit, 
	 * e.g. if the annotation is a localisation, it will need to override this and set itself as
	 * the localisation, as well as setting itself as an annotation. 
	 * @param dataUnit
	 */
	public void addToDataUnit(PamDataUnit dataUnit) {
		dataUnit.addDataAnnotation(this);
	}


}
