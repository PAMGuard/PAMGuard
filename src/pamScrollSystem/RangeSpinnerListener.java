package pamScrollSystem;

public interface RangeSpinnerListener {

	/**
	 * fired when the value in the range spinner changes either 
	 * by the user pressing the spinner buttons or by typing a
	 * value and hitting the enter key. 
	 * @param oldValue old value of the spinner (seconds)
	 * @param newValue new value of the spinner (seconds)
	 */
	public void valueChanged(double oldValue, double newValue);
	
}
