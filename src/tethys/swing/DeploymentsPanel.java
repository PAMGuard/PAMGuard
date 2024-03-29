package tethys.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.calibration.CalibrationHandler;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.RecordingList;
import tethys.deployment.RecordingPeriod;

public class DeploymentsPanel extends TethysExportPanel implements DeploymentTableObserver {
	
//	private JPanel mainPanel;
	
	private PAMGuardDeploymentsTable pamDeploymentsTable;
	
	private DeploymentExportPanel exportPanel;
	
	private JLabel effortName;

	public DeploymentsPanel(TethysControl tethysControl) {
		super(tethysControl, tethysControl.getDeploymentHandler(), true);
		DeploymentHandler deploymentHandler = tethysControl.getDeploymentHandler();
		pamDeploymentsTable = new PAMGuardDeploymentsTable(tethysControl);
		exportPanel = new DeploymentExportPanel(tethysControl, pamDeploymentsTable);
		pamDeploymentsTable.addObserver(exportPanel);
		
		JPanel mainPanel = getMainPanel();
		mainPanel.setBorder(new TitledBorder("Recording periods and deployment information"));
		pamDeploymentsTable.addObserver(this);
		pamDeploymentsTable.addObserver(deploymentHandler);
		
		effortName = new JLabel("   ");
		JPanel centralPanel = new JPanel(new BorderLayout());
		centralPanel.add(BorderLayout.NORTH, effortName);
		centralPanel.add(BorderLayout.CENTER,pamDeploymentsTable.getComponent());
		mainPanel.add(BorderLayout.CENTER, centralPanel);
	}

	@Override
	public void selectionChanged() {
		enableExportButton();
	}
	
	private void enableExportButton() {
		if (!getTethysControl().isServerOk()) {
			disableExport("Tethys server not running");
			return;
		}
		
		CalibrationHandler calHandler = getTethysControl().getCalibrationHandler();
		if (calHandler.haveAllChannelCalibrations() == false) {
			disableExport("Calibration data for each channel must be exported before creating Deployment documents");
			return;
		}
		
		ArrayList<RecordingPeriod> selected = pamDeploymentsTable.getSelectedPeriods();
		if (selected == null || selected.size() == 0) {
			disableExport("You must select one or more deployment periods to export");
			return;
		}
		boolean existing = false;
		if (selected != null) {
			// and see if any warnings are needed: basically if anything selected has an output.
			for (RecordingPeriod aPeriod: selected) {
				if (aPeriod.getMatchedTethysDeployment() != null) {
					existing = true;
					break;
				}
			}
		}
		String warning = null;
		if (existing) {
			warning = "One or more deployment documents already exist. These must be deleted prior to exporting new documents";
			disableExport(warning);
			return;
		}
		
		enableExport(true);
	}

	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		enableExportButton();
		RecordingList recordingList = pamDeploymentsTable.getMasterList();
		if (recordingList == null) {
			effortName.setText("  No available effort data");
		}
		else {
			effortName.setText("  Effort from " + recordingList.getSourceName());
		}
	}

	@Override
	protected void exportButtonPressed(ActionEvent e) {
		getTethysControl().getDeploymentHandler().exportDeployments();
	}

	@Override
	protected void optionsButtonPressed(ActionEvent e) {
		getTethysControl().getDeploymentHandler().showOptions(null);
	}



}
