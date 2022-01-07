package soundPlayback.swing;

import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import PamUtils.LatLong;
import PamView.PamColors.PamColor;
import PamView.PamSlider;


/**
 * slider to control playback rate. 
 * Works on a log scale. 
 * @author dg50
 *
 */
public class PlaySpeedSlider extends PlaySliderComponent{
	
	private static final double MINSPEED = -6;
	
	private static final double MAXSPEED = 6;

	private static final int NSTEPS = 12;
	
	public PlaySpeedSlider() {
		this(null);
	}
	
	public PlaySpeedSlider(PamColor sliderColour) {
		getSlider().setDefaultColor(sliderColour);
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
	
	
	public void setEnabled(boolean enable) {
		getSlider().setEnabled(enable);
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
	public int getNSteps() {
		return NSTEPS;
	}

	@Override
	public int valueToPos(double value) {
		return super.valueToPos(Math.log(value)/Math.log(2.));
	}

	@Override
	public double posToValue(int pos) {
		return Math.pow(2,super.posToValue(pos));
	}
}
