package performanceTests;

import java.util.Random;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IirfFilter;
import PamUtils.PamUtils;
import fftManager.Complex;
import fftManager.FastFFT;

public class CPUFFTTest implements PerformanceTest {

	private String resultString;
	
	@Override
	public String getName() {
		return "Double precision number crunching";
	}

	@Override
	public String getResultString() {
		return resultString;
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public boolean runTest() {
		/**
		 * Generate a load of random numbers and do loads of FFTs
		 * of them. 
		 */

		int nTrial = 10000;
		int fftLen = 1024;
		long startTime, endTime;
		FastFFT fft = new FastFFT();
		
		FilterParams filterParams = new FilterParams();
		filterParams.filterType = FilterType.BUTTERWORTH;
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.filterOrder = 80;
		filterParams.lowPassFreq = 2000;
		filterParams.passBandRipple = 2;
		
		float sampleRate = 48000;
		
		Filter filter = new IirfFilter(0, sampleRate, filterParams);
		
		int m = PamUtils.log2(fftLen);
		
		double[] realData = new double[fftLen];
		Complex[] complexData = Complex.allocateComplexArray(fftLen);
		Random r = new Random();
		for (int i = 0; i < fftLen; i++) {
			realData[i] = r.nextGaussian();
		}
		startTime = System.currentTimeMillis();
		for (int i = 0; i < nTrial; i++) {
			fft.rfft(realData, complexData, m);
//			filter.runFilter(realData);
		}
		endTime = System.currentTimeMillis();
		long timeTaken = endTime-startTime;
		resultString = String.format("Time taken for %d %dpt FFT's = %d ms", nTrial, fftLen, timeTaken);

		return true;
	}

}
