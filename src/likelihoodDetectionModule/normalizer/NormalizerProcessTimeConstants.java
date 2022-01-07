package likelihoodDetectionModule.normalizer;

/**
 * A simple convenience class to pair signal and background time constants together.
 * 
 * @author Dave Flogeras
 *
 */
public class NormalizerProcessTimeConstants {

	public double signalTimeConstant;
	public double backgroundTimeConstant;
	
	public NormalizerProcessTimeConstants( double signalTimeConstant, double backgroundTimeConstant ) {
		
		this.signalTimeConstant = signalTimeConstant;
		this.backgroundTimeConstant = backgroundTimeConstant;
		
	}
	
}
