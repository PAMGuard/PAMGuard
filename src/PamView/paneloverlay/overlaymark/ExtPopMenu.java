package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Interface for showing a pop up menu on a mouse action within an ExtMouseHandler. Usually pop up menus are simple how 
 * @author Jamie Macaulay
 *
 */
public interface ExtPopMenu {
	
	/**
	 * Show the pop up menu
	 * @param e - the mouse location. 
	 * @param extMouseAdapters
	 * @return true if the pop up menu is shown
	 */
	public boolean showPopupMenu(MouseEvent e, ArrayList<ExtMouseAdapter> extMouseAdapters, Node parentNode);
	
}
