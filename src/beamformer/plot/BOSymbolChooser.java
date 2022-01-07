package beamformer.plot;

import PamView.PamSymbolType;
import dataPlotsFX.SimpleSymbolChooserFX;

public class BOSymbolChooser extends SimpleSymbolChooserFX {

	public BOSymbolChooser() {
		super();
		getPamSymbol(null, 0).setSymbol(PamSymbolType.SYMBOL_POINT);
	}


}
