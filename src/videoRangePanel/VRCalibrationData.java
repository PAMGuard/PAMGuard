package videoRangePanel;

import java.io.Serializable;

import PamUtils.PamCalendar;

public class VRCalibrationData extends Object implements Serializable, Cloneable{

	static public final long serialVersionUID = 0;
	
	public double degreesPerUnit;
	
	public long timeMillis;
	
	public double objectSize_cm;
	
	public double objectDistance_m;
	
	public String name;

	public VRCalibrationData() {
		super();
		timeMillis = PamCalendar.getTimeInMillis();
	}

	public void update(VRCalibrationData newData) {
		timeMillis = PamCalendar.getTimeInMillis();
		degreesPerUnit = newData.degreesPerUnit;
		objectDistance_m = newData.objectDistance_m;
		objectSize_cm = newData.objectSize_cm;
		name = new String(newData.name);
		
	}
	@Override
	public VRCalibrationData clone() {
		try {
			return (VRCalibrationData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
//		return String.format("%s; %5g\u00B0/px", name, degreesPerUnit);
		return name;
	}


}
