package autecPhones;

import java.awt.Frame;

import AirgunDisplay.AirgunGraphics;
import PamController.PamControlledUnit;
import PamView.PamOldSymbolManager;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamProcess;

public class AutecPhonesControl extends PamControlledUnit {

	AutecProcess autecProcess;
	
	AutecDataBlock autecDataBlock;
	
	AutecGraphics autecGraphics;
	
	public AutecPhonesControl(String unitName) {
		super("AUTEC Phones", unitName);
		addPamProcess(autecProcess = new AutecProcess(this));
	}

	public Frame getGuiFrame() {
		return super.getGuiFrame();
	}
	class AutecProcess extends PamProcess {

		public AutecProcess(PamControlledUnit pamControlledUnit) {
			super(pamControlledUnit, null);
			addOutputDataBlock(autecDataBlock = new AutecDataBlock(this));
			autecDataBlock.setOverlayDraw(autecGraphics = new AutecGraphics());
			autecDataBlock.setPamSymbolManager(new StandardSymbolManager(autecDataBlock, AutecGraphics.defSymbol, true));

//			PamOldSymbolManager.getInstance().addManagesSymbol(autecGraphics);
		}

		@Override
		public void pamStart() {
		}

		@Override
		public void pamStop() {
		}
		
	}
}
