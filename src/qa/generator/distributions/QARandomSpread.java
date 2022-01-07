package qa.generator.distributions;

import java.util.Arrays;
import java.util.Random;

public class QARandomSpread extends QADistribution {

	private double mean;
	private Random random = new Random();
	
	public QARandomSpread(double mean) {
		super(false, false);
		this.mean = mean;
	}
	
	public QARandomSpread(boolean sort, double mean) {
		super(sort, false);
		this.mean = mean;
	}
	
	public QARandomSpread(boolean sort, boolean integrate, double mean) {
		super(sort, integrate);
		this.mean = mean;
	}

	@Override
	protected double[] createValues(int nValues) {
		double[] vals = new double[nValues];
		for (int i = 0; i < nValues; i++) {
			vals[i] = random.nextDouble() * (nValues*mean);
		}
		Arrays.sort(vals);
		for (int i = nValues-1; i > 0; i--) {
			vals[i] = vals[i] - vals[i-1];
		}
		return vals;
	}

	@Override
	public double[] getRange(double nSigma) {
		double[] range = {0., 2*mean};
		return range;
	}

}
