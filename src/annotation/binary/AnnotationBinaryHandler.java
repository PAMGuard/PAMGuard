package annotation.binary;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

/**
 * Functions used by a specific DataAnnotationType to read and write binary data
 * settings and retrieving that information in PamDataUnits. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public abstract class AnnotationBinaryHandler<T extends DataAnnotation<?>> {

	private DataAnnotationType<T> dataAnnotationType;

	public AnnotationBinaryHandler(DataAnnotationType<T> dataAnnotationType) {
		this.dataAnnotationType = dataAnnotationType;
	}
	
	public abstract AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation);

	public abstract T setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData);

	/**
	 * @return the dataAnnotationType
	 */
	public DataAnnotationType<T> getDataAnnotationType() {
		return dataAnnotationType;
	}
		
}
