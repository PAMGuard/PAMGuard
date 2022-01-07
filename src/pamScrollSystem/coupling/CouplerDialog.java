package pamScrollSystem.coupling;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class CouplerDialog extends PamDialog {

	private JRadioButton[] coupleOptions;
	
	private CouplingParams couplingParams;
	
	private static CouplerDialog singleInstance = null;
	
	private CouplerDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Scroller coupling options"));
		mainPanel.setLayout(new BorderLayout());
		JPanel leftPanel = new JPanel(new GridBagLayout());
		mainPanel.add(leftPanel, BorderLayout.WEST);
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.WEST;
		String[] names = CouplingParams.names;
		String[] tips = CouplingParams.tips;
		coupleOptions = new JRadioButton[names.length];
		ButtonGroup optsGroup = new ButtonGroup();
		for (int i = 0; i < names.length; i++) {
			coupleOptions[i] = new JRadioButton(names[i]);
			coupleOptions[i].setToolTipText(tips[i]);
			optsGroup.add(coupleOptions[i]);
			leftPanel.add(coupleOptions[i], c);
			c.gridy++;
		}
		setHelpPoint("displays.userDisplayHelp.docs.userDisplayPanel");
		leftPanel.setToolTipText("Control how the scrollers on multiple displays follow each other when one of them is moved");
		setDialogComponent(mainPanel);
	}
	
	public static CouplingParams showDialog(Window frame, String name, CouplingParams couplingParams) {
//		if (singleInstance == null || singleInstance.getOwner() != frame || !singleInstance.getTitle().equals(name)) {
			singleInstance = new CouplerDialog(frame, name);
//		}
		singleInstance.couplingParams = couplingParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.couplingParams;
	}

	private void setParams() {
		for (int i = 0; i < coupleOptions.length; i++) {
			coupleOptions[i].setSelected(couplingParams.couplingType == i);
		}
	}
	@Override
	public boolean getParams() {
		for (int i = 0; i < coupleOptions.length; i++) {
			if (coupleOptions[i].isSelected()) {
				couplingParams.couplingType = i;
				return true;
			}
		}
		return showWarning("No coupling option selected!");
	}

	@Override
	public void cancelButtonPressed() {
		couplingParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
