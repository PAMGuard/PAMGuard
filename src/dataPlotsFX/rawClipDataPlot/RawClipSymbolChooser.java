package dataPlotsFX.rawClipDataPlot;

import PamView.symbol.PamSymbolChooser;
import dataPlotsFX.TDManagedSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;

/**
 * TH symbol chooser for raw clips. 
 * @author Jamie Macaulay
 *
 */
public class RawClipSymbolChooser extends TDManagedSymbolChooserFX {

	public RawClipSymbolChooser(TDDataInfoFX dataInfoFX, PamSymbolChooser pamSymbolChooser, int drawTypes) {
		super(dataInfoFX, pamSymbolChooser, drawTypes);
		

	}

}
