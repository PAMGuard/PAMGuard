package dataPlotsFX.overlaymark;

import java.awt.Point;
import java.util.List;

import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamView.HoverData;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.MarkRelationshipsData;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDGraphFX;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Handle mouse events from an FX display and send them off into the 
 * main panel marking thingamagig. This has the advantage over the TDMarkerStandard
 * because it allows users to select individual data units as well as selecting groups. 
 * @author Doug Gillespie
 *
 */
public class TDMarkerFX extends StandardOverlayMarker  {

	/**
	 * The current detection group. 
	 */
	private DetectionGroupSummary currentDetectionGroup;

	/**
	 * The maximum distance a click can be from a data unit to be selected. 
	 */
	private static double maxMarkDistance=15; 



	public TDMarkerFX(TDGraphFX tdGraphFX) {
		super(tdGraphFX);

	}

	/*
	 * Can we make a mark ? For the display, if the mark button is selected then
	 * a mark can always be drawn. 
	 */
	public boolean isCanMark(MouseEvent e) {
		return true; 
	}


	/**
	 * Override to allow a highlighting of selected data units. 
	 */
	@Override
	public boolean mousePressed(MouseEvent e) {
		boolean bool;

		//System.out.println("Select data unit: " + this.isNowMarking());

		if (!this.isNowMarking() /*&& e.getButton()==MouseButton.PRIMARY*/){

			bool = this.selectDataUnit(e); 
			if (bool){
				this.destroyCurrentMark(e);
				getTdGraphFX().repaintMarks();
				//return true; 
			}
		}
		return super.mousePressed(e); 
	}


	/**
	 * Check whether a mouse event is contained within the current mark shape 
	 * @param e - the mouse event
	 * @return true if the mouse event is contained within the shape. 
	 */
	public boolean isMouseContained(MouseEvent e){
		if (this.getCurrentMark() == null) return true; //if selecting single data units then the mark is null. 
		else return super.isMouseContained(e); 

	}


	@Override
	protected boolean completeMark(MouseEvent e) {

		/**
		 * We need to override this to ensure we grab all marked data units 
		 * in the currentDetectionGroup. Otherwise only the data units which 
		 * pass the observer overly mark data selector will be included. This fine
		 * for the normal pop up menu but the advanced menu shows a preview of all
		 * data units and does not make much sense if only those with mark observers
		 * are shown. 
		 */

		if (super.completeMark(e) == false) {
			return false;
		}

		currentDetectionGroup = new DetectionGroupSummary(e, this, this.getCurrentMark(),
				getSelectedMarkedDataUnits(this.getCurrentMark(), null) ); 

		//		Debug.out.println("TDMarkerFX: Marked data units: " + currentDetectionGroup.getNumDataUnits()); 

		this.setNowMarking(false);

		//		//add selected units to TDgraphFX. 
		//		tdGraphFX.getSelectedDataUnits().clear(); 
		//		tdGraphFX.getSelectedDataUnits().addAll(getSelectedMarkedDataUnits(this.getCurrentMark()));
		tdGraphFX.repaintMarks(); //repaint the marks. 

		this.setNowMarking(false);
		//		System.out.println("TDGraph data units: " + 	tdGraphFX.getSelectedDataUnits().size());

		return true;
	}

	/**
	 * Destroy the current mark. 
	 */
	@Override
	public void destroyCurrentMark(MouseEvent e){
		super.destroyCurrentMark(e);
	}


	///Functions to select a single data unit///

	/**
	 * Select a data unit 
	 * @return true if selected
	 */
	private boolean selectDataUnit(MouseEvent e){

		if (e==null) return false; 

		Point pt= new Point(); 
		pt.x= (int) e.getX();
		pt.y= (int) e.getY();

		//work out the distance to each data unit. 
		FoundDataUnitFX foundDataUnit = findClosestUnit(pt); 


		//System.out.println("TDMarkerFX: Select data unit: Closest data unit: " + foundDataUnit); 


		if (foundDataUnit.dataUnit!=null && foundDataUnit.distance<TDMarkerFX.maxMarkDistance){
			//System.out.println("PamMarkerFX: Found a data unit @ distance: " + foundDataUnit.distance + " "
			//		+ PamCalendar.formatDateTime(foundDataUnit.dataUnit.getTimeMilliseconds()));
			currentDetectionGroup =  new DetectionGroupSummary(e, this, this.getCurrentMark(), foundDataUnit.dataUnit);  
			return true; 
		}
		else{
			//	currentDetectionGroup=null; 
			if (e.getButton()==MouseButton.PRIMARY) {
				//only do this if the primary mouse button. Otherwise we might be right clicking on a markout out area in which
				//all saved markes will be removed and the pop up menu will show nothing. So the user can only use the primary button to select data units.
				currentDetectionGroup =null; 
			}
			foundDataUnit=null; 
			return false; 
		}
	}


