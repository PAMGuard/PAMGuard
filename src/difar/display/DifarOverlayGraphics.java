package difar.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import difar.DIFARCrossingInfo;
import difar.DifarControl;
import difar.DifarDataBlock;
import difar.DifarDataUnit;
import pamMaths.PamVector;
import GPS.GpsData;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.BasicKeyItem;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.ProjectorDrawingOptions;
import PamView.GeneralProjector.ParameterType;
import PamView.dialog.IconPanel;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import Spectrogram.SpectrogramProjector;

public class DifarOverlayGraphics extends PamDetectionOverlayGraphics {

	private DifarDataBlock difarDataBlock;
	private DifarControl difarControl;
	private boolean isQueue;

	private float dash1[] = {5.0f, 5.f};
	private float dash2[] = {5.0f, 2f};
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.black, Color.black);

	public DifarOverlayGraphics(DifarControl difarControl, DifarDataBlock difarDataBlock, boolean isQueue) {
		super(difarDataBlock, new PamSymbol(defaultSymbol));
		this.difarControl = difarControl;
		this.difarDataBlock = difarDataBlock;
		this.isQueue = isQueue;
		this.setDefaultRange(10000);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		/**
		 * A few things to do here:
		 * 1. Colour lines by species
		 * 2. Make a key item to show different species
		 * 3. Improve on the tooltiptext. 
		 * 4. Get it showing on the spectrogram again. 
		 */
		if (difarControl.getPamController().getPamStatus() == difarControl.getPamController().PAM_LOADINGDATA)
			return null;
		
		DifarDataUnit difarDataUnit = (DifarDataUnit) pamDetection;
		if (difarDataUnit.isVessel() && difarControl.getDifarParameters().showVesselBearings == false) {
			return null;
		}
//		if (true)
//			return null;
		Graphics2D g2d = (Graphics2D) g;
		PamSymbol dataSymbol = getPamSymbol(difarDataUnit, generalProjector);

		Color col = dataSymbol.getLineColor();
		
		Rectangle rect = null;
		Rectangle bounds = null;
		Point p1 = null;
		Point p2 = null;
		//rr = drawLineAndSymbol(g, pamDetection, detectionCentre.getXYPoint(), 
		//endPoint.getXYPoint(), getPamSymbol(pamDetection));

		ProjectorDrawingOptions drawingOptions = generalProjector.getProjectorDrawingOptions();
		/* 
		 * Presently a hack to make bearing lines narrower 
		 * since lookupItems don't store lineThickness 
		 * independently for each lookupItem. -BSM
		 */
		dataSymbol.setLineThickness(difarControl.getDifarParameters().bearingLineWidth);
		BasicStroke defaultLine = new BasicStroke(dataSymbol.getLineThickness());
		BasicStroke latestLine = new BasicStroke(dataSymbol.getLineThickness()+2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		BasicStroke queueLine = new BasicStroke(dataSymbol.getLineThickness()+3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				1f, dash1, 0.0f);
//		difarControl.getDifarParameters().amplitudeScaledOpacity = true;
//		difarControl.getDifarParameters().timeScaledOpacity = false;
		if(difarControl.getDifarParameters().timeScaledOpacity){
			int minalpha = difarControl.getDifarParameters().minimumOpacity;
			int alpha = (int) Math.round(255.-(255.-minalpha)*((PamCalendar.getTimeInMillis()-pamDetection.getTimeMilliseconds())
					/(difarControl.getDifarParameters().timeToFade*60000.)));
			alpha=Math.max(alpha, minalpha);
			alpha=Math.min(alpha, 255);
			
			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ));
			dataSymbol.setLineColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ));
		}
		else if (difarControl.getDifarParameters().amplitudeScaledOpacity) {
			int minalpha = difarControl.getDifarParameters().minimumOpacity;
			int alpha = (int) Math.round(255 * ((difarDataUnit.getAmplitudeDB()-70)/50));
			alpha=Math.max(alpha, minalpha);
			alpha=Math.min(alpha, 255);
			dataSymbol.setSymbol(PamSymbolType.SYMBOL_POINT);
			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ));
			dataSymbol.setLineColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ));	
			
		} else {
			g.setColor(col);
		}

		Stroke oldStroke = g2d.getStroke();
		if (isQueue) {
			g2d.setStroke(queueLine);
		}
		else if (dataSymbol != null) {
			g2d.setStroke(defaultLine);
			if (difarDataUnit == difarDataBlock.getPreceedingUnit(PamCalendar.getTimeInMillis(),difarDataUnit.getChannelBitmap())){
				g2d.setStroke(latestLine);
			}
			if (difarDataUnit == difarControl.getCurrentDemuxedUnit()) {
				g2d.setStroke(queueLine);
			}
		}
