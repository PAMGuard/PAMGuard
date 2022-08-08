package soundPlayback.fx;

/**
 * Play gain slider. 
 * @author Jamie Macaulay
 *
 */
public class PlayGainSlider extends PlaySliderPane {
	
	static final int MINGAIN = -20;
	
	static final int MAXGAIN = 60;
	
	
	public PlayGainSlider() {
		super();
	}

	@Override
	public double getMinValue() {
		return MINGAIN;
	}

	@Override
	public double getMaxValue() {
		return MAXGAIN;
	}


}