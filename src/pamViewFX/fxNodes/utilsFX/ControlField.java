package pamViewFX.fxNodes.utilsFX;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Convenience class. Often used in dialogs. A label, text field and label again.
 * 
 * @author Jamie Macaulay
 *
 */
public class ControlField<T> extends PamHBox {

	/**
	 * The spinner. 
	 */
	protected PamSpinner<T> spinner;

	/**
	 * Text field. 
	 */
	private TextField textField;

	/**
	 * The first label
	 */
	private Label label1; 

	/**
	 * The second label
	 */
	private Label label2; 


	/**
	 * Get the text field. 
	 * @return the text field. 
	 */
	public TextField getTextField() {
		return textField;
	}

	/**
	 * Get the spinner control. Will be null if a text field has been used
	 * @return the spinner control. 
	 */
	public PamSpinner<T> getSpinner() {
		return spinner;
	}

	/**
	 * Set the value of text box or spinner
	 * @param value- the value to set. 
	 */
	public void setValue(T value) {
		if (spinner!=null)  spinner.getValueFactory().setValue(value);
		if (textField!=null)  textField.setText(value.toString());
	}

	/**
	 * Get the value of the text box or spinner. 
	 * @return the value. 
	 */
	@SuppressWarnings("unchecked")
	public T getValue() {
		if (spinner!=null) return spinner.getValue(); 
		else if (textField!=null) return (T) textField.getText();
		return null;
	}


	/**
	 * Create a control field which has a text input field 
	 * @param label1 - the name of the control label.  
	 * @param label2 - the units of the control label. 
	 */
	public ControlField(Label label1, Label label2){
		this.getChildren().addAll(this.label1 = label1, textField=new TextField(), this.label2 = label2);
		prefStyle();
	}

	/**
	 * Create a control field which has a text input field 
	 * @param label1 - the name of the control 
	 * @param label2 - the units of the controls
	 */
	public ControlField(String label1, String label2){
		this.getChildren().addAll(this.label1 =  new Label(label1), textField=new TextField(), this.label2 =  new Label(label2));
		prefStyle();
	}

	/**
	 * Create a control field which has a spinner. 
	 * @param label1 - the name of the control .
	 * @param label2 - the units of the controls.
	 * @param minValue - the minimum allowed value of the spinner
	 * @param maxValue - the maximum allowed value of the spinner., 
	 * @param stepSize - the step size. 
	 */
	public ControlField(String label1, String label2, double minValue, double maxValue, double stepSize){
		this.getChildren().addAll(this.label1 = new Label(label1), spinner=
				new PamSpinner<T>(minValue, maxValue, minValue, stepSize), this.label2 = new Label(label2));
		spinner.setEditable(true);
		prefStyle();
	}

	/**
	 * Set default style for the control. 
	 */
	private void prefStyle() {
		if (textField!=null) textField.setPrefWidth(100);
		if (spinner!=null) {
			spinner.setPrefWidth(100);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		}
		this.setAlignment(Pos.CENTER_LEFT);
		this.setSpacing(5);
	}

	/**
	 * Set a tool tip both the labels and the control.
	 * @param tooltip - the tool tip to set. 
	 */
	public void setTooltip(Tooltip tooltip) {
		label1.setTooltip(tooltip); 
		label2.setTooltip(tooltip); 
		if (spinner!=null) spinner.setTooltip(tooltip); 
		if (textField!=null) textField.setTooltip(tooltip); 

	}

	/**
	 * Get the first label. 
	 * @return the label. 
	 */
	public Label getLabel1() {
		return label1; 		
	}
	

	/**
	 * Get the second label. 
	 * @return the label. 
	 */
	public Label getLabel2() {
		return label2; 		
	}


}


