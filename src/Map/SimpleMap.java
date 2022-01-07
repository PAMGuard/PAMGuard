/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScrollSlider;
import userDisplay.UserDisplayComponent;
import Array.ArrayManager;
import Array.PamArray;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import GPS.ProcessNmeaData;
import Map.gridbaselayer.GridbaseControl;
import Map.hiddenControls.HiddenSlider;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.ClipboardCopier;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamGui;
import PamView.PanelOverlayDraw;
import PamView.dialog.PamRadioButton;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorderPanel;
import PamView.paneloverlay.overlaymark.ExtMapMouseHandler;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkOverlayDraw;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.debug.Debug;

/**
 * Mainly a container for map objects, holding the main MapPanel and the right
 * hand control items. <br>
 * Originally written by Dave McLaren. Modified by Doug Gillespie to incorporate
 * controls onto main panel to increase overall visible size.
 * 
 */
public class SimpleMap extends JPanel implements PamObserver, PamScrollObserver, PamSettings, UserDisplayComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4328052737031247745L;

	MapMouseMotionAdapter mouseMotion = new MapMouseMotionAdapter();

	MapMouseInputAdapter mouseInput = new MapMouseInputAdapter();

	MouseWheelHandler mouseWheel = new MouseWheelHandler();
	
	private ExtMapMouseHandler externalMouseHandler;

	boolean centerOnFirstShipGPS = true;

	ArrayList<PamDataBlock> dataBlocks;

	double shipRotTest = 0.0;

	// GpsData gpsData;

	GPSDataBlock gpsDataBlock;
	
	GPSControl gpsControl;

	MapPanel mapPanel;

	// JPanel mapControlPanel;

	GpsTextDisplay gpsTextPanel;

	DisplayPanZoom panZoom;
	
	private JPanel viewerControls;

	MouseMeasureDisplay mouseMeasureDisplay;

	PamZoomOnMapPanel panZoomOnMap;

	private JPanel controlContiner;

	protected ClipboardCopier clipboardCopier;

	MapController mapController;

	boolean mouseDragging;

	private PamDataUnit mousedDataUnit = null;

	LatLong lastClickedMouseLatLong;

	boolean mouseReleased = false;

	boolean mapCanScroll = true;

	Coordinate3d newCursorPos = new Coordinate3d();

	Coordinate3d oldCursorPos = new Coordinate3d();

	Coordinate3d diffCursorPos = new Coordinate3d();

	Point mouseDownPoint, mouseDragPoint;

	private PamScrollSlider viewerScroller;

	private boolean isMainTab;

	protected MapParameters mapParameters;

	private static int simpleMapIndex = 0;
	private int thisMapIndex = 0;

	MapFileManager mapFileManager = new GebcoMapFile();

	protected MapDetectionsManager mapDetectionsManager;

	private boolean initialisationComplete;

	private JRadioButton viewerShowAll;

	private JRadioButton viewerShowSelection;

	private HiddenSlider hiddenSlider;
	
	private Integer hiddenSliderTime = null;

	private MapOverlayMarker mapMarker;

	private GridbaseControl gridBaseControl;

	// JToolTip mouseToolTip;

	public SimpleMap(MapController mapController, boolean isMainTab, MapPanel mapPanel) {
		this.mapPanel = mapPanel;
		mapPanel.setMapController(mapController);
		mapPanel.setSimpleMapRef(this);
		/*
		 * Start with the main settings - since these are there and are needed
		 * for compatibility. Then make a local copy, and store from this class
		 * so that different maps can have different sets of parameters. 
		 */
		this.mapController = mapController;
		this.isMainTab = isMainTab;
		externalMouseHandler = new ExtMapMouseHandler(PamController.getMainFrame(), false);
		
		mapParameters = new MapParameters();
		thisMapIndex = simpleMapIndex++;
		if (isMainTab == false) {
			initialisationComplete = true;
		}
		PamSettingManager.getInstance().registerSettings(this);
		mapController.addSimpleMap(this);

		mapDetectionsManager = new MapDetectionsManager(this);
		
		gridBaseControl = new GridbaseControl(mapController.getUnitName());
		
		createMapObjects();
		
		initMapPanel();

		mapPanel.setSimpleMapRef(this);

		externalMouseHandler.addMouseHandler(mapMarker = new MapOverlayMarker());
		OverlayMarkProviders.singleInstance().addProvider(mapMarker);		
		mapPanel.addConstantOverlay(new MarkOverlayDraw(mapMarker));

		lastClickedMouseLatLong = new LatLong(0.0, 0.0);

		clipboardCopier = new ClipboardCopier(this, "PAMGUARD Map");
		
		/**
		 * This is required in secondary maps to subscribe obervables. 
		 * Since they get created after InitialisatinComplete, they miss
		 * out on the main opportunity to do this. 
		 */
		mapPanel.updateObservers();
		
		Timer mapTimer = new Timer(1000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				timerActions();
			}
		});
		mapTimer.start();
	}


	public SimpleMap(MapController mapController, boolean isMainTab) {
		this(mapController, isMainTab, new MapPanel(mapController, null));
	}

	public String getUnitName() {
		if (thisMapIndex != 0) {
			return mapController.getUnitName()+thisMapIndex;
		}
		else {
			return mapController.getUnitName();
		}
	}

	/**
	 * Settings manager for when there are multiple maps in user displays. 
	 * @author dg50
	 *
	 */
	@Override
	public String getUnitType() {
		return mapController.getUnitType();
	}

	@Override
	public Serializable getSettingsReference() {
		mapParameters.mapRangeMetres = mapPanel.getMapRangeMetres();
		return mapParameters;
	}

	@Override
	public long getSettingsVersion() {
		return MapParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		mapParameters = ((MapParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}
	
	/**
	 * Get the time for the map (used when deciding what to plot)
	 * @return time from either the pamguard clock or the scroll bar. 
	 */
	public long getMapTime() {
		if (viewerScroller != null) {
			return viewerScroller.getValueMillis();
		}
		else {
			return PamCalendar.getTimeInMillis();
		}
	}

	/**
	 * Fires once a second so map can re-draw, including update of ship position
	 * based on prediction now that GPS is not read out every second.
	 * This is needed since if there are no gps data, the map dispalys the last detections forever
	 * since it's got no incentive to redraw itself. 
	 */
	private void timerActions() {
		// this repaint and other actions are
		// needed to give the ship a smooth look when GPS data are
		// slow to arrive
		mapPanel.repaint(1000);
		gpsTextPanel.updateGpsTextArea();
		gpsTextPanel.displayZoomedorRotated(); // causes it to redisplay
		// information
	}

	/**
	 * ShouldPlot function for a datablock, moved here from mappanel so 
	 * that we can override for a special map for Target motion (and other) dialogs
	 * @param pamDataBlock
	 * @return true if anything in that datablock should be displayed. 
	 */
	public boolean shouldPlot(PamDataBlock pamDataBlock) {
		return mapDetectionsManager.isShouldPlot(pamDataBlock);
	}

	/**
	 * Shouldplot function for individual data units. 
	 * @param pamDataUnit
	 * @param mapDetectionData
	 * @param earliestToPlot
	 * @param now
	 * @param ds
	 * @return
	 */
	public boolean shouldPlot(PamDataUnit pamDataUnit, MapDetectionData mapDetectionData, long earliestToPlot, long now, DataSelector ds) {
		if (mapDetectionData.select == false) {
			return false;
		}
		if (ds != null && ds.scoreData(pamDataUnit) == 0) {
			return false;
		}
		if (mapDetectionData.allAvailable) {
			return true;
		}
		long unitTime = pamDataUnit.getTimeMilliseconds();
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			PamScrollSlider viewerSlider = getViewerScroller();

			if (unitTime != 0) {
				if (unitTime < viewerSlider.getMinimumMillis() ||
						unitTime > viewerSlider.getMaximumMillis()) {
					return false;
				}			
			}
			if (mapParameters.viewerShowAll) {
				return true;
			}
			else {
				
				// if we are plotting data behind the scrollbar (data that has already occurred)...
				if (!mapDetectionData.lookAhead) {
					if (unitTime > now || unitTime < earliestToPlot) {
						return false;
					}
				}
				
				// if we are plotting data ahead of the scrollbar (future data)...
				else {
					long dispIntval = Math.abs(now-earliestToPlot);
					if (unitTime < now || unitTime > now+dispIntval) {
						return false;
					}
				}
				
			}
			return true;
		}
		else { // not viewer !
			//			if (now - pamDataUnit.getLastChangeTime() > mapDetectionData.displaySeconds * 1000) {
			long unitEndTime = Math.max(unitTime, pamDataUnit.getLastChangeTime());
			if (now - unitEndTime > mapDetectionData.getDisplayMilliseconds()) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Create the map objects, but don't necessarily show them.
	 * 
	 */
	private void createMapObjects() {
		this.setOpaque(true);
		setBackground(Color.white);
		// simpleMap = new JPanel();
		gpsTextPanel = new GpsTextDisplay(mapController, this);

		mouseMeasureDisplay = new MouseMeasureDisplay(mapController, this);

		// mapControlPanel = new JPanel();
		//
		// mapControlPanel.setLayout(new GridLayout(0, 1));

		panZoom = new DisplayPanZoom();

		this.setLayout(new BorderLayout());

//		mapPanel = new MapPanel(mapController, this);
		mapPanel.setMapRotationDegrees(45.0);
		mapPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		mapPanel.addMouseListener(mouseMotion);
		mapPanel.addMouseMotionListener(mouseMotion);
		mapPanel.addMouseListener(mouseInput);
		mapPanel.addMouseWheelListener(mouseWheel);

		panZoom.setHandler(new PamZoomOnMapPanel(mapPanel));

		this.add(BorderLayout.CENTER, mapPanel);

		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			viewerScroller = new PamScrollSlider(mapController.getUnitName(), 
					AbstractPamScrollerAWT.HORIZONTAL, 100, 3600L*1L*1000L, true);
			this.add(BorderLayout.SOUTH, viewerScroller.getComponent());
			viewerScroller.addObserver(this);
		}

		mapPanel.setMapRotationDegrees(shipRotTest);

		mapPanel.setMapRotationDegrees(0.0);
		mapPanel.setMapRangeMetres(mapParameters.mapRangeMetres);

		// gpsTextPanel.setPreferredSize(new Dimension(5 * 35 + 10, 5 * 35 +
		// 10));
		// gpsTextPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		gpsTextPanel.setBorder(new CompoundBorder(BorderFactory
				.createRaisedBevelBorder(), new EmptyBorder(5, 5, 5, 5)));

		panZoom.setBorder(new CompoundBorder(BorderFactory
				.createRaisedBevelBorder(), new EmptyBorder(5, 5, 5, 5)));

		// mapControlPanel.add(panZoom);
		// mapControlPanel.add(gpsTextPanel);
		// this.add(BorderLayout.EAST, mapControlPanel);
		controlContiner = new PamBorderPanel();
		controlContiner.setOpaque(false);
		controlContiner.setLayout(new BoxLayout(controlContiner,
				BoxLayout.Y_AXIS));
		controlContiner.add(mouseMeasureDisplay);
		controlContiner.add(panZoom);
		controlContiner.add(gpsTextPanel);
		CornerLayoutContraint c = new CornerLayoutContraint();
		c.anchor = CornerLayoutContraint.LAST_LINE_END;
		mapPanel.add(controlContiner, c);
		
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			hiddenSlider = new HiddenSlider(this);
			hiddenSlider.setMaxTimeSeconds(mapParameters.dataShowTime);
			c.anchor = CornerLayoutContraint.LAST_LINE_START;
			HidingDialogPanel hidingDialogPanel = new HidingDialogPanel(c.anchor, hiddenSlider);
			hidingDialogPanel.setHiddenToolTipText("Map display time options");
			mapPanel.add(hidingDialogPanel.getShowButton(), c);
		}
		
		this.setVisible(true);
		
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			viewerControls = new JPanel();
			viewerControls.setBackground(PamColors.getInstance().getColor(PamColor.MAP));
			viewerControls.setLayout(new BoxLayout(viewerControls, BoxLayout.Y_AXIS));
			viewerControls.setOpaque(false);
			viewerControls.add(viewerShowAll = new PamRadioButton("Display all selected data"));
			viewerShowAll.addActionListener(new ViewerSelListener(true));
			viewerControls.add(viewerShowSelection = new PamRadioButton("Display only at vessel scroll bar time"));
			viewerShowSelection.addActionListener(new ViewerSelListener(false));
			ButtonGroup bg = new ButtonGroup();
			bg.add(viewerShowAll);
			bg.add(viewerShowSelection);
			viewerShowAll.setOpaque(false);
			viewerShowSelection.setOpaque(false);
			viewerShowAll.setToolTipText("Show all selected detection data for the entire map period");
			viewerShowSelection.setToolTipText("<html>Show only data up to the current cursor position. " +
			"<p>To set how much data are shown for each detector, right click and go to 'Plot Overlay Options'");
			
			c.anchor = CornerLayoutContraint.LAST_LINE_START;
			mapPanel.add(viewerControls, c);
		}

		showMapObjects();

	}
	
	class ViewerSelListener implements ActionListener {

		private boolean showAll;

		public ViewerSelListener(boolean b) {
			this.showAll = b;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			mapParameters.viewerShowAll = showAll;
			mapPanel.repaint(100);
		}
		
	}

	/**
	 * 
	 * @return The map parameters object. 
	 */
	public MapParameters getMapParameters() {
		return mapParameters;
	}

	public void showMapObjects() {
		boolean showMouseMeasure = getShowMouseMeasure();
		controlContiner.setVisible(mapParameters.showGpsData
				|| mapParameters.showPanZoom || showMouseMeasure);
		mouseMeasureDisplay.setVisible(showMouseMeasure);
		gpsTextPanel.setVisible(mapParameters.showGpsData);
		panZoom.setVisible(mapParameters.showPanZoom);
	}

	private boolean getShowMouseMeasure() {
		if (mapController.getMouseMoveAction() == MapController.MOUSE_MEASURE
				&& mouseDragPoint != null) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#initMapPanel()
	 */
	public void initMapPanel() {

		gpsDataBlock = findGpsDataBlock();
		if (gpsDataBlock != null && gpsDataBlock.getParentProcess().getClass() == ProcessNmeaData.class) {
			gpsDataBlock.addObserver(this);
			if (mapController.mapProcess != null) {
				mapController.mapProcess.setParentDataBlock(gpsDataBlock);
			}
		}

		initViewerControls();
		
		if (mapFileManager != null) {
			mapFileManager.readFileData(mapParameters.mapFile);
		}
		mapPanel.setMapCentreDegrees(MasterReferencePoint.getLatLong());
		// mapPanel.refreshPlotableDetectorLists();
		mapPanel.updateObservers();
		mapPanel.createKey();
		mapPanel.repaint();

	}
	
	/**
	 * Find the main GPS Data block
	 * @return the first GPS datablock from a GPSController. 
	 */
	GPSDataBlock findGpsDataBlock() {
		gpsControl = findGpsControl();
		if (gpsControl != null) {
			return gpsControl.getGpsDataBlock();
		}
		else {
			return null;
		}
	}
	
	GPSControl findGpsControl() {
		GPSControl gpsControl = null;
		try {
			gpsControl =  (GPSControl) PamController.getInstance().findControlledUnit(GPSControl.gpsUnitType);
		}
		catch (ClassCastException e) {
			return null;
		}
		if (gpsControl != null) {
			return gpsControl;
		}
		else {
			return null;
		}
	}

	private void initViewerControls() {
		if (viewerShowAll == null) {
			return;
		}
		viewerShowAll.setSelected(mapParameters.viewerShowAll);
		viewerShowSelection.setSelected(!mapParameters.viewerShowAll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#getMapPanel()
	 */
	public MapPanel getMapPanel() {
		return (mapPanel);
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		PamDataBlock block = (PamDataBlock) o;
		if (gpsTextPanel == null)
			return;
		if (mapPanel == null)
			return;
		if (block.getUnitClass() == GpsDataUnit.class) {
			newGpsData((GpsDataUnit) arg);
		}
	}	
	
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}

	private void newGpsData(GpsDataUnit newGpsDataUnit) {

		PamArray currentArray = ArrayManager.getArrayManager()
				.getCurrentArray();
		if (currentArray != null
				&& currentArray.getHydrophoneLocator().isStatic()) {
			return;
		}
		GpsData gpsData = newGpsDataUnit.getGpsData();

		LatLong latLong = new LatLong(gpsData.getLatitude(), gpsData
				.getLongitude());

		// 54.0+40/3600.0;
		// latLong.latDegs = 60.0 + 20.0/3600.0;
		// myMP.setMapCentreDegrees(latLong);
		// myMP.setShipLLD(latLong);
		// myMP.setMapRotationDegrees(shipRotTest++);
		if (centerOnFirstShipGPS) {
			mapPanel.setMapCentreDegrees(latLong);
			centerOnFirstShipGPS = false;
		}
		mapPanel.newGpsData(newGpsDataUnit);
//		gpsTextPanel.setLastFix(gpsData);
//		gpsTextPanel.updateGpsTextArea();
		mapPanel.repaint();
//		gpsTextPanel.setPixelsPerMetre(mapPanel.getPixelsPerMetre());
//		gpsTextPanel.newShipGps();
		// gpsTextPanel.setShipPosition(mapPanel.ship.getShipPosition());

	}

	public String getObserverName() {
		return "simple map display component";
	}

	public void noteNewSettings() {

	}

	public void setSampleRate(float sampleRate, boolean notify) {
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		PamDataBlock block = (PamDataBlock) o;
		if (block.getUnitClass() == GpsDataUnit.class) {
			return Math.max(mapParameters.dataKeepTime,
					mapParameters.trackShowTime) * 1000;
		}
		return 0;
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#getDataBlocks()
	 */
	public ArrayList<PamDataBlock> getDataBlocks() {
		return dataBlocks;
	}

	Coordinate3d getShipCoordinate() {
		GpsData shipGps = mapPanel.getShipGpsData(true);
		if (shipGps == null) {
			return null;
		}
		return mapPanel.getRectProj().getCoord3d(shipGps.getLatitude(),
				shipGps.getLongitude(), 0);
	}

	class MapMouseMotionAdapter extends MouseMotionAdapter implements
	MouseListener, MouseMotionListener {

		@Override
		public void mouseMoved(MouseEvent e) {

			if (externalMouseHandler.mouseMoved(e)) {
				return;
			}
			
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			LatLong mouseLL = mapPanel.getRectProj().panel2LL(
					new Coordinate3d(e.getX(), e.getY(), 0.0));
			gpsTextPanel.updateMouseCoords(mouseLL);
			gpsTextPanel.setMouseX(e.getX());
			gpsTextPanel.setMouseY(e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (e.isShiftDown()) {
				mouseRotate(e);
				return;
			}
			if (externalMouseHandler.mouseDragged(e)) {
				repaint(10);
				return;
			}
			
			if (mousedDataUnit != null) {
				repaint(10);
				return;
			}
			switch (mapController.getMouseMoveAction()) {
			case MapController.MOUSE_MEASURE:
				mouseMeasure(e);
				mouseMoved(e);
				break;
			case MapController.MOUSE_PAN:
				mousePan(e);
				break;
			}
		}

		/**
		 * Rotate the display in 1 or 2 dimensions using the mouse. 
		 * @param e
		 */
		private void mouseRotate(MouseEvent e) {
			newCursorPos.x = e.getX();
			newCursorPos.y = e.getY();
			double dx = oldCursorPos.x - newCursorPos.x;
			double dy = oldCursorPos.y - newCursorPos.y;
			MapRectProjector mapProjector = mapPanel.getRectProj();
			double newV = mapProjector.getMapVerticalRotationDegrees()+dy/5;
			newV = Math.max(0, Math.min(135, newV));
			if (mapParameters.allow3D) {
				mapProjector.setMapVerticalRotationDegrees(newV);
				if (e.getY() < mapPanel.getHeight()/2) {
					dx = -dx; // rotate the other way !
				}
				if (Math.cos(Math.toRadians(newV)) < 0) {
					dx = -dx;
				}
				mapProjector.setMapRotationDegrees(mapProjector.getMapRotationDegrees()+dx/5);
			}
			else {
				/*
				 * Calculate an absolute rotation angle
				 */
				int mx = mapPanel.getWidth()/2;
				int my = mapPanel.getHeight()/2;
				double da = Math.atan2(newCursorPos.y-my, newCursorPos.x-mx) - Math.atan2(oldCursorPos.y-my, oldCursorPos.x-mx);
				mapProjector.setMapRotationDegrees(mapProjector.getMapRotationDegrees()+Math.toDegrees(da));
			}
			oldCursorPos.x = newCursorPos.x;
			oldCursorPos.y = newCursorPos.y;
			mapPanel.repaint(true);
		}

		private void mouseMeasure(MouseEvent e) {

			mouseDragPoint = new Point(e.getX(), e.getY());
			mouseMeasureDisplay.showMouseData(mouseDownPoint, mouseDragPoint);
			mapPanel.repaint(false);

		}

		private void mousePan(MouseEvent e) {

			if (mapCanScroll) {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

				newCursorPos.x = e.getX();
				newCursorPos.y = e.getY();
				diffCursorPos.x = oldCursorPos.x - newCursorPos.x;
				diffCursorPos.y = oldCursorPos.y - newCursorPos.y;

				if (mouseDragging) {
					mapPanel.setMapCentreCoords(new Coordinate3d(mapPanel
							.getWidth()
							/ 2.0 + diffCursorPos.x, mapPanel.getHeight() / 2.0
							+ diffCursorPos.y));
				}
				mouseDragging = true;
				oldCursorPos.x = newCursorPos.x;
				oldCursorPos.y = newCursorPos.y;
				mapPanel.repaint();
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (externalMouseHandler.mouseClicked(e)) {
				return;
			}
			if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
				createMapComment(e.getX(), e.getY());
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			if (externalMouseHandler.mousePressed(e)) {
				return;
			}

			LatLong mouseLL = mapPanel.getRectProj().panel2LL(
					new Coordinate3d(e.getX(), e.getY(), 0.0));

			mouseDownPoint = new Point(e.getX(), e.getY());
			oldCursorPos.x = mouseDownPoint.getX();
			oldCursorPos.y = mouseDownPoint.getY();

			// set this in a global variable, so that other modules can access
			// it
			MapController.setMouseClickLatLong(mouseLL);

			// System.out.println("before mousePressed " + mouseLL.latDegs);
			mapPanel.getRectProj().setLastClickedMouseLatLong(mouseLL);
			// System.out.println("after mousePressed " +
			// myMP.getRectProj().getLastClickedMouseLatLong().latDegs);
			mapPanel.getRectProj().getLastClickedMouseLatLong();

			mouseReleased = false;

			gpsTextPanel.copyMouseMapPositionToClipboard(mouseLL.getLatitude(),
					mouseLL.getLongitude());

			mousedDataUnit = mapPanel.getRectProj().getHoveredDataUnit();
//			System.out.println("Hovered data unit = " + mousedDataUnit);
			if (mousedDataUnit != null) {
				return;
			}

			if (mapController.getMouseMoveAction() == MapController.MOUSE_MEASURE) {
				mapPanel.setCursor(Cursor
						.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				mouseDragPoint = (Point) mouseDownPoint.clone();
				showMapObjects();
			} else {
				mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				maybeShowPopup(e);
			}
			

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (externalMouseHandler.mouseReleased(e)) {
				return;
			}
			mouseDragging = false;
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			mouseDownPoint = mouseDragPoint = null;
			mapPanel.setCursor(Cursor
					.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			if (mapController.getMouseMoveAction() == MapController.MOUSE_MEASURE) {
				showMapObjects();
			}
			mousedDataUnit = null;
			maybeShowPopup(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (externalMouseHandler.mouseEntered(e)) {
				return;
			}

		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (externalMouseHandler.mouseExited(e)) {
				return;
			}

		}
		
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				mapPanel.showPopupMenu(e);
			}
		}

	}

	private class MouseWheelHandler implements MouseWheelListener {

		public void mouseWheelMoved(MouseWheelEvent arg0) {

//			if (arg0.getWheelRotation() < 0
//					& mapPanel.getMapRangeMetres() >= 20) {
//				mapPanel.setMapZoom(0.97f);
//			}
//			if (arg0.getWheelRotation() > 0
//					& mapPanel.getMapRangeMetres() <= 500000000) {
//				mapPanel.setMapZoom(1.03f);
//			}
			mapPanel.stepMapZoom(arg0.getWheelRotation());
			mapPanel.repaint();

		}

	}

	class MapMouseInputAdapter extends MouseInputAdapter {
		@Override
		public void mouseExited(MouseEvent e) {
			gpsTextPanel.mouseExited();
		}

	}

	/**
	 * Gets a data unit currently hovered by the mouse. This only gets set if
	 * the mouse actually hovers and is then clicked.
	 * 
	 * @return hovered data unit.
	 */
	public PamDataUnit getMousedDataUnit() {
		return mousedDataUnit;
	}

	@Override
	protected void paintComponent(Graphics arg0) {
		super.paintComponent(arg0);

		// mapPanel.setPreferredSize(new Dimension(
		// (int) ((double) this.getWidth() * 0.6), (int) ((double) this
		// .getWidth() * 0.6)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#removeObservable(PamguardMVC.PamObservable)
	 */
	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#getLastClickedMouseLatLong()
	 */
	public LatLong getLastClickedMouseLatLong() {
		return lastClickedMouseLatLong;
	}

	protected void createMapComment(int x, int y) {
		LatLong latLong = getLastClickedMouseLatLong().clone();
		long time = PamCalendar.getTimeInMillis();
		String comment = "Test comment";

		MapComment mapComment = new MapComment(time, latLong, comment);

		mapComment = MapCommentDialog.showDialog(mapController.getPamView()
				.getGuiFrame(), mapPanel, new Point(x, y), mapComment);

		if (mapComment != null) {
			mapController.mapProcess.getMapCommentDataBlock().addPamData(
					mapComment);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#getMouseMotion()
	 */
	public MapMouseMotionAdapter getMouseMotion() {
		return mouseMotion;
	}

	public JComponent getPanel() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Map.PamMap#addMouseAdapterToMapPanel(java.awt.event.MouseAdapter)
	 */
	public void addMouseAdapterToMapPanel(MouseAdapter mouseAdapter) {
		mapPanel.removeMouseListener(mouseAdapter);
		mapPanel.removeMouseMotionListener(mouseAdapter);
		mapPanel.addMouseListener(mouseAdapter);
		mapPanel.addMouseMotionListener(mouseAdapter);
	}

	public void mapCanScroll(boolean b) {
		// TODO Auto-generated method stub
		mapCanScroll = b;
	}

	/**
	 * 
	 */
	public void refreshDetectorList() {
		// TODO Auto-generated method stub
		// mapPanel.refreshPlotableDetectorLists();
		// System.out.println(
		// "mapPanel.refreshPlotableDetectorLists(); called from refreshDetectorList"
		// );
	}

	/**
	 * Notification of new viewer times
	 */
	protected void newViewTimes() {
		// need to clear the base drawing
		mapPanel.repaintBaseDrawing();

		newViewTime();
	}

	/**
	 * Notification that the viewer slider has moved.
	 */
	protected void newViewTime() {
		long now = viewerScroller.getValueMillis();
		MasterReferencePoint.setDisplayTime(now);
		if (gpsDataBlock != null) {
			GpsDataUnit gpsDataUnit = gpsDataBlock.getClosestUnitMillis(now);
			mapPanel.newViewTime(gpsDataUnit);
			if (gpsDataUnit != null) {

				newGpsData(gpsDataUnit);

				GpsData gpsData = gpsDataUnit.getGpsData();

				if (centerOnFirstShipGPS) {
					mapPanel.setMapCentreDegrees(gpsData);
					centerOnFirstShipGPS = false;
				}
				
				// TODO Need to send a notification to the gpstextpanel to update it
				// with the closest fix information. 
//				PamCalendar.setViewPosition(gpsData.getTimeInMillis());
//				PamController.getInstance().notifyModelChanged(PamController.);
			}
			
		}

		mapPanel.repaint();
	}


	@Override
	public void scrollRangeChanged(AbstractPamScroller absPamScroller) {
		newViewTimes();
	}

	@Override
	public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
		newViewTime();
	}

	/**
	 * Subscribes a variety of data blocks to the scroll bar. 
	 * @return returns true if the list has changes, indicating
	 * that it's probably necessary to call loadData in the
	 * scroll manager to get new data. 
	 */
	public boolean subscribeViewerBlocks() {
		if (viewerScroller == null) {
			return false;
		}
		boolean changes = false;
		viewerScroller.addDataBlock(gpsDataBlock);
		ArrayList<PamDataBlock> detectorDataBlocks = mapDetectionsManager.plottableBlocks;
		PamDataBlock dataBlock;
		for (int i = 0; i < detectorDataBlocks.size(); i++) {
			dataBlock = detectorDataBlocks.get(i);
			if (mapDetectionsManager.isShouldPlot(dataBlock)) {
//				if (dataBlock.getNumOfflineDataMaps() > 0) {
					if (viewerScroller.isDataBlockUsed(dataBlock) == false) {
						viewerScroller.addDataBlock(dataBlock);
						changes = true;
					}
//				}
			}
			else {
				if (viewerScroller.isDataBlockUsed(dataBlock)) {
					viewerScroller.removeDataBlock(dataBlock);
					changes = true;
				}
			}
		}
		return changes;

	}

	public PamScrollSlider getViewerScroller() {
		return viewerScroller;
	}

	public JMenu createDisplayMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());

		menu.add(getOptionsMenuItem(parentFrame));


		return menu;
	}
	
	public JMenuItem getOptionsMenuItem(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Map options ...");
		menuItem.addActionListener(new menuMapOptions(parentFrame));
		return menuItem;
	}

	class menuMapOptions implements ActionListener {
		Frame parentFrame;

		public menuMapOptions(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			showParametersDialog(parentFrame);
		}
	}
	
	public boolean showParametersDialog(Window parentFrame) {
		MapParameters newParameters = MapParametersDialog.showDialog(parentFrame, this);
		if (newParameters != null) {
			mapParameters = newParameters.clone();
			mapPanel.repaint(true);
			mapPanel.createKey();
			checkViewerData();
			if (hiddenSlider != null) {
				hiddenSlider.setMaxTimeSeconds(mapParameters.dataShowTime);
			}
			return true;
		}
		return false;
	}

	/**
	 * In viewer mode, check the right data are loaded. 
	 */
	protected void checkViewerData() {
		if (mapController.getPamController().getRunMode() == PamController.RUN_PAMVIEW) {
			if (subscribeViewerBlocks()) {
				getViewerScroller().reLoad();
			}
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		mapController.removeSimpleMap(this);
		OverlayMarkProviders.singleInstance().removeProvider(mapMarker);		
	}

	public void notifyModelChanged(int changeType) {
		mapDetectionsManager.notifyModelChanged(changeType);

//		if (initialisationComplete) {
//			initMapPanel();
//		}

//		if (mapController.getPamController().getRunMode() == PamController.RUN_PAMVIEW) {
			switch(changeType) {
			case PamControllerInterface.INITIALIZATION_COMPLETE:
				initialisationComplete = true;
				subscribeViewerBlocks();
				initMapPanel();
				break;
			case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
			case PamControllerInterface.INITIALIZE_LOADDATA:
				if (PamController.getInstance().isInitializationComplete()) {
					checkViewerData();
				}
				break;
			case PamControllerInterface.MASTER_REFERENCE_CHANGED:
				if (gpsTextPanel != null) {
					gpsTextPanel.updateGpsTextArea();
				}
				mapPanel.repaint();
				break;
			case PamControllerInterface.HYDROPHONE_ARRAY_CHANGED:
				mapPanel.repaint();
				break;
			}
//		}
		
	}
	/**
	 * @return the hiddenSliderTime
	 */
	public Integer getHiddenSliderTime() {
		return hiddenSliderTime;
	}

	/**
	 * @param hiddenSliderTime the hiddenSliderTime to set
	 */
	public void setHiddenSliderTime(Integer hiddenSliderTime) {
		this.hiddenSliderTime = hiddenSliderTime;
	}

	/**
	 * @return the gpsDataBlock
	 */
	protected GPSDataBlock getGpsDataBlock() {
		return gpsDataBlock;
	}

	/**
	 * @return the gpsControl
	 */
	protected GPSControl getGpsControl() {
		return gpsControl;
	}

	class MapOverlayMarker extends OverlayMarker {

		public MapOverlayMarker() {
			super(SimpleMap.this, 0, mapPanel.getRectProj());
		}

		@Override
		public boolean notifyObservers(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
			repaint(100);
			return super.notifyObservers(markStatus, mouseEvent, overlayMarker, overlayMark);
		}

		@Override
		public String getMarkerName() {
			return getUnitName();
		}
		
		/*
		 * Can we make a mark ? This is a default behaviour - ctrl is down 
		 * and there is at least one observer, but this coul dbe overridden. 
		 */
		@Override
		public boolean isCanMark(javafx.scene.input.MouseEvent e) {
			if (!e.isControlDown()) return false;
//			if (getObserverCount() == 0) return false;
			return true;
		}

		@Override
		public boolean showNoObserverPopup(javafx.scene.input.MouseEvent e) {
			List<PamDataUnit> markedDataUnits = getSelectedMarkedDataUnits(getCurrentMark(), null, MarkDataSelector.OVERLAP_ALL);
			if (markedDataUnits == null) {
				return false;
			}
//			Debug.out.printf("Marking %d dataunits on map\n", markedDataUnits.size());
			return mapPanel.handleMarkedMapUnits(markedDataUnits);
		}

//		@Override
//		public boolean mousePressed(javafx.scene.input.MouseEvent e) {
//			boolean used = super.mousePressed(e);
////			Debug.out.println("Map mouse pressed is used " + used);
//			if (used == false && e.isPopupTrigger()) {
//				Debug.out.println("Unused mark popup on map");
//			}
//			return used;
//		}
//		
//		@Override
//		public boolean mouseReleased(javafx.scene.input.MouseEvent e) {
//			boolean used = super.mouseReleased(e);
//			if (this.isMarkComplete() == false) {
//				return used;
//			}
////			Debug.out.println("Map mouse released is used " + used);
//			if (used == false && e.isPopupTrigger()) {
//				Debug.out.println("Unused mark popup on map");
//				this.getSelectedMarkedDataUnits(overlayMark, markDataSelector)
//			}
//			return used;
//		}
		
	}

	@Override
	public String getFrameTitle() {
		return getUnitName();
	}

	@Override
	public String getUniqueName() {
		return getUnitName();
	}

	@Override
	public void setUniqueName(String uniqueName) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the gridBaseControl
	 */
	protected GridbaseControl getGridBaseControl() {
		return gridBaseControl;
	}

	public MapDetectionsManager getMapDetectionsManager() {
		return mapDetectionsManager;
	}


	public JPanel getViewerControls() {
		return viewerControls;
	}

	public MapOverlayMarker getMapMarker() {
		return mapMarker;
	}
}