package alfa.swinggui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import alfa.ALFAControl;
import alfa.effortmonitor.AngleHistogram;
import alfa.effortmonitor.IntervalDataUnit;

/**
 * Draws the satellite summary data on the map for the summary module. This shows a ships 
 * track line and the summary localisation data. 
 * 
 * @author Doug Gillespie
 *
 */
public class IntervalOverlayDraw extends PanelOverlayDraw {

	private static PamSymbol defaultSymbol = new PamSymbol();
	private ALFAControl alfaControl;
	
	private double[] colourLims = new double[] {0, 400}; 

	private ColourArray colourArray;
	private boolean isViewer;

	int DRAW_SEGMENTS = 1;
	int DRAW_ARROWS = 2;
	int drawType = DRAW_ARROWS;

	PamSymbol nullSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.RED, Color.RED);

	public IntervalOverlayDraw(ALFAControl alfaControl) {
		super(defaultSymbol );
		this.alfaControl = alfaControl;
		
		//dodger blue, green, yellow, orange, red. 
//		colourArray = ColourArray.createMultiColouredArray(100, new Color(30, 144, 255), Color.YELLOW, Color.ORANGE, Color.RED);
		colourArray = ColourArray.createMultiColouredArray(100, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED);

		//		colourArray.setAlpha(255);
		//		colourArray.setAlpha(128);
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		
		checkColorLims();
	}
	
	/**
	 * Check the colour limits for click trains. 
	 */
	private void checkColorLims() {
		if (this.alfaControl.getAlfaParameters().useClkTrains) {
			colourLims[0] = 0; 
			colourLims[1] = 10; 
		}
		else {
			colourLims[0] = 0; 
			colourLims[1] = 400; 
		}
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
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		Rectangle r = null;
		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			r = drawOnMap(g, pamDataUnit, generalProjector);
		}
		return r;
	}

	private Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {

		IntervalDataUnit idu = (IntervalDataUnit) pamDataUnit;
		if (idu.getnClickTrains() == 0) {
			//			return null;
		}
		Rectangle r = drawGPSLine(g, idu, generalProjector);
		if (r == null) {
			//			r = drawEndPoints(g, idu, generalProjector);
		}
		ArrayList<AngleHistogram> angleHists = idu.getAngleHistograms();
		long defHistLength = alfaControl.getAlfaParameters().effortMsgIntervalNoWhales / 
				alfaControl.getAlfaParameters().histosPerReportInterval * 1000;
		synchronized (angleHists) {
			int iHist = 0;
			long histStart, histEnd;
			for (AngleHistogram angleHist : angleHists) {
				if (isViewer) {
					histStart = (long) (idu.getTimeMilliseconds() + idu.getDurationInMilliseconds()  / angleHists.size() * iHist);
					histEnd = (long) (idu.getTimeMilliseconds() + idu.getDurationInMilliseconds()  / angleHists.size() * (iHist+1));
				}
				else {
					histStart = idu.getTimeMilliseconds() + (iHist*defHistLength);
					histEnd = histStart+defHistLength;
				}
				drawAngleHistogram(g, idu, generalProjector, angleHist,  iHist,  histStart, histEnd);
				iHist++;
			}
		}

		return r;
	}

	private void drawAngleHistogram(Graphics g, IntervalDataUnit pamDataUnit, GeneralProjector generalProjector,
			AngleHistogram angleHist, int iAngleHist, long histStart, long histEnd) {


		Graphics2D g2d = (Graphics2D) g;
		
		//check colour limits. 
		checkColorLims();

		double[] data = angleHist.getData();
		
		if (data == null || data.length == 0) {
			return;
		}
		double angStep = 180/data.length;
		int[] polyX = new int[4];
		int[] polyY = new int[4];
		LatLong ll1 = findGpsPos(histStart, pamDataUnit);
		LatLong ll2 = findGpsPos(histEnd, pamDataUnit);
		Point coord1 = generalProjector.getCoord3d(ll1).getXYPoint();
		Point coord2 = generalProjector.getCoord3d(ll2).getXYPoint();
		LatLong lll = findGpsPos((histStart+histEnd)/2, pamDataUnit);
		Coordinate3d c3d0 = generalProjector.getCoord3d(lll);

		if (ll1 == null || ll2 == null || lll == null) return;
		double angPM = 6;
		double r = 5000;
		r = ll1.distanceToMetres(ll2)/2.;
		if (r == 0) {
			r = 1000;
		}
		/**
		 * Draw the trackline segment coloured by status. 
		 */
		int nWhale = 0;
		Color lineCol = PamColors.getInstance().getColor(PamColor.GPSTRACK);
		for (int i = 0; i < data.length; i++) {
			nWhale += data[i];
		}
		if (nWhale == 1) {
			lineCol = Color.ORANGE;
		}
		else if (nWhale >= 2) {
			lineCol = Color.RED;
		}
		g.setColor(lineCol);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(3));
		g.drawLine(coord1.x, coord1.y, coord2.x, coord2.y);
		//		if (data[i] == 0) {
