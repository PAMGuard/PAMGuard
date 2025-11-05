package Filters;

import fftFilter.FFTFilter;
import fftFilter.FFTFilterParams;

public class FFTFilterMethod extends FilterMethod {

	public FFTFilterMethod(double sampleRate, FilterParams filterParams) {
		super(sampleRate, filterParams);
	}

	@Override
	String filterName() {
		return "FFT Filter";
	}

	@Override
	int calculateFilter() {
		return 0;
	}

	@Override
	public double getFilterGain(double omega) {
		double tiny = 1e-6;
		if (getSampleRate() <= 0) {
			return 1.;
		}
		double lp = filterParams.lowPassFreq / getSampleRate();
		double hp = filterParams.highPassFreq / getSampleRate();
		switch(filterParams.filterBand) {
		case BANDPASS:
			if (omega < hp || omega > lp) {
				return tiny;
			}
			break;
		case BANDSTOP:
			if (omega > hp && omega < lp) {
				return tiny;
			}
			break;
		case HIGHPASS:
			if (omega < hp) {
				return tiny;
			}
			break;
		case LOWPASS:
			if (omega > lp) {
				return tiny;
			}
			break;
		default:
			break;
		
		}
		return 1.0;
	}

	@Override
	public double getFilterPhase(double omega) {
		return 0;
	}

	@Override
	public double getFilterGainConstant() {
		return 1.0;
	}

	@Override
	public Filter createFilter(int channel) {
		FFTFilterParams fftFiltParams = new FFTFilterParams(getFilterParams());
		FFTFilter fftFilter = new FFTFilter(fftFiltParams, (float) getSampleRate());
		fftFilter.setRemoveMean(false);
		return fftFilter;
	}

}
