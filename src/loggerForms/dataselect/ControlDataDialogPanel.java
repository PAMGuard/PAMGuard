package loggerForms.dataselect;


import PamView.dialog.PamDialogPanel;
import loggerForms.controlDescriptions.ControlDescription;

public abstract class ControlDataDialogPanel implements PamDialogPanel {

	private ControlDataSelector controlDataSelector;
	private ControlDescription controlDescription;

	public ControlDataDialogPanel(ControlDataSelector controlDataSelector) {
		this.controlDataSelector = controlDataSelector;
		this.controlDescription = controlDataSelector.getControlDescription();
	}

	/**
	 * @return the controlDataSelector
	 */
	public ControlDataSelector getControlDataSelector() {
		return controlDataSelector;
	}

	/**
	 * @return the controlDescription
	 */
	public ControlDescription getControlDescription() {
		return controlDescription;
	}

}
