package ArrayAccelerometer;

import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;

public class ArrayAccelDataUnit extends PamDataUnit implements ArraySensorDataUnit {

	private Double[] voltsRead;
	private Double[] acceleration;
	private ArrayAccelControl accelControl;

	public ArrayAccelDataUnit(long timeMilliseconds, ArrayAccelControl accelControl, Double[] voltsRead, Double[] accel) {
		super(timeMilliseconds);
		this.accelControl = accelControl;
		this.voltsRead = voltsRead;
		this.acceleration = accel;
	}

	/**
	 * @return the voltsRead
	 */
	public Double[] getVoltsRead() {
		return voltsRead;
	}

	/**
	 * @return the acceleration
	 */
	public Double[] getAcceleration() {
		return acceleration;
	}

	/**
	 * 
	 * @param dim dimension, 0, 1 or 2. 
	 * @return the acceleration for a given dimension
	 */
	public Double getAcceleration(int dim) {
		return acceleration[dim];
	}
	
	/**
	 * 
	 * @return the roll in degrees. 
	 */
	public Double getRoll() {
		if (acceleration[2] == null || acceleration[0] == null) {
			return null;
		}
		double roll = Math.PI/2-Math.atan2(acceleration[2], acceleration[0]);
		return PamUtils.constrainedAngle(Math.toDegrees(roll) + accelControl.accelParams.rollOffset, 180);
	}

	/**
	 * return the pitch in degrees. 
	 * @return pitch in degrees. 
	 */
	public Double getPitch() {
		PamVector aVec = new PamVector();
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			if (acceleration[i] == null) {
				return null;
			}
			else {
				aVec.setElement(i, acceleration[i]);
			}
		}
		aVec.normalise();
		double pitch = Math.asin(-aVec.getElement(1));
		return PamUtils.constrainedAngle(Math.toDegrees(pitch) + accelControl.accelParams.pitchOffset, 180);
	}


	@Override
	public Double getField(int streamer, ArraySensorFieldType fieldType) {
		if (streamer != accelControl.accelParams.streamerIndex) {
			return null;
		}
		switch (fieldType) {
		case PITCH:
			return getPitch();
		case ROLL:
			return getRoll();
		default:
			return null;		
		}
	}
	
}
