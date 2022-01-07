package clickDetector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.BasicKeyItem;
import PamView.GeneralProjector;
import PamView.HoverData;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamOldSymbolManager;
import PamView.PamSymbolType;
import PamView.GeneralProjector.ParameterType;
import PamView.dialog.IconPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.tdPlots.ClickDetSymbolChooser;

/**
 * 
 * @author Douglas Gillespie
 *
 * Overlay graphics for clicks from clickDetector.  
 */
public class NewClickOverlayGraphics extends PamDetectionOverlayGraphics {


	static final public int SHOW_MAP = 0x1;
	static final public int SHOW_SPECTROGRAM = 0x2;
	static final public int SHOW_RADAR = 0x4;

	int drawTypes;
	ClickControl clickControl;
	private String name;

	public NewClickOverlayGraphics(ClickControl clickControl, PamDataBlock parentDataBlock, int drawTypes, String name) {

		super(parentDataBlock, new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK));
		this.clickControl = clickControl;
		this.drawTypes = drawTypes;
		this.name = name;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean canDrawOnMap() {
		return ((drawTypes & SHOW_MAP) != 0);
	}
	//
	//	@Override
	//	protected boolean canDrawOnRadar(ParameterType radialParameter) {
	//		return super.canDrawOnRadar(radialParameter);
	//	}

	@Override
	protected boolean canDrawOnSpectrogram() {
		// TODO Auto-generated method stub
		return (super.canDrawOnSpectrogram() && ((drawTypes * SHOW_SPECTROGRAM) != 0));
	}

//	@Override
//	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
//		if (pamDataUnit == null) {
//			return super.getPamSymbol();
//		}
//		return ClickBTDisplay.getClickSymbol(clickControl.getClickIdentifier(), (ClickDetection) pamDataUnit,
//				clickControl.clickParameters.radarColour);
//	}

	@Override
	protected Rectangle drawAmplitudeOnRadar(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		return super.drawAmplitudeOnRadar(g, pamDataUnit, generalProjector);
	}

//	@Override
//	public PamKeyItem createKeyItem(GeneralProjector projector, int keyType) {
//		if (projector.getParmeterType(0) == ParameterType.LATITUDE
//				&& projector.getParmeterType(1) == ParameterType.LONGITUDE) {
//			return createMapKey(keyType);
//		} else if (projector.getParmeterType(0) == ParameterType.TIME
//				&& projector.getParmeterType(1) == ParameterType.FREQUENCY) {
//			return createSpectrogramKey(keyType);
//		} else if (projector.getParmeterType(0) == ParameterType.BEARING) {
//			return createRadarKey(keyType);
//		}
//		return null;
//	}

	/**
	 * Makes a multi coloured icon for use in key items. 
	 * @param iconStyle full or concise icon
	 * @return a multi coloured icon with lots of coloured circles. 
	 */
	protected Component makeMultiColourIcon(int iconStyle) {
		return makeMultiColourIcon(iconStyle, null);
	}
	/**
	 * 
	 * @param iconStyle full or concise icon
	 * @param forceSymbolType force a particular symbol type
	 * @return multi coloured icon
	 */
	protected Component makeMultiColourIcon(int iconStyle, PamSymbol forceSymbolType) {

		int nRows = 2;
		int nCols = 6;
		if ((iconStyle & PamSymbol.ICON_STYLE_SYMBOL) == 0) {
			nRows = 4;
			nCols = 3;
		}
		IconPanel p = new IconPanel(nRows, nCols);
		PamSymbol pamSymbol;
		for (int whaleId = 1; whaleId <= nRows * nCols; whaleId++) {
			pamSymbol = ClickDetSymbolChooser.getClickSymbol(whaleId).clone();
			pamSymbol.setIconStyle(iconStyle);
			if (forceSymbolType != null) {
				pamSymbol.setSymbol(forceSymbolType.getSymbol());
			}
			p.addIconToGrid(pamSymbol);
		}
		return p;

	}
	//	
	//	private class MultiElementIcon {
	//		
	//	}


//	public PamSymbol getDefPamSymbol(int whaleId) {
//		PamSymbol s = super.getPamSymbol();
//
//		return s;
//	}

