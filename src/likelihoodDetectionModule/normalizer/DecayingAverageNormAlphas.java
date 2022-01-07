package likelihoodDetectionModule.normalizer;

/**
 * This class is used to convert signal and background time constants to ExponentialAverager alpha values.
 * 
 * @author Dave Flogeras
 *
 */
public class DecayingAverageNormAlphas {

	public double signalAlpha;
	public double backgroundAlpha;
	
	/**
	 * Simple constructor, just takes already calculated alpha values
	 * 
	 * @param signalAlpha
	 * @param backgroundAlpha
	 */
	DecayingAverageNormAlphas( double signalAlpha, double backgroundAlpha ) {
		this.signalAlpha = signalAlpha;
		this.backgroundAlpha = backgroundAlpha;
	}
	
	/**
	 * Constructor.  Converts signal and background time constants (in seconds) to ExponentialAverager
	 * alpha values using the data timeResolution.
	 * 
	 * @param signalTimeConstant
	 * @param backgroundTimeConstant
	 * @param timeResolution
	 */
	DecayingAverageNormAlphas( double signalTimeConstant, double backgroundTimeConstant,
								double timeResolution ) {
		
		this.signalAlpha = 1 - timeResolution / signalTimeConstant;
		this.backgroundAlpha = 1 - timeResolution / backgroundTimeConstant;
		
		// Range checks
		if( this.signalAlpha < 0 ) this.signalAlpha = 0;
		if( this.signalAlpha >= 1. ) this.signalAlpha = 0.9999999;
		if( this.backgroundAlpha < 0 ) this.backgroundAlpha = 0;
		if( this.backgroundAlpha >= 1. ) this.backgroundAlpha = 0.9999999;
	}
	
}
