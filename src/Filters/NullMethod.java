package Filters;

public class NullMethod extends FilterMethod {

	public NullMethod(double sampleRate, FilterParams filterParams) {
		super(sampleRate, filterParams);
	}

	@Override
	int calculateFilter() {
		return 0;
	}

	@Override
	public Filter createFilter(int channel) {
		return new NullFilter();
	}

	@Override
	String filterName() {
		return "Null filter";
	}

	@Override
	public double getFilterGain(double omega) {
		return 1;
	}

	@Override
	public double getFilterGainConstant() {
		return 1;
	}

	@Override
	public double getFilterPhase(double omega) {
		return 0;
	}

}
