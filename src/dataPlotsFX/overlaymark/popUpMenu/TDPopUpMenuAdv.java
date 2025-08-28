package dataPlotsFX.overlaymark.popUpMenu;

import java.util.ArrayList;

import PamController.PamGUIManager;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.ExtPopMenu;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import pamViewFX.fxNodes.popOver.PamPopOver;

/**
 * A more complex pop up menu for the display. This shows a preview of data units as well as containing options to 
 * export data and all other menu items.
 * 
 * @author Jamie Macaulay
 *
 */
public class TDPopUpMenuAdv implements ExtPopMenu {
	
	/**
	 * The pop over node
	 */
	private PamPopOver popOver; 
	
	/**
	 * Shows information on a data unit or selection of data units. 
	 */
	private TDMenuPane dataUnitMenu;

	/**
	 * Reference to the TDGraphFX for the pop up menu. 
	 */
	private TDGraphFX tdGraphFX;

	/**
	 * TDPlotPanel - the plot panel. 
	 */
	private TDPlotPane tdPlotPane;
	
	/**
	 * Constructor for TD plot pane. 
	 * @param tdPlotPane - td plot pane
	 */
	public TDPopUpMenuAdv(TDPlotPane tdPlotPane){
		//pop up menu items 
		this.tdPlotPane=tdPlotPane; 
		this.tdGraphFX=tdPlotPane.getTDGraph(); 
		dataUnitMenu = new TDMenuPane(tdPlotPane); 
		createPopUp(); 
	}

	@Override
	public boolean showPopupMenu(MouseEvent e, ArrayList<ExtMouseAdapter> extMouseAdapters, Node parentNode) {
		//System.out.println("Open adv pop up menu: " + getSelectedDetections() + " " + popOver.isShowing());
		//set the data unit list. 
//		if (!popOver.isShowing() && getSelectedDetections()!=null){
		if (!popOver.isShowing() && getSelectedDetections()!=null){
			dataUnitMenu.setDataUnitList(getSelectedDetections(), getSelectedDetections().getOverlayMarker(), e);
		}
		else {
			dataUnitMenu.setDataUnitList(null, null, e); 
		}
		
		if (PamGUIManager.isFX()) {
			//TODO - need to shift by the distance to arrow- not arbitrary 40 pixels
			popOver.show(tdGraphFX.getScene().getWindow(), e.getScreenX(), e.getScreenY()-40); //FX
		}
		else {
			popOver.show(tdGraphFX, e.getScreenX(), e.getScreenY()); //Swing 
		}
		
		return true; 
	}
	
//	/**
//	 * Override this to get a new pop up menu. 
//	 */
//	
//	public boolean showObserverPopups(MouseEvent e) {
//	//	if (e.getButton() == MouseButton.SECONDARY && isMouseContained(e)){
//			//System.out.println("Mouse Clicked: Show pop over: " +  this.getCurrentMark());
//			if (!popOver.isShowing() && getSelectedDetections()!=null){
//				dataUnitMenu.setDataUnitList(getSelectedDetections(), getSelectedDetections().getOverlayMarker(), e);
//				popOver.show(tdGraphFX, e.getScreenX(), e.getScreenY());
//				return true; 
//			}
////
//			return false; 
//	}
	
	/**
	 * Get the currently selected detection group. 
	 * @return the group which is currently selected 
	 */
	private DetectionGroupSummary getSelectedDetections(){
		return tdGraphFX.getOverlayMarkerManager().getCurrentMarker().getSelectedDetectionGroup(); 
	}
	
	
	/**
	 * Create the pop up menu. 
	 */
	private void createPopUp() {
		popOver = new PamPopOver(dataUnitMenu);
		popOver.setDetachable(true);
		popOver.setCornerRadius(4);
		popOver.setOnShown((windowEvent)->{
			dataUnitMenu.drawDataUnit(); 
		});

		//when detection preview is not showing do not want to allow resizing of the pop up menu. 
		dataUnitMenu.getToggle().selectedProperty().addListener((obsVal, oldVal, newVal)->{
			popOver.setResizeAbility(newVal);
		});
		
	}

	@Override
	public boolean closePopupMenu(MouseEvent e) {
		if (popOver==null || !popOver.isShowing()) {
			return false; 
		}
		else {
			popOver.hide();
			return true;
		}
	}

}
