package qa.generator.distributions;

import java.util.Arrays;

public class QAFixed extends QADistribution {
	
	private double value;

	public QAFixed(double value) {
		super(false, false);
		this.value = value;
	}

	public QAFixed(boolean integrate, double value) {
		super(false, true);
		this.value = value;
	}

	@Override
	protected double[] createValues(int nValues) {
		double[] vals = new double[nValues];
		Arrays.fill(vals, value);
		return vals;
	}

	@Override
	public double[] getRange(double nSigma) {
		double[] range = {value, value};
		return range;
	}

}
