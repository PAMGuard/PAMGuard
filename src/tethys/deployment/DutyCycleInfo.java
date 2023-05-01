package tethys.deployment;

import PamUtils.PamCalendar;

public class DutyCycleInfo {

	public boolean isDutyCycled;
	
	public double meanOnTimeS;
	
	public double meanGapS;
	
	int nCycles;

	public DutyCycleInfo(boolean isDutyCycled, double meanOnTimeS, double meanGapS, int nCycles) {
		super();
		this.isDutyCycled = isDutyCycled;
		this.meanOnTimeS = meanOnTimeS;
		this.meanGapS = meanGapS;
		this.nCycles = nCycles;
	}

	@Override
	public String toString() {
		if (isDutyCycled == false) {
			return "No duty cycle";
		}
		else {
			return String.format("%s on, %s off, for %d cycles", PamCalendar.formatDuration((long) (meanOnTimeS*1000)), 
					PamCalendar.formatDuration((long) (meanGapS*1000)), nCycles);
		}
	}
	
	
	
}
