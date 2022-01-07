package backupmanager.swing;

import java.awt.Window;

import PamView.dialog.PamDialog;

public class ActionDialog extends PamDialog {

	public ActionDialog(Window parentFrame, String title, boolean hasDefault) {
		super(parentFrame, title, hasDefault);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
