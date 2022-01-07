package soundPlayback.fx;

import javafx.beans.value.ChangeListener;
import pamViewFX.fxNodes.sliders.PamSlider;


/**
 * Default slider pane. 
 * @author Jamie Macaulay
 *
 */
public abstract class PlaySliderPane {

	private PamSlider slider;

	public PlaySliderPane() {
		slider = new PamSlider(getMinValue(), getMaxValue(), getMinValue());
		//setSliderSize();
	}

	public abstract double getMinValue();

	public abstract double getMaxValue();

	/**
	 * @return the slider
	 */
	public PamSlider getSlider() {
		return slider;
	}

	/**
	 * Get the scaled value of the slider
	 * @return the real scaled value
	 */
	public double getDataValue() {
		return slider.getValue();
	}

	/**
	 * Set the scaled value of the slider
	 * @param value the scaled value.
	 */
	public void setDataValue(double value) {
		slider.setValue(value);
	}

	/**
	 * Add a change listener. 
	 * @param changeListener
	 */
	public void addChangeListener(ChangeListener changeListener) {
		slider.valueProperty().addListener(changeListener);
	}
	
}
