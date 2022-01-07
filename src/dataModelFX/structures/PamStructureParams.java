package dataModelFX.structures;

import java.util.UUID;

import pamViewFX.fxNodes.connectionPane.ConnectionNode;

/**
 * Parameters for the extension structure. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamStructureParams extends StructureParams {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The ID of the child nodes. Note there can be a sub node which is 
	 * not connected to the group structure and thus is not a direct child node. 
	 */
	public UUID[]childNodes; 
	
	
	/**
	 * The ID of the parent structure the extension node was connected to. 
	 */
	public UUID[] parentNodes; 
	
	public PamStructureParams(ConnectionNode connectionStructure) {
		super(connectionStructure);
	}

	@Override
	public PAMConnectionNodeType getNodeType() {
		return PAMConnectionNodeType.PAMExtensionStructure;
	}

}
