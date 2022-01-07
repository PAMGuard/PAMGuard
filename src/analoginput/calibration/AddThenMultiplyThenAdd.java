package analoginput.calibration;

/**
 * Add Then Multiply then Add is a calibration method in it that has 
 * some unnecessary, but useful redundancy. Add then multipl makes it 
 * easy to use 4 - 20mA sensors, since the output is imply (input-.004)*maxDepth/.016;
 * However, if you then need to add an additional offset correction, it's much easier to 
 * add a third parameter, so output is (input-.004)*maxDepth/.016 + offset. Without the 
 * third parameter, it would be necessary to do a complicated recalculation of the
 * first parameter, (input + a)*maxDepth/.016, where a = -.004 + offset*.016/maxDepth. 
 * @author dg50
 *
 */
public class AddThenMultiplyThenAdd implements SensorCalibration {

	
	@Override
	public double rawToValue(double rawValue, CalibrationData calibrationData) throws CalibrationException {
		if (calibrationData == null) {
			throw new CalibrationException("Missing Calibration Data");
		}
		double[] params = calibrationData.getParams();
		if (params == null || params.length < 2) {
			throw new CalibrationException("Wrong array length in calibration data");
		}
		double val = (rawValue + params[0]) * params[1];
		if (params.length >= 3) {
			val += params[2];
		}
		return val;
	}

}