//		nullSymbol.setFillColor(lineCol);
		nullSymbol.setLineColor(lineCol);
		nullSymbol.draw(g, c3d0.getXYPoint());
		//			continue;
		//		}

		g2d.setStroke(new BasicStroke(5));
		
		generalProjector.addHoverData(c3d0, pamDataUnit);

		for (int i = 0; i < data.length; i++) {
			//			data[i] = i+1;
			if (data[i] == 0) {
				continue;
			}
			//			if (ll1 == null || ll2 == null) {
			//				continue;
			//			}
			double[] angles;
			if (i == 0) {
				double[] aa = {0};
				angles = aa;
				//				ll2 = ll1;
				lll = ll2;
				angPM = angStep;
			}
			else if (i == data.length-1) {
				double[] aa = {180};
				angles = aa;
				//				ll1 = ll2;
				lll = ll1;
				angPM = angStep;
			}
			else {
				double ang = ((double) i + 0.5) * angStep;
				double[] aa = {-ang, ang};
				angles = aa;
				angPM = angStep/2;
			}
			angPM = 20;
			//			g.drawString("h", (int) c3d0.x, (int) c3d0.y);
			polyX[0] = (int) c3d0.x;
			polyY[0] = (int) c3d0.y;
			//			Coordinate3d c3d1 = generalProjector.getCoord3d(ll2);
			//			polyX[1] = (int) c3d1.x;
			//			polyY[1] = (int) c3d1.y;
			double shipHead = ll1.bearingTo(ll2);
			for (int s = 0; s < angles.length; s++) {
				double ang = shipHead + angles[s]-angPM;
				LatLong ll = lll.travelDistanceMeters(ang, r*.8);
				Coordinate3d c3d = generalProjector.getCoord3d(ll);
				polyX[1] = (int) c3d.x;
				polyY[1] = (int) c3d.y;

				ang = shipHead + angles[s];
				ll = lll.travelDistanceMeters(ang, r);
				c3d = generalProjector.getCoord3d(ll);
				polyX[2] = (int) c3d.x;
				polyY[2] = (int) c3d.y;

				ang = shipHead + angles[s]+angPM;
				ll = lll.travelDistanceMeters(ang, r*.8);
				c3d = generalProjector.getCoord3d(ll);
				polyX[3] = (int) c3d.x;
				polyY[3] = (int) c3d.y;

//				Color col = colourArray.checkColour((int) data[i]*2);
				Color col = colourArray.getColour(data[i], colourLims[0], colourLims[1]); 
				g.setColor(col);

				if (drawType == DRAW_SEGMENTS) {
					g.fillPolygon(polyX, polyY, 4);
					int xT = (polyX[2]+polyX[3])/2;
					int yT = (polyY[2]+polyY[3])/2;
					g.setColor(Color.black);
					g.drawString(String.format("%.0f", data[i]), xT, yT);
				}
				else if (drawType == DRAW_ARROWS) {
					g2d.drawLine(polyX[0], polyY[0], polyX[2], polyY[2]);
					g2d.drawLine(polyX[1], polyY[1], polyX[2], polyY[2]);
					g2d.drawLine(polyX[3], polyY[3], polyX[2], polyY[2]);
					int xT = (polyX[2]+polyX[3])/2;
					int yT = (polyY[2]+polyY[3])/2;
					g.setColor(Color.black);
					g.drawString(String.format("%.0f", data[i]), xT, yT);
				}
			}
		}
		g2d.setStroke(stroke);
	}

	/**
	 * find lat long either from striahg tline in data unit or from tru gps data. 
	 * @param histStart
	 * @param pamDataUnit
	 * @return
	 */
	private LatLong findGpsPos(long time, IntervalDataUnit pamDataUnit) {
		//		LatLong ll = findRealGPS(time);
		//		if (ll != null) {
		//			return ll;
		//		}
		double interpFac = (time - pamDataUnit.getTimeMilliseconds()) / pamDataUnit.getDurationInMilliseconds();
		interpFac = Math.max(0, Math.min(interpFac, 1.));
		LatLong ll1 = pamDataUnit.getFirstGPSData();
		LatLong ll2 = pamDataUnit.getLastGPSData();
		if (ll1 == null || ll2 == null) {
			return null;
		}
		LatLong ll = new LatLong(ll1.getLatitude() + (ll2.getLatitude()-ll1.getLatitude())*interpFac,
				ll1.getLongitude() + (ll2.getLongitude()-ll1.getLongitude())*interpFac);
		return ll;
	}

	private LatLong findRealGPS(long time) {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return null;
		}
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		if (gpsDataBlock == null) {
			return null;
		}
		GpsDataUnit gpsDataUnit = gpsDataBlock.getClosestUnitMillis(time);
		if (gpsDataUnit != null) {
			return gpsDataUnit.getGpsData();
		}
		else {
			return null;
		}
	}

	private Rectangle drawGPSLine(Graphics g, IntervalDataUnit idu, GeneralProjector generalProjector) {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return null;
		}
		GPSDataBlock gpsDataBlock = gpsControl.getGpsDataBlock();
		if (gpsDataBlock == null) {
			return null;
		}
		// see if we can find some GPS points in this range of track, if so draw a line between them. 
		List<Point> points = new LinkedList<>();
		Point lastPoint = new Point(-1000, -1000);
		synchronized (gpsDataBlock.getSynchLock()) {
			ListIterator<GpsDataUnit> iter = gpsDataBlock.getListIterator(idu.getTimeMilliseconds(), 0, PamDataBlock.MATCH_BEFORE, PamDataBlock.MATCH_BEFORE);
			if (iter == null) {
				return null;
			}
			while (iter.hasNext()) {
				GpsDataUnit gpsDataUnit = iter.next();
				if (gpsDataUnit.getTimeMilliseconds() > idu.getEndTimeInMilliseconds()) {
					break;
				}
				Point p = generalProjector.getCoord3d(gpsDataUnit.getGpsData()).getXYPoint();
				if (p.equals(lastPoint)) {
					continue;
				}
				points.add(p);
				lastPoint = p;
			}
		}
		if (points.isEmpty()) {
			return null;
		}
		lastPoint = points.get(0);
		g.setColor(Color.RED);
		ListIterator<Point> pIter = points.listIterator(1);
		int meanX = lastPoint.x, meanY = lastPoint.y;
		int minX, maxX, minY, maxY;
		minX = maxX = meanX;
		minY = maxY = meanY;
		while (pIter.hasNext()) {
			Point p = pIter.next();
			g.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
			lastPoint = p;
			meanX += p.x;
			meanY += p.y;
			minX = Math.min(minX, p.x);
			maxX = Math.max(maxX, p.x);
			minY = Math.min(minY, p.y);
			maxY = Math.max(maxY, p.y);
		}
		Coordinate3d meanPt = new Coordinate3d(meanX/points.size(), meanY/points.size());
		generalProjector.addHoverData(meanPt, idu);

		return new Rectangle(minX, minY, maxX-minX, maxY-minY);
	}

	/**
	 * Just draw a straight line between the end points of the segment. 
	 * @param g
	 * @param pamDataUnit
	 * @param generalProjector
	 * @return
	 */
	private Rectangle drawEndPoints(Graphics g, IntervalDataUnit idu, GeneralProjector generalProjector) {
		int nTrains = idu.getnClickTrains();
		if (nTrains == 0) {
			//			return null;
		}
		LatLong ll1 = idu.getFirstGPSData();
		LatLong ll2 = idu.getLastGPSData();
		if (ll1 == null || ll2 == null) {
			return null;
		}
		Coordinate3d c3d1 = generalProjector.getCoord3d(ll1);
		Coordinate3d c3d2 = generalProjector.getCoord3d(ll2);
		Point pt1 = c3d1.getXYPoint();
		Point pt2 = c3d2.getXYPoint();
		g.setColor(Color.RED);
		g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
		Rectangle r = new Rectangle(Math.min(pt1.y,  pt2.x), Math.min(pt1.y, pt2.y),
				Math.abs(pt1.y-pt2.x), Math.abs(pt1.y-pt2.y));
		generalProjector.addHoverData(c3d1, idu);
		generalProjector.addHoverData(c3d2, idu);
		return r;
	}


	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

}
