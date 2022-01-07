package clickDetector.ClickClassifiers.basicSweep;

import pamMaths.Regressions;
import pamMaths.STD;
import rocca.RoccaContourStats;

/**
 * Simple container for a few zero crossing parameters. 
 * Will extract these from zero crosing data sent to the 
 * constructor. 
 * @author Doug Gillespie
 *
 */
public class ZeroCrossingStats {

	double[] zeroCrossings;
	double sampleRate;
	
	/**
	 * Number of zero crossings
	 */
	public int nCrossings;
	
	/**
	 * Zero crossing sweep rate in Hz/second
	 */
	public double sweepRate;
	
	/**
	 * Zero crossing start frequency from fit. 
	 */
	public double startFreq;
	
	/**
	 * Zero crossing end frequency from fit
	 */
	public double endFreq;
	
	/**
	 * Mean time between zero crossings, in milliseconds
	 */
	double meanTime;
	
	/**
	 * Median time between zero crossings, in milliseconds
	 */
	double medianTime;
	
	/**
	 * Variance of the time between zero crossings, in milliseconds(squared)
	 */
	double varianceTime;
	
	/**
	 * Default constructor, does nothing. 
	 */
	public ZeroCrossingStats() {
		
	}
	
	/**
	 * Constructor which automatically extracts parameters
	 * from some zero crossing data
	 * @param zeroCrossings array of zero crossing times in samples
	 * @param sampleRate sample rate
	 */
	public ZeroCrossingStats(double[] zeroCrossings, double sampleRate) {
		this.zeroCrossings = zeroCrossings;
		this.sampleRate = sampleRate;
		extractParams();
	}
	
	private void extractParams() {
		nCrossings = zeroCrossings.length;
		if (nCrossings < 2) {
			startFreq = endFreq = sweepRate = 0;
			return;
		}
		double[] freqData = new double[nCrossings-1];
		double[] t = new double[nCrossings-1];
		double[] deltaT = new double[nCrossings-1];	// added MO 2015/05/17
		for (int i = 0; i < nCrossings-1; i++) {
			freqData[i] = sampleRate / 2 / (zeroCrossings[i+1]-zeroCrossings[i]);
			t[i] = ((zeroCrossings[i+1]+zeroCrossings[i])/2 - zeroCrossings[0]) / sampleRate;
			deltaT[i] = (zeroCrossings[i+1]-zeroCrossings[i]) / sampleRate;	// added MO 2015/05/17
		}
		if (freqData.length < 2) {
			startFreq = endFreq = freqData[0];
			sweepRate = 0;
			return;
		}
		double[] fitData = Regressions.linFit(t, freqData);
		sweepRate = fitData[1];
		startFreq = fitData[0];
		endFreq = fitData[0] + sweepRate * t[t.length-1];
		
		/* Add mean, median and variance calcs
		 * Mike Oswald 2015/05/17
		 */
		STD std = new STD();
		meanTime = std.getMean(deltaT)*1000;
		medianTime = std.getMedian()*1000; 
		varianceTime = std.getVariance()*1000*1000; 
	}

	/**
	 * Method to return the zero crossing data
	 * Mike Oswald 2015/05/17
	 * 
	 * @return the zeroCrossings
	 */
	public double[] getZeroCrossings() {
		return zeroCrossings;
	}

	/**
	 * Method to return the mean time between zero crossings (in milliseconds)
	 * Mike Oswald 2015/05/17
	 * 
	 * @return the meanTime
	 */
	public double getMeanTime() {
		return meanTime;
	}

	/**
	 * Method to return the median time between zero crossings (in milliseconds)
	 * Mike Oswald 2015/05/17
	 * 
	 * @return the medianTime
	 */
	public double getMedianTime() {
		return medianTime;
	}

	/**
	 * Method to return the variance of the time between zero crossings (in milliseconds^2)
	 * Mike Oswald 2015/05/17
	 * 
	 * @return the varianceTime
	 */
	public double getVarianceTime() {
		return varianceTime;
	}
	
	
	
}
