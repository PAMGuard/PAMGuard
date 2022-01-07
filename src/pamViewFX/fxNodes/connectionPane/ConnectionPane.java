package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;
import java.util.stream.Collectors;

import pamViewFX.fxNodes.connectionPane.structures.ConnectionStructure;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionSubGroup;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

/**
 * A pane which allows users to drag rectangles around and connect them
 * together.
 * <p>
 * Although not abstract this pane is designed to be sub classed and used for
 * different types of applications. e.g. the PAMGUARD data model.
 * 
 * @author Jamie Macaulay
 *
 */
public class ConnectionPane extends Pane {

	/**
	 * An array to hold every connection node on the pane.
	 */
	private ArrayList<ConnectionNode> connectionNodes;

	/**
	 * The scale transform of the pane.
	 */
	private Scale scaleTransform;

	public ConnectionPane() {
		super();

		scaleTransform = new Scale(1, 1, 0, 0);
		this.getTransforms().add(scaleTransform);

		// setPannable(true);
		// Initialise the connection node array.
		connectionNodes = new ArrayList<ConnectionNode>();

		// set up some basic mouse behaviours
		this.setOnMouseClicked((e) -> {
			this.notifyChange(StandardConnectionNode.CONNECTION_PANE_CLICK);
		});

		this.setOnTouchPressed((e) -> {
			this.notifyChange(StandardConnectionNode.CONNECTION_PANE_CLICK);
		});
	}

	// /**
	// * Create new connection node. This can be overridden if using a subclass of
	// ConnectionNode.
	// * @param orientation - the orientation of the ConnectionNode. HORIZONTAL or
	// VERTICAL.
	// * @param type - a type flag which can be used if needed.
	// * @return a new ConnectionNode.
	// */
	// public abstract ConnectionNode createConnectionNode(Orientation orientation);

	/**
	 * Add a new connection node to the connection pane.
	 * 
	 * @param x - x position to add new node
	 * @param y - y position to add new node.
	 * @return the ConnectionNode which was added.
	 */
	public StandardConnectionNode addNewConnectionNode(StandardConnectionNode connectionNode, double x, double y) {
		// T connectionNode=createConnectionNode(orientation);
		connectionNode.getConnectionNodeBody().setLayoutX(x);
		connectionNode.getConnectionNodeBody().setLayoutY(y);

		connectionNode.setConnectionPane(this);
		connectionNodes.add(connectionNode);

		this.getChildren().add(connectionNode);

		return connectionNode;
	}

	/**
	 * Add a new connection node to the connection pane.
	 * 
	 * @return the ConnectionNode which was added.
	 */
	public void removeConnectionNode(StandardConnectionNode connectionNode) {
		this.getChildren().remove(connectionNode);
		connectionNodes.remove(connectionNode);
	}

	/**
	 * Get all sockets and all plug connection lines from all nodes. If
	 * connectionNode is specified then the plugs from that connection node are not
	 * included.
	 * 
	 * @param connectionNode - connection node NOT to include in list of plugs. If
	 *                       null then all plugs from all nodes are returned.
	 * @return a list of all connection plugs from all nodes.
	 */
	public ArrayList<ConnectorNode> getPlugAcceptingConnectionShapes(StandardConnectionNode connectionNode) {
		// System.out.println("ConnectionPane.getPlugAcceptingConnectionShapes(...)");
		ArrayList<ConnectorNode> shapes = new ArrayList<ConnectorNode>();
		for (int i = 0; i < connectionNodes.size(); i++) {
			if (connectionNodes.get(i) == connectionNode)
				continue;
			shapes.addAll(connectionNodes.get(i).getConnectionSockets());
			shapes.addAll(connectionNodes.get(i).getBranchConnectionSockets());
			shapes.addAll(connectionNodes.get(i).getPlugConnectionLines());
		}
		return shapes;
	}

