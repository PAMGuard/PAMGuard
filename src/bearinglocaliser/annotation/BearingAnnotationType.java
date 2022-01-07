package bearinglocaliser.annotation;

import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import generalDatabase.SQLLoggingAddon;

public class BearingAnnotationType extends DataAnnotationType<BearingAnnotation> {
	
	public static final String NAME = "Bearing";
	
	private BearingAnnotationSQL bearingAnnotationSQL;
	
	private BearingAnnotationBinary bearingAnnotationBinary;
	

	public BearingAnnotationType() {
		bearingAnnotationSQL = new BearingAnnotationSQL(this);
		bearingAnnotationBinary = new BearingAnnotationBinary(this);
		CentralAnnotationsList.addAnnotationType(this);
	}

	@Override
	public String getAnnotationName() {
		return NAME;
	}

	@Override
	public Class getAnnotationClass() {
		return BearingAnnotation.class;
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
		return bearingAnnotationSQL;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<BearingAnnotation> getBinaryHandler() {
		return bearingAnnotationBinary;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getShortIdCode()
	 */
	@Override
	public String getShortIdCode() {
		return NAME;
	}

}
