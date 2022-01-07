package PamView.paneloverlay.overlaymark;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import GPS.GpsData;
import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.HoverData;
import PamView.TransformShape;
import PamguardMVC.PamDataUnit;

public class MarkDataMatcher {

	private OverlayMark overlayMark;
	
	private GeneralProjector projector;
	
	private Shape markShape;
	
	private boolean showsBearings = false;
	
	/**
	 * @param overlayMark
	 * @param projector
	 */
	public MarkDataMatcher(OverlayMark overlayMark, GeneralProjector projector) {
		super();
		this.overlayMark = overlayMark;
		this.projector = projector;
		/*
		 * From the projector, recalculate the mark as a region in screen coordinates. 
		 */
		markShape = overlayMark.getMarkShape(projector);
		showsBearings = doesShowBearing(projector);
	}

	private boolean doesShowBearing(GeneralProjector projector) {
		ParameterType[] types = projector.getParameterTypes();
		if (types == null || types.length < 2) {
			return false;
		}
		return (types[0] == ParameterType.LATITUDE && types[1] == ParameterType.LONGITUDE);
	}

	public boolean matchDataUnit(HoverData hoverData) {
		if (markShape == null) return false;
		// get the shape out of the hoverData...
//		hoverData.
		return false;
	}
	
	/**
	 * The point is contained within the mark. 
	 * @param hoverData date describing what was drawn. 
	 * @return true if the central point is within the mark. 
	 */
	public boolean isContained(HoverData hoverData) {
		if (markShape == null) return false;
//		Point2D pt = hoverData.getPoint2D();
//		if (pt == null) return false;
//		return markShape.contains(pt);
		Shape hoverShape = hoverData.getDrawnShape();
		if (hoverShape == null) {
			return false;
		}
		// need to iterate about the hover shape and 
		// see if it lies within the markShape
		PathIterator it = hoverShape.getPathIterator(null);
		float[] pathCoords = new float[6];
		int nIn = 0;
		while (true) {
			it.next();
			if (it.isDone()) {
				break;
			}
			it.currentSegment(pathCoords);
			// just check the first coordinate of every segment. 
			if (markShape.contains(pathCoords[0], pathCoords[1]) == false) {
				return false;
			}
			nIn++;
		}
		return nIn > 0;
	}
	
	/**
	 * Does a bearing line to the point from the detection origin 
	 * overlap the mark. Implicit assumption before calling this 
	 * that the data are on a map !
	 * @return true if the line crosses part of the mark.
	 */
	public boolean bearingOverlap(HoverData hoverData) {
		if (showsBearings == false) return false;
		if (markShape == null) return false;
		Point2D endPt = hoverData.getShapeCentre();
		if (endPt == null) return false;
		GpsData ll = hoverData.getDataUnit().getOriginLatLong(false);
		if (ll == null) return false;
		Point2D oPt = projector.getCoord3d(ll).getPoint2D();
		Line2D bLine = new Line2D.Double(oPt, endPt);
		PathIterator markPath = markShape.getPathIterator(null);
		double[] pathPoints = new double[6];
		Point2D prevPoint = null;
		while (!markPath.isDone()) {
			markPath.currentSegment(pathPoints);
			Point2D pt = new Point2D.Double(pathPoints[0], pathPoints[1]);
			if (prevPoint != null) {
				Line2D lineSeg = new Line2D.Double(prevPoint, pt);
				if (bLine.intersectsLine(lineSeg)) {
					return true;
				}
			}
			prevPoint = pt;			
			markPath.next();
		}
		
		return false;
	}
}
