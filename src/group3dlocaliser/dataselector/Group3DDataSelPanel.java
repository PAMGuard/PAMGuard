package group3dlocaliser.dataselector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import group3dlocaliser.Group3DLocaliserControl;

public class Group3DDataSelPanel implements PamDialogPanel {

	private Group3DDataSelector g3dDataSelector;
	private Group3DLocaliserControl groupLocControl;
	
	private JPanel mainPanel;
	private JTextField maxChi2;
	private JTextField maxError;
	private JTextField minDF;
	private Group3DDataSelectParams params;

	public Group3DDataSelPanel(Group3DLocaliserControl groupLocControl, Group3DDataSelector g3dDataSelector) {
		this.groupLocControl = groupLocControl;
		this.g3dDataSelector = g3dDataSelector;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("3D Data Selection"));
		maxChi2 = new JTextField(5);
		maxError = new JTextField(5);
		minDF = new JTextField(5);
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Max Chi2 ", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(maxChi2, c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Max Error ", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(maxError, c);
		c.gridx++;
		mainPanel.add(new JLabel(" m", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Min Deg's Freedom ", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(minDF, c);
		c.gridx++;
		mainPanel.add(new JLabel(" ", JLabel.LEFT), c);
		maxChi2.setToolTipText("Maximum chi2 /  Number of degrees of freedom for the localisation fit");
		maxError.setToolTipText("Maximum total localisation error in all dimensions");
		minDF.setToolTipText("<html>Minimum number of degrees of freedom in the fit. <p>"
				+ "This will be three less than the number of used time delays");
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		params = ((Group3DDataSelectParams) g3dDataSelector.getParams()).clone();
		DecimalFormat df = new DecimalFormat("##.#");
		maxChi2.setText(df.format(params.maxChi2));
		maxError.setText(df.format(params.maxError));
		minDF.setText(String.format("%d", params.minDF));
	}

	@Override
	public boolean getParams() {
		try {
			params.maxChi2 = Double.valueOf(maxChi2.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(groupLocControl.getGuiFrame(), "Invalid value", "Invalid Chi2 value");
		}
		try {
			params.maxError = Double.valueOf(maxError.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(groupLocControl.getGuiFrame(), "Invalid value", "Invalid Max Error value");
		}
		try {
			params.minDF = Integer.valueOf(minDF.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(groupLocControl.getGuiFrame(), "Invalid value", "Invalid Min degrees of freedom value");
		}
		g3dDataSelector.setParams(params);
		return true;
	}

}
