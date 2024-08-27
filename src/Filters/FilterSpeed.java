package Filters;

import java.util.Random;

public class FilterSpeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// run some speed tests of filter operations
		FilterParams filtParams = new FilterParams();
		filtParams.filterBand = FilterBand.HIGHPASS;
		filtParams.highPassFreq = 10000;
		filtParams.lowPassFreq = 1000;
		filtParams.filterOrder = 3;
		filtParams.filterType = FilterType.CHEBYCHEV;
		filtParams.passBandRipple = 2.;
		
		int nTrials = 10000;
		int dataLen = 2000;
		IirfFilter filter = new IirfFilter(0, 48000, filtParams);
		filter.sayFilter();
		double data[] = new double[dataLen];
		double testData[] = new double[dataLen];
		Random r = new Random();
		for (int i = 0; i < dataLen; i++) {
			data[i] = r.nextGaussian();
		}
		testData = data.clone();
		long sn, en, mn = 0;
		double a;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < nTrials; i++) {
			sn = System.nanoTime();
			testData = data.clone();
			filter.runFilter(testData);
			en = System.nanoTime();
			mn = Math.max(mn, en-sn);
			for (int j = 0; j < dataLen; j++) {
				a  = Math.log10(testData[j]);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(String.format("Run time for %d filter runs of %d samples = %d ms, longest = %d ns", 
				nTrials, dataLen, endTime-startTime, mn/1000));
		int nan = 0, inf = 0;
		for (int i = 0; i < dataLen; i++) {
			if (Double.isNaN(data[i])) {
				nan ++;
			}
			if (Double.isInfinite(data[i])) {
				inf ++;
			}
		}
		if (inf > 0) {
			System.out.println(String.format("%d infinite values", inf));
		}
		if (nan > 0) {
			System.out.println(String.format("%d NaN values", nan));
		}
	}

}
