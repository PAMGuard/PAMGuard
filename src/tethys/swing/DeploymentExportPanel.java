package tethys.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.FolderInputSystem;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.WestAlignedPanel;
import binaryFileStorage.BinaryStore;
import generalDatabase.DBControlUnit;
import metadata.deployment.DeploymentData;
import nilus.Deployment;
import nilus.Deployment.Data;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.dbxml.DBXMLConnect;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.RecordingPeriod;
import tethys.niluswraps.PDeployment;

public class DeploymentExportPanel extends TethysGUIPanel implements DeploymentTableObserver {
	
	private JPanel mainPanel;
	
	private JButton showAllDeployments, bigExportButton;
	
	private JTextField site, cruise;
//	, region; don't inlude region here - it's set with the NewProject along with the project name. 
	// the stuff here may vary within a project. 
	private JTextField rawURI, binaryURI, databaseURI;
	private JTextField contact, date;
	
	private JComboBox<String> projectDeployments;

	private ArrayList<PDeployment> tethysDeploys;

	private PAMGuardDeploymentsTable pamDeploymentsTable;

	private ArrayList<RecordingPeriod> selectedDeployments;

	public DeploymentExportPanel(TethysControl tethysControl, PAMGuardDeploymentsTable pamDeploymentsTable) {
		super(tethysControl);
		this.pamDeploymentsTable = pamDeploymentsTable;
		mainPanel = new PamAlignmentPanel(BorderLayout.NORTH);
				mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Deployment Detail"));
		GridBagConstraints c = new PamGridBagContraints();
		showAllDeployments = new JButton("Show project deployments");
		showAllDeployments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tethysControl.showProjectDeploymentsDialog();
			}
		});
		site = new JTextField(40);
		cruise = new JTextField(20);
		rawURI = new JTextField(20);
		binaryURI = new JTextField(20);
		databaseURI = new JTextField(20);
		contact = new JTextField(20);
		date = new JTextField(20);
		date.setEditable(false);
		projectDeployments = new JComboBox<String>();
		projectDeployments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectExistingDeployment();
			}
		});
		
//		c.gridx = 1;
//		mainPanel.add(showAllDeployments, c);
//		c.gridwidth = 1;
		addPair("Site ", site, c);
		addPair("Cruise ", cruise, c);
		addPair("Raw data URI ", rawURI, c);
		addPair("Binary data URI ", binaryURI, c);
		addPair("Database URI ", databaseURI, c);
		addPair("Contact ", contact, c);
		addPair("Date ", date, c);
		addPair("Set from ", projectDeployments, c);
		
		bigExportButton = new JButton("Export selection");
		bigExportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportButtonPressed();
			}
		});
		c.gridx = 1;
		c.gridy++;
		mainPanel.add(bigExportButton, c);
		
		
	}

	protected void selectExistingDeployment() {
		int row = projectDeployments.getSelectedIndex();
		if (row < 0 || tethysDeploys == null) {
			return;
		}

		if (row >= tethysDeploys.size()) {
			return;
		}
		PDeployment deployment = tethysDeploys.get(row);
		String msg = "Do you want to copy settings from deploymnet document " + deployment.deployment.getId();
		int ans = WarnOnce.showWarning("Deployment data", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			copyDeploymentData(deployment.deployment);
		}
	}

	private void copyDeploymentData(Deployment deployment) {
		DeploymentData globalMeta = getTethysControl().getGlobalDeplopymentData();
		globalMeta.setSite(deployment.getSite());
		globalMeta.setCruise(deployment.getCruise());
		globalMeta.setRegion(deployment.getRegion());
		setInternal();
	}

	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		switch (tethysState.stateType) {
		case NEWPAMGUARDSELECTION:
			setInternal();
			setDefaultStores();
			enableControls();
			break;
		case NEWPROJECTSELECTION:
			updateDeployments();
			enableControls();
			break;
		case UPDATEMETADATA:
			setInternal();
			break;
		}
	}

	private void updateDeployments() {
		tethysDeploys = null;
		projectDeployments.removeAllItems();
		ArrayList<PDeployment> deploys = getTethysControl().getDeploymentHandler().getProjectDeployments();
		if (deploys == null) {
			return;
		}
		for (PDeployment aDep : deploys) {
			projectDeployments.addItem(aDep.getShortDescription());
		}
		tethysDeploys = deploys;
	}

	private void addPair(String label, JComponent component, GridBagConstraints c) {
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel(label, JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(component, c);
	}

	@Override
	public JComponent getComponent() {
		// TODO Auto-generated method stub
		return mainPanel;
	}
	
	/**
	 * Set the parms from internally stored data. 
	 */
	private void setInternal() {
		DeploymentData globalMeta = getTethysControl().getGlobalDeplopymentData();
		site.setText(globalMeta.getSite());
		cruise.setText(globalMeta.getCruise());
//		region.setText(globalMeta.getRegion());
		date.setText(PamCalendar.formatDBDateTime(System.currentTimeMillis()));
	}
	
	private void setDefaultStores() {
		
		DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
		binaryURI.setText(deploymentHandler.getBinaryDataURI());
		databaseURI.setText(deploymentHandler.getDatabaseURI());
		rawURI.setText(deploymentHandler.getRawDataURI());
		
//		BinaryStore binStore = BinaryStore.findBinaryStoreControl();
//		if (binStore != null) {
//			binaryURI.setText(binStore.getBinaryStoreSettings().getStoreLocation());
//		}
//		
//		DBControlUnit databaseControl = DBControlUnit.findDatabaseControl();
//		if (databaseControl != null) {
//			databaseURI.setText(databaseControl.getLongDatabaseName());
//		}
//		
//		try {
//		PamControlledUnit daq = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
//		if (daq instanceof AcquisitionControl) {
//			AcquisitionControl daqCtrl = (AcquisitionControl) daq;
//			DaqSystem system = daqCtrl.findDaqSystem(null);// getAcquisitionProcess().getRunningSystem();
//			if (system instanceof FolderInputSystem) {
//				FolderInputSystem fip = (FolderInputSystem) system;
//				rawURI.setText(fip.getFolderInputParameters().recentFiles.get(0));
//			}
//		}
//		}
//		catch (Exception e) {
//			rawURI.setText("unknown");
//		}
		
	}

	@Override
	public void selectionChanged() {
		selectedDeployments = pamDeploymentsTable.getSelectedDeployments();
		enableControls();
	}

	protected void exportButtonPressed() {
		if (selectedDeployments == null || selectedDeployments.size() == 0) {
			return;
		};
		getTethysControl().getDeploymentHandler().exportDeployments(selectedDeployments);
	}
	

	private void enableControls() {
		boolean enable = selectedDeployments != null && selectedDeployments.size() > 0;
		bigExportButton.setEnabled(enable);
	}

}
