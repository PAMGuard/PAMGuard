package PamView.paneloverlay.overlaymark;

import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.HoverData;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;
import PamguardMVC.PamDataUnit;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.geometry.Point3D;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Receive mouse actions made on the map, turn them into regions
 * displayed as an overlay on the map and send the region information 
 * off to any observers who might be interested in those data. <p>
 * In some ways this class behaves in a very similar manner to the 
 * spectrogram mark observer system. 
 * @author Doug Gillespie 
 *
 */
abstract public class OverlayMarker extends ExtMouseAdapter implements MarkManager {

	private ArrayList<OverlayMarkObserver> overlayMarkObservers = new ArrayList<>();

	private boolean nowMarking = false;

	private GeneralProjector<PamCoordinate> projector;

	private boolean allowRectangles = true;

	private boolean allowPolygons = true;

	/**
	 * @return the projector
	 */
	public GeneralProjector<PamCoordinate> getProjector() {
		return projector;
	}

	private String markMsg = "Double or right click to cancel mark";
	private PamWarning markWarning = new PamWarning("Mark Drawing", markMsg, 1);

	private OverlayMark currentMark = null;

	private Point3D markStartXY;

	private Object markSource;

	private int markChannels;

	private boolean mouseIsDown;

	private static final double MAXFINDDISTANCE = 15;

	public OverlayMarker(Object markSource, int markChannels, GeneralProjector projector){
		this.markSource = markSource;
		this.markChannels = markChannels;

		this.projector = projector;
	}

	abstract public String getMarkerName();

	/*
	 * Can we make a mark ? This is a default behaviour - ctrl is down 
	 * and there is at least one observer, but this coul dbe overridden. 
	 */
	public boolean isCanMark(MouseEvent e) {
		if (!e.isControlDown()) return false;
		if (getObserverCount() == 0) return false;
		return true;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		mouseIsDown = true;
		
		//System.out.println("OverlayMarker: Mouse Pressed: Current mark: " + this.currentMark + " pop-up: " + e.isPopupTrigger() + " class: " + this); 

		if (e.isPopupTrigger()) {
			//System.out.println("OverlayMark: Mouse pressed: show SWING pop up menu:"); 
			if (showObserverPopups(e)) {
				return true;
			}
			if (showNoObserverPopup(e)) {
				return true;
			};
		}

		if (!isCanMark(e)) return false;

		if (e.getButton() == MouseButton.SECONDARY && canDestroyMark(e)) {
			destroyCurrentMark(e);
			return true;
		}

		if (e.getButton() != MouseButton.PRIMARY) {
			return false;
		}
		
		PamCoordinate newCoord = getCoordinate(e);
		if (newCoord==null) return false;	// if we can't get the position, return null

		if (nowMarking) return true;

		nowMarking = true;
		MarkExtraInfo markInfo = findExtraInfo(e.getSource());

		int chans = markChannels;
		if (markInfo != null && markInfo.getChannelMap() != null) {
			markChannels = markInfo.getChannelMap();
		}

		currentMark = new OverlayMark(this, markSource, markInfo, markChannels, 
				projector.getParameterTypes(), projector.getParameterUnits(), newCoord);
		
		//can;t have polygons starting all over the place or else a nightmare. m
		if (e.getClickCount()>=2 || e.isControlDown()){
			//System.out.println("Begin a polygon mark!");
			this.getCurrentMark().setMarkType(OverlayMarkType.POLYGON);
		}
		else this.getCurrentMark().setMarkType(OverlayMarkType.RECTANGLE);

		updateObservers(OverlayMarkObserver.MARK_START, e, currentMark);
		WarningSystem.getWarningSystem().addWarning(markWarning);
		setMarkSTartXY(new Point3D(e.getX(), e.getY(), 0));

		return nowMarking;
	}

