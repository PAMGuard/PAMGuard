package bearinglocaliser;

import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataUnit;
import beamformer.loc.BeamFormerLocalisation;

public class BearingLocalisation extends AbstractLocalisation implements Cloneable {

	private double[] angles;
	private double[] angleErrors;
	private double[] referenceAngles;
	private String algorithmName;
	
	public BearingLocalisation(PamDataUnit pamDataUnit, String algorithmName, int locContents, int referenceHydrophones, double[] angles, double[] angleError, double[] referenceAngles) {
		super(pamDataUnit, locContents, referenceHydrophones);
		this.algorithmName = algorithmName;
		this.angles = angles;
		this.angleErrors = angleError;
		this.referenceAngles = referenceAngles;
	}

	@Override
	public double[] getAngles() {
		return angles;
	}

	@Override
	public double[] getAngleErrors() {
		return angleErrors;
	}

	@Override
	public BeamFormerLocalisation clone() {
		try {
			return (BeamFormerLocalisation) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @return the referenceAngles
	 */
	public double[] getReferenceAngles() {
		return referenceAngles;
	}


}
