package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickControl;

public class OfflineEventViewer extends PamDialog {

	private OfflineEventListPanel offlineEventListPanel;
	
	private static OfflineEventViewer singleInstance;
	
	private ClickControl clickControl;

	private OfflineEventViewer(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " Label clicks", false);
		this.clickControl = clickControl;
		JPanel mainPanel = new JPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		mainPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		setDialogComponent(mainPanel);
	}
	
	public static void showDialog(Window parentFrame, ClickControl clickControl, ClickBTDisplay clickBTDisplay) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.clickControl != clickControl) {
			singleInstance = new OfflineEventViewer(parentFrame, clickControl);
		}
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}


}
