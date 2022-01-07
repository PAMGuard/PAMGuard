package PamModel;

import javax.swing.JMenu;

/**
 * Used to control menu groups for the main PAMGUARD File/Add Modules ... menu.
 * <p>
 * A Number of these are created in PamModel for specific groups of modules. 
 * @author Douglas Gillespie
 * 
 *
 */
public class ModulesMenuGroup  {

	private JMenu menuItem;
	
	private String menuName;

	public ModulesMenuGroup(String menuName) {
		super();
		// TODO Auto-generated constructor stub
		this.menuName = menuName;
	}

	public void setMenuItem(JMenu menuItem) {
		this.menuItem = menuItem;
	}


	public String getMenuName() {
		return menuName;
	}

	public JMenu getMenuItem() {
		if (menuItem!=null) menuItem.getPopupMenu().setLightWeightPopupEnabled(false);
		return menuItem;
	}


	public void setMenuName(String menuName) {
		this.menuName = menuName;
		menuItem.setName(menuName);
	}

}
