package PamModel.parametermanager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Parameter description which can use a single parameterless getter
 * to retrieve a field from a class. 
 * @author dg50
 *
 */
public class PamParameterDataGetter extends PrivatePamParameterData {

	/**
	 * Getter and setter methods that can return or set a single field. 
	 */
	private Method getter;
	
	private Method setter;

	/**
	 * 
	 * @param parentObject
	 * @param field
	 * @param getter
	 */
	public PamParameterDataGetter(Object parentObject, Field field, Method getter) {
		super(parentObject, field);
		this.getter = getter;
	}

	/**
	 * @param parentObject
	 * @param field
	 * @param shortName
	 * @param toolTip
	 */
	public PamParameterDataGetter(Object parentObject, Field field, Method getter, String shortName, String toolTip) {
		super(parentObject, field, shortName, toolTip);
		this.getter = getter;
	}


	@Override
	public Object getData() throws IllegalAccessException, IllegalArgumentException {
		try {
			return getter.invoke(getParentObject());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
		if (setter == null) {
			return false;
		}
		try {
			setter.invoke(getParentObject(), data);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
