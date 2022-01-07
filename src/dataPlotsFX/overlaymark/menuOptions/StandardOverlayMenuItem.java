package dataPlotsFX.overlaymark.menuOptions;

import PamView.paneloverlay.overlaymark.OverlayMark;
import dataPlotsFX.overlaymark.popUpMenu.TDMenuPane;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import pamViewFX.fxNodes.PamButton;

/**
 * Converts a MenuItem to an OverlayMenuItem. 
 * 
 * This is typically used for MenuItems which have been previously defined in overlay 
 * markers etc. 
 * @author Jamie Macaulay
 *
 */
public class StandardOverlayMenuItem implements OverlayMenuItem  {
	
	private MenuItem menuItem;
	
	/**
	 * The menu node. 
	 */
	private Region menuNode;

	StandardOverlayMenuItem(MenuItem menuItem){
		this.menuItem=menuItem; 
		this.menuNode=createMenuNode(); 
	}
	
	
	/**
	 * Create a menu node. 
	 * @return the menu node r
	 */
	private Region createMenuNode() {
		Region menuNode = null; 
		if (menuItem.getClass()  == Menu.class) {
			MenuButton  choice = new MenuButton (menuItem.getText()); 
			choice.setGraphic(menuItem.getGraphic());
			choice.getItems().addAll(((Menu) menuItem).getItems());
			TDMenuPane.styleButton(choice, OverlayMenuItem.buttonWidthStandard);
			menuNode=choice; 
		}
		else if (menuItem.getClass()  == SeparatorMenuItem.class) {
			menuNode = new Separator(Orientation.HORIZONTAL); 
		}
		else {
			PamButton pamButton =new PamButton(menuItem.getText()); 
			pamButton.setGraphic(menuItem.getGraphic());
			pamButton.setOnAction(menuItem.getOnAction());
			TDMenuPane.styleButton(pamButton, OverlayMenuItem.buttonWidthStandard);
			menuNode=pamButton; 
		}
		
		return menuNode; 
	}


	@Override
	public Tooltip getNodeToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control menuAction(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {
		return (Control) menuNode; 
	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public int getFlag() {
		return OverlayMenuItem.NO_GROUP;
	}
	
	@Override
	public int getSubMenuGroup() {
		return -1;
	}

}
