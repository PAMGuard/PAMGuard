package soundPlayback.fx;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.sliders.PamSlider;


/**
 * Default slider pane for the sound output sliders. 
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class PlaySliderPane extends PamHBox {

	/**
	 * The slider to chnage the value of something. 
	 */
	private PamSlider slider;
	
	/**
	 * The label which shows a slider value
	 */
	private Label sliderLabel;

	public PlaySliderPane() {
		slider = new PamSlider(getMinValue(), getMaxValue(), getMinValue());
		//slider.setShowTickMarks(true);
		slider.setMaxWidth(Double.POSITIVE_INFINITY);
		//slider.setShowTickLabels(true);
		//setSliderSize();
		
		sliderLabel = new Label(); 
		slider.setId("thickslider");
		
		HBox.setHgrow(slider, Priority.ALWAYS);
		
		this.setAlignment(Pos.CENTER_LEFT);
		this.setSpacing(5);
		
		slider.setTrackColor(Color.rgb(0,204,204));
		slider.setTrackColor(Color.DODGERBLUE);
		//slider.setTrackColor(Color.GRAY);
		
		this.getChildren().addAll(slider,sliderLabel);
	}

	/**
	 * get the minimum value of the slider
	 * @return the minimum value fo the slider. 
	 */
	public abstract double getMinValue();

	/**
	 * Get the maximum value of the slider. 
	 * @return the maximum value of the slider. 
	 */
	public abstract double getMaxValue();

	/**
	 * @return the slider
	 */
	public Slider getSlider() {
		return slider;
	}

	/**
	 * Get the scaled value of the slider
	 * @return the real scaled value
	 */
	public double getDataValue() {
		return posToValue(slider.getValue());
	}

	/**
	 * Set the scaled value of the slider
	 * @param value the scaled value.
	 */
	public void setDataValue(double value) {
		slider.setValue(valueToPos(value));
	}

	/**
	 * Add a change listener. 
	 * @param changeListener
	 */
	public void addChangeListener(ChangeListener changeListener) {
		slider.valueProperty().addListener(changeListener);
	}
	
	
	public void setTextLabel(String textValue) {
		this.sliderLabel.setText(textValue);
	}

	
	/**
	 * Get the slider position from a value (usually value = out).
	 * @param value 
	 * @return
	 */
	public double valueToPos(double value) {
		return value;
	}

	/**
	 * Get the value from a slider position (usually pos = out).
	 * @param pos - the position of the slider. 
	 * @return - the corresponding value. 
	 */
	public double posToValue(double pos) {
		return pos;
	}
	
}
