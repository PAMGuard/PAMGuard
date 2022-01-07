package Filters;

public class FIRFilter implements Filter {
	
	double sampleRate;
	
	private FilterParams filterParams;
	
	private int channel;
	
	private FIRFilterMethod firFilterMethod;
	
	private double[] filterTaps;
	
	private int nTaps;
	
	private int delay = 0;
	
	private double[] filterHistory;
	private int historyPos;

	public FIRFilter(FIRFilterMethod firFilterMethod, int channel, double sampleRate) {
		this.channel = channel;
		this.sampleRate = sampleRate;
		this.firFilterMethod = firFilterMethod;
		this.filterParams = firFilterMethod.getFilterParams();
		prepareFilter();
	}

	@Override
	public int getFilterDelay() {
		return delay;
	}

	@Override
	public void prepareFilter() {
		/*
		 *  no need to reverse the order of the filter taps
		 *  since we'll reverse the waveform instead. 
		 */
		double[] tempTaps = firFilterMethod.getFilterTaps();
		if (tempTaps == null) {
			return;
		}
		nTaps = tempTaps.length;
		filterTaps = new double[nTaps];
		for (int i = 0; i < nTaps; i++) {
			filterTaps[i] = tempTaps[i];
		}		
		delay = nTaps/2;
		filterHistory = new double[nTaps+1];
		historyPos = 0;
	}

	@Override
	public void runFilter(double[] inputData) {
		runFilter(inputData, inputData);
	}

	@Override
	public void runFilter(double[] inputData, double[] outputData) {
		int n = inputData.length;
		for (int i = 0; i < n; i++) {
			outputData[i] = runFilter(inputData[i]);
		}
	}

	@Override
	public double runFilter(double aData) {
		double val = 0;
		if (filterTaps == null) {
			return Double.NaN;
		}
		filterHistory[nTaps] = aData;
		for (int i = 0, j = 1; i < nTaps; i++, j++) {
			filterHistory[i] = filterHistory[j];
			val += filterHistory[i]*filterTaps[i];
		}
		return val;
	}
}
