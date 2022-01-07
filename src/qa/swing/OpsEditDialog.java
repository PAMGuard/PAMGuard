package qa.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import PamView.component.PamDateTimeField;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import qa.operations.QAOperationsStatus;
import qa.operations.QAOpsDataUnit;

public class OpsEditDialog extends PamDialog {

	private static OpsEditDialog singleInstance;
	private QAOperationsStatus qaOperationsStatus;
	
	private PamDateTimeField dateTime;
	private LookupComponent lutComponent;
	private QAOpsDataUnit qaOpsDataUnit;
	
	private OpsEditDialog(Window parentFrame, QAOperationsStatus qaOperationsStatus) {
		super(parentFrame, "Edit Operations Data", true);
		this.qaOperationsStatus = qaOperationsStatus;
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		dateTime = new PamDateTimeField();
		lutComponent = new LookupComponent(QAOperationsStatus.qaStatusTopic, null, true);
		mainPanel.setBorder(new TitledBorder("Update status"));
		mainPanel.add(new JLabel("UTC ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(dateTime, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Status ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(lutComponent.getComponent(), c);
		
		setDialogComponent(mainPanel);
	}
	
	public static QAOpsDataUnit showDialog(Window parentFrame, QAOperationsStatus qaOperationsStatus, QAOpsDataUnit qaOpsDataUnit) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || qaOperationsStatus != singleInstance.qaOperationsStatus) {
			singleInstance = new OpsEditDialog(parentFrame, qaOperationsStatus);
		}
		singleInstance.setParams(qaOpsDataUnit);
		singleInstance.setVisible(true);
		return singleInstance.qaOpsDataUnit;
	}

	private void setParams(QAOpsDataUnit qaOpsDataUnit) {
		this.qaOpsDataUnit = qaOpsDataUnit;
		dateTime.setDateTime(qaOpsDataUnit.getTimeMilliseconds());
		lutComponent.setSelectedCode(qaOpsDataUnit.getOpsStatusCode());		
	}

	@Override
	public boolean getParams() {
		Long t = dateTime.getDateTime();
		if (t == null) {
			return showWarning("Invalid date time");
		}
		LookupItem lutItem = lutComponent.getSelectedItem();
		if (lutItem == null) {
			return showWarning("you must select a valid code (right click to edit the list)");
		}
		qaOpsDataUnit.setTimeMilliseconds(t);
		qaOpsDataUnit.setOpsStatusCode(lutItem.getCode());
		qaOpsDataUnit.setOpsStatusName(lutItem.getText());
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		qaOpsDataUnit = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(qaOpsDataUnit);
	}


}
