package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;

/**
 * Holds all elements of a connection node together. This consists
 * of a rectangle which can contain graphics, string names, controls etc., plugs
 * which can be dragged to connect to other connection nodes and lines
 * connecting plugs to the connection node.
 * <p>
 * Note:Originally a single subclass of pane was going to deal with all of this
 * but issues with overlapping panes, z order, mouse events calling through
 * overlapping panes and several bits and pieces made thing very difficult.
 * Hence each node within the ConnectioNode is a child of whatever pane the
 * whole thing sits on- the whole class is packaged as a Group in order to make
 * it easier to all to a scene. That way all parts use the same co-ordinate
 * system.
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardConnectionNode extends Group implements ConnectionNode {

	/**
	 * Indicates the a node has finished dragging. 
	 */
	public static final int DRAG_DROP = 0;
	
	/**
	 * Indicates the a node has been dragged to another position.
	 */
	public static final int DRAGGING_CHNAGED = 1;

	/**
	 * Indicates a click outside the connection pane. 
	 */
	public static final int CONNECTION_PANE_CLICK = 2;
	
	/**
	 * Indicates that a node is being dragged. 
	 */
	public static final int DRAGGING = 4; 
	
	/**
	 * The connection rectangle pane. 
	 */
	private ConnectionNodeBody connectionNodeBody;

	/**
	 * List of all plugs associated with a ConnectionNode. Multiple plugs can
	 * associated with each CopnnectionNode.
	 */
	private ArrayList<StandardConnectionPlug> connectionPlugs;

	/**
	 * List of all main sockets associated with a ConnectionNode. Multiple sockets
	 * can associated with each CopnnectionNode and each socket in this is assumed
	 * to be an input to the connection node. Note this is different to
	 * branchConnectionSockets which are sockets on the plug line of the connection
	 * node and do not act as inputs for the ConnectionNode they're associated with.
	 */
	private ArrayList<StandardConnectionSocket> connectionSockets;

	/**
	 * List of sockets on a connection line of one the plugs of the this connection
	 * node. These socket to do not provide any input to the ConnectionNode but are
	 * stored here for reference.
	 */
	private ArrayList<StandardConnectionSocket> branchConnectionSockets;

	/**
	 * Connection pane associated with the ConnectionNode. 
	 */
	protected ConnectionPane connectionPane;

	/**
	 * List of connection listeners.
	 */
	private ArrayList<ConnectionListener> connectionListeners;
	
	/**
	 * List of ConnectorNodeListeners
	 */
	private ArrayList<ConnectorNodeListener> connectorNodeListeners;

	/**
	 * Orientation of the node. This can only be set in the constructor. 
	 */
	private Orientation orientation=Orientation.HORIZONTAL;

	/**
	 * Allow branch sockets. Branch sockets are sockets which allow plugs to connect to the 
	 * plug connection lines of the node. 
	 */
	boolean allowBranchSockets=false;
	
	/**
	 * Indicates whether mouse actions are disabled or not. 
	 */
	private boolean mouseDisable= false; 
	
	/**
	 * Default x position of plug in relation to top of rectangle.
	 */
	DoubleProperty plugX=new SimpleDoubleProperty(20);
	
	/**
	 * Default y position of plug in relation to right of rectangle.
	 */
	DoubleProperty plugY=new SimpleDoubleProperty(20);
	
	/**
	 * Default y position of socket in relation to right of rectangle.
	 */
	DoubleProperty socketX=new SimpleDoubleProperty(20);
	
	/**
	 * Default y position of socket in relation to right of rectangle.
	 */
	DoubleProperty socketY=new SimpleDoubleProperty(20);

	/**
	 * Default plug dimensions
	 */
	public static double plugBodyWidth=12;
	public static double plugEndWidth=4;
	public static double plugBodyHeight=25;
	public static double plugEndHeight=16;
	public static double cornerRadius=5;

	/**
	 * Default width of the node
	 */
	public static double DEFUALT_PREF_WIDTH = 100;
	
	/**
	 * Default height of the node
	 */
	public static double DEFUALT_PREF_HEIGHT = 100;

	/**
	 * Create a connection node. 
	 * @param connectionPane - the pane the connection node belongs to. 
	 * @param oreintation - orientation of the node- HORIZONTAL or VERTICAL. 
	 */
	public StandardConnectionNode(ConnectionPane connectionPane, Orientation orientation){
		this.orientation=orientation; 
		this.connectionPane=connectionPane;
		createConnectionNode();
	}
	

	/**
	 * Create a standard horizontal connection node.	
	 * @param connectionPane - the pane the connection node belongs to. 
	 */
	public StandardConnectionNode(ConnectionPane connectionPane){
		this.connectionPane=connectionPane;
		createConnectionNode();
	}

	
	/**
	 * Create the connection node. This should only be called once in the constructor. 
	 */
	private void createConnectionNode(){
		
		//create listeners. 
		connectionListeners=new ArrayList<ConnectionListener>(); 
		connectorNodeListeners=new ArrayList<ConnectorNodeListener>(); 

		//create rectangle
		getChildren().add(getConnectionNodeBody());

		//create plug lists. 
		connectionPlugs=new ArrayList<StandardConnectionPlug>();
		connectionSockets=new ArrayList<StandardConnectionSocket>();
		branchConnectionSockets=new ArrayList<StandardConnectionSocket>();

		//create first plug
		addDefaultPlug();
		//create first socket
		addDefaultSocket(); 
	}

	
	/**
	 * Create a default plug and adds to the ConnectionNode. This is initially bound to the rectangle but un-binds as 
	 * soon as a drag event is registered or enableFreePlug();
	 * @return a default plug.
	 */
	public StandardConnectionPlug addDefaultPlug(){

		StandardConnectionPlug plug=createPlug();
		/**
		 * If a new plug but not the first set to disabled- important this is placed here 
		 * in function to prevent weird behaviour where plug is created enabled, binds to something and then
		 * CONNECTION_DISABLED flag is set. 
		 */
		//System.out.println("CONNECTION NODE: ADD DEFAULT PLUG. "+  connectionPlugs.size());
		if (connectionPlugs.size() > 0) {
			plug.setConnectionStatus(ConnectorNode.CONNECTION_DISABLED, null);
			plug.toBack();
		}
		
		//init layout of the plug
		initDefaultPlugLayout(plug);  

		//add children to group; 
		getChildren().add(plug);
		getChildren().addAll(plug.getPlugConnectionLines());

		//create a local reference; 
		connectionPlugs.add(plug);

		return plug; 
	}
	
	/**
	 * Create the connection plug. 
	 * @return the connection plug. 
	 */
	public StandardConnectionPlug createPlug() {
		return new StandardConnectionPlug(this, plugBodyWidth, plugEndWidth, plugBodyHeight, plugEndHeight,  cornerRadius, orientation);
	}
	
	
	/**
	 * Layout defaults and binding for a socket. 
	 * @param socket - the socket to apply binding and layout to. 
	 */
	public void initDefaultPlugLayout(StandardConnectionPlug plug) {
		if (orientation==Orientation.HORIZONTAL){
			//if horizontal the plug sits at the right of the rectangle. 
			plug.layoutXProperty().bind(connectionNodeBody.layoutXProperty().add(plugX).add(connectionNodeBody.widthProperty()));
			plug.layoutYProperty().bind(connectionNodeBody.layoutYProperty().add(plugY));
		}
		else{
			//if vertical the plug sits at the bottom of the rectangle. 
			plug.layoutXProperty().bind(connectionNodeBody.layoutXProperty().add(connectionNodeBody.widthProperty().divide(2)));
			plug.layoutYProperty().bind(connectionNodeBody.layoutYProperty().add(connectionNodeBody.heightProperty()).add(plugY));
		}
	}


	/**
	 * Remove a connectionPlug from the connection node. If no plugs are present a new one will be added. 
	 * @param newPlug - true to add new plug IF there are no plugs after plug  is removed. 
	 */
	protected void removeConnectionPlug(StandardConnectionPlug plug, boolean newPlug){
		this.getChildren().remove(plug);
		this.getChildren().removeAll(plug.getPlugConnectionLines());
		//remove from one of two arrays. 
		this.connectionPlugs.remove(plug);
		//if only one plug left in node or no plugs at all then add new plug. 
		//System.out.println("REMOVE CONNECTION PLUG: " + connectionPlugs.size() + " new plug? "+newPlug);
		
		if (connectionPlugs.size()<=0 && newPlug){
			addDefaultPlug();
			return;
		}
		
		//if there is another plug and it's disabled enable it. 
		if (connectionPlugs.size()==1) {
			connectionPlugs.get(0).setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
		}
	}


	/**
	 * Remove all plugs. 
	 */
	public void removeAllConnectionPlugs(){
		while (connectionPlugs.size()>0){
			removeConnectionPlug(connectionPlugs.get(0), false);
		}
	}


	/**
	 * Create a default socket which has layout bounds binded to the ConnectionRectangle. 
	 * @return a default connection socket. 
	 */
	public StandardConnectionSocket addDefaultSocket(){
		StandardConnectionSocket socket=new StandardConnectionSocket(StandardConnectionNode.this, plugBodyWidth-5, 
				plugEndWidth, plugBodyHeight, plugEndHeight,cornerRadius,orientation);
		
		//init the default layout. 
		initDefaultSocketLayout(socket); 

		//add children to group; 
		socket.setSocketConnectionLines(createSocketLines(socket, orientation));
		this.getChildren().add(socket);
		this.getChildren().addAll(socket.getSocketConnectionLines());
		StandardConnectionNode.this.getChildren().add(socket.getCanConnectLine());

		//create a local reference; 
		connectionSockets.add(socket);

		return socket; 
	}
	
	
	/**
	 * Layout defaults and binding for a socket. 
	 * @param socket - the socket to apply binding and layout to. 
	 */
	public void initDefaultSocketLayout(StandardConnectionSocket socket) {
		if (orientation==Orientation.HORIZONTAL){
			socket.layoutXProperty().bind(connectionNodeBody.layoutXProperty().subtract(socketX));
			socket.layoutYProperty().bind(connectionNodeBody.layoutYProperty().add(socketY));
		}
		else{
			socket.layoutXProperty().bind(connectionNodeBody.layoutXProperty().add(connectionNodeBody.widthProperty().divide(2)));
			socket.layoutYProperty().bind(connectionNodeBody.layoutYProperty().subtract(socketY));
		}
	}

	
	/**
	 * Remove a connectionSocket from the connection node. 
	 */
	public void removeConnectionSocket(StandardConnectionSocket socket){
		if (socket.getConnectedShape()!=null){
			socket.getConnectedShape().setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
			socket.getConnectedShape().getShape().layoutXProperty().unbind();
			socket.getConnectedShape().getShape().layoutYProperty().unbind();
		}
		this.getChildren().remove(socket);
		this.getChildren().removeAll(socket.getSocketConnectionLines());
		this.getChildren().remove(socket.getCanConnectLine());

		//remove from one of two arrays. 
		if (socket.isBranch()){
			this.branchConnectionSockets.remove(socket);
			//now need to trigger the connection listener for the socket that was accepting this branch. - a bit difficult because it's
			//parent socket rather than the temp socket attached to the connection line.m 
			ConnectorNode branchPrimarySocket=socket.getParentConnectionPlug().getConnectedShape();
			if (branchPrimarySocket!=null){
				//System.out.println("A socket has been found for the branch which is about to be removed.");
				branchPrimarySocket.getConnectionNode().notifyConnectionListeners(branchPrimarySocket, null, ConnectorNode.NO_CONNECTION);
			}
		}
		this.connectionSockets.remove(socket);

	}

	
	/**
	 * Remove all sockets. 
	 */
	public void removeAllConnectionSockets(){
		for (int i=0; i<connectionSockets.size(); i++){
			removeConnectionSocket(connectionSockets.get(0));
		}
	}

	
	/**
	 * Remove all branch sockets; 
	 */
	public void removeAllBranchSockets() {
		//System.out.println("Number of branch sockets "+branchConnectionSockets.size());
		for (int i=branchConnectionSockets.size()-1; i >= 0; i--){
			//System.out.println("Number of branch sockets "+branchConnectionSockets.size() + "i "+i);
			removeConnectionSocket(branchConnectionSockets.get(i));
		}

	}

	
	/**
	 * Create a line connecting the rectangle to the default socket. The first line
	 * in the list remain invisible unless a plug is nearby, in which case it
	 * connects to the plug.
	 * 
	 * @param connectionSocket - the connection socket to be associated with the
	 *                         lines.
	 * @return a line connecting the socket to the rectangle
	 */
	private ArrayList<ConnectionLine> createSocketLines(StandardConnectionSocket connectionSocket, Orientation orientation){

		//line which connects the socket to the ConnectionRectangle. 
		ConnectionLine line1= new ConnectionLine(this, connectionSocket);
		if (orientation==Orientation.HORIZONTAL){
			line1.startXProperty().bind(connectionNodeBody.layoutXProperty()); 
			line1.startYProperty().bind(connectionNodeBody.layoutYProperty().add(socketY));
			line1.endXProperty().bind(connectionSocket.layoutXProperty().add(plugBodyWidth));
			line1.endYProperty().bind(connectionSocket.layoutYProperty());
		}
		else{
			line1.startXProperty().bind(connectionNodeBody.layoutXProperty().add(connectionNodeBody.widthProperty().divide(2))); 
			line1.startYProperty().bind(connectionNodeBody.layoutYProperty());
			line1.endXProperty().bind(line1.startXProperty());
			line1.endYProperty().bind(connectionSocket.layoutYProperty().add(plugBodyWidth));
		}

		ArrayList<ConnectionLine> socketLines=new ArrayList<ConnectionLine>();
		socketLines.add(line1);

		return socketLines;
	}


	/**
	 * Create straight plug lines. This is one straight line between the plug and the rectangle.
	 * @param polygonPlug - the plug the line is associated with.
	 * @return an array with one line connecting the male plug to the rectangle. 
	 */
	@SuppressWarnings("unused")
	private ArrayList<Line> createStraightPlugLines(StandardConnectionPlug polygonPlug) {

		Line line= new Line();
		line.startXProperty().bind(connectionNodeBody.widthProperty()); 
		line.startYProperty().bind(plugY);
		line.endXProperty().bind(polygonPlug.layoutXProperty());
		line.endYProperty().bind(polygonPlug.layoutYProperty());

		ArrayList<Line> plugLines=new ArrayList<Line>();

		return plugLines;
	}
	
	
	/**
	 * Pass all collision listeners on all 'connection' shapes which are not in
	 * proximity to other shapes a null shape. This will return shapes to default
	 * colours etc.
	 */
	public void setCollisionShapesNull(){
		setCollisionShapesNull(connectionPane); 
	}


	/**
	 * Pass all collision listeners on all 'connection' shapes which are not in
	 * proximity to other shapes a null shape. This will return shapes to default
	 * colours etc.
	 * @param connectionPane - the connection pane to search for other conenction shapes.  
	 */
	public void setCollisionShapesNull(ConnectionPane connectionPane){
		ArrayList<ConnectorNode> connectorShapes= new ArrayList<ConnectorNode>();
		connectorShapes.addAll(connectionPane.getConnectionSockets(null));
		connectorShapes.addAll(connectionPane.getConnectionPlugs(null));

		//need to check all plugs and sockets. Don't want to needlessly send NO_CONNECTION flags to all so check each one. 
		for (int i=0; i<connectorShapes.size(); i++){
			//don't want to tell shapes which are connected that they are no longer connected. 
			if (connectorShapes.get(i).getConnectionStatus()!=ConnectorNode.CONNECTED && connectorShapes.get(i).getConnectionStatus()!=ConnectorNode.CONNECTION_DISABLED){
				if (!connectorShapes.get(i).checkPossibleConnection(false)){
					connectorShapes.get(i).setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
				} 
			}
		}

		//now set all lines to null as these can only be connected. 
		ArrayList<ConnectionLine> connectionLines=connectionPane.getConnectionLines(null); 
		for (int i=0; i<connectionLines.size(); i++){
			if (connectionLines.get(i).getConnectionStatus()!=ConnectorNode.CONNECTION_DISABLED){
				connectionLines.get(i).setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
			}
		}

	}

	/**
	 * Get the ConnectionRectangle for a ConnectionNode. 
	 * The ConnectionRectangle is essentially a blank pane and can have
	 * various controls, images etc. added. 
	 * @return the ConnectionRectangle for a ConnectionNode. 
	 */
	public ConnectionNodeBody getConnectionNodeBody() {
		if (connectionNodeBody==null) {
			connectionNodeBody=createNodeBody();
		}
		return connectionNodeBody;
	}

	/**
	 * Create the connection node shape. 
	 * @return the connection node shape. 
	 */
	public ConnectionNodeBody createNodeBody() {
		return new ConnectionNodeBody(this); 
	}

	/**
	 * Gets all the connection plugs for a ConnectionNode. 
	 * @return a list of plugs for a ConnectionNode. 
	 */
	public ArrayList<StandardConnectionPlug> getConnectionPlugs() {
		return connectionPlugs;
	}

	/**
	 * Gets a list of sockets associated  with a ConnectionNode. 
	 * @return a list of sockets for a ConnectionNode. 
	 */
	public ArrayList<StandardConnectionSocket> getConnectionSockets() {
		return connectionSockets;
	}


	/**
	 * Get list of sockets on a connection line of one the plugs of the this connection node. These socket to do not provide any input 
	 * to the ConnectionNode but are stored here for reference. 
	 * @return list of branch connection sockets. 
	 */
	public ArrayList<StandardConnectionSocket> getBranchConnectionSockets() {
		return branchConnectionSockets;
	}

	/**
	 * Get all connection lines connected to plugs
	 * @return all connection lines for all plugs associated with a ConnectionNode. 
	 */
	public ArrayList<ConnectionLine> getPlugConnectionLines(){
		ArrayList<ConnectionLine> connectionLines=new ArrayList<ConnectionLine>();
		for (int i=0; i< connectionPlugs.size(); i++){
			connectionLines.addAll(connectionPlugs.get(i).getPlugConnectionLines()); 
		}

		return connectionLines; 
	}

	/**
	 * Get all connection lines connected to sockets
	 * @return all connection lines for all sockets associated with a ConnectionNode. 
	 */
	public ArrayList<ConnectionLine> getSocketConnectionLines(){
		ArrayList<ConnectionLine> connectionLines=new ArrayList<ConnectionLine>();

		for (int i=0; i< connectionPlugs.size(); i++){
			connectionLines.addAll(connectionSockets.get(i).getSocketConnectionLines()); 
		}

		return connectionLines; 
	}

	/**
	 * Get the position of the node in the list of nodes in the connection pane. 
	 * @return position of node in list of nodes added to connection pane. 
	 */
	public int getNodeId() {
		return connectionPane.getConnectionNodes().indexOf(this);
	}

	/**
	 * Get the connection pane the ConnectionNode is associated with. 
	 * @return The ConnectionPane the ConnectionNode is associated with. Returns null if the node has no pane. 
	 */
	public ConnectionPane getConnectionPane() {
		return connectionPane;
	}

	/**
	 * Check that a point is within the connection pane (top and left) and if no return nearest position on pane boundary.
	 * @param x -scene x co-ordinate
	 * @param y - scene y co-ordinate
	 * @return null if within ConnectionPane boundary or a new Point2D of closest point on boundary if outside boundary. 
	 */
	public Point2D checkWithinPane(double x ,double y){
		Point2D point2D=new Point2D(x, y); 
		//System.out.println(" Pane x "+ point2D.getX()+ " Pane y "+point2D.getY());
		if (point2D.getX()<0 || point2D.getY()<0){
			Point2D newPoint= new Point2D(Math.max(0, point2D.getX()), Math.max(0, point2D.getY()));
			return newPoint; 
		}
		return null;
	}

	/**
	 * Check whether branch sockets are allowed. Branch sockets allow plugs to connect to other plugs allowing multiple inputs 
	 * into another ConnectionNode. 
	 * @return true if branch sockets are allowed
	 */
	public boolean isAllowBranchSockets() {
		return allowBranchSockets;
	}

	/**
	 * Set whether branch sockets are allowed. Branch sockets allow plugs to connect to other plugs allowing multiple inputs 
	 * into another ConnectionNode. 
	 * @param allowBranchSockets - true to allow branch sockets. False to prevent branch sockets. 
	 */
	public void setAllowBranchSockets(boolean allowBranchSockets) {
		this.allowBranchSockets = allowBranchSockets;
		ArrayList<ConnectionLine> plugLines=this.getPlugConnectionLines();
		//set all lines - new lines will be set to the nodes allowBranchSockets. 
		for (int i=0; i<plugLines.size(); i++){
			plugLines.get(i).setAllowBranchSockets(allowBranchSockets);
		}
	}

	/**
	 * Set all lines connecting a plug to node to accept/reject branch sockets
	 * @param connectedPlug - the plug to which the lines belong
	 * @param allow - true to allow branch sockets. False to reject branch sockets. 
	 */
	public void setAllowBranchSocket(StandardConnectionPlug connectedPlug, boolean allow){
		if (!allow) connectedPlug.getConnectionNode().removeAllBranchSockets(); 
		//System.out.println("Plug connection lines size "+connectedPlug.getPlugConnectionLines().size());
		for (int i=0; i<connectedPlug.getPlugConnectionLines().size(); i++){
			connectedPlug.getPlugConnectionLines().get(i).setAllowBranchSockets(allow);
		}
	}
	

	/**
	 * Disconnect a node.
	 * @param parentNode - the parent node to disconnect
	 * @return true if the plug was null. 
	 */
	public boolean disconnectNode(StandardConnectionNode parentNode){
//		System.out.println("Standard Connection Node: DISCONNECT NODE: ");
		
		 StandardConnectionPlug plug = (StandardConnectionPlug) ConnectionPane.disconnectNodes(this, parentNode); 
		 
		 if (plug==null) return false; 
		 else {
			plug.getConnectionNode().removeConnectionPlug(plug, true);
//			//only allow branch connection if the plug is connected
			setAllowBranchSocket(plug, false); 
			return true; 
		 }

//		//first find the plug which connects to this node. 
//		StandardConnectionPlug plug=(StandardConnectionPlug) ConnectionPane.getConnectionPlug(this,  parentNode, true);
//		if (plug==null){
//			//System.out.println("DISCONNECT NODE: plug is null"); 
//			return true; //no plug so not connected to this parent node anyway. 
//		} 
//
//		//set socket connection status to no connection
//		if (plug.getConnectedShape()!=null){
//			//System.out.println("DISCONNECT NODE: Connection Socket branch" +((ConnectionSocket) plug.getConnectedShape()).isBranch());
//			plug.getConnectedShape().setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
//		}
//
//		//set plug connection status to no connection.
//		plug.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
//		plug.getConnectionNode().removeConnectionPlug(plug, true);
//		setAllowBranchSocket(plug, false); 
//
//		//		/**
//		//		 * If the plug is dragged then the connection with the last connection shape is set to null
//		//		 * Therefore when disconnecting through code the last connection shape must be set to null otherwise
//		//		 * when plug is dragged a NO_CONNECTION will be sent to the socket. 
//		//		 */
//		//		plug.lastConnectionShape=null;
//
//		return false;

	}

	/**
	 * Connect another node to this node. Look for a free connection socket. If no socket is free then remove 
	 * shape from first socket and connect with that socket. If no socket exists do nothing. Note this does not support
	 * connections via branch sockets
	 * @return true if connection was successful, otherwise false. 
	 */
	public boolean connectNode(StandardConnectionNode parentNode){ 

		if (ConnectionPane.isNodeConnected(this, parentNode, true, false)){
			System.out.println("Connection Node: these nodes are already connected!");
			return false; 
		}

		if (getConnectionSockets().size()<=0) return false; 

		//first find the first free socket. 
		StandardConnectionSocket connectionSocket = null; 
		for (int i=0; i<connectionSockets.size(); i++){
			//check if a socket is free. 
			if (connectionSockets.get(i).getConnectedShape()!=null && 
					connectionSockets.get(i).getConnectedShape().getConnectionNode()==parentNode){
				//important here as complexity of all this connection socket code means occasionally can attempt to connect connected nodes. 
				System.out.println("Connection Node: these nodes are already connected!");
				return false; 
			}
			if (connectionSockets.get(i).getConnectedShape()==null
					&& connectionSockets.get(i).getConnectionStatus() !=  ConnectorNode.CONNECTION_DISABLED){
				connectionSocket=getConnectionSockets().get(i); 
				break; 
			}
		}

		//next find a free plug in the parent node.
		StandardConnectionPlug connectionPlug = null; 
		boolean newPlug=false; 
		for (int i=0; i<parentNode.getConnectionPlugs().size(); i++){
			if (parentNode.getConnectionPlugs().get(i).getConnectedShape()==null
					&& parentNode.getConnectionPlugs().get(i).getConnectionStatus()!=ConnectorNode.CONNECTION_DISABLED ){
				connectionPlug=parentNode.getConnectionPlugs().get(i);
			}
		}

		//if no plug create a new one
		if (connectionPlug==null){
			newPlug=true; 
			connectionPlug=parentNode.addDefaultPlug(); 
			connectionPlug.enableFreePlug();
		}


		//if no socket is free disconnect first socket or connect via a branch socket. 
		if (connectionSocket==null){
			//System.out.println(" CONNECTION NODE: No socket is free: "); 
			//get first socket and find connected plug. 
			connectionSocket=getConnectionSockets().get(0); 
			StandardConnectionPlug plug= (StandardConnectionPlug) connectionSocket.getConnectedShape(); 
			if (plug==null){
				System.err.println("Connection Node: could not connect two nodes."); 
				return false; 
			}
			//check if branch sockets are allowed and if so connect that way 
			ConnectionLine line=plug.getPlugConnectionLines().get(plug.getPlugConnectionLines().size()-1); 

			if (line.isAllowBranchSockets()){
				//				System.out.println(" CONNECTION NODE: Connect via branch socket: "); 
				//if a branch socket connect by branch socket. 
				connectionPlug.layoutXProperty().unbind();
				connectionPlug.layoutYProperty().unbind();

				connectionPlug.setLayoutX(line.getStartX()+Math.max(30,Math.random()*80));
				connectionPlug.setLayoutY(line.getStartY()+Math.max(30,Math.random()*80));

				connectionPlug.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);

				/**
				 * Might be an issue were line has been disconnected but the sockets have not nad therefore
				 * end up with a branch socket on line connected to the parent- need to check this. 
				 * TODO- may need to check all lines instead of just the one.
				 */
				if (line.checkBranchSocketParent(connectionPlug)){
					line.removeTempSocket();
					line.addBranchNewSocket(connectionPlug);
				}
				else {
					if (newPlug) connectionPlug.getConnectionNode().removeConnectionPlug(connectionPlug, true);
				}
				return true;
			}
			//otherwise need to connect by getting rid of a plug which is already connected. 
			else{
				//				System.out.println(" CONNECTION NODE: Force connect by removing other plug socket: "); 
				connectionSocket.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);

				//set correct connect status on disconnected shape and shift it slightly to show has been disconnected.
				if (plug!=null){

					plug.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
					setAllowBranchSocket(plug, false); //this plug can no longer have branch sockets as its disconnected
					plug.getConnectionNode().removeConnectionPlug(plug, true);
					//					/**
					//					 * If the plug is dragged then the connection with the last connection shape is set to null
					//					 * Therefore when disconnecting through code the last connection shape must be set to null otherwise
					//					 * when plug is dragged a NO_CONNECTION will be sent to the socket. 
					//					 */
					//					plug.lastConnectionShape=null;

				}
			}
		}

		//connect socket and plug
		//inform both socket and plug they're now connected. 
		connectionSocket.setConnectionStatus(ConnectorNode.CONNECTED, connectionPlug);
		connectionPlug.setConnectionStatus(ConnectorNode.CONNECTED, connectionSocket);

		return true; 
	}

	/**
	 * Set the y location of the connection plugs. 
	 * @param plugY - how far down the connection node in pixels the plug should be
	 */
	public void setPlugY(double plugY) {
		this.plugY.set(plugY);
	}

	/**
	 * Set the x offset of the connection plugs, i.e. how far they jut out from a connection node main body
	 * @param plugY - how far down the connection node in pixels the plug should be
	 */
	public void setPlugX(double plugX) {
		this.plugX.set(plugX);
	}

	/**
	 * Called from the connection pane to notify a change from another connection node. 
	 * @param flag - the change type
	 * @param connectionNode - the connection node associated with the changed
	 */
	public void notifyChange(int flag, StandardConnectionNode connectionNode2) {
		//no action for standard connections nodes. 
	}

	/**
	 * Disables all mouse interactions within the node. 
	 * @param true to disable all mouse interaction. False to enable mouse actions.
	 */
	public void setDisableMouseInteraction(boolean disable) {
		this.mouseDisable=disable; 
	}
	
	/**
	 * True to disable mouse connections.
	 * @return true. 
	 */
	public boolean isMouseDisable() {
		return mouseDisable; 
	}

	/**
	 * Get the orientation of the ConnectionNode. Orientation effects
	 * Positions of plugs and sockets. 
	 * @return the orientation
	 */
	public Orientation getOrientation() {
		return this.orientation;
	}

	/**
	 * Get plugY property. How far the plug sits down the node in Y. 
	 * @return the plug Y property
	 */
	public DoubleProperty plugYProperty() {
		return this.plugY;
	}
	
	/**
	 * Get plugX property. How far the plug sits down the node in Y. 
	 * @return the plug X property
	 */
	public DoubleProperty plugXProperty() {
		return this.plugX;
	}
	
	/**
	 * Get the socket Y property. How many from top of connection node body the scoket
	 * should be. 
	 * @return the socket y property. 
	 */
	public DoubleProperty socketYProperty() {
		return this.socketY;
	}
	
	/**
	 * Get the socket X property. How many from top of connection node body the scoket
	 * should be. 
	 * @return the socket y property. 
	 */
	public DoubleProperty socketXProperty() {
		return this.socketX;
	}

	/**
	 * Get all node parents of a connection node. Parents of a ConnectionNode are
	 * those noses which are connected directly before that node. i.e. you can draw
	 * a direct line form the socket of the node to the plug of a parent node. 
	 * Note that structures are not included as parents. 
	 * 
	 * @param connectionNode - the connection node for which to find parent
	 * @return a list of children. List will be empty if the node has no children.
	 */
	@Override
	public ArrayList<ConnectionNode> getParentConnectionNodes(){
		
		ArrayList<ConnectionNode> parentNodes=new ArrayList<ConnectionNode>(); 
		StandardConnectionPlug connectedPlug;
		for (int i=0; i<this.getConnectionSockets().size(); i++){
			if (this.getConnectionSockets().get(i).getConnectionStatus() == ConnectorNode.CONNECTED ){
				//socket is connected, therefore need to find connected plug and it's ConnectionNode
				connectedPlug=((StandardConnectionPlug) this.getConnectionSockets().get(i).getConnectedShape());
				parentNodes.add(connectedPlug.getConnectionNode());
				for (int j=0; j<connectedPlug.getBranchSockets().size(); j++){
					//get ConnectionNodes attached via branch sockets. 
					if (connectedPlug.getBranchSockets().get(j).getConnectedShape()!=null){
						parentNodes.add(connectedPlug.getBranchSockets().get(j).getConnectedShape().getConnectionNode());
					}
				}
				
			}
		}

//		long time2=System.currentTimeMillis(); 
		//System.out.println("ConnectionPane.getParentConnectionNodes(...) "+(time2-time1));

		return parentNodes;
	}


	@Override
	public ArrayList<ConnectionNode> getChildConnectionNodes() {
		ArrayList<ConnectionNode> childNodes=new ArrayList<ConnectionNode>(); 
		StandardConnectionSocket connectedSocket;
		
		for (int i=0; i<this.getConnectionPlugs().size(); i++){
			//System.out.println("connectionNode.getConnectionPlugs() "+i  + " status "+connectionNode.getConnectionPlugs().get(i).getConnectionStatus() + " connected Shape  "+connectionNode.getConnectionPlugs().get(i).getConnectedShape());
			if (this.getConnectionPlugs().get(i).getConnectionStatus() == ConnectorNode.CONNECTED ){
				//plug is connect- but connected to what?..
				connectedSocket=((StandardConnectionSocket) this.getConnectionPlugs().get(i).getConnectedShape());
				//System.out.println("connectedSocket.isBranch() "+connectedSocket.isBranch());
				if (connectedSocket.isBranch()) {
					//have a branch socket. Therefore need to trace onto connection line and check what the plug of that line is connected to. 
					ConnectorNode connectionShape=connectedSocket.getParentConnectionPlug().getConnectedShape();
					//now we have the plug which, if connected, connects to a child node. 
					if (connectionShape!=null){
						childNodes.add(connectionShape.getConnectionNode());
					}
				}
				else childNodes.add(connectedSocket.getConnectionNode());
			}
		}
		
//		long time2=System.currentTimeMillis(); 
//		System.out.println("ConnectionPane.getChildConnectionNodes(...) "+(time2-time1));
		
		return childNodes; 
	}
	
	/***Connection Listeners***/
	
	/**
	 * Adds a connectionListener which is triggered by any CollisionShape; 
	 * @param connectListener - connectionlistener to add; 
	 */
	@Override
	public void addConnectionListener(ConnectionListener connectListener){
		this.connectionListeners.add(connectListener);
	}
	
	@Override
	public void removeConnectionListener(ConnectionListener connectListener){
		this.connectionListeners.remove(connectListener);
	}

	/**
	 * Get the number of connection listeners which have been added to the node. 
	 * @return the number of connection listeners attached to the node. 
	 */
	public int getNumConnectionListeners(){
		return this.connectionListeners.size();
	}

	/**
	 * Trigger all connection listeners
	 * @param shape - shape which has connection
	 * @param shape - shape with which connection has occurred- null if no shape has been found/ connection has been broken. 
	 * @param type -  type of connection as defined in {@link ConnectorNode}
	 */
	protected void triggerConnectionListeners(ConnectorNode shape,
			ConnectorNode foundShape, int type) {
		for (int i=0; i<connectionListeners.size(); i++){
			connectionListeners.get(i).collisionEvent(shape, foundShape, type);
		}
	}


	@Override
	public void notifyConnectionListeners(ConnectorNode connectedShape, ConnectorNode plugShape,
			int connected) {
		triggerConnectionListeners(connectedShape, plugShape, connected);
	}


	@Override
	public void addConnectorNodeListener(ConnectorNodeListener connectorNodeListener) {
		this.connectorNodeListeners.add(connectorNodeListener);
	}


	@Override
	public void removeConnectorNodeListener(ConnectorNodeListener connectorNodeListener) {
		this.connectorNodeListeners.remove(connectorNodeListener);
	}
	
	/**
	 * Trigger all connector node listeners
	 * @param shape - shape which has connection
	 * @param type -  type of connection as defined in {@link ConnectorNode}
	 */
	protected void triggerConnectorNodeListeners(ConnectorNode shape, int type) {
		for (int i=0; i<connectorNodeListeners.size(); i++){
			connectorNodeListeners.get(i).nodeConnectorEvent(shape, type);
		}
	}


	@Override
	public Group getConnectionGroup() {
		return this;
	}
	
	/**
	 * Set the nodes connection pane. 
	 */
	public void setConnectionPane(ConnectionPane connectionPane) {
		this.connectionPane = connectionPane;
	}

//	/**
//	 * Set plugs only visible
//	 * @param visible. 
//	 */
//	public void setPlugsVisible(boolean visible) {
//		//set plugs visible/invisible
//		for (int i=0; i< connectionPlugs.size(); i++) {
//			connectionPlugs.get(i).getShape().setVisible(visible);
//		}
//		
//		//set plugs visible/invisible
//		for (int i=0; i< connectionSockets.size(); i++) {
//			connectionSockets.get(i).getShape().setVisible(visible);
//		}
//		
//	}



}
