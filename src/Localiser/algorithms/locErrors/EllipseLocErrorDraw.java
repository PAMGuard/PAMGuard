package Localiser.algorithms.locErrors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamArrayUtils;
import PamView.TransformShape;
import PamView.GeneralProjector;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;

/**
 * Draws an elliptical error. The error is drawn as a semi translucent ellipse
 * @author Jamie Macaulay
 *
 */
public class EllipseLocErrorDraw implements LocErrorGraphics {


	/*
	 * Reference to the elliptical error. 
	 */
	private EllipticalError ellipticalError;

	public EllipticalError getEllipticalError() {
		return ellipticalError;
	}

	public static final int DRAW_LINES = 1;
	public static final int DRAW_OVALS = 2;
	private int drawType = DRAW_OVALS;


	/**
	 * Constructor for drawing an ellipsoid. 
	 * @param ellipticalError
	 */
	protected EllipseLocErrorDraw(EllipticalError ellipticalError){
		this.ellipticalError=ellipticalError;
	}


	@Override
	public TransformShape drawOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, 
			GeneralProjector generalProjector, Color ellipseColor) {
		if (ellipticalError == null) {
			return null;
		}
		switch(drawType) {
		case DRAW_LINES:
			return drawLinesOnMap(g, pamDetection, errorOrigin, generalProjector, ellipseColor);
		case DRAW_OVALS:
			return drawOvalsOnMap(g, pamDetection, errorOrigin, generalProjector, ellipseColor);
		}
		return null;
	}
	
	
	public TransformShape drawLinesOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, 
			GeneralProjector generalProjector, Color ellipseColor) {
		Graphics2D g2d = (Graphics2D) g;
		double[] angles = ellipticalError.getAngles();
		double[] errs = ellipticalError.getEllipseDim();
		if (angles == null || errs == null) {
			return null;
		}
		int n = Math.min(angles.length, errs.length);

		/*
		 *  now for each dimension we need to do the following ...
		 *  Dim0
		 *  need unit vector along heading, with appropriate pitch.
		 *  x = cos(h)cos(p), y = sin(h)cos(p), z = sin(p)
		 *  Dim1
		 *  need heading along roll, so that's a very weird rotation of head and roll.
		 */
		PamVector[] errVec = new PamVector[3];
		errVec[0] = PamVector.rotateVector(PamVector.yAxis, angles); 
		errVec[1] = PamVector.rotateVector(PamVector.xAxis, angles); 
		errVec[2] = PamVector.rotateVector(PamVector.zAxis, angles); 
//		for (int i = 0; i < n; i++) {
//			errVec[i] = errVec[i].times(errs[i]);
//		}
		Coordinate3d centCoord = generalProjector.getCoord3d(errorOrigin);
		Point xy = centCoord.getXYPoint();
		Point[] ends = new Point[2];
		g.setColor(ellipseColor);
		for (int i = 0; i < n; i++) {
			for (int d = 0; d < 2; d++) {
				double errMag = errs[i];
				if (d == 1) {
					errVec[i] = errVec[i].times(-1);
					if (errs.length > i+n) {
						errMag = errs[i+n];
					}
				}
				LatLong endLatLong = errorOrigin.addDistanceMeters(errVec[i].times(errMag));
				ends[d] = generalProjector.getCoord3d(endLatLong).getXYPoint();
				g.drawLine(xy.x, xy.y, ends[d].x, ends[d].y);
			}
		}
		
		
		return null;
	}
	
	public TransformShape drawOvalsOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, 
			GeneralProjector generalProjector, Color ellipseColor) {
		

		//this is 2D- need to make a slice through the ellipse and get the localisation points. 
		double[] errors2D=ellipticalError.getErrorEllipse2D(ErrorEllipse.PLANE_XY_PROJ);
		//		if (1>0) return null;
		
		if (errors2D==null) return null; 


		if (errors2D[0] > PamConstants.EARTH_RADIUS_METERS || errors2D[1] > PamConstants.EARTH_RADIUS_METERS) {
			return null; //don't draw infintie stuff - causes nasty errors. 
		}
//		System.out.println("Draw ovals on map"); 
//		System.out.println("EllipseLocErrorDraw: draw ellipse:"+errors2D[0]+" "+errors2D[1]+" "+Math.toDegrees(errors2D[2]));

		//System.out.println("Plot errors:  perp: "+ perpError+  " horz: "+horzError+ " " + errorDirection); 
		Graphics2D g2d = (Graphics2D)g;

		//draw oval
		//		//need to work out the size of the horizontal error. 
		//		perpError=Math.max(perpError, 100);
		//		horzError=Math.max(horzError, 50);

		double perpError=errors2D[0];
		double horzError=errors2D[1]; 
		double errorDirection=errors2D[2]; 
		
		/**
		 * Need to do all the error dimension calculation in true latlong coorinates, or it simply won't
		 * work with a rotated map. 
		 */
		Point errorOriginXY=generalProjector.getCoord3d(errorOrigin).getXYPoint();
		LatLong perpEnd = errorOrigin.travelDistanceMeters(errorDirection*180./Math.PI, perpError);
		LatLong horzEnd = errorOrigin.travelDistanceMeters(errorDirection*180./Math.PI + 90, horzError);
		Point perpEndPoint = generalProjector.getCoord3d(perpEnd).getXYPoint();
		Point horzEndPoint = generalProjector.getCoord3d(horzEnd).getXYPoint();
		double paintAngle = Math.PI/2.-Math.atan2(perpEndPoint.y-errorOriginXY.y, perpEndPoint.x-errorOriginXY.x);
		double perpErrPix = Math.sqrt(Math.pow(perpEndPoint.y-errorOriginXY.y,2) + Math.pow(perpEndPoint.x-errorOriginXY.x,2));
		double horzErrPix = Math.sqrt(Math.pow(horzEndPoint.y-errorOriginXY.y,2) + Math.pow(horzEndPoint.x-errorOriginXY.x,2));
		
		//draw the ellipse and rotate. 
		Ellipse2D oval=new Ellipse2D.Double(errorOriginXY.getX()-horzErrPix/2, errorOriginXY.getY()-perpErrPix/2, horzErrPix, perpErrPix);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 5 * 0.05f));
		g2d.setPaint(ellipseColor.brighter());

		if (!Double.isNaN(errorDirection)) g2d.rotate(-paintAngle, errorOriginXY.getX(), errorOriginXY.getY());
		g2d.draw(oval); 
		g2d.fill(oval); 

		//		AffineTransform transform=new AffineTransform(g2d.getTransform()); 

		//reset transparency. 
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		//need to reset the rotation
		if (!Double.isNaN(errorDirection)) g2d.rotate(paintAngle,errorOriginXY.getX(), errorOriginXY.getY());

		return new TransformShape(oval, paintAngle, new Point2D.Double(errorOriginXY.getX(), errorOriginXY.getY()));

	}

	public int getDrawType() {
		return drawType;
	}

}
