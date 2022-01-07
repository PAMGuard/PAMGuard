package loc3d_Thode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.ProjectorDrawingOptions;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class TowedArray3DOverlayGraphics extends PamDetectionOverlayGraphics {

	TowedArray3DProcess towedArray3DProcess;
	int circle_size;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 16, 16, true, Color.RED, Color.GREEN);

	public TowedArray3DOverlayGraphics(TowedArray3DProcess towedArray3DProcess) {

		super(towedArray3DProcess.localizationDataBlock, new PamSymbol(defaultSymbol));

		this.towedArray3DProcess = towedArray3DProcess;


	}

	double maxCircleSize = 30;
	double minCircleSize = 3;
	double minDepth = 10;
	double maxDepth = 1500;
	
	public void setSymbolSize(double depth){
		if (depth <= minDepth) circle_size = (int) maxCircleSize;
		else if (depth >= maxDepth) circle_size = (int) minCircleSize;
		else {
			double dScale = 1.-(Math.log(depth/minDepth) / Math.log(maxDepth/minDepth));
		  circle_size = (int) (dScale * (maxCircleSize - minCircleSize) + minCircleSize);
		}
//		circle_size = Math.min(circle_size, 30);
		getDefaultSymbol().setHeight(circle_size);
		getDefaultSymbol().setWidth(circle_size);
//		setPamSymbol(new PamSymbol(PamSymbol.SYMBOL_CIRCLE, circle_size,circle_size, true, Color.RED, Color.GREEN));
	}

	//keep object on screen for a certain length of time...
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {
//		if (pamDetection.getTimeMilliseconds() < PamCalendar.getTimeInMillis() - 10000) {
//			return null;
//		}
//
//		if (true)
//			return null;

		/*
		 * 
		 * four possibilities here.
		 * 1) No localisation information so just draw the symbol at the hydrophone location
		 * 2) Bearing only - draw bearing lines of default range.
		 * 3) Range only - draw symbol and a circle around it
		 * 4) Bearing and Range - draw lines to symbol at correct location. 
		 * 5) LatLong - supersedes range and bearing
		 */

		// all need to start off by finding the position of the hydrophones at moment of
		// detection. This may be in the localisation info, if not, get it from the detection channels. 
		TowedArray3DDataUnit detection3D = (TowedArray3DDataUnit) pamDetection;
		AbstractLocalisation localisation = pamDetection.getLocalisation(); 
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) return null;
		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
		if (hydrophoneLocator == null) return null;

