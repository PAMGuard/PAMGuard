package likelihoodDetectionModule.normalizer;

/**
 * This class is used to convert signal and background estimator times in seconds into window widths in samples
 * suitable for use with the SplitWindowNorm class. 
 * 
 * @author Dave Flogeras
 *
 */
public class SplitWindowNormWidths {

	public int signalWidth;
	public int backgroundWidth;
	
	/**
	 * Simple constructor. Takes precalculated values for the signalWidth abdn backgroundWidth
	 * 
	 * @param signalWidth
	 * @param backgroundWidth
	 */
	SplitWindowNormWidths( int signalWidth, int backgroundWidth ) {
		this.signalWidth = signalWidth;
		this.backgroundWidth = backgroundWidth;
	}
	
	/**
	 * Converts the signalTimeConstant and backgroundTimeConstant to windows widths in samples given the
	 * timeResolution.  If the signalTimeConstant is greater than the backgroundTimeConstant a
	 * RuntimeException is thrown.
	 * 
	 * @param signalTimeConstant
	 * @param backgroundTimeConstant
	 * @param timeResolution
	 */
	SplitWindowNormWidths( double signalTimeConstant, double backgroundTimeConstant,
							double timeResolution ) {
		
		if( signalTimeConstant > backgroundTimeConstant ) {
			throw new java.lang.RuntimeException();
		}
		
		this.signalWidth = (int)( signalTimeConstant / timeResolution );
		this.backgroundWidth = (int)( backgroundTimeConstant / timeResolution );
		
	}
	
}
