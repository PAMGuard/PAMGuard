package bearinglocaliser.beamformer;

/**
 * Intermediate data in beam former bearing measurements. 
 */
public class BeamAngleData {

	private double[] angles;
	private double[] errors;
	private double[] snr;
	/**
	 * @param angles
	 * @param errors
	 * @param snr
	 */
	public BeamAngleData(double[] angles, double[] errors, double[] snr) {
		super();
		this.angles = angles;
		this.errors = errors;
		this.snr = snr;
	}
	/**
	 * @return the angles
	 */
	public double[] getAngles() {
		return angles;
	}
	/**
	 * @return the errors
	 */
	public double[] getErrors() {
		return errors;
	}
	/**
	 * @return the snr
	 */
	public double[] getSnr() {
		return snr;
	}
	
	
}
