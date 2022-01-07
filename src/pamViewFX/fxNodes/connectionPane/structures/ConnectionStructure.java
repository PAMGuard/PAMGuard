package pamViewFX.fxNodes.connectionPane.structures;

import javafx.scene.control.Tooltip;

/**
 * A connection structure is a connection node which is a utility rather than a node. e.g. 
 * for grouping different nodes. 
 * 
 * @author Jamie Macaulay
 */
public interface ConnectionStructure {
	
	/**
	 * Connection structure types. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	public enum ConnectionStructureType {GroupConnection, ExtensionSocket}; 
	
	/**
	 * Get the tool describing what the structure does. 
	 * @return get the tool tip for the 
	 */
	public Tooltip getToolTip();
	
	/**
	 * Get the connection structure type. 
	 * @return the strcuture type. 
	 */
	public ConnectionStructureType getStructureType(); 
	
	

}