//		g.drawLine(0, 0, 300, 300);

		GpsData detOrigin = difarDataUnit.getOriginLatLong(false);
		if (detOrigin == null) return null;
		double range = difarControl.getDifarProcess().rangeForDataType(difarDataUnit);
		Coordinate3d crossPoint = null;
		DIFARCrossingInfo crossInfo = difarDataUnit.getDifarCrossing();
		if (crossInfo == null) {
			crossInfo = difarDataUnit.getTempCrossing();
		}
		if (crossInfo != null) { // Draw a temporary crossing position with a dashed line
			LatLong crossLatLong = crossInfo.getCrossLocation();
			crossPoint = generalProjector.getCoord3d(crossLatLong.getLatitude(), crossLatLong.getLongitude(), 0);
			rect = dataSymbol.draw(g2d, crossPoint.getXYPoint());
			range = detOrigin.distanceToMetres(crossLatLong);
			generalProjector.addHoverData(crossPoint, pamDetection);
 
			bounds = rect;
			Double[] errors = crossInfo.getErrors();
//			FIXME: Errors is not being set in the viewer is causing a null pointer exception. 
//			For now disable in viewer mode, but really should fix the underlying cause. 
//			if (!difarControl.isViewer()){
				if (errors.length >= 2 && errors[0] != null && errors[1] != null && !Double.isNaN(errors[0]) && !Double.isNaN(errors[1])) {
					LatLong ell = crossLatLong.addDistanceMeters(errors[0], errors[1]);
					Coordinate3d errPoint = generalProjector.getCoord3d(ell.getLatitude(), ell.getLongitude(), 0);
					double dx = errPoint.x-crossPoint.x;
					double dy = errPoint.y-crossPoint.y;
					
					p1 = new Point((int) (crossPoint.x-dx), (int) (crossPoint.y));
					p2 = new Point((int) errPoint.x, (int) crossPoint.y);
					rect = drawLineOnly(g, difarDataUnit, p1, p2, dataSymbol, drawingOptions);
					bounds = updateMapBounds(bounds,rect);
					
//					g.drawLine((int) (crossPoint.x-dx), (int) (crossPoint.y), (int) errPoint.x, (int) crossPoint.y);
					p1.setLocation((int) (crossPoint.x), (int) (crossPoint.y-dy));
					p2.setLocation((int) crossPoint.x, (int) errPoint.y);
//					g.drawLine((int) (crossPoint.x), (int) (crossPoint.y-dy), (int) crossPoint.x, (int) errPoint.y);
					rect = drawLineOnly(g, difarDataUnit, p1, p2, dataSymbol, drawingOptions);
					bounds = updateMapBounds(bounds,rect);
					if (difarDataUnit.getDifarCrossing() != null) // return if this is an old crossbearing
						return bounds;
				}
//			}
		}
		// skip the default drawing and do it ourselves. 
		Double difarAngle = difarDataUnit.getSelectedAngle();
		Coordinate3d cenPoint = generalProjector.getCoord3d(detOrigin.getLatitude(), detOrigin.getLongitude(), 0);
		if (difarAngle == null) {
			rect = dataSymbol.draw(g2d, cenPoint.getXYPoint());
			bounds = updateMapBounds(bounds, rect);
			generalProjector.addHoverData(cenPoint, pamDetection);
		}
		else {
			double bearing = difarAngle + detOrigin.getHeading();
			LatLong endLatLong = detOrigin.travelDistanceMeters(bearing, range);
			Coordinate3d endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), 0);
			p1 = cenPoint.getXYPoint();
			p2 = endPoint.getXYPoint();
			if (difarControl.getDifarParameters().useMaxSourceLevel & !difarDataUnit.isVessel()){
				double startRange = difarControl.getDifarProcess().getWhaleRange(difarDataUnit, difarControl.getDifarParameters().maxSourceLevel);
				LatLong startLatLong = detOrigin.travelDistanceMeters(bearing, startRange);
				Coordinate3d startPoint= generalProjector.getCoord3d(startLatLong.getLatitude(), startLatLong.getLongitude(), 0);
				Point p0 = startPoint.getXYPoint();
				rect = drawLineOnly(g, difarDataUnit, p0, p2, dataSymbol, drawingOptions);
			} else {
				rect = drawLineOnly(g, difarDataUnit, p1, p2, dataSymbol, drawingOptions);
			}			
			bounds = updateMapBounds(bounds, rect);
