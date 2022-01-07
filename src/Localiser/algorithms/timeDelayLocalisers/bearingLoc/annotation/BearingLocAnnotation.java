package Localiser.algorithms.timeDelayLocalisers.bearingLoc.annotation;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class BearingLocAnnotation extends DataAnnotation<BearingLocAnnotationType> {

	private BearingLocAnnotationType bearingLocAnnotationType;
	private double[][] anglesAndErrors;

	/**
	 * @return the anglesAndErrors
	 */
	public double[][] getAnglesAndErrors() {
		return anglesAndErrors;
	}

	public BearingLocAnnotation(BearingLocAnnotationType dataAnnotationType, double[][] anglesAndErrors) {
		super(dataAnnotationType);
		this.bearingLocAnnotationType = dataAnnotationType;
		this.anglesAndErrors = anglesAndErrors;
	}

}