	/**
	 * Get all the connection lines from all the ConnectionNodes in a
	 * ConnectionPane.
	 * 
	 * @param connectionNode - ConnectionNode to be excluded
	 * @return a list of lines from all non excluded ConnectionNodes.
	 */
	public ArrayList<ConnectionLine> getConnectionLines(StandardConnectionNode connectionNode) {
		// System.out.println("ConnectionPane.getConnectionLines(...)");
		ArrayList<ConnectionLine> shapes = new ArrayList<ConnectionLine>();
		for (int i = 0; i < connectionNodes.size(); i++) {
			if (connectionNodes.get(i) == connectionNode)
				continue;
			shapes.addAll(connectionNodes.get(i).getPlugConnectionLines());
		}
		return shapes;
	}

	/**
	 * Get all the connection plugs from all the ConnectionNodes in a
	 * ConnectionPane.
	 * 
	 * @param connectionNode - ConnectionNode to be excluded
	 * @return a list of plugs from all non excluded ConnectionNodes.
	 */
	public ArrayList<ConnectorNode> getConnectionPlugs(StandardConnectionNode connectionNode) {
		// System.out.println("ConnectionPane.getConnectionPlugs(...)");
		ArrayList<ConnectorNode> plugs = new ArrayList<ConnectorNode>();
		for (int i = 0; i < connectionNodes.size(); i++) {
			if (connectionNodes.get(i) == connectionNode)
				continue;
			plugs.addAll(connectionNodes.get(i).getConnectionPlugs());
		}
		return plugs;
	}

	/**
	 * Get all connection sockets from all Connection Nodes. This is includes branch
	 * sockets.
	 * 
	 * @param connectionNode - ConnectionNode to be excluded
	 * @return a list of sockets from all non excluded ConnectionNodes.
	 */
	public ArrayList<ConnectorNode> getConnectionSockets(StandardConnectionNode connectionNode) {
		// System.out.println("ConnectionPane.getConnectionSockets(...)");
		ArrayList<ConnectorNode> sockets = new ArrayList<ConnectorNode>();
		for (int i = 0; i < connectionNodes.size(); i++) {
			if (connectionNodes.get(i) == connectionNode)
				continue;
			sockets.addAll(connectionNodes.get(i).getConnectionSockets());
			sockets.addAll(connectionNodes.get(i).getBranchConnectionSockets());
		}
		return sockets;
	}

	@Override
	public String toString() {
		// print info on all nodes
		String string = "";
		for (int i = 0; i < connectionNodes.size(); i++) {
			string = string + "Connection Node : " + i + " No. plugs "
					+ connectionNodes.get(i).getConnectionPlugs().size() + " No. sockets "
					+ connectionNodes.get(i).getConnectionSockets().size() + " No. branch sockets "
					+ connectionNodes.get(i).getBranchConnectionSockets().size() + "\n";
		}
		return string;
	}

	// public ArrayList<ConnectionShape> getConnectionLines(ConnectionNode
	// connectionNode) {
	// ArrayList<ConnectionShape> lines=new ArrayList<ConnectionShape>();
	// for (int i=0; i<connectionNodes.size(); i++){
	// if (connectionNodes.get(i)==connectionNode) continue;
	// lines.addAll(connectionNodes.get(i).getConnectionLines());
	// }
	// return lines;
	// }

	/**
	 * Get all connectionNodes added to the ConnectionPane. This does NOT include
	 * structures.
	 * 
	 * @return a list of ConnectionNodes added to the connection pane.
	 */
	public ArrayList<ConnectionNode> getConnectionNodes() {
		return getConnectionNodes(false);
	}

	/**
	 * Get all connection nodes within the connection pane.
	 * 
	 * @param includeStructures - true to include structure nodes.
	 * @return a list of connection nodes including or not including connection
	 *         nodes.
	 */
	public ArrayList<ConnectionNode> getConnectionNodes(boolean includeStructures) {
		if (includeStructures)
			return connectionNodes;
		else {
			// use java streams to get all connection nodes other than connection
			// structures.
			return filterConnectionStructs(connectionNodes);
		}
	}

