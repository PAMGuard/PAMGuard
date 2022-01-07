package dataPlotsFX.clickPlotFX;

import java.util.ArrayList;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;

/**
 * Menu items for manually classifying selected clicks in a TDGraph. 
 */
public class ClickReclassifyMenuItem implements OverlayMenuItem {
	
	/**
	 * Flag to ensure clicks types can be grouped into sub menu if needs be
	 */
	public static final int CLICK_GROUP = 0;
	
	/**
	 * Reference to the ClickPlotInfoFX
	 */
	private ClickPlotInfoFX clickPlotInfoFX;



	public ClickReclassifyMenuItem(ClickPlotInfoFX clickPlotInfoFX) {
		this.clickPlotInfoFX=clickPlotInfoFX; 
	}

	@Override
	public Tooltip getNodeToolTip() {
		return new Tooltip(
							"Manually change the click type. Note that this will be undone if there\n"
						+ 	"clicks are reclassified etc. For more robust click marking use the Detection\n"
						+ 	"Group module"); 
	}

	@Override
	public Control menuAction(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {
			
		ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>(); 

		int[] codeList = this.clickPlotInfoFX.getClickControl().getClickTypeMasterManager().getCodeList();
		String[] speciesList = this.clickPlotInfoFX.getClickControl().getClickTypeMasterManager().getSpeciesList();
		
		

		//unclassified menu items
		MenuItem item; 
		
		item = new MenuItem("Declassify"); 
		item.setOnAction((action) -> reclassifyAs(foundDataUnits, (byte) 0));
		menuItems.add(item); 
		
		if (codeList!=null) {
			//add an item for all classifications. 
			for (int i=0; i<codeList.length; i++) {
				item = new MenuItem("Classify as " + speciesList[i]); 
				final byte code = (byte) codeList[i];
				item.setOnAction((action) -> reclassifyAs(foundDataUnits, code));
			}
		}
		menuItems.add(item); 
		
//		if (menuItems.size()<=2) {
//			PamVBox vBox = new PamVBox(); 
//			vBox.getChildren().addAll(menuItems); 
//			return vBox; 
//		}
//		else {
			MenuButton menuButton = new MenuButton("Manually classify clicks...");
			menuButton.getItems().addAll(menuItems); 
			menuButton.setPopupSide(Side.RIGHT);
			return menuButton; 
//		}
	}
	
	/**
	 * Reclassify any clicks within a group to the specified type
	 * @param foundDataUnits - the group of data units to reclassify (may not contain clicks); 
	 * @param type
	 */
	private void reclassifyAs(DetectionGroupSummary foundDataUnits, byte clickType) {
		PamDataUnit pamDataUnit; 
		for (int i=0; i<foundDataUnits.getNumDataUnits(); i++) {
			pamDataUnit = foundDataUnits.getDataList().get(i); 
			if (pamDataUnit instanceof ClickDetection) {
				((ClickDetection) pamDataUnit).setClickType((byte) clickType);
				pamDataUnit.getDataUnitFileInformation().setNeedsUpdate(true); //force PG to update data unit when saving binary files. 
			}
		}
		clickPlotInfoFX.getTDGraph().repaint(100); //force a repaint
	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {	
		//the pop up checks to see if ClickDetection data units are in foundDataUnits and only accesses 
		//menu items from ClickPlotInof if there are some. No need to re-check that we have any click 
		//detections in foundDataUnits. 
		
		
		return true;
	}

	@Override
	public int getFlag() {
		return OverlayMenuItem.DATAINFO;
	}

	@Override
	public int getSubMenuGroup() {
		//clicks should be in the same group. 
		return CLICK_GROUP;
	}

}
