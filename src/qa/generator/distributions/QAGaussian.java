package qa.generator.distributions;

import java.util.Random;

public class QAGaussian extends QADistribution {

	private double mean, sigma;
	private Random random = new Random();
	
	public QAGaussian( double mean, double sigma) {
		super(false, false);
		this.mean = mean;
		this.sigma = sigma;
	}
	
	public QAGaussian(boolean sort, double mean, double sigma) {
		super(sort, false);
		this.mean = mean;
		this.sigma = sigma;
	}

	public QAGaussian(boolean sort, boolean integrate, double mean, double sigma) {
		super(sort, integrate);
		this.mean = mean;
		this.sigma = sigma;
	}

	@Override
	protected double[] createValues(int nValues) {
		double[] vals = new double[nValues];
		for (int i = 0; i < nValues; i++) {
			while (vals[i] <= 0) {
				vals[i] = random.nextGaussian()*sigma + mean;
			}
		}
		return vals;
	}

	@Override
	public double[] getRange(double nSigma) {
		double[] range = {mean-nSigma*sigma, mean+nSigma*sigma};
		return range;
	}

}
