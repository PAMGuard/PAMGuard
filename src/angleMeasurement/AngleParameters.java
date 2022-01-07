package angleMeasurement;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class AngleParameters extends Object implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 0;
	
	public double angleOffset;
	
	public double geoReferenceAngle;
	

	/**
	 * This is the raw calibration data, and is 
	 * probably the oposote of what we want, but it's the 
	 * way we want to measure it - i..e it's the output 
	 * of the 3030 (or other device and a measured real angle. 
	 * I'll write some funcitons to convert the other way !
	 */
	private double[] calibrationData;
	
	private int calibrationInterval = 15;
	
	private AngleLoggingParameters angleLoggingParameters = new AngleLoggingParameters();
	

	@Override
	protected AngleParameters clone() {
		try {
			AngleParameters newAP = (AngleParameters) super.clone();
			if (newAP.calibrationInterval <= 0) {
				newAP.calibrationInterval = 15;
			}
			if (angleLoggingParameters != null) {
				newAP.angleLoggingParameters = angleLoggingParameters.clone();
			}
			else {
				newAP.angleLoggingParameters = new AngleLoggingParameters();
			}
			return newAP;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public double[] getCalibrationData() {
		int nC = getNumPoints();
		if (calibrationData == null || calibrationData.length != nC) {
			calibrationData = new double[nC];
			for (int i = 0; i < nC; i++) {
				calibrationData[i] = i * calibrationInterval;
			}
		}
		return calibrationData;
	}
	
	public void setCalibrationData(double[] calibrationData) {
		this.calibrationData = calibrationData;
		this.calibrationInterval = 360 / calibrationData.length; 
	}

	public int getNumPoints() {
		return (int) Math.ceil(360./calibrationInterval);
	}

	public double[] getCalibrationPoints() {
		int nC = getNumPoints();
		double[] calibrationPoints = new double[nC];
		for (int i = 0; i < nC; i++) {
			calibrationPoints[i] = i * calibrationInterval;
		}
		return calibrationPoints;
	}

	public int getCalibrationInterval() {
		return calibrationInterval;
	}

	public void setCalibrationInterval(int calibrationInterval) {
		if (this.calibrationInterval == calibrationInterval){
			return;
		}
		this.calibrationInterval = calibrationInterval;
		// need to delete existing cal data. 
		// it should get automatically recreated next time it's asked for. 
		calibrationData = null;
	}

	public AngleLoggingParameters getAngleLoggingParameters() {
		return angleLoggingParameters;
	}

	public void setAngleLoggingParameters(
			AngleLoggingParameters angleLoggingParameters) {
		this.angleLoggingParameters = angleLoggingParameters;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
