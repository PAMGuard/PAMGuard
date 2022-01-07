package PamModel.parametermanager;

import java.lang.reflect.Field;

/**
 * Abstract instance of PamParameterDataInterface which implements everything 
 * apart from the getData() function
 * @author dg50
 *
 */
public abstract class PrivatePamParameterData extends PamParameterData {

	/**
	 * @param parentObject
	 * @param field
	 * @param shortName
	 * @param toolTip
	 */
	public PrivatePamParameterData(Object parentObject, Field field, String shortName, String toolTip) {
		super(parentObject, field, shortName, toolTip);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param parentObject
	 * @param field
	 */
	public PrivatePamParameterData(Object parentObject, Field field) {
		super(parentObject, field);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
		/**
		 * This should really be implemented in every concrete class, but no time to do that now. Aim to delete 
		 * this function here, then go through and implement everywhere ...
		 */
		return false;
	}
	


}
