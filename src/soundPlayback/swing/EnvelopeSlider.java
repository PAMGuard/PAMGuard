package soundPlayback.swing;

public class EnvelopeSlider extends PlaySliderComponent {

	public EnvelopeSlider() {
		super();
	}

	@Override
	public double getMinValue() {
		return 0;
	}

	@Override
	public double getMaxValue() {
		return 1;
	}

	@Override
	public int getNSteps() {
		return 100;
	}

}
