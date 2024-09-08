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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Array.sensors.ArraySensorDataUnit;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GPSParameters;
import GPS.GpsData;
import GPS.GpsDataUnit;
import Map.gridbaselayer.GridbaseControl;
import Map.gridbaselayer.MapRasterImage;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.BasicKeyItem;
import PamView.ColorManaged;
import PamView.ColourArray;
import PamView.ColourScheme;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.SymbolKeyItem;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.paneloverlay.OverlayCheckboxMenuItem;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkOverlayDraw;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.debug.Debug;
import effort.EffortDataUnit;
import effort.EffortProvider;
import pamMaths.PamVector;

/**
 * This is the actual map display, with the bluebackground, the gps track, etc.
 * IT sits inside the SimpleMap along with the right hand controls.
 *
 */
public class MapPanel extends JPanelWithPamKey implements PamObserver, ColorManaged {

	private static final long serialVersionUID = 1L;
	JPopupMenu detectorMenu;
	JPopupMenu plotDetectorMenu;
	// private double mapRotationDegrees;
	// private LatLong shipLLD = new LatLong();
	private MapRectProjector rectProj = new MapRectProjector();
	private LatLong mapCentreDegrees;
	private int mapRangeMetres;
	TransformUtilities trans = new TransformUtilities();
	private double pixelsPerMetre = 1.0;
	public Vessel ship;
	public Compass myCompass;
	StraightLineGrid grid;
	MouseAdapter popupListener;
	MouseAdapter detectorPopupListener;
	MapController mapController;
	MapContourGraphics mapContourGraphics;
	SimpleMap simpleMapRef;
	Coordinate3d[] arraygeom;
	Coordinate3d[] arrayPanelOffsets;
	Coordinate3d[] arrayPanelPositions;

	/**
	 * String type name for map based data selectors.
	 */
	public static final String DATASELECTNAME = "MAP";

	private ArrayList<PanelOverlayDraw> constantOverlays = new ArrayList<>();

	private AffineTransform baseXform;

	private boolean repaintBase = false;
	
	private EffortDataUnit latestEffort;

	public MapPanel(MapController mapController, SimpleMap simpleMap) {
		this.mapController = mapController;
		this.simpleMapRef = simpleMap;
		this.setOpaque(true);
		ship = new Vessel(Color.RED);
		grid = new StraightLineGrid(this);
		mapCentreDegrees = new LatLong();
		myCompass = new Compass();
		mapContourGraphics = new MapContourGraphics();

		baseXform = new AffineTransform();
		baseXform.scale(1, 1);

		// GetPlotDetectorMenu();
		detectorPopupListener = new DetectorPopupListener();
		addMouseListener(detectorPopupListener);
		// contour();
		baseXform = new AffineTransform();
		baseXform.scale(1, 1);
		rectProj.setMapPanelRef(this);

		addMouseListener(rectProj.getMouseHoverAdapter(this));
		addMouseMotionListener(rectProj.getMouseHoverAdapter(this));

		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// PamColors.getInstance().registerComponent(this, PamColor.MAP);
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	@Override
	public PamColor getColorId() {
		return PamColor.MAP;
	}

	public void newViewTime(GpsDataUnit gpsDataUnit) {

		if (gpsDataUnit == null)
			return;

		GpsData gpsData = gpsDataUnit.getGpsData();

		ship.setShipGps(gpsData);

	}

	/**
	 * Called from simple map when new gps data arrive.
	 * 
	 * @param newGpsDataUnit
	 */
	protected void newGpsData(GpsDataUnit newGpsDataUnit) {
		ship.setShipGps(newGpsDataUnit.getGpsData());
		newShipLLD();
		paintNewGPSData(newGpsDataUnit);
	}

	private PamArray lastArray;
	// long lastCall = 0;
	private LatLong lastMapCentreDegrees = new LatLong(0, 0);
	private int lastMapRangeMetres = 0;
	private double lastMapRotationDegrees = 0;
	private int lastHeight, lastWidth;
	private BufferedImage baseDrawing;
	private int imageWidth, imageHeight;

	@Override
	public void paintComponent(Graphics g) {

		if (!isShowing() || g == null)
			return;

		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		// g2d.setTransform(AffineTransform.getShearInstance(.1, .1));

		rectProj.clearHoverList();
		
		if (PamController.getInstance().getPamStatus() == PamController.PAM_LOADINGDATA) {
			Debug.out.println("Loading data, don't draw");
			return;
		}

		pixelsPerMetre = (double) this.getHeight() / mapRangeMetres;
		rectProj.setMapCentreDegrees(mapCentreDegrees);
		rectProj.setMapRangeMetres(mapRangeMetres);
		// rectProj.setMapRotationDegrees(mapRotationDegrees);
		rectProj.setPanelHeight(this.getHeight());
		rectProj.setPanelWidth(this.getWidth());
		rectProj.setPixelsPerMetre(pixelsPerMetre);
		ship.setPixelsPerMetre(pixelsPerMetre);
		// simpleMapRef.gpsTextPanel.setPixelsPerMetre(getPixelsPerMetre());
		/*
		 * to speed up map drawing, only perform certain actions if the map dimension
		 * have changed in any way. This is to include most of the drawing of the base
		 * map, grid and ships track (eventually). These will all be drawn onto a
		 * separate BufferedImage which will then be quickly drawn onto the main
		 * graphics device without having to go through all the transformations. In the
		 * future, it may also be extended to include a lot of the other data, but will
		 * leave that for now.
		 */
		if (repaintBase || lastHeight != getHeight() || lastWidth != getWidth() || lastMapRangeMetres != mapRangeMetres
				|| lastMapRotationDegrees != rectProj.getMapRotationDegrees()
				|| !lastMapCentreDegrees.equals(mapCentreDegrees)) {
			lastHeight = getHeight();
			lastWidth = getWidth();
			lastMapCentreDegrees = mapCentreDegrees.clone();
			lastMapRangeMetres = mapRangeMetres;
			lastMapRotationDegrees = rectProj.getMapRotationDegrees();
			pixelsPerMetre = (double) this.getHeight() / mapRangeMetres;
			// pixelsPerMetre = (double)this.getWidth()/mapRangeMetres;
			rectProj.setMapCentreDegrees(mapCentreDegrees);
			rectProj.setMapRangeMetres(mapRangeMetres);
			// rectProj.setMapRotationDegrees(mapRotationDegrees);
			rectProj.setPanelHeight(this.getHeight());
			rectProj.setPanelWidth(this.getWidth());
			rectProj.setPixelsPerMetre(pixelsPerMetre);
			ship.setPixelsPerMetre(pixelsPerMetre);

			prepareBaseImage();

			// drawBase = true;

		}

		// long timeNow2 = PamCalendar.getTimeInMillis();

		myCompass.setMapRotationDegrees(rectProj.getMapRotationDegrees());
		myCompass.setPanelWidth(this.getWidth());

		Color currentColor;
		currentColor = PamColors.getInstance().getColor(PamColor.MAP);
		currentColor = new Color(.7f, .7f, 1, .1f);
		((Graphics2D) g).setColor(currentColor);
		((Graphics2D) g).fillRect(0, 0, this.getWidth(), this.getHeight());
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.drawImage(baseDrawing, baseXform, this);
		paintRangeRings(g2d);

		/*
		 * Draw the ship track
		 */
		// if (currentArray.getHydrophoneLocator().isStatic() == false) {
		// paintTrack(g); // draw it all
		// was drawn on the base map.
		if (!simpleMapRef.mapParameters.hideShip) {
			paintPredictedGPS(g);
			paintShip(g);
		}
		// }
		paintDetectorData(g);

		if (simpleMapRef.mapParameters.showHydrophones) {
			paintHydrophones(g);
		}
		// long timeTaken = PamCalendar.getTimeInMillis() - timeNow;
		// long timeTaken2 = PamCalendar.getTimeInMillis() - timeNow2;
		// if (drawBase)
		// System.out.println("Draw map and base map in " + timeTaken + " ms, and " +
		// timeTaken2 +" ms");
		// else
		// System.out.println("Draw map in " + timeTaken + " ms, and " + timeTaken2 +"
		// ms");
		if (simpleMapRef.mouseDownPoint != null && simpleMapRef.mouseDragPoint != null
				&& mapController.getMouseMoveAction() == MapController.MOUSE_MEASURE) {
			g.setColor(PamColors.getInstance().getColor(PamColor.AXIS));
			g.drawLine(simpleMapRef.mouseDownPoint.x, simpleMapRef.mouseDownPoint.y, simpleMapRef.mouseDragPoint.x,
					simpleMapRef.mouseDragPoint.y);
			measureSymbol.setFillColor(PamColors.getInstance().getColor(PamColor.AXIS));
			measureSymbol.setLineColor(measureSymbol.getFillColor());
			measureSymbol.draw(g, simpleMapRef.mouseDownPoint);
			measureSymbol.draw(g, simpleMapRef.mouseDragPoint);
		}

		drawConstantOverlays(g);
	}

	/**
	 * Draw 'constant' map overlays. Currently, this is just marks from the
	 * MapMarker, but might get extended
	 * 
	 * @param g
	 */
	private void drawConstantOverlays(Graphics g) {
		if (constantOverlays == null) {
			return;
		}
		for (PanelOverlayDraw pd : constantOverlays) {
			if (!pd.canDraw(getRectProj())) {
				continue;
			}
			pd.drawDataUnit(g, null, getRectProj());
		}
	}

	PamSymbol measureSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.RED, Color.RED);
	private LatLong minCornerLatLong;
	private LatLong maxCornerLatLong;
	private ColourScheme currentColorScheme;

