package PamModel.parametermanager;

import java.lang.reflect.Field;

/**
 * Description of data within a field in a class
 * @author Doug Gillespie
 *
 */
abstract public class PamParameterData {
	
	/**
	 * Name of the field (variable)
	 */
	private Field field;
	
	/**
	 * Parent / owning object
	 */
	private Object parentObject;
	
	/**
	 * Short name, e.g. to use as a title for the field in a dialog
	 */
	private String shortName;
	
	/**
	 * tool tip to use with the field in a dialog
	 */
	private String toolTip;
	
	/**
	 * post title to used with the field in a dialog (e.g. units, coming after a JTextField)
	 */
	private String postTitle;
	

	/**
	 * @param parentObject
	 * @param field
	 */
	public PamParameterData(Object parentObject, Field field) {
		super();
		this.parentObject = parentObject;
		this.field = field;
	}

	/**
	 * @param parentObject
	 * @param field
	 * @param shortName
	 * @param toolTip
	 */
	public PamParameterData(Object parentObject, Field field, String shortName, String toolTip) {
		super();
		this.field = field;
		this.shortName = shortName;
		this.toolTip = toolTip;
	}


	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	/**
	 * Set info about a parameter
	 * @param shortName short name, e.g. to use in a dialog
	 * @param postTitle post title, e.g. text coming after a data entry field in a dialog
	 * @param toolTip tool tip to display over the component in a dialog. 
	 */
	public void setInfo(String shortName, String postTitle, String toolTip) {
		this.shortName = shortName;
		this.postTitle = postTitle;
		this.toolTip = toolTip;
	}

	/**
	 * @param toolTip the toolTip to set
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	/**
	 * @return the parentObject
	 */
	public Object getParentObject() {
		return parentObject;
	}

	/**
	 * 
	 * @return The name of the field
	 */
	public String getFieldName() {
		return getField().getName();
	}
	/**
	 * 
	 * @return The field object describing this parameter
	 */
	public Field getField() {
		return field;
	}

	/**
	 * 
	 * @return The data. Primitives will be wrapped as an object. 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public abstract Object getData() throws IllegalArgumentException, IllegalAccessException;
	
	/**
	 * Set the data in the field. 
	 * @param data
	 * @return true if successful, false if null, Exception if data are wrong type
	 */
	public abstract boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException;
	
	
	/**
	 * @return The data class
	 */
	public Class getDataClass() {
		return getField().getType();
	};
	
	/**
	 * A short name for the data. May default to the field name
	 * if it's not been explicitly set. 
	 * @return a short name for the field, suitable for use in dialogs.
	 */
	public String getShortName() {
		return shortName;
	}
	
	/**
	 * 
	 * @return Longer descriptive text for a field. Suitable for use in tooltips. Can be null. 
	 */
	public String getToolTip() {
		return toolTip;
	}

	/**
	 * @return the postTitle
	 */
	public String getPostTitle() {
		return postTitle;
	}

	/**
	 * @param postTitle the postTitle to set
	 */
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	@Override
	public String toString() {
		return String.format("Param %s class %s", getFieldName(), getDataClass());
	}

	
}
