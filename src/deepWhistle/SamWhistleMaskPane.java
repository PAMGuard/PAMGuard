package deepWhistle;

/**
 * Settings pane for the {@link SamWhistleMask}. Identical to the
 * {@link DeepWhistleMaskPane} (a single confidence threshold plus a check that
 * the FFT source is configured correctly) but the SAM-Whistle model was trained
 * with a different FFT length and hop: an ~8 ms analysis window and a ~2 ms hop
 * (the reverse of the DeepWhistle model). The model was also trained with a
 * Hamming window - configure the FFT source's window accordingly for best
 * results.
 *
 * @author Jamie Macaulay
 */
public class SamWhistleMaskPane extends DeepWhistleMaskPane {

	/**
	 * Analysis window length the SAM-Whistle model expects, in seconds (8 ms).
	 */
	private static final double SAM_WINDOW_SECONDS = 0.008;

	/**
	 * Hop the SAM-Whistle model expects, in seconds (2 ms).
	 */
	private static final double SAM_HOP_SECONDS = 0.002;

	@Override
	protected double getWindowSeconds() {
		return SAM_WINDOW_SECONDS;
	}

	@Override
	protected double getHopSeconds() {
		return SAM_HOP_SECONDS;
	}

	@Override
	protected String getModelName() {
		return "SAM-Whistle";
	}

}
