package metadata;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamModel.parametermanager.ParameterSetManager;
import generalDatabase.parameterstore.ParameterDatabaseStore;
import metadata.deployment.DeploymentData;

public class MetaDataContol extends PamControlledUnit {

	public static final String unitType = "Meta Data";
	
	private DeploymentData deploymentData = new DeploymentData();
	
	private ParameterSetManager<DeploymentData> deploymentSetManager;
	
	
	public MetaDataContol(String unitName) {
		super(unitType, unitName);
		deploymentSetManager = new ParameterSetManager<DeploymentData>(deploymentData, "Deployment Data");
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		return deploymentSetManager.getMenuItem(parentFrame);
	}

}