	private Color getSurfaceColour() {
		Color currentColor = PamColors.getInstance().getColor(PamColor.MAP);
		if (Math.cos(Math.toRadians(rectProj.getMapVerticalRotationDegrees())) < 0) {
			float uwScale = 255.f * 1.1f; // scale for seeing the surface form below ....
			return new Color(currentColor.getRed() / uwScale, currentColor.getGreen() / uwScale,
					currentColor.getBlue() / uwScale, 1.f);
		} else {
			return currentColor;
		}
	}

	private void prepareBaseImage() {
		// System.out.println("Draw base map, image width = " + imageWidth);
		if (baseDrawing == null || baseDrawing.getWidth() != lastWidth || baseDrawing.getHeight() != lastHeight) {
			imageWidth = getWidth();
			imageHeight = getHeight();
			baseDrawing = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		}
		Graphics2D g = (Graphics2D) baseDrawing.getGraphics();

		Color currentColor;
		calculatePlotLimits();

		if (isFillSurface()) {
			currentColor = PamColors.getInstance().getColor(PamColor.MAP);
			g.setColor(currentColor);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		} else {
			currentColor = PamColors.getInstance().getColor(PamColor.PlOTWINDOW);
			g.setColor(currentColor);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			drawRotatedSurface(g);
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		GridbaseControl gridbaseControl = simpleMapRef.getGridBaseControl();
		if (gridbaseControl != null) {
			ColourScheme colorScheme = PamColors.getInstance().getColourScheme();
			boolean remakeImage = false;
			if (colorScheme != currentColorScheme) {
				currentColorScheme = colorScheme;
				remakeImage = true;
			}

			MapRasterImage rasterImage = gridbaseControl.getImage(null, null, remakeImage);
			if (rasterImage != null) {
				gridbaseControl.getSwingPainter().paintMapImage(g, rectProj, rasterImage);
			}

		}

		if (grid != null) {
			if (!simpleMapRef.mapParameters.hideGrid) {
				grid.drawGrid(g, rectProj);
			}
			myCompass.drawCompass(g, rectProj);

		}

		paintContours(baseDrawing.getGraphics());

		myCompass.setPanelWidth(getWidth());
		myCompass.drawCompass((g), rectProj);

		/*
		 * Draw the track in the main paintComponent since it continually needs
		 * redrawing as it's uncovered by the ships body.
		 */
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		if (!currentArray.getHydrophoneLocator().isStatic()) {
			paintTrack(g); // paint the entire track held in memory
			// paintShip(g);
		}

		repaintBase = false;
	}

	/*
	 * Paint range rings on the map.
	 */
	private void paintRangeRings(Graphics2D g) {
		if (simpleMapRef.mapParameters.showRangeRings == MapParameters.RANGE_RINGS_NONE) {
			return;
		}
		g.setColor(PamColors.getInstance().getColor(PamColor.LATLINE));
		double ringsRange = simpleMapRef.mapParameters.rangeRingDistance;
		String units = "m";
		if (simpleMapRef.mapParameters.showRangeRings == MapParameters.RANGE_RINGS_NMI) {
			ringsRange *= LatLong.MetersPerMile;
			units = "nmi";
		} else if (simpleMapRef.mapParameters.showRangeRings == MapParameters.RANGE_RINGS_KM) {
			ringsRange *= 1000;
			units = "km";
		}
		// will have to distort to allow for the map rotation, but otherwise this is
		// pretty striaght forward.
		LatLong masterRef = MasterReferencePoint.getLatLong();
		if (masterRef == null) {
			masterRef = mapCentreDegrees;
		}
		double maxRange = masterRef.distanceToMetres(minCornerLatLong);
		maxRange = Math.max(maxRange, masterRef.distanceToMetres(maxCornerLatLong));
		int nRings = (int) (maxRange / ringsRange);
		double vertScale = rectProj.getMapVerticalRotationDegrees();
		vertScale = Math.cos(Math.toRadians(vertScale));
		Coordinate3d centre = rectProj.getCoord3d(masterRef);
		DecimalFormat rangeFormat = new DecimalFormat("##.## " + units);
		for (int i = 1; i < nRings; i++) {
			double xR = ringsRange * i * rectProj.pixelsPerMetre;
			double yR = xR * vertScale;
			int x = (int) (centre.x - xR);
			int y = (int) (centre.y - yR);
			int width = (int) (2 * xR);
			int height = (int) (2 * yR);
			g.drawOval(x, y, width, height);
			int textX = (int) (centre.x - xR * .707);
			int textY = (int) (centre.y - yR * .707);
			String txt = rangeFormat.format(simpleMapRef.mapParameters.rangeRingDistance * i);
			g.drawString(txt, textX, textY);
		}

	}

	private void drawRotatedSurface(Graphics2D g) {
		double mapScale = 2 * Math.PI * MapRectProjector.EARTHRADIUS * rectProj.pixelsPerMetre / 360.0;
		mapScale = rectProj.pixelsPerMetre;
		int[] px = new int[4];
		int[] py = new int[4];
		LatLong centLatLong = rectProj.mapCentreDegrees;
		Coordinate3d p;
		p = rectProj.getCoord3d(minCornerLatLong.getLatitude(), minCornerLatLong.getLongitude(), 0);
		px[0] = (int) p.x;
		py[0] = (int) p.y;
		p = rectProj.getCoord3d(minCornerLatLong.getLatitude(), maxCornerLatLong.getLongitude(), 0);
		px[1] = (int) p.x;
		py[1] = (int) p.y;
		p = rectProj.getCoord3d(maxCornerLatLong.getLatitude(), maxCornerLatLong.getLongitude(), 0);
		px[2] = (int) p.x;
		py[2] = (int) p.y;
		p = rectProj.getCoord3d(maxCornerLatLong.getLatitude(), minCornerLatLong.getLongitude(), 0);
		px[3] = (int) p.x;
		py[3] = (int) p.y;
		g.setColor(getSurfaceColour());
		if (simpleMapRef.getMapParameters().hideSurface) {
			g.drawPolygon(px, py, 4);
		}
		else {
			g.fillPolygon(px, py, 4);
		}

	}

	/**
	 * 
	 * @return true if the entire surface should be filled, not a rated rectangle.
	 */
	public boolean isFillSurface() {
		return (rectProj.getMapVerticalRotationDegrees() == 0. && !simpleMapRef.getMapParameters().hideSurface);
	}

	/**
	 * Calculate the absolute limits of the plot. If it's flat, then these will be
	 * the extremities of the corners of the rotated map. If it's rotated in 3D,
	 * then it will be whatever the corners would have been when the map was flat
	 * and north up.
	 */
	private void calculatePlotLimits() {
		// double mapScale = 2 * Math.PI * MapRectProjector.EARTHRADIUS *
		// rectProj.pixelsPerMetre / 360.0;
		LatLong[] cornerLatLong = new LatLong[4];
		if (isFillSurface()) {
			cornerLatLong[0] = rectProj.panel2LL(0, 0);
			cornerLatLong[1] = rectProj.panel2LL(0, getHeight());
			cornerLatLong[2] = rectProj.panel2LL(getWidth(), getHeight());
			cornerLatLong[3] = rectProj.panel2LL(getWidth(), 0);
		} else {
			double mapScale = rectProj.pixelsPerMetre;
			double halfAngle = Math.toRadians(15);
			double correction = Math.tan(halfAngle) / halfAngle;
			double hLatLong = this.getHeight() / mapScale / 2. * correction;
			halfAngle *= (double) getWidth() / (double) getHeight();
			correction = Math.tan(halfAngle) / halfAngle;
			double widLatLong = this.getWidth() / mapScale / 2. * correction;
			LatLong centLatLong = rectProj.mapCentreDegrees;
			cornerLatLong[0] = centLatLong.addDistanceMeters(-widLatLong, -hLatLong);
			cornerLatLong[1] = centLatLong.addDistanceMeters(-widLatLong, hLatLong);
			cornerLatLong[2] = centLatLong.addDistanceMeters(widLatLong, hLatLong);
			cornerLatLong[3] = centLatLong.addDistanceMeters(widLatLong, -hLatLong);
		}
		double centreLong = rectProj.getMapCentreDegrees().getLongitude();
		for (int i = 1; i < 4; i++) {
			double cl = PamUtils.constrainedAngle(cornerLatLong[i].getLongitude(), 180 + centreLong);
			cornerLatLong[i].setLongitude(cl);
		}
		// now find the absolute limits of the four corners.
		minCornerLatLong = cornerLatLong[0].clone();
		maxCornerLatLong = cornerLatLong[0].clone();
		for (int i = 1; i < 4; i++) {
			LatLong ll = cornerLatLong[i];
			minCornerLatLong.setLatitude(Math.min(minCornerLatLong.getLatitude(), ll.getLatitude()));
			minCornerLatLong.setLongitude(Math.min(minCornerLatLong.getLongitude(), ll.getLongitude()));
			maxCornerLatLong.setLatitude(Math.max(maxCornerLatLong.getLatitude(), ll.getLatitude()));
			maxCornerLatLong.setLongitude(Math.max(maxCornerLatLong.getLongitude(), ll.getLongitude()));
		}
	}

	/**
	 * @return the minCornerLatLong
	 */
	public LatLong getMinCornerLatLong() {
		return minCornerLatLong;
	}

	/**
	 * @return the maxCornerLatLong
	 */
	public LatLong getMaxCornerLatLong() {
		return maxCornerLatLong;
	}

	private boolean[] wantedContours = null;
	private Color[] contourColours = null;
	private int[] contourDepths = null;

	/**
	 * Need to make two lists, one is a list of depth numbers and colours, the other
	 * is a list of contours that should actually be plotted.
	 * 
	 * @return true if there is anything at all to plot.
	 */
	private boolean prepareContourData() {
		/*
		 * First work out what we WANT to plot
		 */
		MapFileManager mapManager = simpleMapRef.mapFileManager;
		Vector<java.lang.Integer> availableContours = mapManager.getAvailableContours();
		if (availableContours == null || availableContours.size() == 0 || simpleMapRef == null) {
			return false;
		}
		if (simpleMapRef.mapParameters.mapContours == null) {
			return false;
		}
		int nPossibles = Math.min(availableContours.size(), simpleMapRef.mapParameters.mapContours.length);
		if (nPossibles == 0) {
			return false;
		}
		wantedContours = new boolean[nPossibles];
		contourColours = new Color[nPossibles];
		contourDepths = new int[nPossibles];
		int nUsed = 0;
		for (int i = 0; i < nPossibles; i++) {
			if (simpleMapRef.mapParameters.mapContours[i]) {
				nUsed++;
			}
		}
		int iUsed = 0;
		for (int i = 0; i < nPossibles; i++) {
			wantedContours[i] = simpleMapRef.mapParameters.mapContours[i];
			contourDepths[i] = availableContours.get(i);
			if (wantedContours[i]) {
				contourColours[i] = getContourColour(iUsed++, nUsed);
			}
		}
		if (nUsed == 0) {
			return false;
		}
		return true;
		/*
		 * We now know how many there are, so can work out the colours putting null in
		 * for contours that aren't used.
		 */
		// for (int i = 0; i < nUsed; i++) {
		// contourColours[i] = getContourColour(i, nUsed);
		// }
		//
		// int nColours = availableContours.size();
		// int nContours = mapManager.getContourCount(); // count of contour objects
		// contourValues = null;
		//// contourValues = null;
		// int nUsedContours = 0;
		// if (nColours == 0) {
		// return false;
		// }
		//// if (nColours > 0) {
		//// contourColours = new Color[nColours];
		//// for (int i = 0; i < nColours; i++) {
		//// contourColours[i] = getContourColour(i, nColours);
		//// }
		//// }
		// boolean[] shouldPlot = simpleMapRef.mapParameters.mapContours;
		// if (shouldPlot == null || shouldPlot.length < nContours) {
		// simpleMapRef.mapParameters.mapContours = shouldPlot =
		// new boolean[nContours];
		// shouldPlot[0] = true;
		// }
		// nUsedContours = 0;
		// for (int i = 0; i < nContours; i++) {
		// if (shouldPlot[i]) {
		// nUsedContours = 0;
		// }
		// }
		//// contourColours = new Color[nUsedContours];
		//// usedContours = new MapContour[nUsedContours];
		//// int iCont = 0;
		//// for (int i = 0; i < nContours; i++) {
		//// if (shouldPlot[i]) {
		//// contourColours[iCont] = getContourColour(iCont, nUsedContours);
		//// usedContours[iCont] = mapManager.getMapContour(iCont);
		//// iCont++;
		//// }
		//// }
		// return true;
	}

	private void paintContours(Graphics g) {
		if (!prepareContourData()) {
			return;
		}
		MapFileManager mapManager = simpleMapRef.mapFileManager;
		MapContour mapContour;
		int ci;
		int nSegments = mapManager.getContourCount();
		for (int i = 0; i < nSegments; i++) {
			mapContour = mapManager.getMapContour(i);
			ci = mapManager.getContourIndex(mapContour.getDepth());
			if (ci < 0)
				continue;
			if (ci >= contourColours.length)
				continue;
			if (contourColours[ci] == null)
				continue;
			g.setColor(contourColours[ci]);
			paintContour(g, mapContour);
		}
	}

	private Color getContourColour(int listPos, int nContours) {
		// if (listPos == 0) return coastColour;
		// double f = (double) listPos / (double) nContours;
		// double f1 = 1-f;
		// return new Color((int)(shallowColour.getRed()*f1 + deepestColour.getRed()*f),
		// (int)(shallowColour.getGreen()*f1 + deepestColour.getGreen()*f),
		// (int)(shallowColour.getBlue()*f1+ deepestColour.getBlue()*f));
		if (listPos == 0) {
			return Color.BLACK;
		}
		if (listPos >= nContours) {
			return Color.WHITE;
		}
		ColourArray colourArray = ColourArray.createMergedArray(nContours - 1, Color.GREEN, Color.BLUE);
		return colourArray.getColours()[listPos - 1];
	}

	private void paintContour(Graphics g, MapContour mapContour) {
		Graphics2D g2d = (Graphics2D) g;
		Point p1, p2;
		p1 = new Point();
		p2 = new Point();
		Rectangle rect = new Rectangle(getWidth(), getHeight());
		Coordinate3d c1, c2;
		Vector<LatLong> latLongs = mapContour.getLatLongs();
		if (latLongs == null || latLongs.size() < 2)
			return;
		LatLong latLong = latLongs.get(0);
		LatLong prevLatLong = latLong;
		c1 = rectProj.getCoord3d(latLong.getLatitude(), latLong.getLongitude(), -latLong.getHeight());
		for (int i = 1; i < latLongs.size(); i++) {
			latLong = latLongs.get(i);
			c2 = rectProj.getCoord3d(latLong.getLatitude(), latLong.getLongitude(), -latLong.getHeight());
			p1.x = (int) c1.x;
			p1.y = (int) c1.y;
			p2.x = (int) c2.x;
			p2.y = (int) c2.y;
			if (wantContour(prevLatLong, latLong)) {
				if (p1.x != p2.x || p1.y != p2.y) {
					g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}
			c1 = c2;
			prevLatLong = latLong;
		}
	}
	
	/**
	 * Modified from the original wantLatLong method, but does a more thorough check to see if
	 * the contour line crosses any of the map edges.  The original method failed when both of the
	 * end points of the contour line were outside of the map area, but it still crossed through.  This
	 * typically happened when the user zoomed in very close to the map.
	 * 
	 * @param prevLatLong One end of the contour line
	 * @param latLong The other end of the contour line
	 * 
	 * @return true if the contour line crosses through the map
	 */
	private boolean wantContour(LatLong prevLatLong, LatLong latLong) {
		if (minCornerLatLong == null || maxCornerLatLong == null) {
			return false;
		}
		/*
		 * Need to make sure these angles are constrained to the centre of the map for
		 * high longitudes.
		 */
		double centralLong = rectProj.getMapCentreDegrees().getLongitude();
		double prevLong = PamUtils.constrainedAngle(prevLatLong.getLongitude(), 180+centralLong);
		double thisLong = PamUtils.constrainedAngle(latLong.getLongitude(), 180+centralLong);
		double maxCornerLong = PamUtils.constrainedAngle(maxCornerLatLong.getLongitude(), 180+centralLong);
		double minCornerLong = PamUtils.constrainedAngle(minCornerLatLong.getLongitude(), 180+centralLong);

		/*
		 * don't display coordinates > 170 degrees from centre to avoid wrapping on
		 * Antarctica map
		 */
//		double longRange = Math.min(Math.abs(maxCornerLong-minCornerLong)/2.,170);
		if ((Math.abs(thisLong-centralLong) > 170) && (Math.abs(prevLong)-centralLong > 170)) {
			return false;
		}
		
		// if either of the contour end points are within the map area, return true
		if ((prevLatLong.getLatitude() >= minCornerLatLong.getLatitude() && prevLatLong.getLatitude() <= maxCornerLatLong.getLatitude() &&
			prevLong >= minCornerLong && prevLong <= maxCornerLong )
				||
			(latLong.getLatitude() >= minCornerLatLong.getLatitude() && latLong.getLatitude() <= maxCornerLatLong.getLatitude() &&
			thisLong >= minCornerLong && thisLong <= maxCornerLong )
			) {
				return true;	
			}
		
		// now test if the contour line crosses any edge of the map
		boolean crosses = PamUtils.doesLineIntersectRect(
				prevLong,
				prevLatLong.getLatitude(),
				thisLong,
				latLong.getLatitude(),
				minCornerLong,
				minCornerLatLong.getLatitude(),
				maxCornerLong,
				maxCornerLatLong.getLatitude());

		return crosses;
	}

	private void paintTrack(Graphics g) {
		paintTrack(g, null);
	}

	private GpsDataUnit lastDrawGpsDataUnit;
	private Coordinate3d lastGpsCoodinate;
	private PamSymbol latestSymbol;

	long getMapStartTime() {
		switch (PamController.getInstance().getRunMode()) {
		case PamController.RUN_NORMAL:
		case PamController.RUN_MIXEDMODE:
			return PamCalendar.getTimeInMillis() - simpleMapRef.mapParameters.trackShowTime * 1000;
		case PamController.RUN_PAMVIEW:
			return PamCalendar.getSessionStartTime();
		}
		return PamCalendar.getTimeInMillis();
	}
	
	public EffortProvider findEffortProvider() {
		if (!simpleMapRef.mapParameters.colourByEffort) {
			return null;
		}
		if (simpleMapRef.effortDataBlock == null) {
			return null;
		}
		return simpleMapRef.effortDataBlock.getEffortProvider();
	}

	private void paintTrack(Graphics g, PamDataUnit lastDrawnUnit) {


		GPSDataBlock gpsDataBlock = simpleMapRef.getGpsDataBlock();
		if (gpsDataBlock == null) {
			return;
		}
		PamSymbolChooser symbolModifier = null;
		List<EffortDataUnit> effortThings = null;
		EffortDataUnit currentEffortThing = null;
		EffortProvider effortProvider = findEffortProvider();
		PamSymbol effortSymbol = null;
		Iterator<EffortDataUnit> effortIterator = null;
		if (effortProvider != null) {
			symbolModifier = effortProvider.getSymbolChooser(simpleMapRef.getSelectorName(), rectProj);
			effortThings = effortProvider.getAllEffortThings();
			if (effortThings == null || effortThings.size() == 0) {
				effortThings = null;
			}
			else {
				effortIterator = effortThings.iterator();
			}
			if (effortIterator != null && effortIterator.hasNext()) {
				currentEffortThing = effortIterator.next();
				if (symbolModifier != null) {
//					effortSymbol = symbolModifier.getPamSymbol(rectProj, currentEffortThing); 
					effortSymbol = effortProvider.getPamSymbol(symbolModifier, currentEffortThing);
					latestEffort = currentEffortThing;
					latestSymbol = effortSymbol;
				}
			}
		}
		Graphics2D g2d = (Graphics2D) g;
		long mapStartTime = getMapStartTime();
		Color defaultColour = PamColors.getInstance().getColor(PamColor.GPSTRACK);
		if (effortSymbol != null) {
			g2d.setStroke(new BasicStroke(effortSymbol.getLineThickness()));
		}
		else {
			g2d.setStroke(new BasicStroke(1));
		}
		if (effortSymbol != null && mapStartTime > currentEffortThing.getEffortStart()) {
			g.setColor(effortSymbol.getLineColor());
		}
		else {
			g.setColor(defaultColour);
		}
		
		// GPSControl gpsControl = GPSControl.getGpsControl();
		// if (gpsControl == null) {
		// return;
		// }
		// PamDataBlock<GpsDataUnit> gpsDataBlock = gpsControl.getGpsDataBlock();
		long t1 = getMapStartTime(), t2 = Long.MAX_VALUE;
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			t1 = simpleMapRef.getViewerScroller().getMinimumMillis();
			t2 = simpleMapRef.getViewerScroller().getMaximumMillis();
		}
		long maxInterpTime = mapController.getMaxInterpolationTime() * 1000;
		long lastFixTime = 0, thisFixTime;
		if (gpsDataBlock != null && gpsDataBlock.getUnitsCount() > 0) {
			GpsData gpsData;
			Coordinate3d c1, c2 = null;
			GpsDataUnit dataUnit;
			synchronized (gpsDataBlock.getSynchLock()) {
				ListIterator<GpsDataUnit> gpsIterator = gpsDataBlock.getListIterator(0);
				dataUnit = gpsIterator.next();
				
				if (gpsIterator.hasNext()) {
					
					gpsData = dataUnit.getGpsData();
					lastFixTime = dataUnit.getTimeMilliseconds();
					
					
					
					c1 = rectProj.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0.);
					while (gpsIterator.hasNext()) {
						dataUnit = gpsIterator.next();
						
						// sort out effort colours. 
//						if (currentEffortThing != null) {
							while (currentEffortThing != null && dataUnit.getTimeMilliseconds() > currentEffortThing.getEffortEnd()) {
								// get the next one. then decide if we're in or not. 
								if (effortIterator.hasNext()) {
									currentEffortThing = effortIterator.next();
									if (symbolModifier != null) {
										effortSymbol = effortProvider.getPamSymbol(symbolModifier, currentEffortThing);
									}
								}
								else {
									break;
								}
//								else {
//									currentEffortThing = null;
//									effortSymbol = null;
//								}
							}
//						}
						if (effortSymbol != null && currentEffortThing.inEffort(dataUnit.getTimeMilliseconds())) {
							g.setColor(effortSymbol.getLineColor());
							latestEffort = currentEffortThing;
							latestSymbol = effortSymbol;
						}
						else {
							g.setColor(defaultColour);
//							latestSymbol = null;
						}
						
						gpsData = dataUnit.getGpsData();
						if (!gpsData.isDataOk()) {
							continue;
						}
						c2 = rectProj.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0.);
						thisFixTime = dataUnit.getTimeMilliseconds();
						if (lastDrawnUnit == null
								|| dataUnit.getTimeMilliseconds() <= lastDrawnUnit.getTimeMilliseconds()) {
							if (dataUnit.getTimeMilliseconds() >= t1 && dataUnit.getTimeMilliseconds() <= t2
									&& thisFixTime - lastFixTime < maxInterpTime) {
								g.drawLine((int) c1.x, (int) c1.y, (int) c2.x, (int) c2.y);
							}
						}
						lastFixTime = thisFixTime;
						c1.x = c2.x;
						c1.y = c2.y;
						lastDrawGpsDataUnit = dataUnit;
					}
					lastGpsCoodinate = c2;
					// and finally, draw to the predicted ship position ...
					// if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
					// if (gpsData != null) {
					// gpsData = gpsData.getPredictedGPSData(PamCalendar.getTimeInMillis());
					// c2 = rectProj.getCoord3d(gpsData.getLatitude(), gpsData
					// .getLongitude(), 0.);
					// g.drawLine((int) c1.x, (int) c1.y, (int) c2.x, (int) c2.y);
					// }
					// }
				}
			}
		}
	}

	private void paintPredictedGPS(Graphics g) {

		if (lastDrawGpsDataUnit == null || lastGpsCoodinate == null) {
			return;
		}
		GpsData gpsData = lastDrawGpsDataUnit.getGpsData();
		Coordinate3d c2;
		g.setColor(PamColors.getInstance().getColor(PamColor.GPSTRACK));
		// and finally, draw to the predicted ship position ...
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			if (gpsData != null) {
				gpsData = gpsData.getPredictedGPSData(PamCalendar.getTimeInMillis());
				c2 = rectProj.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0.);
				g.drawLine((int) lastGpsCoodinate.x, (int) lastGpsCoodinate.y, (int) c2.x, (int) c2.y);
			}
		}
	}

	/**
	 * Paint the new GPS data onto the base image, starting from the previous
	 * coordinate.
	 * 
	 * @param newGpsDataUnit new Gps data
	 */
	private void paintNewGPSData(GpsDataUnit newGpsDataUnit) {
		if (baseDrawing == null) {
			return;
		}
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return;
		}
		paintNewGPSData(baseDrawing.getGraphics(), newGpsDataUnit);
	}

	/**
	 * Pint new gps data on the given graphics handle
	 * 
	 * @param g              graphics handle
	 * @param newGpsDataUnit gps data unit.
	 */
	private void paintNewGPSData(Graphics g, GpsDataUnit newGpsDataUnit) {

		Coordinate3d c2;
//		g.setColor(PamColors.getInstance().getColor(PamColor.GPSTRACK));
//		if (latestSymbol != null) {
//			float t = latestSymbol.getLineThickness();
//			if (t != 1) {
//				Graphics2D g2d = (Graphics2D) g;
//				g2d.setStroke(new BasicStroke(t));
//			}
//			if (latestEffort != null && latestEffort.inEffort(newGpsDataUnit.getTimeMilliseconds())) {
//				g.setColor(latestSymbol.getLineColor());
//			}
//		}
//		else {
		EffortProvider effortProvider = findEffortProvider();
		boolean effSet = false;
		if (effortProvider != null) {
			latestEffort = effortProvider.getLastEffort();
			if (latestEffort != null) {
				PamSymbolChooser symbolModifier = effortProvider.getSymbolChooser(simpleMapRef.getSelectorName(), rectProj);
				if (symbolModifier != null) {
					PamSymbol symbol = effortProvider.getPamSymbol(symbolModifier, latestEffort);
//					PamSymbol symbol = symbolModifier.getPamSymbol(rectProj, latestEffort);
					if (symbol != null && latestEffort.inEffort(newGpsDataUnit.getTimeMilliseconds())) {
						symbol.getSymbolData().setGraphicsProperties(g);
						effSet = true;
					}
				}
			}
		}
		if (!effSet) {
			g.setColor(PamColors.getInstance().getColor(PamColor.GPSTRACK));
		}
		/**
		 * This can get quite long, so need to do the same iterating through the effort data as for normal plotting. 
		 */
		
		GpsData gpsData = newGpsDataUnit.getGpsData();
		c2 = rectProj.getCoord3d(gpsData.getLatitude(), gpsData.getLongitude(), 0.);
		if (lastGpsCoodinate != null) {
			g.drawLine((int) lastGpsCoodinate.x, (int) lastGpsCoodinate.y, (int) c2.x, (int) c2.y);
		}
		lastDrawGpsDataUnit = newGpsDataUnit;
		lastGpsCoodinate = c2;

	}

	private void paintShip(Graphics g) {
		if (ship == null || ship.getShipGps(false) == null) {
			return;
		}
		// find the GPS controller - need to get the ships dimensions out
		// of it.
		PamControllerInterface pc = PamController.getInstance();
		GPSControl gpsControl = (GPSControl) pc.findControlledUnit("GPS Acquisition");
		GPSParameters gpsParameters = null;
		if (gpsControl != null) {
			gpsParameters = gpsControl.getGpsParameters();
			ship.setVesselDimension(gpsParameters.dimA, gpsParameters.dimB, gpsParameters.dimC, gpsParameters.dimD);
			ship.setPredictionArrow(gpsParameters.plotPredictedPosition ? gpsParameters.predictionTime : 0);
		}

		ship.drawShip(((Graphics2D) g), rectProj);
	}

	private void paintHydrophones(Graphics g) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int phoneCount = array.getHydrophoneCount();
		int phoneMap = (1 << phoneCount) - 1;
		if (phoneCount == 32) {
			phoneMap = PamUtils.makeChannelMap(phoneCount);
		}
		SnapshotGeometry geometry = ArrayManager.getArrayManager().getSnapshotGeometry(phoneMap,
				PamCalendar.getTimeInMillis());
		if (geometry == null)
			return;
		GpsData refLatLong = geometry.getReferenceGPS();
		if (refLatLong == null)
			return;
		PamVector[] phoneVectors = geometry.getGeometry();
		Coordinate3d c3d;// = rectProj.getCoord3d(refLatLong.getLatitude(), refLatLong.getLongitude(),
							// refLatLong.getHeight());
		PamSymbol symbol;
		if (phoneVectors == null)
			return;
		for (int i = 0; i < phoneVectors.length; i++) {
			if (phoneVectors[i] == null)
				continue;
			LatLong phonePos = refLatLong.addDistanceMeters(phoneVectors[i]);
			c3d = rectProj.getCoord3d(phonePos.getLatitude(), phonePos.getLongitude(), phonePos.getHeight());
			symbol = array.getHydrophoneSymbol(i);
			double symSize = rectProj.symbolSizePerpective(simpleMapRef.mapParameters.symbolSize, c3d);
			symbol.setWidth(symSize);
			symbol.setHeight(symSize);
			Color symbolColour = Color.blue;
			if (simpleMapRef.mapParameters.colourHydrophonesByChannel) {
				symbolColour = PamColors.getInstance().getChannelColor(i);
			}
			symbol.setLineColor(symbolColour);
			symbol.setFillColor(symbolColour);
			symbol.draw(g, c3d.getXYPoint());
			// g.drawString(String.format("%d", i), (int) c3d.x, (int) c3d.y);
		}

	}

	@Deprecated
	private void paintHydrophones_old(Graphics g) {

		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int phoneCount = array.getHydrophoneCount();
		// Hydrophone h;
		LatLong latLong;
		long t = PamCalendar.getTimeInMillis();
		Coordinate3d c3d;
		PamSymbol symbol;
		for (int i = 0; i < phoneCount; i++) {
			latLong = array.getHydrophoneLocator().getPhoneLatLong(t, i);
			if (latLong == null)
				continue;
			c3d = rectProj.getCoord3d(latLong.getLatitude(), latLong.getLongitude(), latLong.getHeight());
			symbol = array.getHydrophoneSymbol(i);
			double symSize = rectProj.symbolSizePerpective(simpleMapRef.mapParameters.symbolSize, c3d);
			symbol.setWidth(symSize);
			symbol.setHeight(symSize);
			Color symbolColour = Color.blue;
			if (simpleMapRef.mapParameters.colourHydrophonesByChannel) {
				symbolColour = PamColors.getInstance().getChannelColor(i);
			}
			symbol.setLineColor(symbolColour);
			symbol.setFillColor(symbolColour);
			symbol.draw(g, c3d.getXYPoint());
		}
	}

	private void paintDetectorData(Graphics g) {
		g.setColor(Color.BLACK);
		// long mapStartTime = now - simpleMapRef.mapParameters.dataShowTime * 1000;
		ArrayList<PamDataBlock> detectorDataBlocks = simpleMapRef.mapDetectionsManager.plottableBlocks;
		if (detectorDataBlocks != null) {
			PamDataBlock dataBlock;
			for (int m = 0; m < detectorDataBlocks.size(); m++) {
				dataBlock = detectorDataBlocks.get(m);
//				boolean cont = dataBlock.getOverlayDraw().preDrawAnything(g, dataBlock, rectProj);
//				if (cont) {
					paintDetectorData(g, dataBlock);
//				}
			}
		}
	}

	private void paintDetectorData(Graphics g, PamDataBlock dataBlock) {
		MapDetectionData mapDetectionData;
		PamDataUnit dataUnit;
		// String tempDataUnitId;
		ListIterator<PamDataUnit> duIterator;
		long now = simpleMapRef.getMapTime();
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			now = System.currentTimeMillis();
			// simpleMapRef.setm
		}
		Integer sliderVal = simpleMapRef.getHiddenSliderTime();
		long earliestToPlot = 0;
		long latestToPlot = Long.MAX_VALUE;
		MapDrawingOptions mapDrawingOptions = null;
		// int n1=0, n2 =0;
