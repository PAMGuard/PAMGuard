package PamView;

import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * 
 * Now that there are many menus, there will several instances of each
 * menu item. Each menu item may therefore be added to a MenuItemEnabler 
 * which will ensure that all items are enabled / disabled together.
 * <p>
 * Can also be used to setSelected
 * <p>
 * Reworked with a static list of all MenuItemEnablers so that when menus are
 * removed from the system (which happens a lot !) they can be easily removed 
 * from all the Enablers.  
 * 
 * @author Doug Gillespie
 *
 */
public class MenuItemEnabler {

	private Vector<AbstractButton> menuItemList= null;
	
	private boolean currentEnabled = true, currentSelected = false;
	
	private static Vector<MenuItemEnabler> enablerList = new Vector<MenuItemEnabler>();
	
	/**
	 * Default constructor compiles a list of all
	 * MenuItemEnablers so that they can be cleaned up on mass
	 */
	public MenuItemEnabler() {
		enablerList.add(this);
	}
	
	public MenuItemEnabler(boolean initialState) {
		enablerList.add(this);
		currentEnabled = initialState;
	}
	
	public void removeMenuItemEnabler(){
		enablerList.remove(this);
	}
	
	/**
	 * Call this for a menu that is no longer needed to remove
	 * all it's items from the enabler list. 
	 * @param menu menu to be cleaned from menu enablers. 
	 */
	public static void removeMenuBar(JMenuBar menuBar) {
		if (menuBar == null) {
			return;
		}
		for (int i = 0; i < enablerList.size(); i++) {
			for (int m = 0; m < menuBar.getMenuCount(); m++) {
				enablerList.get(i).removeAllMenuItems(menuBar.getMenu(m));
			}
		}
	}
	
	/**
	 * Iterate through the menu and it's menu items 
	 * and remove every one from the enabler
	 * @param menu
	 */
	synchronized private void removeAllMenuItems(JMenu menu) {
		JMenuItem menuItem;
		int nItems = menu.getMenuComponentCount();
		for (int i = 0; i < nItems; i++) {
			menuItem = menu.getItem(i);
			if (menuItem == null) {
				continue;
			}
			if (JMenu.class.isAssignableFrom(menuItem.getClass())) {
				removeAllMenuItems((JMenu) menuItem);
			}
			
			removeMenuItem(menuItem);
		}
	}
	
	/**
	 * Add a menu item to a menu item enabler. 
	 * <p> 
	 * The menu items selected state and enabled state
	 * will be set immediately. 
	 * @param newItem new menu item to manage. 
	 */
	synchronized public void addMenuItem(AbstractButton newItem) {
		if (menuItemList == null) {
			menuItemList  = new Vector<AbstractButton>();
		}
		newItem.setSelected(currentSelected);
		newItem.setEnabled(currentEnabled);
		menuItemList.add(newItem);
//		System.out.println(String.format("% d items now in menuenabler %s", menuItemList.size(), newItem.getText()));
	}
	
	synchronized public void removeMenuItem(JMenuItem newItem) {
		if (menuItemList == null) {
			return;
		}
		if (menuItemList.remove(newItem)) {
//			System.out.println(newItem.getText() + " removed from enabler list. " + enablerList.size() + " items left in enablerList." );
		}
	}

	synchronized public void removeMenuItem(AbstractButton newItem) {
		if (menuItemList == null) {
			return;
		}
		if (menuItemList.remove(newItem)) {
//			System.out.println(newItem.getText() + " removed from enabler list" + enablerList.size() + " items left in enablerList.");
		}
	}
	
	/**
	 * Enable all menu items in a MenuItemEnabler list
	 * @param enable enable or disable
	 */
	synchronized public void enableItems(boolean enable) {
		currentEnabled = enable;
		if (menuItemList == null) return;
		for (int i = 0; i < menuItemList.size(); i++) {
			menuItemList.get(i).setEnabled(enable);
		}
	}
	

	/**
	 * Select all menu items in a MenuItemEnabler list
	 * @param select select or deselect
	 */
	synchronized public void selectItems(boolean select) {
		currentSelected = select;
		if (menuItemList == null) return;
		for (int i = 0; i < menuItemList.size(); i++) {
			menuItemList.get(i).setSelected(select);
		}
	}

	/**
	 * Returns the currentEnabled state
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return currentEnabled;
	}

	/**
	 * Gets the current selected state for this enabler
	 * @return true if selected
	 */
	public boolean isSelected() {
		return currentSelected;
	}	
	
	/**
	 * @return the menuItemList
	 */
	public Vector<AbstractButton> getMenuItemList() {
		return menuItemList;
	}
}

