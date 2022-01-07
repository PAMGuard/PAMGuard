package Localiser.algorithms.timeDelayLocalisers.bearingLoc.annotation;

import PamguardMVC.AcousticDataUnit;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;

public class BearingLocAnnotationType extends DataAnnotationType<BearingLocAnnotation> {

	private BearingLocAnnotationBinary bearingLocAnnotationBinary;

	public BearingLocAnnotationType() {
		super();
		CentralAnnotationsList.addAnnotationType(this);
	}

	@Override
	public String getAnnotationName() {
		return "TDOA Bearing";
	}

	@Override
	public Class getAnnotationClass() {
		return BearingLocAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return AcousticDataUnit.class.isAssignableFrom(dataUnitType);
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getShortIdCode()
	 */
	@Override
	public String getShortIdCode() {
		return "TDBL";
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<BearingLocAnnotation> getBinaryHandler() {
		if (bearingLocAnnotationBinary == null) {
			bearingLocAnnotationBinary = new BearingLocAnnotationBinary(this);
		}
		return bearingLocAnnotationBinary;
	}


}
