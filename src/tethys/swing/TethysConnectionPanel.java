package tethys.swing;

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

import Array.ArrayDialog;
import Array.ArrayManager;
import Array.PamArray;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.ScrollingPamLabel;
import PamView.dialog.SettingsButton;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import metadata.deployment.DeploymentData;
import nilus.Deployment;
import pamViewFX.fxNodes.PamComboBox;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.TethysTimeFuncs;
import tethys.dbxml.ServerStatus;
import tethys.deployment.PInstrument;
import tethys.niluswraps.PDeployment;
import tethys.output.TethysExportParams;

/**
 * Top strip of main Tethys GUI for connection and project information
 * @author dg50
 *
 */
public class TethysConnectionPanel extends TethysGUIPanel {
	
	private static final int SERVERNAMELENGTH = 30;
	private static final int SERVERSTATUSLENGTH = 20;
	
	private JPanel mainPanel;

	private JTextField serverName;
	
	private SettingsButton serverSelButton;
	
	private ScrollingPamLabel serverStatus;
	
	private JComboBox<String> projectList;
	
//	private JComboBox<PDeployment> deploymentList;
	
	private JButton newProjectButton;
	
	private JComboBox<PInstrument> projectInstruments;
	
	private JButton newInstrument;
	
	private JButton openClient;
	
	public TethysConnectionPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new WestAlignedPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Connection and project details"));
		serverName = new JTextField(SERVERNAMELENGTH);
		serverSelButton = new SettingsButton();
		serverSelButton.setToolTipText("Select server");
		serverStatus = new ScrollingPamLabel(SERVERSTATUSLENGTH);
		serverName.setEditable(false);
		openClient = new JButton("Open Client");
		openClient.setToolTipText("Open Tethys client in web browser");
//		serverStatus.setEditable(false);
		serverSelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectServer();
			}
		});
		openClient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tethysControl.openTethysClient();
			}
		});
		newProjectButton = new JButton("New project");
		newProjectButton.setToolTipText("Create new project information");
		newProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewProject();
			}
		});
		projectList = new JComboBox<>();
		projectList.setToolTipText("All projects present in the current Tethys database");
		projectList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newProjectSelect();
			}
		});
		projectInstruments = new JComboBox<PInstrument>();
		newInstrument = new JButton("New / Edit");
		projectInstruments.setToolTipText("Instruments currently listed within all deployments of the current project");
		newInstrument.setToolTipText("Edit or create a new instrument (uses PAMGuard Array dialog)");
		projectInstruments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newInstrumentSelect();
			}
		});
		newInstrument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewInstrument();
			}
		});
