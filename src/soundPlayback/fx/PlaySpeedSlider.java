package soundPlayback.fx;

import java.text.DecimalFormat;


public class PlaySpeedSlider extends PlaySliderPane {

	
	private static final double MINSPEED = -6;
	
	private static final double MAXSPEED = 6;
	
	public PlaySpeedSlider() {
		this.getSlider().setBlockIncrement(1);
	}
	

	public String getRatioString() {
		return getRatioString(getDataValue());
	}
	
	public static String getRatioString(double speed) {
		if (speed >= 1) {
			DecimalFormat df = new DecimalFormat(" x #.##");
			return df.format(speed);
		}
		else if (speed <= 0) {
			return "Err 0";
		}
		else {
			DecimalFormat df = new DecimalFormat(" \u00F7 #.##");
			return df.format(1./speed);
		}
	}
	

	@Override
	public double getMinValue() {
		return MINSPEED;
	}

	@Override
	public double getMaxValue() {
		return MAXSPEED;
	}


	@Override
	public double valueToPos(double value) {
		return super.valueToPos(Math.log(value)/Math.log(2.));
	}

	
	@Override
	public double posToValue(double pos) {
		return Math.pow(2,super.posToValue(pos));
	}

}
