/**
 * 
 */
package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

/**
 * @author dg50
 *
 */
public class MLLineBearingLocaliser2 extends MLGridBearingLocaliser2 {

	/**
	 * @param hydrophoneBitMap
	 * @param timMillis
	 * @param timingError
	 */
	public MLLineBearingLocaliser2(int hydrophoneBitMap, long timMillis, double timingError) {
		super(hydrophoneBitMap, timMillis, timingError);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Convert a bin into an angle. 
	 * @param bin bin index
	 * @return angle
	 */
	protected double thetaBinToAngle(double bin) {
		return Math.PI/2. - super.thetaBinToAngle(bin);
	}
	
	/**
	 * Convert an angle into a bin, constrained to 0 <= bin < nBins
	 * @param theta angle
	 * @return bin
	 */
	protected int thetaAngleToBin(double theta) {
		return thetaAngleToBin(Math.PI/2. - theta);
	}
}
