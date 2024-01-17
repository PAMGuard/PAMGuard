package generalDatabase.dataExport;

import generalDatabase.SQLTypes;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Abstract class for ValueFilters for filtering database data tables. 
 * 
 * @author Doug Gillespie
 *
 */
public abstract class ValueFilterParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private boolean useMin;
	
	
	private boolean useMax;
	

	@Override
	protected ValueFilterParams clone() {
		try {
			return (ValueFilterParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the useMin
	 */
	public boolean isUseMin() {
		return useMin;
	}

	/**
	 * @param useMin the useMin to set
	 */
	public void setUseMin(boolean useMin) {
		this.useMin = useMin;
	}

	/**
	 * @return the useMax
	 */
	public boolean isUseMax() {
		return useMax;
	}

	/**
	 * @param useMax the useMax to set
	 */
	public void setUseMax(boolean useMax) {
		this.useMax = useMax;
	}

	/**
	 * Get the min value as a string
	 * @return min value as a string
	 */
	abstract public String getMinValue();
	
	/**
	 * GEt the max value as a string
	 * @return max value as a string
	 */
	abstract public String getMaxValue();
	
	/**
	 * Set the minimum value
	 * @param minValue min value as a string
	 * @return true if decoded successfully
	 */
	abstract public boolean setMinValue(String minValue);
	
	/**
	 * Set the maximum value
	 * @param maxValue max value as a string
	 * @return true if decoded successfully
	 */
	abstract public boolean setMaxValue(String maxValue);
	
	/**
	 * Get the length of a typical text field need to display these data. 
	 * @return the length of a typical text field need to display these data. 
	 */
	abstract public int getTextFieldLength();

	/**
	 * Get the maximum value in a format suitable for including in an SQL string
	 * @return the maximum value in a format suitable for including in an SQL string
	 */
	public String getMaxQueryValue(SQLTypes sqlTypes) {
		return getMaxValue();
	}
	/**
	 * Get the minimum value in a format suitable for including in an SQL string
	 * @return the minimum value in a format suitable for including in an SQL string
	 */
	public String getMinQueryValue(SQLTypes sqlTypes) {
		return getMinValue();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
