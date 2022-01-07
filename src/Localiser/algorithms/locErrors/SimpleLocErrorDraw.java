package Localiser.algorithms.locErrors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.TransformShape;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;

public class SimpleLocErrorDraw implements LocErrorGraphics {
	
	/**
	 * The simple error. 
	 */
	private SimpleError simpleError;
	
//	Color col=Color.BLACK; 

	public SimpleLocErrorDraw(SimpleError simpleError){
		this.simpleError=simpleError; 
	}

	@Override
	public TransformShape drawOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin,
			GeneralProjector generalProjector, Color ellipseColor) {
		g.setColor(ellipseColor);
		if (simpleError == null) {
			return null;
		}
		plotErrors(g,  errorOrigin,  simpleError.getPerpAngle(), simpleError.getPerpError(), simpleError.getParallelError(), simpleError.getDepthError(), generalProjector); 		
		return null;
	}
	
	private Rectangle plotErrors(Graphics g, LatLong refPoint, Double refAngle, Double perpError, Double parallelError, Double depthError, GeneralProjector generalProjector) {

//		// col doesn't seem to be used - will probably have already been set when the symbol was drawn ? 
//		if (drawingOptions != null) {
//			col = drawingOptions.createColor(col, drawingOptions.getLineOpacity());
//			g.setColor(col);
//		}
		
		LatLong ll;
		Coordinate3d eS, eE;
		eS = generalProjector.getCoord3d(refPoint);
		Rectangle r = new Rectangle(eS.getXYPoint());
		double refBearing = (refAngle * 180 / Math.PI);
		if (perpError != null) {
			ll = refPoint.travelDistanceMeters(refBearing, perpError);
			eS = generalProjector.getCoord3d(ll);
			ll = refPoint.travelDistanceMeters(refBearing+ 180, perpError);
			eE = generalProjector.getCoord3d(ll);
			g.drawLine((int) eS.x, (int) eS.y, (int) eE.x, (int) eE.y); 
			r.add(eS.getXYPoint());
			r.add(eE.getXYPoint());
		}
		
		if (parallelError != null) {
			ll = refPoint.travelDistanceMeters(refBearing + 90, parallelError);
			eS = generalProjector.getCoord3d(ll);
			ll = refPoint.travelDistanceMeters(refBearing - 90, parallelError);
			eE = generalProjector.getCoord3d(ll);
			g.drawLine((int) eS.x, (int) eS.y, (int) eE.x, (int) eE.y); 
			r.add(eS.getXYPoint());
			r.add(eE.getXYPoint());
		}
		
		if (depthError != null) {
			ll = refPoint.clone();
			ll.setHeight(ll.getHeight() + depthError);
			eS = generalProjector.getCoord3d(ll);
			ll.setHeight(refPoint.getHeight() - depthError);
			eE = generalProjector.getCoord3d(ll);
			g.drawLine((int) eS.x, (int) eS.y, (int) eE.x, (int) eE.y); 
			r.add(eS.getXYPoint());
			r.add(eE.getXYPoint());
		}
		return r;
		
//		// refBEaring should be an angle in radians from the x axis (trig coordinates)
//		// convert this to a compass heading and get the positions of the ends. 
//		g.setColor(col);
//		double compassHeading = 90 - (refAngle * 180 / Math.PI);
//		Coordinate3d centre = generalProjector.getCoord3d(refPoint.getLatitude(), refPoint.getLongitude(), 0);
//		LatLong ll1 = refPoint.travelDistanceMeters(compassHeading, err1);
//		LatLong ll2 = refPoint.travelDistanceMeters(compassHeading+90, err2);
//		Coordinate3d p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), 0);
//		Coordinate3d p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), 0);
//		int cx = (int) centre.x;
//		int cy = (int) centre.y;
//		int dx = (int) (p1.x- centre.x);
//		int dy = (int) (p1.y- centre.y);
//		g.drawLine(cx + dx, cy - dy, cx - dx, cy + dy);
//		dx = (int) (p2.x- centre.x);
//		dy = (int) (p2.y- centre.y);
//		g.drawLine(cx + dx, cy - dy, cx - dx, cy + dy);
//		
//		return null;
	}

}
