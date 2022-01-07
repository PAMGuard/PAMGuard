package whistleDetector;

import Array.ArrayManager;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.PamUtils;

public class WhistleLocalisation extends AbstractLocalisation {

	private int[] delays;
	
	private ShapeDataUnit shapeDataUnit;
	
	private double bearing = Double.NaN;

	public WhistleLocalisation(ShapeDataUnit shapeDataUnit, int referenceChannels) {
		super(shapeDataUnit, 0, referenceChannels);
		this.shapeDataUnit = shapeDataUnit;

		int nChannels = PamUtils.getNumChannels(shapeDataUnit.getChannelBitmap());
		delays = new int[nChannels];
	}
	
	public int getDelay(int nDelay) {
		return delays[nDelay];
	}
	
	public void setDelay(int delay, int nDelay){
		delays[nDelay] = delay;
		getLocContents().setLocContent(LocContents.HAS_BEARING);
	}
	
	public ShapeDataUnit getShapeDataUnit() {
		return shapeDataUnit;
	}

	@Override
	public double getBearing(int iSide) {
		if (Double.isNaN(bearing)) {
			bearing = calculateBearing();
		}
		return bearing;
	}
	
	public double calculateBearing() {
		double ang = (double) delays[0] / shapeDataUnit.getParentDataBlock().getParentProcess().getSampleRate()
				/ ArrayManager.getArrayManager().getCurrentArray().getSeparationInSeconds(getReferenceHydrophones(),shapeDataUnit.getTimeMilliseconds());
		ang = Math.min(1., Math.max(ang, -1.));
		double angle = Math.acos(ang);
		return angle;
	}

	@Override
	public boolean bearingAmbiguity() {
		return true;
	}

	
}
