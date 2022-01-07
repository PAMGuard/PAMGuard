package likelihoodDetectionModule.normalizer;


/** This class is almost identical to the simple exponential (decaying) average.  For each input sample, the output is:
 *
 *  y[i] = x[i]*(1-alpha) + x[i-1]*alpha;
 *
 *  However, there are two values for alpha, a highAlpha and a lowAlpha.  Which one gets used in the above formula
 *  is based upon the following logic:
 * 
 *  if referenceInput[ i ] < output[ i - 1 ], then lowAlpha is used
 *  if referenceInput[ i ] > output[ i - 1 ], then highAlpha is used
 *
 *
 *  Again, just like the simple ExponentialAverager this requires knowledge of the previous sample which makes a
 *  problem for the very first data point.  To get around this, we initialize the x[-1] point to the same value
 *  as the x[0] point.  After this, we simply keep a copy of the last sample.
 * 
 * @author Dave Flogeras
 *
 */

public class TwoSpeedExponentialAverager {
	
	private double highAlpha;
	private double lowAlpha;
	private double lastSample;
	private boolean initialized;
	
	TwoSpeedExponentialAverager( double highAlpha, double lowAlpha ) {
		this.highAlpha = highAlpha;
		this.lowAlpha = lowAlpha;
		this.lastSample = 0;
		this.initialized = false;
	}
	
	public double[] Process( double[] data, double[] referenceInput ) {
		
		double[] results = new double[ data.length ];
		
		if( data.length != 0 && ! initialized ) {
			lastSample = data[0];
			initialized = true;
		}
		
		int i = 0;
		for( double d : data ) {
			if( referenceInput[ i ] > lastSample ) {
				lastSample = d * ( 1 - highAlpha ) + lastSample * highAlpha;					
			}
			else {
				lastSample = d * ( 1 - lowAlpha ) + lastSample * lowAlpha;
			}
			
			results[ i++ ] = lastSample;
			
		}
		
		return results;
	}
	
}
