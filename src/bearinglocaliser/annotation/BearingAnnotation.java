package bearinglocaliser.annotation;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import bearinglocaliser.BearingLocalisation;

public class BearingAnnotation extends DataAnnotation<BearingAnnotationType> {
	
	private BearingLocalisation bearingLocalisation;

	public BearingAnnotation(BearingAnnotationType bearingAnnotationType, BearingLocalisation bearingLocalisation) {
		super(bearingAnnotationType);
		this.setBearingLocalisation(bearingLocalisation);
		
	}

	/**
	 * @return the bearingLocalisation
	 */
	public BearingLocalisation getBearingLocalisation() {
		return bearingLocalisation;
	}

	/**
	 * @param bearingLocalisation the bearingLocalisation to set
	 */
	public void setBearingLocalisation(BearingLocalisation bearingLocalisation) {
		this.bearingLocalisation = bearingLocalisation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return bearingLocalisation.getAlgorithmName() + "-" + bearingLocalisation.toString();
	}

}
