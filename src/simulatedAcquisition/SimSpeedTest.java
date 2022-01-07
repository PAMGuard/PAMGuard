package simulatedAcquisition;

import java.util.Arrays;
import java.util.Random;

import Spectrogram.WindowFunction;

/**
 * Quick test of NBHF click simulation test. 
 * @author doug
 *
 */
public class SimSpeedTest {

	public static void main(String[] args) {
		int nTest = 100000;
		double sr = 500000;
		double f = 130e3;
		double l = .07e-3;
		double a = 0.07; // aperture radius. 
		double c = 1500; // speed of sound. 
		int nSamp = (int) (Math.floor(l*sr)+1);
		double[] winFunc = WindowFunction.hann(nSamp);
		double[] wav = new double[nSamp];
		long start = System.nanoTime();
		// check all ir's work OK. 
//		for (double ang = 0; ang < 90; ang++) {
//			getImpulseResponse(a, ang*Math.PI/180, sr, c);
//		}
		for (int iTest = 0; iTest < nTest; iTest++) {
			for (int i = 0; i < nSamp; i++) {
				double t = i/sr;
				wav[i] = winFunc[i] * Math.sin(2*Math.PI*t*f); 
			}
			double[] g = getImpulseResponse(a, (Math.random()-0.5)*Math.PI, sr, c);
//			double[] g = getImpulseResponse(a, 0*Math.PI/2, sr, c);
			// make an impulse response for a given angle. 
			double[] out = new double[g.length + nSamp -1];
			// pad the click to make the maths quicker. 
			wav = Arrays.copyOf(wav, out.length);
			// now do the convolution. 
			for (int j = 0; j < g.length; j++) {
				if (g[j] == 0) continue;
				for (int i = 0; i < nSamp; i++) {
					out[i+j] += g[j]*wav[i];
				}
			}
		}
		long taken = System.nanoTime()-start;
		System.out.println(String.format("Time taken for %d clicks = %3.1fs = %3.3fuS per click",
				nTest, (double) taken / 1e9, (double) taken / (double) nTest / 1000.));
		// now see how long it takes to make a minute of guassian data. 
		int blockLen = (int) sr;
		int nBlocks = 100;
		Random randNo = new Random();
//		randNo.
		start = System.nanoTime();
		for (int i = 0; i < nBlocks; i++) {
			double[] nse = new double[blockLen];
			for (int j = 0; j < blockLen; j++) {
				nse[j] = randNo.nextGaussian();
			}
		}
		taken = System.nanoTime() - start;
		double sTaken = (double) taken / 1.0e9;
		System.out.println(String.format("%3.1fs to generate %ds of data = %3.1fx real time", 
				sTaken, nBlocks, (double) nBlocks / sTaken));

	}
	
	static double[] getImpulseResponse(double a, double theta, double sr, double c) {
		double R = a * 1000;
		double r = R*Math.sin(theta);
		double z = R*Math.cos(theta);
		int nBins = (int) Math.ceil(2*a*sr/c);
		if (nBins%2 == 0) {
			// ensure there is a central bin. 
			nBins++;
		}
		int midBin = nBins/2;
		double[] g = new double[nBins];
		if (r <= a) {
			g[midBin] = 1;
			return g;
		}
		// for now just make a symetric function. 
		int totVals = 0;
		double t0 = R/c;
		double t1 =  t0 - Math.sqrt(z*z+(a-r)*(a-r))/c;
		double t2 =  Math.sqrt(z*z+(a+r)*(a+r))/c - t0;
		int n1 = (int) Math.floor(t1*sr);
		int n2 = (int) Math.floor(t2*sr);
		for (int i = -n1; i <= n2; i++) {
			double t = t0+i/sr;
			g[midBin+i] = Math.acos((c*c*t*t - z*z + r*r -a*a)/(2*r*Math.sqrt(c*c*t*t-z*z)));
			totVals += g[midBin+i];
		}

		for (int i = -n1; i <= n2; i++) {
			g[midBin+i] /= totVals;
		}
		
		return g;
		
	}

}
