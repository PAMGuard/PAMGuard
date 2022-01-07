package loggerForms.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.controlDescriptions.ControlDescription;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class LookupDataSelector extends ControlDataSelector {
	
	private LookupDataSelParams lookupDataSelParams = new LookupDataSelParams();
	

	public LookupDataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores,
			ControlDescription controlDescription) {
		super(pamDataBlock, selectorName, allowScores, controlDescription);
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof LookupDataSelParams) {
			lookupDataSelParams = (LookupDataSelParams) dataSelectParams;
		}

	}

	@Override
	public LookupDataSelParams getParams() {
		return lookupDataSelParams;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ControlDataDialogPanel getControlDataDialogPanel() {
		return new LookupDataDialogPanel(this);
	}

	@Override
	protected double scoreData(PamDataUnit loggerDataUnit, Object controlData) {
		if (controlData == null) {
			return lookupDataSelParams.isUseUnassigned() ? 1 : 0;
		}
		if (controlData instanceof String == false) {
			return 0;
		}
		String code = (String) controlData;
		if (code.length() == 0) {
			return lookupDataSelParams.isUseUnassigned() ? 1 : 0;
		}
		return lookupDataSelParams.getItemSelection(code) ? 1 : 0;
	}

}