	/**
	 * Get all connection nodes within the connection pane and any associated sub
	 * panes. Note that this does NOT include connection structures.
	 * 
	 * @return a list of connection nodes including or not including connection
	 *         nodes.
	 */
	public ArrayList<ConnectionNode> getAllConnectionNodes() {
		return getAllConnectionNodes(false);
	}

	/**
	 * Get all connection structures within the connection pane and any associated
	 * sub panes. Note that this does NOT include connection nodes.
	 * 
	 * @return a list of connection structures including or not including connection
	 *         nodes.
	 */
	public ArrayList<ConnectionNode> getAllConnectionStructures() {
		return filterConnectionStructs(getAllConnectionNodes(true), false);
	}

	/**
	 * Get all connection nodes within the connection pane and any associated sub
	 * panes.
	 * 
	 * @param includeStructures - true to include structure nodes.
	 * @return a list of connection nodes including or not including connection
	 *         nodes.
	 */
	public synchronized ArrayList<ConnectionNode> getAllConnectionNodes(boolean includeStructures) {
		ArrayList<ConnectionNode> connectionNodeTemp = new ArrayList<ConnectionNode>();
		connectionNodeTemp.addAll(getConnectionNodes(true));
		for (ConnectionNode connectionNode : getConnectionNodes(true)) {
			if (connectionNode instanceof ConnectionSubGroup) {
				connectionNodeTemp.addAll(((ConnectionSubGroup) connectionNode).getConnectionSubNodes(true));
			}
		}
		if (includeStructures)
			return connectionNodeTemp;
		else
			return filterConnectionStructs(connectionNodeTemp);
	}

	/**
	 * Removes all connection structures from an array of connection nodes.
	 * 
	 * @param connectionNodes - the array of connection nodes. .
	 * @return the same array with all connection structures removed.
	 */
	private ArrayList<ConnectionNode> filterConnectionStructs(ArrayList<ConnectionNode> connectionNodes) {
		return filterConnectionStructs(connectionNodes, true);
	}

	/**
	 * Removes all connection structures from an array of connection nodes.
	 * 
	 * @param connectionNodes - the array of connection nodes.
	 * @param removeStructs   - true to remove the connection structure.
	 * @return the same array with all connection structures removed.
	 */
	private ArrayList<ConnectionNode> filterConnectionStructs(ArrayList<ConnectionNode> connectionNodes,
			boolean removestructs) {
		// use java streams to get all connection nodes other than connection
		// structures.
		if (removestructs) {
			return new ArrayList<ConnectionNode>(connectionNodes.stream()
					.filter(o -> !(o instanceof ConnectionStructure)).collect(Collectors.toList()));
		} else {
			return new ArrayList<ConnectionNode>(connectionNodes.stream()
					.filter(o -> (o instanceof ConnectionStructure)).collect(Collectors.toList()));
		}
	}

	/**
	 * Get the main pane which holds connectionNode. ConnectionNodes are children of
	 * this pane.
	 * 
	 * @return the pane which holds connectionNode.
	 */
	protected Pane getMainPane() {
		return this;
	}

	/**
	 * Get all node children of a connection node. Children of a connectionNode are
	 * those nodes which are connected directly after that node. i.e. you can draw a
	 * line directly from the plug of that node to a socket of a child node,
	 * including nay branch sockets;
	 * 
	 * @param connectionNode - the connection node for which to find children
	 * @return a list of children. List will be empty if the node has no children.
	 */
	public ArrayList<ConnectionNode> getChildConnectionNodes(StandardConnectionNode connectionNode) {
		return connectionNode.getChildConnectionNodes();
	}

	/**
	 * Get all node parents of a connection node. Parents of a ConnectionNode are
	 * those noses which are connected directly before that node. i.e. you can draw
	 * a direct line form the socket of the node to the plug of a parent node. Note
	 * that structures are not included as parents.
	 * 
	 * @param connectionNode - the connection node for which to find parent
	 * @return a list of children. List will be empty if the node has no children.
	 */
	public ArrayList<ConnectionNode> getParentConnectionNodes(StandardConnectionNode connectionNode) {
		return connectionNode.getParentConnectionNodes();
	}

