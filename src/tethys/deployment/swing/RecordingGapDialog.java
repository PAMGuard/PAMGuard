package tethys.deployment.swing;

import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tethys.deployment.DeploymentExportOpts;

public class RecordingGapDialog extends PamDialog {

	private JTextField maxGap, minLength;
	
	private DeploymentExportOpts exportOpts;
	
	private RecordingGapDialog(Window parentFrame) {
		super(parentFrame, "Maximum Gap", true);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Max recording gap"));
		PamGridBagContraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Maximum gap ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(maxGap = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" seconds", JLabel.RIGHT), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Minimum length ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(minLength = new JTextField(3), c);
		c.gridx++;
		mainPanel.add(new JLabel(" seconds", JLabel.RIGHT), c);
		
		maxGap.setToolTipText("Maximum gap between recording periods. Periods with a gap less than this will be counted as one");
		minLength.setToolTipText("Minimum recording length. Recording sections shorter than this will be ignored");
		
		setDialogComponent(mainPanel);
	}
	
	public static DeploymentExportOpts showDiloag(Window parent, DeploymentExportOpts exportOpts) {
		RecordingGapDialog dialog = new RecordingGapDialog(parent);
		dialog.setParams(exportOpts);
		dialog.setVisible(true);
		return dialog.exportOpts;
	}

	private void setParams(DeploymentExportOpts exportOpts) {
		this.exportOpts = exportOpts;
		maxGap.setText(String.format("%d", exportOpts.maxRecordingGapSeconds));
		minLength.setText(String.format("%d", exportOpts.minRecordingLengthSeconds));
	}

	@Override
	public boolean getParams() {
		try {
			exportOpts.maxRecordingGapSeconds = Integer.valueOf(maxGap.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid inter recording interval");
		}
		try {
			exportOpts.minRecordingLengthSeconds = Integer.valueOf(minLength.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid minimum recording length");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		exportOpts = null;
	}

	@Override
	public void restoreDefaultSettings() {
		DeploymentExportOpts defaults = new DeploymentExportOpts();
	}

}
