package Filters;

import java.util.Arrays;

import fftManager.Complex;
import fftManager.FastFFT;

/**
 * Arbitrary response filters. 
 * this really doens't fit well into the filterMethod framework since
 * it no longer used a filterParams method, but will 
 * try to use as best I can so that these more arbitrary filters
 * can be used within the same framework as other types of filter. 
 * @author Doug Gillespie
 *
 */
public class FIRArbitraryFilter extends FIRFilterMethod {
	
	double[] frequency;
	
	double[] gain;
	
	int logFilterOrder;
	
	double windowGamma;

	private int filterLength;

	public FIRArbitraryFilter(double sampleRate, FilterParams filterParams) {
		super(sampleRate, null);
		if (filterParams != null) {
			setResponse(filterParams.getArbFreqsReNiquist(sampleRate), filterParams.getArbGainsFact(), filterParams.filterOrder, filterParams.chebyGamma);
		}
	}
	
	/**
	 * Set the filter response. The first and last 
	 * frequency points should correspond to the 0 and
	 * the niquist frequency
	 * @param frequency array of frequency values. 
	 * @param gain array of gain values. 
	 * @param filterOrder must be an exact  power of 2
	 */
	public void setResponse(double[] frequency, double[] gain, int logFilterOrder, double windowGamma) {
		if (frequency == null || gain == null) {
			return;
		}
		this.frequency = Arrays.copyOf(frequency, frequency.length);
		this.gain = Arrays.copyOf(gain, gain.length);
		this.logFilterOrder = logFilterOrder;
		this.windowGamma = windowGamma;
		calculateFilter();
	}

	@Override
	public int calculateFilter() {
		filterTaps = null;
		filterResponse = null;
		if (logFilterOrder == 0) {
			return 0;
		}
		int filterOrder = 1<<logFilterOrder;
		filterLength = filterOrder-1;
		double[] chebwin = ChebyWindow.getWindow(filterOrder, windowGamma);
		int lap = Math.round(filterOrder/25);
		int nFs = frequency.length; // number of frequency / gain values. 
		int nInt = nFs-1; // number of frequency intervals. 
		double[] df = new double[nInt];
		double[] H = new double[filterOrder+1]; 
		for (int i = 0; i < nInt; i++) {
			df[i] = frequency[i+1] - frequency[i];
		}
		int nb = 0;
		int ne = 0;
		double inc;
		H[0] = 0;
		for (int i = 0; i < nInt; i++) {
			if (df[i] == 0) {
				nb = nb - lap/2;
				ne = nb + lap;
			}
			else {
				ne = (int) Math.round(frequency[i+1]*filterLength)-1;
			}
			if (nb < 0 || ne > filterOrder) {
				System.out.println(String.format("Too abrupt change near end of frequency interval bins %d to %d", nb, ne));
				return 0;
			}
			if (nb == ne) {
				H[ne] = gain[i];
			}
			else {
				for (int j = nb; j <= ne; j++) { // interpolate values. 
					inc = (double)(j-nb)/(double)(ne-nb);
					H[j] = inc*gain[i+1] + (1-inc)*gain[i];
				}
			}
			nb = ne+1;
		}
		// time shift the data
		double dt = 0.5*(filterLength-1);
		double rad;
		Complex c = new Complex();
		Complex[] complexResponse = Complex.allocateComplexArray(2*filterOrder);
		for (int i = 0; i < H.length; i++) {
			rad = -dt*Math.PI*i/filterOrder;
			c.assign(Math.cos(rad), Math.sin(rad));
			complexResponse[i] = c.times(H[i]);
		}
		for (int i = 0, j = filterOrder*2-1; i < filterOrder-1; i++, j--) {
			complexResponse[j] = complexResponse[i+1].conj();
		}
		FastFFT fFFT = new FastFFT();
		fFFT.ifft(complexResponse, logFilterOrder+1);
		
		filterTaps = new double[filterLength];
		
		for (int i = 0; i < filterLength; i++) {
			filterTaps[i] = complexResponse[i].real*chebwin[i]/(filterOrder*2);
		}
		
		
		return filterLength;
	}

	@Override
	String filterName() {
		return "Arbitrary FIR filter";
	}


	@Override
	public double getFilterPhase(double omega) {
		// TODO Auto-generated method stub
		return 0;
	}

}
