package Localiser.algorithms;

import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IirfFilter;

public class UpSampler {
	
	private int upFactor = 2;
	
	private int filterOrder = 6;

	private IirfFilter filter;

	public UpSampler(int upFactor) {
		this.upFactor = upFactor;
	}
	
	public double[][] upSample(double[][] wav, int upFactor) {
		if (wav == null) {
			return null;
		}
		int nChan = wav.length;
		double[][] newWav = new double[nChan][];
		for (int i = 0; i < nChan; i++) {
			newWav[i] = upSample(wav[i], upFactor);
		}
		return newWav;
	}
	
	public double[] upSample(double[] wav, int upFactor) {
		if (wav == null) {
			return null;
		}
		if (filter == null || this.upFactor != upFactor) {
			createFilter(filterOrder, upFactor);
			this.upFactor = upFactor;
		}
		int len = wav.length;
		int newLen = len * upFactor;
		double[] newWav = new double[newLen];
		for (int i = 0, j = 0; i < len; i++, j += upFactor) {
			newWav[j] = wav[i] * upFactor;
		}
		filter.resetFilter();
		filter.runFilter(newWav);
		
		return newWav;
	}
	
	private void createFilter(int filterOrder, int upFactor) {
		FilterParams filterParams = new FilterParams();
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.lowPassFreq = (float) (1./(2.*upFactor));
		filterParams.filterType = FilterType.BUTTERWORTH;
		filterParams.filterOrder = 6;
		filter = new IirfFilter(0, 1, filterParams);
	}

}
