package group3dlocaliser.algorithm.toadmcmc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import Localiser.algorithms.locErrors.EllipseLocErrorDraw;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.ErrorEllipse;
import PamUtils.LatLong;
import PamUtils.PamArrayUtils;
import PamView.GeneralProjector;
import PamView.TransformShape;
import PamguardMVC.PamDataUnit;
import pamMaths.PamVector;


/**
 * Plots some point son the map 
 * @author Jamie Macaulay
 *
 */
public class MCMCErrorDraw extends EllipseLocErrorDraw {

	private MCMCEllipticalError ellipticalErr;

	MCMCErrorDraw(MCMCEllipticalError ellipticalError) {
		super(ellipticalError);
		this.ellipticalErr = ellipticalError; 
	}

	@Override
	public TransformShape drawOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, 
			GeneralProjector generalProjector, Color ellipseColor) {
		if (getEllipticalError() == null) {
			return null;
		}

		if (ellipticalErr.getPoints()!=null) {
			drawMCMCCloudOnMap(g, pamDetection, errorOrigin, generalProjector, ellipseColor);
		}

		switch(getDrawType()) {
		case DRAW_LINES:
			return drawLinesOnMap(g, pamDetection, errorOrigin, generalProjector, ellipseColor);
		case DRAW_OVALS:
			return drawOvalsOnMap(g, pamDetection, errorOrigin, generalProjector, ellipseColor);
		}


		return null;
	}


	public TransformShape drawMCMCCloudOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, 
			GeneralProjector generalProjector, Color ellipseColor) {


		//System.out.println("Plot errors:  perp: "+ perpError+  " horz: "+horzError+ " " + errorDirection); 
		Graphics2D g2d = (Graphics2D)g;

		ellipticalErr.getPoints(); 

		LatLong point1 = null;
		LatLong point2 = null; 
		Point point1xy = null;
		Point point2xy = null; 
		for (int i=0; i<ellipticalErr.getPoints().length-1; i++) {

			/**
			 * Need to do all the error dimension calculation in true latlong coorinates, or it simply won't
			 * work with a rotated map. 
			 */
			Point errorOriginXY=generalProjector.getCoord3d(errorOrigin).getXYPoint();

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,  0.05f));
			g2d.setPaint(ellipseColor.brighter().brighter());
			
			PamVector meanLoc = new PamVector(ellipticalErr.getMeanLoc()); 

			if (point1==null) {
				point1 = errorOrigin.addDistanceMeters(new PamVector(PamArrayUtils.float2Double(ellipticalErr.getPoints()[i])).sub(meanLoc));
				point1xy=generalProjector.getCoord3d(point1).getXYPoint();
			}

			point2 = errorOrigin.addDistanceMeters(new PamVector(PamArrayUtils.float2Double(ellipticalErr.getPoints()[i+1])).sub(meanLoc));
			point2xy=generalProjector.getCoord3d(point2).getXYPoint();

			//pixel points
			g2d.drawLine((int) point2xy.getX(), (int) point2xy.getY(), (int) point1xy.getX(),  (int)  point1xy.getY());

			point1=point2; //no need to recalculate that. 
			point1xy=point2xy; //no need to recalculate that. 


		}


		return null; 

	}

}
