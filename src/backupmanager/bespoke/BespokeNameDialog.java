package backupmanager.bespoke;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;

public class BespokeNameDialog extends PamDialog {

	private BespokeBackups bespokeBackups;
	
	private static BespokeNameDialog singleInstance;
	
	private String streamName;
	
	private JTextField name;

	private BespokeNameDialog(Window parentFrame, BespokeBackups bespokeBackups) {
		super(parentFrame, "Backup stream name", true);
		this.bespokeBackups = bespokeBackups;
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Unique backup stream name"));
		mainPanel.add(BorderLayout.WEST, new JLabel(" Name "));
		mainPanel.add(name = new JTextField(30), BorderLayout.EAST);
		setDialogComponent(mainPanel);
	}

	public static String showDialog(Window owner, BespokeBackups bespokeBackups) {
		if (singleInstance == null || singleInstance.getOwner() != owner || singleInstance.bespokeBackups != bespokeBackups) {
			singleInstance = new BespokeNameDialog(owner, bespokeBackups);
		}
		singleInstance.setVisible(true);
		return singleInstance.streamName;
	}
	
	@Override
	public boolean getParams() {
		streamName = name.getText();
		if (streamName == null || streamName.length() == 0) {
			return showWarning("Backup name not specified");
		}
		BespokeIdentity exId = bespokeBackups.findByName(streamName);
		if (exId != null) {
			return showWarning(String.format(" A backup stream called %s already exists. Pick something else", streamName));
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		streamName = null;
	}

	@Override
	public void restoreDefaultSettings() {
		name.setText("");
	}

}
