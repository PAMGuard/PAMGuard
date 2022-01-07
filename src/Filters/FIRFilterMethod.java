package Filters;

import java.util.Arrays;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * FIR filter method, filter design uses a Cheychev window function.
 * <p>Bit of a bodge to make it fit into the framework originally developed 
 * for IIR filters. 
 * <p>Since it uses the same parameter set as the IIR filters, the filter order for 
 * FIR filters will be 2^N-1. 
 * @author Doug Gillespie
 *
 */
public class FIRFilterMethod extends FilterMethod {
	
	protected double[] filterTaps;
	
	protected double[] filterResponse;
	
	public static final int NRESPONSEPOINTS = 2048;

	public FIRFilterMethod(double sampleRate, FilterParams filterParams) {
		super(sampleRate, filterParams);
		calculateFilter();
	}

	@Override
	int calculateFilter() {
		filterResponse = null;
		int nPoints = 1<<filterParams.filterOrder;
		int nTaps = nPoints-1;
		double[] chebwin = ChebyWindow.getWindow(nPoints, filterParams.chebyGamma);
		double[] xtraTaps = null;
		switch(filterParams.filterBand) {
		case LOWPASS:
			filterTaps = getTaps(nTaps, filterParams.lowPassFreq, getSampleRate(), chebwin, false);
			break;
		case HIGHPASS:
			filterTaps = getTaps(nTaps, filterParams.highPassFreq, getSampleRate(), chebwin, true);
			break;
		case BANDPASS:
			filterTaps = getTaps(nTaps, minCutOff(), getSampleRate(), chebwin, false);
			xtraTaps = getTaps(nTaps, maxCutOff(), getSampleRate(), chebwin, true);
			break;
		case BANDSTOP:
			filterTaps = getTaps(nTaps, minCutOff(), getSampleRate(), chebwin, false);
			xtraTaps = getTaps(nTaps, maxCutOff(), getSampleRate(), chebwin, true);
			break;
		default:
			filterTaps = getTaps(nTaps, filterParams.lowPassFreq, getSampleRate(), chebwin, false);
			break;
		}
		if (xtraTaps != null) {
			for(int i = 0; i < nTaps; i++) {
				filterTaps[i] += xtraTaps[i];
			}
			if (filterParams.filterBand == FilterBand.BANDPASS) {
				invertBand(filterTaps);
			}
		}
		
		
		return nTaps;
	}
	
	private double minCutOff() {
		return Math.min(filterParams.lowPassFreq, filterParams.highPassFreq);
	}
	private double maxCutOff() {
		return Math.max(filterParams.lowPassFreq, filterParams.highPassFreq);
	}
	private void invertBand(double[] taps) {
		int mid = (taps.length-1)/2;
		for (int i = 0; i < taps.length; i++) {
			if (i == mid) {
				taps[i] = 1. - taps[i];
			}
			else {
				taps[i] = -taps[i];
			}			
		}
	}
	
	private double[] getTaps(int nTaps, double frequency, double sampleRate, double[] window, boolean highPass) {
		double[] taps = new double[nTaps];
		double K = frequency / (sampleRate/2.) * nTaps;
		double smallk;
		for (int i = 0; i < nTaps; i++) {
			smallk = i - nTaps/2;
			if (smallk == 0) {
				taps[i] = K/nTaps*window[i];
			}
			else {
				taps[i] = 1./nTaps*Math.sin(Math.PI*smallk*K/nTaps)/Math.sin(Math.PI*smallk/nTaps)*window[i];
			}
		}
		if (highPass) {
			invertBand(taps);
		}
		
		return taps;
	}

	@Override
	String filterName() {
		return "FIR filter";
	}

	@Override
	public double getFilterGain(double omega) {
		if (filterResponse == null) {
			calcFilterResponse();
		}
		if (filterResponse == null) {
			return 0;
		}
		int ind = (int) (omega / Math.PI * NRESPONSEPOINTS);
		if (ind < 0) ind = 0;
		if (ind >= NRESPONSEPOINTS) return 0;
		
		return filterResponse[ind];
	}

	private void calcFilterResponse() {
		if (filterTaps == null) {
			return;
		}
		DoubleFFT_1D fft = new DoubleFFT_1D(NRESPONSEPOINTS*2);
		double[] tempData = Arrays.copyOf(filterTaps, NRESPONSEPOINTS*2);
		fft.realForward(tempData);
		filterResponse = new double[NRESPONSEPOINTS];
		for (int i = 0; i < NRESPONSEPOINTS; i++) {
			filterResponse[i] = Math.sqrt(tempData[i*2]*tempData[i*2]+tempData[i*2+1]*tempData[i*2+1]);
		}
		if (filterParams != null && filterParams.filterBand == FilterBand.HIGHPASS) {
			filterResponse[0] = 0;
		}
	}

	@Override
	public double getFilterGainConstant() {
		return 1;
	}

	@Override
	public double getFilterPhase(double omega) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public	Filter createFilter(int channel) {
		return new FIRFilter(this, channel, getSampleRate());
	}

	/**
	 * @return the filterTaps
	 */
	public double[] getFilterTaps() {
		return filterTaps;
	}

}
