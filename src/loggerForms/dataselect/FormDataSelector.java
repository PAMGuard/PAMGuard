package loggerForms.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.controlDescriptions.InputControlDescription;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class FormDataSelector extends DataSelector {

	private FormDescription formDescription;
	
	private FormDataSelParams formDataSelParams = new FormDataSelParams();
	
	private ControlDataSelector controlDataSelector;

	public FormDataSelector(PamDataBlock pamDataBlock, FormDescription fromDescription, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.formDescription = fromDescription;
//		setSelectorTitle(formDescription.getFormName());
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof FormDataSelParams) {
			this.formDataSelParams = (FormDataSelParams) dataSelectParams;
			setupControlDataSelector();
		}
		
	}

	/**
	 * Currently just set up a single data selector for one control only. 
	 */
	private void setupControlDataSelector() {
		int ctrlInd = formDescription.findInputControlByName(formDataSelParams.controlName);
		if (ctrlInd < 0) {
			controlDataSelector = null;
			return;
		}
		else {
			InputControlDescription inputCtrl = formDescription.getInputControlDescriptions().get(ctrlInd);
			ControlDataSelCreator dsc = inputCtrl.getDataSelectCreator();
			if (dsc == null) {
				return;
			}
			controlDataSelector = dsc.getDataSelector(getSelectorName(), isAllowScores(), null);
		}
	}

	@Override
	public FormDataSelParams getParams() {
		return formDataSelParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new FormDataSelPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (controlDataSelector == null) {
			return 1;
		}
		Object[] formData = null;
		if (pamDataUnit instanceof FormsDataUnit) {
			formData = ((FormsDataUnit) pamDataUnit).getFormData();
			return controlDataSelector.scoreData(pamDataUnit, formData);
		}
		else {
			return 0;
		}
	}
	
	public double scoreData(PamDataUnit dataUnit, Object[] formData) {
		if (controlDataSelector == null) {
			return 0;
		}
		return controlDataSelector.scoreData(dataUnit, formData);
	}

	/**
	 * @return the fromDescription
	 */
	public FormDescription getFromDescription() {
		return formDescription;
	}

	@Override
	public String getSelectorTitle() {
		return formDescription.getFormName() + ": " + formDataSelParams.controlName;
	}

}
