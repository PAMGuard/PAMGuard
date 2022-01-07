package dataModelFX.structures;

import java.io.Serializable;
import java.util.UUID;

import dataModelFX.ConnectionNodeParams;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;

/**
 * Contains saveable information for connection structures. 
 * 
 * @author Jamie Macaulay 
 *
 */
public abstract class StructureParams implements ConnectionNodeParams, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The X position of the node in it's connection pane.
	 */
	public double layoutX;
	
	/**
	 * The Y position of the node in it's connection pane. 
	 */
	public double layoutY;
	
	public UUID id; 
	
	
	public StructureParams(ConnectionNode connectionStructure) {
		this.layoutX = connectionStructure.getConnectionNodeBody().getLayoutX(); 
		this.layoutY = connectionStructure.getConnectionNodeBody().getLayoutY(); 
		this.id = UUID.randomUUID(); 

	}

	@Override
	public double getLayoutX() {
		return layoutX;
	}

	@Override
	public double getLayoutY() {
		return layoutY;
	}
	
	@Override
	public UUID getID() {
		return id; 
	}
	
	

}
