package dataModelFX.structures;

import java.util.ArrayList;
import java.util.UUID;

import dataModelFX.ConnectionNodeParams;
import dataModelFX.connectionNodes.ModuleConnectionNode;
import dataModelFX.connectionNodes.PAMConnectionNode;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.structures.ExtensionSocketStructure;

/**
 * PAM extension structure
 * 
 * @author Jamie Macaulay
 *
 */
public class PamExtensionStructure extends ExtensionSocketStructure implements PAMConnectionNode {

	private PamStructureParams groupStructureParams;


	public PamExtensionStructure(ConnectionPane connectionPane) {
		super(connectionPane);
		groupStructureParams = new PamStructureParams(this); 

		final ContextMenu contextMenu = new ContextMenu();
		MenuItem paste = new MenuItem("Remove");
		contextMenu.getItems().addAll(paste);
		paste.setOnAction((action)->{
			removeStructure(); 
		});

		this.getConnectionNodeBody().setOnMousePressed((event)->{
			if (event.isSecondaryButtonDown()) {
				contextMenu.show(this.getConnectionNodeBody(), event.getScreenX(), event.getScreenY());
			}
		});

	}


	/**
	 * Remove the structure 
	 */
	private void removeStructure () {
		connectionPane.removeConnectionNode(this);
	}


	@Override
	public ConnectionNodeParams getConnectionNodeParams() {
		return groupStructureParams;
	}


	@Override
	public void updateParams() {
		groupStructureParams.layoutX = this.getConnectionNodeBody().getLayoutX();
		groupStructureParams.layoutY = this.getConnectionNodeBody().getLayoutY();
		//save a list of connection node ID's currently in the group structure. 
		ArrayList<ConnectionNode> subNodes = super.getChildConnectionNodes(); 

		UUID[] subNodeIds = new UUID[subNodes.size()]; 
		for (int i=0; i<subNodes.size(); i++) {
			subNodeIds[i] = ((PAMConnectionNode) subNodes.get(i)).getConnectionNodeParams().getID(); 
		}

		//System.out.println("NUMBER OF CHILDREN: " + subNodes.size()); 
		//get the node IDs
		groupStructureParams.childNodes = subNodeIds; 

		ArrayList<ConnectionNode> parentNodes = super.getParentConnectionNodes();

		//get the parent node IDs
		UUID[] parentNodeID = new UUID[parentNodes.size()]; 
		for (int i=0; i<parentNodes.size(); i++) {
			parentNodeID[i] = ((PAMConnectionNode) parentNodes.get(i)).getConnectionNodeParams().getID(); 
		}

		//System.out.println("NUMBER OF PARENTS: " + parentNodes.size()); 
		groupStructureParams.parentNodes = parentNodeID; 
	}

	@Override
	public void setConnectionNodeParams(ConnectionNodeParams usedStructInfo) {
		this.groupStructureParams=(PamStructureParams) usedStructInfo; 
	}

	@Override
	public void loadsettings() {
		//add the connection nodes the correct group. 
		ArrayList<ConnectionNode>  connectionNodes = this.getConnectionPane().getAllConnectionNodes(true); 

		//		System.out.println("PAMEXTENSION STRUCTURE: " + connectionNodes.size());
		//		System.out.println("PAMEXTENSION STRUCTURE: child nodes " + this.groupStructureParams.childNodes.length);
		//		System.out.println("PAMEXTENSION STRUCTURE: parent nodes " + this.groupStructureParams.parentNodes.length);


		for (int i=0; i<connectionNodes.size(); i++) {
			PAMConnectionNode pamConnectionNode = ((PAMConnectionNode) connectionNodes.get(i));
			if (connectionNodes.get(i)!=this) { //import to prevent this updating it'sown parames before loading is fully complete. 

				for (int j=0; j<this.groupStructureParams.childNodes.length; j++) {
					//System.out.println("PAMEXTENSION STRUCTURE: Searching for child node: " + j + " "+ this.groupStructureParams.childNodes[j] + " Found node: " + pamConnectionNode.getConnectionNodeParams().getID()+ " " + this.groupStructureParams.childNodes.length); 
					if (this.groupStructureParams.childNodes[j].equals(pamConnectionNode.getConnectionNodeParams().getID())) {
						//this node is inside the connection node. 
						((StandardConnectionNode) connectionNodes.get(i)).connectNode(this); 
					}
				}

				//check if a parent node 
				for (int j=0; j<this.groupStructureParams.parentNodes.length; j++) {
					//System.out.println("PAMEXTENSION STRUCTURE: Searching for parent node: " + j + " "+ 
					//this.groupStructureParams.parentNodes[j] + " Found node: " + pamConnectionNode.getConnectionNodeParams().getID()+ 
					//" " + this.groupStructureParams.parentNodes.length); 
					if (this.groupStructureParams.parentNodes[j].equals(pamConnectionNode.getConnectionNodeParams().getID())) {
						//System.out.println("Found parent!: attempting a connect"); 
						this.connectNode((StandardConnectionNode) connectionNodes.get(i)); 
					}
				}
			}
		}	

	}


}
