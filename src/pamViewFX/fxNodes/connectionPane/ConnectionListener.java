package pamViewFX.fxNodes.connectionPane;

/**
 * Listener for connection events with ConnectionShapes within a ConnectionNode. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface ConnectionListener {

	/**
	 * Called whenever a connection is made, could possibly happen, or been broken
	 * @param shape - shape which has connection
	 * @param shape - shape with which connection has occurred- null if no shape has been found/ connection has been broken. 
	 * @param type -  type of connection as defined in {@link ConnectorNode}
	 */
	public void collisionEvent(ConnectorNode shape, ConnectorNode foundShape,  int type);
}
