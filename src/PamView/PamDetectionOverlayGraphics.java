package PamView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import GPS.GPSControl;
import GPS.GpsDataUnit;
import Map.MapRectProjector;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.Coordinate3d;
import PamUtils.FrequencyFormat;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import Spectrogram.SpectrogramProjector;
import pamMaths.PamVector;

public class PamDetectionOverlayGraphics extends PanelOverlayDraw {

	private PamDataBlock<PamDataUnit> parentDataBlock;

	protected PamSymbol pamSymbol;

	private static final int DEFSYMBOLSIZE = 8;

	/**
	 * The colour for lines
	 */
	protected Color lineColour = Color.BLACK;

	/**
	 * The colour for localisation symbols. 
	 */
	protected Color locColour = Color.YELLOW;


	private boolean drawLineToLocations = true;

	/**
	 * Default range for detections with bearing and no range information in metres. 
	 */
	private double defaultRange = 1000;

	private boolean isDetectionData;


	final static float dash1[] = {10.0f};

	final static BasicStroke dashedStroke = new BasicStroke(2.0f, 
			BasicStroke.CAP_BUTT, 
			BasicStroke.JOIN_MITER, 
			10.0f, dash1, 0.0f);

	final static BasicStroke solidStroke = new BasicStroke(2.0f);


	private Font depthFont = new Font("Arial", Font.PLAIN, 12);
	private Color depthColour = new Color(120,120,200);

	/**
	 * Constructor for standard overlay graphics class. Requires parent data block 
	 * as a parameter. 
	 * 
	 * @param parentDataBlock
	 */
	public PamDetectionOverlayGraphics(PamDataBlock parentDataBlock, PamSymbol defaultSymbol) {
		super(defaultSymbol);
		this.parentDataBlock = parentDataBlock;
		//		pamSymbol = new PamSymbol(PamSymbol.SYMBOL_CIRCLE, DEFSYMBOLSIZE, DEFSYMBOLSIZE, true, lineColour, lineColour);
		/*
		 * 
		 * work out if the base class is based on PamDetection or not - if not, then it's 
		 * not going to have localisation information available, but it will still have a time and a 
		 * symbol, so could be used for drawing on the map and possibly some other displays. 
		 * 
		 */
		Class unitClass = parentDataBlock.getUnitClass();
		isDetectionData = PamDetection.class.isAssignableFrom(unitClass);

		//		PamOldSymbolManager.getInstance().addManagesSymbol(this);

		/*
		 * Then check there is a symbol loaded. If not, create one now
		 */
		if (pamSymbol == null) {
			pamSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, DEFSYMBOLSIZE, 
					DEFSYMBOLSIZE, false, Color.BLACK, Color.BLACK);
		}

	}

	/**
	 * 
	 * @return parent PamDataBlock for this PanelOverlayDraw. 
	 */
	public PamDataBlock<PamDataUnit> getParentDataBlock() {
		return parentDataBlock;
	}

	/**
	 * Used to tell the various displays whether or not the data in the
	 * parentDataBlock can be drawn on each of those displays. This is 
	 * based purely on the axis types of the displays and whether or not
	 * the parentDataBlock's data untits are likely to have data which 
	 * can transform into those axis types. 
	 * <p>
	 * For simplicity I've broken it up into the three main display types 
	 * currently existing in Pamguard. 
	 */
	@Override
	public boolean canDraw(GeneralProjector generalProjector) {
		return canDraw(generalProjector.getParameterTypes(), generalProjector.getParameterUnits());
	}

	/**	 * Used to tell the various displays whether or not the data in the
	 * parentDataBlock can be drawn on each of those displays. This is 
	 * based purely on the axis types of the displays and whether or not
	 * the parentDataBlock's data untits are likely to have data which 
	 * can transform into those axis types.
	 * 
	 * @param parameterTypes parameter types
	 * @param parameterUnits parameter units. 
	 * @return true if the data can be (probably) drawn using this projector. 
	 */
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {

		/*
		 * Go through the different combinations of parameters and return true
		 * if we think it's likely we'll be able to draw on them based on information 
		 * in the pamDataBlock.
		 * 
		 * If new displays are added that support graphic overlay, then this list of if, else if's
		 * will need to be expanded. 
		 * 
		 */
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return canDrawOnMap();
		}
		else if (parameterTypes[0] == ParameterType.TIME
				&& parameterTypes[1] == ParameterType.FREQUENCY) {
			return canDrawOnSpectrogram();
		}
		else if (parameterTypes[0] == ParameterType.BEARING &&
				(parameterTypes[1] == ParameterType.AMPLITUDE ||
				parameterTypes[1] == ParameterType.RANGE ||
				parameterTypes[1] == ParameterType.SLANTANGLE)) {
			return canDrawOnRadar(parameterTypes[1]);
		}

		return false;
	}

	/**
	 * 
	 * @return true if these data can be drawn on the map. 
	 * <p> Thsi shoudl always be true, since it's always possible
	 * to draw a symbol at the hydrophone location even if no range
	 * or bearing information are available. 
	 */
	protected boolean canDrawOnMap() {
		return true;
		//		return (parentDataBlock.getLocalisationContents() != 0);
	}

	/**
	 * 
	 * @return true if these data can be drawn on the spectrogram. 
	 * <p> Generally, this is always true since it will just draw a 
	 * rectangle with the time and frequency limits of the sound. If 
	 * it is not the case, then override this function and return false. 
	 */
	protected boolean canDrawOnSpectrogram() {
		return true;
	}

	/**
	 * 
	 * @param radialParameter
	 * @return true if these data can be drawn on the radar. The detection will always need
	 * a bearing. The radial parameter is
	 * either amplitude (which all detections should have) or range which may or may not be there. 
	 */
	protected boolean canDrawOnRadar(GeneralProjector.ParameterType radialParameter) {

		if (!parentDataBlock.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING)) {
			return false;
		}

		if (radialParameter == ParameterType.AMPLITUDE) {
			return isDetectionData;
		}
		else if (radialParameter == ParameterType.RANGE) {
			return (parentDataBlock.getLocalisationContents().hasLocContent(LocContents.HAS_RANGE));
		}
		else if (radialParameter == ParameterType.SLANTANGLE) {
			return isDetectionData;
		}
		return false;
	}

	/**
	 * Gets information for making up a key on various displays. 
	 * PamKeyItem is not yet implemented.
	 */
	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return new PanelOverlayKeyItem(this);
	}

	/**
	 * Draw a PamDataUnit on a display. <p>
	 * This is split into separate routines for the three main display types for simplicity both 
	 * of reading this code and for overriding the various functions. <p>
	 * If display types are added to PAMGUARD, these functions will need to be added to. 
	 */
	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {

		if (!canDraw(generalProjector)) return null;
		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = null;
		pamSymbol = getPamSymbol(pamDataUnit, generalProjector);
		if (pamSymbol != null) {
			g.setColor(pamSymbol.getLineColor());
			oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(pamSymbol.getLineThickness(), BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_MITER));
		}
		else {
			g.setColor(lineColour);
		}

		Rectangle r = null;

		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			r = drawOnMap(g, pamDataUnit, generalProjector);
		}
		else if (generalProjector.getParmeterType(0) == ParameterType.TIME
				&& generalProjector.getParmeterType(1) == ParameterType.FREQUENCY) {
			r = drawOnSpectrogram(g, pamDataUnit, generalProjector);
		}
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING &&
				generalProjector.getParmeterType(1) == ParameterType.AMPLITUDE) {
			r = drawAmplitudeOnRadar(g, pamDataUnit, generalProjector);
		}
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING &&
				generalProjector.getParmeterType(1) == ParameterType.RANGE) {
			r = drawRangeOnRadar(g, pamDataUnit, generalProjector);
		}
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING &&
				generalProjector.getParmeterType(1) == ParameterType.SLANTANGLE) {
			r = drawSlantOnRadar(g, pamDataUnit, generalProjector);
		}
		if (oldStroke != null) {
			g2d.setStroke(oldStroke);
		}
		return r;
	}

	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {

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
		AbstractLocalisation localisation = pamDetection.getLocalisation();
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null) {
			return null;
		}
		
		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
		if (hydrophoneLocator == null) {
			return null;
		}

		//		LatLong centreLatLong = getDetectionCentre(pamDetection);
		LatLong centreLatLong = null;
		if (localisation != null) {
			centreLatLong = localisation.getOriginLatLong();
		}
		if (centreLatLong == null) {
//			centreLatLong = pamDetection.getOriginLatLong(false);
			centreLatLong = pamDetection.getSnapshotGeometry().getCentreGPS();
		}
		if (centreLatLong == null) {
			return null;
		}
		
		int chMap = pamDetection.getChannelBitmap();
		
		boolean drawLine = true;

		ProjectorDrawingOptions drawingOptions = generalProjector.getProjectorDrawingOptions();

		Coordinate3d detectionCentre = generalProjector.getCoord3d(centreLatLong.getLatitude(), centreLatLong.getLongitude(), centreLatLong.getHeight());
