package loggerForms;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import PamView.dialog.PamDialog;

public class FormsPlotOptionsDialog extends PamDialog {

	private static FormsPlotOptionsDialog singleInstance;
	
	private FormsControl formsControl;
	
	private ArrayList<FormPlotOptionsPanel> optionsPanels = new ArrayList<FormPlotOptionsPanel>();

	private boolean answer;
	
	private FormsPlotOptionsDialog(Window parentFrame, FormsControl formsControl) {
		super(parentFrame, "Logger forms plot options", false);
		this.formsControl = formsControl;
		int n = formsControl.getNumFormDescriptions();
		FormPlotOptionsPanel aPanel;
		FormDescription formDescription;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		mainPanel.add(BorderLayout.CENTER, tabbedPane);
		for (int i = 0; i < n; i++) {
			formDescription = formsControl.getFormDescription(i);
			if (formDescription.canDrawOnMap() == false) {
				continue;
			}
			aPanel = new FormPlotOptionsPanel(formDescription);
			optionsPanels.add(aPanel);
			JPanel leftPanel = new JPanel(new BorderLayout());
			leftPanel.add(BorderLayout.WEST, aPanel.getComponent());
			tabbedPane.add(formDescription.getFormNiceName(), leftPanel);
		}
		
		setDialogComponent(mainPanel);
	}
	
	/**
	 * Called when all forms are regenerated to destroy existing dialog 
	 * so that it get's rebuilt next time it's called
	 */
	public static void deleteDialog() {
		singleInstance = null;
	}
	
	public static boolean showDialog(Window parentFrame, FormsControl formsControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame ||
				singleInstance.formsControl != formsControl) {
			singleInstance = new FormsPlotOptionsDialog(parentFrame, formsControl);
		}
		singleInstance.answer = true;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.answer;
	}

	private void setParams() {
		for (FormPlotOptionsPanel aPanel:optionsPanels) {
			aPanel.setParams();
		}
	}

	@Override
	public void cancelButtonPressed() {
		answer = false;
	}

	@Override
	public boolean getParams() {
		for (FormPlotOptionsPanel aPanel:optionsPanels) {
			aPanel.getParams();
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
