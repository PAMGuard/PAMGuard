package pamViewFX.fxNodes.connectionPane.structures;

import java.util.ArrayList;

import pamViewFX.fxNodes.connectionPane.ConnectionNode;

/**
 * A ConnectionNode which holds other connection nodes. 
 * @author Jamie Macaulay
 *
 */
public interface ConnectionSubGroup {
	
	/**
	 * Get a list of the sub nodes within the node.
	 * @param includeStructures - true to also return connection structures
	 * @return a list of connection nodes.
	 */
	public ArrayList<ConnectionNode> getConnectionSubNodes(boolean includeStructures);

}
