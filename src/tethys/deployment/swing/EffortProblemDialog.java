package tethys.deployment.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tethys.deployment.DeploymentOverview;
import tethys.deployment.RecordingList;

/**
 * Handle problems when binary and raw effort don't add up
 * @author dg50
 *
 */
public class EffortProblemDialog extends PamDialog {
	
	private JRadioButton useRaw, useBinary, useNeither;
	
	private JLabel generalInfo;
	
	private InfoSet[] infoSets = new InfoSet[2];

	private RecordingList chosenList;

	private DeploymentOverview deploymentOverview;
	
	private static EffortProblemDialog singleInstance;
	
	private static final String[] setNames = {"Raw data", "Binary data"};

	private EffortProblemDialog(Window parentFrame) {
		super(parentFrame, "Deployment Effort", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Effort information"));
		String info = "<html>There is a mismatch between the time period covered by the raw<br>"
				+ "data recordings and the time covered in the binary data.<br> "
				+ "Select the one you wish to use, or Cancel and sort out your data<br>"
				+ "prior to restarting the Tethys export process</html>";
		generalInfo = new JLabel(info);
//		generalInfo.setBorder(new TitledBorder("General"));
		mainPanel.add(generalInfo, BorderLayout.NORTH);
		JPanel botPanel = new JPanel(new GridLayout(2, 1));
		mainPanel.add(botPanel, BorderLayout.CENTER);
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 2; i++) {
			GridBagConstraints c = new PamGridBagContraints();
			JPanel subPanel = new JPanel(new GridBagLayout());
			botPanel.add(subPanel);
			infoSets[i] = new InfoSet(setNames[i]);
			c.gridwidth = 2;
			subPanel.add(infoSets[i].name, c);
			c.gridx += c.gridwidth;
			subPanel.add(infoSets[i].select, c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 1;
			subPanel.add(new JLabel("Start: ", JLabel.RIGHT), c);
			c.gridx++;
			subPanel.add(infoSets[i].start, c);
			c.gridx++;
			subPanel.add(new JLabel("End: ", JLabel.RIGHT), c);
			c.gridx++;
			subPanel.add(infoSets[i].end, c);
			c.gridy++;
			c.gridx = 0;
			subPanel.add(new JLabel("Duration: ", JLabel.RIGHT), c);
			c.gridx++;
			subPanel.add(infoSets[i].duration, c);
			c.gridx++;
			subPanel.add(new JLabel("Coverage: ", JLabel.RIGHT), c);
			c.gridx++;
			subPanel.add(infoSets[i].occupancy, c);

			bg.add(infoSets[i].select);
		}
		
		setDialogComponent(mainPanel);
		setResizable(true);
	}
	
	public static RecordingList showDialog(Window parentFrame, DeploymentOverview deploymentOverview) {
		singleInstance = new EffortProblemDialog(parentFrame);
		singleInstance.setData(deploymentOverview);
		singleInstance.setVisible(true);
		return singleInstance.chosenList;
	}

	private void setData(DeploymentOverview deploymentOverview) {
		this.deploymentOverview = deploymentOverview;
		RecordingList rl;
		for (int i = 0; i < 2; i++) {
			if (i == 0) {
				rl = deploymentOverview.getRawDataList();
			}
			else {
				rl = deploymentOverview.getBinaryDataList();
			}
			infoSets[i].start.setText(PamCalendar.formatDBDateTime(rl.getStart()));
			infoSets[i].end.setText(PamCalendar.formatDBDateTime(rl.getEnd()));
			infoSets[i].duration.setText(PamCalendar.formatDuration(rl.duration()));
			infoSets[i].occupancy.setText(String.format("%3.0f%%", rl.getCoverage()*100.));
		}
		invalidate();
		pack();
	}

	@Override
	public boolean getParams() {
		if (infoSets[0].select.isSelected()) {
			chosenList = deploymentOverview.getRawDataList();
			return true;
		}
		if (infoSets[1].select.isSelected()) {
			chosenList = deploymentOverview.getBinaryDataList();
			return true;
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class InfoSet {
		JLabel name, start, end, duration, occupancy;
		JCheckBox select;
		/**
		 * 
		 */
		public InfoSet(String name) {
			super();
			this.name = new JLabel(name);
			this.start = new JLabel("                   ");
			this.end = new JLabel("                        ");
			this.select = new JCheckBox("Select " + name);
			duration = new JLabel(" ");
			occupancy = new JLabel(" ");
		}
	}

}
