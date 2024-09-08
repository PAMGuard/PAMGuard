package propagation;

import PamUtils.LatLong;
import pamMaths.PamVector;

public class SurfaceEcho implements PropagationModel {

	private PropagationModel basePropagation;

	private double[] delays2;
	
	private double[] gains2;
	
	private PamVector[] pointingVestors = new PamVector[2];

	
	public SurfaceEcho(PropagationModel basePropagation) {
		super();
		this.basePropagation = basePropagation;
	}
	
	@Override
	public boolean setLocations(LatLong hydrophoneLatLong, LatLong sourceLatLong, double speedOfSound) {
		delays2 = new double[2];
		gains2 = new double[2];
		basePropagation.setLocations(hydrophoneLatLong, sourceLatLong, speedOfSound);
		delays2[0] = basePropagation.getDelays()[0];
		gains2[0] = basePropagation.getGains()[0];
		pointingVestors[0] = basePropagation.getPointingVectors()[0];
		LatLong echoPhone = hydrophoneLatLong.clone();
		echoPhone.setHeight(-echoPhone.getHeight());
		basePropagation.setLocations(echoPhone, sourceLatLong, speedOfSound);
		delays2[1] = basePropagation.getDelays()[0];
		gains2[1] = -basePropagation.getGains()[0];
		pointingVestors[1] = basePropagation.getPointingVectors()[0];
		return true;
	}

	@Override
	public double[] getDelays() {
		return delays2;
	}

	@Override
	public double[] getGains() {
		return gains2;
	}

	@Override
	public int getNumPaths() {
		return 2;
	}

	@Override
	public String getName() {
		return basePropagation.getName() + " + Surface Echo";
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public PamVector[] getPointingVectors() {
		return pointingVestors;
	}

	
}
