package dataPlotsFX.clickPlotFX;

import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.RawWavePlotManager;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDScaleInfo;
import javafx.scene.paint.Color;

/**
 * 
 * The click raw plot manager. Handles plotting multiple raw waveforms on a time amplitude display. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickRawPlotManager extends RawWavePlotManager {

	
	private ClickPlotInfoFX rawClipInfo; 
	
	public ClickRawPlotManager(ClickPlotInfoFX rawClipInfo) {
		super(rawClipInfo);
		this.rawClipInfo=rawClipInfo; 
	}

	@Override
	public TDScaleInfo getWaveScaleInfo() {
		return rawClipInfo.getRawScaleInfo();
	}

	@Override
	public Color getColor(PamDataUnit pamDataUnit, int type) {
		return rawClipInfo.getSymbolChooser().getPamSymbol(pamDataUnit, type).getFillColor();
	}


	
}
