package tethys.deployment.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.DutyCycleInfo;
import tethys.deployment.SepDeployment;

public class DeploymentDataCard extends PamWizardCard {

	private TethysControl tethysControl;
	private DeploymentHandler deploymentHandler;
	
	private JRadioButton[] numDocuments;

	private JTextField[] dataStores;
//	private JTextField rawURI, binaryURI, databaseURI;
	private ArrayList<OfflineDataStore> offlineDataStores;
	private JLabel dutyCycle;

	public DeploymentDataCard(PamWizard pamWizard, TethysControl tethysControl) {
		super(pamWizard, "Data");
		this.tethysControl = tethysControl;
		deploymentHandler = tethysControl.getDeploymentHandler();
		ButtonGroup bg = new ButtonGroup();
		
		SepDeployment[] sepOpts = SepDeployment.values();
		numDocuments = new JRadioButton[sepOpts.length];
		for (int i = 0; i < sepOpts.length; i++) {
			numDocuments[i] = new JRadioButton(sepOpts[i].toString());
			numDocuments[i].setToolTipText(sepOpts[i].getTip());
			bg.add(numDocuments[i]);
		}

		JPanel optsPanel = new PamAlignmentPanel(new GridBagLayout(), BorderLayout.WEST);
		optsPanel.setBorder(new TitledBorder("Number of documents"));
		GridBagConstraints c = new PamGridBagContraints();
		for (int i = 0; i < numDocuments.length; i++) {
			c.gridwidth = 2;
			optsPanel.add(numDocuments[i], c);
			c.gridy++;
		}
		c.gridwidth = 1;
		optsPanel.add(new JLabel("Duty cycle: ", JLabel.RIGHT), c);
		c.gridx++;
		optsPanel.add(dutyCycle = new JLabel(" "), c);


		JPanel dataPanel = new JPanel(new GridBagLayout());
		dataPanel.setBorder(new TitledBorder("Data location"));
		
		// automatically generate fields for every offline data store. 
		offlineDataStores = PamController.getInstance().findOfflineDataStores();
		dataStores = new JTextField[offlineDataStores.size()];
		c = new PamGridBagContraints();
		for (int i = 0; i < offlineDataStores.size(); i++) {
			OfflineDataStore aStore = offlineDataStores.get(i);
			dataPanel.add(new JLabel(aStore.getDataSourceName() + " ", JLabel.RIGHT), c);
			c.gridx++;
			dataStores[i] = new JTextField(40);
			dataPanel.add(dataStores[i], c);
			c.gridx = 0;
			c.gridy++;
		}
				
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(optsPanel);
		this.add(dataPanel);
	}


	@Override
	public boolean getParams(Object cardParams) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setParams(Object cardParams) {
		for (int i = 0; i < offlineDataStores.size(); i++) {
			OfflineDataStore aStore = offlineDataStores.get(i);
			dataStores[i].setText(aStore.getDataLocation());
		}
		DutyCycleInfo dsInf = deploymentHandler.getDutyCycle();
		if (dsInf == null) {
			dutyCycle.setText("no duty cycle information");
		}
		else {
			dutyCycle.setText(dsInf.toString());
		}

	}

	public boolean getParams(DeploymentExportOpts exportOptions, Deployment deployment) {
		SepDeployment[] sepOpts = SepDeployment.values();
		boolean selOk = false;
		for (int i = 0; i < sepOpts.length; i++) {
			if (numDocuments[i].isSelected()) {
				exportOptions.sepDeployments = sepOpts[i];
				selOk = true;
			}
		}
		if (!selOk) {
			return false;
		}
		return true;
	}

	public void setParams(DeploymentExportOpts exportOptions, Deployment deployment) {

		/* 
		 * temp code to only allow export of multiple documents. 
		 */
//		exportOptions.separateDeployments = true;
//		exportOne.setEnabled(false);
//		exportOne.setToolTipText("Feature not yet enabled");

		SepDeployment[] sepOpts = SepDeployment.values();
		for (int i = 0; i < sepOpts.length; i++) {
			numDocuments[i].setSelected(exportOptions.sepDeployments == sepOpts[i]);
		}
		
		setParams(deployment);
	}

}
