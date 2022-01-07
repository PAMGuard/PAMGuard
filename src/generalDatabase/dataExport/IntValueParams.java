package generalDatabase.dataExport;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * 
 * Class for filtering integer values in a database table
 * @author Doug Gillespie
 *
 */
public class IntValueParams extends ValueFilterParams implements ManagedParameters {

	private static final long serialVersionUID = 1L;

	private int minValue;
	
	private int maxValue;
	
	/**
	 * Get the min value as a string
	 * @return min value as a string
	 */
	@Override
	public String getMinValue() {
		return Integer.valueOf(minValue).toString();
	}
	
	/**
	 * Get the max value as a string
	 * @return max value as a string
	 */
	@Override
	public String getMaxValue() {
		return Integer.valueOf(maxValue).toString();
	}
	/**
	 * Set the minimum value
	 * @param minValue min value as a string
	 * @return true if decoded successfully
	 */
	@Override
	public boolean setMinValue(String minValue) {
		try {
			this.minValue = Integer.valueOf(minValue);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Set the maximum value
	 * @param maxValue max value as a string
	 * @return true if decoded successfully
	 */
	@Override
	public boolean setMaxValue(String maxValue) {
		try {
			this.maxValue = Integer.valueOf(maxValue);
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get the length of a typical text field need to display these data. 
	 * @return the length of a typical text field need to display these data. 
	 */
	@Override
	public int getTextFieldLength() {
		return 5;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
