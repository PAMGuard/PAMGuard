package whistleDetector;

import java.awt.Color;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.GeneralProjector.ParameterType;
import PamView.symbol.SymbolData;

public class WhistleEventGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true,
			Color.BLUE, Color.BLUE);
	
	private WhistleEventDetector whistleEventDetector;
	
	public WhistleEventGraphics(WhistleEventDetector whistleEventDetector) {
		super(whistleEventDetector.getOutputDataBlock(0), new PamSymbol(defaultSymbol));
		this.whistleEventDetector = whistleEventDetector;
//		if (getPamSymbol() == null) {
//			PamSymbol mapSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true,
//					Color.BLUE, Color.BLUE);
//			setPamSymbol(mapSymbol);
//		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canDraw(GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE){
			return true;
		}
		return false;
	}

}