	/**
	 * Find the closest data unit to a point on the graph. 
	 * @param pt- point on the graph panel.
	 * @return the closest data unit. 
	 */
	public FoundDataUnitFX findClosestUnit(Point pt){
		double minDist = Integer.MAX_VALUE;
		double dist;
		FoundDataUnitFX foundDataUnit = new FoundDataUnitFX(null, null, FoundDataUnitFX.SINGLE_SELECTION);
		Point dataPoint;

		List<HoverData> it = this.tdGraphProjector.getHoverDataList();
		//loop through all the different types of data that is displayed on the graph
		for (HoverData hoverData:it) {
			dist = hoverData.distFromCentre(pt.getX(), pt.getY()); 
			if (dist < minDist) {
				minDist = dist;
				foundDataUnit.dataUnit = hoverData.getDataUnit();
				foundDataUnit.distance = dist;
				foundDataUnit.dataInfo = tdGraphFX.findDataInfo(hoverData.getDataUnit());
			}
		}
		return foundDataUnit;
	}

	/**
	 * Get the pixel location of a data unit on the graph. 
	 * @param dataInfo
	 * @param dataUnit
	 * @return point of the data unit in pixels- pixels represent pixels on the tdGraph, not the entire screen. 
	 */
	public Point getDataUnitPoint(TDDataInfoFX dataInfo, PamDataUnit dataUnit) {
		Double val = dataInfo.getDataValue(dataUnit);
		if (val == null) {
			return null;
		}
		Coordinate3d pixPos = tdGraphProjector.getCoord3d(dataUnit.getTimeMilliseconds(), val, 0); 

		return pixPos.getXYPoint(); 
	}


	@Override
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector) {
		List<PamDataUnit> markedDataUnits = super.getSelectedMarkedDataUnits(overlayMark, markDataSelector);
		return markedDataUnits; 
	}


	/**
	 * Get the currently selected detections
	 * @return a class containing info on selected detections
	 */
	@Override
	public DetectionGroupSummary getCurrentDetectionGroup() {
		return currentDetectionGroup; 
	}


	//we do not want to show the swing menu because the FX menu is handled by ExtMouseAdapter subclased by ExtMapMouseHandler
	//in TDPlotPane. 
	@Override
	public boolean showObserverPopups(MouseEvent e) {

		//System.out.println("TDMarkerFX: OverlayMark: ShowObserverPopUps ---"); 

		JPopupMenu menuToShow = createJPopMenu(e);

		if (menuToShow==null) return false;

		/***
		 * HACK. showObserverPopups is used throughout OverlayMarker to bring up swing
		 * pop up menus in display. However the TDMarkerFX is just one of a few adapters
		 * which are combined in a TDPlotPane using a ExtMapMouseHandler. The
		 * ExtMapMouseHandler listens for pop up mouse actions, then asks all the
		 * adapters for their pop up menus. These are then converted into an FX menu and
		 * shown on the display. There is therefore no used for showObserverPopups(e)
		 * EXCEPT when immediate menus is selected. In this case the ExtMapMouseHandler
		 * has no completeMark knowledge - it deals solely with the mouse input and
		 * markers. Therefore we must allow pop up swing menus if the immedateMenus is
		 * selected. This is messy and should be improved but will require changes to
		 * many displays so is staying a hack for now.
		 */
		MarkRelationships links = MarkRelationships.getInstance();
		MarkRelationshipsData params = links.getMarkRelationshipsData();

		if (params.isImmediateMenus()) {
			
			List<MenuItem> adapMenuItems = getPopupMenuItems(e);
			ContextMenu contextMenu = new ContextMenu();
			contextMenu.getItems().addAll(adapMenuItems);
			contextMenu.show(tdGraphFX, e.getScreenX(), e.getScreenY());
			
//			Point mousePoint = OverlayMark.getSwingComponentMousePos(PamController.getMainFrame(), e);
//			menuToShow.show(PamController.getMainFrame(), mousePoint.x, mousePoint.y);
		}

		return true;
	}


	//	public List<FoundDataUnitFX> getSelectedMarkedDataUnits(OverlayMark overlayMark) {
	//		 List<FoundDataUnitFX> foundList= new ArrayList<FoundDataUnitFX>(); 
	//		for (PamDataUnit dataUnit: super.getSelectedMarkedDataUnits(overlayMark, null)){
	//			foundList.add(new FoundDataUnitFX(tdGraphFX.findDataInfo(dataUnit), dataUnit, FoundDataUnitFX.MARKED_AREA_DETECTION));
	//		}
	//		return foundList;
	//	}

	@Override
	public void updateMarkedDisplay() {
		tdGraphFX.repaint(0);
	}	

}
