package PamModel.parametermanager;

import java.lang.reflect.Field;

/**
 * Simple parameter description which can work with a public
 * field. Private fields require either using PamParameterDataGetter
 * or a bespoke solution. 
 * @author dg50
 *
 */
public class SimplePamParameterData extends PrivatePamParameterData {


	/**
	 * @param parentObject
	 * @param field
	 */
	public SimplePamParameterData(Object parentObject, Field field) {
		super(parentObject, field);
	}

	/**
	 * @param parentObject
	 * @param field
	 * @param shortName
	 * @param toolTip
	 */
	public SimplePamParameterData(Object parentObject, Field field, String shortName, String toolTip) {
		super(parentObject, field, shortName, toolTip);
	}

	@Override
	public Object getData() throws IllegalArgumentException, IllegalAccessException {
		return getField().get(getParentObject());
	}

	@Override
	public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = getField().getType();
		String className = type.getName();
//		switch (className) {
//		case (Double.)
//		}
			getField().set(getParentObject(), data);
		
		// TODO Auto-generated method stub
		return true;
	}
	
	


}
