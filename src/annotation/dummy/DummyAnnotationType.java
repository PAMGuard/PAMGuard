package annotation.dummy;

import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;

public class DummyAnnotationType extends DataAnnotationType<DummyAnnotation> {

	private DummyBinaryHandler dummyBinaryHandler;
	
	public DummyAnnotationType() {
		super();
		dummyBinaryHandler = new DummyBinaryHandler(this);
	}

	@Override
	public String getAnnotationName() {
		return "Dummy annotation";
	}

	@Override
	public Class getAnnotationClass() {
		return DummyAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<DummyAnnotation> getBinaryHandler() {
		return dummyBinaryHandler;
	}

}
