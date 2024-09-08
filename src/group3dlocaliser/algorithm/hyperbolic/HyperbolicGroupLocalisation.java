package group3dlocaliser.algorithm.hyperbolic;

import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;

/**
 * Keeps a copy of the raw position vector for hyperbolic localisation. 
 * @author Jamie Macaulay
 *
 */
public class HyperbolicGroupLocalisation extends GroupLocalisation {
	
	/**
	 * The raw localisation result. 
	 */
	 double[] posVec; 


	public HyperbolicGroupLocalisation(PamDataUnit pamDataUnit, GroupLocResult targetMotionResult) {
		super(pamDataUnit, targetMotionResult);
		// TODO Auto-generated constructor stub
	}

	public double[] getPosVec() {
		return posVec;
	}

	public void setPosVec(double[] posVec) {
		this.posVec = posVec;
	}
}
