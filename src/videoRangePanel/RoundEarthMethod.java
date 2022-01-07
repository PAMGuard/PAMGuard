package videoRangePanel;

import PamController.SettingsPane;
import videoRangePanel.layoutAWT.RangeDialogPanel;

public class RoundEarthMethod extends VRHorzCalcMethod {

	protected VRControl vrControl;
	
	
	public RoundEarthMethod(VRControl vrControl) {
		super();
		this.vrControl = vrControl;
	}

	@Override
	public void configure() {
		// TODO Auto-generated method stub

	}


	@Override
	public double getRange(double height, double angle) {

		double ha = getHorizonAngle(height);
		double psi = Math.PI / 2 - ha - angle;
		
		return rangeFromPsi(height, psi);
	}
	
	/**
	 * Many calc, including those in the refraction method use the angle
	 * from the vertical, so put that as a separate calculation.
	 * <p>
	 * Eq. 1. From Leaper and Gordon.
	 * @param height camera height
	 * @param psi angle up from vertical
	 * @return distance to object. 
	 */
	@Override
	public double rangeFromPsi(double height, double psi) {

		double rnh = earthRadius + height;
		double d = (rnh) * Math.cos(psi);
		double sqrtTerm = Math.pow(earthRadius,2) - Math.pow(rnh * Math.sin(psi), 2);
		d -= Math.sqrt(sqrtTerm);
		
		return d;
	}
	

	@Override
	public double getAngle(double height, double range) {
		if (range > getHorizonDistance(height)) {
			return -1;
		}
		return Math.PI/2 - getHorizonAngle(height) - psiFromRange(height, range);
	}
	
	/**
	 * Convert a range to an angle up from the vertical
	 * (cosine rule).
	 * @param height platform height
	 * @param range range to object
	 * @return angle in radians. 
	 */
	@Override
	public double psiFromRange(double height, double range) {
		if (range > getHorizonDistance(height)) {
			return -1;
		}
		double a = earthRadius;
		double b = earthRadius + height;
		double c = range;
		double cosPsi = (b*b + c*c - a*a)/(2*b*c);
		if (Math.abs(cosPsi) > 1) {
			return -1;
		}
		return Math.acos(cosPsi);
	}

	@Override
	String getName() {
		return "Round Earth";
	}

	@Override
	public double getHorizonDistance(double height) {
		// simple pythagorus. 
		return Math.sqrt(Math.pow(height+earthRadius, 2) - Math.pow(earthRadius, 2));
	}

	@Override
	public SettingsPane<?> getRangeMethodPane() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public RangeDialogPanel dialogPanel() {
		return null;
	}
	
	

}