//		deploymentList = new JComboBox<PDeployment>();
		
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Tethys Server "), c);
		c.gridx++;
		mainPanel.add(serverName, c);
		c.gridx++;
		mainPanel.add(serverSelButton, c);
		c.gridx++;
		c.gridwidth = 2;
		mainPanel.add(serverStatus, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(openClient, c);
		
		c.gridx =0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Projects ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(projectList, c);
		c.gridx++;
		mainPanel.add(newProjectButton, c);
		
		// instrument section
		c.gridx++;
		mainPanel.add(new JLabel(" Instruments ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(projectInstruments, c);
		c.gridx++;
		mainPanel.add(newInstrument, c);
		
		
//		c.gridx = 0;
//		c.gridy++;
//		mainPanel.add(new JLabel("Deployments ", JLabel.RIGHT), c);
//		c.gridx += c.gridwidth;
//		c.gridwidth = 2;
//		mainPanel.add(deploymentList, c);
		
//		fillServerControl(); // no need Will get set from TethysControl as soon as all initialised. 
	}

	protected void newInstrumentSelect() {
		PInstrument pInstr = (PInstrument) projectInstruments.getSelectedItem();
		if (pInstr == null) {
			return;
		}
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		currentArray.setInstrumentType(pInstr.instrumentType);
		currentArray.setInstrumentId(pInstr.instrumentId);
		getTethysControl().sendStateUpdate(new TethysState(StateType.UPDATEMETADATA));
	}

	protected void createNewInstrument() {
		PamArray updatedArray = ArrayDialog.showDialog(getTethysControl().getGuiFrame(), ArrayManager.getArrayManager());
		if (updatedArray != null) {
			updateInstrumentsList();
		}
	}

	/**
	 * Action from new project button
	 */
	protected void createNewProject() {
		DeploymentData pamDeploymentData = getTethysControl().getGlobalDeplopymentData();
		pamDeploymentData = NewProjectDialog.showDialog(getTethysControl().getGuiFrame(), getTethysControl(), pamDeploymentData);
		if (pamDeploymentData != null) {
			updateProjectList();
		}
	}

	protected void newProjectSelect() {
		String project = (String) projectList.getSelectedItem();
		if (project == null) {
			return;
		}
		DeploymentData globData = getTethysControl().getGlobalDeplopymentData();
		globData.setProject(project);
		getTethysControl().getDeploymentHandler().updateProjectDeployments();
		/*
		 *  if there are existing deployment data, then copy the info to the
		 *  internal project information
		 */
		ArrayList<PDeployment> projectDeployments = getTethysControl().getDeploymentHandler().getProjectDeployments();
		if (projectDeployments != null && projectDeployments.size() > 0) {
			Deployment dep = projectDeployments.get(0).deployment;
			globData.setProject(dep.getProject());
			globData.setRegion(dep.getRegion());
			getTethysControl().sendStateUpdate(new TethysState(TethysState.StateType.NEWPROJECTSELECTION));
		}
		
		updateInstrumentsList();
//		fillDeploymentsList(project);
	}
	
	
//	private void fillDeploymentsList(String project) {
//		ArrayList<PDeployment> projectDeployments = getTethysControl().getDeploymentHandler().getProjectDeployments();
//		deploymentList.removeAllItems();
//		if (projectDeployments == null) {
//			return;
//		}
//		for (PDeployment dep : projectDeployments) {
////			String str = String.format("%s:%d, %s to %s", dep.getId(), dep.getDeploymentId(), 
////					TethysTimeFuncs.formatGregorianTime(dep.getDeploymentDetails().getAudioTimeStamp()), 
////					TethysTimeFuncs.formatGregorianTime(dep.getRecoveryDetails().getAudioTimeStamp()));
//			deploymentList.addItem(dep);
//		}
//	}

	protected void selectServer() {
		// will return the same object at the moment, so no need to do anything. 
		TethysExportParams newParams = SelectServerdDialog.showDialog(getTethysControl(), getTethysControl().getGuiFrame(), getTethysControl().getTethysExportParams());
		if (newParams != null) {
			getTethysControl().checkServer();//  sendStateUpdate(new TethysState(TethysState.StateType.UPDATESERVER));
		}
	}

	private void fillServerControl() {
		TethysExportParams exportParams = getTethysControl().getTethysExportParams();
		serverName.setText(exportParams.getFullServerName());
		ServerStatus status = getTethysControl().getDbxmlConnect().pingServer();
		serverStatus.setText(status.toString());

		colourBackground(status.ok ? 0 : 1);
	}


	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		if (tethysState.stateType == StateType.UPDATESERVER) {
			fillServerControl();
			updateProjectList();
		}
		
	}

	private void updateProjectList() {
		projectList.removeAllItems();
		/*
		 *  put the project name assigned within this PAMGuard config at the top of the
		 *  list.  
		 */
		String localProjName = null;
		DeploymentData pamDeploymentData = getTethysControl().getGlobalDeplopymentData();
		if (pamDeploymentData != null && pamDeploymentData.getProject() != null) {
			localProjName = pamDeploymentData.getProject();
			if (localProjName.length() == 0) {
				localProjName = null;
			}
		}
		if (localProjName != null) {
			projectList.addItem(localProjName);
		}
		
		ArrayList<String> projectNames = getTethysControl().getDbxmlQueries().getProjectNames();
		if (projectNames == null || projectNames.size() == 0) {
//			System.out.println("No existing projects");
			return;
		}
		for (int i = 0; i < projectNames.size(); i++) {
			String projName = projectNames.get(i);
			if (projName.equals(localProjName)) {
				continue;
			}
			projectList.addItem(projectNames.get(i));
		}
	}
	
	/**
	 * Update displayed list of instruments
	 */
	private void updateInstrumentsList() {
		projectInstruments.removeAllItems();
		PInstrument currentInstrument = getTethysControl().getDeploymentHandler().getCurrentArrayInstrument();
		if (currentInstrument != null) {
			projectInstruments.addItem(currentInstrument);
		}
		ArrayList<PInstrument> projectInst = getTethysControl().getDeploymentHandler().getProjectInstruments();
		if (projectInst == null) {
			return;
		}
		for (int i = 0; i < projectInst.size(); i++) {
			if (projectInst.get(i).equals(currentInstrument) == false) {
				projectInstruments.addItem(projectInst.get(i));
			}
		}
	}

	
	
}
