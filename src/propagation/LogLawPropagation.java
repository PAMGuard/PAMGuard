package propagation;

import java.util.Random;

import PamUtils.LatLong;
import pamMaths.PamVector;

public class LogLawPropagation implements PropagationModel {

	private double attenuationRate = 20;
	
	private Random random = new Random();

	public LogLawPropagation(double attenuationRate) {
		super();
		this.setAttenuationRate(Math.abs(attenuationRate));
	}

	private double[] delays;

	private double[] gains;

	private PamVector[] pointingVector = new PamVector[1];

	@Override
	public double[] getDelays() {
		return delays;
	}

	@Override
	public double[] getGains() {
		return gains;
	}

	@Override
	public int getNumPaths() {
		return 1;
	}

	@Override
	public boolean setLocations(LatLong hydrophoneLatLong,
		LatLong sourceLatLong, double speedOfSound) {

		double posErr = 0;
		double sosErr = 0;
		speedOfSound += sosErr * random.nextGaussian();
		/** 
		 * work out the vector pointing from the source to the hydrophone
		 */
		double xDist = sourceLatLong.distanceToMetresX(hydrophoneLatLong) + posErr * random.nextGaussian();
		double yDist = sourceLatLong.distanceToMetresY(hydrophoneLatLong) + posErr * random.nextGaussian();
		double zDist = hydrophoneLatLong.getHeight() - sourceLatLong.getHeight();
		pointingVector[0] = new PamVector(xDist, yDist, zDist);

		//		System.out.println("Spherical Prop: "+ depth);

		double dist = pointingVector[0].norm();
		//		System.out.println(String.format("dx,dy,dz %3.1f, %3.1f, %3.1f dist %3.1fm", xDist, yDist, zDist, dist));
		//		if (dist < 1) {
		//			System.out.println(String.format("Prop dist = %3.1f", dist));
		//		}


		delays = new double[1];
		gains = new double[1];
		delays[0] = pointingVector[0].norm()/speedOfSound;
		gains[0] = Math.pow(1./Math.max(dist, .1), attenuationRate/20.);
		return true;
	}

	@Override
	public String getName() {
		return String.format("%d.log(r) Propagation", attenuationRate);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public PamVector[] getPointingVectors() {
		return pointingVector;
	}

	public double getAttenuationRate() {
		return attenuationRate;
	}

	public void setAttenuationRate(double attenuationRate) {
		this.attenuationRate = Math.abs(attenuationRate);
	}



}
