package loggerForms.monitor;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelectorCreator;
import loggerForms.FormsControl;
import loggerForms.FormsDataUnit;

public class FormsMasterDataBlock extends PamDataBlock {

	private FormsMonitorMaster formsMonitor;
	
	private FormsDataSelectorCreator formsDataSelectorCreator;

	public FormsMasterDataBlock(FormsMonitorMaster formsMonitor, String dataName, PamProcess parentProcess) {
		super(FormsDataUnit.class, dataName, parentProcess, 0);
		this.formsMonitor = formsMonitor;
		formsDataSelectorCreator = new FormsDataSelectorCreator(formsMonitor.getFormsControl(), this);
	}

	@Override
	public DataSelectorCreator getDataSelectCreator() {
		return formsDataSelectorCreator;
	}

}
