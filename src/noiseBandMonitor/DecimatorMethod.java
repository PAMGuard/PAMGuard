package noiseBandMonitor;

import Filters.FilterMethod;

public class DecimatorMethod {

	private FilterMethod filterMethod;
	private double outputSampleRate;
	
	public DecimatorMethod(FilterMethod filterMethod, double outputSampleRate) {
		this.filterMethod = filterMethod;
		this.outputSampleRate = outputSampleRate;
	}

	/**
	 * @return the filterMethod
	 */
	public FilterMethod getFilterMethod() {
		return filterMethod;
	}

	/**
	 * @return the outputSampleRate
	 */
	public double getOutputSampleRate() {
		return outputSampleRate;
	}

}
