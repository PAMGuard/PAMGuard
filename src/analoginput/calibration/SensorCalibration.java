package analoginput.calibration;

public interface SensorCalibration {

	/**
	 * Convert a value from an ADC, which will probably 
	 * have already been converted to volts or amps into 
	 * an 'engineering' unit, e.g. depth, heading, etc. 
	 * @param rawValue Value from acquisition device
	 * @return value in engineering units. 
	 * @throws Calibration exception if calibration data is missing or incompatible. 
	 */
	public double rawToValue(double rawValue, CalibrationData calibrationData) throws CalibrationException;
	
	
}
