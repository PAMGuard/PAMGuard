package dataModelFX;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import dataModelFX.DataModelModulePane.ModuleRectangle;
import dataModelFX.connectionNodes.ModuleConnectionNode;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;

/**
 * Sub class of ConnectionPane to deal with modules being dragged from 
 * module list to pane. 
 * 
 * @author Jamie Macaulay
 */
public class DataModelConnectPane extends ConnectionPane {

	/**
	 * The last location of a dropped node. 
	 */
	private Point2D lastDropPos=null;

	/**
	 * Preferred module width
	 */
	private double moduleWidth=100;

	/**
	 * Preferred module height
	 */
	private double moduleHeight=100;

	/**
	 * Reference to the data model
	 */
	private DataModelPaneFX dataModelPaneFX; 


	public DataModelConnectPane (DataModelPaneFX dataModelPaneFX){
		super();
		this.dataModelPaneFX=dataModelPaneFX; 

		//set what happens when mouse is dragged over pane
		this.setOnDragOver(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent event)
			{
				
//				System.out.println("START drag over node drag dropped" + dataModelPaneFX.getDraggingStructure());

				final Dragboard dragboard = event.getDragboard();
				if (dragboard.hasString()
						&& dataModelPaneFX.getModuleDragKey().equals(dragboard.getString())
						&& dataModelPaneFX.getDraggingModule().get() != null || dataModelPaneFX.getDraggingStructure()!=null)
				{
//					System.out.println("ACCEPT drag over node drag dropped" + dataModelPaneFX.getDraggingStructure());

					event.acceptTransferModes(TransferMode.ANY);
					//event.consume(); //causesd issues with dropping nodes not being detected
				}
			}
		});

		//set what happens when module is dropped. 
		this.setOnDragDropped(new EventHandler<DragEvent>(){
			@Override
			public void handle(DragEvent event)
			{
				
				System.out.println("Add Some Node drag dropped: " + dataModelPaneFX.getDraggingStructure());

				final Dragboard dragboard = event.getDragboard();
				if (dragboard.hasString()
						&& dataModelPaneFX.getModuleDragKey().equals(dragboard.getString())
						&& dataModelPaneFX.getDraggingModule().get() != null){
					
					lastDropPos = new Point2D(event.getX(), event.getY());


					ModuleRectangle moduleRect = dataModelPaneFX.getDraggingModule().get();

//					System.out.println("Add Connection Node drag dropped");
					dataModelPaneFX.getDraggingModule().set(null);
					dataModelPaneFX.getConnectionNodeFactory().addNewModule(moduleRect.getPamModuleInfo(), event.getX(), event.getY()); 

				}

				if (dragboard.hasString()
						&& dataModelPaneFX.getModuleDragKey().equals(dragboard.getString())
						&& dataModelPaneFX.getDraggingStructure().get() != null){
//					System.out.println("Add Structure Node drag dropped");

					dataModelPaneFX.getConnectionNodeFactory().addNewStructure(dataModelPaneFX.getDraggingStructure().get(), event.getX(), event.getY()); 
					dataModelPaneFX.getDraggingStructure().set(null);

				}
				event.setDropCompleted(true);
				event.consume();

			}
		});
	}


	/**
	 * Called through from the PamGUiMangerFX.
	 * @param type - notification flag. 
	 */
	public void notifyModelChanged(int type) {

		switch(type){
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			//check if the module is already added to pane.
			checkModulesAdded();
			break; 
			//			case PamControllerInterface.INITIALIZATION_COMPLETE:
			//			break; 
			//		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//
			//			break; 
		}
	}


	/**
	 * Add a new ModuleConnectionNode, representing a new PamControlledUnit, to the ConnectionPane.
	 * @param pamControlledUnit - new PamControlledUnit (PAMGUARD module)
	 * @param x - x position to place module	
	 * @param y - y position to place module
	 */
	private ModuleConnectionNode addNewModuleNode(PamControlledUnit pamControlledUnit, double x, double y){
		ModuleConnectionNode newNode = this.dataModelPaneFX.getConnectionNodeFactory().createConnectionnNode(pamControlledUnit);
		addNewConnectionNode(newNode, x, y);
		return newNode;
	}

	/**
	 * Remove module from the pane and PAM model. 
	 * @param moduleConnectionNode - the module connection node to remove
	 */
	public void removeModuleNode(ModuleConnectionNode moduleConnectionNode) {
		/**
		 * First need to disconnect modules. 
		 * Note, the 'check connection' functions assume any parent/child ControlledUnits are 
		 * present and hence here, must graphically disconnect and disconnect in pam model.
		 */
		ArrayList<ConnectionNode> parentNodes=getParentConnectionNodes(moduleConnectionNode);
		if (parentNodes!=null){ 
			for (int i=0; i<parentNodes.size(); i++){
				ConnectionPane.disconnectNodes(moduleConnectionNode, parentNodes.get(i));
				dataModelPaneFX.attemptModuleDisconnect(moduleConnectionNode, (ModuleConnectionNode) parentNodes.get(i));
			}
		}
		ArrayList<ConnectionNode> childNodes=getChildConnectionNodes(moduleConnectionNode);
		if (childNodes!=null){ 
			for (int i=0; i<childNodes.size(); i++){
				((StandardConnectionNode) childNodes.get(i)).disconnectNode(moduleConnectionNode);
				dataModelPaneFX.attemptModuleDisconnect((ModuleConnectionNode) childNodes.get(i), moduleConnectionNode);
			}
		}
		this.removeConnectionNode(moduleConnectionNode);

		PamController.getInstance().removeControlledUnt(moduleConnectionNode.getPamControlledUnit());

		dataModelPaneFX.getModuleSelectPane().updateModuleListPane(); 

	}



	/**
	 * Check that the correct modules have been added to the ConnectionPane. If not add modules. 
	 */
	private synchronized void checkModulesAdded() {
		PamController pamController = PamController.getInstance();
		ArrayList<ConnectionNode> connectionNodes = getAllConnectionNodes(); 

		//System.out.println("checkModulesAdded: Number of controlled units: " + pamController.getNumControlledUnits() ); 
		for (int i = 0; i <= pamController.getNumControlledUnits(); i++) {
			checkModuleAdded(pamController.getControlledUnit(i), connectionNodes); 
		}

		this.dataModeltoPamModel();
	}

	/**
	 * Check whether a module has been added or not. If not then add the correct ModuleConnectionNode. 
	 * @param pamControlledUnit - the pamController. 
	 * @param connectionNodes - all the module connection nodes (does not include structures)
	 * @return true if a new module was added. 
	 */
	public boolean checkModuleAdded(PamControlledUnit pamControlledUnit, ArrayList<ConnectionNode> connectionNodes) {

		//System.out.println("datamodelConnectPane: checkModuleAdded: " + pamControlledUnit + " No. nodes: " + connectionNodes.size()); 

		if (pamControlledUnit==null) return false; 

		//There are three possibilities here. 
		//1) The module connection node does not exist and must be added
		//2) The module connection node is already in the pane and nothing needs to happen
		//3) The module connection node has been added as a shell and needs a reference to the PamContrrolledUnit. 


		//possibilities 1 and 2. 
		ModuleConnectionNode moduleConnectionNode; 
		for (int j = 0; j < connectionNodes.size(); j++) {
			moduleConnectionNode = (ModuleConnectionNode) connectionNodes.get(j); 
			//System.out.println("Looking for node: " + moduleConnectionNode.getPamControlledUnit());
			
			if (moduleConnectionNode.getPamControlledUnit()!=null && moduleConnectionNode.getPamControlledUnit().equals(pamControlledUnit)) {
				//System.out.println("There is already a node for : " + moduleConnectionNode.getPamControlledUnit().getUnitName()); 
				return true; 
			}
			else if (moduleConnectionNode.getConnectionNodeParams()!=null && moduleConnectionNode.getConnectionNodeParams().unitName!=null 
					&& moduleConnectionNode.getConnectionNodeParams().unitName.equals(pamControlledUnit.getUnitName())){
				//set the pamcontrolled unit reference within the connection node. node. 
				moduleConnectionNode.setPamControlledUnit(pamControlledUnit);
				//System.out.println("There is already a node waiting for : " + moduleConnectionNode.getPamControlledUnit().getUnitName()); 

				if (PamController.getInstance().isInitializationComplete()) {
					//only to be used on drag and dropped nodes.
					ModuleConnectionNode potentialParent = dataModelPaneFX.attemptDefaultParentConnect(moduleConnectionNode);
				}
				return true; 
			}
		}
		
		//System.out.println("Could not find the node: " + pamControlledUnit.getUnitName()); 

		//possibility 3
		ModuleConnectionNode newNode;
		// System.out.println("Need to add new module " +
		// pamController.getControlledUnit(i).getUnitName());
		if (lastDropPos == null) {
			//called if a module is added from another location other than the data model e.g. a add modules menu
			Point2D xy = getAutoDropPosition(connectionNodes); //get an automatic posiiton to drop the node that does not overlap with other nodes
			newNode = this.dataModelPaneFX.getConnectionNodeFactory().addNewModule(pamControlledUnit, xy.getX(), xy.getY());
		} 

		else {
			// add a new module which has been dropped from the data model. 
			newNode = this.dataModelPaneFX.getConnectionNodeFactory().addNewModule(pamControlledUnit, lastDropPos.getX(), lastDropPos.getY());
			lastDropPos = null;
		}

		//attempt to connect the module to a default parent. 
		ModuleConnectionNode potentialParent = dataModelPaneFX.attemptDefaultParentConnect(newNode);

		return true; 
		// /**
		// * Need to check if already connected as might be if the raw data block was
		// explicitly set in the pam process.
		// */
		// if (potentialParent!=null) System.out.println(" New module potential
		// parent"+potentialParent.getPamControlledUnit().getUnitName());
		// else System.out.println(" New module potential parent null");
		// if (potentialParent!=null){
		// System.out.println(" Added "+newNode.getPamControlledUnit().getUnitName() + "
		// connecting: "+potentialParent.getPamControlledUnit().getUnitName());
		// newNode.connectNode(potentialParent);
		// }
	}


	/**
	 * Find the first position on a grid that does not currently contain a node. 
	 * @param connectionNodes - the current list of connection nodes. 
	 * @return the new position in pixels. 
	 */
	private Point2D getAutoDropPosition(ArrayList<ConnectionNode> connectionNodes) {
		Point2D newPoint = null; 
		int nDrops = 0; 
		double minDist = 150; 
		double dist; 
		boolean pointOK = true; 

		//iterate sequentially through positions...
		while (nDrops<1000) {
			newPoint =  new Point2D(250*(nDrops % 5), 250*(nDrops % 5));

			pointOK = true; 
			for (int i=0; i<connectionNodes.size(); i++) {
				dist= newPoint.distance(new Point2D(connectionNodes.get(i).getConnectionNodeBody().getLayoutX(),
						connectionNodes.get(i).getConnectionNodeBody().getLayoutY())); 
				if (dist<minDist) {
					//one of the nodes is too close
					pointOK=false; 
					break; 
				}
			}

			if (pointOK) {
				return newPoint; 
			}

			nDrops++; 
		}
		return newPoint;
	}


	/**
	 * Check module connections. Attempts to make the pamDataModel reflect the GUI data model. 
	 */
	public void pamModeltoDataModel() {
		dataModelPaneFX.pamModeltoDataModel(true); 
	}

	/**
	 * Check module connections. Attempts to make the GUI data model reflect the current pamDataModel. For example 
	 * use this function if source data is changed in external dialogs. 
	 */
	public void dataModeltoPamModel(){
		dataModelPaneFX.dataModeltoPamModel();
	}


	public double getModuleWidth() {
		return this.moduleWidth;
	}

	public double getModuleHeight() {
		return this.moduleHeight;
	}

}