//		LatLong centreLatLong = getDetectionCentre(pamDetection);
		LatLong centreLatLong = detection3D.getOriginLatLong(true);
		if (centreLatLong == null) {
			return null;
		}
		
		ProjectorDrawingOptions drawingOptions = generalProjector.getProjectorDrawingOptions();

		Coordinate3d detectionCentre = generalProjector.getCoord3d(centreLatLong.getLatitude(), centreLatLong.getLongitude(), 0);

		if (localisation == null || 
				!localisation.getLocContents().hasLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE)) {
			// no localisation infomation, so draw the symbol and return.
			generalProjector.addHoverData(detectionCentre, pamDetection);
			return getPamSymbol(detection3D, generalProjector).draw(g, detectionCentre.getXYPoint());
		}
		// here we know we have localisation information with range and or bearing. 
		double range, bearing, depth, referenceBearing;
		LatLong endLatLong, errLatLong1, errLatLong2;
		referenceBearing = 0;
		depth = 0;
		range = 0;
		bearing = 0;
		Coordinate3d endPoint;

		Rectangle bounds = null;
		Rectangle rr;
		double perpError, parallelError;
		Coordinate3d errEndPoint1, errEndPoint2;
		if (localisation.getLocContents().hasLocContent(LocContents.HAS_LATLONG)) {
			for (int i = 0; i < localisation.getNumLatLong(); i++) {
				endLatLong = localisation.getLatLong(i);
				if (endLatLong == null) continue;
				endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);
				if (localisation.getLocContents().hasLocContent(LocContents.HAS_DEPTH)) {
					depth = localisation.getHeight(i);
					setSymbolSize(depth);
					String depth_str = String.format("%.0f m depth", localisation.getDepth());
					((Graphics2D) g).setFont(new Font("Arial", Font.PLAIN, 12));
					((Graphics2D) g).setColor(new Color(120,120,200));
					((Graphics2D) g).drawString(depth_str ,(int)endPoint.x+10, (int)endPoint.y);
					
				}
				rr = drawLineAndSymbol(g, pamDetection, detectionCentre.getXYPoint(), 
						endPoint.getXYPoint(), getPamSymbol(pamDetection, generalProjector), drawingOptions);
				if (bounds == null) {
					bounds = rr;
				}
				else {
					bounds = bounds.union(rr);
				}
				generalProjector.addHoverData(endPoint, pamDetection);
				if (localisation.getLocContents().hasLocContent(LocContents.HAS_PERPENDICULARERRORS)) {
					perpError = localisation.getPerpendiculaError(i);
					parallelError = localisation.getParallelError(i);
					//plotErrors(getPamSymbol().getLineColor(), endLatLong, 0, parallelError, perpError);
//					errLatLong1 = endLatLong.addDistanceMeters(longError, latError);
//					errLatLong2 = endLatLong.addDistanceMeters(-longError, -latError);
//					errEndPoint1 = generalProjector.getCoord3d(errLatLong1.getLatitude(), errLatLong1.getLongitude(), 0);
//					errEndPoint2 = generalProjector.getCoord3d(errLatLong2.getLatitude(), errLatLong2.getLongitude(), 0);
//					g.setColor(getPamSymbol().getLineColor());
//					g.drawLine((int) endPoint.x, (int) errEndPoint1.y, (int) endPoint.x, (int) errEndPoint2.y);
//					g.drawLine((int) errEndPoint1.x, (int) endPoint.y, (int) errEndPoint2.x, (int) endPoint.y);

				}
			}
			return bounds;
		}
		else if (localisation.getLocContents().hasLocContent(LocContents.HAS_BEARING)) {
			bearing = localisation.getBearing(0);
			if (Double.isNaN(bearing)) {
				return null;
			}
			referenceBearing = localisation.getBearingReference();
			range = getDefaultRange(generalProjector);

			if (localisation.getLocContents().hasLocContent(LocContents.HAS_RANGE)) {
				range = localisation.getRange(0);
				if (range>1E6)
					range=getDefaultRange(generalProjector);
			}

//			System.out.println("Range: " + range + " m, bearing " + bearing*180/Math.PI + " degrees.");
			// draw lines from the centreLatLong to the true location and put a symbol at the end of 
			// the line.

			endLatLong = centreLatLong.travelDistanceMeters((referenceBearing + bearing) * 180. / Math.PI, range);
			endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);

			if (localisation.getLocContents().hasLocContent(LocContents.HAS_DEPTH)) {
				depth = localisation.getHeight(0);
				setSymbolSize(depth);
				String depth_str = String.format("%.0f m depth", localisation.getHeight(0));
				((Graphics2D) g).setFont(new Font("Arial", Font.PLAIN, 12));
				((Graphics2D) g).setColor(new Color(120,120,200));

				((Graphics2D) g).drawString(depth_str ,(int)endPoint.x+10, (int)endPoint.y);

			}
			bounds = drawLineAndSymbol(g, pamDetection, detectionCentre.getXYPoint(), 
					endPoint.getXYPoint(), getPamSymbol(pamDetection, generalProjector), drawingOptions);
			generalProjector.addHoverData(endPoint, pamDetection);

			if (localisation.bearingAmbiguity()) {
				endLatLong = centreLatLong.travelDistanceMeters((referenceBearing - bearing) * 180. / Math.PI, range);
				endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);
				bounds = bounds.union(drawLineAndSymbol(g, pamDetection, detectionCentre.getXYPoint(), 
						endPoint.getXYPoint(), getPamSymbol(pamDetection, generalProjector), drawingOptions));
				generalProjector.addHoverData(endPoint, pamDetection);				
			}
			return bounds;
		}
		else if (localisation.getLocContents().hasLocContent(LocContents.HAS_RANGE)){
			// draw the symbol at the centre and a circle around it connected by a single line
			generalProjector.addHoverData(detectionCentre, pamDetection);
			Point p = detectionCentre.getXYPoint();
			endLatLong = centreLatLong.travelDistanceMeters(0, localisation.getRange(0));
			endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);
			int radius = endPoint.getXYPoint().y - p.y;

			getPamSymbol(pamDetection, generalProjector).draw(g, p);
			g.setColor(lineColour);
			g.drawLine(p.x, p.y, p.x, p.y - radius);
			g.drawOval(p.x-radius, p.y-radius, radius * 2, radius * 2);

		}
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return super.getHoverText(generalProjector, dataUnit, iSide);
	}

}


