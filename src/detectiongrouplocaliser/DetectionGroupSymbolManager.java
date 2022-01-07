package detectiongrouplocaliser;


import PamguardMVC.superdet.swing.SuperDetectionSymbolManager;

public class DetectionGroupSymbolManager extends SuperDetectionSymbolManager {

	public DetectionGroupSymbolManager(DetectionGroupDataBlock pamDataBlock) {
		super(pamDataBlock, DetectionGroupGraphics.defaultSymbol);
		setSpecialColourName("Event Index");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * PamView.symbol.StandardSymbolManager#createSymbolChooser(java.lang.String,
	 * PamView.GeneralProjector)
	 */
//	@Override
//	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
//		return new DetectionGroupSymbolChooser(this, getPamDataBlock(), displayName,
//				DetectionGroupGraphics.defaultSymbol, projector);
//	}
//
//	private class DetectionGroupSymbolChooser extends StandardSymbolChooser {
//
//		public DetectionGroupSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock,
//				String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
//			super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
//		}

//		/* (non-Javadoc)
//		 * @see PamView.symbol.StandardSymbolChooser#colourBySpecial(PamView.symbol.SymbolData, PamView.GeneralProjector, PamguardMVC.PamDataUnit)
//		 */
//		@Override
//		public SymbolData colourBySpecial(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
//			SymbolData newSymbolData =  super.colourBySpecial(symbolData, projector, dataUnit);
//			if (DetectionGroupDataUnit.class.isAssignableFrom(dataUnit.getClass())) {
//				DetectionGroupDataUnit dgdu = (DetectionGroupDataUnit) dataUnit; 
//				Color col = PamColors.getInstance().getWhaleColor((int) dgdu.getUID());
//				newSymbolData.setFillColor(col);
//				newSymbolData.setLineColor(col);
//			}
//			return newSymbolData;
//		}

//	}

}
