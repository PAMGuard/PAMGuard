package dataPlotsFX.rawClipDataPlot;

import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.RawWavePlotManager;
import dataPlotsFX.data.TDScaleInfo;
import javafx.scene.paint.Color;


/**
 * Plots clips waveforms. 
 * @author Jamie macaulay 
 *
 */
public class RawClipWavePlotManager extends RawWavePlotManager {

	private RawClipDataInfo rawClipInfo;

	public RawClipWavePlotManager(RawClipDataInfo rawClipInfo) {
		super(rawClipInfo);
		this.rawClipInfo=rawClipInfo; 
	}

	@Override
	public TDScaleInfo getWaveScaleInfo() {
		return rawClipInfo.getRawScaleInfo();
	}

	@Override
	public Color getColor(PamDataUnit pamDataUnit, int type) {
		return rawClipInfo.getSymbolChooser().getPamSymbol(pamDataUnit, type).getLineColor();
	}

}