//				if (localisation != null) {
//					System.out.printf("Loc %s contents = 0X%X\n", localisation.toString(), localisation.getLocContents());
//				}
		if (localisation == null || 
				((localisation.getLocContents().getLocContent() & 
						(LocContents.HAS_BEARING | LocContents.HAS_RANGE)) == 0)) {
			// no localisation information, so draw the symbol and return.
			generalProjector.addHoverData(detectionCentre, pamDetection);
			return getPamSymbol(pamDetection, generalProjector).draw(g, detectionCentre.getXYPoint(), drawingOptions);
		}

		// here we know we have localisation information with range and or bearing. 
		double range, bearing, depth, referenceBearing;
		LatLong endLatLong = null, errLatLong1, errLatLong2;
		referenceBearing = 0;
		depth = 0;
		range = 0;
		bearing = 0;
		Coordinate3d endPoint;

		Rectangle bounds = null;
		Rectangle rr = null;
		
		if (localisation.getLocContents().hasLocContent(LocContents.HAS_LATLONG)) {
			drawLine = showLatLongLines(generalProjector);
			for (int i = 0; i < localisation.getAmbiguityCount(); i++) {
				endLatLong = localisation.getLatLong(i);

				//checks
				if (endLatLong == null) continue;
				if (Double.isNaN(endLatLong.getLatitude())) {
					continue;
				}

				endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), endLatLong.getHeight());
				if (localisation.getLocContents().hasLocContent(LocContents.HAS_DEPTH)) {
					localisation.getHeight(i);
					String depth_str = String.format("%.0f", -localisation.getHeight(i));
					if (localisation.getLocContents().hasLocContent(LocContents.HAS_DEPTHERROR)){
						depth_str+= String.format("\u00B1%.0f", localisation.getHeightError(i));
					}
//					depth_str+="m depth";
//					((Graphics2D) g).setFont(depthFont);
//					((Graphics2D) g).setColor(depthColour);
//					((Graphics2D) g).drawString(depth_str ,(int)endPoint.x+10, (int)endPoint.y);
				}
				PamSymbol symbol = getPerspectiveSymbol(pamDetection, generalProjector, endPoint);
				if (drawLine) {
				rr = drawLineAndSymbol(g, pamDetection, detectionCentre.getXYPoint(), 
						endPoint.getXYPoint(), symbol, drawingOptions);
				}
				else {
					rr = symbol.draw(g, endPoint.getXYPoint(), drawingOptions);
				}
				if (bounds == null) {
					bounds = rr;
				}
				else {
					bounds = bounds.union(rr);
				}

				generalProjector.addHoverData(endPoint, pamDetection);
				//plot localisation errors. 
				if (localisation.getLocError(i)!=null){
					
					Color col =symbol.getLineColor();
					if (drawingOptions != null) {
						col = drawingOptions.createColor(symbol.getLineColor(), drawingOptions.getLineOpacity());
					}
					TransformShape shape=localisation.getLocError(i).getErrorDraw().drawOnMap(g, pamDetection, endLatLong, generalProjector, col);
					generalProjector.addHoverData(shape, pamDetection);
				}
				else {
				}
				//				//20/07/2016. Old way of plotting errors before the introduction of an error class 
				//				if ((localisation.getLocContents() & AbstractLocalisation.HAS_PERPENDICULARERRORS) != 0) {
				//					perpError = localisation.getPerpendiculaError(i);
				//					parallelError = localisation.getParallelError(i);
				//										drawErrors(g, pamDetection, generalProjector, endLatLong, localisation.getErrorDirection(i), 
				//							parallelError, perpError, locColour);
				//					
				//				}
			}
			return bounds;
		}

		else if (localisation.getLocContents().hasLocContent(LocContents.HAS_BEARING)) {
			int n = 1;
			double[] surfaceAngles = localisation.getPlanarAngles(); // returns a single angle for planar arrays. 
			n = surfaceAngles.length;

			PamVector[] locVectors = localisation.getRealWorldVectors(); // need to get two angles back from this for a plane array
			if (locVectors != null) n = locVectors.length;
			//			if ((localisation.getLocContents() & AbstractLocalisation.HAS_AMBIGUITY) != 0) {
			//				n = 2;
			//			}
			for (int i = 0; i < n; i++) {
				range = getDefaultRange(generalProjector);
				if (localisation.getLocContents().hasLocContent(LocContents.HAS_RANGE)) {
					range = localisation.getRange(i);
					if (range>1E6) {
						range=getDefaultRange(generalProjector);
					}
					else {
						drawLine = showLatLongLines(generalProjector);
					}
				}
				/**
				 * The use of perspective seems to distort the end positions of lines that
				 * extend very far beyond the end of the map, so we need to hard limit this
				 * range value so that it only goes to not much more than the map 
				 * extent in order to avoid distortions when we're zoomed right in. 
				 */
				MapRectProjector mrProjector = (MapRectProjector) generalProjector;
				double mapRange = mrProjector.getMapRangeMetres();
				range = Math.min(range, mapRange*1.0);

				//				System.out.println("Range: " + range + " m, bearing " + bearing*180/Math.PI + " degrees.");
				// draw lines from the centreLatLong to the true location and put a symbol at the end of 
				// the line.
				if (locVectors != null && locVectors.length > i) {
					bearing = PamVector.vectorToSurfaceBearing(locVectors[i]);
					referenceBearing = 0;//localisation.getBearingReference();
					endLatLong = centreLatLong.travelDistanceMeters(Math.toDegrees(referenceBearing + bearing), 
							range * locVectors[i].xyDistance(), range * locVectors[i].getElement(2));
					//					endLatLong = centreLatLong.addDistanceMeters(locVectors[i].getElement(0)*range, 
					//							locVectors[i].getElement(1)*range);
				}
				else if (surfaceAngles != null && surfaceAngles.length > i) {
					bearing = surfaceAngles[i];
					referenceBearing = localisation.getBearingReference();
					endLatLong = centreLatLong.travelDistanceMeters(Math.toDegrees(referenceBearing + bearing), range);
				}
				if (endLatLong == null) {
					continue;
				}
				endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), endLatLong.getHeight());
				if (localisation.getLocContents().hasLocContent(LocContents.HAS_DEPTH)) {
					localisation.getHeight(0);
					String depth_str = String.format("%.0f m depth", localisation.getHeight(0));
					((Graphics2D) g).setFont(new Font("Arial", Font.PLAIN, 12));
					((Graphics2D) g).setColor(new Color(120,120,200));

					((Graphics2D) g).drawString(depth_str ,(int)endPoint.x+10, (int)endPoint.y);
				}
				PamSymbol symbol = getPerspectiveSymbol(pamDetection, generalProjector, endPoint);
				if (drawLine) {
				bounds = drawLineOnly(g, pamDetection, detectionCentre.getXYPoint(), 
						endPoint.getXYPoint(), symbol, drawingOptions);
				}
				else {
					bounds = symbol.draw(g, endPoint.getXYPoint(), drawingOptions);
				}
				generalProjector.addHoverData(endPoint, pamDetection);
			}
			return bounds;
		}
		else if (localisation.getLocContents().hasLocContent(LocContents.HAS_RANGE)) {
			// draw the symbol at the centre and a circle around it connected by a single line
			generalProjector.addHoverData(detectionCentre, pamDetection);
			Point p = detectionCentre.getXYPoint();
			endLatLong = centreLatLong.travelDistanceMeters(0, localisation.getRange(0));
			endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);
			int radius = endPoint.getXYPoint().y - p.y;

			getPamSymbol(pamDetection, generalProjector).draw(g, p, drawingOptions);
			//			g.setColor(lineColour); // no need - line type will have been set when the symbol was drawn. 
			g.drawLine(p.x, p.y, p.x, p.y - radius);
			g.drawOval(p.x-radius, p.y-radius, radius * 2, radius * 2);

		}
		return null;
	}


