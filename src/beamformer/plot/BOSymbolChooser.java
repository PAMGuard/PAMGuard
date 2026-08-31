package beamformer.plot;

import PamView.PamSymbolType;
import dataPlotsFX.SimpleSymbolChooserFX;

public class BOSymbolChooser extends SimpleSymbolChooserFX {

	public BOSymbolChooser() {
		super();
		getSymbolData().symbol = PamSymbolType.SYMBOL_POINT;
	}


}
