package seismicVeto;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;

public class VetoBackgroundDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements AcousticDataUnit {

	private double background;
	
	private boolean vetoOn = false;
	
	public VetoBackgroundDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration, double background, boolean vetoOn) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.background = background;
		this.vetoOn = vetoOn;
	}

	public double getBackground() {
		return background;
	}

	public void setBackground(double background) {
		this.background = background;
	}

	public boolean isVetoOn() {
		return vetoOn;
	}

	public void setVetoOn(boolean vetoOn) {
		this.vetoOn = vetoOn;
	}

}
