package pamViewFX.fxNodes;

import javafx.collections.ObservableList;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Spinner with some bug fixes from raw JavaFX versions. 
 * @author Jamie Macaulay
 *
 * @param <T>
 */
public class PamSpinner<T> extends Spinner<T>{

	public PamSpinner() {
		super();
		// TODO Auto-generated constructor stub
	}


	public PamSpinner(double min, double max, double initialValue, double amountToStepBy) {
		super(min, max, initialValue, amountToStepBy);
		addDefocusConverter();
	}


	public PamSpinner(double min, double max, double initialValue) {
		super(min, max, initialValue);
		addDefocusConverter();
	}

	public PamSpinner(int min, int max, int initialValue, int amountToStepBy) {
		super(min, max, initialValue, amountToStepBy);
		addDefocusConverter();
	}

	public PamSpinner(int min, int max, int initialValue) {
		super(min, max, initialValue);
		addDefocusConverter();
	}

	public PamSpinner(ObservableList<T> arg0) {
		super(arg0);
		addDefocusConverter();
	}

	public PamSpinner(SpinnerValueFactory<T> arg0) {
		super(arg0);
		addDefocusConverter();
	}
	
	/**
	 * Adds a listener so that if a value is typed and spinner is de-focused it is set as the value property
	 * of the spinner. The de-focus problem is a known issue in JavaFX spinners- this may be unnecessary in Java 9+
	 */
	private void addDefocusConverter() {
		this.focusedProperty().addListener((observable, oldValue, newValue) -> {
			  if (!newValue) {
			    this.increment(0); // won't change value, but will commit editor
			  }
			});
	}
	
	/**
	 * Convenience function to make a converter which shows numbers to N decimal places
	 * @param nDecimal - the number of decimal places
	 * @return the string converter. 
	 */
	public static StringConverter<Double> createStringConverter(int nDecimal) {
		PamStringConverter stringConverter = new PamStringConverter();
		stringConverter.setSignificantDigits(nDecimal); 
		return stringConverter; 
	}


}