	/**
	 * Connect two nodes.
	 * 
	 * @param childNode  - the child node.
	 * @param parentNode - the parent node.
	 * @return true if the connection was successful.
	 */
	public static boolean connectNodes(StandardConnectionNode childNode, StandardConnectionNode parentNode) {
		boolean connect = childNode.connectNode(parentNode);
		return connect;
	}

	/**
	 * Check whether a parent and child node are connected.
	 * 
	 * @param childNode      - the node which has a plug connected from the
	 *                       parentNode.
	 * @param parentNode     - the node which connects to the socket.
	 * @param includeBranch  - include parents connected via branch sockets.
	 * @param bypasStructure - if the parent node is a structure go to the next node
	 *                       to check for connection.
	 * @return true if connected and false if not connected.
	 */
	public static boolean isNodeConnected(StandardConnectionNode childNode, StandardConnectionNode parentNode,
			boolean includeBranch, boolean bypasStructure) {
		if (getConnectionPlug(childNode, parentNode, includeBranch, bypasStructure) == null)
			return false;
		else
			return true;
	}

	/**
	 * Find the plug used to connect between two nodes.
	 * 
	 * @param childNode      - the node which has a plug connected from the
	 *                       parentNode.
	 * @param parentNode     - the node whoch connects to the socket.
	 * @param includeBranch  - include parents connected via branch sockets.
	 * @param bypasStructure - if the parent node is a structure go to the next node
	 *                       to check for connection.
	 * @return the plug of the parent which connects to the child or null if no plug
	 *         is found.
	 */
	public static ConnectorNode getConnectionPlug(ConnectionNode childNode, ConnectionNode parentNode,
			boolean includeBranch, boolean bypassStructure) {

		// long time1=System.currentTimeMillis();

		// System.out.println("ConnectionPane: No. plugs found
		// "+parentNode.getConnectionPlugs().size() +
		// " Parent Node: "+parentNode.getClass().getName()+" Child Node:
		// "+childNode.getClass().getName());

		// find direct stuff
		ConnectorNode node = null;
		for (int i = 0; i < parentNode.getConnectionPlugs().size(); i++) {
			// System.out.println("ConnectionPane: Connected Shape
			// "+parentNode.getConnectionPlugs().get(i).getConnectedShape());
			if (parentNode.getConnectionPlugs().get(i).getConnectedShape() != null) {

				if (parentNode.getConnectionPlugs().get(i).getConnectedShape().getConnectionNode() == childNode) {
					node = parentNode.getConnectionPlugs().get(i);
				}
				// if bypassing connection structures then search whether a connection strcuture
				// is connected toa child
				else if (bypassStructure && parentNode instanceof ConnectionStructure) {
					// go down another level
					System.out.println("getConnectionPlug: Yep, the plug is defo connected to a structure: "); 
					node = getConnectionPlug(parentNode,
							parentNode.getConnectionPlugs().get(i).getConnectedShape().getConnectionNode(),
							includeBranch, bypassStructure);
					System.out.println("getConnectionPlug: node " + node); 

				}
				if (node != null)
					return node;
			}
		}

		// if no direct connection found and branch sockets can be looked for then now
		// look for branch sockets
		if (includeBranch) {
			StandardConnectionSocket socket;
			for (int i = 0; i < parentNode.getConnectionPlugs().size(); i++) {
				socket = (StandardConnectionSocket) parentNode.getConnectionPlugs().get(i).getConnectedShape();
				if (socket == null || !socket.isBranch())
					continue;
				// is this socket connected to child node?
				if (socket.getParentConnectionPlug().getConnectedShape() != null) {

					if (socket.getParentConnectionPlug().getConnectedShape().getConnectionNode() == childNode) {
						// return socket.getParentConnectionPlug();
						node = socket.getConnectedShape();
					} 
					else if (bypassStructure && socket.getParentConnectionPlug().getConnectedShape()
							.getConnectionNode() instanceof ConnectionStructure) {
						// go down another level
						node = getConnectionPlug(parentNode,
								socket.getParentConnectionPlug().getConnectedShape().getConnectionNode(), includeBranch,
								bypassStructure);
					}
				}
				if (node != null)
					return node;

			}
		}

		// long time2=System.currentTimeMillis();

		// System.out.println("ConnectionPane.getConnectionPlug(...) " + (time2-time1));
		// System.out.println("ConnectionPane: Returned plug: null" );
		return null;
	}

