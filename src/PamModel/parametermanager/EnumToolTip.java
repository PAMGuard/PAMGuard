package PamModel.parametermanager;

/**
 * interface which can be added to an enum so that it provides tooltips as well as toString.
 * this can help standardise Comboboxes whereby the toString is used in the box, but the tooltip 
 * displayed elsewhere. 
 */
public interface EnumToolTip {

	public String getToolTip();
	
}
