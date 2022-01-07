package pamViewFX.fxNodes.connectionPane.structures;

import java.util.ArrayList;

import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionPlug;
import pamViewFX.fxNodes.connectionPane.StandardConnectionSocket;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionNodeUtils;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.ConnectorNode;
import pamViewFX.fxNodes.connectionPane.ConnectorNodeListener;

/**
 * A node which holds other connection nodes, minimising the space they use in
 * the data model. This has one connection point which is passed to all
 * connection nodes in the group. Hence all connection nodes in the group have
 * the same parent.
 * 
 * @author Jamie Macaulay
 *
 */
public class ConnectionGroupStructure extends StandardConnectionNode implements ConnectionStructure, ConnectionSubGroup {

	/**
	 * A plug drag listener to make sure plugs cannot be dragged outside the bounds of the rectangle. . 
	 */
	private PlugDragListener plugDragListener;


	public ConnectionGroupStructure(ConnectionPane connectionPane) {
		super(connectionPane);

		this.addDefaultPlug();

		plugDragListener = new PlugDragListener(); 

		//		expandStructure(true); //for testing while we get this made up. 
	}


	/***Extra Connection Plug Stuff****/


	/**
	 * Plug with a few differences to standard connection plug.
	 * 
	 * This is a rather unique plug in that it is connected to a node which is part
	 * of one connection pane but connects with ConnectionNodes in a sub pane. Thus
	 * it requires quite a bit of tinkering to ensure it is searching for sockets in
	 * the correct connection pane etc.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	class GroupConnectionPlug extends StandardConnectionPlug {

		double deleteBarrier = 10; 

		public GroupConnectionPlug(StandardConnectionNode connectionNode, double plugBodyWidth, double plugEndWidth,
				double plugBodyHeight, double plugEndHeight, double cornerRadius, Orientation orientation) {
			super(connectionNode, plugBodyWidth, plugEndWidth, plugBodyHeight, plugEndHeight, cornerRadius, orientation);
		}

		@Override
		public boolean checkPlugDelete(){
			//the plug is inside the node and so deletes if the outside the node. 
			return !isChildInRectBoundsNode(this);
		}

		/**Because this plug is technically in the "parent" connection pane must change many of the functions so that it
		 * interacts with sockets etc in the sub connection pane within the group structure.
		 */

		@Override
		public Pane getConnectionNodeBody() {
			//plug has to be a little special because it is essentially inside the node and not part of a group. 
			return ConnectionGroupStructure.this.getConnectionNodeBody().getConnectionSubPane();
		}

		@Override
		public Point2D getLocalCords(double x, double y) {
			//plug has to be a little special because it is essentially inside the node and not part of a group. 
			return ConnectionGroupStructure.this.getConnectionNodeBody().getConnectionSubPane().sceneToLocal(x, y);
		}

		@Override
		public ArrayList<ConnectorNode> getPossibleConnectionShapes() {
			//the plug is part of a connection node which is in a ConnectionPane but it is really for connecting stuff in the sub connection pane. 
			//Thus this function must change in order for the plug to search for other plugs in the sub pane, not the primary pane. 
			ArrayList<ConnectorNode> possibleShapes= ConnectionGroupStructure.this.getConnectionNodeBody().getConnectionSubPane().getPlugAcceptingConnectionShapes(ConnectionGroupStructure.this);
			return possibleShapes;
		}

		@Override
		public void setConnectorNodesNull(){
			//set all the rest of the shapes to have no possible collisions
			//need this to search the sub connection pane, not the connection pane the group structure is in. 
			super.getConnectionNode().setCollisionShapesNull(ConnectionGroupStructure.this.getConnectionNodeBody().getConnectionSubPane());
		}

