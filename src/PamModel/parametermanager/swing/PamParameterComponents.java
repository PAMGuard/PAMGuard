package PamModel.parametermanager.swing;

import javax.swing.JComponent;

public interface PamParameterComponents {

	/**
	 * Get a set of three swing components which can be added to a dialog. These
	 * will be a title, a field and a post title. note that the two titles may be
	 * null if there is no text to display, but the main data entry field will 
	 * always not be null and will always be component[1]. 
	 * @return array of swing components. 
	 */
	public JComponent[] getAllComponents();
	
	/**
	 * Set the text in the data field from the parameter value. 
	 * @return true if the text was set correctly
	 */
	public boolean setField();
	
	/**
	 * Get the text in the field and use it to set the parameter value
	 * @return true if the text was read correctly and set as a parameter 
	 */
	public boolean getField();
	
}
