package soundPlayback.swing;

public class PlayGainSlider extends PlaySliderComponent {
	
	private static final int MINGAIN = -20;
	
	private static final int MAXGAIN = 60;
	
	private static final int GAINSTEP = 2;
	
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

	@Override
	public int getNSteps() {
		return (MAXGAIN - MINGAIN) / GAINSTEP + 1;
	}

}
