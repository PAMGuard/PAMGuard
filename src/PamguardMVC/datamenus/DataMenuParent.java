package PamguardMVC.datamenus;

/**
 * No real idea what this is for, but it's going to get passed to DataBlocks along with a list
 * of selected data units, so that they can return data unit bespoke menu items 
 * @author dg50
 *
 */
public interface DataMenuParent {

	public String getDisplayName();

}
