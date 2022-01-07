package Filters.interpolate;

public class PolyInterpolator2 extends PolyInterpolator {

	public PolyInterpolator2() {
		super(2);
	}

	@Override
	public double getOutputValue(double arrayPosition) {
		int i2 = (int) (arrayPosition+1.5);
		int i1 = i2-1;
		int i3 = i2+1;
		double f1 = arrayPosition-i1;
		double v1 = internalData[i1];
		double v2 = internalData[i2];
		double v3 = internalData[i3];
		double a = (v1+v3)/2.-v2;
		double b = (v3-v1)/2.;
		double c = v2;
		double res = a*f1*f1 + b*f1 + c;
		return res;
	}

}
