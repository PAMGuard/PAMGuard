package analoginput;

public class AnalogSensorData {

	private double rawValue, calibratedValue;

	/**
	 * @param rawValue
	 * @param calibratedValue
	 */
	public AnalogSensorData(double rawValue, double calibratedValue) {
		super();
		this.rawValue = rawValue;
		this.calibratedValue = calibratedValue;
	}

	/**
	 * @return the rawValue
	 */
	public double getRawValue() {
		return rawValue;
	}

	/**
	 * @param rawValue the rawValue to set
	 */
	public void setRawValue(double rawValue) {
		this.rawValue = rawValue;
	}

	/**
	 * @return the calibratedValue
	 */
	public double getCalibratedValue() {
		return calibratedValue;
	}

	/**
	 * @param calibratedValue the calibratedValue to set
	 */
	public void setCalibratedValue(double calibratedValue) {
		this.calibratedValue = calibratedValue;
	}


}
