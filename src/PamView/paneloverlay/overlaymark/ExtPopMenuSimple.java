package PamView.paneloverlay.overlaymark;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import PamController.PamController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Simple implementation of a JavaFX pop up menu for a an ExtMapMouseHandler. 
 * 
 * @author Jamie Macaulay, Doug Gillespie
 *
 */
public class ExtPopMenuSimple implements ExtPopMenu {
	
	/**
	 * Show a master menu at the current mouse position. 
	 * @param e
	 * @return
	 */
	@Override
	public boolean showPopupMenu(MouseEvent e, ArrayList<ExtMouseAdapter> extMouseAdapters, Node parentNode) {
			
		ContextMenu contextMenu = null;
		for (ExtMouseAdapter ma:extMouseAdapters) {
			List<MenuItem> adapMenuItems = ma.getPopupMenuItems(e);
//			System.out.println("ExtMapMouseHandler: adpater " + ma.getPopupMenuItems(e).size());
			if (adapMenuItems != null) {
				if (contextMenu == null) {
					contextMenu = new ContextMenu();
				}
				else if (contextMenu.getItems().size()>0) {
					// add a separator but only if therre are actually some menu items
					contextMenu.getItems().add(new SeparatorMenuItem());
				}
				contextMenu.getItems().addAll(adapMenuItems);
			}
		}
		if (contextMenu != null && contextMenu.getItems().size()>0) {
//			contextMenu.setAnchorX(e.getSceneX());
//			contextMenu.setAnchorY(e.getScreenY());
			if (parentNode != null) {
				contextMenu.show(parentNode, e.getScreenX(), e.getScreenY());
			}
			else {
				// have to use a swing menu which can operate without a parent. 
				Object source = e.getSource();
				Component awtComponent = null;
				int x = (int) e.getScreenX();
				int y = (int) e.getScreenY();
				if (source != null && Component.class.isAssignableFrom(source.getClass())) {
					awtComponent = (Component) source;
				}
				else {
					awtComponent = PamController.getMainFrame();
				}
				if (awtComponent != null) {
					x -= awtComponent.getLocationOnScreen().x;
					y -= awtComponent.getLocationOnScreen().y;
				}
				// need to do a quick double rethreading here - to convert the many in FX, then 
				// display it in Swing - this is getting ridiculous !
				final ContextMenu fxMenu = contextMenu;
				final Component awtComp = awtComponent;
				final int menuX, menuY;
				menuX = x;
				menuY = y;
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JPopupMenu popMenu = PamUtilsFX.contextMenuToSwing(fxMenu);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								popMenu.show(awtComp, menuX, menuY);
							}
						});
					}
				});
			}
			return true;
		}
		else {
			return false;
		}
	}

}
