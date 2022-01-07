package Filters;
/**
 * Abstract implementation of Filter interface to 
 * provide common functionality for two of the main
 * function calls for processing arrays of data.  
 * 
 * @author Doug Gillespie
 *
 */
public abstract class AbstractFilter implements Filter {

	@Override
	abstract public void prepareFilter();

	@Override
	public void runFilter(double[] inputData) {
		runFilter(inputData, inputData);
	}

	@Override
	public void runFilter(double[] inputData, double[] outputData) {
		if (outputData == null || outputData.length != inputData.length) {
			outputData = new double[inputData.length];
		}
		for (int i = 0; i < inputData.length; i++) {
			outputData[i] = runFilter(inputData[i]);
		}
	}

	@Override
	abstract public double runFilter(double data);

}
