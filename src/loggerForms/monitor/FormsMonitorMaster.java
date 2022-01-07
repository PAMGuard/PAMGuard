package loggerForms.monitor;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.dataSelector.DataSelector;
import loggerForms.FormsControl;
import loggerForms.FormsProcess;

/**
 * Easy class to allow other parts of PAMGuard to monitor some or all 
 * Logger forms. Since each form writes to a different data block, this 
 * should be easier than monitoring each form directly. 
 * 
 * Doesn't have to do much apart from put everything into a single data block
 * and the data block normal functionality can handle all the monitoring.
 * 
 *  Only need one of these for everything anyway since each monitoring 
 *  entity can use a data selector to pick what it wants. 
 * @author dg50
 *
 */
public class FormsMonitorMaster {
	
	private FormsMasterDataBlock masterDataBlock;
	private FormsControl formsControl;

	public FormsMonitorMaster(FormsControl formsControl, FormsProcess formsProcess) {
		this.formsControl = formsControl;
		masterDataBlock = new FormsMasterDataBlock(this, formsControl.getUnitName(), formsProcess);
	}
	
	public void addObserver(PamObserver pamObserver, boolean reThread) {
		masterDataBlock.addObserver(pamObserver, reThread);
	}
	
	public void deleteObserver(PamObserver pamObserver) {
		masterDataBlock.deleteObserver(pamObserver);
	}
	
	public DataSelector getDataSelector(String selectorName) {
		return masterDataBlock.getDataSelectCreator().getDataSelector(selectorName, false, null);
	}

	/**
	 * @return the formsControl
	 */
	public FormsControl getFormsControl() {
		return formsControl;
	}

	/**
	 * Call whenever the forms have been regenerated to pick 
	 * up new data blocks.
	 */
	public void rebuiltForms() {
		int n = formsControl.getFormsProcess().getNumOutputDataBlocks();
		for (int i = 0; i < n; i++) {
			PamDataBlock db = formsControl.getFormsProcess().getOutputDataBlock(i);
			db.addObserver(new FormsObserver(), false);
		}
	}

	private class FormsObserver extends PamObserverAdapter  {

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			masterDataBlock.addPamData(arg);
		}

		@Override
		public String getObserverName() {
			return "Forms Master monitor";
		}
	}

	/**
	 * @return the masterDataBlock
	 */
	public FormsMasterDataBlock getMasterDataBlock() {
		return masterDataBlock;
	}
	
}