//			g.drawLine((int) cenPoint.x, (int) cenPoint.y, (int) endPoint.x, (int) endPoint.y);
			
			generalProjector.addHoverData(endPoint, pamDetection);
			if (crossPoint != null) {
				p1 = crossPoint.getXYPoint();
				p2 = endPoint.getXYPoint();
				rect = drawLineOnly(g, difarDataUnit, p1, p2, dataSymbol, drawingOptions);
				bounds = updateMapBounds(bounds, rect);
//				g.drawLine((int) crossPoint.x, (int) crossPoint.y, (int) endPoint.x, (int) endPoint.y); 
			}
		}
		
		
		g2d.setStroke(oldStroke);
		return bounds;
	}
	
	private Rectangle updateMapBounds(Rectangle bounds, Rectangle rect){
		if (bounds == null) {
			return rect;
		}
		else {
			return bounds.union(rect);
		}
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#getPamSymbol(PamguardMVC.PamDataUnit)
	 */
	@Override
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		DifarDataUnit difarDataUnit = (DifarDataUnit) pamDataUnit;
		PamSymbol dataSymbol = difarControl.getSpeciesSymbol(difarDataUnit); 
		if (dataSymbol != null) {
			return dataSymbol;
		}
		return super.getPamSymbol(pamDataUnit, generalProjector);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#getHoverText(PamView.GeneralProjector, PamguardMVC.PamDataUnit, int)
	 */
	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		DifarDataUnit pd = (DifarDataUnit) dataUnit;
		return pd.getSummaryString();

	}
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// draw a rectangle with time and frequency bounds of detection.
		// spectrogram projector is now updated to use Hz instead of bins.
		DifarDataUnit difarDataUnit = (DifarDataUnit) pamDataUnit;
		PamSymbol dataSymbol = getPamSymbol(pamDataUnit, generalProjector); 
		BasicStroke queueLine = new BasicStroke(dataSymbol.getLineThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				1f, dash2, 0.0f);
		BasicStroke currentLine = new BasicStroke(dataSymbol.getLineThickness()+2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
				1f, dash1, 0.0f);

		Graphics2D g2d = (Graphics2D) g;
		Stroke oldStroke = g2d.getStroke();
		if (dataSymbol == null) {
			g.setColor(this.lineColour);
		}
		else {
			g.setColor(dataSymbol.getLineColor());
		}
		
		
		boolean hasBearing = false;
		// Make clips in the queue dashed -- Clips in the queue will not yet have a bearing
		if (difarDataUnit.getLocalisation() != null){
			hasBearing = difarDataUnit.getLocalisation().hasLocContent(LocContents.HAS_BEARING);
		}
		if (hasBearing == false){
				g2d.setStroke(queueLine);
		}else if (difarDataUnit == difarControl.getCurrentDemuxedUnit()) {
			g2d.setStroke(currentLine);
		}
		
		double[] frequency = difarDataUnit.getFrequency();		
		Coordinate3d topLeft = generalProjector.getCoord3d(difarDataUnit.getTimeMilliseconds(), 
				frequency[1], 0);
		Coordinate3d botRight = generalProjector.getCoord3d(difarDataUnit.getTimeMilliseconds() + 
				difarDataUnit.getSampleDuration() * 1000./difarDataBlock.getSampleRate(),
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


		g2d.drawRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
		Rectangle r1 = new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);

		double preMillis = difarDataUnit.getPreMarkSeconds() * 1000;
		if (preMillis > 0) {
			// now draw the second rectangle with a dotted line. 
//			Graphics2D g2d = (Graphics2D) g;

			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
					1f, dash1, 0.0f));	

			topLeft = generalProjector.getCoord3d(difarDataUnit.getTimeMilliseconds()-preMillis, 
					frequency[1], 0);
			botRight = generalProjector.getCoord3d(difarDataUnit.getTimeMilliseconds(),
					frequency[0], 0);
			g.drawRect((int) topLeft.x, (int) topLeft.y, 
					(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
			Rectangle r2 = new Rectangle((int) topLeft.x, (int) topLeft.y, 
					(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);

			g2d.setStroke(oldStroke);
			return r2.union(r1);
		}
		else {
			return r1;
		}

	}


	@Override
	protected Rectangle drawAmplitudeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
//		PamDetection pamDetection = (PamDetection) pamDataUnit;
		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || localisation.hasLocContent(LocContents.HAS_BEARING) == false) return null;

		GpsData detOrigin = ((DifarDataUnit) pamDataUnit).getOriginLatLong(false);
		if (detOrigin == null) return null;

		/*
		 * Try to get the new bearing information first, then if that fails, get 
		 * the old one. 
		 */
		double bearing = 0;
		double[] angleData = localisation.getPlanarAngles();
		bearing = Math.toDegrees(angleData[0]);
		bearing = bearing + detOrigin.getHeading();
		double amplitude = pamDataUnit.getAmplitudeDB();

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
	
	@Override
	protected Rectangle drawRangeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		DifarDataUnit difarDataUnit = (DifarDataUnit) pamDataUnit;
		AbstractLocalisation localisation = pamDataUnit.getLocalisation();
		if (localisation == null || localisation.hasLocContent(LocContents.HAS_BEARING | LocContents.HAS_RANGE) == false) return null;
		GpsData detOrigin = ((DifarDataUnit) pamDataUnit).getOriginLatLong(false);
		if (detOrigin == null) return null;
		double bearing, range;
		Rectangle r = null, newR;
		Coordinate3d c3d;
		ProjectorDrawingOptions drawOptions = generalProjector.getProjectorDrawingOptions();
		
		bearing =  difarDataUnit.getSelectedAngle() + detOrigin.getHeading();
		range = difarDataUnit.getRange();
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

