package loggerForms.monitor;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import loggerForms.FormsControl;
import loggerForms.FormsDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class FormsDataSelector extends DataSelector {
	
	private FormsSelectorParams formsSelectorParams = new FormsSelectorParams();
	private FormsControl formsControl;

	public FormsDataSelector(FormsControl formsControl, FormsMasterDataBlock formsMasterBlock, String selectorName, boolean allowScores) {
		super(formsMasterBlock, selectorName, allowScores);
		this.formsControl = formsControl;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof FormsSelectorParams) {
			formsSelectorParams = (FormsSelectorParams) dataSelectParams;
		}
	}

	@Override
	public FormsSelectorParams getParams() {
		return formsSelectorParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new FormsSelPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		FormsDataUnit formsDataUnit = (FormsDataUnit) pamDataUnit;
		String formName = formsDataUnit.getFormDescription().getFormName();
		return formsSelectorParams.isFormSelected(formName) ? 1 : 0;
	}

	/**
	 * @return the formsControl
	 */
	public FormsControl getFormsControl() {
		return formsControl;
	}


}