	/**
	 * Create a key item specific to the map
	 * @param keyType
	 * @return key item
	 */
	protected PamKeyItem createMapKey(int keyType) {
		//		if (keyType == PamKeyItem.KEY_SHORT) {
		//			PamSymbol pamSymbol = ClickBTDisplay.getClickSymbol(0).clone();
		//			pamSymbol.setIconStyle(PamSymbol.ICON_STYLE_LINE);
		//			return new BasicKeyItem(pamSymbol, name);
		//		}
		//		else {
		return new BasicKeyItem(makeMultiColourIcon(PamSymbol.ICON_STYLE_LINE), name);
		//		}
	}

	/**
	 * create a key item specific to the radar
	 * @param keyType type of key to draw
	 * @return key item
	 */
	protected PamKeyItem createRadarKey(int keyType) {
		if (keyType == PamKeyItem.KEY_SHORT) {
			return new BasicKeyItem(ClickDetSymbolChooser.getClickSymbol(0).clone(), name);
		}
		else {
			return new BasicKeyItem(makeMultiColourIcon(PamSymbol.ICON_STYLE_SYMBOL), name);
		}
	}

	/**
	 * Draw little triangles at top and bottom of spectrogram - better than the boxes for clicks. 
	 */
	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		//		return super.drawOnSpectrogram(g, pamDataUnit, generalProjector);
		ClickDetection click = (ClickDetection) pamDataUnit;
		Coordinate3d c3d = generalProjector.getCoord3d(click.getTimeMilliseconds(), 0, 0);
		Point pt = c3d.getXYPoint();
		PamSymbol specSymbol = getPamSymbol(pamDataUnit, generalProjector);
		
//		if (click.getEventId() != 0) {
//		if (symbol != null)
//			Color col = PamColors.getInstance().getWhaleColor(click.getEventId());
//			specSymbol.setFillColor(col);
//			specSymbol.setLineColor(col);
//		}
//		else {
//			specSymbol.setFillColor(Color.WHITE);
//			specSymbol.setLineColor(Color.BLACK);
//		}
		pt.y -= specSymbolSize / 2;
		specSymbol.setSymbol(PamSymbolType.SYMBOL_TRIANGLEU);
		specSymbol.draw(g, pt);
//		generalProjector.addHoverData(new HoverData(c, pamDataUnit, iSide, subPlotNumber));
		specSymbol.setSymbol(PamSymbolType.SYMBOL_TRIANGLED);
		pt.y = specSymbolSize/2;
		specSymbol.draw(g, pt);

		return null;
	}
	private static int specSymbolSize = 8;
	private PamSymbol specSymbol = new PamSymbol(PamSymbolType.SYMBOL_TRIANGLEU, specSymbolSize, specSymbolSize, true, Color.RED, Color.RED);

	protected PamKeyItem createSpectrogramKey(int keyType) {
		if (keyType == PamKeyItem.KEY_SHORT) {
			return new BasicKeyItem(ClickDetSymbolChooser.getClickSymbol(0).clone(), name);
		}
		else {
			return new BasicKeyItem(makeMultiColourIcon(PamSymbol.ICON_STYLE_SYMBOL), name);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

//	@Override
//	public double getDefaultRange() {
//		return clickControl.clickDetector.getClickControl().clickParameters.defaultRange;
//		//		return super.getDefaultRange();
//	}

	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		//		setLineColour(PamColors.getInstance().getWhaleColor(((ClickDetection) pamDetection).getEventId()));
		//		setLineColour(Color.RED);
		//		g.setColor(getLineColour());
		//		System.out.println("Set line colour to red");
		ClickDetection cd = (ClickDetection) pamDetection;
		if (shouldDrawOnMap(cd) == false) {
			return null;
		}
		//		if (cd.getClickType() == 0) {
		//			return null;
		//		}
//		setDefaultRange(clickControl.clickDetector.getClickControl().clickParameters.defaultRange);
		return super.drawOnMap(g, pamDetection, generalProjector);
	}

	private boolean shouldDrawOnMap(ClickDetection cd) {

		return (cd.getClickType() >= 0 || cd.getEventId() > 0 || cd.getSuperDetectionsCount() > 0);

	}


}
