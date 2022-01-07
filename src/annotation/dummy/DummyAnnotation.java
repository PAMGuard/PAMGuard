package annotation.dummy;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;

public class DummyAnnotation extends DataAnnotation<DataAnnotationType> {

	private AnnotationBinaryData annotationBinaryData;
	
	public DummyAnnotation(DataAnnotationType dataAnnotationType, AnnotationBinaryData annotationBinaryData) {
		super(dataAnnotationType);
		this.annotationBinaryData = annotationBinaryData;
	}

	/**
	 * @return the annotationBinaryData
	 */
	public AnnotationBinaryData getAnnotationBinaryData() {
		return annotationBinaryData;
	}


}
