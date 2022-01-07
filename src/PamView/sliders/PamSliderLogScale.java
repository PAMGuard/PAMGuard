package PamView.sliders;

public class PamSliderLogScale extends PamSliderScale {

	@Override
	public double getValue(int oldValue, int minValue, int maxValue) {
		if (oldValue == 0) {
			return 0;
		}
		if (minValue <= 0) minValue = 1;
		double logMin = Math.log(minValue);
		double logMax = Math.log(maxValue);
		double rat = (double) (oldValue - minValue) / (double) (maxValue - minValue);
		double logVal = logMin + rat * (logMax-logMin); 
		return Math.exp(logVal);
	}

}
