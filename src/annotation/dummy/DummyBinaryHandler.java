package annotation.dummy;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryData;
import annotation.binary.AnnotationBinaryHandler;

public class DummyBinaryHandler extends AnnotationBinaryHandler<DummyAnnotation>{

	private DummyAnnotationType dummyAnnotationType;

	public DummyBinaryHandler(DummyAnnotationType dummyAnnotationType) {
		super(dummyAnnotationType);
		this.dummyAnnotationType = dummyAnnotationType;
	}

	@Override
	public AnnotationBinaryData getAnnotationBinaryData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		DummyAnnotation dummyAnnotation = (DummyAnnotation) annotation;
		return dummyAnnotation.getAnnotationBinaryData();
	}

	@Override
	public DummyAnnotation setAnnotationBinaryData(PamDataUnit pamDataUnit, AnnotationBinaryData annotationBinaryData) {
		return new DummyAnnotation(dummyAnnotationType, annotationBinaryData);
	}

}
