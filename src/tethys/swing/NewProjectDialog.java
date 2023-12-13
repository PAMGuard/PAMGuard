package tethys.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import metadata.PamguardMetaData;
import nilus.Deployment;
import tethys.TethysControl;

public class NewProjectDialog extends PamView.dialog.PamDialog {

	private static final long serialVersionUID = 1L;

	private static NewProjectDialog singleInstance;
	
	private JTextField projectName;
	
	private JTextField projectRegion;	
		
	private Deployment deployment;
	
	private NewProjectDialog(Window parentFrame, TethysControl tethysControl) {
		super(parentFrame, "New Project", false);
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Project details"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Project name ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(projectName = new JTextField(30), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Geographic region ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(projectRegion = new JTextField(30), c);
		
		projectName.setToolTipText("Name of project associated with this deployment.  Can be related to a geographic region, funding source, etc");
		projectRegion.setToolTipText("Name of geographic region (optional)");
		
		setDialogComponent(mainPanel);
	}
	
	public static Deployment showDialog(Window parent, TethysControl tethysControl, Deployment deployment) {
		if (singleInstance == null) {
			singleInstance = new NewProjectDialog(parent, tethysControl);
		}
		singleInstance.setParams(deployment);
		singleInstance.setVisible(true);
		return singleInstance.deployment;
	}

	private void setParams(Deployment deploymentData) {
		if (deploymentData == null) {
			return;
		}
		this.deployment = deploymentData;
		projectName.setText(deploymentData.getProject());
		projectRegion.setText(deploymentData.getRegion());
	}

	@Override
	public boolean getParams() {
		if (deployment == null) {
			return false;
		}
		deployment.setProject(projectName.getText());
		deployment.setRegion(projectRegion.getText());
		if (deployment.getProject() == null || deployment.getProject().length() == 0) {
			return showWarning("you must specify a project name");
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		deployment = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