	/**
	 * Get the current scale transform.
	 * 
	 * @return the current scale transfrom.
	 */
	protected Scale getScaleTransform() {
		return scaleTransform;
	}

	/**
	 * Zoom into the ConnectionPane by a certain percentage
	 * 
	 * @param percent Percentage to zoom e.g. 0.1= 10%
	 */
	public void zoomIn(double percent) {
		scaleTransform.setX(scaleTransform.getX() * (1 + percent));
		scaleTransform.setY(scaleTransform.getY() * (1 + percent));
	}

	/**
	 * Zoom out of the pane by a certain percentage.
	 * 
	 * @param percent Percentage to zoom e.g. 0.1= 10%
	 */
	public void zoomOut(double percent) {
		scaleTransform.setX(scaleTransform.getX() * (1 - percent));
		scaleTransform.setY(scaleTransform.getY() * (1 - percent));
	}

	/**
	 * Get the current zoom factor
	 * 
	 * @return the current zoom factor
	 */
	public double getZoomFactor() {
		/// x and y are always the same
		return scaleTransform.getX();
	}

	/**
	 * Called whenever a connection undergoes a behaviour that other connection
	 * nodes to know about.
	 * 
	 * @param flag - flag to be passed to other connection nodes.
	 */
	public void notifyChange(int flag) {
		notifyChange(flag, null);
	}

	/**
	 * Called whenever a connection undergoes a behaviour that other connection
	 * nodes to know about.
	 * 
	 * @parma connectionNode - connection node associated with the notify flag.
	 * @param flag - flag to be passed to other connection nodes.
	 */
	public void notifyChange(int flag, StandardConnectionNode connectionNode) {
		// System.out.println("Hello connection pane notification: " +
		// getConnectionNodes().size());
		ArrayList<ConnectionNode> connectionsNodes = getConnectionNodes(true);
		for (int i = 0; i < connectionsNodes.size(); i++) {
			connectionsNodes.get(i).notifyChange(flag, connectionNode);
		}
	}

	/**
	 * Disconnect a node programtically.
	 * 
	 * @param parentNode - the parent node to disconnect
	 * @return the plug that was removed
	 */
	public static ConnectorNode disconnectNodes(ConnectionNode childNode, ConnectionNode parentNode) {
		// System.out.println("Standard Connection Node: DISCONNECT NODE: ");

		// first find the plug which connects to this node.
		ConnectorNode plug = ConnectionPane.getConnectionPlug(childNode, parentNode, true, true);
		if (plug == null) {
			// System.out.println("DISCONNECT NODE: plug is null");
			return plug; // no plug so not connected to this parent node anyway.
		}

		// set socket connection status to no connection
		if (plug.getConnectedShape() != null) {
			// System.out.println("DISCONNECT NODE: Connection Socket branch"
			// +((ConnectionSocket) plug.getConnectedShape()).isBranch());
			plug.getConnectedShape().setConnectionStatus(ConnectorNode.NO_CONNECTION, null);
		}

		// set plug connection status to no connection.
		plug.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);

		// /**
		// * If the plug is dragged then the connection with the last connection shape
		// is set to null
		// * Therefore when disconnecting through code the last connection shape must be
		// set to null otherwise
		// * when plug is dragged a NO_CONNECTION will be sent to the socket.
		// */
		// plug.lastConnectionShape=null;

		return plug;

	}

}
