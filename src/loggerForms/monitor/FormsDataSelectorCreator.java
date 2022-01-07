package loggerForms.monitor;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import loggerForms.FormsControl;

public class FormsDataSelectorCreator extends DataSelectorCreator {

	private FormsMasterDataBlock masterDataBlock;
	private FormsControl formsControl;

	public FormsDataSelectorCreator(FormsControl formsControl, FormsMasterDataBlock masterDataBlock) {
		super(masterDataBlock);
		this.formsControl =formsControl;
		this.masterDataBlock = masterDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new FormsDataSelector(formsControl, masterDataBlock, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new FormsSelectorParams();
	}

}