//		try {
//			synchronized (dataBlock.getSynchLock()) {
				mapDetectionData = simpleMapRef.mapDetectionsManager.findDetectionData(dataBlock);
				if (mapDetectionData == null) {
					return; // this should never happen !
				}
				if (!mapDetectionData.select) {
					return;
				}
				if (mapDetectionData.fade) {
					mapDrawingOptions = new MapDrawingOptions(1);
					rectProj.setProjectorDrawingOptions(mapDrawingOptions);
				} else {
					mapDrawingOptions = null;
					rectProj.setProjectorDrawingOptions(null);
				}
				DataSelector ds = dataBlock.getDataSelector(simpleMapRef.getSelectorName(), false, DATASELECTNAME);
				rectProj.setDataSelector(ds);
				// see if the datablock has a symbol manager.
				PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
				if (symbolManager != null) {
					PamSymbolChooser symbolChooser = symbolManager
							.getSymbolChooser(simpleMapRef.mapDetectionsManager.getUnitName(), rectProj);
					rectProj.setPamSymbolChooser(symbolChooser);
				} else {
					rectProj.setPamSymbolChooser(null);
				}
				boolean contin = dataBlock.getOverlayDraw().preDrawAnything(g, dataBlock, rectProj);
				if (!contin) {
					return;
				}
				if (mapDetectionData.allAvailable) {
					earliestToPlot = 0;
				} else if (sliderVal != null) {
					earliestToPlot = now - 1000 * sliderVal;
				} else {
					long tOffs = now - earliestToPlot;
					earliestToPlot = now - mapDetectionData.getDisplayMilliseconds();
				}
				if (simpleMapRef.getMapParameters().viewerShowAll) {
					if (simpleMapRef.getViewerScroller() != null) {
						earliestToPlot = simpleMapRef.getViewerScroller().getMinimumMillis();
						now = simpleMapRef.getViewerScroller().getMaximumMillis();
					}
				}
				// tempDataUnitId = dataBlock.getDataName() + ", "
				// + dataBlock.getParentProcess().getProcessName();
				if (!mapDetectionData.select) {
					return;
				}
				ds = dataBlock.getDataSelector(simpleMapRef.getUnitName(), false, DATASELECTNAME);
