package dataPlotsFX.overlaymark.menuOptions;

import PamView.paneloverlay.overlaymark.OverlayMark;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

/**
 * Interface for menu items which are TD Display specific. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface OverlayMenuItem {
	
	public static final int buttonWidthStandard=200;
	
	/**
	 * 
	 */
	public static final int NO_GROUP=0; 

	/*
	 * Flag for menu items used to export data. 
	 */
	public static final int EXPORT_GROUP=1;

	/**
	 * Flag for menu items which are form Data Infos
	 */
	public static final int DATAINFO = 2; 
	

	/*
	 * Get the tool tip for the node. 
	 */
	public Tooltip getNodeToolTip(); 
	
	/**
	 * Returns a control which performs an action when pressed (or action occurs)
	 * @param foundDataUnits all the selected data units
	 * @param index - the currently selected data unit
	 * @param overlayMark - the overlay mark. Can be null if a data unit is individually selected 
	 */
	public Control menuAction(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark);
	
	/**
	 * Check whether the menu item can be used within a group of data units.  
	 * @param index:. If the index is <0 then returns whether the menu item can be used for at least one of the units in the list. 
	 * @param foundDataUnits - class which holds a list of data units and metadata info. 
	 * @param overlayMark - holds info on the current mark drawn on the display. 
	 */
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark);
	
	/**
	 * Flags for general grouping menu items
	 * @return integer flag which groups items
	 */
	public int getFlag();
	
	/**
	 * Indicates whether menu items should be grouped together in a sub menu. Make the same group and 
	 * same flag if so. 
	 * @return the group number. -1 for no grouping. 
	 */
	public int getSubMenuGroup(); 

}
