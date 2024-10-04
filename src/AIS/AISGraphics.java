package AIS;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import GPS.GpsData;
import GPS.GpsDataUnit;
import Map.MapRectProjector;
import Map.Vessel;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import radardisplay.RadarProjector;


public class AISGraphics extends PamDetectionOverlayGraphics {

	//	PamSymbol aisSymbol = new PamSymbol(PamSymbol.SYMBOL_CIRCLE, 4, 4, true,
	//			Color.CYAN, Color.BLUE);

	private Vessel aisVessel;

	private GpsData aisGPSPosition;

	private AISControl aisControl;

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_NONE, 2, 2, 1, true, Color.green, Color.blue);

	private PamSymbol navAidSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.yellow, Color.yellow);

	//	AISControl aisControl;

	public AISGraphics(AISControl aisControl, AISDataBlock aisDataBlock) {

		super(aisDataBlock, new PamSymbol(defaultSymbol));

		this.aisControl = aisControl;

		aisVessel = new Vessel(Color.BLUE);
		//		
		//		setPamSymbol(aisVessel);

		aisGPSPosition = new GpsData();
	}

	@Override
	protected boolean canDrawOnRadar(ParameterType radialParameter) {
		// TODO Auto-generated method stub
		return super.canDrawOnRadar(radialParameter);
	}

	@Override
	protected Rectangle drawRangeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		AISDataUnit aisDataUnit = (AISDataUnit) pamDataUnit;
		AISStaticData staticData = aisDataUnit.getStaticData();
		AISPositionReport positionReport = aisDataUnit.getPositionReport();

		if (!aisDataUnit.isComplete()) {
			return null;
		}

		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || !localisation.hasLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE)) return null;
		double bearing = localisation.getBearing(0) * 180 / Math.PI;
		double range = localisation.getRange(0);
		Coordinate3d c3d = generalProjector.getCoord3d(bearing, range, 0);
		if (c3d == null) return null;
		generalProjector.addHoverData(c3d, pamDataUnit);
		//		PamSymbol symbol = ClickBTDisplay.getClickSymbol(clickDetector.getClickIdentifier(), click);
		RadarProjector radarProjector = (RadarProjector) generalProjector;
		double ppm = radarProjector.getPixelsPerMetre();
		//		aisVessel.drawShip(g, c3d.getXYPoint(), ppm, 0);
		//		getPamSymbol(pamDataUnit).draw(g, c3d.getXYPoint());
		// heading must be given relative to our own.
		aisVessel.setVesselDimension(staticData.dimA, staticData.dimB, staticData.dimC, staticData.dimD);
		GpsDataUnit gpsDataUnit = radarProjector.getRadarDisplay().getLastGpsDataUnit();
		return aisVessel.drawShip(g, c3d.getXYPoint(), ppm, positionReport.courseOverGround - 
				gpsDataUnit.getGpsData().getCourseOverGround());
	}

	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		Rectangle r = null;
		AISDataUnit aisDataUnit = (AISDataUnit) pamDataUnit;
		AISParameters aisParameters = aisControl.aisParameters;

		AISStaticData staticData = aisDataUnit.getStaticData();
		AISPositionReport positionReport = aisDataUnit.getPositionReport();

		if (positionReport == null) {
			return r;
		}

		long now = PamCalendar.getTimeInMillis();

		int staticType = 0;
		if (staticData != null) staticType = staticData.shipType;
		PamSymbol plotSymbol = getSymbol(aisDataUnit.stationType, staticType);

		// need to bodge things a little to make a gps object in the right place,
		// then set the ship dimensions, then draw it.
		int runMode = PamController.getInstance().getRunMode();
		if (runMode == PamController.RUN_PAMVIEW) {
			positionReport = aisDataUnit.findPositionReport(now);
		}
		if (positionReport.timeMilliseconds < now - aisParameters.tailLength * 60 * 1000) {
			return null;
		}

		aisGPSPosition.setLatitude(positionReport.getLatitude());
		aisGPSPosition.setLongitude(positionReport.getLongitude());
		aisGPSPosition.setCourseOverGround(positionReport.courseOverGround);
		aisGPSPosition.setTrueHeading(positionReport.trueHeading);
		aisGPSPosition.setSpeed(positionReport.speedOverGround);
		aisGPSPosition.setTimeInMillis(positionReport.timeMilliseconds);


		//		aisGPSPosition.setSpeed(aisDataUnit.positionReport.)
		//		aisVessel.setShipGps(aisGPSPosition.getPredictedGPSData(PamCalendar.getTime()));
		if (aisVessel != null) {
			aisVessel.setFillColor(Color.BLUE);
			aisVessel.setLineColor(Color.BLUE);
			aisVessel.setShipGps(aisGPSPosition);
			if (staticData != null) {
				aisVessel.setVesselDimension(staticData.dimA, staticData.dimB,
						staticData.dimC,staticData.dimD);
			}
			else {
				aisVessel.setVesselDimension(0, 0, 0, 0);
			}
			if (positionReport.hasTrueHeading() && aisParameters.showPredictionArrow) {
				aisVessel.setPredictionArrow(aisParameters.predictionLength);			
			}
			else {
				aisVessel.setPredictionArrow(0);
			}
		}
		Coordinate3d c3d;

		// draw the track.
		Point p1 = null, p2;
		if (aisParameters.showTail) {
			long startTime = positionReport.timeMilliseconds - aisParameters.tailLength * 60 * 1000;
			g.setColor(new Color(192,255,192));
			ArrayList<AISPositionReport> pr = aisDataUnit.getPositionReports();
			if (pr.size() > 0) {
				for (int i = 0; i < pr.size(); i++) {
					positionReport = pr.get(i);
					if (positionReport.timeMilliseconds < startTime) {
						continue;
					}
					if (positionReport.timeMilliseconds > now) {
						break;
					}
					//					c3d = generalProjector.getCoord3d(positionReport.getLatitude(), positionReport.getLongitude(), 0);
					//					p1 = c3d.getXYPoint();
					//					positionReport = pr.get(i);
					c3d = generalProjector.getCoord3d(positionReport.getLatitude(), positionReport.getLongitude(), 0);
					p2 = c3d.getXYPoint();
					if (p1 != null) {
						g.drawLine(p1.x, p1.y, p2.x, p2.y);
					}
					p1 = p2;
				}
			}
		}
		GpsData shipGps = aisVessel.getShipGps(false);
		if (Math.abs(shipGps.getTimeInMillis()-PamCalendar.getTimeInMillis()) > 300*1000) {
			return r;
		}
		if (plotSymbol == aisVessel) {
			c3d = aisVessel.drawShip((Graphics2D) g, (MapRectProjector) generalProjector);
		}
		else {
			if (plotSymbol.getHeight() > 10) {
				plotSymbol.setHeight(10);
			}
			c3d = generalProjector.getCoord3d(positionReport.latLong.getLatitude(), 
					positionReport.latLong.getLongitude(), 0);
			plotSymbol.draw(g, c3d.getXYPoint());
		}
		p2 = c3d.getXYPoint();
		// finally draw the extraploation
		if (p1 != null && PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		generalProjector.addHoverData(c3d, aisDataUnit);

		return r;
	}



	private PamSymbol getSymbol(StationType stationType, int staticType) {
		if (stationType == null) {
			return getNavaidSymbol(0);
		}
		switch(stationType) {
		case A:
		case B:
			return aisVessel;
		case NAVAID:
			return getNavaidSymbol(staticType);
		case BASESTATION:
		case UNKNOWN:
			return aisVessel;
		}

		return getDefaultSymbol();
	}

	private PamSymbol getNavaidSymbol(int staticType) {
		PamSymbol mainSymbol = getDefaultSymbol();
		navAidSymbol.setHeight(mainSymbol.getHeight());
		navAidSymbol.setWidth(mainSymbol.getWidth());
		Color col = Color.yellow;
		switch(staticType) {
		case 13:
		case 15:
		case 24:
		case 26:
			col = Color.RED;
			break;
		case 14:
		case 16:
		case 25:
		case 27:
			col = Color.GREEN;
			break;
		}
		navAidSymbol.setFillColor(col);
		navAidSymbol.setLineColor(col);
		return navAidSymbol;
	}

	@Override
	public void setDefaultSymbol(PamSymbol pamSymbol) {
		// keep the same symbol, but reset it's colour and size
		// otherwise we'll end up drawing blobs. 
		if (aisVessel == null) {
			aisVessel = new Vessel(Color.BLUE);
		}
		aisVessel.setFillColor(pamSymbol.getFillColor());
		aisVessel.setLineColor(pamSymbol.getLineColor());
		aisVessel.setHeight(pamSymbol.getHeight());
		aisVessel.setWidth(pamSymbol.getWidth());
		aisVessel.setLineThickness(pamSymbol.getLineThickness());
		super.setDefaultSymbol(pamSymbol);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return super.createKeyItem(generalProjector, keyType);
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int side) {
		AISDataUnit aisDataUnit = (AISDataUnit) dataUnit;
		AISStaticData staticData = aisDataUnit.getStaticData();
		AISPositionReport positionReport = aisDataUnit.getPositionReport();
		if (positionReport == null) {
			return null;
		}
		String str = new String("<html>");
		if (staticData == null) {
			str += aisDataUnit.getStationtypeString() + " (unknown)";
		}
		else {
			str += aisDataUnit.getStationtypeString() + ": " + staticData.shipName;
			str += "<br>Destination: " + staticData.destination;
			str += "<br>Call Sign  : " + staticData.callSign;
			if (staticData.imoNumber > 0) {
				str += "<br>imo : " + staticData.imoNumber;
			}
			else {
				str += "<br>imo unknown";
			}
		}
		str += "<br>Last position : " +PamCalendar.formatTime(positionReport.timeMilliseconds) + " UTC";
		str += "<br>mmsi : " + aisDataUnit.mmsiNumber;
		//str += "<br>ETA        : " + PamCalendar.formatDateTime(aisDataUnit.staticData.etaMilliseconds);
		str += String.format("<br>%s   %s", LatLong.formatLatitude(positionReport.getLatitude()),
				LatLong.formatLongitude(positionReport.getLongitude()));
		str += String.format("<br>Speed      : %3.1f", positionReport.speedOverGround);
		str += String.format("<br>Course      : %d\u00B0", (int) positionReport.courseOverGround);
		str += String.format("<br>Head        : %d\u00B0", (int) positionReport.trueHeading);
		if (staticData != null) {
			str += String.format("<br>LOA %dm, Beam %dm", (int)staticData.getLength(), (int) staticData.getWidth());
			str += String.format("<br>%s", staticData.getStationTypeString(aisDataUnit.stationType, staticData.shipType));
		}
		str += "</html>";
		return str;
	}

}
