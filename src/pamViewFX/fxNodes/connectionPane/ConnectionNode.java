package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;

import javafx.scene.Group;
import javafx.scene.Node;


/**
 * A connection node is a node that has a parent and children
 * 
 * @author Jamie Macaulay
 *
 */
public interface ConnectionNode {

	/**
	 * Get the parents of this connection node
	 * @return the parents of this connection node. 
	 */
	public ArrayList<? extends ConnectionNode> getParentConnectionNodes(); 

	/**
	 * Get children of the connection node
	 * @return a list of the children connected to to the node. 
	 */
	public ArrayList<? extends ConnectionNode> getChildConnectionNodes();

	/**
	 * Get connection sockets for a node. Connection sockets are connections that accept a parent node. 
	 * @return a list of the connection sockets. 
	 */
	public ArrayList<? extends ConnectorNode> getConnectionSockets(); 


	/**
	 * Get connection plugs. Connection plugs are connections that can be connected to a child. 
	 * @return a list of the connection plugs. 
	 */
	public ArrayList<? extends ConnectorNode> getConnectionPlugs();

	/**
	 * Get the lines that connect plugs and the connection node.
	 * @return all lines connecting plugs and connection nodes. 
	 */
	public ArrayList<? extends ConnectionLine> getPlugConnectionLines();

	/**
	 * Get any branch sockets- these are inputs connection to the primary socket. 
	 * @return branch sockets
	 */
	public ArrayList<? extends ConnectorNode> getBranchConnectionSockets();

	/**
	 * Get the main connection body. This is the node that can be dragged etc. 
	 * @return the main connection node body. 
	 */
	public Node getConnectionNodeBody(); 
	
	/**
	 * Get the group that holds all components of the ConnectionNode e.g. the body, plugs etc.
	 * @return the connection node group. 
	 */
	public Group getConnectionGroup(); 


	/**
	 * Notify the connection node of a general change. 
	 * @param flag - the change flag.
	 * @param connectionNode - the connection node 
	 */
	public void notifyChange(int flag, StandardConnectionNode connectionNode);

	/***Connector Node Listeners***/

	/**
	 * Add a connector node listener. This listens for any events from connector nodes 
	 * attached to the connection node. 
	 * @param connectorNodeListener - the connector node listener to add. 
	 */
	public void addConnectorNodeListener(ConnectorNodeListener connectorNodeListenr);

	/**
	 * Remove a connector node listener. This listens for any events from connector nodes 
	 * attached to the connection node. 
	 * @param connectorNodeListener - the connector node listener to remove. 
	 */
	public void removeConnectorNodeListener(ConnectorNodeListener connectorNodeListenr);


	/***Connection Listeners***/

	/**
	 * Notify all connection listener in the node. 
	 * @param connectedShape
	 * @param plugShape
	 * @param connected
	 */
	public void notifyConnectionListeners(ConnectorNode connectedShape, ConnectorNode plugShape,
			int connected);

	/**
	 * Add a connection listener. This listens for other connection nodes which have connected
	 * @param connectListener - the connection listener to add. 
	 */
	public void addConnectionListener(ConnectionListener connectListener);

	/**
	 * Remove a connection listeners. This listens for other connection nodes which have connected
	 * @param connectListener - the connection listener to remove. 
	 */
	public void removeConnectionListener(ConnectionListener connectListener);

}
