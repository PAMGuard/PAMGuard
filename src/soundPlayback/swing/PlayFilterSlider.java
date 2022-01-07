package soundPlayback.swing;

public class PlayFilterSlider extends PlaySliderComponent {
	
	private static final double MIN = 0.5e-3;
	
	private static final double MAX = 0.5;
	
	private static final int NSTEP = 100;
	
	public PlayFilterSlider() {
		super();
	}
	
	
	@Override
	public int valueToPos(double filter) {
		if (filter <= MIN) {
			return 0;
		}
		else {
			double grad = (Math.log(MAX)-Math.log(MIN))/NSTEP;
			int pos = (int) Math.round((Math.log(filter/MIN))/grad);
			return pos;
		}
	}
	
	@Override
	public double posToValue(int pos) {
		if (pos == 0) {
			return 0;
		}
		else if (pos >= NSTEP) {
			return MAX;
		}
		else {
			double grad = (Math.log(MAX)-Math.log(MIN))/NSTEP;
			return Math.exp(pos*grad)*MIN;
		}
	}

	@Override
	public double getMinValue() {
		return MIN;
	}

	@Override
	public double getMaxValue() {
		return MAX;
	}

	@Override
	public int getNSteps() {
		return NSTEP;
	}

}
