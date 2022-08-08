package soundPlayback.fx;


/**
 * Slider for the high pass filter sound out process. 
 * @author Jamie Macaulay
 *
 */
public class FilterSlider extends PlaySliderPane {
	
	private static final double MIN = 0.5e-3;
	
	private static final double MAX = 0.5;
	
	private static final double NSTEP = 100;

	
	public FilterSlider() {
		super();
	}
	
	
	@Override
	public double valueToPos(double filter) {
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
	public double posToValue(double pos) {
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
		return 0;
	}

	@Override
	public double getMaxValue() {
		return 100;
	}


}
