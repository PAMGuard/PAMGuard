package clickTrainDetector;

import java.util.List;

import PamguardMVC.PamDataUnit;

/**
 * Temporary data unit for unconfirmed click trains. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class TempCTDataUnit extends  CTDetectionGroupDataUnit {

	private double chi2;

	public TempCTDataUnit(long timeMilliseconds, @SuppressWarnings("rawtypes") List<PamDataUnit> list) {
		super(timeMilliseconds, list);
	}

	/**
	 * Get the chi^2 value for the click train. Can be null if 
	 * the click train algorithm does not calculate it. 
	 * @return the chi2 algorithm. 
	 */
	public Double getCTChi2() {
		return chi2; 
	}

	/**
	 * Set the chi^2 value for the click train. 
	 * @param chi^2 the chi^2 value to set.
	 */
	public void setCTChi2(Double chi2) {
		this.chi2 = chi2;
	}

}
