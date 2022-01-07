package PamView;

import javax.swing.JComponent;

/**
 * Interface for anything that can add menu items to a Swing menu
 * @author dg50
 *
 */
public interface PamMenuParts {

	/**
	 * Add menu items to a menu or popup menu. 
	 * @param parentComponent menu to add to
	 * @return number of items added
	 */
	int addMenuItems(JComponent parentComponent);
	
}
