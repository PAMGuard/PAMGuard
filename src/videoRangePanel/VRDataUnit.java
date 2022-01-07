package videoRangePanel;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

public class VRDataUnit extends PamDataUnit {

	private VRMeasurement vrMeasurement;
	
	public VRDataUnit(long timeMilliseconds,  VRMeasurement vrMeasurement) {
		super(timeMilliseconds);
		this.vrMeasurement = vrMeasurement;
	}
	
	public VRMeasurement getVrMeasurement() {
		return vrMeasurement;
	}
	
	public void setVrMeasurement(VRMeasurement vrMeasurement) {
		this.vrMeasurement = vrMeasurement;
	}
	
	public String getHoverText() {
		return String.format("<html>Video Range Data<br>%s<br>%s", 
				PamCalendar.formatDateTime(getTimeMilliseconds()), vrMeasurement.getHoverText());
	}

}
