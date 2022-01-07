package likelihoodDetectionModule.normalizer;


/**This is a simple exponential (decaying) average.  For each input sample, the output is:
 *
 *  y[i] = x[i]*(1-alpha) + x[i-1]*alpha;
 *
 *  Obviously this requires knowledge of the previous sample which makes a problem for the very first data point.
 *  To get around this, we initialize the x[-1] point to the same value as the x[0] point.  After this, we simply
 *  keep a copy of the last sample.
 * 
 * @author Dave Flogeras
 *
 */
public class ExponentialAverager {
	
	private double alpha;
	private double lastSample;
	boolean initialized;
	
	ExponentialAverager( double alpha ) { 
		this.alpha = alpha;
		this.lastSample = 0;
		this.initialized = false;
	}
	
	
	// This consumes the data input, and returns an array of output data
	double[] Process( double[] data ) {
		
		double[] results = new double[ data.length ];
		
		if( data.length != 0 && ! initialized ) {
			lastSample = data[ 0 ];
			initialized = true;
		}
		
		int i = 0;
		for( double d : data ) {
			lastSample = d * ( 1 - alpha ) + lastSample * alpha;
			
			results[ i++ ] = lastSample;
		}
		
		return results;
		
	}
}
