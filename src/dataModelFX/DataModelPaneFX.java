package dataModelFX;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.glyphfont.Glyph;

import pamViewFX.PamGuiTabFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamSplitPane;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionPlug;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionGroupStructure;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionStructure;
import pamViewFX.fxStyles.PamStylesManagerFX;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamModel.PamModuleInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import dataModelFX.DataModelModulePane.ModuleRectangle;
import dataModelFX.DataModelModulePane.StructureRectangle;
import dataModelFX.connectionNodes.ConnectionNodeFactory;
import dataModelFX.connectionNodes.ModuleConnectionNode;

/**
 * The data model pane allows users to change PAMGuard's data model. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DataModelPaneFX extends PamBorderPane {

	/**
	 * Reference to the PamController.
	 */
	@SuppressWarnings("unused")
	private PamController pamController;

	/**
	 * Pane which holds a list of available modules.
	 */
	private DataModelModulePane moduleSelectPane;

	/**
	 * The pane in which modules are connected, disconnected.
	 */
	private DataModelConnectPane dataModelPane;

	/**
	 * Settings reference.
	 */
	private DataModelPaneFXSettings dataModelSettingsFX = new DataModelPaneFXSettings();

	/**
	 * Zoom in button.
	 */
	private PamButton zoomIn;

	/**
	 * Zoom out button.
	 */
	private PamButton zoomOut;

	/*
	 * When the data model is changing either by setting itself to the current
	 * PamController settings or setting the PamController settings all sorts of
	 * notifications start getting passed round, which, in some cases will call back
	 * into the data model. Thus functions such as datamodel2PamMode and
	 * pamModel2DataModel can by called multiple times within each other which is a
	 * recipe for disaster. When set to true disableNotificaiton disables calling
	 * these functions. The functions both set to true during execution and return
	 * to false upon completion.
	 */
	private boolean disableNotification = false;

	/**
	 * The data model settings manager.
	 */
	private DataModelSettingsManager dataModelSettingsManager;

	/**
	 * Produces the connection nodes. 
	 */
	private ConnectionNodeFactory connectionNodeFactory;


	/**
	 * Constructor for the data model.
	 */
	public DataModelPaneFX() {
		this.setCenter(createPane());

		//create the connection node factory which handles producing the correct ocnnection nodes
		connectionNodeFactory = new ConnectionNodeFactory(this);

		//create the settings manager which handles loading previous settings and saving settings. 
		dataModelSettingsManager = new DataModelSettingsManager(this);


		// addDragDropListeners();

		// //TEMP//////////////////////////
		// Button tempButton=new Button("Temp false");
		// Button tempButton1=new Button("Temp true");
		//
		// this.pamTab.getContentToolbar().getContentHBox().getChildren().addAll(tempButton,
		// tempButton1);
		// tempButton.setOnAction((action)->{
		//
		//// //print module connection info
		//// ModuleConnectionNode childNode;
		//// /**
		//// * First check all parent nodes of each node- are those connections correct
		//// */
		//// for (int i=0; i<this.dataModelPane.getConnectionNodes().size(); i++){
		//// childNode=dataModelPane.getConnectionNodes().get(i);
		//// if (childNode.getPamControlledUnit()==null) continue;
		//// //System.out.println("Children of " + ((ModuleConnectionNode)
		// dataModelPane.getConnectionNodes().get(i)).getPamControlledUnit().getUnitType());
		//// checkParentChildConnections(childNode, true);
		//// }
		//
		// for (int i=0; i<dataModelPane.getConnectionNodes().size(); i++){
		// System.out.println("Node
		// "+dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getUnitName()
		// + " has parents.... ");
		// for (int j=0;
		// j<dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getNumPamProcesses();
		// j++){
		//
		// if
		// (dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getPamProcess(j).getParentDataBlock()==null)
		// System.out.print(" "+null);
		// else System.out.print("
		// "+dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getPamProcess(j).getParentDataBlock().getDataName()+
		// " : ");
		//
		// if
		// (dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getPamProcess(j).isMultiplex()){
		// for (int k=0;
		// k<dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getPamProcess(j).getNumMuiltiplexDataBlocks();
		// k++){
		// System.out.print(" (MULTIPLEX)
		// "+dataModelPane.getConnectionNodes().get(i).getPamControlledUnit().getPamProcess(j).getMuiltiplexDataBlock(k).getDataName()+"
		// : ");
		// }
		// }
		//
		// }
		// System.out.println("----------------------------");
		// }
		//
		// });
		//
		// tempButton1.setOnAction((action)->{
		// dataModeltoPamModel();
		// });
		//
		// /////////////////////////////////
	}

	/**
	 * Create the main components of the pane.
	 * 
	 * @return the data model pane.
	 */
	private PamSplitPane createPane() {

		dataModelPane = new DataModelConnectPane(this);

		moduleSelectPane = new DataModelModulePane(this);

		PamSplitPane splitPane = new PamSplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.25);

		moduleSelectPane.setMinWidth(Region.USE_PREF_SIZE);
		moduleSelectPane.setPrefWidth(370);

		// splitPane.getItems().addAll(pamScrollPane,dataModelPane);
		splitPane.getItems().addAll(moduleSelectPane, dataModelPane);
		SplitPane.setResizableWithParent(moduleSelectPane, false);

		return splitPane;

	}

	/**
	 * Add data model specific controls to a PamGuiTab.
	 * 
	 * @param pamTab - the PamGuiTab to add controls to.
	 */
	public void createToolbarControls(PamGuiTabFX pamTab) {

		// create button to zoom
		zoomIn = new PamButton();
		zoomIn.setGraphic(Glyph.create("FontAwesome|PLUS").size(18).color(Color.GRAY));
		zoomIn.setOnAction((action) -> {
			dataModelPane.zoomIn(0.1);
		});
		zoomIn.getStyleClass().add("transparent-button");
		zoomIn.setStyle(" -fx-background-radius: 50;");

		zoomOut = new PamButton();
		zoomOut.setGraphic(Glyph.create("FontAwesome|MINUS").size(18).color(Color.GRAY));
		zoomOut.setOnAction((action) -> {
			dataModelPane.zoomOut(0.1);
		});
		zoomOut.getStyleClass().add("transparent-button");
		zoomOut.setStyle(" -fx-background-radius: 50;");

		// want to alter the toolbar at the a bit
		// remove all children
		pamTab.getContentToolbar().getRightHBox().getChildren()
		.removeAll(pamTab.getContentToolbar().getRightHBox().getChildren());
		pamTab.getContentToolbar().getRightHBox().getChildren().addAll(zoomIn, zoomOut);
	}

	// /**
	// * Add listeners to allow importing via drag and drop of .wav files/other
	// files
	// */
	// public void addDragDropListeners(){
	//
	// dataModelPane.setOnDragOver(new EventHandler<DragEvent>() {
	// @Override
	// public void handle(DragEvent event) {
	// Dragboard db = event.getDragboard();
	// if (db.hasFiles()) {
	// event.acceptTransferModes(TransferMode.COPY);
	// } else {
	// event.consume();
	// }
	// }
	// });
	//
	// // Dropping over surface
	// dataModelPane.setOnDragDropped(new EventHandler<DragEvent>() {
	// @Override
	// public void handle(DragEvent event) {
	// Dragboard db = event.getDragboard();
	// boolean success = false;
	// if (db.hasFiles()) {
	// success = true;
	// filemport(db.getFiles());
	// }
	// event.setDropCompleted(success);
	// event.consume();
	// }
	// });
	//
	// }

	/**
	 * Called whenever files are imported into the data model. e.g. by drag and
	 * drop.
	 */
	public void filemport(List<File> files) {
		// TODO - some sort of import manager? viewer v real time mode
		String filePath = null;
		for (File file : files) {
			filePath = file.getAbsolutePath();
			//			System.out.println(filePath);
		}
	}

	/**
	 * Gets passed model changed flags from the GUI Manager.
	 * 
	 * @param type - flag to describe notification
	 */
	boolean allowChange = true;

	public void notifyModelChanged(int type) {
		switch (type) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			// populate list with available modules.
			this.moduleSelectPane.populateModuleListPane();
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			//			System.out.println("DataModelPaneFX: New module has been added: ");
			// check if the module is already added to pane.
			dataModelPane.notifyModelChanged(type);
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//			System.out.println("DataModelPaneFX: Process settings changes: ");
			// //NOTE. Had to be careful of stack overflow errors here. Would like to change
			// data model GUI here but touching
			// //parent data blocks results in infinite loop. Yuck.
			if (allowChange) {
				allowChange = false;
				dataModeltoPamModel();
				allowChange = true;
			}
			break;
		}
	}

	/**
	 * Check module connections. This functions attempts to make the data model GUI
	 * reflect the current pamDataModel. For example use this function if source
	 * data is changed in external dialogs.
	 */
	protected void dataModeltoPamModel() {
		// System.out.println("************DATAMODELTOPAMMODEL*************");

		if (disableNotification)
			return;
		disableNotification = true;

		ModuleConnectionNode childNode;
		enableConectionListerner(false);
		/**
		 * First check all parent nodes of each node- are those connections correct
		 */
		for (int i = 0; i < this.dataModelPane.getConnectionNodes().size(); i++) {
			childNode = (ModuleConnectionNode) dataModelPane.getConnectionNodes().get(i);
			if (childNode.getPamControlledUnit() == null)
				continue;
			// System.out.println("Children of " + ((ModuleConnectionNode)
			// dataModelPane.getConnectionNodes().get(i)).getPamControlledUnit().getUnitType());
			checkParentChildConnections(childNode, true);
		}
		enableConectionListerner(true);

		disableNotification = false;
		// System.out.println("************DATAMODELTOPAMMODEL_END*************");

	}

	private void enableConectionListerner(boolean enable) {
		for (int i = 0; i < this.dataModelPane.getConnectionNodes().size(); i++) {
			((ModuleConnectionNode) dataModelPane.getConnectionNodes().get(i)).setEnableConnectListeners(enable);
		}
	}

	/**
	 * Check all module connections. This essentially attempts to make the
	 * pamDataModel the same as the GUI connections in the data model pane.
	 * 
	 * @param disconnect - if true then all module which are not connected have any
	 *                   parent datablock etc. removed. If false then any modules
	 *                   which have parent data blocks and no connection are
	 *                   connected. The later instance is used, for example, when
	 *                   modules with dependencies are added.
	 */
	protected void pamModeltoDataModel(boolean disconnect) {
		// System.out.println("************PAMMODELTODATAMODEL*************");
		if (disableNotification)
			return;
		disableNotification = true;

		ModuleConnectionNode childNode;
		/**
		 * First check all parent nodes of each node- are those connections correct
		 */
		for (int i = 0; i < this.dataModelPane.getConnectionNodes().size(); i++) {
			childNode = (ModuleConnectionNode) dataModelPane.getConnectionNodes().get(i);
			if (childNode.getPamControlledUnit() == null)
				continue;
			// System.out.println("Children of " + ((ModuleConnectionNode)
			// dataModelPane.getConnectionNodes().get(i)).getPamControlledUnit().getUnitType());
			checkModuleConnection(childNode);
			checkParentChildConnections(childNode, !disconnect);
		}

		disableNotification = false;
		// System.out.println("************PAMMODELTODATAMODEL_END*************");
		// PamController.getInstance().notifyModelChanged(PamController.CHANGED_PROCESS_SETTINGS);
	}

	/**
	 * Check a module connections. Check the connections between a child and it's
	 * parent nodes. Attempt to connect any child to parent if physical connection
	 * exists. If there is an error in connection highlight the connection line.
	 * 
	 * @param childNode  - the child node.
	 * @param parentNodes - array of parent the parent nodes
	 * @param disconnect - if true then all modules which are not connected have any
	 *                   parent data block etc. removed. If false then any modules
	 *                   which have parent data blocks and no connection between
	 *                   nodes are connected. The later instance is used, for
	 *                   example, when modules with dependenices are added.
	 * @return true if all module connection OK. false if there is a connection
	 *         error.
	 */
	private boolean checkModuleConnection(ModuleConnectionNode childNode) {

		// find all parent nodes.
		ArrayList<ConnectionNode> parentNodesS = dataModelPane.getParentConnectionNodes(childNode);

		boolean allOK = false;

		/**
		 * Check that the connection between modules which have been made in the
		 * ConnectionPane are OK. If not colour problem connection red.
		 */
		if (parentNodesS == null || parentNodesS.size() <= 0) {
			// there are no parents. Therefore the parent data block should be null. Unless
			// there is not supposed to be
			// a parent data block for the node (e.g. acquisition module)
		} 
		else {

			// System.out.println(" CHILD NODE: "
			// +childNode.getPamControlledUnit().getUnitName());
			// for (int i=0; i<parentNodes.size() ;i++){
			// System.out.println(" PARENTS NODE: "
			// +parentNodes.get(i).getPamControlledUnit().getUnitName());
			// }
			// System.out.println(" ------------------------------------------");

			ModuleConnectionNode parentModuleNode;


			ArrayList<StandardConnectionNode> parentNodes = getNonStructParentNodes(parentNodesS);


			// First deal with the direct connection: Should these two nodes have been
			// connected? i.e. are the at all compatible
			for (int j = 0; j < parentNodes.size(); j++) {


				parentModuleNode = (ModuleConnectionNode) parentNodes.get(j);
				allOK = attemptModuleConnect(childNode, parentModuleNode);
				if (!allOK) {
					StandardConnectionPlug connectionPlug = (StandardConnectionPlug) ConnectionPane
							.getConnectionPlug(childNode, parentModuleNode, true, true);
					if (connectionPlug != null) {
						connectionPlug.setError(true);
						if (connectionPlug.getConnectedShape() != null)
							connectionPlug.getConnectedShape().setError(true);
					}
				}
			}
		}

		return allOK;
	}

	/**
	 * Get all the parent nodes of a ConnectionNode ignoring structure connections. i.e. if a structure is a parent then the node
	 * goes to the parent of the structure, iterating up through parents until it reaches a parent that is not a structure or null parent. 
	 * @param parentNodesl
	 */
	private ArrayList<StandardConnectionNode> getNonStructParentNodes(ArrayList<? extends ConnectionNode> parentNodesl) {
		ArrayList<StandardConnectionNode> parentNodes= new ArrayList<StandardConnectionNode>();

		for (int j = 0; j < parentNodesl.size(); j++) {

			ConnectionNode parentNode = parentNodesl.get(j);

			if (parentNode instanceof ConnectionStructure) {
				//get the parent nodes ignoring structures. 
				parentNodes.addAll(getNonStructParentNodes(parentNode.getParentConnectionNodes()));

			}
			else if (parentNode!=null) {
				parentNodes.add((StandardConnectionNode) parentNode);
			}
		}

		return parentNodes;

	}

	/**
	 * Iterate through all nodes and check whether the the physical connections
	 * reflect the parent child connections of the controlled unit.
	 * 
	 * @param childNode         - the child node to check.
	 * @param useNodeConnection - if a GUI connection exists between nodes which
	 *                          does not reflect the child/parent process connection
	 *                          then set to true to set child/parent to node
	 *                          connections. Otherwise to set node connections to
	 *                          child/parent process connections set to false.
	 */
	private synchronized void checkParentChildConnections(ModuleConnectionNode childNode, boolean useNodeConnection) {

		/**
		 * Check through the PAMGuard data model and check for corresponding connection
		 * in the ConnectionPane. If a connection exists in the PAMGuard data model but
		 * not in the connection pane either connect in connection pane or disconnect in
		 * data model depending on @param useNodeConnection.
		 */
		PamProcess pamProcess;
		ArrayList<PamDataBlock> parentDataBlocks;
		ModuleConnectionNode potentialParent;
		boolean parentsNull = true;
		for (int i = 0; i < childNode.getPamControlledUnit().getNumPamProcesses(); i++) {
			// just in case we have a multiplex process with multiple parents (usually this
			// is not the case)
			parentDataBlocks = childNode.getPamControlledUnit().getPamProcess(i).getParentDataBlocks();
			// for (int j=0; j<parentDataBlocks.size(); j++){
			// if (parentDataBlocks.get(j)!=null) System.out.println(" ParentDataBlock: "+
			// parentDataBlocks.get(j).getDataName() );
			// else System.out.println(" ParentDataBlock: NULL");
			// }
			for (int j = 0; j < parentDataBlocks.size(); j++) {
				if (parentDataBlocks.get(j) != null) {
					pamProcess = parentDataBlocks.get(j).getParentProcess();
					parentsNull = false;
					// System.out.println("DataModelPaneFX: Child node:
					// "+childNode.getPamControlledUnit().getUnitName() + " ParentDataBlock:
					// "+parentDataBlocks.get(j).getDataName() + " size "+parentDataBlocks.size());
				} else {
					// System.out.println(" Child node:
					// "+childNode.getPamControlledUnit().getUnitName() + " ParentDataBlock:
					// "+"NULL" + " size "+parentDataBlocks.size());
					// TODO- what if null parent but node is connected?
					continue;
				}
				if (pamProcess != null) {
					// so has a parent process- that means the module should be connected.
					potentialParent = findConnectionModule(pamProcess.getPamControlledUnit());
					
					if (potentialParent == childNode || potentialParent == null) {
						continue;
					}
					
//					System.out.println("Checking: child: " + childNode + " parent: " +  potentialParent); 
					if (ConnectionPane.isNodeConnected(childNode, potentialParent, true, true)) {
						// everything is fine. Connected;
						// System.out.println("The module "+
						// childNode.getPamControlledUnit().getUnitName()+" is connected to
						// "+potentialParent.getPamControlledUnit().getUnitName());
					} 
					else {
						// not OK. the module and parent should be connected. Can either now remove
						// parent data block
						// or connect modules.
//						 System.out.println("DataModelPaneFX: The process " +
//						 childNode.getPamControlledUnit().getPamProcess(i).getProcessName() +
//						 " should be connected to " +
//						 potentialParent.getPamControlledUnit().getUnitName());
						if (useNodeConnection) {
//							 System.out.println("DataModelPaneFX: CONNECTING TWO NODES: childNode: "
//							 +childNode.getPamControlledUnit().getUnitName() +" to parentNode "
//							 +potentialParent.getPamControlledUnit().getUnitName()+ " "
//							 +potentialParent.isEnableConnectListeners());
							childNode.connectNode(potentialParent);
						} else {
							// System.out.println("DataModelPaneFX: DISCONNECTING TWO NODES: childNode: "
							// +childNode.getPamControlledUnit().getUnitName() +" from parentNode "
							// +potentialParent.getPamControlledUnit().getUnitName());
							attemptModuleDisconnect(childNode, potentialParent);
						}
					}
				}
			}
		}

		/**
		 * Now check through data model connections and make sure that any connection
		 * there have a corresponding parent->process connection in the PAMGuard data
		 * model. If not then either disconnect in ConnectionPane or connect in the
		 * PAMGuard data model depending on @param useNodeConnection
		 */
		ArrayList<StandardConnectionNode> parentNodes = this.getNonStructParentNodes(dataModelPane.getParentConnectionNodes(childNode));
		
		// System.out.println("CHECKING PARENT : THIS MODULE HAS NO DATABLOCK pam model
		// PARENT!!!! "+ childNode.getPamControlledUnit().getUnitName()+ " parent nodes:
		// "+parentNodes.size());
		if (parentNodes != null) {
			// must now check all parents
			for (int i = 0; i < parentNodes.size(); i++) {
				// connect up the two modules
				boolean connected = modulesConnected(childNode, (ModuleConnectionNode) parentNodes.get(i));
				if (!connected) {
					if (!useNodeConnection) {
						// if (attemptModuleConnect(childNode, parentNodes.get(i))){
						// System.out.println("Connect the
						// cuchildNode.getPamControlledUnit().getUnitName()+rrently connected nodes");
						// childNode.connectNode(parentNodes.get(i));
						// }
					} else {
						// System.out.println("DataModelPaneFX: DISCONNECTING TWO NODES PARENT NULL:
						// childNode: " +childNode.getPamControlledUnit().getUnitName() +" from
						// parentNode " +parentNodes.get(i).getPamControlledUnit().getUnitName());
						childNode.disconnectNode(parentNodes.get(i));
					}
				}
			}
		}

	}

	/**
	 * Find the ModuleConnection node in the ConnectionPane which holds a certain
	 * PamControlledUnit.
	 * 
	 * @param pamControlledUnit - the PamControlledUnit to find the
	 *                          ModuleConnectionNode for
	 * @return the ModuleConnectionNode associated with the pamControlledUnit.
	 *         Return null if no unit is found.
	 */
	public ModuleConnectionNode findConnectionModule(PamControlledUnit pamControlledUnit) {
		return findConnectionModule(pamControlledUnit, dataModelPane.getConnectionNodes(true));
	}

	/**
	 * Find the ModuleConnection node in the ConnectionPane which holds a certain
	 * PamControlledUnit.
	 * 
	 * @param pamControlledUnit - the PamControlledUnit to find the
	 *                          ModuleConnectionNode
	 * @param connectionNodes   - the list of nodes to search.
	 * @return the ModuleConnectionNode associated with the pamControlledUnit.
	 *         Return null if no unit is found.
	 */
	public ModuleConnectionNode findConnectionModule(PamControlledUnit pamControlledUnit,
			ArrayList<ConnectionNode> connectionNodes) {
		ModuleConnectionNode newNode;
		for (ConnectionNode connectionnode : connectionNodes) {
			if (connectionnode instanceof ModuleConnectionNode) {
				newNode = (ModuleConnectionNode) connectionnode;
				if (newNode.getPamControlledUnit() == pamControlledUnit)
					return newNode;

				// what if the new node is inside a sub connection pane?
				if (connectionnode instanceof ConnectionGroupStructure) {
					newNode = findConnectionModule(pamControlledUnit,
							((ConnectionGroupStructure) connectionnode).getConnectionPane().getConnectionNodes());
					if (newNode != null)
						return newNode;
				}

			}
		}
		return null;
	}

	/**
	 * Check whether two modules are already connected in the PAMGuard data model.
	 * 
	 * @param childNode  - the module with connecting socket
	 * @param parentNode - the module with connecting plug
	 * @return true if a connection was made.
	 */
	private boolean modulesConnected(ModuleConnectionNode childNode, ModuleConnectionNode parentNode) {

		PamControlledUnit childUnit = childNode.getPamControlledUnit();
		PamControlledUnit parentUnit = parentNode.getPamControlledUnit();

		for (int i = 0; i < childUnit.getNumPamProcesses(); i++) {
			for (int j = 0; j < parentUnit.getNumPamProcesses(); j++) {
				if (childUnit.getPamProcess(i).getParentProcess() != null
						&& childUnit.getPamProcess(i).getParentProcess() == parentUnit.getPamProcess(j)) {
					// System.out.println(" Child parent Process:
					// "+childUnit.getPamProcess(i).getParentProcess().getProcessName()
					// + " Parent process "+parentUnit.getPamProcess(j).getProcessName());
					return true;
				}
				// check multiplex data blocks -this is rarely used
				if (childUnit.getPamProcess(i).isMultiplex()) {
					// need to check through multiplex data blocks
					for (int k = 0; k < childUnit.getPamProcess(i).getNumMuiltiplexDataBlocks(); k++) {
						if (parentUnit.getPamProcess(j).getOutputDataBlocks()
								.contains(childUnit.getPamProcess(i).getMuiltiplexDataBlock(k)))
							return true;
					}
				}
			}
		}

		return false;

	}

	/**
	 * Checks whether a module can be a parent of another module (child). If it can
	 * then the parent data block which can be a parent of the child's first
	 * available process is added to that process. Note: for flexibility this does
	 * not connect socket/plug in the display. Use childNode.connectNode(parentNode)
	 * to do that.
	 * 
	 * @param childNode  - the module with connecting socket
	 * @param parentNode - the module with connecting plug
	 * @return true if a connection was made.
	 */
	private boolean attemptModuleConnect(ModuleConnectionNode childNode, ModuleConnectionNode parentNode) {

		PamControlledUnit childUnit = childNode.getPamControlledUnit();
		PamControlledUnit parentUnit = parentNode.getPamControlledUnit();

		boolean connected = false;

		// System.out.println("Attempting to connect: " + parentUnit.getUnitName() + "
		// to " + childUnit.getUnitName());

		// First, are these modules already connected.
		if (modulesConnected(childNode, parentNode)) {
			// System.out.println("Modules already connected");
			return true;
		}

		/**
		 * This is a confusing mess of nested loops however most will only iterate once.
		 * Have several things to think about here. 1) There maybe multiple PAM process
		 * of which only one might have the correct parentDataBlock. 2) The parent data
		 * block might have multiple output data blocks and again only one of these
		 * might be compatible with any one of the pam process in the child node.
		 * Therefore lots of searching to find correct data block - process match.
		 */
		for (int i = 0; i < childUnit.getNumPamProcesses(); i++) {
			for (int j = 0; j < parentUnit.getNumPamProcesses(); j++) {
				for (int k = 0; k < parentUnit.getPamProcess(j).getOutputDataBlocks().size(); k++) {
					// a module may have no compatible parent.
					if (childUnit.getPamProcess(i).getCompatibleDataUnits() == null)
						continue;
					// now iterate through compatible parent types.
					for (int l = 0; l < childUnit.getPamProcess(i).getCompatibleDataUnits().size(); l++) {
						if (parentUnit.getPamProcess(j).getOutputDataBlocks().get(k).getUnitClass()
								.equals(childUnit.getPamProcess(i).getCompatibleDataUnits().get(l))) {

							// System.out.println("Parent unit
							// "+parentUnit.getPamProcess(j).getOutputDataBlocks().get(k).getUnitClass().getName()
							// +" can be parent of child "+
							// childUnit.getPamProcess(i).getCompatibleDataUnits().get(l));

							// Found a compatible data block in the parent process. Only connect if not
							// already connected.
							// if (childUnit.getPamProcess(i).getParentDataBlock()!=null)
							// System.out.println("DataModelFX: AttemptModuleConnect: CHILD PARENT PROCESS "
							// +childUnit.getPamProcess(i).getProcessName() +" " +
							// " PARENT " + parentUnit.getUnitName() + " PARENT DATABLOCK
							// "+parentUnit.getPamProcess(j).getOutputDataBlocks().get(k).getLongDataName());
							/**
							 * Careful here- seems like setParentDataBlock function can cause some kind of
							 * processor leak if used improperly.
							 */
							if (childUnit.getPamProcess(i).isMultiplex()
									&& childUnit.getPamProcess(i).isExternalProcess()) {
								// add a data block. Use parent available- else add to multiplex.
								if (childUnit.getPamProcess(i).getParentDataBlock() == null)
									childUnit.getPamProcess(i).setParentDataBlock(
											parentUnit.getPamProcess(j).getOutputDataBlocks().get(k));
								else
									childUnit.getPamProcess(i).addMultiPlexDataBlock(
											parentUnit.getPamProcess(j).getOutputDataBlocks().get(k));
								connected = true;
							} else if (childUnit.getPamProcess(i).isExternalProcess()) {
								childUnit.getPamProcess(i)
								.setParentDataBlock(parentUnit.getPamProcess(j).getOutputDataBlocks().get(k));
								connected = true;
							}

							/**
							 * So lets say we have a parent module which has multiple output data blocks
							 * which could be the parent of this child. Which data block to use? Try and use
							 * the childs' preferred data block.
							 */
							if (childNode.getPrefferedParent(parentUnit) == parentUnit.getPamProcess(j)
									.getOutputDataBlocks().get(k))
								return true;
						}
					}
				}
			}
		}
		return connected;
	}

	/**
	 * Disconnect two modules. To disconnect modules no process in the child node
	 * must subscribe to any output data blocks from the parent node. Note: this
	 * does not disconnect socket/plug. Use childNode.connectNode(parentNode) to do
	 * that.
	 * 
	 * @param childNode  - the module with connecting socket
	 * @param parentNode - the module with connecting plug
	 * @return true if at least one process in the childNode had it's parent set to
	 *         null.
	 */
	public boolean attemptModuleDisconnect(ModuleConnectionNode childNode, ModuleConnectionNode parentNode) {
		// System.out.println("DataModelPaneFX: DISCONNECT
		// "+childNode.getPamControlledUnit().getUnitName()+ " FROM " +
		// parentNode.getPamControlledUnit().getUnitName());

		boolean disconnect = false;

		PamControlledUnit childUnit = childNode.getPamControlledUnit();
		PamControlledUnit parentUnit = parentNode.getPamControlledUnit();

		for (int i = 0; i < parentUnit.getNumPamProcesses(); i++) {
			for (int j = 0; j < parentUnit.getPamProcess(i).getNumOutputDataBlocks(); j++) {
				for (int k = 0; k < childNode.getPamControlledUnit().getNumPamProcesses(); k++) {
					if (childUnit.getPamProcess(k).getParentDataBlock() == parentUnit.getPamProcess(i)
							.getOutputDataBlock(j)) {
						childUnit.getPamProcess(k).setParentDataBlock(null);
						disconnect = true;
					}
					// disconnect any multiplex data block.
					if (childUnit.getPamProcess(k).isMultiplex()) {
						System.out.println("Removing parent datablock from multiplex list");
						childUnit.getPamProcess(k)
						.removeMultiPlexDataBlock(parentUnit.getPamProcess(i).getOutputDataBlock(j));
					}
				}
			}
		}

		return disconnect;
	}

	/**
	 * Check the current modules to see if there is a suitable parent for a module
	 * to connect to. If there is connect to that parent. Note: this does not
	 * connect socket/plug. Use childNode.connectNode(parentNode) to do that.
	 * 
	 * @return - the parent node to connect to. null if no suitable node.
	 */
	protected ModuleConnectionNode attemptDefaultParentConnect(ModuleConnectionNode childNode) {

		if (!childNode.isAutoConnect())
			return null;
		// System.out.println(" Search for parent for
		// "+childNode.getPamControlledUnit().getUnitName());
		ModuleConnectionNode potentialParent;

		for (int iNode = 0; iNode < this.dataModelPane.getConnectionNodes().size(); iNode++) {
			potentialParent = (ModuleConnectionNode) dataModelPane.getConnectionNodes().get(iNode);
			if (attemptModuleConnect(childNode, potentialParent)) {
				return potentialParent;
			}
		}

		return null;
	}

	// The DataModel pane needs to keep a record of where the module connection
	// nodes are.



	/**
	 * Check whether a PAM Module has an FX compatible GUI.
	 * 
	 * @param pamModuleInfo - the PAMModuleInfo to check for an FX GUI
	 * @return true if there is an FX GUI flag.
	 */
	public static boolean isFXModule(PamModuleInfo pamModuleInfo) {
		for (int i = 0; i < pamModuleInfo.getNGUIFlags(); i++) {
			if (pamModuleInfo.getGUICompatabilityFlag(i) == PamGUIManager.FX) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the module pane. This contains a list of possible modules to add to the
	 * data model.
	 * 
	 * @return the data module pane.
	 */
	public DataModelModulePane getModuleSelectPane() {
		return this.moduleSelectPane;
	}

	/**
	 * Get the module drag key. Used for drag and drop listeners between module and
	 * connection panes.
	 * 
	 * @return the module drag key string.
	 */
	public String getModuleDragKey() {
		return DataModelModulePane.MODULE_DRAG_KEY;
	}

	/**
	 * Get the dragging module properties. Used for drag and drop listeners between
	 * module and connection panes.
	 * 
	 * @return the dragging module properties
	 */
	public ObjectProperty<ModuleRectangle> getDraggingModule() {
		return this.moduleSelectPane.getDraggingModule();
	}

	/**
	 * Get settings for the data model.
	 * 
	 * @return settings for the data model.
	 */
	public DataModelPaneFXSettings getDataModelSettings() {
		return this.dataModelSettingsFX;
	}

	/**
	 * Get the dragging structure. Used for drag and drop listeners between module
	 * and connection panes.
	 * 
	 * @return the dragging structure.
	 */
	public ObjectProperty<StructureRectangle> getDraggingStructure() {
		return moduleSelectPane.getDraggingStructure();
	}

	/**
	 * Get the connection pane in which the nodes are connected. 
	 * @return the connection pane. 
	 */
	public DataModelConnectPane getConnectionPane() {
		return this.dataModelPane;
	}

	/**
	 * Get the connection node factory. This handles creating new connection nodes
	 * and structures. 
	 * @return the connection node factory. 
	 */
	public ConnectionNodeFactory getConnectionNodeFactory() {
		return this.connectionNodeFactory;
	}

}
