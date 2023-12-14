package metadata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import metadata.swing.MetaDataDialog;

/**
 * Class to handle Project MetaData. Am making this a PAMControlledUnit, but may never
 * register it with the model ? Will see what advantages and disadvantages there are. 
 * @author dg50
 *
 */
public class MetaDataContol extends PamControlledUnit implements PamSettings {

	public static final String unitType = "Meta Data";
	
	private static MetaDataContol singleInstance;
	
	private PamguardMetaData pamguardMetaData = new PamguardMetaData();
	
//	private ParameterSetManager<DeploymentData> deploymentSetManager;
	
	
	private MetaDataContol(String unitName) {
		super(unitType, unitName);
//		deploymentSetManager = new ParameterSetManager<DeploymentData>(deploymentData, "Deployment Data");
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Easy getter for singleton MetaData controller. 
	 * @return meta data controller
	 */
	public static MetaDataContol getMetaDataControl() {
		if (singleInstance == null) {
			singleInstance = new MetaDataContol(unitType);
			// add this line to add it to the main modules list. Then it will get menu's, etc. 
			PamController.getInstance().addControlledUnit(singleInstance);	
		}
		return singleInstance;
	}
	
	/**
	 * Get PAMGuard Metadata. This contains a nilus Deployment object wrapped up 
	 * so that it can be serialised into other PAMGuard settings. 
	 * @return PAMGuard meta data
	 */
	public PamguardMetaData getMetaData() {
		return pamguardMetaData;
	}
	
	/**
	 * Set the meta data object. 
	 * @param metaData
	 */
	public void setMetaData(PamguardMetaData metaData) {
		this.pamguardMetaData = metaData;
	}

	@Override
	public Serializable getSettingsReference() {
		pamguardMetaData.checkSerialisation();
		return pamguardMetaData;
	}

	@Override
	public long getSettingsVersion() {
		return PamguardMetaData.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		Object obj = pamControlledUnitSettings.getSettings();
		if (obj instanceof PamguardMetaData) {
			pamguardMetaData = (PamguardMetaData) obj;
			return true;
		}
		return false;
	}

//	@Override
	public JMenuItem createMenu(JFrame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Project information ...");
		menuItem.setToolTipText("General project objectives, region, etc.");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showDialog(JFrame parentFrame) {
		PamguardMetaData newData = MetaDataDialog.showDialog(parentFrame, pamguardMetaData);
		if (newData != null) {
			this.pamguardMetaData = newData;
			// send around a notification ? 
		}
	}



}