	/**
	 * See if the display making the mark has any extra information. 
	 * @param mouseSource
	 * @return
	 */
	protected MarkExtraInfo findExtraInfo(Object mouseSource) {
		if (mouseSource == null) {
			return null;
		}
		if (MarkExtraInfoSource.class.isAssignableFrom(mouseSource.getClass()) == false) {
			return null;
		}
		return ((MarkExtraInfoSource) mouseSource).getExtraInfo();
	}


	@Override
	public boolean mouseReleased(MouseEvent e) {
		boolean wasMarking = nowMarking;
		mouseIsDown = false;
		
		//System.out.println("OverlayMarker: Mouse released!: " + e.isPopupTrigger()); 

		if (e.isPopupTrigger()) {
//			Debug.out.println("OverlayMark: Mouse released!!!"); 
			//System.out.println("OverlayMark: Mouse released: show SWING pop up menu:"); 
			if (showObserverPopups(e)) {
				return true;
			}
			if (showNoObserverPopup(e)) {
				return true;
			};
		}
		if (getCurrentMark() == null) {
			return false;
		}
		if (getCurrentMark().getMarkType() == OverlayMarkType.RECTANGLE && isNowMarking()) {
			completeMark(e);
		}
		return wasMarking;
	}
	
	/**
	 * An optional additional popup that can occur even if there were no observers of the popup
	 * This get's used on displays that are starting to  label
	 * data units using generic annotations, thereby avoiding the need for more advanced
	 * marks and groupers. 
	 * @param e Mouse event (needed for popup coordinates)
	 * @return true if a menu was shown. 
	 */
	public boolean showNoObserverPopup(MouseEvent e) {
		return false;
	}

