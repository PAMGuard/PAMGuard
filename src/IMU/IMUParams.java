package IMU;

import java.util.ArrayList;

import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRParameters;

public class IMUParams implements Cloneable {
	
	public final static int IMPORT_CSV=0;
	
	public int currentIMUType=0;

	/**
	 * A list of the last files loaded by the IMU import utility.
	 */
	public ArrayList<String> lastCSVFiles=new ArrayList<String>();
	
	/**
	 * Calibration values for heading, pitch and roll. In RADIANS. 
	 */
	public Double headingCal=0.0;
	public Double pitchCal=0.0;
	public Double tiltCal=0.0;

	

	@Override
	public IMUParams clone() {
		try {
			return(IMUParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
