package qa.swing;

import java.awt.Component;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import qa.QAControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;
import warnings.PamWarning;
import warnings.WarningSystem;

public class QADisplayProvider implements UserDisplayProvider {

	private QAControl qaControl;

	private int nDisplays = 0;
	
	private PamWarning dispWarning;
	
	public QADisplayProvider(QAControl qaControl) {
		this.qaControl = qaControl;
		displayWarning();
	}

	@Override
	public String getName() {
		return qaControl.getUnitName() + " Display";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		nDisplays++;
		displayWarning();
		return new QADisplayPanel(qaControl);
	}

	@Override
	public Class getComponentClass() {
		return QADisplayPanel.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		nDisplays--;
		displayWarning();
	}

	/**
	 * Show a pamguard warning if there are no displays. This 
	 * may flash a warning at the start since it's shown from the constructor. 
	 */
	private synchronized void displayWarning() {

		if (nDisplays == 0) {
			String msg = String.format("You are using a %s module (%s) but have no display for this module." + 
					" You should create a user display panel and add the \"%s\"", 
					qaControl.getUnitType(), qaControl.getUnitName(), getName());
			dispWarning = new PamWarning(qaControl.getUnitName() + " Module", msg, 2);
			WarningSystem.getWarningSystem().addWarning(dispWarning);
		}
		else if (dispWarning != null) {
			WarningSystem.getWarningSystem().removeWarning(dispWarning);
			dispWarning = null;
		}
	}
	

}
