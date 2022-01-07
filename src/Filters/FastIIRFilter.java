package Filters;

/**
 * This has the same functionality as IirfFilterm. It still works on a series of biquad pairs, the
 * difference being that all the biquad pairs are merged into a single history and a single 
 * loop through the filter coefficients. This is more confusing to read, but it runs a fair
 * bit quicker than having to call into multiple FilterPair objects and keeping separate history
 * for each pair.  
 * @author Doug Gillespie
 *
 */
public class FastIIRFilter implements Filter {

	private int channel;
	private double sampleRate;
	private FilterParams filterParams;
	private FilterMethod filterMethod;
	double[] filterCoefficients;
	private int nCoefs;
	double[] State;
	private double filterGain;

	public FastIIRFilter(int channel, double sampleRate, FilterParams filterParams) {
		this.channel = channel;
		this.sampleRate = sampleRate;
		this.filterParams = filterParams;
		prepareFilter();
	}

	@Override
	public void prepareFilter() {

		switch (filterParams.filterType) {
		case BUTTERWORTH:
			filterMethod = new ButterworthMethod(sampleRate, filterParams);
			break;
		case CHEBYCHEV:
			filterMethod = new ChebyshevMethod(sampleRate, filterParams);
			break;
		default:
			filterMethod = null;
		}

		if (filterMethod != null && sampleRate != 0) {
			filterCoefficients = filterMethod.getFastFilterCoefficients();
			nCoefs = filterCoefficients.length;
			filterGain = filterMethod.getFilterGainConstant();
			State = new double[nCoefs];
		}
	}

	@Override
	public void runFilter(double[] inputData) {
		runFilter(inputData, inputData);
	}

	@Override
	public void runFilter(double[] inputData, double[] outputData) {
		for (int i = 0; i < inputData.length; i++) {
			outputData[i] = runFilter(inputData[i]);
		}
	}

	@Override
	public double runFilter(double aVal) {
		double p0, p1;
		int i, j;
		double x = aVal;
		for (i = j = 0; i < nCoefs; i += 4, j += 2) {
			p0 = filterCoefficients[i + 2] * State[j] + filterCoefficients[i + 3] * State[j + 1];
			p1 = filterCoefficients[i] * State[j] + filterCoefficients[i + 1] * State[j + 1];

			State[j + 1] = State[j];

			State[j] = x + p0;
			x += (p0 + p1);
		}

		return x/filterGain;
	}

	@Override
	public int getFilterDelay() {
		// TODO Auto-generated method stub
		return 0;
	}

}
