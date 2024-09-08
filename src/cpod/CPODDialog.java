package cpod;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

@Deprecated
public class CPODDialog extends PamDialog {

	private CPODControl cpodControl;

	private CPODParams cpodParams;

	private static CPODDialog singleInstance;
	
	private SelectFolder selectFolder;
	
	private JTextField startOffset, timeStretch;
	
	private CPODDialog(Window parentFrame, CPODControl cpodControl) {
		super(parentFrame, cpodControl.getUnitName() + " settings", false);
		this.cpodControl = cpodControl;
		selectFolder = new SelectFolder(cpodControl.getUnitName() + " Folder", 50, true);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(selectFolder.getFolderPanel());
		JPanel sPanel = new JPanel(new GridBagLayout());
		sPanel.setBorder(new TitledBorder("Time Corrections"));
		GridBagConstraints c = new PamGridBagContraints();
		sPanel.add(new JLabel("Start offset ", SwingConstants.RIGHT), c);
		c.gridx++;
		sPanel.add(startOffset = new JTextField(5), c);
		startOffset.setToolTipText("Enter the start time offset in seconds");
		c.gridx++;
		sPanel.add(new JLabel(" s", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		sPanel.add(new JLabel("Time strestching ", SwingConstants.RIGHT), c);
		c.gridx++;
		sPanel.add(timeStretch = new JTextField(5), c);
		timeStretch.setToolTipText("Time streth is in parts per million");
		c.gridx++;
		sPanel.add(new JLabel(" ppm", SwingConstants.LEFT), c);
		JPanel soPanel = new JPanel(new BorderLayout());
		soPanel.add(BorderLayout.WEST, sPanel);
		mainPanel.add(soPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static CPODParams showDialog(Window frame, CPODControl cpodControl, CPODParams cpodParams) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.cpodControl != cpodControl) {
			singleInstance = new CPODDialog(frame, cpodControl);
		}
		singleInstance.cpodParams = cpodParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.cpodParams;
	}

	private void setParams() {
		selectFolder.setFolderName(cpodParams.offlineFolder);
		selectFolder.setIncludeSubFolders(cpodParams.subFolders);
		startOffset.setText(String.format("%3.3f", cpodParams.startOffset));
		timeStretch.setText(Double.toString(cpodParams.timeStretch));
	}

	@Override
	public boolean getParams() {
		cpodParams.offlineFolder = selectFolder.getFolderName(true);
		cpodParams.subFolders = selectFolder.isIncludeSubFolders();
		try {
			cpodParams.startOffset = Double.valueOf(startOffset.getText());
			cpodParams.timeStretch = Double.valueOf(timeStretch.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		cpodParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		startOffset.setText("0.0");
		timeStretch.setText("0.0");
	}

}
