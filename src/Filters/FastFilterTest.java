package Filters;

import java.util.Random;

public class FastFilterTest {

	public static void main(String[] args) {
		FilterParams filtParams = new FilterParams();
		filtParams.filterBand = FilterBand.BANDPASS;
		filtParams.highPassFreq = 10000;
		filtParams.lowPassFreq = 1000;
		filtParams.filterOrder = 4;
		filtParams.filterType = FilterType.CHEBYCHEV;
		filtParams.passBandRipple = 2.;

		String[] names = {"iiir", "fast"};
		int nSamples = 12800;
		int nTrials = 300;

		int channel = 0;
		float sampleRate = 48000;

		Filter[] filters = new Filter[2];
		filters[0] = new IirfFilter(channel, sampleRate, filtParams);
		filters[1] = new FastIIRFilter(channel, sampleRate, filtParams);

		double[][] dataOut = new double[2][nSamples];
		double[] inputData = new double[nSamples];
		Random r = new Random();
		long[] ftime = new long[2];
		for (int t = 0; t < nTrials; t++) {
			for (int i = 0; i < nSamples; i++) {
				inputData[i] = r.nextGaussian();
			}
			for (int f = 0; f < 2; f++) {
				long t1 = System.nanoTime();
				//			System.out.print(names[f] + " = [");
				for (int i = 0; i < nSamples; i++) {
					dataOut[f][i] = filters[f].runFilter(inputData[i]);
					//				if (i > 0) {
					//					System.out.print(",");
					//				}
					//				System.out.print(String.format("%6.4f", dataOut[f][i]));
				}
				ftime[f] += (System.nanoTime()-t1);
				//			System.out.println("];");
			}
		}
		for (int i = 0; i < 2; i++) {
			double totSecs = (double) ftime[i] / 1.0e9;
			double totSamples = nTrials * nSamples; 
			System.out.println(String.format("%s took %d nanoseconds = %7.0f ks/sec", names[i], ftime[i], totSamples/totSecs/1000));
		}
		System.out.println(String.format("Speed increase = x%3.2f", (double) ftime[0] / (double) ftime[1]));
	}

}
