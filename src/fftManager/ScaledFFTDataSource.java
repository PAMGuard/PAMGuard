package fftManager;

/**
 * Optional interface for a PamProcess whose FFTDataBlock output is not a dB
 * magnitude spectrum, but another quantity per frequency bin. For example,
 * the Azigram plugin outputs bearing in degrees.
 * <p>
 * The Spectrogram Display defaults to a dB colour scale
 * (SpectrogramParameters.amplitudeLimits), which draws most of such a range
 * as one flat colour. A process that implements this interface tells the
 * display which scale and fade values to use, so users do not have to set
 * them by hand.
 * <p>
 * Kept in fftManager so that displays and plugins can use it without
 * depending on each other.
 *
 * @author brian_mil
 */
public interface ScaledFFTDataSource {

	/**
	 * @return the recommended lower bound of the display colour scale for this
	 * source's output values (in whatever units they're expressed in - e.g.
	 * degrees for a bearing source).
	 */
	double getRecommendedScaleMin();

	/**
	 * @return the recommended upper bound of the display colour scale for this
	 * source's output values.
	 */
	double getRecommendedScaleMax();

	/**
	 * @return true if this source's values wrap around, such as a compass
	 * bearing where 359 degrees is next to 0 degrees. These are best shown
	 * with a circular colour map. Defaults to false. This is a hint rather
	 * than a named colour map, because Swing and FX each have their own
	 * ColourArrayType, and this interface depends on neither.
	 */
	default boolean isCircularScale() {
		return false;
	}

	/**
	 * @return the recommended fade floor, in the units of this source's
	 * getAlphaData() values. Cells below it are drawn in the display's floor
	 * colour. Defaults to 70, the display's original value, which assumes
	 * absolute dB SPL. Sources using other units, such as dB above
	 * background, should override it.
	 */
	default double getRecommendedFadeFloor() {
		return 70;
	}

	/**
	 * @return the recommended fade threshold, in the same units as
	 * getRecommendedFadeFloor(). Cells above it are drawn at full colour.
	 * Defaults to 90, the display's original value.
	 */
	default double getRecommendedFadeThreshold() {
		return 90;
	}

}
