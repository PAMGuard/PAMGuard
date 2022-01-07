package dataPlotsFX.overlaymark.menuOptions;

import java.util.ArrayList;
import java.util.List;
import PamView.paneloverlay.overlaymark.OverlayMark;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.overlaymark.menuOptions.MLExport.MLExportOverlayMenu;
import dataPlotsFX.overlaymark.menuOptions.RExport.RExportOverlayMenu;
import dataPlotsFX.overlaymark.menuOptions.wavExport.WavExportOverlayMenu;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

/**
 * Manages all menu items which are specific to the TDDisplay. 
 * @author Jamie Macaulay	
 *
 */
public class OverlayMenuManager {

	/**
	 * The overlay menu items
	 */
	ArrayList<OverlayMenuItem> tdOverlayMenuItems;


	/**
	 * Reference to the TDGraphFX
	 */
	private TDPlotPane tdPlotPane; 


	public OverlayMenuManager(TDPlotPane tdPlotPane){
		this.tdPlotPane= tdPlotPane;

		//add new overlay menu options here
		tdOverlayMenuItems= new ArrayList<OverlayMenuItem>();

		//Export menu items 
		tdOverlayMenuItems.add(new MLExportOverlayMenu());
		tdOverlayMenuItems.add(new WavExportOverlayMenu());
		tdOverlayMenuItems.add(new RExportOverlayMenu());

		//copy figure export.


	}

	/**
	 * Get the menu nodes for the the TDdisplay. 
	 * @param foundDataUnits - the currently selected data units
	 * @return all nodes for the menu. 
	 */
	public ArrayList<OverlayMenuItem> getMenuNodes(DetectionGroupSummary foundDataUnits, OverlayMark mark, MouseEvent e) {

		ArrayList<OverlayMenuItem> tdMenuItems = new ArrayList<OverlayMenuItem>(); 


		// add menu items which  are more generic. 
		OverlayMenuItem overlayMenuItem; 
		List<MenuItem> listMenuItems = tdPlotPane.getPopUpMenuItems(e);

		if (listMenuItems!=null) {
			for (int i=0; i<tdPlotPane.getPopUpMenuItems(e).size(); i++){
				overlayMenuItem= new StandardOverlayMenuItem(listMenuItems.get(i)); 
				tdMenuItems.add(overlayMenuItem); 
			}
		}
		else {
			//			System.out.println("OverlayMenuManager: There are no menu items to add. ");
		}

		if (foundDataUnits!=null) {
			//find which data plots are things
			//		Stream<PamDataBlock> dataBlockStream = foundDataUnits.getUsedDataBlocks().stream();
			//add menu items which are specific to the current data infos
			TDDataInfoFX adataInfo; 
			for (int i = 0; i<tdPlotPane.getTDGraph().getDataList().size(); i++) {
				adataInfo = tdPlotPane.getTDGraph().getDataList().get(i);
				if (foundDataUnits.getUsedDataBlocks().contains(adataInfo.getDataBlock())){
					//add the menu items
					if (adataInfo.getMenuNodes()!=null) {
						tdMenuItems.addAll(adataInfo.getMenuNodes()); 
					}
				}

			}
		}
		
		if (foundDataUnits!=null) {
			//add menu items which are specific to the TD display 
			for (int i=0; i<tdOverlayMenuItems.size(); i++){
				tdMenuItems.add(tdOverlayMenuItems.get(i)); 
				//if the node can't be used disable it. This shows users it is there but prevents confusion
				//on it being used. 
				tdMenuItems.get(tdMenuItems.size()-1).menuAction(foundDataUnits, -1, mark).setDisable(!tdOverlayMenuItems.get(i).canBeUsed(foundDataUnits, -1, mark));
			}
		}

		return tdMenuItems;
	}








}
