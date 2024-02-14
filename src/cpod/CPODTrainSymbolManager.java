package cpod;

import PamView.symbol.SymbolData;
import PamguardMVC.superdet.swing.SuperDetectionSymbolManager;

public class CPODTrainSymbolManager extends SuperDetectionSymbolManager {

	public CPODTrainSymbolManager(CPODClickTrainDataBlock pamDataBlock) {
		super(pamDataBlock, new SymbolData());
		super.setSpecialColourName("Event Colour");
	}


}


