package Filters;

public class NullFilter implements Filter {

	@Override
	public int getFilterDelay() {
		return 0;
	}

	@Override
	public void prepareFilter() {
		// no need to do anything at all. 
	}

	@Override
	public void runFilter(double[] inputData) {
		// no need to do anything at all. 
	}

	@Override
	public void runFilter(double[] inputData, double[] outputData) {
		if (inputData != outputData) {
			int n = inputData.length;
			for (int i = 0; i < n; i++) {
				outputData[i] = inputData[i];
			}
		}
	}

	@Override
	public double runFilter(double aData) {
		return aData;
	}

}
