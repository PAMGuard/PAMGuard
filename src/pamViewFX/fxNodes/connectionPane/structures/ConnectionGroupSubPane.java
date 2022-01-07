package pamViewFX.fxNodes.connectionPane.structures;

import java.util.ArrayList;

import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.ConnectorNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;

/**
 * A sub connection pane with a single plug. 
 *  
 * @author Jamie Macaulay 
 *
 */
public class ConnectionGroupSubPane extends ConnectionPane {

	/**
	 * The size of the rectangle. 
	 */
	public static double RECTANGLE_SIZE= 200;  


	/**
	 * Reference to the connection group structure that holds. 
	 */
	private ConnectionGroupStructure connectionGroupStructure;


	public ConnectionGroupSubPane(ConnectionGroupStructure connectionGroupStructure) {
		super();
		this.connectionGroupStructure=connectionGroupStructure; 
	}
	
	
	@Override
	public void notifyChange(int flag, StandardConnectionNode connectionNode) {
		super.notifyChange(flag, connectionNode);
		connectionGroupStructure.notifySubChange(flag, connectionNode);
	}
	
	
	
	/**
	 * Get all the connection plugs from all the ConnectionNodes in a ConnectionPane. 
	 * @param connectionNode  - ConnectionNode to be excluded
	 * @return a list of plugs from all non excluded ConnectionNodes. 
	 */
	public ArrayList<ConnectorNode> getConnectionPlugs(StandardConnectionNode connectionNode) {
		ArrayList<ConnectorNode> connectionPlugs = super.getConnectionPlugs(connectionNode);
		//now add the plug from the group rectangle. 
		//must add the connection plugs from the recatngle. 
		connectionPlugs.addAll(connectionGroupStructure.getConnectionPlugs());
		return connectionPlugs; 
	}

}
