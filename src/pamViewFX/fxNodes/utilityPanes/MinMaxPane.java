package pamViewFX.fxNodes.utilityPanes;

import java.io.Serializable;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Pane which allows users to set minimum and maximum value of a variable
 * 
 * <T> the number type e.g. Double, Integer, Long
 * 
 * @author Jamie Macaulay
 *
 */
public class MinMaxPane<T extends Number> extends PamBorderPane {
	
	/**
	 * Check box to enable or disable the variable 
	 */
	private CheckBox checkBox;
	
	/**
	 * The min spinner
	 */
	private PamSpinner<T> minSpinner;
	
	/**
	 * The max spinner
	 */
	private PamSpinner<T> maxSpinner;

	
	/**
	 * Create a min/max pane. This is a minimum and maximum spinner with approriate labels. 
	 * @param varName - the name of the variable (e.g. distance)
	 * @param unitType - the unit of the variable (e.g. meters)
	 * @param min - the minimum value of the variable.
	 * @param max - the maximum allowed value of the variable.
	 * @param step - the step size for the spinners. 
	 */
	public MinMaxPane(String varName, String unitType, T min, T max, T step){
		this.setCenter(createMinMaxPane(varName, unitType, min, max, step));
	}
	
	/**
	 * Create the minimum and maximum pane. 
	 * @return the minimum/maximum pane. 
	 */
	private Pane createMinMaxPane(String varName, String unitType, T min, T max, T step) {
		
		PamHBox pamHBox = new PamHBox();
		pamHBox.setSpacing(5);
		pamHBox.setAlignment(Pos.CENTER_LEFT);
		
		//e
		checkBox = new CheckBox(varName); 
		checkBox.setOnAction(action->{
			enableConttrols(); 
		});
		checkBox.setPrefWidth(115);
		
		//need to instantiate with min, max, initial and step in order top prevent value factory from returning null?
		minSpinner = new PamSpinner<T>(min.doubleValue(), max.doubleValue(), 0, step.doubleValue()); 
		minSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minSpinner.setPrefWidth(100);
		minSpinner.setEditable(true);

		
		maxSpinner = new PamSpinner<T>(min.doubleValue(), max.doubleValue(), 0, step.doubleValue());
		maxSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxSpinner.setPrefWidth(100);
		maxSpinner.setEditable(true);

		
		pamHBox.getChildren().addAll(checkBox, new Label("Min"),
				minSpinner, new Label("Max"), maxSpinner, new Label(unitType)); 
	
		return pamHBox; 
	}
	
	/**
	 * Set the preferred width of the check box and name
	 * @param width - the width to set. 
	 */
	public void setNamePrefWidth(double width) {
		checkBox.setPrefWidth(width);
	}
	
	/**
	 * Set the preferred width of the minimum and maximum spinners
	 * @param width - the width to set. 
	 */
	public void setSpinnersPrefWidth(double width) {
		minSpinner.setPrefWidth(width);
		maxSpinner.setPrefWidth(width);
	}
	
	/**
	 * Enable or disable the pane depending on whether the check box has been selected. 
	 */
	private void enableConttrols(){
		minSpinner.setDisable(!checkBox.isSelected());
		maxSpinner.setDisable(!checkBox.isSelected());
	}
	
	
	/**
	 * Set parameters for the minimum/maximum pane. 
	 * @param minMaxParams - the min/max parameter class. 
	 */
	public void setParams(MinMaxParams minMaxParams) {
		setParams(minMaxParams.min, minMaxParams.max, minMaxParams.enabled); 
	}
	
	public MinMaxParams getParams() {
		return new MinMaxParams(minSpinner.getValue(), maxSpinner.getValue(), checkBox.isSelected());
	}
	
	/**
	 * Set parameters for the minimum/maximum pane. 
	 * @param min - the minimum value.
	 * @param max - the maximum value.
	 * @param enabled - true to enable the pane. 
	 */
	public void setParams(T min, T max,boolean enabled) {
		this.minSpinner.getValueFactory().setValue(min);
		this.maxSpinner.getValueFactory().setValue(max);
		this.checkBox.setSelected(enabled); 
		enableConttrols();
	}
	
	/**
	 * Holds the min and max variables. 
	 * @author Jamie Macaulay
	 *
	 */
	public class MinMaxParams implements Serializable {
		
		public MinMaxParams(T min, T max, boolean enabled) {
			this.min = min;
			this.max = max; 
			this.enabled = enabled; 
		}
		
		/**
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The minimum value
		 */
		public T min;
		
		/**
		 * The maximum value
		 */
		public T max; 
		
		/**
		 * True if the pane is enabled or not. 
		 */
		public boolean enabled = true;
		
	}

	/**
	 * Set the converter for the spinners. 
	 * @param aStringConverter - the string converter to set. 
	 */
	public void setConverter(StringConverter<T> aStringConverter) {
		this.minSpinner.getValueFactory().setConverter(aStringConverter);
		this.maxSpinner.getValueFactory().setConverter(aStringConverter);

	}

}
