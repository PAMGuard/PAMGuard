package PamView.paneloverlay.overlaymark;

import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.input.MouseEvent;

public class OverlayMark {

	static public enum OverlayMarkType {POLYGON, RECTANGLE};

	private GeneralProjector.ParameterType[] parameterTypes;

	private GeneralProjector.ParameterUnits[] parameterUnits;
	
	private ArrayList<PamCoordinate> coordinates = new ArrayList<>();
	
	private OverlayMarkType markType = OverlayMarkType.POLYGON;
	private PamCoordinate currentMouse;

	private Object markSource; // source of the data mark. 
	private int markChannels; // bitmap of channels associated with the mark. 

	private MarkExtraInfo markExtraInfo;

	private OverlayMarker overlayMarker;

	private boolean hidden;

	public OverlayMark(OverlayMarker overlayMarker, Object markSource, MarkExtraInfo markExtraInfo, int markChannels, GeneralProjector.ParameterType[] parameterTypes, 
			GeneralProjector.ParameterUnits[] parameterUnits) {
		this.overlayMarker = overlayMarker;
		this.markSource = markSource;
		this.markExtraInfo = markExtraInfo;
		this.markChannels = markChannels;
		this.parameterTypes = parameterTypes;
		this.parameterUnits = parameterUnits;
	}

	public OverlayMark(OverlayMarker overlayMarker, Object markSource, MarkExtraInfo markExtraInfo, int markChannels, GeneralProjector.ParameterType[] parameterTypes, 
			GeneralProjector.ParameterUnits[] parameterUnits, PamCoordinate firstCoordinate) {
		this.overlayMarker = overlayMarker;
		this.markSource = markSource;
		this.markExtraInfo = markExtraInfo;
		this.markChannels = markChannels;
		this.parameterTypes = parameterTypes;
		this.parameterUnits = parameterUnits;
		addCoordinate(firstCoordinate);
	}

	public int size() {
		return coordinates.size();
	}

	public PamCoordinate getCoordinate(int index) {
		return coordinates.get(index);
	}

	public PamCoordinate getLastCoordinate() {
		if (coordinates.size() < 1) return null;
		return coordinates.get(coordinates.size()-1);
	}

	public void addCoordinate(PamCoordinate pamCoordinate) {
		if (markType == OverlayMarkType.POLYGON) {
			coordinates.add(pamCoordinate);
		}
		else if (markType == OverlayMarkType.RECTANGLE) {
			while (coordinates.size() > 1) {
				coordinates.remove(coordinates.size()-1);
			}
			coordinates.add(pamCoordinate);
		}
	}


	/**
	 * @return the markType
	 */
	public OverlayMarkType getMarkType() {
		return markType;
	}

	/**
	 * @param markType the markType to set
	 */
	public void setMarkType(OverlayMarkType markType) {
		this.markType = markType;
	}

	public void setCurrentMouseCoordinate(PamCoordinate currentMouse) {
		this.currentMouse = currentMouse;
	}

	public PamCoordinate getCurrentMouseCoordinate() {
		return currentMouse;
	}
	/**
	 * @return the parameterTypes
	 */
	public GeneralProjector.ParameterType[] getParameterTypes() {
		return parameterTypes;
	}
	
	/**
	 * Find the index of a given parameter type, 
	 * @param parameterType
	 * @return index of the parameter, or -1 if it can't be found. 
	 */
	public int findParameterIndex(GeneralProjector.ParameterType parameterType) {
		if (parameterTypes == null) {
			return -1;
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i] == parameterType) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return the parameterUnits
	 */
	public GeneralProjector.ParameterUnits[] getParameterUnits() {
		return parameterUnits;
	}

	/**
	 * @return the coordinates
	 */
	public ArrayList<PamCoordinate> getCoordinates() {
		return coordinates;
	}

	/**
	 * 
	 * @return the central or mean position of all the mark points. 
	 */
	public PamCoordinate getCentre() {
		Coordinate3d pc = new Coordinate3d();
		double coords[] = new double[3];
		for (int i = 0; i < coordinates.size(); i++) {
			PamCoordinate coord = coordinates.get(i);
			for (int c = 0; c < coord.getNumCoordinates(); c++) {
				coords[c] += coord.getCoordinate(c);
			}
		}
		int n = coordinates.size();
		for (int i = 0; i <3; i++) {
			pc.setCoordinate(i, coords[i]/n);
		}
		return pc;
	}