//	/**
//	 * Draw a localisation symbol. This is just a circel with a specified radius. 
//	 * @param g - graphics handle
//	 * @param pamDataUnit - the PAM data unit which holds the localisation that is being plotted/ 
//	 * @param originPoint - the origin of the localisation. 
//	 * @param locPoint - the location of the localisation
//	 * @return a rectangle which is the bounds of the localisation symbol. 
//	 */
//	protected Rectangle drawLocSymbol(Graphics g, PamDataUnit pamDataUnit, Point originPoint, Point locPoint, Color color){
//
//		//need some better graphics options- cast to Graphics2D. 
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.setPaint(color.brighter());
//
//		double width=10;
//		double height=10; 
//
//		Ellipse2D oval=new Ellipse2D.Double(originPoint.getX()-width/2, originPoint.getY()-height/2, width, height);
//		g2d.draw(oval); 
//		g2d.fill(oval); 
//
//		//draw line to origin- this is where loclaisation started. 
//		g2d.setStroke(dashedStroke);
//		g2d.drawLine(originPoint.x, originPoint.y, locPoint.x, locPoint.y);
//		//reset the stroke
//		g2d.setStroke(solidStroke);
//
//		//draw line to closest localisation point, make tranparent. 
//
//		return oval.getBounds(); 
//	}
//
//
//		/**
//		 * Plots errors as an ellipse. 
//		 * @param g - the graphics handle. 
//		 * @param errorOrigin - the error origin point. 
//		 * @param errorDirection. The direction of the perpendicular error. In RADIANS
//		 * @param perpError - the error perpendicular to the track line or (if no track line this is simply the error in x)
//		 * @param horzError - the error horizontal to the track line or (if no track line this is simply the error in x)
//		 * @param color
//		 * @return
//		 */
//		private Rectangle drawErrors(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector, LatLong errorOrigin, double errorDirection, double perpError, double horzError, Color color) {
//			
//			//System.out.println("Plot errors:  perp: "+ perpError+  " horz: "+horzError+ " " + errorDirection); 
//			
//			Graphics2D g2d = (Graphics2D)g;
//	
//			//draw oval
//	//		//need to work out the size of the horizontal error. 
//	//		perpError=Math.max(perpError, 100);
//	//		horzError=Math.max(horzError, 50);
//			
//			//must work out the horizontal and perpindicular error size in pixels using the projector
//			//this is a bit of round about way to do thinfs but best use of framework here. 
//			LatLong llperp=errorOrigin.addDistanceMeters(0, perpError); 
//			LatLong l2perp=errorOrigin.addDistanceMeters(horzError, 0); 
//		
//			Point pointPerp = generalProjector.getCoord3d(llperp.getLatitude(), llperp.getLongitude(), 0).getXYPoint();
//			Point pointHorz = generalProjector.getCoord3d(l2perp.getLatitude(), l2perp.getLongitude(), 0).getXYPoint();
//			Point errorOriginXY=generalProjector.getCoord3d(errorOrigin.getLatitude(), errorOrigin.getLongitude(), 0).getXYPoint();
//			
//			double perpErrPix=errorOriginXY.distance(pointPerp);
//			double horzErrPix=errorOriginXY.distance(pointHorz);
//	
//			//draw the ellipse and rotate if possible. 
//			Ellipse2D oval=new Ellipse2D.Double(errorOriginXY.getX()-horzErrPix/2, errorOriginXY.getY()-perpErrPix/2, horzErrPix, perpErrPix);
//			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 5 * 0.1f));
//			g2d.setPaint(color.brighter());
//	
//			if (!Double.isNaN(errorDirection)) g2d.rotate(errorDirection,errorOriginXY.getX(), errorOriginXY.getY());
//			g2d.draw(oval); 
//			g2d.fill(oval); 
//	
//			//reset transparency. 
//			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
//			//need to reset the rotation
//			if (!Double.isNaN(errorDirection)) g2d.rotate(-errorDirection,errorOriginXY.getX(), errorOriginXY.getY());
//			 
//			 
//			return oval.getBounds();
//			
//			
//	//		// refBEaring should be an angle in radians from the x axis (trig coordinates)
//	//		// convert this to a compass heading and get the positions of the ends. 
//	//		g.setColor(col);
//	//		double compassHeading = 90 - (refAngle * 180 / Math.PI);
//	//		Coordinate3d centre = generalProjector.getCoord3d(refPoint.getLatitude(), refPoint.getLongitude(), 0);
//	//		LatLong ll1 = refPoint.travelDistanceMeters(compassHeading, err1);
//	//		LatLong ll2 = refPoint.travelDistanceMeters(compassHeading+90, err2);
//	//		Coordinate3d p1 = generalProjector.getCoord3d(ll1.getLatitude(), ll1.getLongitude(), 0);
//	//		Coordinate3d p2 = generalProjector.getCoord3d(ll2.getLatitude(), ll2.getLongitude(), 0);
//	//		int cx = (int) centre.x;
//	//		int cy = (int) centre.y;
//	//		int dx = (int) (p1.x- centre.x);
//	//		int dy = (int) (p1.y- centre.y);
//	//		g.drawLine(cx + dx, cy - dy, cx - dx, cy + dy);
//	//		dx = (int) (p2.x- centre.x);
//	//		dy = (int) (p2.y- centre.y);
//	//		g.drawLine(cx + dx, cy - dy, cx - dx, cy + dy);
//	//		
//	//		return null;
//		}

	protected Rectangle drawLineAndSymbol(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector, LatLong LL1, LatLong LL2, PamSymbol symbol, ProjectorDrawingOptions drawingOptions) {
		return drawLineAndSymbol(g, pamDataUnit, generalProjector.getCoord3d(LL1.getLatitude(), LL1.getLongitude(), 0).getXYPoint(),
				generalProjector.getCoord3d(LL2.getLatitude(), LL2.getLongitude(), 0).getXYPoint(), symbol, drawingOptions);
	}

	protected Rectangle drawLineAndSymbol(Graphics g, PamDataUnit pamDataUnit, Point p1, Point p2, 
			PamSymbol symbol, ProjectorDrawingOptions drawingOptions) {
		Rectangle r = symbol.draw(g, p2, drawingOptions);
		if (drawLineToLocations) {
			Color col = symbol.getLineColor();
			if (drawingOptions != null) {
				col = drawingOptions.createColor(col, drawingOptions.getLineOpacity());
			}
			g.setColor(col);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			r = r.union(new Rectangle(p1.x-1, p1.y-1, 2, 2));
		}
		return r;
	}


	protected Rectangle drawLineAndSymbol(Graphics g, PamDataUnit pamDataUnit, Point p1, Point p2, 
			PamSymbol symbol, int symbolWidth, int symbolHeight, ProjectorDrawingOptions drawingOptions) {
		Rectangle r = symbol.draw(g, p2, symbolWidth, symbolHeight, drawingOptions);
		if (drawLineToLocations) {
			Color col = symbol.getLineColor();
			if (drawingOptions != null) {
				col = drawingOptions.createColor(col, drawingOptions.getLineOpacity());
			}
			g.setColor(col);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			r = r.union(new Rectangle(p1.x-1, p1.y-1, 2, 2));
		}
		return r;
	}

	protected Rectangle drawLineOnly(Graphics g, PamDataUnit pamDataUnit, Point p1, Point p2, PamSymbol symbol, ProjectorDrawingOptions drawingOptions) {
		Color col = symbol.getLineColor();
		if (drawingOptions != null) {
			col = drawingOptions.createColor(col, drawingOptions.getLineOpacity());
		}
		g.setColor(col);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		return new Rectangle(p1.x-1, p1.y-1, 2, 2);
	}

	/**
	 * 
	 * @param pamDetection PamDetection
	 * @return a map of hydrophones associated with this detection. 
	 */
	protected int getHydrophones(PamDataUnit pamDetection) { 

		int hydrophones = 0;
		AbstractLocalisation localisation = pamDetection.getLocalisation();
		if (localisation != null) {
			hydrophones = localisation.getReferenceHydrophones();
		}
		if (hydrophones == 0) {
			// annoying - will have to find the data souce and get the relationship between the software channels
			// listed in the detection and hydrophone mapping (not necessarily 1:1)
			AcquisitionProcess daqProcess = null;
			try {
				daqProcess = (AcquisitionProcess) parentDataBlock.getSourceProcess();
			}
			catch (Exception ex) {
				return 0;
			}
			if (daqProcess == null) {
				return 0;
			}
			hydrophones = daqProcess.getAcquisitionControl().ChannelsToHydrophones(pamDetection.getChannelBitmap());
		}
		return hydrophones;
	}

	/**
	 * Draw on spectrogram changed March 2010 so that the default time unit is 
	 * milliseconds (Java time from 1970) rather than samples. This makes it posible 
	 * to work with data collected over multiple files when operating in viewer mode. 
	 * @param g
	 * @param pamDataUnit
	 * @param generalProjector
	 * @return updated rectangle
	 */
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// draw a rectangle with time and frequency bounds of detection.
		// spectrogram projector is now updated to use Hz instead of bins. 
		if (!isDetectionData) return null;
		PamDataUnit pamDetection = pamDataUnit;	// originally cast pamDataUnit to PamDetection class
		
		
		double[] frequency = pamDetection.getFrequency();
		Coordinate3d topLeft = generalProjector.getCoord3d(pamDetection.getTimeMilliseconds(), 
				frequency[1], 0);
		Coordinate3d botRight = generalProjector.getCoord3d(pamDetection.getTimeMilliseconds() + 
				pamDetection.getDurationInMilliseconds(),
				frequency[0], 0);
				
		if (botRight.x < topLeft.x){
			botRight.x = g.getClipBounds().width;
		}
		if (generalProjector.isViewer()) {
			Coordinate3d middle = new Coordinate3d();
			middle.x = (topLeft.x + botRight.x)/2;
			middle.y = (topLeft.y + botRight.y)/2;
			middle.z = (topLeft.z + botRight.z)/2;
			generalProjector.addHoverData(middle, pamDataUnit);
		}
		//		int channel = PamUtils.getSingleChannel(pamDetection.getChannelBitmap());
		//		if (channel >= 0) {
		//			g.setColor(PamColors.getInstance().getChannelColor(channel));
		//		}
		//		g.setColor(lineColour);
		// Try to use a colour that will be nicely visible on the spectrogram.
		try {
			SpectrogramProjector spectrogramProjector = (SpectrogramProjector) generalProjector;
			g.setColor(spectrogramProjector.getSpectrogramDisplay().getColourArray().getContrastingColour());
		} catch(ClassCastException e){
			// Not actually drawing on a spectrogramProjector, so don't have any info on the background color
		}

		g.drawRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
		return new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
	}
	
	
	protected Rectangle drawAmplitudeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (!isDetectionData) return null;
		PamDataUnit pamDetection = pamDataUnit;	// originally cast pamDataUnit to PamDetection class
		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || !localisation.hasLocContent(LocContents.HAS_BEARING)) return null;
		/*
		 * Try to get the new bearing information first, then if that fails, get 
		 * the old one. 
		 */
		double bearing = 0;
		double[] angleData = localisation.getPlanarAngles();
		bearing = Math.toDegrees(angleData[0]);

		/*
		 * For debug purposes, try to work out what is going on with the array orientation
		 */
		//		PamVector[] v = localisation.getArrayOrientationVectors();
		//		if (v != null) {
		//
		//			Graphics2D g2d = (Graphics2D) g;
		//
		//			// g2d.setPaint(lineColor);
		//			// g2d.setColor(lineColor);
		//			Stroke oldStroke = g2d.getStroke();
		//			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,
		//					BasicStroke.JOIN_MITER));
		//			Coordinate3d plotCent = generalProjector.getCoord3d(0, 10000, 0);
		//			Point plotCentPt = plotCent.getXYPoint();
		//			PamVector av;
		//			for (int i = 0; i < v.length; i++) {
		//				av = v[i];
		//				if (i == 0) {
		//					g.setColor(Color.RED);
		//				}
		//				else if (i == 1) {
		//					g.setColor(Color.GREEN);
		//				}
		//				else if (i == 2) {
		//					g.setColor(Color.BLUE);
		//				}
		//				g.drawLine(plotCentPt.x, plotCentPt.y, 
		//						(int) (plotCentPt.x + 1000*av.getElement(0)), 
		//						(int) (plotCentPt.y-1000*av.getElement(1)));
		//			}
		//		}

		double amplitude = pamDetection.getAmplitudeDB();

		ProjectorDrawingOptions drawOptions = generalProjector.getProjectorDrawingOptions();

		PamVector[] worldVectors = localisation.getWorldVectors();
		Rectangle r = null;
		Coordinate3d c3d;
		if (worldVectors == null) {
			c3d = generalProjector.getCoord3d(bearing, amplitude, 0);
			if (c3d == null) return null;
			generalProjector.addHoverData(c3d, pamDataUnit);	
			r = getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint(), drawOptions);		
		}
		else {
			// slightly annoying, but will have to convert the vectors, which are in x,y,z coordinates into 
			// a bearing from North !
			double lastBearing = 9999999999.;
			for (int i = 0; i < worldVectors.length; i++) {
				bearing = Math.atan2(worldVectors[i].getElement(1), worldVectors[i].getElement(0));
				bearing = Math.toDegrees(bearing);
				bearing = 90.-bearing;
				if (bearing == lastBearing) {
					continue;
				}
				lastBearing = bearing;
				c3d = generalProjector.getCoord3d(bearing, amplitude, 0);
				if (c3d == null) continue;
				generalProjector.addHoverData(c3d, pamDataUnit);	
				r = getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint(), drawOptions);		
			}
		}
		return r;
	}
	protected Rectangle drawRangeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {

		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || !localisation.hasLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE)) return null;
		int nLocs = localisation.getAmbiguityCount();
		double bearing, range;
		Rectangle r = null, newR = null;
		Coordinate3d c3d;
		ProjectorDrawingOptions drawOptions = generalProjector.getProjectorDrawingOptions();
		
		if (localisation.hasLocContent(LocContents.HAS_LATLONG)) {
			for (int i = 0; i < nLocs; i++) {
				LatLong ll = localisation.getLatLong(i);
				/*
				 * Get the master reference for where we are now.
				 */
				LatLong lo = findCurrentOrigin(pamDataUnit);
				if (ll == null || lo == null) {
					continue;
				}
				bearing = lo.bearingTo(ll);
				double plotHeading = localisation.getBearingReference();
				bearing -= Math.toDegrees(plotHeading);
				range = lo.distanceToMetres(ll);
//				System.out.printf("Range and bearing are %3.1f and %3.1f\n", range, bearing);
				c3d = generalProjector.getCoord3d(bearing, range, 0);
				generalProjector.addHoverData(c3d, pamDataUnit);
				newR = getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint(), drawOptions);
			}
			return newR;
		}
		
		double[] angles = localisation.getAngles();
		for (int i = 0; i < nLocs; i++) {
			if (angles != null && angles.length > i) {
				bearing = Math.toDegrees(angles[i]);
			}
			else {
				bearing = localisation.getBearing(i) * 180 / Math.PI;
			}
			range = localisation.getRange(i);
			c3d = generalProjector.getCoord3d(bearing, range, 0);
			if (c3d == null) return null;
			generalProjector.addHoverData(c3d, pamDataUnit);
			newR = getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint(), drawOptions);
			if (r == null) {
				r = newR;
			}
			else {
				r = r.union(newR);
			}
		}
		//		PamSymbol symbol = ClickBTDisplay.getClickSymbol(clickDetector.getClickIdentifier(), click);
		return r;
	}
	
	/**
	 * Work out the best origin to use for this detection when 
	 * plotting from latlong data. 
	 * @param pamDataUnit data unit
	 * @return Origin lat long
	 */
	private LatLong findCurrentOrigin(PamDataUnit pamDataUnit) {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return pamDataUnit.getOriginLatLong(false);
		}
		GpsDataUnit gpsData = gpsControl.getShipPosition(PamCalendar.getTimeInMillis());
		if (gpsData != null) {
			return gpsData.getGpsData();
		}
		return pamDataUnit.getSnapshotGeometry().getCentreGPS();
	}

	protected Rectangle drawSlantOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {

		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || 
				!localisation.hasLocContent(LocContents.HAS_BEARING)) return null;
		double bearing, slantAngle;
		ProjectorDrawingOptions drawOptions = generalProjector.getProjectorDrawingOptions();
		Rectangle r = null, newR;
		Coordinate3d c3d;
		PamVector v[] = localisation.getWorldVectors();
		if (v == null | v.length == 0) {
			return null;
		}
		double[] vec;
		PamSymbol pamSymbol = getPamSymbol(pamDataUnit, generalProjector);
		for (int i = 0; i < v.length; i++) {
			vec = v[i].getVector();
			bearing = Math.atan2(vec[1],vec[0]);
			bearing = 90.-Math.toDegrees(bearing);
			slantAngle = Math.atan2(vec[2], v[i].xyDistance());
			slantAngle = Math.toDegrees(slantAngle);
			c3d = generalProjector.getCoord3d(bearing, slantAngle, 0);
			if (c3d == null) return null;
			generalProjector.addHoverData(c3d, pamDataUnit);
			newR = pamSymbol.draw(g, c3d.getXYPoint(), drawOptions);
			if (r == null) {
				r = newR;
			}
			else {
				r = r.union(newR);
			}
		}
		//		for (int i = 0; i < localisation.getNumLatLong(); i++) {
		//			bearing = localisation.getBearing(i) * 180 / Math.PI;
		////			range = localisation.getRange(i);
		//			slantAngle = 
		//			c3d = generalProjector.getCoord3d(bearing, slantAngle, 0);
		//			if (c3d == null) return null;
		//			generalProjector.addHoverData(c3d, pamDataUnit);
		//			newR = getPamSymbol(pamDataUnit).draw(g, c3d.getXYPoint());
		//			if (r == null) {
		//				r = newR;
		//			}
		//			else {
		//				r = r.union(newR);
		//			}
		//		}
		//		PamSymbol symbol = ClickBTDisplay.getClickSymbol(clickDetector.getClickIdentifier(), click);
		return r;
	}

	private File imFile;
	private BufferedImage smallImage;
	private static final int imHeight = 50;
	private static final int imWidth = 50;


	/**
	 * Get tool tip content for the data unit for this projector. 
	 * @param generalProjector projector
	 * @param dataUnit dataunit
	 * @param iSide left or right (0 or 1 I think, might be -1 or +1 though !)
	 * @return tooltip content consisting of text and  / or an image. 
	 */
	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		BufferedImage im = getHoverImage(generalProjector, dataUnit, iSide);
		//		String hoverText = getHoverTextWithoutWrap(generalProjector, dataUnit, iSide);
		String hoverText = dataUnit.getSummaryString();
		if (im != null) {
			try {
				if (imFile == null) {
					imFile = File.createTempFile("clipimage", ".jpg");
					imFile.deleteOnExit();
				}
				if (smallImage == null) {
					smallImage = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_RGB);
				}
				smallImage.createGraphics().drawImage(im, 0, 0, imWidth, imHeight, 0, 0, im.getWidth(), im.getHeight(), null);
				ImageIO.write(smallImage, "jpg", imFile);
				if (hoverText == null) {
					hoverText = String.format("<br><img src=\"%s\"><br>",  imFile.toURI());
				}
				else {
					hoverText = String.format("%s<br><img src=\"%s\"><br>",  hoverText, imFile.toURI());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		if (hoverText != null && hoverText.length() > 0 && !hoverText.startsWith("<html>")) {
			return "<html>" + hoverText + "</html>";
		}
		else {
			return hoverText;
		}
	}

	/**
	 * GEt an image for the data unit to include in overlay text. 
	 * @param generalProjector projector
	 * @param dataUnit dataunit
	 * @param iSide left or right (0 or 1 I think, might be -1 or +1 though !)
	 * @return a small image or null
	 */
	public BufferedImage getHoverImage(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return null;
	}

	public String getHoverTextWithoutWrap(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		String str = parentDataBlock.getDataName();
		str += String.format("<br> Start: %s %s", 
				PamCalendar.formatDate(dataUnit.getTimeMilliseconds()),
				PamCalendar.formatTime(dataUnit.getTimeMilliseconds(), true));
//		if (isDetectionData && dataUnit.getChannelBitmap() != 0) {
//			str += String.format("<br> Channels: %s", PamUtils.getChannelList(dataUnit.getChannelBitmap()));
		if (isDetectionData && dataUnit.getSequenceBitmap() != 0) {
			str += String.format("<br> Channels: %s", PamUtils.getChannelList(dataUnit.getSequenceBitmap()));
		}
		if (isDetectionData) {
			PamDataUnit pd = dataUnit;	// originally this was a cast to PamDetection
			double[] fRange = pd.getFrequency();
			boolean hasF = false;
			if (fRange != null) {
				for (int i = 0; i < fRange.length; i++) {
					if (fRange[i] > 0) {
						hasF = true;
						break;
					}
				}
			}
			if (hasF) {
				str += "<br>"+FrequencyFormat.formatFrequencyRange(pd.getFrequency(), true);
			}
		}
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc != null) {
			loc.getLocContents();
			if (loc.getLocContents().hasLocContent(LocContents.HAS_LATLONG)) {
				LatLong latLong = loc.getLatLong(iSide);
				str += String.format("<br> %s, %s", latLong.formatLatitude(), latLong.formatLongitude());
			}
			if (loc.getLocContents().hasLocContent(LocContents.HAS_BEARING)) {
				double[] surfaceAngles = loc.getPlanarAngles();
				if (surfaceAngles != null) {
					str += String.format("<br> Angle %4.1f\u00B0", Math.toDegrees(surfaceAngles[0]));
					//					if (surfaceAngles.length > 1) {
					//						str += String.format("  � %4.1f\u00B0", loc.getBearingError(iSide)*180/Math.PI);
					//					}
					if (loc.getAmbiguityCount() > 1) {
						str += "<br> (Left Right Ambiguity)";
					}
				}
			}
			if (loc.getLocContents().hasLocContent(LocContents.HAS_RANGE)) {
				str += String.format("<br> Range: %4.1f", loc.getRange(iSide));
				if (loc.getLocContents().hasLocContent(LocContents.HAS_RANGEERROR)) {
					str += String.format("  � %4.1f", loc.getRangeError(iSide));
				}
				str +="m";
			}

			if(loc.getLocContents().hasLocContent(LocContents.HAS_DEPTH)) {
				str += String.format("<br> Depth: %4.1f", -loc.getHeight(iSide));
				if (loc.getLocContents().hasLocContent(LocContents.HAS_DEPTHERROR)) {
					str += String.format("  � %4.1f", loc.getHeightError(iSide));
				}
				str +="m";
			}
			else str += new String("<br> Depth: No depth information");

			if(dataUnit.getLocalisation().getLocError(iSide)!=null) {
				str += "<br>Error Info: ";
				str += dataUnit.getLocalisation().getLocError(iSide).getStringResult();
			}
		}
		//		str += "</html>";
		return str;
	}

	public Color getLineColour() {
		return lineColour;
	}

	public void setLineColour(Color lineColour) {
		if (pamSymbol != null) {
			pamSymbol.setLineColor(lineColour);
		}
		this.lineColour = lineColour;
	}

	/**
	 * 
	 * @param pamDataUnit
	 * @return PamSymbol to use in plotting. Generally this is just the 
	 * set symbol for the overlay, but can be overridden if a detector has
	 * some complicated way of using different symbols for different dataUnits. 
	 */
	@Override
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector projector) {
		PamSymbolChooser symbolChooser = projector.getPamSymbolChooser();
		if (symbolChooser == null) {
			return pamSymbol;
		}
		else {
			return symbolChooser.getPamSymbol(projector, pamDataUnit);
		}
	}
	
	/**
	 * Get the symbol. 
	 * @param pamDataUnit
	 * @param projector
	 * @param screenCoordinate
	 * @return
	 */
	public PamSymbol getPerspectiveSymbol(PamDataUnit pamDataUnit, GeneralProjector projector, Coordinate3d screenCoordinate) {
		PamSymbol baseSymbol = getPamSymbol(pamDataUnit, projector);
		if (screenCoordinate.z == 0 || !(projector instanceof MapRectProjector) || baseSymbol == null) {
			return baseSymbol;
		}
		else {
			MapRectProjector rectProj = (MapRectProjector) projector;
			int newWidth = rectProj.symbolSizePerpective(baseSymbol.getWidth(), screenCoordinate);
			int newHeight = rectProj.symbolSizePerpective(baseSymbol.getHeight(), screenCoordinate);
			if (newWidth == baseSymbol.getWidth() && newHeight == baseSymbol.getHeight()) {
				return baseSymbol;
			}
			/**
			 * Clone since we don't want to modify the symbol itself since it is needed for th e
			 * next point and the one after ...
			 */
			baseSymbol = baseSymbol.clone();
			baseSymbol.setWidth(newWidth);
			baseSymbol.setHeight(newHeight);
			return baseSymbol;
		}
	}
	
	/**
	 * Options controlling if lines are shown even when an absolute latitude and longitude. 
	 * is available. 
	 * @param generalProjector - the general projector.
	 * @return true to show latitude and longitude lines. 
	 */
	private boolean showLatLongLines(GeneralProjector generalProjector) {
		if (generalProjector == null) {
			return true;
		}
		PamSymbolChooser symbolChooser = generalProjector.getPamSymbolChooser();
		if (symbolChooser instanceof StandardSymbolChooser) {
			return ((StandardSymbolChooser) symbolChooser).showLinesToLatLongs();
		}
		else {
			return true;
		}
	}

	public double getDefaultRange(GeneralProjector generalProjector) {
		if (generalProjector == null) {
			return defaultRange;
		}
		if (generalProjector.getPamSymbolChooser() == null) {
			return defaultRange;
		}
		PamSymbolChooser symbolChooser = generalProjector.getPamSymbolChooser();
		if (StandardSymbolChooser.class.isAssignableFrom(symbolChooser.getClass())) {
			return ((StandardSymbolChooser) symbolChooser).getMapLineLength();
		}
		else {
			return defaultRange;
		}
	}

	public void setDefaultRange(double defaultRange) {
		this.defaultRange = defaultRange;
	}

	/**
	 * 
	 * @return true if the datablock associated with this overlay contians data units
	 * subclassed from PamDetection - in which case they might have Localisation information. 
	 */
	public boolean isDetectionData() {
		return isDetectionData;
	}

	public void setDetectionData(boolean isDetectionData) {
		this.isDetectionData = isDetectionData;
	}

	public ManagedSymbolInfo getSymbolInfo() {
		return new ManagedSymbolInfo(this.getParentDataBlock().getDataName());
	}

	public PamKeyItem getMenuKeyItem() {
		return new PanelOverlayKeyItem(this);
	}

	public void setLineColor( Color c ) {
		this.lineColour = c;
	}

	public Color getLineColor() {
		return this.lineColour;
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

	/**
	 * Get the colour for localisation symbols on the map
	 * @return the colour of localisation symbols 
	 */
	public Color getLocColour() {
		return locColour;
	}

	/**
	 * Set the colour of localisation symbols on the map. 
	 * @param locColour
	 */
	public void setLocColour(Color locColour) {
		this.locColour = locColour;
	}

}
