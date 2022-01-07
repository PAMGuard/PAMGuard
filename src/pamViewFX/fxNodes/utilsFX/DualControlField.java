package pamViewFX.fxNodes.utilsFX;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Control field with two spinner. A label, spinner, label, spinner and label. 
 * 
 * @author Jamie Macaulay 
 */
public class DualControlField<T> extends ControlField<T> {


	/**
	 * The third label (after spinner 2)
	 */
	private Label label3;


	/**
	 * The second spinner. 
	 */
	private PamSpinner<T> spinner2;
	

	public DualControlField(String label1, String label2, String label3, double minValue, double maxValue, double stepSize) {
		super(label1, label2, minValue, maxValue, stepSize);
		this.getChildren().addAll(spinner2 =
				new PamSpinner<T>(minValue, maxValue, minValue, stepSize), this.label3 = new Label(label3)); 
		
		spinner2.setPrefWidth(100);
		spinner2.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner2.setEditable(true);
	}

	
	/**
	 * Set the value of the two spinners. 
	 * @param value1- the value to set for the first spinner
	 * @param - the value tos et for the second spinner
	 */
	public void setValues(T value, T value2) {
		super.setValue(value);
		spinner2.getValueFactory().setValue(value2);
	}
	
	/**
	 * Set the value of the second spinner
	 * @param value2 - the second value to set. 
	 */
	public void setValue2( T value2) {
		spinner2.getValueFactory().setValue(value2);
	}
	
	/**
	 * Get the value of the second spinner. 
	 * @return the spinner value. 
	 */
	public T getValue2() {
		return spinner2.getValue(); 
	}
	
	/**
	 * Get the third label (after spinner 2)
	 * @return the label. 
	 */
	public Label getLabel3() {
		return label3;
	}


	/**
	 * Add a change listener to both spinners. 
	 * @param object - the chnage listener. 
	 */
	public void addChangeListener(ChangeListener<? super T> object) {
		spinner.valueProperty().addListener(object);
		spinner2.valueProperty().addListener(object);
	}

	/**
	 * Set the preferred width of the spinner. 
	 * @param width - the width of the spinner. 
	 */
	public void setPrefSpinnerWidth(int width) {
		spinner.setPrefWidth(width);
		spinner2.setPrefWidth(width);
	}

	/**
	 * Get the second spinner. 
	 * @return the second spinner. 
	 */
	public  PamSpinner<T>  getSpinner2() {
		return spinner2;
	}


}
