package loggerForms.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.controlDescriptions.ControlDescription;

/**
 * Data selector for a single logger control. 
 * @author Douglas Gillespie
 *
 */
public abstract class ControlDataSelector extends DataSelector {

	private FormDescription formDescription;
	
	private ControlDescription controlDescription;
	
	private int controlIndex;

	public ControlDataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores, ControlDescription controlDescription) {
		super(pamDataBlock, selectorName, allowScores);
		this.controlDescription = controlDescription;
		formDescription = controlDescription.getFormDescription();
		controlIndex = formDescription.getControlIndex(controlDescription);
	}

	@Override
	public final double scoreData(PamDataUnit pamDataUnit) {
		FormsDataUnit loggerDataUnit = (FormsDataUnit) pamDataUnit;
		Object[] data = loggerDataUnit.getFormData();
		if (data == null) {
			return 0;
		}
		return scoreData(loggerDataUnit, data);
	}

	public double scoreData(PamDataUnit pamDataUnit, Object[] formData) {
		if (formData == null) {
			return 0;
		}
		if (controlIndex < 0 || controlIndex >= formData.length) {
			return 0;
		}
		return scoreData(pamDataUnit, formData[controlIndex]);
	}

	/**
	 * Score data for this specific control. May have to check that it's the right
	 * type, and it may be null. 
	 * @param loggerDataUnit 
	 * @param controlData
	 * @return data score, usually 0 or 1, can sometimes be scalar. 
	 */
	protected abstract double scoreData(PamDataUnit dataUnit, Object controlData);

	@Override
	public final PamDialogPanel getDialogPanel() {
		return getControlDataDialogPanel();
	}

	protected abstract ControlDataDialogPanel getControlDataDialogPanel();

	/**
	 * @return the controlDescription
	 */
	public ControlDescription getControlDescription() {
		return controlDescription;
	}

	@Override
	public abstract ControlDataSelParams getParams();


}
