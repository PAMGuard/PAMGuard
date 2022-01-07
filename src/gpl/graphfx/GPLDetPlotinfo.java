package gpl.graphfx;



import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import PamDetection.AbstractLocalisation;
import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.HoverData;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDManagedSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import gpl.GPLControlledUnit;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import gpl.GPLStateDataUnit;
import gpl.contour.GPLContour;
import gpl.contour.GPLContourPoint;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import pamViewFX.fxNodes.PamSymbolFX;

public class GPLDetPlotinfo extends TDDataInfoFX {

	private GPLControlledUnit gplControlledUnit;
	private GPLDetectionBlock gplDetectionBlock;
	
	private GPLDetSettingsPaneFX settingPane;
	
	private TDSymbolChooserFX managedSymbolChooser;

	private ScaleInfo scaleInfo;
	private TDScaleInfo bearingScaleInfo;
	public GPLDetPlotinfo(TDDataProviderFX tdDataProvider, GPLControlledUnit gplControlledUnit, TDGraphFX tdGraph, GPLDetectionBlock gplDetectionBlock) {
		super(tdDataProvider, tdGraph, gplDetectionBlock);
		this.gplControlledUnit = gplControlledUnit;
		this.gplDetectionBlock = gplDetectionBlock;
		scaleInfo = new ScaleInfo();
		settingPane = new GPLDetSettingsPaneFX(gplControlledUnit, this);
		getScaleInfos().add(scaleInfo);
		bearingScaleInfo = new TDScaleInfo(180, 0, ParameterType.BEARING, ParameterUnits.DEGREES);
		getScaleInfos().add(bearingScaleInfo);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		ParameterType dataType = getScaleInfo().getDataType();
		switch (dataType) {
		case AMPLITUDE:
			break;
		case BEARING:
			AbstractLocalisation loc = pamDataUnit.getLocalisation();
			if (loc != null) {
				double[] angles = loc.getAngles();
				if (angles != null && angles.length > 0) {
					return angles[0] * 180/Math.PI;
				}
			}
			break;
		case FREQUENCY:
			break;
		case ICI:
			break;
		case LATITUDE:
			break;
		case LONGITUDE:
			break;
		case RANGE:
			break;
		case SLANTANGLE:
			break;
		case TIME:
			break;
		default:
			break;
		
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#shouldDraw(int, int)
	 */
	@Override
	public boolean shouldDraw(int plotNumber, int sequenceMap) {
		ParameterType dataType = getScaleInfo().getDataType();
		switch (dataType) {
		case BEARING:
			return true;
		}
		return super.shouldDraw(plotNumber, sequenceMap);
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		if (managedSymbolChooser == null) {
			managedSymbolChooser = createSymbolChooser();
		}
		return managedSymbolChooser;
	}

	private TDSymbolChooserFX createSymbolChooser() {
		PamSymbolManager symbolManager = getDataBlock().getPamSymbolManager();
		if (symbolManager == null) {
			return null;
		}
		GeneralProjector p = this.getTDGraph().getGraphProjector();
		PamSymbolChooser sc = symbolManager.getSymbolChooser(getTDGraph().getUniqueName(), p);
		return new TDManagedSymbolChooserFX(this, sc, TDSymbolChooserFX.DRAW_SYMBOLS);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		ParameterType dataType = getScaleInfo().getDataType();
		if (dataType == ParameterType.FREQUENCY) {
			return drawOnSpectrogram(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}
		else {
			return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}
	}

	public Polygon drawOnSpectrogram(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		GPLDetection gplDetection = (GPLDetection) pamDataUnit;
		GPLContour contour = gplDetection.getContour();
		if (contour != null) {
			return drawContour(plotNumber, gplDetection, g, scrollStart, tdProjector, type);
		}
		else {
			return drawFrequencyBox(plotNumber, gplDetection, g, scrollStart, tdProjector, type);
		}
	}
	

	public Polygon drawContour(int plotNumber, GPLDetection gplDetection, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		/*
		 * Make a pixelated patch, then stretch it onto the display somehow. 
		 */
		GPLContour contour = gplDetection.getContour();
		ArrayList<GPLContourPoint> contourPoints = contour.getContourPoints();
		int[][] outline = contour.getOutline(0);
		double dT = gplDetection.getTimeRes();
		double dF = gplDetection.getFreqRes();
//		double binLo = gplDetection.getBinLo();
		int nX = outline.length;
		int xPixMin, xPixMax;
		int yPixMin, yPixMax;
		xPixMin = outline[0][0];
		xPixMax = outline[nX-1][0];
		yPixMin = contour.getMinFBin();
		yPixMax = contour.getMaxFBin();
		int imWid = xPixMax-xPixMin+1;
		int imHei = yPixMax-yPixMin+1;
		WritableImage fxImage = new WritableImage(imWid, imHei);
		PixelWriter imageRaster = fxImage.getPixelWriter();
		for (int i = 0; i < imWid; i++) {
			for (int j = 0; j < imHei; j++) {
				imageRaster.setArgb(i,j,0);
			}
		}
		PamSymbolFX symbol = getSymbolChooser().getPamSymbol(gplDetection, TDSymbolChooserFX.NORMAL_SYMBOL);
		Color col = symbol.getFillColor();
//		col = Color.RED;
		for (Point p : contourPoints) {
			try {
				imageRaster.setColor(p.x-xPixMin, p.y-yPixMin, col);
			}
			catch (Exception e) {
			}
		}
		double t0 = gplDetection.getTimeMilliseconds();
		int x0 = xPixMin;
		double t1 = t0 + (xPixMin-x0)*dT*1000; 
		double f1 = dF * (yPixMin);
		double t2 = t0 + (xPixMax-x0)*dT*1000; 
		double f2 = dF * (yPixMax+1);
		Coordinate3d c1 = tdProjector.getCoord3d(t1, f1, 0);
		Coordinate3d c2 = tdProjector.getCoord3d(t2, f2, 0);
//		Coordinate3d c1 = tdProjector.getCoord3d(gplDetection.getTimeMilliseconds(), freq[0], 0);
//		Coordinate3d c2 = tdProjector.getCoord3d(gplDetection.getEndTimeInMilliseconds(), freq[1], 0);
		if (c2.x >= c1.x) {
			g.drawImage(fxImage, 0, 0, fxImage.getWidth(), fxImage.getHeight(), c1.x, c1.y, c2.x-c1.x, c2.y-c1.y);
		}
		else {
			double w = g.getCanvas().getWidth();
			c2.x += w;
			g.drawImage(fxImage, 0, 0, fxImage.getWidth(), fxImage.getHeight(), c1.x, c1.y, c2.x-c1.x, c2.y-c1.y);
			c2.x -= w;
			c2.x -= w;
			g.drawImage(fxImage, 0, 0, fxImage.getWidth(), fxImage.getHeight(), c1.x, c1.y, c2.x-c1.x, c2.y-c1.y);
		}
		
		
		
		return null;
	}
	public Polygon drawFrequencyBox(int plotNumber, GPLDetection gplDetection, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		double[] freq = gplDetection.getFrequency();
		Coordinate3d c1 = tdProjector.getCoord3d(gplDetection.getTimeMilliseconds(), freq[0], 0);
		Coordinate3d c2 = tdProjector.getCoord3d(gplDetection.getEndTimeInMilliseconds(), freq[1], 0);
		PamSymbolFX symbol = getSymbolChooser().getPamSymbol(gplDetection, TDSymbolChooserFX.NORMAL_SYMBOL);
		if (symbol == null) {
			return null;
		}
		g.setStroke(symbol.getLineColor());
		g.setLineWidth(symbol.getLineThickness());
		
//		boolean canPlot = canPlotDataUnit(plotNumber, pamDataUnit);
		
		if (c1.x < c2.x) {
			// all is normal, draw a rectangle. 
			g.strokeRect(c1.x, Math.min(c1.y, c2.y), c2.x-c1.x, Math.abs(c2.y-c1.y));
			Rectangle r = new Rectangle((int) c1.x, (int) Math.min(c1.y, c2.y), (int)(c2.x-c1.x), (int)Math.abs(c2.y-c1.y));
			tdProjector.addHoverData(new HoverData(r, gplDetection, 0, plotNumber));
		}
		else {
			// we're in wrap mode and it's looping round so draw two half rectangles. 
			int pad = 20;
			g.strokeRect(c1.x, Math.min(c1.y, c2.y), tdProjector.getWidth()-c1.x+pad, Math.abs(c2.y-c1.y));
			g.strokeRect(-pad, Math.min(c1.y, c2.y), c2.x + pad, Math.abs(c2.y-c1.y));
		}

		return null;
	}


	private class ScaleInfo extends TDScaleInfo {
	
		public ScaleInfo() {
			super(0, gplDetectionBlock.getSampleRate()/2, ParameterType.FREQUENCY, ParameterUnits.HZ);
		}
		
	}
//
//	/* (non-Javadoc)
//	 * @see dataPlotsFX.data.TDDataInfoFX#getScaleInfo()
//	 */
//	@Override
//	public TDScaleInfo getScaleInfo() {
//		return scaleInfo;
//	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getGraphSettingsPane()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return settingPane;
	}

}
