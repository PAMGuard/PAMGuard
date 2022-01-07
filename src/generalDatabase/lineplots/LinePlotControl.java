package generalDatabase.lineplots;

import java.util.List;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import dataPlotsFX.data.TDDataProviderRegisterFX;

public abstract class LinePlotControl extends PamControlledUnit {
	
	private List<EnhancedTableItem> columnItems;
	private LinePlotDataBlock linePlotDataBlock;
	private LinePlotLogging linePlotLogging;

	public LinePlotControl(String unitType, String unitName) {
		super(unitType, unitName);
		this.columnItems = getColumnItems(); 
		LinePlotProcess lpp = new LinePlotProcess(this);
		linePlotDataBlock = new LinePlotDataBlock("Data", this, lpp);
		linePlotLogging = new LinePlotLogging(this, linePlotDataBlock);
		linePlotDataBlock.SetLogging(linePlotLogging);
		lpp.addOutputDataBlock(linePlotDataBlock);
		addPamProcess(lpp);
		LinePlotDataProvider lpdp = new LinePlotDataProvider(this, linePlotDataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(lpdp);
	}

	/**
	 * 
	 * @return a list of column items. This get's called once from 
	 * the constructor, It's likely that this will get called from the superclass constructor, 
	 * so needs to make it's own list and not wait for the constructor of the subclass to get
	 * called, because that will be too late !
	 */
	abstract public List<EnhancedTableItem> getColumnItems();
	
	private class LinePlotProcess extends PamProcess {

		public LinePlotProcess(PamControlledUnit pamControlledUnit) {
			super(pamControlledUnit, null);
		}

		@Override
		public void pamStart() {
		}

		@Override
		public void pamStop() {
		}
		
	}
}
