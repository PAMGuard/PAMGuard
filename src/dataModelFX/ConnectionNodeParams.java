package dataModelFX;

import java.io.Serializable;
import java.util.UUID;

/**
 * Information on structures used in the data model. This is saved by PAMGuard
 * and then used to remake the data model with all nodes in the same position
 * etc.
 * 
 * @author Jamie Macaulay
 *
 */
public interface ConnectionNodeParams extends Serializable {

	public enum PAMConnectionNodeType {
		ModuleConnectionNode, PAMGroupStructure, PAMExtensionStructure
	}

	/**
	 * Get the node type. This indicates what type of conneciton node the parameter
	 * belong to.
	 * 
	 * @return the connection node type.
	 */
	public PAMConnectionNodeType getNodeType();

	/**
	 * The x location of the module.
	 */
	public double getLayoutX();

	/**
	 * The y location of the module.
	 */
	public double getLayoutY();

	/**
	 * Get the unique ID for the node. This should eb the same after the node has
	 * been saved.
	 * 
	 * @return the unique ID number.
	 */
	public UUID getID();

}
