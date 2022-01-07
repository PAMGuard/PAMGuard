package analoginput.calibration;

public class MultiplyThenAdd implements SensorCalibration {

	@Override
	public double rawToValue(double rawValue, CalibrationData calibrationData)  throws CalibrationException {
		if (calibrationData == null) {
			throw new CalibrationException("Missing Calibration Data");
		}
		double[] params = calibrationData.getParams();
		if (params == null || params.length != 2) {
			throw new CalibrationException("Wrong array length in calibration data");
		}
		return (rawValue * params[0]) + params[1];
	}

}