//				ds = null;
				ArrayList<PamDataUnit> dataCopy = dataBlock.getDataCopy(earliestToPlot, now, true, ds);
				duIterator = dataCopy.listIterator();
				while (duIterator.hasNext()) {
					dataUnit = duIterator.next();
					// if (dataUnit.getTimeMilliseconds() != 0 &&
					// dataUnit.getLastChangeTime() < earliestToPlot) {
					// continue; // functionality moved into shouldPlot.
					// }
					if (!simpleMapRef.shouldPlot(dataUnit, mapDetectionData, earliestToPlot, now, null)) {
						continue;
					}
					rectProj.setDataSelector(ds);
					if (mapDrawingOptions != null && mapDetectionData.fade) {
						float opacity = mapDrawingOptions.calculateOpacity(now, earliestToPlot,
								dataUnit.getLastChangeTime());
						mapDrawingOptions.opacity = opacity;
					}
					dataBlock.drawDataUnit(g, dataUnit, this.rectProj);
					// if (dataUnit.getChannelBitmap() == 15) n1++;
					// else n2++;
				}
//			}
//			// if (dataBlock.getDataName().contains("Contour"))
//			// System.out.printf("Drew %d front group and %d back group now %s, earliest
//			// %s\n", n1, n2,
//			// PamCalendar.formatTime(now), PamCalendar.formatTime(earliestToPlot));;
//		} catch (ConcurrentModificationException ex) {
//			System.out.println("Concurrency exception in " + dataBlock.getDataName());
//			// ex.printStackTrace();
//		}
	}

	// Timer t = new Timer(100, new ActionListener() {
	// public void actionPerformed(ActionEvent evt) {
	// repaint();
	// }
	// });

	/**
	 * Instruct map to redraw it's base image next time anything is redrawn.
	 */
	public void repaintBaseDrawing() {
		repaintBase = true;
	}

	// @Override
	public void repaint(boolean baseToo) {
		// sets a flag to make sure the base map gets repainted too.
		repaintBase |= baseToo;
		super.repaint();

	}

	public double getMapRotationDegrees() {
		return rectProj.getMapRotationDegrees();
	}

	public void setMapRotationDegrees(double mapRotationDegrees) {
		rectProj.setMapRotationDegrees(mapRotationDegrees);
	}

	public double getMapVerticalRotationDegrees() {
		return rectProj.getMapVerticalRotationDegrees();
	}

	public LatLong getMapCentreDegrees() {
		return mapCentreDegrees;
	}

	public void setMapCentreDegrees(LatLong mapCentreDegrees) {
		if (mapCentreDegrees != null) {
			this.mapCentreDegrees = mapCentreDegrees;
		}
		/*
		 * if (mapCentreDegrees.longDegs<0.0){ //System.out.println("MapPanel Setter:
		 * long is negative"); System.exit(0); }
		 */
	}

	public void setMapCentreCoords(Coordinate3d c) {
		this.mapCentreDegrees = rectProj.panel2LL(c);
	}

	public int getMapRangeMetres() {
		return mapRangeMetres;
	}

	public void setMapRangeMetres(int mapRangeMetres) {
		this.mapRangeMetres = mapRangeMetres;
		if (simpleMapRef != null) {
			simpleMapRef.gpsTextPanel.displayZoomedorRotated();
		}
	}

	public LatLong getShipLLD() {
		return ship.getShipLLD();
	}

	// public GpsData getShipGpsData() {
	// return ship.getShipGps();
	// }
	public GpsData getShipGpsData(boolean predict) {
		return ship.getShipGps(predict);
	}

	public void newShipLLD() {
		LatLong shipLLD = ship.getShipLLD();
		if (simpleMapRef.mapParameters.headingUp) {
			rotateHeadingUp(false);
		}
		if (simpleMapRef.mapParameters.keepShipCentred) {
			mapCentreDegrees.setLatitude(shipLLD.getLatitude());
			mapCentreDegrees.setLongitude(shipLLD.getLongitude());
		} else if (simpleMapRef.mapParameters.keepShipOnMap) {
			Coordinate3d sc = rectProj.getCoord3d(shipLLD.getLatitude(), shipLLD.getLongitude(), 0);
			if (sc.x <= 0 || sc.x >= getWidth()) {
				mapCentreDegrees.setLongitude((shipLLD.getLongitude() + mapCentreDegrees.getLongitude()) / 2.);
				repaint();
			}
			if (sc.y <= 0 || sc.y >= getHeight()) {
				mapCentreDegrees.setLatitude((shipLLD.getLatitude() + mapCentreDegrees.getLatitude()) / 2.);
				repaint();
			}
		}
		if (simpleMapRef != null) {
			simpleMapRef.gpsTextPanel.displayZoomedorRotated();
		}
	}

	protected void rotateHeadingUp(boolean repaint) {
		try {
			setMapRotationDegrees(-(ship.getShipGps(false).getCourseOverGround()));
			// System.out.println("blkjedw: "+mp.ship.getShipGps().getTrueCourse());
			myCompass.setMapRotationDegrees(getMapRotationDegrees());
		} catch (NullPointerException e) {

		}
		if (repaint) {
			this.repaint();
		}
	}

	protected void rotateNorthUp(boolean repaint) {
		setMapRotationDegrees(0.0);
		myCompass.setMapRotationDegrees(0.0);
		simpleMapRef.mapParameters.headingUp = false;
		if (repaint) {
			this.repaint();
		}
	}

	public MapRectProjector getRectProj() {
		return rectProj;
	}

	public void setRectProj(MapRectProjector rectProj) {
		this.rectProj = rectProj;
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
//		if (arg instanceof EffortDataUnit) {
		// won't work since only the data send notifications. 
//			System.out.println("Effort add, so repaint base");
//			repaintBaseDrawing();
//		}
		// PamDataBlock block = (PamDataBlock) o;
		repaint(250);
	}
	
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
//		if (pamDataUnit instanceof EffortDataUnit) {
//			System.out.println("Effort update, so repaint base");
//			repaintBaseDrawing();
//		}
		repaint(250);
	}

	@Override
	public String getObserverName() {
		return "Map Panel";
	}

	@Override
	// public void paint(Graphics g) {
	// if (getKeyPanel() != null) {
	// synchronized (getKeyPanel()) {
	// super.paint(g);
	// }
	// }
	// else {
	// super.paint(g);
	// }
	// }

	public void setSampleRate(float sampleRate, boolean notify) {
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	public double getPixelsPerMetre() {
		return pixelsPerMetre;
	}

	public void setPixelsPerMetre(double pixelsPerMetre) {
		this.pixelsPerMetre = pixelsPerMetre;
	}

	class DetectorPopupListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			// moved to mouse handler in simplemap
			// if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			// simpleMapRef.createMapComment(e.getX(), e.getY());
			// }
		}

		// @Override
		// public void mousePressed(MouseEvent e) {
		// maybeShowPopup(e);
		// }
		//
		// @Override
		// public void mouseReleased(MouseEvent e) {
		// maybeShowPopup(e);
		// }
		//
		// private void maybeShowPopup(MouseEvent e) {
		// if (e.isPopupTrigger()) {
		// plotDetectorMenu().show(e.getComponent(), e.getX(), e.getY());
		// }
		// }
	}

	protected void updateObservers() {

		ArrayList<PamDataBlock> detectorDataBlocks = simpleMapRef.mapDetectionsManager.plottableBlocks;
		for (int i = 0; i < detectorDataBlocks.size(); i++) {
			detectorDataBlocks.get(i).addObserver(this);
		}
		// also make the map observe any sensor data blocks. 
		ArrayList<PamDataBlock> sensorBlocks = PamController.getInstance().getDataBlocks(ArraySensorDataUnit.class, true);
		if (sensorBlocks != null) {
			for (PamDataBlock aBlock : sensorBlocks) {
				aBlock.addObserver(this);
			}
		}
		EffortProvider effBlock = findEffortProvider();
		if (effBlock != null) {
			effBlock.getParentDataBlock().addObserver(this);
		}
	}

	protected void createKey() {
		if (!PamController.getInstance().isInitializationComplete()) {
			return;
		}
		if (!simpleMapRef.mapParameters.showKey) {
			// keyPanel.getPanel().setVisible(false);
			this.setKeyPanel(null);
			return;
		}

		KeyPanel keyPanel = new KeyPanel("Key", PamKeyItem.KEY_SHORT);

		simpleMapRef.mapDetectionsManager.createBlockList();
		ArrayList<PamDataBlock> detectorDataBlocks = simpleMapRef.mapDetectionsManager.plottableBlocks;
		int nUsed = 0;
		if (detectorDataBlocks != null) {
			PamDataBlock dataBlock;
			for (int m = 0; m < detectorDataBlocks.size(); m++) {
				dataBlock = detectorDataBlocks.get(m);
				// tempDataUnitId = detectorDataBlocks.get(m).getDataName() + ", "
				// + detectorDataBlocks.get(m).getParentProcess().getProcessName();
				if (!dataBlock.canDraw(rectProj))
					continue;
				if (simpleMapRef.shouldPlot(dataBlock)) {
					SymbolData symbolData = null;
					PamDataBlock aBlock = detectorDataBlocks.get(m);
					PamSymbolManager symbolManager = aBlock.getPamSymbolManager();
					if (symbolManager != null) {
						PamSymbolChooser symbolChooser = symbolManager
								.getSymbolChooser(simpleMapRef.mapDetectionsManager.getUnitName(), rectProj);
						try {
							symbolData = symbolChooser.getSymbolChoice(rectProj, null);
						} catch (Exception e) {
							// will throw when the symbolchooser can't handle a null data unit
						}
					}
					if (symbolData != null) {
						keyPanel.add(new SymbolKeyItem(new PamSymbol(symbolData), aBlock.getDataName()));
					} else {
						keyPanel.add(detectorDataBlocks.get(m).createKeyItem(rectProj, PamKeyItem.KEY_SHORT));
					}
					nUsed++;
				}
			}
		}
		// then add the coast and the colours.
		if (prepareContourData()) {

			// private boolean[] wantedContours = null;
			// private Color[] contourColours = null;
			// private int[] contourDepths = null;
			for (int i = 0; i < wantedContours.length; i++) {
				if (!wantedContours[i]) {
					continue;
				}
				keyPanel.add(createContourKeyItem(contourDepths[i], contourColours[i]));
				nUsed++;
			}
		}

		this.setKeyPanel(keyPanel);
		keyPanel.getPanel().setVisible(nUsed > 0);
	}

	private PamKeyItem createContourKeyItem(int depth, Color colour) {
		String str;
		if (depth == 0) {
			str = " Coast";
		}
		if (depth == -1) {
			str = " Ice edge";
		} else {
			str = String.format(" %d m Contour", depth);
		}
		// color
		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 2, 20, true, colour, colour);
		symbol.setLineThickness(3);
		symbol.setIconStyle(PamSymbol.ICON_STYLE_LINE);
		return new BasicKeyItem(symbol, str);
	}

	//
	// public void refreshPlotableDetectorLists(){
	// updateObservers();
	// detectorDataBlocks =
	// PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
	// if(simpleMapRef.mapParameters.plotableBlockIdList==null){
	// simpleMapRef.mapParameters.plotableBlockIdList = new ArrayList<String>();
	// }
	// if(simpleMapRef.mapParameters.plottedBlockIds==null){
	// simpleMapRef.mapParameters.plottedBlockIds = new ArrayList<String>();
	// }
	// ArrayList<String> dataBlockIdList = new ArrayList<String>();
	// for(int i = 0;i<detectorDataBlocks.size();i++){
	// if (detectorDataBlocks.get(i).canDraw(rectProj) == false) continue;
	// String dataBlockId = detectorDataBlocks.get(i).getDataName() + ", "
	// + detectorDataBlocks.get(i).getParentProcess().getProcessName();
	// dataBlockIdList.add(dataBlockId);
	// if(simpleMapRef.mapParameters.plotableBlockIdList.indexOf(dataBlockId)==-1){
	// //Add the ID to the list
	// simpleMapRef.mapParameters.plotableBlockIdList.add(dataBlockId);
	// }
	// }
	// for (int i = 0; i<simpleMapRef.mapParameters.plotableBlockIdList.size();i++){
	// if(dataBlockIdList.indexOf(simpleMapRef.mapParameters.plotableBlockIdList.get(i))==-1){
	// //Remove the ID from the list
	// simpleMapRef.mapParameters.plotableBlockIdList.set(i, "");
	// }
	// }
	// while(simpleMapRef.mapParameters.plotableBlockIdList.contains("")){
	// simpleMapRef.mapParameters.plotableBlockIdList.
	// remove(simpleMapRef.mapParameters.plotableBlockIdList.indexOf(""));
	// }
	// }

	JPopupMenu plotDetectorMenu() {

		// refreshPlotableDetectorLists();
		plotDetectorMenu = new JPopupMenu();
		OverlayCheckboxMenuItem checkMenuItem;

		JMenuItem menuItem;
		plotDetectorMenu.add(simpleMapRef.getOptionsMenuItem(simpleMapRef.mapController.getGuiFrame()));
		menuItem = new JMenuItem("Plot overlay options...");
		menuItem.addActionListener(new OverlayOptions());
		plotDetectorMenu.add(menuItem);
		plotDetectorMenu.addSeparator();

//		ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
//		ImageIcon settingsIconNot = new ImageIcon(
//				ClassLoader.getSystemResource("Resources/SettingsButtonSmallWhite.png"));

		ArrayList<MapDetectionData> mddList = simpleMapRef.mapDetectionsManager.getMapDetectionDatas();
		for (int i = 0; i < mddList.size(); i++) {
			if (mddList.get(i).dataBlock == null)
				continue;
			// DataSelector dataSel =
			// mddList.get(i).dataBlock.getDataSelector(simpleMapRef.mapDetectionsManager.getUnitName(),
			// false);
			boolean needFancy = needFancyMapMenuItem(mddList.get(i).dataBlock);
			// if (needFancy) {
			// if(mddList.get(i).select) {
			// checkMenuItem = new OverlayCheckboxMenuItem(mddList.get(i).dataName,
			// settingsIcon);
			// }
			// else {
			// checkMenuItem = new OverlayCheckboxMenuItem(mddList.get(i).dataName,
			// settingsIconNot);
			// }
			// }
			// else {
			// checkMenuItem = new OverlayCheckboxMenuItem(mddList.get(i).dataName);
			// }
			checkMenuItem = new OverlayCheckboxMenuItem(mddList.get(i).dataBlock,
					simpleMapRef.mapDetectionsManager.getUnitName(), mddList.get(i).select, true);
			// try {
			// checkMenuItem.setToolTipText(mddList.get(i).dataBlock.getParentProcess().getPamControlledUnit().getUnitName());
			// }
			// catch (NullPointerException e) {
			// }
			checkMenuItem.setSelected(mddList.get(i).select);
			checkMenuItem.addActionListener(new ManagedDisplaySelection(mddList.get(i)));
			plotDetectorMenu.add(checkMenuItem);
		}

		plotDetectorMenu.addSeparator();
		plotDetectorMenu.add(showKeyMenu = new JCheckBoxMenuItem("Show Key", simpleMapRef.mapParameters.showKey));
		showKeyMenu.addActionListener(new ShowKey());
		plotDetectorMenu.add(
				panZoomMenu = new JCheckBoxMenuItem("Show pan zoom controls", simpleMapRef.mapParameters.showPanZoom));
		panZoomMenu.addActionListener(new ShowPanZoom());
		plotDetectorMenu.add(showGpsMenu = new JCheckBoxMenuItem("Show Gps and Cursor data",
				simpleMapRef.mapParameters.showGpsData));
		showGpsMenu.addActionListener(new ShowGpsMenu());
		// plotDetectorMenu.addMouseListener(detectorPopupListener); // why was this
		// here ??????
		plotDetectorMenu.addSeparator();
		plotDetectorMenu.add(simpleMapRef.clipboardCopier.getCopyMenuItem("Copy map to clipboard"));
		plotDetectorMenu.add(simpleMapRef.clipboardCopier.getPrintMenuItem("Print map ..."));

		return plotDetectorMenu;
	}

	/**
	 * Work out if the menu item needs afancy menu item with additional settings.
	 * This is needed if there is data or symbol management built into the data
	 * block.
	 * 
	 * @param dataBlock
	 * @return true if a fancy menu is needed.
	 */
	private boolean needFancyMapMenuItem(PamDataBlock dataBlock) {
		DataSelector dataSel = dataBlock.getDataSelector(simpleMapRef.mapDetectionsManager.getUnitName(), false,
				DATASELECTNAME);
		if (dataSel != null) {
			return true;
		}
		PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
		if (symbolManager != null) {
			return true;
		}
		return false;
	}

	JCheckBoxMenuItem panZoomMenu, showGpsMenu, showKeyMenu;

	class ShowKey implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			simpleMapRef.mapParameters.showKey = showKeyMenu.isSelected();
			createKey();
		}
	}

	class ShowPanZoom implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			simpleMapRef.mapParameters.showPanZoom = panZoomMenu.isSelected();
			simpleMapRef.showMapObjects();
		}
	}

	class ShowGpsMenu implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			simpleMapRef.mapParameters.showGpsData = showGpsMenu.isSelected();
			simpleMapRef.showMapObjects();
		}
	}

	class ManagedDisplaySelection implements ActionListener {

		private MapDetectionData mapDetectionData;

		public ManagedDisplaySelection(MapDetectionData mapDetectionData) {
			this.mapDetectionData = mapDetectionData;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			OverlayCheckboxMenuItem menuItem = (OverlayCheckboxMenuItem) e.getSource();
			Point mousePoint = MouseInfo.getPointerInfo().getLocation();
			Point menuPos = menuItem.getLocOnScreen();
			boolean iconClicked = false;
			if (menuItem.getIcon() != null && menuPos != null) {
				int w = menuItem.getIcon().getIconWidth() * 2; // allow for the icon and the check mark.
				if (mousePoint.x - menuPos.x < w + menuItem.getIconTextGap()) {
					iconClicked = true;
				}
			}
			if (iconClicked) {
				menuItem.setSelected(!menuItem.isSelected());
				DataSelector ds = mapDetectionData.dataBlock
						.getDataSelector(simpleMapRef.mapDetectionsManager.getUnitName(), false, DATASELECTNAME);
				PamSymbolManager symbolManager = mapDetectionData.dataBlock.getPamSymbolManager();
				PamSymbolChooser symbolChooser = null;
				if (symbolManager != null) {
					symbolChooser = symbolManager.getSymbolChooser(simpleMapRef.mapDetectionsManager.getUnitName(),
							rectProj);
				}
				if (ds == null && symbolChooser == null) {
					return;
				}
				// if (ds.showSelectDialog(mapController.getGuiFrame())) {
				if (showDataSelectDialog(mapDetectionData, ds, symbolChooser)) {
					menuItem.setSelected(true);
					simpleMapRef.mapDetectionsManager.setShouldPlot(mapDetectionData.dataName, true);
					simpleMapRef.checkViewerData();
					updateObservers();
					createKey();
				}
			} else {
				simpleMapRef.mapDetectionsManager.setShouldPlot(mapDetectionData.dataName, menuItem.isSelected());
				simpleMapRef.checkViewerData();
				updateObservers();
				createKey();
			}
		}
	}

	private boolean showDataSelectDialog(MapDetectionData mapDetectionData, DataSelector ds,
			PamSymbolChooser symbolChooser) {
		DataSelectDialog dataSelectDialog = new DataSelectDialog(mapController.getGuiFrame(),
				mapDetectionData.dataBlock, ds, symbolChooser);
		boolean ok = dataSelectDialog.showDialog();
		// selectDialogToOpen();
		// boolean ok = dataSelectDialog.showDialog();
		// selectDialogClosed(ok);
		return ok;
	}

	class OverlayOptions implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Frame frame = (JFrame) PamController.getMainFrame();
			if (mapController.getPamView() != null) {
				frame = mapController.getGuiFrame();
			}

			MapDetectionsParameters newParams = MapDetectionsDialog.showDialog(frame,
					simpleMapRef.mapDetectionsManager);
			if (newParams != null) {
				simpleMapRef.mapDetectionsManager.setMapDetectionsParameters(newParams);
				updateObservers();
				createKey();
			}

		}

	}

	// class DisplaySelection implements ActionListener {
	// public void actionPerformed(ActionEvent e) {
	// JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
	// if(menuItem.getState()){
	// simpleMapRef.mapParameters.plottedBlockIds.add(menuItem.getText());
	// }else{
	// simpleMapRef.mapParameters.plottedBlockIds.remove(menuItem.getText());
	// }
	// createKey();
	// }
	// }
