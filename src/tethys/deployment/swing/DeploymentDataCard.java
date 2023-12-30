package tethys.deployment.swing;

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
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.DeploymentHandler;

public class DeploymentDataCard extends PamWizardCard {

	private TethysControl tethysControl;
	private DeploymentHandler deploymentHandler;
	
	private JRadioButton exportOne, exportMany;

	private JTextField[] dataStores;
//	private JTextField rawURI, binaryURI, databaseURI;
	private ArrayList<OfflineDataStore> offlineDataStores;

	public DeploymentDataCard(PamWizard pamWizard, TethysControl tethysControl) {
		super(pamWizard, "Data");
		this.tethysControl = tethysControl;
		deploymentHandler = tethysControl.getDeploymentHandler();
		ButtonGroup bg = new ButtonGroup();
		exportOne = new JRadioButton("Export a single detection document for all data");
		exportMany = new JRadioButton("Export separate documents for each ad-hoc recording period");
		bg.add(exportOne);
		bg.add(exportMany);
		
		JPanel optsPanel = new JPanel(new GridBagLayout());
		optsPanel.setBorder(new TitledBorder("Number of documents"));
		GridBagConstraints c = new PamGridBagContraints();
		optsPanel.add(exportOne, c);
		c.gridy++;
		optsPanel.add(exportMany, c);

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

	}

	public boolean getParams(DeploymentExportOpts exportOptions, Deployment deployment) {
		exportOptions.separateDeployments = exportMany.isSelected();
		return true;
	}

	public void setParams(DeploymentExportOpts exportOptions, Deployment deployment) {
		exportOne.setSelected(exportOptions.separateDeployments == false);
		exportMany.setSelected(exportOptions.separateDeployments == true);
		setParams(deployment);
	}

}