		@Override
		public ConnectorNode checkForCollision(ConnectorNode shape, ArrayList<? extends ConnectorNode> nodes, double min_dist){
			//need this because the plug is technically in master connection node but we do not want it to connect with anything in that pane
			ConnectorNode node = super.checkForCollision(shape, nodes, min_dist);
			if (node!=null && getConnectionSubPane().getConnectionNodes().contains(node.getConnectionNode())){
				return node;
			}
			else return null;
		}

	}


	/***Change Plug Behaviour ****/

	@Override
	public StandardConnectionPlug createPlug(){
		return new GroupConnectionPlug(this, StandardConnectionNode.plugBodyWidth, StandardConnectionNode.plugEndWidth, 
				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight,  StandardConnectionNode.cornerRadius, 
				Orientation.HORIZONTAL);
	}


	/**
	 * Create a default plug and adds to the ConnectionNode. This is initially bound to the rectangle but un-binds as 
	 * soon as a drag event is registered or enableFreePlug();
	 * @return a default plug.
	 */
	@Override
	public StandardConnectionPlug addDefaultPlug(){

		StandardConnectionPlug plug=createPlug();
		/**
		 * If a new plug but not the first set to disabled- important this is placed here 
		 * in function to prevent weird behaviour where plug is created enabled, binds to something and then
		 * CONNECTION_DISABLED flag is set. 
		 */
		//System.out.println("CONNECTION NODE: ADD DEFAULT PLUG. "+  connectionPlugs.size());
		if (super.getConnectionPlugs().size() > 0) {
			plug.setConnectionStatus(ConnectorNode.CONNECTION_DISABLED, null);
			plug.toBack();
		}

		//add children to group; 
		this.getConnectionNodeBody().getConnectionSubPane().getChildren().add(plug);
		this.getConnectionNodeBody().getConnectionSubPane().getChildren().addAll(plug.getPlugConnectionLines());

		//init layout of the plug
		initDefaultPlugLayout(plug);  

		//create a local reference; 
		getConnectionPlugs().add(plug);

		return plug; 
	}

	@Override
	protected void removeConnectionPlug(StandardConnectionPlug plug, boolean newPlug){

		this.getConnectionNodeBody().getConnectionSubPane().getChildren().remove(plug);
		this.getConnectionNodeBody().getConnectionSubPane().getChildren().removeAll(plug.getPlugConnectionLines());
		//remove from one of two arrays. 
		this.getConnectionPlugs().remove(plug);
		//if only one plug left in node or no plugs at all then add new plug. 

		//System.out.println("REMOVE CONNECTION PLUG: " + connectionPlugs.size() + " new plug? "+newPlug);
		if (this.getConnectionPlugs().size()<=0 && newPlug){
			addDefaultPlug();
			return;
		}

		//if there is another plug and it's disabled enable it. 
		if (this.getConnectionPlugs().size()==1) {
			this.getConnectionPlugs().get(0).setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
		}
	}


	@Override
	public void initDefaultPlugLayout(StandardConnectionPlug plug) {
		//change plug so it sits inside the connection structure. 
		//if horizontal the plug sits at the right of the rectangle. 
		plug.layoutXProperty().bind(getConnectionNodeBody().getConnectionSubPane().layoutXProperty().add(40));
		plug.layoutYProperty().bind(getConnectionNodeBody().getConnectionSubPane().layoutYProperty().add(plugYProperty()));

		plug.getPlugConnectionLines().get(0).startXProperty().bind(getConnectionNodeBody().getConnectionSubPane().layoutXProperty().add(0));
		plug.getPlugConnectionLines().get(0).startYProperty().bind(getConnectionNodeBody().getConnectionSubPane().layoutYProperty().add(plugYProperty()));

	}

	@Override
	public Tooltip getToolTip() {
		return new Tooltip("Allows connection nodes to be group together");
	}

	/***Connection Node Stuff***/

	@Override
	public ConnectionGroupBody createNodeBody(){
		ConnectionGroupBody connectionGroupBody = new ConnectionGroupBody(this); 
		//plug scales so must be bound to connection pane. 
		super.plugYProperty().bind(connectionGroupBody.getConnectionSubPane().prefHeightProperty().divide(2));

		super.socketYProperty().bind(connectionGroupBody.heightProperty().divide(2));
		return connectionGroupBody; 
	}

	@Override
	public ConnectionGroupBody getConnectionNodeBody(){
		return (ConnectionGroupBody) super.getConnectionNodeBody(); 

	}

	@Override
	public ConnectionStructureType getStructureType() {
		return ConnectionStructureType.GroupConnection;
	}

	@Override
	public void notifyChange(int flag, StandardConnectionNode connectionNode2) {
		if (connectionNode2==null || connectionNode2.equals(this)) return; 

		super.notifyChange(flag, connectionNode2); 
		switch (flag) {
		case StandardConnectionNode.DRAG_DROP:
			checkNodeDrop(connectionNode2);
			break; 
		case StandardConnectionNode.DRAGGING:
			//this gets called for connection nodes in both the 
			//master only
			checkNodeOverDrag(connectionNode2);
			break; 
		} 
	}


	/**
	 * Notify a change from 
	 * @param flag - the flag
	 * @param connectionNode2 - the connection node. 
	 */
	public void notifySubChange(int flag, StandardConnectionNode connectionNode2) {
		switch (flag) {
		case StandardConnectionNode.DRAG_DROP:
			checkNodeDrop(connectionNode2); 
			break; 
		case StandardConnectionNode.DRAGGING:
			//this gets called for connection nodes in both the 
			//master only

			//checkNodeOverDrag(connectionNode2);

			break; 
		} 
	}



	/*****Drag and drop properties of sub nodes*****/


	/**
	 * Check whether a connection node has been dropped inside the GroupConnectionNode;.
	 * @param connectionNode2 - check the drop. 
	 */
	private void checkNodeDrop(StandardConnectionNode connectionNode2) {
		//cannot connect this node or other structures. 
		if (connectionNode2 == this || connectionNode2 instanceof ConnectionGroupStructure) return;


		// now is that node within the layout bounds of the connection rectangle.
		if (!getConnectionSubPane().getConnectionNodes(true).contains(connectionNode2) && isInRectBounds(connectionNode2)) {
			//			System.out.println("Bind node: " ); 
			bindConnectionNode(connectionNode2);
		}

		// unbind the connection node. 
		else if (getConnectionSubPane().getConnectionNodes(true).contains(connectionNode2) && !isChildInRectBoundsNode(connectionNode2.getConnectionNodeBody())) {
			//			System.out.println("Unbind node: "); 
			unBindConnectionNode(connectionNode2);
		}

		highlightStructure(false);
	}


	/**
	 * Check whether a node is dragging over this node. 
	 * @param connectionNode2  the node to check. 
	 */
	private void checkNodeOverDrag(StandardConnectionNode connectionNode2) {
		if (!getConnectionSubPane().getConnectionNodes().contains(connectionNode2) && isInRectBounds(connectionNode2)) {
			//only highlight the structure if an external is dragged over. 
			highlightStructure(true); 
		}
		else {
			highlightStructure(false); 
		}

	}

	/**
	 * Highlight the rectangle. 
	 * @param b - true to highlight. 
	 */
	private void highlightStructure(boolean b) {
		this.getConnectionNodeBody().highlightStructure(b);

	}


	/**
	 * Check whether a node is within the bounds of the rectangle. The node in questions is a child
	 * of the same parent of the node. 
	 * @param connectionNode2 - the connection node to check. 
	 * @return true if in bounds. 
	 */
	private boolean isInRectBounds(StandardConnectionNode connectionNode2) {
		//check whether anode has been dropped inside the connection node. 
		//find centre of connection node. 
		double xLayout = connectionNode2.getConnectionNodeBody().getLayoutX()+
				connectionNode2.getConnectionNodeBody().getWidth()/2; 
		double yLayout = connectionNode2.getConnectionNodeBody().getLayoutY()+
				connectionNode2.getConnectionNodeBody().getHeight()/2; 
		//		System.out.println("ConnectionNode: Layoutx : " + xLayout + "Layouty: " + yLayout);
		//		System.out.println("Structure : " + getConnectionNodeShape().getLayoutX() + 
		//				" Layouty: " + getConnectionNodeShape().getLayoutY());

		// now is that node within the layout bounds of the connection rectangle.
		return isInRectBounds(xLayout, yLayout); 
	}

	/**
	 * Check whether a pixel point (in the scene) is inside the current location of the rectangle. 
	 * @param xLayout - x position 
	 * @param yLayout - y position
	 * @return true if inside the rectangle. 
	 */
	private boolean isInRectBounds(double xLayout, double yLayout) {
		if (xLayout > getConnectionNodeBody().getLayoutX()
				&& xLayout < getConnectionNodeBody().getLayoutX() + getConnectionNodeBody().getWidth()
				&& yLayout > getConnectionNodeBody().getLayoutY()
				&& yLayout < getConnectionNodeBody().getLayoutY() + getConnectionNodeBody().getHeight()){
			return true;
		}
		else {
			return false; 
		}
	}

	/**
	 * Check whether a child of the rectangle is within  
	 * @param connectionNode2 - the connection node to check. 
	 * @return true if in bounds. 
	 */
	private boolean isChildInRectBoundsNode(Node child) {
		if (child.getLayoutX() > 0
				&& child.getLayoutX() < getConnectionNodeBody().getWidth()
				&& child.getLayoutY() > 0
				&& child.getLayoutY()  < getConnectionNodeBody().getHeight()){
			return true;
		}
		else {
			return false; 
		}
	}




	/**
	 * Bind a connection node to the group structure i.e. it becomes a sub node of the group. 
	 * 
	 * @param connectionNode2- the connection node to bind
	 */
	public void bindConnectionNode(StandardConnectionNode connectionNode2) {
		bindConnectionNode(connectionNode2, true); 
	}
	
	
	/**
	 * Bind a connection node to the group structure i.e. it becomes a sub node of the group. 
	 * 
	 * @param connectionNode2- the connection node to bind
	 * @param convertSceneLocation - true to convert from the current node co-ordinates to co-ordinates inside the sub pane. 
	 */
	public void bindConnectionNode(StandardConnectionNode connectionNode2, boolean convertSceneLocation) {

		//		connectionNode2.setManaged(false);
		//		connectionNode2.getConnectionNodeBody().setManaged(false);

		disConnectNode( connectionNode2 );
		//now some of the plugs may have disconnected outside the node. 
		connectionNode2.removeAllConnectionPlugs();
		connectionNode2.addDefaultPlug();

		Point2D subPaneLocation; 
		if (convertSceneLocation) {
			//add connection node to the sub list. 
			//Note- remmeber to use the layout of the connection noded node, not the connection node group. 
			Point2D sceneLocation = connectionNode2.getParent().localToScene(connectionNode2.getConnectionNodeBody().getLayoutX(), connectionNode2.getConnectionNodeBody().getLayoutY()); 

			subPaneLocation = this.getConnectionNodeBody().getConnectionSubPane().sceneToLocal(sceneLocation); 

		}
		else {
			subPaneLocation = new Point2D(connectionNode2.getConnectionNodeBody().getLayoutX(), connectionNode2.getConnectionNodeBody().getLayoutY()); 
		}
		
		connectionNode2.getConnectionPane().removeConnectionNode(connectionNode2);

		this.getConnectionNodeBody().getConnectionSubPane().addNewConnectionNode(connectionNode2, subPaneLocation.getX(), subPaneLocation.getY());

		//allow the node to be dragged outside the rectangle. 
		connectionNode2.getConnectionNodeBody().setDragOutsidePane(true); 

		connectionNode2.setConnectionPane(this.getConnectionNodeBody().getConnectionSubPane());

		this.getConnectionSubPane().getConnectionNodes().add(connectionNode2); 

		//		//just in case
		//		connectionNode2.getConnectionNodeBody().layoutXProperty().unbind();
		//		connectionNode2.getConnectionNodeBody().layoutYProperty().unbind();

		//bring to front so if node is clicked rectangle does not drag. 
		connectionNode2.toFront();


		//must prevent node plugs from exiting the rectangle. 
		connectionNode2.addConnectorNodeListener(this.plugDragListener);

		//set collision shapes to null to make sure weird plug bits left around. 
		connectionNode2.setCollisionShapesNull();
	}

	/***
	 * Called whenever a node leaves the group. 
	 * 
	 * @param connectionNode2 - the connection node leaving the group. 
	 */
	public void unBindConnectionNode(StandardConnectionNode connectionNode2) {
		//add connection node to the sub list. 
		//Note- remmeber to use the layout of the connection node body, not the connection node group. 
		Point2D sceneLocation = connectionNode2.getParent().localToScene(connectionNode2.getConnectionNodeBody().getLayoutX(), 
				connectionNode2.getConnectionNodeBody().getLayoutY()); 

		connectionNode2.getConnectionPane().removeConnectionNode(connectionNode2);
		this.getConnectionSubPane().getConnectionNodes().remove(connectionNode2); 

		Point2D connectionPaneLocation = this.getConnectionPane().sceneToLocal(sceneLocation); 

		this.getConnectionPane().addNewConnectionNode(connectionNode2, connectionPaneLocation.getX(), connectionPaneLocation.getY());

		connectionNode2.toFront();
		connectionNode2.removeConnectorNodeListener(this.plugDragListener);


		//prevent the node from being dragged outside the primary rectangle. 
		connectionNode2.getConnectionNodeBody().setDragOutsidePane(true); 

		//disconnect the connection from everything!!!
		disConnectNode( connectionNode2 );


		//now some of the plugs may have disconnected outside the node. 
		connectionNode2.removeAllConnectionPlugs();
		connectionNode2.addDefaultPlug();

		//set collision shapes to null to make sure weird plug bits left around. 
		connectionNode2.setCollisionShapesNull();
	}


	/**
	 * Disconnect a node from everything and disconnect everything from the node. 
	 * @param connectionNode2 - the node to disconnect.
	 */
	private void disConnectNode(StandardConnectionNode connectionNode2 ) {
		//disconnect the connection from everything!!!
		for (StandardConnectionSocket sockets: connectionNode2.getConnectionSockets()) {
			if (sockets.getConnectedShape()!=null) {
				//				sockets.getConnectedShape().getShape().layoutXProperty().unbind();
				//				sockets.getConnectedShape().getShape().layoutYProperty().unbind();
				sockets.getConnectedShape().setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
				checkShapeInBounds(sockets.getConnectedShape());
			}
			sockets.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
		}


		//disconnect the connection from everything!!!
		for (StandardConnectionPlug plug: connectionNode2.getConnectionPlugs()) {
			if (plug.getConnectedShape()!=null) {
				//				sockets.getConnectedShape().getShape().layoutXProperty().unbind();
				//				sockets.getConnectedShape().getShape().layoutYProperty().unbind();
				plug.getConnectedShape().setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
			}
			plug.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
		}
	}


	@Override
	public void notifyConnectionListeners(ConnectorNode connectedShape, ConnectorNode plugShape, int connected) {
		// TODO Auto-generated method stub
	}

	public void resizeFinished() {
		// TODO Auto-generated method stub

	}


	private class PlugDragListener implements ConnectorNodeListener {

		@Override
		public void nodeConnectorEvent(ConnectorNode shape, int type) {
			if (type == ConnectorNode.CONNECTOR_MOVED) {
				//check if the plug is within bounds,. 
				checkShapeInBounds(shape);
			}
		}
	}

	/***
	 * Check whether a shape is within the rect bounds. If not return to the clsoest point inside the bounds. 
	 * @param shape
	 */
	private void checkShapeInBounds(ConnectorNode shape) {
		if (!isChildInRectBoundsNode(shape.getShape())){
			//find the closest bounds 
			if (!shape.getShape().layoutXProperty().isBound() && !shape.getShape().layoutYProperty().isBound()) {

				//find the closest point to the rectangle 
				Point2D point = ConnectionNodeUtils.closestPointOnRect(0, 0, 
						getConnectionNodeBody().getWidth(), getConnectionNodeBody().getHeight(), 
						shape.getShape().getLayoutX(),  shape.getShape().getLayoutY()) ;

				if (Math.abs(point.getX())<1 || Math.abs(point.getY())<1){
					//shape cannot move past that point. 
					shape.getShape().setLayoutX(point.getX()); 
					shape.getShape().setLayoutY(point.getY()); 
				}
				else {
					shape.getShape().setLayoutX(point.getX()+shape.getShape().getBoundsInLocal().getCenterX()); 
					shape.getShape().setLayoutY(point.getY()+shape.getShape().getBoundsInLocal().getCenterY()); 
				}

			}
		}
	}


	@Override
	public ArrayList<ConnectionNode> getConnectionSubNodes(boolean includeStructures) {
		return this.getConnectionSubPane().getConnectionNodes(includeStructures);
	}

	/**
	 * Get the connection sub pane. This contains any sub connection nodes. 
	 * @return the connection sub pane. 
	 */
	public ConnectionPane getConnectionSubPane() {
		return this.getConnectionNodeBody().getConnectionSubPane();
	}


	public boolean isExpanded() {
		// TODO Auto-generated method stub
		return this.getConnectionNodeBody().isExpanded();
	}



	//	this.getConnectionNodeBody().layoutXProperty().addListener((obsVal, oldVal, newVal)->{
	//		for (ConnectionNode subDetNode: subConnectionNodes) {
	//			subDetNode.getConnectionNodeBody().setLayoutX(subDetNode.getConnectionNodeBody().getLayoutX()+(newVal.doubleValue()-oldVal.doubleValue()));
	//			//must move all the plugs too as they can be unbound
	//			for (ConnectorNode plug: subDetNode.getConnectionPlugs()) {
	//				if (!plug.getShape().layoutXProperty().isBound()) {
	//					plug.getShape().setLayoutX(plug.getShape().getLayoutX()+(newVal.doubleValue()-oldVal.doubleValue()));
	//				}
	//			}
	//			//TODO add sockets?
	//		}
	//	});
	//
	//	this.getConnectionNodeBody().layoutYProperty().addListener((obsVal, oldVal, newVal)->{
	//		for (ConnectionNode subDetNode: subConnectionNodes) {
	//			subDetNode.getConnectionNodeBody().setLayoutY(subDetNode.getConnectionNodeBody().getLayoutY()+(newVal.doubleValue()-oldVal.doubleValue()));
	//			//must move all the plugs too as they can be unbound
	//			for (ConnectorNode plug: subDetNode.getConnectionPlugs()) {
	//				if (!plug.getShape().layoutXProperty().isBound()) {
	//					plug.getShape().setLayoutY(plug.getShape().getLayoutY()+(newVal.doubleValue()-oldVal.doubleValue()));
	//				}
	//			}
	//			//TODO- add sockets?
	//		}
	//	});


}
