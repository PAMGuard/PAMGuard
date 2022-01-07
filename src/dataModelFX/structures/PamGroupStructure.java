package dataModelFX.structures;

import java.util.ArrayList;
import java.util.UUID;

import dataModelFX.ConnectionNodeParams;
import dataModelFX.DataModelConnectPane;
import dataModelFX.connectionNodes.ModuleConnectionNode;
import dataModelFX.connectionNodes.PAMConnectionNode;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionGroupStructure;

/**
 * Structure which groups together connection nodes. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamGroupStructure extends ConnectionGroupStructure implements PAMConnectionNode {

	private PamGroupStructureParams groupStructureParams;

	/**
	 * Text field showing the name of the group structure when it is expanded.
	 */
	private TextField textField;

	/**
	 * Button for deleting the pane. 
	 */
	private PamButton deleteButton;
	
	DataModelConnectPane connectionPane; 

	public PamGroupStructure(DataModelConnectPane connectionPane) {
		super(connectionPane);
		this.connectionPane=connectionPane; 
		groupStructureParams = new PamGroupStructureParams(this); 

		getConnectionNodeBody().getChildren().addAll(textField = new TextField()); 
		textField.setEditable(true);
		textField.setText(groupStructureParams.name);

		//text field should be above the group
		textField.layoutYProperty().bind(textField.heightProperty().multiply(-1).subtract(5));
		textField.layoutXProperty().bind(getConnectionNodeBody().widthProperty().divide(2).subtract(textField.widthProperty().divide(2)));
		textField.setAlignment(Pos.CENTER);

		
		//add a remove button
		deleteButton = new PamButton(); 
//		deleteButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.REMOVE, Color.GRAY, PamGuiManagerFX.iconSize));
		deleteButton.setGraphic(PamGlyphDude.createPamIcon("mdi2w-window-close", Color.GRAY, PamGuiManagerFX.iconSize));

		deleteButton.setOnAction((action)->{
			removeStructure(); 
		});
		deleteButton.layoutXProperty().bind(this.getConnectionNodeBody().widthProperty().subtract(deleteButton.widthProperty()).subtract(10));
		deleteButton.layoutYProperty().setValue(10);
		deleteButton.setRippleColor(Color.RED);
		deleteButton.getStyleClass().add("delete-button");
	
		this.getConnectionNodeBody().getChildren().add(deleteButton); 
		
		resizeFinished();

	}
	
	/**
	 * Remove the structure 
	 */
	private void removeStructure () {
		//remove all children as well. 
		for (int i=0; i<this.getChildConnectionNodes().size(); i++) {
			if (this.getChildConnectionNodes().get(i) instanceof ModuleConnectionNode) {
				connectionPane.removeModuleNode((ModuleConnectionNode) this.getChildConnectionNodes().get(i));
			}
			else {
				connectionPane.removeConnectionNode((StandardConnectionNode) this.getChildConnectionNodes().get(i));

			}
		}
		connectionPane.removeConnectionNode(this);
	}

	@Override
	public ConnectionNodeParams getConnectionNodeParams() {
		//params are generally only used for saving- ensure updated before doing so
		//updateParams();
		return groupStructureParams;
	}

	@Override
	public void resizeFinished() {
		if (super.isExpanded()) {
			textField.setVisible(true);
			deleteButton.setVisible(true);
		}
		else {
			textField.setVisible(false);
			deleteButton.setVisible(false);
		}
	}


	@Override
	public void updateParams() {
		groupStructureParams.layoutX = this.getConnectionNodeBody().getLayoutX();
		groupStructureParams.layoutY = this.getConnectionNodeBody().getLayoutY();
		//save a list of connection node ID's currently in the group structure. 
		ArrayList<ConnectionNode> subNodes = super.getConnectionSubNodes(true); 

		UUID[] subNodeIds = new UUID[subNodes.size()]; 
		for (int i=0; i<subNodes.size(); i++) {
			subNodeIds[i] = ((PAMConnectionNode) subNodes.get(i)).getConnectionNodeParams().getID(); 
		}

		//set the node IDs
		groupStructureParams.subNodeIDs = subNodeIds; 

		ArrayList<ConnectionNode> parentNodes = super.getParentConnectionNodes(); 

		//get the parent node IDs
		UUID[] parentNodeID = new UUID[parentNodes.size()]; 
		for (int i=0; i<parentNodes.size(); i++) {
			parentNodeID[i] = ((PAMConnectionNode) parentNodes.get(i)).getConnectionNodeParams().getID(); 
		}

		groupStructureParams.parentNodes = parentNodeID; 

		ArrayList<ConnectionNode> childNodes = super.getChildConnectionNodes(); 

		//get the parent node IDs
		UUID[] childNodeID = new UUID[childNodes.size()]; 
		for (int i=0; i<childNodes.size(); i++) {
			childNodeID[i] = ((PAMConnectionNode) childNodes.get(i)).getConnectionNodeParams().getID(); 
		}

		groupStructureParams.childNodes = childNodeID; 

	}

	@Override
	public void setConnectionNodeParams(ConnectionNodeParams usedStructInfo) {
		this.groupStructureParams = (PamGroupStructureParams) usedStructInfo; 
	}

	@Override
	public synchronized void loadsettings() {

		//add the connection nodes the correct group. 
		ArrayList<ConnectionNode>  connectionNodes = this.getConnectionPane().getAllConnectionNodes(true); 
		for (int i=0; i<connectionNodes.size(); i++) {
			//cast to PAMConnectionNode to grab settings. 
			PAMConnectionNode pamConnectionNode = ((PAMConnectionNode) connectionNodes.get(i)); 

			if (connectionNodes.get(i)!=this) { //import to prevent this updating it'sown parames before loading is fully complete. 
				for (int j=0; j<this.groupStructureParams.subNodeIDs.length; j++) {
					//System.out.println("Searching for sub node: " + j + " "+ this.groupStructureParams.subNodeIDs[j] + " Found node: " + pamConnectionNode.getConnectionNodeParams().getID()+ " " + this.groupStructureParams.subNodeIDs.length); 
					if (this.groupStructureParams.subNodeIDs[j].equals(pamConnectionNode.getConnectionNodeParams().getID())) {
						//this node is inside the connection node. 
						this.bindConnectionNode((StandardConnectionNode) connectionNodes.get(i), false); 
					}
				}

				//check if a parent node 
				for (int j=0; j<this.groupStructureParams.parentNodes.length; j++) {
					//System.out.println("Searching for parent node: " + j + " "+ this.groupStructureParams.parentNode[j] + " Found node: " + pamConnectionNode.getConnectionNodeParams().getID()+ " " + this.groupStructureParams.parentNode.length); 
					if (this.groupStructureParams.parentNodes[j].equals(pamConnectionNode.getConnectionNodeParams().getID())) {
						this.connectNode((StandardConnectionNode) connectionNodes.get(i)); 
					}
				}
			}
		}	

		//check child nodes but has to be after any nodes that should be, have been added to the sub pane. 
		for (int i=0; i<connectionNodes.size(); i++) {
			if ( connectionNodes.get(i)!=this) { //import to prevent this updating it'sown parames before loading is fully complete. 

				PAMConnectionNode pamConnectionNode = ((PAMConnectionNode) connectionNodes.get(i)); 
				//check if a parent node 
				for (int j=0; j<this.groupStructureParams.childNodes.length; j++) {
					//System.out.println("Searching for child node: " + j + " "+ this.groupStructureParams.childNodes[j] + " Found node: " + pamConnectionNode.getConnectionNodeParams().getID()+ " " + this.groupStructureParams.parentNode.length); 
					if (this.groupStructureParams.childNodes[j].equals(pamConnectionNode.getConnectionNodeParams().getID())) {
						//connect as a child. 
						((StandardConnectionNode) connectionNodes.get(i)).connectNode(this); 
					}
				}
			}
		}

	}

}
