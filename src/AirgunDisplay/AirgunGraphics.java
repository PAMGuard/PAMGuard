package AirgunDisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import GPS.GpsData;
import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.BasicKeyItem;
import PamView.GeneralProjector;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class AirgunGraphics extends PanelOverlayDraw  {

	AirgunControl airgunControl;
	
//	PamSymbol gunsSymbol;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 6, 6, true, Color.RED, Color.RED);

	public AirgunGraphics(AirgunControl airgunControl) {
		super(new PamSymbol(defaultSymbol));
		// TODO Auto-generated constructor stub
		this.airgunControl = airgunControl;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		return (parameterTypes[0] == GeneralProjector.ParameterType.LATITUDE && 
				parameterTypes[1] == GeneralProjector.ParameterType.LONGITUDE);
	}

	public PamKeyItem createKeyItem(GeneralProjector generalProjector,int keyType) {

		return new BasicKeyItem(getPamSymbol(null, generalProjector), airgunControl.getUnitName());
		
	}

	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		
		AirgunDataUnit airgunDataUnit = (AirgunDataUnit) pamDataUnit;

		double verticalRotation = 0.;
		if (generalProjector instanceof MapRectProjector) {
			MapRectProjector mapProj = (MapRectProjector) generalProjector;
			verticalRotation = mapProj.getMapVerticalRotationDegrees();
		}

//		if (verticalRotation == 0) {
//			return drawFlatMap(g, airgunDataUnit, generalProjector);
//		}
//		else {
			return drawRotatedMap(g, airgunDataUnit, generalProjector);
//		}
	}
	
	public Rectangle drawRotatedMap(Graphics g, AirgunDataUnit airgunDataUnit, GeneralProjector generalProjector) {
		GpsData gpsData = airgunDataUnit.getGpsData();
		MapRectProjector mapProj = (MapRectProjector) generalProjector;
		double verticalRotation = mapProj.getMapVerticalRotationDegrees();
		double yScale = Math.cos(Math.toRadians(verticalRotation));
		double radiusX = mapProj.getPixelsPerMetre()*airgunControl.airgunParameters.exclusionRadius;
		double radiusY = Math.max(radiusX * yScale, 0);
		
		double gunZ = -airgunControl.airgunParameters.gunDepth;
		
		// need to move from the gps position to the gund position. 
		LatLong gunsGps = gpsData.travelDistanceMeters(gpsData.getCourseOverGround() + 90, 
				airgunControl.airgunParameters.dimF); 
		gunsGps = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround() + 180, 
				airgunControl.airgunParameters.dimE); 
//		int mitigationRadius = airgunControl.airgunParameters.exclusionRadius;
		Coordinate3d gunsCentre = generalProjector.getCoord3d(gunsGps.getLatitude(), gunsGps.getLongitude(), gunZ);
		// draw the guns
		Point gc = gunsCentre.getXYPoint(); 
		PamSymbol gunsSymbol = getPamSymbol(airgunDataUnit, generalProjector);
		gunsSymbol.setFillColor(airgunControl.airgunParameters.exclusionColor);
		gunsSymbol.setLineColor(airgunControl.airgunParameters.exclusionColor);
		gunsSymbol.draw(g, gc);
		generalProjector.addHoverData(gunsCentre, airgunDataUnit);
		
		if (airgunControl.airgunParameters.showExclusionZone) {
//			LatLong rEnd = gunsGps.addDistanceMeters(mitigationRadius, 0);
//			
//			Coordinate3d gunsCirc = generalProjector.getCoord3d(rEnd.getLatitude(), rEnd.getLongitude(), 0);
//			double radius = Math.pow(gunsCentre.x-gunsCirc.x, 2) + 
//					Math.pow(gunsCentre.y-gunsCirc.y, 2);
			if (radiusX > 5) {
				g.drawOval((int) (gunsCentre.x - radiusX), (int) (gunsCentre.y - radiusY), 
						(int) (2*radiusX), (int) (2*radiusY));
			}
			
			if (airgunControl.airgunParameters.predictAhead) {
				double mapAngle = (int) (90 - gpsData.getCourseOverGround() - 90) - mapProj.getMapRotationDegrees();
				double predictionLengthMiles = airgunControl.airgunParameters.secondsAhead * 
					gpsData.getSpeed() / 3600;
				LatLong predictedPos = gunsGps.TravelDistanceMiles(gpsData.getCourseOverGround(), predictionLengthMiles);
				Coordinate3d pCirc = generalProjector.getCoord3d(predictedPos.getLatitude(), predictedPos.getLongitude(), gunZ);
//				int a1 = 90 - gpsData.getTrueCourse() + 90;
//				g.drawArc((int) (pCirc.x - radius), (int) (pCirc.y - radius), (int) radius*2, (int) radius*2, 
//						50, 180);
				float[] dashes = {2, 6};
				((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));

				g.drawOval((int) (pCirc.x - radiusX), (int) (pCirc.y - radiusY), 
						(int) (2*radiusX), (int) (2*radiusY));
//				g.drawArc((int) (pCirc.x - radiusX), (int) (pCirc.y - radiusY), (int) radiusX*2, (int) ((int) 2*radiusY), 
//								(int) mapAngle, 180);
				// now need to find the edge points of all this to link up to other circle
				LatLong ll1, ll2;
				Coordinate3d p1, p2;
				ll1 = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround()+90, 
						airgunControl.airgunParameters.exclusionRadius);
				ll2 = predictedPos.travelDistanceMeters(gpsData.getCourseOverGround()+90, 
						airgunControl.airgunParameters.exclusionRadius);
				p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), gunZ);
				p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), gunZ);
				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
				ll1 = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround()-90, 
						airgunControl.airgunParameters.exclusionRadius);
				ll2 = predictedPos.travelDistanceMeters(gpsData.getCourseOverGround()-90, 
						airgunControl.airgunParameters.exclusionRadius);
				p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), gunZ);
				p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), gunZ);
				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
