package angleMeasurement;

import java.awt.Frame;
import java.util.ArrayList;

import PamController.PamSettingManager;
import PamController.PamSettings;

public abstract class AngleMeasurement implements PamSettings{
		
	private String name;
	
	private AngleParameters angleParameters;
	
	private ArrayList<AngleMeasurementListener> measurementListeners = new ArrayList<AngleMeasurementListener>();
	
	private AngleCalibration angleCalibration;

	public AngleMeasurement(String name) {
		super();
		this.name = name;
		PamSettingManager.getInstance().registerSettings(this);
	}

	abstract public Double getRawAngle();
	
	public Double getCalibratedAngle() {
		Double rawAngle = getRawAngle();
		if (rawAngle == null) {
			return null;
		}
		return getCalibratedAngle(rawAngle);
	}
	
	abstract public Double getCorrectedAngle();
		
	abstract public void setZero();
	
	abstract public boolean settings(Frame parentFrame);
	

	public double getAngleOffset() {
		return angleParameters.angleOffset;
	}

	public void setAngleOffset(double angleOffset) {
		this.angleParameters.angleOffset = angleOffset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getUnitName() {
		return name;
	}

	@Override
	public String getUnitType() {
		return "AngleMeasurement";
	}

	public AngleParameters getAngleParameters() {
		return angleParameters;
	}

	public void setAngleParameters(AngleParameters angleParameters) {
		this.angleParameters = angleParameters;
		setupCalibration();
	}
	
	public void addMeasurementListener(AngleMeasurementListener angleMeasurementListener) {
		if (!measurementListeners.contains(angleMeasurementListener)) {
			measurementListeners.add(angleMeasurementListener);
		}
	}
	public void removeMeasurementListener(AngleMeasurementListener angleMeasurementListener) {
		if (!measurementListeners.contains(angleMeasurementListener)) {
			measurementListeners.remove(angleMeasurementListener);
		}
	}
	protected void notifyAngleMeasurementListeners() {
		for (int i = 0; i < measurementListeners.size(); i++) {
			measurementListeners.get(i).newAngle(getRawAngle(), getCalibratedAngle(), getCorrectedAngle());
		}
	}
	
	public void setCalibrationData(double[] calibrationData) {
		angleParameters.setCalibrationData(calibrationData);
		setupCalibration();
	}
	/**
	 * Sets up the angle calibration. 
	 * Most angle measurement devices wil just use it as is, 
	 * but some may be able to extract the data from it and
	 * upload them to the external device. 
	 *
	 */
	public void setupCalibration() {
		if (angleParameters != null) {
			angleCalibration = new AngleCalibration(angleParameters.getCalibrationPoints(), 
					angleParameters.getCalibrationData());
		}
		else {
			angleCalibration = null;
		}
	}
	
	/**
	 * Converts a raw angle into a calibrated angle. <br>
	 * Most angle measurement devices wil just use it as is, 
	 * but some may be able to extract the data from it and
	 * upload them to the external device, in which case 
	 * developer will probably want to override this function 
	 * so that it doesn't do anything (just returns the rawAngle)
	 * @param rawAngle uncalibrated angle
	 * @return calibrated angle
	 */
	public double getCalibratedAngle(double rawAngle) {
		if (angleCalibration == null) {
			return rawAngle;
		}
		return angleCalibration.getCalibratedAngle(rawAngle);
	}
}
