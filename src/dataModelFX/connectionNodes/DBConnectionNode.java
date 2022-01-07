package dataModelFX.connectionNodes;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import PamController.PamControlledUnit;
import PamController.StorageOptions;
import PamguardMVC.PamDataBlock;
import dataModelFX.DataModelConnectPane;
import dataModelFX.DataModelStyle;

/**
 * ConnectionNode for database module. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DBConnectionNode extends ModuleConnectionNode {
		
	/*
	 *Any file storage line has a different colour.  
	 */
	private Color connectionColour=DataModelStyle.outputFileLines;

	public DBConnectionNode(DataModelConnectPane connectionPane) {
		super(connectionPane);
		//set so module shows connecting lines from output of other modules. 
		this.setCoreOutput(false);
		this.setCoreLineColour(connectionColour);
		this.setCoreCircleColour(Color.color(connectionColour.getRed(), 
				connectionColour.getGreen(), connectionColour.getBlue()));
	}
	
	@Override
	public void setPamControlledUnit(PamControlledUnit pamControlledUnit){
		super.setPamControlledUnit(pamControlledUnit);
	}
	
	@Override
	public ArrayList<ConnectionNode> getCoreConnectionNodes(){

		ArrayList<ConnectionNode> connectionNodes=getConnectionPane().getConnectionNodes();
		
		ArrayList<ConnectionNode> allowedConnectionNodes=new ArrayList<ConnectionNode> (); 

		ModuleConnectionNode moduleConNode; 
		for (int i=0; i<connectionNodes.size(); i++){
			//safe cast to ModuleConnectionNode. 
			moduleConNode=(ModuleConnectionNode) connectionNodes.get(i); 
			if (canConnect(moduleConNode)){
				allowedConnectionNodes.add(moduleConNode);
			}
		}
		return allowedConnectionNodes;
	}


	/**
	 * Check if the database is used by another module and hence can connect to it. 
	 * @param moduleConNode - the node to check.
	 * @return true if the PamControlledUnit of the node subscribes to the database fdor storage. 
	 */
	private boolean canConnect(ModuleConnectionNode moduleConNode){
		for (int j=0; j<moduleConNode.getPamControlledUnit().getNumPamProcesses(); j++){
			for (int k=0; k<moduleConNode.getPamControlledUnit().getPamProcess(j).getNumOutputDataBlocks(); k++){
				if (canConnect(moduleConNode.getPamControlledUnit().getPamProcess(j).getOutputDataBlock(k))) return true; 
			}
		}
		return false; 
	}
	
	/**
	 * Check whether a datablock subscribes to the database to store data. 
	 * @param pamDataBlock- data block to check 
	 * @return true if the datablock does subscribe. 
	 */
	protected boolean canConnect(PamDataBlock pamDataBlock){
//		if (pamDataBlock.getShouldLog(null)){
//			//this does subscribe to database
//			System.out.println("The PamControlledUnit "+pamDataBlock.getDataName()+ 
//					" subscribed to the Database");
//			return true;
//		}
//		else return false; 
		
		boolean isDatabaseLogging=false;
		boolean hasDatabase = pamDataBlock.getLogging() != null;
		boolean hasBinaryStore = pamDataBlock.getBinaryDataSource() != null;
		if (hasBinaryStore) {
			hasBinaryStore = pamDataBlock.getBinaryDataSource().isDoBinaryStore();
		}
		
		if (hasDatabase) {
			isDatabaseLogging=StorageOptions.getInstance().getStorageParameters()
					.isStoreDatabase(pamDataBlock, !hasBinaryStore);
		}
		
		return isDatabaseLogging;
	}



}
