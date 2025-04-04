package dataModelFX.connectionNodes;

import Array.ArrayManager;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamModel.DependencyManager;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import binaryFileStorage.BinaryStore;
import dataModelFX.ConnectionNodeParams;
import dataModelFX.ConnectionNodeParams.PAMConnectionNodeType;
import dataModelFX.DataModelModulePane.StructureRectangle;
import generalDatabase.DBControl;
import dataModelFX.DataModelPaneFX;
import dataModelFX.ModuleNodeParams;
import dataModelFX.structures.PamExtensionStructure;
import dataModelFX.structures.PamGroupStructure;
import javafx.stage.Stage;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionGroupStructure;
import pamViewFX.fxNodes.connectionPane.structures.ExtensionSocketStructure;
import userDisplayFX.UserDisplayControlFX;

/**
 * Creates connection nodes from both saved parameters and when new modules and/or 
 * as structures are added manually. 
 */
public class ConnectionNodeFactory {

	/**
	 * Reference to the data model the connection factory is associated with. 
	 */
	private DataModelPaneFX dataModelFX;

	public ConnectionNodeFactory(DataModelPaneFX dataModel) {
		this.dataModelFX = dataModel; 
	}


	/**
	 * Add an instance of this module type to the data model. 
	 * @param primaryStage- the primary stage	
	 * @return the new PamControlledUnit if added successfully. Null if the module was not added successfully. 
	 */
	public PamControlledUnit addModule(Stage primaryStage, PamModuleInfo pamModuleInfo){
		// first check dependencies to see if everything required
		// by this module actually exists
		try {
			if (pamModuleInfo.getDependency() != null && PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				DependencyManager dependencyManager = PamModel.getPamModel().getDependencyManager();
				dependencyManager.checkDependency(null, pamModuleInfo, true);
			}
			return PamController.getInstance().addModule(null, pamModuleInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Called whenever there is an attempt to add a new module to the pane. 
	 * @param pamModuleInfo - the 
	 * @param x -x position of dropped module	 
	 * @param y	- y position of dropped module
	 */
	public void addNewModule(PamModuleInfo pamModuleInfo, double x, double y) {
		try {


			//System.out.println(pamModuleInfo.getDefaultName()+ " module dropped at x: "+x+" y: "+y);
			//			lastDropPos=new Point2D(x,y);
			PamControlledUnit pamControlledUnit=addModule(PamController.getMainStage(), pamModuleInfo);

			if (pamControlledUnit==null){
				/**
				 * Show error message. This should NEVER be called as dependency manager should take 
				 * care of any issues.
				 */
				System.err.println("Error adding module: The controlled unit is null ");
			}

			//NOW 
			System.err.println("Error adding module: The controlled unit is null ");


			//DOUBLE CHECK - 
			//Now a notification will be sent via the notifyModelChanged function. 
			//This is where the new ModuleConnectionNode will be added.
			dataModelFX.getModuleSelectPane().updateModuleListPane(); 
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Adds a new connection node to the 
	 * @param pamController - the pamControlledUnit
	 * @param x - the x position on the connection pane to place the node.
	 * @param y - the y position on the connection pane to place the node
	 * @return 
	 */
	public ModuleConnectionNode addNewModule(PamControlledUnit pamControlledUnit, double x, double y) {
		// TODO Auto-generated method stub
		ModuleConnectionNode connectionNode =  createConnectionnNode(pamControlledUnit);
		if (connectionNode!=null) {
			//the data model is added the pane.
			dataModelFX.getConnectionPane().addNewConnectionNode(connectionNode , x, y);
			return connectionNode; 
		}
		return null; 
	}

	/**
	 * Called whenever there is an attempt to add a structure to the pane. 
	 * @param structureRectangle - the structure node
	 * @param x - the pixel x co-ordinate
	 * @param y - the pixel y co-ordinate
	 */
	public StandardConnectionNode addNewStructure(StructureRectangle structureRectangle, double x, double y) {
				System.out.println("DataModelConnectPane: add new structure " + structureRectangle.getStructureType() );
		StandardConnectionNode connectionNode = createConnectionNode(structureRectangle.getStructureType(), null);

		if (connectionNode!=null) {
			//the data model is added the pane.
			dataModelFX.getConnectionPane().addNewConnectionNode(connectionNode , x, y);
			return connectionNode; 
		}
		return null; 
	}


	/**
	 * Create a connection node from a structure rectangle 
	 * @param structureRectangle - the structure rectangle to create. 
	 * @return the new structure conneciton node. 
	 */
	public StandardConnectionNode createConnectionStructure(StructureRectangle structureRectangle) {
		return createConnectionNode(structureRectangle.getStructureType(), null);  
	}


	//	/**
	//	 * Create a connection from saved connection node parameters. This can be a structure or a ModuleConnectionNode
	//	 * @param params - the parameters. 
	//	 * @return the PAMConnectionNode. 
	//	 */
	//	public StandardConnectionNode createConnectionNode(ConnectionNodeParams params) {
	//		StandardConnectionNode connectionNode = createConnectionNode(params.getNodeType());
	//		((PAMConnectionNode) connectionNode).setConnectionNodeParams(params);
	//		return connectionNode;
	//	}


	/**
	 * Create a new connection node. Will create sub class nodes if appropriate, 
	 * @param pamControlledUnit - the pmaControlledUnit for the module
	 * @return a new module node. 
	 */
	public ModuleConnectionNode createConnectionnNode(String unitType){

		//TODO
		//////THIS IS ALL A BIT MESSY- NEATER WAY TO DO THIS//////

		//System.out.println("NEW MODULE CONNECTION NODE: " + pamControlledUnit);

		//create node or specialist node. 
		ModuleConnectionNode newConnectionNode;
		if (unitType.equals(BinaryStore.defUnitType)){
			newConnectionNode=new BinaryConnectionNode(dataModelFX.getConnectionPane()); 
			newConnectionNode.setCore(true);
		}
		else if (unitType.equals(DBControl.getDbUnitType())){
			//set DB ConnectioNode
			newConnectionNode=new DBConnectionNode(dataModelFX.getConnectionPane()); 
			newConnectionNode.setCore(true);
		}
		else if (unitType.equals(UserDisplayControlFX.defUnitType)){
			newConnectionNode=new DisplayConnectionNode(dataModelFX.getConnectionPane()); 
		}
		else {
			newConnectionNode=new ModuleConnectionNode(dataModelFX.getConnectionPane()); 
		}

		if (unitType.equals(ArrayManager.getArrayFileType())){
			newConnectionNode=new DisplayConnectionNode(dataModelFX.getConnectionPane()); 
			newConnectionNode.setCore(true);
		}

		//		System.out.println("DataModelFX: createConnectionnNode : "+pamControlledUnit.getPamModuleInfo() + "  "+ pamControlledUnit.getUnitName());
		//		if (pamControlledUnit.getPamModuleInfo().isCoreModule()) newConnectionNode.setCore(true);

		return newConnectionNode;
	}


	/**
	 * Create a new connection node. Will create sub class nodes if appropriate, 
	 * @param pamControlledUnit - the pmaControlledUnit for the module
	 * @return a new module node. 
	 */
	public ModuleConnectionNode createConnectionnNode(PamControlledUnit pamControlledUnit){

		ModuleConnectionNode newConnectionNode = createConnectionnNode(pamControlledUnit.getUnitType()); 

		newConnectionNode.setPamControlledUnit(pamControlledUnit);

		return newConnectionNode;
	}

	/**
	 * Create a connection node based on it's type. 
	 * @param connectionParams - the parameters. 
	 * @return the new connection node. 
	 */
	public StandardConnectionNode createConnectionNode( ConnectionNodeParams connectionParams) {
		return createConnectionNode(connectionParams.getNodeType(), connectionParams); 
	}


	/**
	 * Create a connection node based on it's type. 
	 * @param connectionType - the type of connection node to create
	 * @param connectionParams - the parameters. Can be null if a structure or for default ModuleConnectionNode.  
	 * @return the new connection node. 
	 */
	public StandardConnectionNode createConnectionNode(PAMConnectionNodeType connectionNodeType, ConnectionNodeParams connectionParams) {

		StandardConnectionNode connectionNode = null;
		switch (connectionNodeType) {
		case ModuleConnectionNode:
			connectionNode = createConnectionnNode(connectionParams==null? null : ((ModuleNodeParams) connectionParams).getUnitType()); 
			break; 
		case PAMGroupStructure:
			connectionNode = new PamGroupStructure(dataModelFX.getConnectionPane());
			break;
		case PAMExtensionStructure:
			connectionNode = new PamExtensionStructure(dataModelFX.getConnectionPane());
			break;
		default:
			break;
		}

		if (connectionParams!=null) {
			((PAMConnectionNode) connectionNode).setConnectionNodeParams(connectionParams);
		}

		return connectionNode; 
	}





}
