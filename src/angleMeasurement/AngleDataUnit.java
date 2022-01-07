package angleMeasurement;

import PamguardMVC.PamDataUnit;

/**
 * Data unit for storing angular information, often used to store data from imu intruments (inertial measurement unit) and heading data from compass sensors. 
 * (Wiki) An inertial measurement unit, or IMU, is an electronic device that measures and reports on a craft's velocity, orientation, and gravitational forces, using a combination of accelerometers and gyroscopes, sometimes also magnetometers
 * In PAMGUARD the convention for angles is as follows:
 * <p>
 * Bearing- 0==north, 90==east 180=south, 270==west
 * <p>
 * Pitch- 90=-g, 0=0g, -90=g
 * <p>
 * Tilt 0->180 -camera turning towards left to upside down 0->-180 camera turning right to upside down
 * <p>
 * All angles are in RADIANS. 
 * 
 * @author Douglas Gillespie modified by Jamie Macaulay
 *
 */
public class AngleDataUnit extends PamDataUnit {

	/**
	 * For use with fluxgate compass-Raw data as came out of the instrument
	 */
	private Double rawAngle;
		
	/**
	 * For use with fluxgate compass-flag as to whether angle was held by user. 
	 */
	private Boolean held;
	
	/**
	 * Heading in radians- includes any calibration value: i.e. calibration value already added.
	 */
	private Double trueHeading;
	/**
	 * Pitch in radians- includes in calibration value. 
	 */
	private Double pitch;
	/**
	 * Tilt in radians- includes any calibration value. 
	 */
	private Double tilt;
	/**
	 * Offset added to heading in radians
	 */
	private Double calTrueHeading;
	/**
	 * Offset added to pitch in radians
	 */
	private Double calPitch;
	/**
	 * Offset added to tilt in radians
	 */
	private Double calTilt;
	/**
	 *Error in heading in radians
	 */
	private Double errorHeadings;
	/**
	 *Error in pitch in radians
	 */
	private Double errorPitch;
	/**
	 *Error in tilt in radians
	 */
	private Double errorTilt;
	
	/**
	 * The number of units that made up this dat unit. Usually one but if the data unit is an average will be >=1; 
	 */
	private int nUnits=1;

	/**
	 * Constructor for heading data: Primarily used in AngleMeasurment module 
	 * <p>
	 * Data from an angle measurements 
	 * <br>
	 * Angle data come in three stages. 
	 * <br>1. Raw data as came out of the instrument
	 * <br>2. Calibrated data - the raw data after calibration, 0 degrees
	 * should be equal to 0 degrees in the calibrated data.
	 * <br>3. Correct raw data - the calibrated data - the set constant offset. 
	 * <p>
	 * @param timeMilliseconds- time in millis
	 * @param rawAngle - Raw data as came out of the instrument
	 * @param calibratedAngle-the raw data after calibration, 0 degrees
	 * should be equal to 0 degrees in the calibrated data.
	 * @param correctedAngle- the calibrated data - the set constant offset. 
	 * @param timeMilliseconds- time in millis
	 */
	public AngleDataUnit(long timeMilliseconds, double rawAngle, double calibratedAngle, double correctedAngle) {
		super(timeMilliseconds);
		this.rawAngle = rawAngle;
		this.calTrueHeading = calibratedAngle;
		this.trueHeading = correctedAngle;
	}
	
	/**
	 * Constructor for IMU angle data. Note that this assumes the calibration values are zero for each measurement (unless set afterwards)
	 * @param timeMilliseconds- the time of this measurment
	 * @param IMU- imuData heading, pitch and roll in radians. 
	 */
	public AngleDataUnit(long timeMilliseconds, Double[] IMU){
		super(timeMilliseconds);
		this.tilt = IMU[2];
		this.pitch = IMU[1];
		this.trueHeading = IMU[0];
	}
	
	/**
	 * Constructor for IMU angle data. Note that this assumes the calibration values are zero for each measurement (unless set afterwards)
	 * @param timeMilliseconds- the time of this measurment
	 * @param IMU- imuData heading, pitch and tilt in radians. 
	 * @param IMU Errors- error in the heading, pitch and tilt in radians.
	 */
	public AngleDataUnit(long timeMilliseconds, Double[] IMU, Double[] errors){
		super(timeMilliseconds);
		this.trueHeading = IMU[0];
		this.pitch = IMU[1];
		this.tilt = IMU[2];
		this.errorHeadings=errors[0];
		this.errorPitch=errors[1];
		this.errorTilt=errors[2];
	}
	
	public Double getRawAngle() {
		return rawAngle;
	}

	
	public Boolean getHeld() {
		return held;
	}

	public Double getTrueHeading() {
		return trueHeading;
	}

	public Double getPitch() {
		return pitch;
	}

	public Double getTilt() {
		return tilt;
	}

	public void setRawAngle(Double rawAngle) {
		this.rawAngle = rawAngle;
	}

	public void setHeld(Boolean held) {
		this.held = held;
	}

	public void setTrueHeading(Double trueHeading) {
		this.trueHeading = trueHeading;
	}

	public void setPitch(Double pitch) {
		this.pitch = pitch;
	}

	public void setTilt(Double tilt) {
		this.tilt = tilt;
	}
	
	public Double getCalTrueHeading() {
		return calTrueHeading;
	}

	public Double getCalPitch() {
		return calPitch;
	}

	public Double getCalTilt() {
		return calTilt;
	}

	public void setCalTrueHeading(Double calTrueHeading) {
		this.calTrueHeading = calTrueHeading;
	}

	public void setCalPitch(Double calPitch) {
		this.calPitch = calPitch;
	}

	public void setCalTilt(Double calTilt) {
		this.calTilt = calTilt;
	}
	
	public Double getErrorHeading() {
		return errorHeadings;
	}

	public Double getErrorPitch() {
		return errorPitch;
	}

	public Double getErrorTilt() {
		return errorTilt;
	}

	public void setErrorHeadings(Double errorHeadings) {
		this.errorHeadings = errorHeadings;
	}

	public void setErrorPitch(Double errorPitch) {
		this.errorPitch = errorPitch;
	}

	public void setErrorTilt(Double errorTilt) {
		this.errorTilt = errorTilt;
	}
	
	public int getNUnits() {
		return nUnits;
	}

	public void setNUnits(int nUnits) {
		this.nUnits = nUnits;
	}


	
}
