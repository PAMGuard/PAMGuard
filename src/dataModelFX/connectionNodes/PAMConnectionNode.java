package dataModelFX.connectionNodes;

import dataModelFX.ConnectionNodeParams;

/**
 * 
 * Any connection node node used in the PAMGuard data model must satisfy this interface. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface PAMConnectionNode {
	
	/**
	 * Settings to save. 
	 * @return the used structure information
	 */
	public ConnectionNodeParams getConnectionNodeParams(); 
	
	/**
	 * Set the used structure info. 
	 * @return the used structure info. 
	 */
	public void setConnectionNodeParams(ConnectionNodeParams usedStructInfo); 
	
	
	/**
	 * Called whenever settings are first loaded in PAMGuard.
	 */
	public void loadsettings(); 
	
	/**
	 * Update parameters the paramters to from current node position etc. 
	 */
	public void updateParams(); 
}
