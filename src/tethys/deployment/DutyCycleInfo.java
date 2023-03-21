package tethys.deployment;

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
			return String.format("%3.1fs on, %3.1fs off, for %d cycles", meanOnTimeS, meanGapS, nCycles);
		}
	}
	
	
	
}