	/**
	 * The limits of the overlay mark. The limits are x and y extremities. 
	 * @return the limits (minX, maxX, minY, maxY)
	 */
	public double[] getLimits(){

		//now must find the limits of the zoom. Can be complex shapes so a bit difficult; 
		double minX=Double.MAX_VALUE; 
		double maxX=Double.NEGATIVE_INFINITY; 

		double minY=Double.MAX_VALUE; 
		double maxY=Double.NEGATIVE_INFINITY; 

		//now find those maximum, and minimum values, 
		for (int i=0; i<coordinates.size(); i++){
			if (coordinates.get(i).getCoordinate(0)>maxX){
				maxX=coordinates.get(i).getCoordinate(0); 
			}
			if (coordinates.get(i).getCoordinate(0)<minX){
				minX=coordinates.get(i).getCoordinate(0);
			}

			if (coordinates.get(i).getCoordinate(1)>maxY){
				maxY=coordinates.get(i).getCoordinate(1);
			}
			if (coordinates.get(i).getCoordinate(1)<minY){
				minY=coordinates.get(i).getCoordinate(1);
			}
		}
		
		double[] limits = {minX, maxX, minY,maxY};
		
		return limits; 
	}

	/**
	 * @return the currentMouse
	 */
	public PamCoordinate getCurrentMouse() {
		return currentMouse;
	}

	/**
	 * Calculate the shape of the mark in screen coordinates. 
	 * @param projector projector onto screen coordinates. 
	 * @return shape (Rectangle or Region)
	 */
	public Shape getMarkShape(GeneralProjector projector) {
		switch (markType) {
		case RECTANGLE:
			return getRectangleShape(projector);
		case POLYGON:
			return getPolygonShape(projector);
		}
		return null;
	}
	/**
	 * Get the shape as a rectangle in screen coordinates. 
	 * @param projector screen projector
	 * @return rectangle shape.
	 */
	private Shape getRectangleShape(GeneralProjector projector) {
		if (coordinates.size() != 2) {
			return null;
		}
		PamCoordinate c0 = projector.getCoord3d(coordinates.get(0));
		PamCoordinate c1 = projector.getCoord3d(coordinates.get(1));
		if (c0 == null || c1 == null) {
			return null;
		}
		double x = Math.min(c0.getCoordinate(0), c1.getCoordinate(0));
		double y = Math.min(c0.getCoordinate(1), c1.getCoordinate(1));
		double w = Math.abs(c0.getCoordinate(0) - c1.getCoordinate(0));
		double h = Math.abs(c0.getCoordinate(1) - c1.getCoordinate(1));
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * Get the shape as a polygon in screen coordinates. 
	 * @param projector screen projector
	 * @return polygon shape.
	 */
	private Polygon getPolygonShape(GeneralProjector projector) {
		int nPoints = coordinates.size();
		int[] x = new int[nPoints];
		int[] y = new int[nPoints];
		for (int i = 0; i < nPoints; i++) {
			Coordinate3d c = projector.getCoord3d(coordinates.get(i));
			x[i] = (int) c.x;
			y[i] = (int) c.y;
		}
		return new Polygon(x, y, nPoints);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String type = "Unknown";
		if (markType != null) {
			type = markType.toString();
		}
		return super.toString() + String.format(" %s with %d points", type, size());
	}

	/**
	 * @return the markSource
	 */
	public Object getMarkSource() {
		return markSource;
	}

	/**
	 * @param markSource the markSource to set
	 */
	public void setMarkSource(Object markSource) {
		this.markSource = markSource;
	}

	/**
	 * @return the markChannels
	 */
	public int getMarkChannels() {
		return markChannels;
	}

	/**
	 * @param markChannels the markChannels to set
	 */
	public void setMarkChannels(int markChannels) {
		this.markChannels = markChannels;
	}

	/**
	 * Get the coordinate of the mouse in this mark in the coordinate
	 * frame of the given component. Used primarily for positioning popup menus. 
	 * @param component Swing component. 
	 * @param mouseEvent 
	 * @return mouse coordinate within the given component. 
	 */
	static public Point getSwingComponentMousePos(Component component, MouseEvent mouseEvent) {
		Point mouseLoc;
		if (mouseEvent == null) {
			mouseLoc = MouseInfo.getPointerInfo().getLocation();
		}
		else {
			mouseLoc = new Point((int) mouseEvent.getScreenX(), (int) mouseEvent.getScreenY());
		}
		if (component == null) {
			return mouseLoc;
		}
		Point comLoc = component.getLocationOnScreen();
		mouseLoc.translate(-comLoc.x, -comLoc.y);
		return mouseLoc;
	}

	/**
	 * Repaint whatever drew this mark. 
	 */
	public void repaintOwner() {
		if (markSource != null) {
			if (Component.class.isAssignableFrom(markSource.getClass())) {
				((Component) markSource).repaint();
			}
			else if (TDPlotPane.class.isAssignableFrom(markSource.getClass())) {
				((TDPlotPane) markSource).repaint();
			}
		}
		if (overlayMarker != null) {
			overlayMarker.updateMarkedDisplay();
		}
	}

	/**
	 * Set flag to hide the mark. This can be useful if
	 * the mark observer is dragging something and wants 
	 * to stop the mark rectangle appearing on the screen. 
	 * @param hidden hide flag
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return Flag to say the mark should not be drawn. 
	 */
	public boolean isHidden() {
		return hidden;
	}
	
}
