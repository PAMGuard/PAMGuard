package meygenturbine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import GPS.GpsData;
import Map.MapPanel;
import Map.MapRectProjector;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;

public class MeygenGraphics extends PanelOverlayDraw {

	private static final double[] meygenBearings = {285., 90};
	private static final double shaftLength = 1.73;
	private static final double bladeRadius = 9.;
	private MeygenDataBlock meygenDataBlock;
	
	public MeygenGraphics(MeygenDataBlock meygenDataBlock, PamSymbol defaultSymbol) {
		super(defaultSymbol);
		this.meygenDataBlock = meygenDataBlock;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		ParameterType[] parameterTypes = generalProjector.getParameterTypes();
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return drawOnMap(g, (MeygenDataUnit) pamDataUnit, generalProjector);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Find the data unit closest in time to the scroller on the map
	 * @return closest unit in time to the map scroller. 
	 */
	private PamDataUnit findClosestDataUnit(MapRectProjector mapProj) {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return meygenDataBlock.getLastUnit();
		}
		else {
			MapPanel mapPanel = mapProj.getMapPanelRef();
			long millis = mapPanel.getSimpleMapRef().getViewerScroller().getValueMillis();
			return meygenDataBlock.getClosestUnitMillis(millis);
		}
	}

	private Rectangle drawOnMap(Graphics g, MeygenDataUnit dataUnit, GeneralProjector generalProjector) {
		if (!(generalProjector instanceof MapRectProjector)) {
			return null;
		}
		MapRectProjector mapProj = (MapRectProjector) generalProjector;
		PamDataUnit closestUnit = findClosestDataUnit(mapProj);
		if (dataUnit != closestUnit) {
			return null;
		}
		// all drawn relative to the streamer, base on which way the tide is flowing.
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		Streamer streamer = currentArray.getStreamer(0);
		GpsData streamerPos = streamer.getHydrophoneLocator().getStreamerLatLong(dataUnit.getTimeMilliseconds());
		double tHead;
		if (Math.cos(Math.toRadians(90.-dataUnit.getBearing())) > 0) {
			tHead = meygenBearings[0];
		}
		else {
			tHead = meygenBearings[1];
		}
		if (streamerPos == null) {
			return null;
		}
		LatLong hub = streamerPos.travelDistanceMeters(tHead, shaftLength);
		Point2D p1 = mapProj.getCoord3d(streamerPos).getPoint2D();
		Coordinate3d hubPoint = mapProj.getCoord3d(hub);
		Point2D p2 = hubPoint.getPoint2D();
		PamSymbol symbol = getPamSymbol(dataUnit, mapProj);
		g.setColor(symbol.getLineColor());
//		mapProj.addHoverData(new HoverData(hubPoint, dataUnit, 0, 0));
		mapProj.addHoverData(hubPoint, dataUnit);
		
		g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
		int nP = 36;
		int[] x = new int[nP];
		int[] y = new int[nP];
		for (int i = 0; i < nP; i++) {
			double a = 2*Math.PI/nP*i;
			LatLong tip = hub.travelDistanceMeters(tHead+90, bladeRadius*Math.cos(a));
			tip.setHeight(hub.getHeight() + bladeRadius*Math.sin(a));
			Point2D p3 = mapProj.getCoord3d(tip).getPoint2D();
			x[i] = (int) p3.getX();
			y[i] = (int) p3.getY();
		}
		g.drawPolygon(x, y, nP);
		if (symbol.isFill()) {
			Color col = symbol.getFillColor();
			col = new Color((float)col.getBlue()/65536.f, (float)col.getGreen()/65536.f, (float)col.getBlue()/65536.f, .1f);
			g.setColor(col);
		
			g.fillPolygon(x, y, nP);
		}
		/*
		 * Play  abit - draw the blades rotating. 
		 */
//		g.setColor(symbol.getLineColor());
//		long now = System.currentTimeMillis();
//		long rotPeriod = 6000;
//		double fracRot = (double)(now%rotPeriod)/(double)rotPeriod;
//		double a = fracRot*2*Math.PI;
//		for (int i = 0; i < 3; i++) {
//			LatLong tip = hub.travelDistanceMeters(tHead+90, bladeRadius*Math.cos(a));
//			tip.setHeight(hub.getHeight() + bladeRadius*Math.sin(a));
//			Point2D p3 = mapProj.getCoord3d(tip).getPoint2D();
//			a += Math.PI*2/3;
//			g.drawLine((int) p2.getX(), (int) p2.getY(), (int) p3.getX(), (int) p3.getY());
//		}
		
		
		
		
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

}
