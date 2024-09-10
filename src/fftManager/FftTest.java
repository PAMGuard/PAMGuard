package fftManager;

import java.util.Random;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IirfFilter;
import PamUtils.CPUMonitor;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;

public class FftTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new FftTest().conjugateTest();
	}
	
	private void conjugateTest() {
		// check exactly how it does complex conj of real data. 
		FastFFT fastFFT = new FastFFT();
		int n = 8;
		double[] data = new double[n];
		for (int i = 0; i < n; i++) {
			data[i] = Math.random();
		}
		ComplexArray halfDat = fastFFT.rfft(data, n);
		double[] fullDat = data.clone();
		ComplexArray other = fastFFT.rfftFull(fullDat, n);
		double[] reverse = fastFFT.realInverse(halfDat);
		
		System.out.println(halfDat.toString());
		System.out.println(other.toString());
		
	}
	private void speedTest() {
		int nTrial = 100000;
		int fftLen = 2000;
		long startTime, endTime;
		FastFFT fastFFT = new FastFFT();
		FFT fft = new FFT();
		
		FilterParams filterParams = new FilterParams();
		filterParams.filterType = FilterType.BUTTERWORTH;
		filterParams.filterBand = FilterBand.LOWPASS;
		filterParams.filterOrder = 80;
		filterParams.lowPassFreq = 2000;
		filterParams.passBandRipple = 2;
		
		float sampleRate = 48000;
		
		Filter filter = new IirfFilter(0, sampleRate, filterParams);
		
//		try {
//			if (args.length > 0) {
//				nTrial = Integer.valueOf(args[0]); 
//			}
//			if (args.length > 1) {
//				fftLen = Integer.valueOf(args[1]); 
//			}
//		}
		nTrial = 10;
		fftLen = 512;
//		catch(NumberFormatException e) {
//			e.printStackTrace();
//			return;
//		}

		int m = PamUtils.log2(fftLen);
		
		double[] realData = new double[fftLen];
		ComplexArray complexData = null;
		Random r = new Random();
		for (int i = 0; i < fftLen; i++) {
			realData[i] = r.nextGaussian();
		}
		startTime = System.currentTimeMillis();
		CPUMonitor cpuMonitor = new CPUMonitor();
		cpuMonitor.start();
		for (int i = 0; i < nTrial; i++) {
			complexData = fastFFT.rfft(realData, fftLen);
//			filter.runFilter(realData);
		}
		cpuMonitor.stop();
		endTime = System.currentTimeMillis();
		System.out.println("Complex data len = " + complexData.length());
		long timeTaken = endTime-startTime;
		System.out.println(String.format("Time taken for %d %dpt FFT's = %d ms", nTrial, fftLen, timeTaken));
		System.out.println(cpuMonitor.getSummary());

	}
}
