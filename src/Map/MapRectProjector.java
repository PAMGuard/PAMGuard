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

import java.awt.Point;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.util.ListIterator;

import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamguardMVC.debug.Debug;

/**
 * The Map Rectangle Projector.
 *  
 * @author Doug Gillespie
 *
 */
public class MapRectProjector extends MapProjector {
	
	/**
	 * The radius of the erath in meters. 
	 */
	static final double EARTHRADIUS = 6378160.;
	//	static final double EARTHRADIUS = 6366700.;

	double xScale;

	double yScale;

	double zScale;

	double originX;

	double originY;

	double originZ;

	double mapRangeMetres;

	LatLong mapCentreDegrees;// = new LatLong();

	double panelWidth;

	double panelHeight;

	private double mapRotationDegrees = 0.0;

	private double mapVerticalRotationDegrees = 0.0;

	double pixelsPerMetre;

	private MapPanel mapPanelRef;

	private LatLong lastClickedMouseLatLong;

	private TransformUtilities transformUtilities;

	public MapRectProjector() {
		super();
		mapCentreDegrees = new LatLong();
		lastClickedMouseLatLong = new LatLong();
		transformUtilities = new TransformUtilities();
	}

	@Override
	public void setScales(double xScale, double yScale, double zScale,
			double originX, double originY, double originZ) {
		this.originX = originX;
		this.originY = originY;
		this.originZ = originZ;
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getDataPosition(PamUtils.Coordinate3d)
	 */
	@Override
	public LatLong getDataPosition(PamCoordinate screenPosition) {
		return panel2LL(screenPosition);
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getCoord3d(PamUtils.PamCoordinate)
	 */
	@Override
	public Coordinate3d getCoord3d(LatLong latLong) {
		return getCoord3d(latLong.getLatitude(), latLong.getLongitude(), latLong.getHeight());
	}

	@Override
	public Coordinate3d getCoord3d(double latDegrees, double longDegrees, double height) {
		Coordinate3d panelPos = new Coordinate3d();
		panelPos.x = (PamUtils.constrainedAngle(longDegrees,mapCentreDegrees.getLongitude()+ 180)-mapCentreDegrees.getLongitude())
				* 2
				* Math.PI
				* EARTHRADIUS
				* Math.abs(Math.cos(mapCentreDegrees.getLatitude() * Math.PI
						/ 180.0)) * pixelsPerMetre / 360.0;
		panelPos.y = (latDegrees - mapCentreDegrees.getLatitude()) * 2 * Math.PI
				* EARTHRADIUS * pixelsPerMetre / 360.0;
		panelPos.z = height * pixelsPerMetre;
		transformUtilities.rotateDegreesZ(panelPos);
		if (mapVerticalRotationDegrees != 0) {
			transformUtilities.rotateDegreesX(panelPos);
		}
		/*
		 * Annoyingly, this do Perspective function badly distorts highly zoomed in 
		 * end points so that long lines that go far beyond the bounds of the map 
		 * plot aren't in the right place. 
		 */
		//		transformUtilities.doPerspective(panelPos, panelHeight);
		panelPos.x = panelPos.x + panelWidth / 2.0;
		panelPos.y = panelHeight / 2.0 - panelPos.y;
		return (panelPos);		

	}
	public Coordinate3d getCoord3dNoRotate(double latDegrees, double longDegrees, double height) {
		Coordinate3d panelPos = new Coordinate3d();
		panelPos.x = (PamUtils.constrainedAngle(longDegrees,mapCentreDegrees.getLongitude()+ 180)-mapCentreDegrees.getLongitude())
				* 2
				* Math.PI
				* EARTHRADIUS
				* Math.abs(Math.cos(mapCentreDegrees.getLatitude() * Math.PI
						/ 180.0)) * pixelsPerMetre / 360.0;
		panelPos.y = (latDegrees - mapCentreDegrees.getLatitude()) * 2 * Math.PI
				* EARTHRADIUS * pixelsPerMetre / 360.0;
		panelPos.z = height * pixelsPerMetre;
//		transformUtilities.rotateDegreesZ(panelPos);
//		if (mapVerticalRotationDegrees != 0) {
//			transformUtilities.rotateDegreesX(panelPos);
//		}
		/*
		 * Annoyingly, this do Perspective function badly distorts highly zoomed in 
		 * end points so that long lines that go far beyond the bounds of the map 
		 * plot aren't in the right place. 
		 */
		//		transformUtilities.doPerspective(panelPos, panelHeight);
		panelPos.x = panelPos.x + panelWidth / 2.0;
		panelPos.y = panelHeight / 2.0 - panelPos.y;
		return (panelPos);		

	}

	public Coordinate3d lld2Coord3dMeters(double latDegrees, double longDegrees,
			double d3, LatLong origin) {
		Coordinate3d offsetPosition  = new Coordinate3d();
		offsetPosition.x = (longDegrees - origin.getLongitude())
				* 2
				* Math.PI
				* EARTHRADIUS
				* Math
				.abs(Math.cos(origin.getLatitude() * Math.PI
						/ 180.0)) / 360.0;
		offsetPosition.y = (latDegrees - origin.getLatitude()) * 2 * Math.PI
				* EARTHRADIUS  / 360.0;
		return (offsetPosition);
	}

	public Coordinate3d LL2panel(LatLong LL) {
		return (getCoord3d(LL.getLatitude(), LL.getLongitude(), 0));
	}

	/**
	 * 
	 * Get an absolute lat long from a map position
	 * @param x
	 * @param y
	 * @return
	 */
	public LatLong panel2LL(double x, double y) {
		return panel2LL(new Coordinate3d(x,y,0));
	}
	/**
	 * Get an absolute lat long from a map position
	 * @param screenPosition
	 * @return
	 */
	public LatLong panel2LL(PamCoordinate screenPosition) {
		LatLong LL = new LatLong();
		Coordinate3d scPos = new Coordinate3d(screenPosition.getCoordinate(0) - panelWidth / 2, 
				panelHeight / 2.0 - screenPosition.getCoordinate(1));
		transformUtilities.unDoPerspective(scPos, panelHeight);
		if (mapVerticalRotationDegrees != 0.) {
			transformUtilities.unRotateDegreesX(scPos);
		}
		transformUtilities.unRotateDegreesZ(scPos);
		// LL.longDegs=c.x*360.0/Math.abs(Math.cos(mapCentreDegrees.getLatitude()*Math.PI/180.0)*2*Math.PI*EARTHRADIUS*pixelsPerMetre)-mapCentreDegrees.longDegs;
		LL.setLongitude( mapCentreDegrees.getLongitude()
				+ (scPos.x * 360.0)
				/ (Math.abs(Math.cos(Math.toRadians(mapCentreDegrees.getLatitude())))
						* 2 * Math.PI * EARTHRADIUS * pixelsPerMetre));
		LL.setLatitude( scPos.y * 360.0 / (2 * Math.PI * EARTHRADIUS * pixelsPerMetre)
				+ mapCentreDegrees.getLatitude());
		return (LL);
	}

	/**
	 * @return the mapVerticalRotationDegrees
	 */
	public double getMapVerticalRotationDegrees() {
		return mapVerticalRotationDegrees;
	}

	/**
	 * @param mapVerticalRotationDegrees the mapVerticalRotationDegrees to set
	 */
	public void setMapVerticalRotationDegrees(double mapVerticalRotationDegrees) {
		this.mapVerticalRotationDegrees = mapVerticalRotationDegrees;
		transformUtilities.setRotateXDegrees(mapVerticalRotationDegrees);
	}

	/**
	 * Not quite sure what this does - it seems to only be some relative measure of lat long, 
	 * not absolute. 
	 * @param c
	 * @return
	 */
	public LatLong image2LL(Coordinate3d c) {
		return image2LL(c.x, c.y);
	}

	/**
	 * Recalculate a symbol size using perspective...
	 * @param originalSize original symbol size
	 * @param screenPos 3D screen coordinate 
	 * @return new size. 
	 */
	public int symbolSizePerpective(int originalSize, Coordinate3d screenPos) {
		return symbolSizePerpective(originalSize, screenPos, 2, originalSize*3);
	}

	/**
	 * Recalculate symbol size using perspective
	 * @param originalSize original symbol size
	 * @param screenPos 3D screen coordinate 
	 * @param minSize minimum size for symbol
	 * @param maxSize maximum size for symbol.
	 * @return new size. 
	 */
	public int symbolSizePerpective(int originalSize, Coordinate3d screenPos, int minSize, int maxSize) {
		double cameraHeight = transformUtilities.getCameraHeight(mapPanelRef.getHeight());
		double newSize = (double) originalSize * cameraHeight / (cameraHeight - screenPos.z);
		return (int) Math.round(newSize);
	}

	/**
	 * 
	 * Not quite sure what this does - it seems to only be some relative measure of lat long, 
	 * not absolute. 
	 * @param x
	 * @param y
	 * @return
	 */
	public LatLong image2LL(double x, double y) {
		LatLong LL = new LatLong();
		// LL.longDegs=c.x*360.0/(Math.abs(Math.cos(mapCentreDegrees.getLatitude()*Math.PI/180.0))*2*Math.PI*EARTHRADIUS*pixelsPerMetre);
		LL.setLongitude( x * 360.0
				/ (Math.abs(Math.cos(mapCentreDegrees.getLatitude() * Math.PI / 180.0))
						* 2 * Math.PI * EARTHRADIUS) / pixelsPerMetre);

		// LL.getLatitude()=c.y*360.0/(2*Math.PI*EARTHRADIUS*pixelsPerMetre);
		LL.setLatitude( y * 360.0 / (2 * Math.PI * EARTHRADIUS) / pixelsPerMetre );
		return (LL);
	}

	public LatLong getMapCentreDegrees() {
		return mapCentreDegrees;
	}

	public void setMapCentreDegrees(LatLong mapCentreDegrees) {
		this.mapCentreDegrees = mapCentreDegrees;
	}

	public double getMapRangeMetres() {
		return mapRangeMetres;
	}

	public void setMapRangeMetres(double mapRangeMetres) {
		this.mapRangeMetres = mapRangeMetres;
	}

	public double getMapRotationDegrees() {
		return mapRotationDegrees;
	}

	public void setMapRotationDegrees(double mapRotationDegrees) {
		this.mapRotationDegrees = mapRotationDegrees;
		transformUtilities.setRotateZDegrees(mapRotationDegrees);
	}

	public double getPanelHeight() {
		return panelHeight;
	}

	public void setPanelHeight(double panelHeight) {
		this.panelHeight = panelHeight;
	}

	public double getPanelWidth() {
		return panelWidth;
	}

	public void setPanelWidth(double panelWidth) {
		this.panelWidth = panelWidth;
	}

	public double getPixelsPerMetre() {
		return pixelsPerMetre;
	}

	public void setPixelsPerMetre(double pixelsPerMetre) {
		this.pixelsPerMetre = pixelsPerMetre;
	}

	public void setMapPanelRef(MapPanel mapPanelRef) {
		this.mapPanelRef = mapPanelRef;
	}

	public MapPanel getMapPanelRef() {
		return mapPanelRef;
	}

	public LatLong getLastClickedMouseLatLong() {
		if (mapPanelRef != null) {
			if (mapPanelRef.simpleMapRef != null) {
				lastClickedMouseLatLong = mapPanelRef.simpleMapRef.getLastClickedMouseLatLong();
			}
		}
		return lastClickedMouseLatLong;
	}

	public void setLastClickedMouseLatLong(LatLong lastClickedMouseLatLong) {
		this.lastClickedMouseLatLong.setLatitude(lastClickedMouseLatLong.getLatitude());
		this.lastClickedMouseLatLong.setLongitude(lastClickedMouseLatLong.getLongitude());
	}

	public MouseMotionAdapter getMouseMotionAdapter(){
		return mapPanelRef.getSimpleMapRef().getMouseMotion();
	}


	public AffineTransform getAffineTransform() {
		double rz = transformUtilities.getRotateZDegrees();
		double rx = transformUtilities.getRotateXDegrees();
		if (rx == 0 && rz == 0) {
//		return null;
		}
//		AffineTransform zTrans = AffineTransform.getRotateInstance(Math.toRadians(rz), panelWidth/2., panelHeight/2.);
//		zTrans.scale(1, Math.cos(Math.toRadians(rx)));
//		zTrans.translate(0, panelHeight/4/Math.cos(Math.toRadians(rx)));
//		return zTrans;
		

	
		AffineTransform xTrans = new AffineTransform();
		double yTrans = panelHeight/2.*(1-Math.cos(Math.toRadians(rx)));
//		Debug.out.printf("ph = %3.1f, xAngle %3.1f, YTranslate = %3.1f\n", panelHeight, rx, yTrans);
		xTrans.translate(0, yTrans);
		xTrans.scale(1, Math.cos(Math.toRadians(rx)));
		xTrans.rotate(Math.toRadians(rz), panelWidth/2., panelHeight/2.);
//		xTrans.translate(0, 100);
//		xTrans.translate(0, panelHeight/4/Math.cos(Math.toRadians(rx)));
		return xTrans;
	}

	@Override
	public String getHoverText(Point mousePoint, int ploNumberMatch) {
		String text = super.getHoverText(mousePoint, ploNumberMatch);
		if (text == null) {
			return findGpsTrackText(mousePoint, ploNumberMatch);
		}
		else {
			return text;
		}
	}

	private String findGpsTrackText(Point mousePoint, int ploNumberMatch) {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return null;
		}
		LatLong currentPos = getDataPosition(new Coordinate3d(mousePoint.x, mousePoint.y));
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		double dist = Double.MAX_VALUE;
		GpsDataUnit closest = null;
		ListIterator<GpsDataUnit> it = gpsDataBlock.getListIterator(0);
		while (it.hasNext()) {
			GpsDataUnit gpsUnit = it.next();
			double r = gpsUnit.getGpsData().distanceToMetres(currentPos);
			if (r < dist) {
				dist = r;
				closest = gpsUnit;
			}
		}
		if (closest == null) {
			return null;
		}
		double rPix = dist*this.pixelsPerMetre;
		if (rPix > 20) {
			return null;
		}
		return closest.getSummaryString();
	}



}
