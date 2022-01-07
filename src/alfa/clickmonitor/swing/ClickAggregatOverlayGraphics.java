package alfa.clickmonitor.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import alfa.clickmonitor.eventaggregator.AggregateLocalisation;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import alfa.clickmonitor.eventaggregator.AggregateLocalisation.AggLatLong;

public class ClickAggregatOverlayGraphics extends PamDetectionOverlayGraphics {

	/**
	 * Default symbol for click aggregate overalays
	 */
	private static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 9, 9, true, Color.BLACK, Color.RED);
	
	
	public ClickAggregatOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, defaultSymbol);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#canDraw(PamView.GeneralProjector)
	 */
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {
		ClickEventAggregate agg = (ClickEventAggregate) pamDetection;
		if (agg.getSubDetectionsCount() < 3) {
			return null;
//			return super.drawOnMap(g, pamDetection, generalProjector);
		}
		Graphics2D g2d = (Graphics2D) g;
		/*
		 *  need to make a complex hull for each side and try to draw them. 
		 *  Started coding a localisation - but probably don't want it as such. 
		 */
		AggregateLocalisation aggLoc = new AggregateLocalisation(agg.getSubDetection(0));
		for (int i = 1; i < agg.getSubDetectionsCount(); i++) {
			aggLoc.addLocalisation(agg.getSubDetection(i).getLocalisation());
		}
		AggLatLong[] aggShapes = aggLoc.getSideLatLongs();
		PamSymbol symbol = getPamSymbol(pamDetection, generalProjector);
		for (int i = 0; i < Math.min(1, aggShapes.length); i++) {
			AggLatLong all = aggShapes[i];
			if (all == null) {
				continue;
			}
			List<LatLong> outline = all.getOuterHull();
			int[] xPoints = new int[outline.size()];
			int[] yPoints = new int[outline.size()];
			for (int p = 0; p < outline.size(); p++) {
				LatLong ll = outline.get(p);
				Coordinate3d pt3 = generalProjector.getCoord3d(ll.getLatitude(), ll.getLongitude(), 0);
				xPoints[p] = (int) pt3.x;
				yPoints[p] = (int) pt3.y;
			}
			g.setColor(symbol.getLineColor());
			g.drawPolygon(xPoints, yPoints, yPoints.length);
			Color fillCol = new Color(0.2f,  .2f,  0.2f, 0.5f);//.getFillColor()
			g.setColor(fillCol);
			g2d.fillPolygon(xPoints, yPoints, yPoints.length);
		}
		
		return null;
	}

}
