package dataModelFX.structures;

import java.util.UUID;

import pamViewFX.fxNodes.connectionPane.ConnectionNode;

/**
 * Parameters for the group structure. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamGroupStructureParams extends PamStructureParams {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The nodes within the group pane. 
	 */
	public UUID[] subNodeIDs; 
	

	/**
	 * The user defined name of the group structure. 
	 */
	public String name = "Module Group";

	
	public PamGroupStructureParams(ConnectionNode connectionStructure) {
		super(connectionStructure);
	}

	@Override
	public PAMConnectionNodeType getNodeType() {
		return PAMConnectionNodeType.PAMGroupStructure;
	}


}
