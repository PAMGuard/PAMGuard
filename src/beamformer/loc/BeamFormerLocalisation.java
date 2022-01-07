package beamformer.loc;

import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataUnit;

public class BeamFormerLocalisation extends AbstractLocalisation implements Cloneable {

	double[] angles = new double[1];
	double[] angleErrors = new double[1];
	
	public BeamFormerLocalisation(PamDataUnit pamDataUnit, int locContents, int referenceHydrophones, double[] angles, double angleError) {
		super(pamDataUnit, locContents, referenceHydrophones);
		this.angles = angles;
		this.angleErrors = new double[angles.length];
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

}
