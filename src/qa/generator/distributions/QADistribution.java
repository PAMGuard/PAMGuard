package qa.generator.distributions;

import java.util.Arrays;

public abstract class QADistribution {

	private boolean sort = false;
	private boolean integrate = false;
	
	/**
	 * @param sort sort the data in ascending order. 
	 * @param integrate data will be integrated (e.g. to generate click times rather than inter-click intervals)
	 */
	public QADistribution(boolean sort, boolean integrate) {
		super();
		this.sort = sort;
		this.setIntegrate(integrate);
	}

	/**
	 * Create a set of random values according to the underlying distribution.<p>
	 * Concrete classes should override createValues rather than this 
	 * getValues function, leaving getValues to sort and integrate as required 
	 * @param nValues number of values to generate. 
	 * @return array of values. 
	 */
	public final double[] getValues(int nValues) {
		double[] values = createValues(nValues);
		if (sort) {
			Arrays.sort(values);
		}
		if (integrate) {
			for (int i = 1; i < nValues; i++) {
				values[i] += values[i-1];
			}
		}
		return values;
	}
	
	/**
	 * Get the range of the distrubution. How this is defined
	 * is a little nebulous, for Guassian like distributions its
	 * the mean +/- n standard deviations. 
	 * @param nSigma number of Standard Deviations (or equivalents).
	 * @return The range of values. 
	 */
	public abstract double[] getRange(double nSigma);
	
	/**
	 * Get the range of the distrubution. How this is defined
	 * is a little nebulous, for Guassian like distributions its
	 * the mean +/- n standard deviations. 
	 * @param nSDs number of Standard Deviations (or equivalents).
	 * @return The range of values. 
	 */
	public double[] getRange() {
		return getRange(1);
	}
	/**
	 * Generate a set of random values according to the underlying distribution. <p>
	 * Do not sort or integrate within createValues, that wil be handled by the calling
	 * getValues function
	 * @param nValues number of values to generate. 
	 * @return array of values. 
	 */
	protected abstract double[] createValues(int nValues);

	/**
	 * @return the sort
	 */
	public boolean isSort() {
		return sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(boolean sort) {
		this.sort = sort;
	}

	/**
	 * @return the integrate
	 */
	public boolean isIntegrate() {
		return integrate;
	}

	/**
	 * @param integrate the integrate to set
	 */
	public void setIntegrate(boolean integrate) {
		this.integrate = integrate;
	}

}
