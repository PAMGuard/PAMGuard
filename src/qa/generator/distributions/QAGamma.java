package qa.generator.distributions;

import org.apache.commons.math3.distribution.GammaDistribution;

public class QAGamma extends QADistribution {

	private double scale;
	private double shape;
	private GammaDistribution gammaDist;

	public QAGamma(double mean, double width) {
		super(false, false);
		makeFunc(mean, Math.pow(width,  2));
	}

	public QAGamma(boolean sort, double mean, double width) {
		super(sort, false);
		makeFunc(mean, Math.pow(width,  2));
	}
	
	public QAGamma(boolean sort, boolean integrate, double mean, double width) {
		super(sort, integrate);
		makeFunc(mean, Math.pow(width,  2));
	}
	
	private void makeFunc(double mean, double variance) {
		scale = variance / mean;
		shape = mean / scale;
		gammaDist = new GammaDistribution(shape, scale);
	}

	/* (non-Javadoc)
	 * @see qa.generator.distributions.QADistribution#getRange(int)
	 */
	@Override
	public double[] getRange(double nSigma) {
		double mean = shape * scale;
		double sigma = Math.sqrt(scale * mean);
		double[] range = {mean-nSigma*sigma, mean+nSigma*sigma};
		return range;
	}

	@Override
	protected double[] createValues(int nValues) {
		return gammaDist.sample(nValues);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Gamma dist: Shape %3.1f, scale %3.1f, mean %3.1f, variance %3.1f", 
				shape, scale, gammaDist.getNumericalMean(), gammaDist.getNumericalVariance());
	}

}
