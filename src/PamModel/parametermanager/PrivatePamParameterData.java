package PamModel.parametermanager;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

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
//		return false;
		Object convData = convertStringType(data);
		getField().set(this, convData);
		
		return true;
	}
	
	/**
	 * convert a string type to a different type appropriate for the field in
	 * question. 
	 * @param value
	 * @return
	 */
	public Object convertStringType(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String == false) {
			return value;
		}
		String str = (String) value;
		Type type = getField().getGenericType();
		Class<?> cls = getField().getType();
		String clsName = cls.getName();
		switch (clsName) {
		case "int":
		case "Integer":
			return Integer.valueOf(str);
		case "double":
		case "Double":
			return Double.valueOf(str);
		case "float":
		case "Float":
			return Float.valueOf(str);
		case "short":
		case "Short":
			return Short.valueOf(str);
			
		}
		
		return value;
	}


}