//
//	private boolean shouldPlot(PamDataBlock pamDataBlock) {
//		return simpleMapRef.mapDetectionsManager.isShouldPlot(pamDataBlock);
//	}
//
//	private boolean shouldPlot(PamDataUnit pamDataUnit, MapDetectionData mapDetectionData, long earliestToPlot, long now, DataSelector ds) {
//		if (mapDetectionData.select == false) {
//			return false;
//		}
//		if (ds != null && ds.scoreData(pamDataUnit) == 0) {
//			return false;
//		}
//		if (mapDetectionData.allAvailable) {
//			return true;
//		}
//		long unitTime = pamDataUnit.getTimeMilliseconds();
//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//			PamScrollSlider viewerSlider = simpleMapRef.getViewerScroller();
//
//			if (unitTime != 0) {
//				if (unitTime < viewerSlider.getMinimumMillis() ||
//						unitTime > viewerSlider.getMaximumMillis()) {
//					return false;
//				}			
//			}
//			if (simpleMapRef.mapParameters.viewerShowAll) {
//				return true;
//			}
//			else {
//				
//				// if we are plotting data behind the scrollbar (data that has already occurred)...
//				if (!mapDetectionData.lookAhead) {
//					if (unitTime > now || unitTime < earliestToPlot) {
//						return false;
//					}
//				}
//				
//				// if we are plotting data ahead of the scrollbar (future data)...
//				else {
//					long dispIntval = Math.abs(now-earliestToPlot);
//					if (unitTime < now || unitTime > now+dispIntval) {
//						return false;
//					}
//				}
//				
//			}
//			return true;
//		}
//		else { // not viewer !
//			//			if (now - pamDataUnit.getLastChangeTime() > mapDetectionData.displaySeconds * 1000) {
//			long unitEndTime = Math.max(unitTime, pamDataUnit.getLastChangeTime());
//			if (now - unitEndTime > mapDetectionData.getDisplayMilliseconds()) {
//				return false;
//			}
//		}
//		return true;
//	}

	class SettingsAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// spectrogramDisplay.SetSettings();
		}
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		MapDetectionData mdd = simpleMapRef.mapDetectionsManager.findDetectionData((PamDataBlock) o);
		if (mdd != null) {
			return mdd.getDisplayMilliseconds();
		} else {
			return Math.max(simpleMapRef.mapParameters.dataKeepTime, simpleMapRef.mapParameters.dataShowTime) * 1000;
		}
	}

	@Override
	public void noteNewSettings() {
		createKey();
	}

	public void stepMapZoom(int direction) {
		if (direction == 0) {
			return;
		}
		if (direction < 0 & getMapRangeMetres() >= 20) {
			setMapZoom(0.97f);
		} else if (direction > 0 & getMapRangeMetres() <= 500000000) {
			setMapZoom(1.03f);
		}
	}

	public void setMapZoom(float zoomFactor) {
		int newVal = (int) (this.mapRangeMetres * zoomFactor);
		/**
		 * can get stuck since it's an integer. so get it unstuck !
		 */
		if (newVal == mapRangeMetres) {
			newVal += Math.signum(zoomFactor);
		}
		this.setMapRangeMetres(newVal);
	}

	@Override
	public void removeObservable(PamObservable o) {

	}

	public SimpleMap getSimpleMapRef() {
		return simpleMapRef;
	}

	public void setSimpleMapRef(SimpleMap simpleMapRef) {
		this.simpleMapRef = simpleMapRef;
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		repaint(true);
	}

	public void addConstantOverlay(MarkOverlayDraw markOverlayDraw) {
		constantOverlays.add(markOverlayDraw);
	}

	public void showPopupMenu(MouseEvent e) {
		/*
		 * See if we're on a data unit, in which case show it's context menu instead. 
		 */
		PamDataUnit hoveredDataUnit = rectProj.getHoveredDataUnit();
		List<PamDataUnit> markedDataUnits = simpleMapRef.getMapMarker().getSelectedMarkedDataUnits(null, null, MarkDataSelector.OVERLAP_ALL);
		JPopupMenu popMenu = null;
		if (hoveredDataUnit != null) {
			if (markedDataUnits != null) { // have hover AND marked units. 
				popMenu = hoveredDataUnit.getDataUnitPopupMenu(null, e.getPoint());
				PamDataBlock dataBlock = hoveredDataUnit.getParentDataBlock();
				markedDataUnits = dataBlock.getMyDataUnits(markedDataUnits); // convert to list of ones belonging to the one datablock. 
				if (markedDataUnits != null && markedDataUnits.size() > 1) {
					//				Object[] datas = markedDataUnits.toArray();
					PamDataUnit[] dataUnits = new PamDataUnit[markedDataUnits.size()];
					markedDataUnits.toArray(dataUnits);
					popMenu = dataBlock.getDataUnitPopupMenu(null, e.getPoint(), dataUnits);
				}
			}
			if (popMenu == null) {
				/**
				 * No marked units, or no menu for multiple units so try to get one for just the 
				 * one hovered unit. 
				 */
				popMenu = hoveredDataUnit.getDataUnitPopupMenu(null, e.getPoint());
			}
		}
		if (popMenu != null) {
			// then add an option to the end of it to show the standard menu instead 
			JMenuItem standardMenu = new JMenuItem("Show standard map menu");
			standardMenu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e2) {
					plotDetectorMenu().show(e.getComponent(), e.getX(), e.getY());	
				}
			});
			popMenu.addSeparator();
			popMenu.add(standardMenu);
		}
		if (popMenu == null) {
			popMenu = plotDetectorMenu();
		}
		popMenu.show(e.getComponent(), e.getX(), e.getY());		
	
	}
	
	/**
	 * This gets called when there are no other markes being used on the map, but some data
	 * have been marked out. Can use to annotate data units that have everything built
	 * into them using generic annotations. 
	 * @param markedDataUnits
	 * @return
	 */
	public boolean handleMarkedMapUnits(List<PamDataUnit> markedDataUnits) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get the map controller.
	 * 
	 * @return the map controller.
	 */
	public MapController getMapController() {
		return mapController;
	}

	public void setMapController(MapController mapController) {
		this.mapController = mapController;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		String tip =  super.getToolTipText(event);
		if (tip == null) {
			return tip;
		}
		if (!tip.startsWith("<html>GPS")) {
			return tip;
		}
		
		return tip;
	}

	public EffortDataUnit findEffortThing(long timeMilliseconds) {
		EffortProvider effortProvider = findEffortProvider();
		if (effortProvider == null) {
			return null;
		}
		return effortProvider.getEffort(timeMilliseconds);
	}



}