	private Object getSynchObject() {
		if (projector != null) {
			return projector;
		}
		else if (markSource != null) {
			return markSource;
		}
		else {
			return this;
		}
	}
	/**
	 * Create the JPopupMenu. this is a composite menu made up 
	 * from all observers of a particular marker. 
	 * @return the jpop up menu with overlay options. 
	 */
	public JPopupMenu createJPopMenu(MouseEvent e){
		
		//got rid of this as it wasn't allowing single detections to be generate a pop up menu
//		if (isMarkComplete() == false) {
//			return null;
//		}
				
		ClosestDataInfo closestDataInfo = null;
		ArrayList<JPopupMenu> doubleMenuList = new ArrayList<>();
		synchronized (getSynchObject()) {
			if (getCurrentMark() == null) {
				closestDataInfo = findClosestData(e, MAXFINDDISTANCE);
			}
			for (OverlayMarkObserver obs:overlayMarkObservers) {
				JPopupMenu obsMenu = getObserverPopup(obs, getCurrentMark(), e, closestDataInfo);
				if (obsMenu != null) {
					doubleMenuList.add(obsMenu);
				}
			}
		}
		if (doubleMenuList.size() == 0) {
			return null;
		}
		JPopupMenu menuToShow = null;
		if (doubleMenuList.size() == 1) {
			menuToShow = doubleMenuList.get(0);
		}
		else {
			menuToShow = new JPopupMenu();
			for (JPopupMenu oldSub:doubleMenuList) {
				JMenu newSub = new JMenu(oldSub.getLabel());
				newSub.setToolTipText(oldSub.getToolTipText());
				while(oldSub.getComponentCount() > 0) {
					/*
					 * Bit weird - but as components get added to newSub, they 
					 * get removed from oldSub, so we're always picking off the 0th.
					 */
					newSub.add(oldSub.getComponent(0));
				}
				//				for (int i = 0; i < oldSub.getComponentCount(); i++) {
				//					newSub.add(oldSub.getComponent(i));
				//				}
				menuToShow.add(newSub);
			}
		}		
		return menuToShow;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.ExtMouseAdapter#getPopupMenuItems(javafx.scene.input.MouseEvent)
	 */
	@Override
	public List<MenuItem> getPopupMenuItems(MouseEvent e) {
		JPopupMenu swingMenu = this.createJPopMenu(e);
		if (swingMenu == null || swingMenu.getComponentCount() == 0) {
			return null;
		}
		// otherwise return a list if menu items converted to FX versions !!!!!
//		ArrayList<MenuItemInfo> fxMenu = PamUtilsFX.jPopMenu2FX(swingMenu);
		// no idea why J put them into MenuItemInfos - take them out again !
		// probably to handle sub menus ... 
		return PamUtilsFX.getSwingMenuItems(swingMenu);
	}

	/**
	 * Show any popup menus form the mark observers in response to the 
	 * mouse going down with a right click.<p>
	 * If a mark has been made, and is in existence, then  
	 * @param e Mouse event
	 * @return true if a menu was shown. 
	 */
	public boolean showObserverPopups(MouseEvent e) {
		
		//System.out.println("OverlayMark: ShowObserverPopUps ---"); 
		
		JPopupMenu menuToShow = createJPopMenu(e);
		
		if (menuToShow==null) return false;
		
		Point mousePoint = OverlayMark.getSwingComponentMousePos(PamController.getMainFrame(), e);
		
		menuToShow.show(PamController.getMainFrame(), mousePoint.x, mousePoint.y);

		return true;
	}

	/**
	 * Get the appropriate popup menu for a single observer. 
	 * @param e
	 * @param obs
	 * @return 
	 */
	private JPopupMenu getObserverPopup(OverlayMarkObserver obs, OverlayMark overlayMark, MouseEvent mouseEvent, ClosestDataInfo closestDataInfo) {
		
		List<PamDataUnit> markedDataUnits = null;
		if (overlayMark != null) {
			markedDataUnits = getSelectedMarkedDataUnits(overlayMark, obs.getMarkDataSelector(this));
		}
		else if (closestDataInfo != null) {
			markedDataUnits = new ArrayList<>();
			markedDataUnits.add(closestDataInfo.dataUnit);
		}

		DetectionGroupSummary dgs = new DetectionGroupSummary(mouseEvent, this, overlayMark, markedDataUnits);
		
		return obs.getPopupMenuItems(dgs);
	}
	




	/**
	 * Find the closest data unit to the mouse point. 
	 * @param e Mouse informatoin
	 * @param maxDistance Maximum distance to consider. 
	 * @return
	 */
	public ClosestDataInfo findClosestData(MouseEvent e, double maxDistance) {
		ClosestDataInfo cdi = new ClosestDataInfo(null, maxDistance);
		if (projector == null) {
			return null;
		}
		synchronized (getSynchObject()) {
			List<HoverData> dataList = projector.getHoverDataList();
			double x = e.getX();
			double y = e.getY();
			for (HoverData hd:dataList) {
				double r = hd.distFromCentre(x, y);
				if (r < cdi.distance) {
					cdi.distance = r;
					cdi.dataUnit = hd.getDataUnit();
				}
			}
		}
		if (cdi.dataUnit == null) {
			return null;
		}
		else {
			return cdi;
		}
	}
	/**
	 * Destroy the current mark. 
	 */
	public void destroyCurrentMark(MouseEvent e){
		//System.out.println("Overlay Marker: destroy mark" );
		currentMark = null;
		nowMarking = false;
		WarningSystem.getWarningSystem().removeWarning(markWarning);
		updateObservers(OverlayMarkObserver.MARK_CANCELLED, e, null);
	}

	/* (non-Javadoc)
	 * @see Map.marks.ExtMapMouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public boolean mouseClicked(MouseEvent e) {

		//might need to destroy a mark 
		if (e.getClickCount() == 2 && currentMark != null) {
			destroyCurrentMark(e);
			return true; // reutrn true to show that the mouse actin has done something
		}
		//		if (e.getButton() == MouseButton.SECONDARY && canDestroyMark(e)) {
		////			System.out.println("Mouse Clicked: Destroy");
		//			destroyCurrentMark(e);
		//			return true;
		//		}

		if (currentMark == null || this.isNowMarking()==false) return false;

		if (currentMark.getMarkType() == OverlayMarkType.RECTANGLE) {
			return false;
		}

		//only want to start a polygon on a double click!	
		PamCoordinate newCoord = getCoordinate(e);
		if (newCoord==null) return false;	// if we can't get the position, return null
		if (currentMark.size() >= 1) {
			double dist = coordinateDistance(newCoord, currentMark.getLastCoordinate());
			if (dist == 0){
				return true;
			}

			// see if we're back to the start ...
			double dist2Start = startDistance(e.getX(), e.getY());
			//			System.out.printf("Point %d distance from polygon start = %3.1f pixels\n", currentMark.size()+1, dist2Start);
			if (dist2Start < 10 && isNowMarking()) {
				completeMark(e);
				return true;
			}
		}

		currentMark.addCoordinate(getCoordinate(e));
		updateObservers(OverlayMarkObserver.MARK_UPDATE, e, currentMark);
		return true;

	}

	/**
	 * 
	 * @return true if whatever is going on makes it desirable to 
	 * pause display scrolling. 
	 */
	public boolean needPaused() {
		return nowMarking || mouseIsDown;
	}

	/**
	 * Check whether a current mark can be destroyed. 
	 * @param e - the mouse event
	 * @return true if mark can be destroyed. False otherwise. 
	 */
	public boolean canDestroyMark(MouseEvent e){
		//don't return true if there is no mark as this blocks other functions
		if (this.isNowMarking()==false) return false;
		//check whether the mouse is inside the mark 
		if (this.isMouseContained(e)) return false;
		//check whether the mark is over a data unit 
		return true;
	}

	private double startDistance(double x, double y) {
		return Math.sqrt(Math.pow(x-markStartXY.getX(), 2) + Math.pow(y-markStartXY.getY(), 2));
	}

	protected void updateObservers(int markStatus, MouseEvent mouseEvent, OverlayMark overlayMark) {
		notifyObservers(markStatus, mouseEvent, this, overlayMark);
	}

	public void addObserver(OverlayMarkObserver observer) {
		if (overlayMarkObservers.contains(observer)) {
			return;
		}
		overlayMarkObservers.add(observer);
	}

	public void removeObserver(OverlayMarkObserver observer) {
		overlayMarkObservers.remove(observer);
	}

	/**
	 * Get the number of observers.
	 * @return The number of things observing this marker. 
	 */
	public int getObserverCount() {
		return overlayMarkObservers.size();
	}
	
	/**
	 * Get the list of observers. 
	 * @return the observers
	 */
	public ArrayList<OverlayMarkObserver> getObservers() {
		return overlayMarkObservers;
	}


	public boolean notifyObservers(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
		boolean consumed = false;
		for (OverlayMarkObserver obs:overlayMarkObservers) {
			consumed |= obs.markUpdate(markStatus, mouseEvent, overlayMarker, overlayMark);
		}
		return consumed;
	}

	private double coordinateDistance(PamCoordinate c1, PamCoordinate c2) {
		int n = Math.min(c1.getNumCoordinates(), c2.getNumCoordinates());
		double r2 = 0;
		for (int i = 0; i < n; i++) {
			r2 += Math.pow(c2.getCoordinate(i)-c1.getCoordinate(i), 2);
		}
		return Math.sqrt(r2);
	}

	/* (non-Javadoc)
	 * @see Map.marks.ExtMapMouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public boolean mouseMoved(MouseEvent e) {
		if (isNowMarking() == false) {
			return false;
		}
		PamCoordinate newCoord = getCoordinate(e);
		if (newCoord==null) return false;	// if we can't get the position, return null
		if (currentMark != null) {
			currentMark.setCurrentMouseCoordinate(newCoord);
			updateObservers(OverlayMarkObserver.MARK_UPDATE, e, currentMark);
			return true;
		}
		return false;
	}

	public boolean isMarkComplete() {
		return currentMark != null && nowMarking == false;
	}
	/**
	 * Complete the mark. 
	 * @param e - mouse event
	 */
	protected boolean completeMark(MouseEvent e) {
		nowMarking = false;
		WarningSystem.getWarningSystem().removeWarning(markWarning);
		if (currentMark == null || currentMark.getCoordinates().size() <= 1) {
			return false;
		}
		updateObservers(OverlayMarkObserver.MARK_END, e, currentMark);

		MarkRelationships links = MarkRelationships.getInstance();
		MarkRelationshipsData params = links.getMarkRelationshipsData();
		
		if (params.isImmediateMenus()) {
			showObserverPopups(e);
		};
		
		return true;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		if (!nowMarking || currentMark == null) return false;
		if (isAllowPolygons() == false) return false;
		PamCoordinate newCoord = getCoordinate(e);
		if (newCoord==null) return false;	// if we can't get the position, return null
		if (currentMark.size() > 1 && currentMark.getMarkType() != OverlayMarkType.RECTANGLE) return true;
		if (currentMark.size() == 1) {
			currentMark.setMarkType(OverlayMarkType.RECTANGLE);
		}
		currentMark.addCoordinate(newCoord);
		updateObservers(OverlayMarkObserver.MARK_UPDATE, e, currentMark);
		return nowMarking;
	}


	@Override
	public OverlayMark getCurrentMark() {
		return currentMark;
	}

	/**
	 * Get a list of marked and selected data units. For now, work off the hover data 
	 * within the projector, but feel free to override or use a totally different 
	 * system of working out what's in the mark - that's the responsibility of 
	 * whatever display has the mark. The MarkDataSelector comes from the 
	 * Mark Observer, i.e. the thing that uses the data. 
	 * 
	 * @param overlayMark
	 * @param markDataSelector
	 * @return
	 */
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector) {
		return getSelectedMarkedDataUnits(overlayMark, markDataSelector, MarkDataSelector.OVERLAP_SOME);
	}
	
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector, int minOverlap) {
		if (overlayMark == null) {
			overlayMark = getCurrentMark();
			if (overlayMark == null) {
				return null;
			}
		}
		MarkDataMatcher markDataMatcher = new MarkDataMatcher(overlayMark, projector);
		List<PamDataUnit> selectedData = new ArrayList<>();
		
		if (projector == null) {
			return selectedData;
		}

		List<HoverData> plottedList = projector.getHoverDataList();

//		Debug.out.printf("OverlayMarker: _1 In getSelectedMarkedDataUnits with %d available units\n", plottedList.size());

		synchronized (getSynchObject()) {
			for (HoverData hoverData:plottedList) {
				int overlap = MarkDataSelector.OVERLAP_NONE;
				
//				Debug.out.println("OverlayMarker: _1a In "  + markDataSelector.wantDataUnit(hoverData.getDataUnit(), overlap));

				if (markDataMatcher.isContained(hoverData)) {
					overlap = MarkDataSelector.OVERLAP_ALL;
				}
				
				else if (markDataMatcher.bearingOverlap(hoverData)) {
					overlap = MarkDataSelector.OVERLAP_SOME;
				}

				if (markDataSelector==null && overlap >= minOverlap) {
					selectedData.add(hoverData.getDataUnit());
				}
				else if (markDataSelector!=null && markDataSelector.wantDataUnit(hoverData.getDataUnit(), overlap)) {
					selectedData.add(hoverData.getDataUnit());
				}
			}
		}
		
//		Debug.out.printf("OverlayMarker: _ 2 In getSelectedMarkedDataUnits with %d available units\n", selectedData.size());

		
		return selectedData;
	}
	//	/**
	//	 * Return a list of data units that are with in a mark. 
	//	 * @param overlayMark
	//	 * @return
	//	 */
	//	public List<PamDataUnit> getMarkedDataUnits(OverlayMark overlayMark, MarkDataMatcher dataMatcher) {
	//		projector.getHoverDataList();
	//		
	//		return null;
	//	}

	/**
	 * Get an x,y coordinate as a lat long. 
	 * @param e Mouse event
	 * @return coordinate. 
	 */
	protected PamCoordinate getCoordinate(MouseEvent e) {
		return projector.getDataPosition(new Coordinate3d(e.getX(), e.getY()));
	}

	/**
	 * 
	 * @param markObserver
	 * @return true if this marker can send marks to this observer. 
	 */
	public boolean canMark(OverlayMarkObserver markObserver) {
		return canMark(markObserver.getRequiredParameterTypes());
	}

	private boolean canMark(ParameterType[] requiredParameterTypes) {
		if (requiredParameterTypes == null) {
			return true;
		}
		if (projector == null) {
			return false;
		}
		ParameterType[] projParams = projector.getParameterTypes();
		if (projParams == null) {
			return true;
		}
		int n = Math.min(requiredParameterTypes.length, projParams.length);
		for (int i = 0; i < requiredParameterTypes.length; i++) {
			if (projParams[i] != requiredParameterTypes[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the allowRectangles
	 */
	protected boolean isAllowRectangles() {
		return allowRectangles;
	}

	/**
	 * @param allowRectangles the allowRectangles to set
	 */
	protected void setAllowRectangles(boolean allowRectangles) {
		this.allowRectangles = allowRectangles;
	}

	/**
	 * @return the allowPolygons
	 */
	protected boolean isAllowPolygons() {
		return allowPolygons;
	}

	/**
	 * @param allowPolygons the allowPolygons to set
	 */
	protected void setAllowPolygons(boolean allowPolygons) {
		this.allowPolygons = allowPolygons;
	}

	/**
	 * @return the markSource
	 */
	protected Object getMarkSource() {
		return markSource;
	}

	/**
	 * @param markSource the markSource to set
	 */
	protected void setMarkSource(Object markSource) {
		this.markSource = markSource;
	}

	/**
	 * @return the markChannels
	 */
	protected int getMarkChannels() {
		return markChannels;
	}

	/**
	 * @param markChannels the markChannels to set
	 */
	protected void setMarkChannels(int markChannels) {
		this.markChannels = markChannels;
	}

	/**
	 * Now marking
	 * @return true if now marking.
	 */
	public boolean isNowMarking() {
		return nowMarking;
	}

	/**
	 * 
	 * @param projector the projector to set
	 */
	public void setProjector(GeneralProjector<PamCoordinate> projector) {
		this.projector = projector;
	}

	/**
	 * Update the marked display. This can be called after the mark has been used
	 * to repaint the owner display. Forcing a repaint will generally make 
	 * the mark disappear and may also recolour any changed dataunits. 
	 */
	public void updateMarkedDisplay() {

	}

	/**
	 * Check whether a mouse event is contained within the current mark shape 
	 * @param e - the mouse event
	 * @return true if the mouse event is cointained within the shape. 
	 */
	public boolean isMouseContained(MouseEvent e){
		if (this.getCurrentMark() == null) return false; 
		Shape shape = this.getCurrentMark().getMarkShape(this.projector);
		if (shape == null) return false; 
		return shape.contains(e.getX(), e.getY());
	}

	/**
	 * Set if marking
	 * @return true if now marking.
	 */
	public void setNowMarking(boolean nowMarking) {
		this.nowMarking=nowMarking; 
	}

	/**
	 * Set the current mark.
	 * @param currentMark - set the current mark
	 */
	public void setCurrentMark(OverlayMark currentMark) {
		this.currentMark = currentMark;
	}

	protected void setMarkSTartXY(Point3D point3d) {
		this.markStartXY=point3d; 

	}

	/**
	 * Get the current mark warning. Can be null.
	 * @return the current mark warning. 
	 */
	public PamWarning getMarkWarning() {
		return this.markWarning;
	}







}
