package RightWhaleEdgeDetector.datasel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class RSDataSelDialog implements PamDialogPanel {
	
	private RWDataSelector rwDataSelector;
	
	private JPanel mainPanel;
	
	private JTextField minType;

	public RSDataSelDialog(RWDataSelector rwDataSelector) {
		super();
		this.rwDataSelector = rwDataSelector;
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Right Whale sound type"));
		mainPanel.add(new JLabel("Minimum RW type ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minType = new JTextField(3), c);
		minType.setToolTipText("Right whale sound type: an integer number between 5 and 11");
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		RWDataSelParams params = rwDataSelector.getDataSelParams();
		minType.setText(Integer.valueOf(params.minType).toString());
	}

	@Override
	public boolean getParams() {
		int type = 0;
		try {
			type = Integer.valueOf(minType.getText()); 
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(PamController.getMainFrame(), "Error", "Invalid minimum right whale type, must be Integer");
		}
		RWDataSelParams params = rwDataSelector.getDataSelParams();
		params.minType = type;
		return true;
	}

}
