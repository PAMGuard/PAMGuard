package tethys.deployment.swing;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.WestAlignedPanel;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.SepDeployment;

/**
 * dialog accessed from corner of Deployments panel that controls duty cycles, merging, and 
 * potentially even discarding (of short) of effort periods in the data for both evenly duty cycled
 * and also for more adhoc data collection. 
 * @author dg50
 *
 */
public class RecordingGapDialog extends PamDialog {

	private JTextField maxGap, minLength;
	
	private DeploymentExportOpts exportOpts;

	private JRadioButton[] dutycycleButtons;
	
	private JLabel dutyCycle;
	
	private RecordingGapDialog(Window parentFrame) {
		super(parentFrame, "Maximum Gap", true);
		JPanel mainPanel = new JPanel(new BorderLayout());
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		
		// main duty cycle panel. 
		JPanel dsPanel = new JPanel(new GridBagLayout());
		dsPanel.setBorder(new TitledBorder("Duty Cycle"));
		PamGridBagContraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		ButtonGroup bg = new ButtonGroup();		
		ButtonChange bc = new ButtonChange();
		SepDeployment[] sepOpts = SepDeployment.values();
		dutycycleButtons = new JRadioButton[sepOpts.length];
		for (int i = 0; i < sepOpts.length; i++) {
			dutycycleButtons[i] = new JRadioButton(sepOpts[i].toString());
			dutycycleButtons[i].setToolTipText(sepOpts[i].getTip());
			dutycycleButtons[i].addActionListener(bc);
			bg.add(dutycycleButtons[i]);
			dsPanel.add(dutycycleButtons[i], c);
			c.gridy++;
		}
		c.gridwidth = 1;

		
		
		// lower panel for adhoc cycles ? 
		JPanel adhocPanel = new JPanel(new GridBagLayout());
		adhocPanel.setBorder(new TitledBorder("Max Recording Gap"));
		c = new PamGridBagContraints();
		adhocPanel.add(new JLabel("Maximum gap ", JLabel.RIGHT), c);
		c.gridx++;
		adhocPanel.add(maxGap = new JTextField(3), c);
		c.gridx++;
		adhocPanel.add(new JLabel(" seconds", JLabel.RIGHT), c);
		c.gridx = 0;
		c.gridy++;
		adhocPanel.add(new JLabel("Minimum length ", JLabel.RIGHT), c);
		c.gridx++;
		adhocPanel.add(minLength = new JTextField(3), c);
		c.gridx++;
		adhocPanel.add(new JLabel(" seconds", JLabel.RIGHT), c);
		
		maxGap.setToolTipText("Maximum gap between recording periods. Sequential periods with a gap less than this will be counted as one");
		minLength.setToolTipText("Minimum recording length. Recording sections shorter than this will be ignored");
		
		mainPanel.add(dsPanel, BorderLayout.NORTH);
		mainPanel.add(new PamAlignmentPanel(adhocPanel, BorderLayout.WEST, true), BorderLayout.SOUTH);
		setDialogComponent(mainPanel);
	}
	
	public static DeploymentExportOpts showDiloag(Window parent, DeploymentExportOpts exportOpts) {
		RecordingGapDialog dialog = new RecordingGapDialog(parent);
		dialog.setParams(exportOpts);
		dialog.setVisible(true);
		return dialog.exportOpts;
	}

	private class ButtonChange implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	
	private void setParams(DeploymentExportOpts exportOpts) {
		this.exportOpts = exportOpts;
		
		// main duty cycle options
		SepDeployment[] sepOpts = SepDeployment.values();
		for (int i = 0; i < sepOpts.length; i++) {
			dutycycleButtons[i].setSelected(sepOpts[i] == exportOpts.sepDeployments);
		}
		
		// other options
		maxGap.setText(String.format("%d", exportOpts.maxRecordingGapSeconds));
		minLength.setText(String.format("%d", exportOpts.minRecordingLengthSeconds));
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		// main duty cycle options
		SepDeployment[] sepOpts = SepDeployment.values();
		for (int i = 0; i < sepOpts.length; i++) {
			if (dutycycleButtons[i].isSelected()) {
				exportOpts.sepDeployments = sepOpts[i];
			}
		}
		if (exportOpts.sepDeployments == null) {
			return showWarning("You must select a duty cycle options");
		}
		
		// other options
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
	
	private void enableControls() {
		
	}

	@Override
	public void cancelButtonPressed() {
		exportOpts = null;
	}

	@Override
	public void restoreDefaultSettings() {
		DeploymentExportOpts defaults = new DeploymentExportOpts();
		setParams(defaults);
	}

}