//		PamSymbol symbol = ClickBTDisplay.getClickSymbol(clickDetector.getClickIdentifier(), click);
		return r;
	}

	
	
	@Override
	public PamKeyItem createKeyItem(GeneralProjector projector, int keyType) {
		if (projector.getParmeterType(0) == ParameterType.LATITUDE
				&& projector.getParmeterType(1) == ParameterType.LONGITUDE) {
			return createMapKey(keyType);
		} 
//		else if (projector.getParmeterType(0) == ParameterType.TIME
//				&& projector.getParmeterType(1) == ParameterType.FREQUENCY) {
//			return createSpectrogramKey(keyType);
//		} else if (projector.getParmeterType(0) == ParameterType.BEARING) {
//			return createRadarKey(keyType);
//		}
		return null;
	}
	
	protected PamKeyItem createMapKey(int keyType) {

		int nRows = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().size();
		if (nRows == 0) {
			return null;
		}
		if (isQueue) {
			PamSymbol queueSymbol = new PamSymbol();
			queueSymbol.setIconStyle(PamSymbol.ICON_STYLE_LINE);
			queueSymbol.setLineThickness(3.5f);
			return new BasicKeyItem(queueSymbol,difarDataBlock.getDataName());
		}
		int nCols = 3;
		BasicKeyItem basicKeyItem = new BasicKeyItem();
		IconPanel p = new IconPanel(nRows, nCols);
		for (int whaleId = 0; whaleId < nRows; whaleId++) {
			PamSymbol icon = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().get(whaleId).getSymbol();
			icon.setIconStyle(PamSymbol.ICON_STYLE_LINE | PamSymbol.ICON_STYLE_SYMBOL);
			icon.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_CENTRE);
			String text = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().get(whaleId).getText() 
					+ " (DIFAR)";
			basicKeyItem.addIcon(new IconPanel(icon), text);
		}
		return basicKeyItem;
	}

}
