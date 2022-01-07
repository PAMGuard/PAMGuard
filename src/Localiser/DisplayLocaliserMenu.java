package Localiser;

import javax.swing.JPopupMenu;

import PamguardMVC.PamDataUnit;

/**
 * This is a simple interface which should be implemented in the controlled unit of a localiser. It allows a menu item to be added to any display which selects a detection. A good example is the click detector, where a menu item is displayed if either the static or 
 * target motion localiser can localise a selected detection. Note that at the moment care should be taken when passing a PamDataUnit
 * to this interface to ensure that information exists within the PamDataUnit to actually allow localisation
 * @author Jamie Macaulay
 *
 */
public interface DisplayLocaliserMenu {
	
	public void addLocaliserMenuItem(JPopupMenu menu, PamDataUnit selectedDetion);
		
	
}
