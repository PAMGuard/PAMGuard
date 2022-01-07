package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import java.awt.Window;

import Localiser.DelayMeasurementParams;
import PamView.dialog.PamDialog;

public class DelayOptionsDialog extends PamDialog {

	private DelayOptionsPanel delayOptionsPanel;
	
	private static DelayOptionsDialog singleInstance;
	
	private DelayMeasurementParams delayMeasurementParams;
	
	private DelayOptionsDialog(Window parentFrame) {
		super(parentFrame, "Delay Measurement", false);
		
		delayOptionsPanel = new DelayOptionsPanel(parentFrame);
		
		setDialogComponent(delayOptionsPanel.getMainPanel());
	}
	
	public static DelayMeasurementParams showDialog(Window owner, DelayMeasurementParams delayMeasurementParams) {
		if (singleInstance == null || singleInstance.getOwner() != owner) {
			singleInstance = new DelayOptionsDialog(owner);
		}
		singleInstance.delayMeasurementParams = delayMeasurementParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.delayMeasurementParams;
	}

	private void setParams() {
		delayOptionsPanel.setParams(delayMeasurementParams);
	}

	@Override
	public void cancelButtonPressed() {
		delayMeasurementParams = null;
	}

	@Override
	public boolean getParams() {
		return delayOptionsPanel.getParams(delayMeasurementParams);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
