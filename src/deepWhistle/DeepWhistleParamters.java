package deepWhistle;


public class DeepWhistleParamters extends MaskedFFTParamters {

	private static final long serialVersionUID = 1L;

	/**
	 * Default confidence threshold for whistle detection.
	 */
	public static final double DEFAULT_CONFIDENCE = 0.5;

	/**
	 * Confidence threshold for whistle detection
	 */
	public double confidenceThreshold = DEFAULT_CONFIDENCE;

}
