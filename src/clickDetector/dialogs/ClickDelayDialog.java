package clickDetector.dialogs;

import java.awt.Window;

import PamView.dialog.PamDialog;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;

/**
 * dialog to wrap around clickDelayPanel for making a stand alone dialog
 * @author dg50
 *
 */
public class ClickDelayDialog extends PamDialog {

	private ClickDelayPanel clickDelayPanel;
	private static ClickDelayDialog singleInstance;
	private ClickParameters clickParameters;
	private ClickControl clickControl;
	
	private ClickDelayDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, "Click delay measurement", false);
		this.clickControl = clickControl;
		clickDelayPanel = new ClickDelayPanel(clickControl, parentFrame);
		setDialogComponent(clickDelayPanel.getDialogComponent());
		setResizable(true);
	}

	public static ClickParameters showDialog(Window owner, ClickControl clickControl) {
		if (singleInstance == null || singleInstance.getOwner() != owner || singleInstance.clickControl != clickControl) {
			singleInstance = new ClickDelayDialog(owner, clickControl);
		}
		singleInstance.clickParameters = clickControl.getClickParameters();
	
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickParameters;
	}
	
	
	private void setParams() {
		clickDelayPanel.setParams(clickParameters);
	}

	@Override
	public boolean getParams() {
		boolean ans = clickDelayPanel.getParams(clickParameters);
		return ans;
	}

	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
