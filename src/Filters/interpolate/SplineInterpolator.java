package Filters.interpolate;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class SplineInterpolator extends PolyInterpolator {
	
	private AkimaSplineInterpolator splineInterpolator;

	private double[] xValues;

	private PolynomialSplineFunction spline;
	
	public SplineInterpolator() {
		super(6);
		splineInterpolator = new AkimaSplineInterpolator();
	}

	@Override
	public void setInputData(double[] inputArray) {
		if (xValues == null || xValues.length != inputArray.length+order) {
			xValues = makeXValues(inputArray.length);
		}
		super.setInputData(inputArray);
		spline = splineInterpolator.interpolate(xValues, internalData);
	}

	private double[] makeXValues(int length) {
		xValues = new double[length+order];
		for (int i = 0, k = -3; i < xValues.length; i++, k++) {
			xValues[i] = k; 
		}
		return xValues;
	}

	@Override
	public double getOutputValue(double arrayPosition) {
		return spline.value(arrayPosition);
	}

}
