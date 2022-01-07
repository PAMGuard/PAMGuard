package loggerForms;

/**
 * 
 * @author Graham Weatherup
 *
 */
public class PropertyDescription extends ItemDescription implements Cloneable {


	private PropertyTypes eType;
	
	public PropertyDescription(FormDescription formDescription, ItemInformation itemInformation) {
		// TODO Auto-generated constructor stub
		super(formDescription, itemInformation);
		eType = PropertyTypes.valueOf(getType());

	}
	

	public static enum hotkeys{/*F1,F2,F3,F4,*/F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19,F20,F21,F22,F23,F24}
	
	/**
	 * @return the property Type
	 */
	public PropertyTypes getPropertyType() {
		return eType;
	}

	public static boolean isProperty(String type){	
		if (type == null) {
			return false;
		}
		PropertyTypes c = null;
		try {
			c = PropertyTypes.valueOf(type);
		}
		catch (IllegalArgumentException e) {
		}
		catch (NullPointerException e) {
			System.out.println("Null property: " + type);
		}
		return (c != null);
	}

	/**
	 * 
	 */
	@Override
	public String getItemWarning() {
		// TODO Auto-generated method stub
		return null;
	}
	
		
}
