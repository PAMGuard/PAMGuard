package qa.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import qa.QAControl;
import qa.QANotifyable;
import qa.QAParameters;
import qa.operations.OpsStatusParams;
import qa.operations.QAOperationsStatus;
import qa.operations.QAOpsDataUnit;

/**
 * Panel of options for different operational states. 
 * Currently this is just the option to run random tests. 
 * @author dg50
 *
 */
public class OpsOptionsPanel {

	private QAControl qaControl;
	
	private JPanel mainPanel;

	private QAOperationsStatus qaOpsStatus;

	private JCheckBox[] randomSelect;
	
	public OpsOptionsPanel(QAControl qaControl) {
		this.qaControl = qaControl;
		qaOpsStatus = qaControl.getQaOperationsStatus();
		mainPanel = new PamPanel();
		mainPanel.setLayout(new GridBagLayout());
		setParams();
	}
	
	public Component getComponent() {
		return mainPanel;
	}

	private void setParams() {
		QAParameters qaParams = qaControl.getQaParameters();
		QAOpsDataUnit currentStateDU = qaControl.getQaOperationsStatus().getCurrentStatus();
		String currentCode = null;
		if (currentStateDU != null) {
			currentCode = currentStateDU.getOpsStatusCode();
		}
		
		mainPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
//		c.anchor = GridBagConstraints.CENTER;
		mainPanel.add(new PamLabel("State", JLabel.CENTER), c);
		c.gridx++;
		mainPanel.add(new PamLabel("<html><center>Allow<br>Random<br>Drills</html>", JLabel.CENTER), c);
		c.gridx = 0;
//		c.anchor = GridBagConstraints.f
		LookupList currList = qaOpsStatus.getLookupList();
		Vector<LookupItem> lutList = currList.getSelectedList();
		randomSelect = new JCheckBox[lutList.size()];
		int i = 0;
		for (LookupItem lutItem:lutList) {
			c.gridy++;
			c.gridx = 0;
			String s = lutItem.getCode() + ":" + lutItem.getText() + " ";
			mainPanel.add(new PamLabel(s, JLabel.RIGHT), c);
			randomSelect[i] = new PamCheckBox();
			c.gridx++;
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.HORIZONTAL;
			mainPanel.add(randomSelect[i], c);
			OpsStatusParams opsParams = qaParams.getOpsStatusParams(lutItem.getCode());
			randomSelect[i].setSelected(opsParams.isAllowRandomTesting());
			randomSelect[i].addActionListener(new OptSelectListener(lutItem, randomSelect[i]));
			if (lutItem.getCode().equals(currentCode)) {
				c.gridx++;
				randomSelect[i].setText("<");
			}
			i++;
		}
	}
	
	private class OptSelectListener implements ActionListener {

		private LookupItem lutItem;
		private JCheckBox checkBox;
		/**
		 * @param lutItem
		 */
		public OptSelectListener(LookupItem lutItem, JCheckBox checkBox) {
			super();
			this.lutItem = lutItem;
			this.checkBox = checkBox;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			OpsStatusParams optParams = qaControl.getQaParameters().getOpsStatusParams(lutItem.getCode());
			optParams.setAllowRandomTesting(checkBox.isSelected());
			qaControl.tellNotifyables(QANotifyable.PARAMETER_CHANGE);
		}
		
	}

	public void newState(String opsStatusCode) {
		setParams();
	}


}
