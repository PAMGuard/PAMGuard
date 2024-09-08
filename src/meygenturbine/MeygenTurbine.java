package meygenturbine;

import PamController.PamControlledUnit;
import PamguardMVC.PamProcess;

public class MeygenTurbine extends PamControlledUnit {

	public static final String unitType = "Meygen Turbine 4";
	private MeygenTurbineProcess turbineProcess;
	private MeygenDataBlock meygenDataBlock;
	
	public MeygenTurbine(String unitName) {
		super(unitType, unitName);
		turbineProcess = new MeygenTurbineProcess(this);
		addPamProcess(turbineProcess);
		meygenDataBlock = new MeygenDataBlock(turbineProcess);
		turbineProcess.addOutputDataBlock(meygenDataBlock);
		meygenDataBlock.SetLogging(new MeygenLogging(meygenDataBlock));
		meygenDataBlock.setOverlayDraw(new MeygenGraphics(meygenDataBlock, null));
	}
	
	private class MeygenTurbineProcess extends PamProcess {

		public MeygenTurbineProcess(PamControlledUnit pamControlledUnit) {
			super(pamControlledUnit, null);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void pamStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pamStop() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
