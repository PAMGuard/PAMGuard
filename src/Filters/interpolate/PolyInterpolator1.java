package Filters.interpolate;

public class PolyInterpolator1 extends PolyInterpolator {

	public PolyInterpolator1() {
		super(1);
	}

	@Override
	public double getOutputValue(double arrayPosition) {
		int i1 = (int) arrayPosition;
		int i2 = i1+1;
		double f1 = i2-arrayPosition;
		double f2 = 1.-f1;
		return internalData[i1]*f1+internalData[i2]*f2;
	}

}