//				g.setColor(Color.RED);
//				g.drawOval((int) (pCirc.x - radius), (int) (pCirc.y - radius), 
//						(int) (2*radius), (int) (2*radius));
				
				
				// put it back to a solid line, otherwise the next drawn object will be dashed !
				((Graphics2D) g).setStroke(new BasicStroke(1));
			}
		}
		
		return null;
	}
	public Rectangle drawFlatMap(Graphics g, AirgunDataUnit airgunDataUnit, GeneralProjector generalProjector) {
		
		GpsData gpsData = airgunDataUnit.getGpsData();
		
		// need to move from the gps position to the gund position. 
		LatLong gunsGps = gpsData.travelDistanceMeters(gpsData.getCourseOverGround() + 90, 
				airgunControl.airgunParameters.dimF); 
		gunsGps = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround() + 180, 
				airgunControl.airgunParameters.dimE); 
		int mitigationRadius = airgunControl.airgunParameters.exclusionRadius;
		Coordinate3d gunsCentre = generalProjector.getCoord3d(gunsGps.getLatitude(), gunsGps.getLongitude(), 0);
		// draw the guns
		Point gc = gunsCentre.getXYPoint(); 
		PamSymbol gunsSymbol = getPamSymbol(airgunDataUnit, generalProjector);
		gunsSymbol.setFillColor(airgunControl.airgunParameters.exclusionColor);
		gunsSymbol.setLineColor(airgunControl.airgunParameters.exclusionColor);
		gunsSymbol.draw(g, gc);
		generalProjector.addHoverData(gunsCentre, airgunDataUnit);
		
		if (airgunControl.airgunParameters.showExclusionZone) {
			LatLong rEnd = gunsGps.addDistanceMeters(mitigationRadius, 0);
			
			Coordinate3d gunsCirc = generalProjector.getCoord3d(rEnd.getLatitude(), rEnd.getLongitude(), 0);
			double radius = Math.pow(gunsCentre.x-gunsCirc.x, 2) + 
					Math.pow(gunsCentre.y-gunsCirc.y, 2);
			if (radius > 25) {
				radius = Math.sqrt(radius);
				
				g.drawOval((int) (gunsCentre.x - radius), (int) (gunsCentre.y - radius), 
						(int) (2*radius), (int) (2*radius));
			}
			
			if (airgunControl.airgunParameters.predictAhead) {
				double predictionLengthMiles = airgunControl.airgunParameters.secondsAhead * 
					gpsData.getSpeed() / 3600;
				LatLong predictedPos = gunsGps.TravelDistanceMiles(gpsData.getCourseOverGround(), predictionLengthMiles);
				Coordinate3d pCirc = generalProjector.getCoord3d(predictedPos.getLatitude(), predictedPos.getLongitude(), 0);
//				int a1 = 90 - gpsData.getTrueCourse() + 90;
//				g.drawArc((int) (pCirc.x - radius), (int) (pCirc.y - radius), (int) radius*2, (int) radius*2, 
//						50, 180);
				float[] dashes = {2, 6};
				((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dashes, 0));
				
				g.drawArc((int) (pCirc.x - radius), (int) (pCirc.y - radius), (int) radius*2, (int) radius*2, 
								(int) (90 - gpsData.getCourseOverGround() - 90),
								180);
				// now need to find the edge points of all this to link up to other circle
				LatLong ll1, ll2;
				Coordinate3d p1, p2;
				ll1 = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround()+90, 
						airgunControl.airgunParameters.exclusionRadius);
				ll2 = predictedPos.travelDistanceMeters(gpsData.getCourseOverGround()+90, 
						airgunControl.airgunParameters.exclusionRadius);
				p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), 0);
				p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), 0);
				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
				ll1 = gunsGps.travelDistanceMeters(gpsData.getCourseOverGround()-90, 
						airgunControl.airgunParameters.exclusionRadius);
				ll2 = predictedPos.travelDistanceMeters(gpsData.getCourseOverGround()-90, 
						airgunControl.airgunParameters.exclusionRadius);
				p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), 0);
				p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), 0);
				g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
//				g.setColor(Color.RED);
//				g.drawOval((int) (pCirc.x - radius), (int) (pCirc.y - radius), 
//						(int) (2*radius), (int) (2*radius));
				
				
				// put it back to a solid line, otherwise the next drawn object will be dashed !
				((Graphics2D) g).setStroke(new BasicStroke(1));
			}
		}
		
		return null;
	}

	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		String str =  "<html>" + airgunControl.getUnitName();
		if (airgunControl.airgunParameters.showExclusionZone) {
			str += "<br>Exclusion zone " + airgunControl.airgunParameters.exclusionRadius + " m";
		}
		str += "</html>";
		return str;
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}

	@Override
	public boolean showOptions(Window parentWindow,
			GeneralProjector generalProjector) {
		return false;
	}	
}
