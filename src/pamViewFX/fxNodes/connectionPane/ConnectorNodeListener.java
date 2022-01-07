package pamViewFX.fxNodes.connectionPane;

/**
 * Listener for node connector events such as dragging, being detected etc. 
 * 
 * @author Jamie Macaulay 
 *
 */
public interface ConnectorNodeListener {
	
	/**
	 * Called whenever a node connector event occurs
	 * @param shape - the node connector. 
	 * @param type -  type of event as defined in {@link ConnectorNode}
	 */
	public void nodeConnectorEvent(ConnectorNode shape,  int type);

}
