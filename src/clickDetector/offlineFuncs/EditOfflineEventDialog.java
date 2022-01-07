package clickDetector.offlineFuncs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class EditOfflineEventDialog extends PamDialog {
	
	private static EditOfflineEventDialog singleInstance;
	
	private OfflineEventDataUnit offlineEventDataUnit;
	
	private JTextField eventNo;

	private EditOfflineEventDialog(Window parentFrame) {
		super(parentFrame, "Offline click event", false);
		
		JPanel idPanel = new JPanel();
		idPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		addComponent(idPanel, new JLabel("Event No. "), c);
		c.gridx++;
		addComponent(idPanel, eventNo = new JTextField(3), c);
		
		setDialogComponent(idPanel);
	}

	public static OfflineEventDataUnit showDialog(Window parentFrame, OfflineEventDataUnit offlineEventDataUnit) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new EditOfflineEventDialog(parentFrame);
		}
		singleInstance.offlineEventDataUnit = offlineEventDataUnit;
		singleInstance.setVisible(true);
		return singleInstance.offlineEventDataUnit;
	}

	@Override
	public void cancelButtonPressed() {
		offlineEventDataUnit = null;
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
