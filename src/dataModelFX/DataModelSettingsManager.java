package dataModelFX;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import dataModelFX.connectionNodes.PAMConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;

/**
 * The data model setting manager handles building and saving the data model
 * connection nodes and structure based on saved data and the current PAMGuard
 * data model.
 * 
 * @author Jamie Macaulay
 * 
 */
public class DataModelSettingsManager implements PamSettings {
	
	
	/**
	 * Reference to the data model frame. 
	 */
	private DataModelPaneFX dataModelPaneFX;
	
	
	/**
	 * The data model settings.
	 */
	private DataModelPaneFXSettings dataModelSettingsFX = new DataModelPaneFXSettings(); 

	
	/**
	 * Constructor for the data model settings manager. 
	 * @param dataModelPane - the data model pane. 
	 */
	public DataModelSettingsManager(DataModelPaneFX dataModelPane) {
		this.dataModelPaneFX=dataModelPane; 
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Called once at startup to put the correct nodes and structures initially in the data model. 
	 */
	public void loadSettings() {
		ConnectionNodeParams aConnectionNodeParams; 
		
		//place all the connection nodes on the connection pane
		//connect structures etc. 
		//add pamcontrolled units to module connection nodes and connect everything
		//check that the data model is correct. 
				
		StandardConnectionNode newConnectionNode; 
		for (int i = 0; i < this.dataModelSettingsFX.usedModuleInfos.size(); i++) {
			aConnectionNodeParams = dataModelSettingsFX.usedModuleInfos.get(i); 
			
			if (aConnectionNodeParams==null) {
				System.err.println("DataModelSettingsManager: saved settings are null: " + i); 
				continue; 
			}
			
//			System.err.println("DataModelSettingsManager: load saved settings: " + aConnectionNodeParams.toString() + 
//					"x,y " + aConnectionNodeParams.getLayoutX() + "," + aConnectionNodeParams.getLayoutY()); 

			
			//create the node
			newConnectionNode =  dataModelPaneFX.getConnectionNodeFactory().createConnectionNode(aConnectionNodeParams); 
			
			dataModelPaneFX.getConnectionPane().addNewConnectionNode(newConnectionNode, aConnectionNodeParams.getLayoutX(), aConnectionNodeParams.getLayoutY());
		}
		
		//Because connection structures are essentially invisible to the data model these must be connected to their children and parents. 
		//Do this by calling loadsettings. 
		ArrayList<ConnectionNode>  connectionNodes = dataModelPaneFX.getConnectionPane().getConnectionNodes(true); 
		for (int i=0; i<connectionNodes.size(); i++) {
			PAMConnectionNode pamConnectionNode = ((PAMConnectionNode) connectionNodes.get(i)); 
			pamConnectionNode.loadsettings();//this tells each node that settings need to be loaded. Will do nothing for some nodes. 
		}
				
//		System.out.println("DataModelSettingsManager: No nodes: " + dataModelPaneFX.getConnectionPane().getConnectionNodes().size()); 
		
	}

	/**
	 * Called once what settings have been updated.
	 */
	private void saveUsedModuleInfo() {

		//get all connection nodes from all connection panes and all connection structures. 
		ArrayList<ConnectionNode> modules = dataModelPaneFX.getConnectionPane().getAllConnectionNodes(true);
		
		ArrayList<ConnectionNodeParams> usedModules = new ArrayList<ConnectionNodeParams>();
		ConnectionNodeParams usedModuleInfoFX;
		for (int i = 0; i < modules.size(); i++) {
			((PAMConnectionNode) modules.get(i)).updateParams(); //make sure params are updates 
			usedModuleInfoFX = ((PAMConnectionNode) modules.get(i)).getConnectionNodeParams();
			usedModules.add(usedModuleInfoFX);
//			System.out.println("DataModelSettingsManager: Saving used module info: " + usedModuleInfoFX + 
//					"x,y " + usedModuleInfoFX.getLayoutX() + "," + usedModuleInfoFX.getLayoutY());
			
		}
		dataModelSettingsFX.usedModuleInfos = usedModules; 
	}

	
//	/**
//	 * Get the saved module position for a module. These are saved in a list in the
//	 * DataModelPaneFXSettings class.
//	 * 
//	 * @param pamControlledUnit - the PamControlledUnit
//	 * @return the modules saved position or null.
//	 */
//	private UsedModuleInfoFX getUsedModuleInfo(PamControlledUnit pamControlledUnit) {
//		if (dataModelPaneFX.getDataModelSettings().usedModuleInfos == null) {
//			System.err.println("Module list is null");
//			return null;
//		}
//		for (int i = 0; i < dataModelPaneFX.getDataModelSettings().usedModuleInfos.size(); i++) {
//			if (pamControlledUnit.getUnitName()
//					.equals(dataModelPaneFX.getDataModelSettings().usedModuleInfos.get(i).unitName)
//					&& pamControlledUnit.getUnitType()
//					.equals(dataModelPaneFX.getDataModelSettings().usedModuleInfos.get(i).getUnitType())
//					&& pamControlledUnit.getClass().getName()
//					.equals(dataModelPaneFX.getDataModelSettings().usedModuleInfos.get(i).className)) 
//			{
//				return dataModelSettingsFX.usedModuleInfos.get(i);
//			}
//		}
//		return null;
//	}
	
	
	@Override
	public long getSettingsVersion() {
		return DataModelPaneFXSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.dataModelSettingsFX = ((DataModelPaneFXSettings) pamControlledUnitSettings.getSettings()).clone();
		//load settings
		loadSettings();
		return true;
	}
	
	@Override
	public String getUnitName() {
		return "Data_Model";
	}

	@Override
	public String getUnitType() {
		return "Data_Model_Pane";
	}

	@Override
	public Serializable getSettingsReference() {
		saveUsedModuleInfo();
		return this.dataModelSettingsFX.clone();
	}


}
