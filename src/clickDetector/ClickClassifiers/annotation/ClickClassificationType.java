package clickDetector.ClickClassifiers.annotation;

import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import generalDatabase.SQLLoggingAddon;

/**
 * Annotation type for showing which click classifiers in the click detector
 * passed the clicks detection, not just the first one.
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickClassificationType extends DataAnnotationType<ClickClassifierAnnotation> {
	
	public static final String NAME = "ClickClasssifier_1";
	
	private ClickAnnotationSQL clickAnnotationSQL;
	
	private ClickAnnotationBinary clickAnnotationBinary;
	

	public ClickClassificationType() {
		clickAnnotationSQL = new ClickAnnotationSQL(this);
		clickAnnotationBinary = new ClickAnnotationBinary(this);
		CentralAnnotationsList.addAnnotationType(this);
	}

	@Override
	public String getAnnotationName() {
		return NAME;
	}

	@Override
	public Class getAnnotationClass() {
		return ClickClassificationType.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSQLLoggingAddon()
	 */
	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return clickAnnotationSQL;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<ClickClassifierAnnotation> getBinaryHandler() {
		return clickAnnotationBinary;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getShortIdCode()
	 */
	@Override
	public String getShortIdCode() {
		return NAME;
	}

}
