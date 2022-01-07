package Filters.interpolate;

public class PolyInterpolator0 extends PolyInterpolator {

	public PolyInterpolator0() {
		super(0);
	}

	@Override
	public double getOutputValue(double arrayPosition) {
		return internalData[(int) Math.round(arrayPosition)];
	}

	@Override
	public void setInputData(double[] inputArray) {
		internalData = inputArray;
	}

}
