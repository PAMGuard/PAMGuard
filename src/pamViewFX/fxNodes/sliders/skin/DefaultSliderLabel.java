package pamViewFX.fxNodes.sliders.skin;

/**
 * Default label format for range slider. Just shows current thumb value. 
 * @author Jamie Macaulay
 *
 */
public class DefaultSliderLabel implements RangeSliderLabel {

	@Override
	public String getText(Double value) {
		return String.format("%.1f", value);
	}

}
