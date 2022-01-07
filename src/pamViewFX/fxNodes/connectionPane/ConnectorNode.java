package pamViewFX.fxNodes.connectionPane;

import javafx.geometry.Orientation;
import javafx.scene.shape.Shape;

/**
 * Interface for any interactive shape in a {@code ConnectionPane}. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface ConnectorNode {

	/**
	 * No connection to any other shapes. 
	 */
	public final static int NO_CONNECTION=0; 
	
	/**
	 * A possible connection to other shapes. 
	 */
	public final static int POSSIBLE_CONNECTION=1; 

	/**
	 * The shape is connected to another shape. 
	 */
	public final static int CONNECTED=2; 
	
	/**
	 * The shape is disabled and therefore no connections can be made. 
	 */
	public final static int CONNECTION_DISABLED=3;
	
	/**General connector events**/
	
	/**
	 * No connection to any other shapes. 
	 */
	public final static int CONNECTOR_MOVED=-1; 
	

	
	/**
	 * Get the distance form the CollisionShape to another shape. Usually this is just the centre point of the shape to the other shape centre, 
	 * however in some cases might be slightly different e.g. for a line you want the closest point on the line to a shape rather than just the centre of the line. 
	 * @param shape - shape to get distance to. 
	 * @return the distance in pixels from the CollisionShape to the shape. 
	 */
	public double getDistance(Shape shape);
		
	/**
	 * Get the Shape associated with the Collision shape. 
	 * @return the shape 
	 */
	public Shape getShape();

	/**
	 * Set the connection status.
	 * @param type- type of connection status, 
	 * @param shape - the connected or possibly connected shape. Can be null if NO_CONNECTION FLAG. 
	 */
	void setConnectionStatus(int type, ConnectorNode shape); 
	
	/**
	 * Check if the shape is currently connected to another shape. 
	 * @return true if the shape is connected to another shape. 
	 */
	public int getConnectionStatus();

	/**
	 * Get the orientation of the shape	.
	 * @return orientation of the shape. 
	 */
	public Orientation getOrientation();

	/**
	 * Set the connected shape- the shape which this shape is connected to. null if no shape is connected; 
	 * @param connecionShape - the connected shape. Can be null. 
	 */
	public void setConnectedShape(ConnectorNode connecionShape);
	
	/**
	 * Get the connected shape- the shape this shape is connected to. null if shape is not connected to anything; 
	 * @param connecionShape - the connected shape. Can be null.  
	 */
	public ConnectorNode getConnectedShape();
	
	/**
	 * Get the connection node the shape is associated with; 
	 * @return the connection node the shape is associated with; 
	 */
	public ConnectionNode getConnectionNode();
	
	/**
	 * Check whether a connection has 
	 * @param notify
	 * @return
	 */
	public boolean checkPossibleConnection(boolean notify);
	
	/**
	 * Check whether there is an error in the connection
	 * @return true if error in connection.
	 */
	public boolean isError();
	
	/**
	 * Set the connection to have an error flag. 
	 * @param error - true if error in connection
	 */
	public void setError(boolean error);
		
	

	
//	/**
//	 * List of ConnectionListeners currently associated with the ConnectionShape. 
//	 * @return a list of listeners associated with the ConnectionShape. 
//	 */
//	public ArrayList<ConnectionListener> getConnectionListeners();
//	
//	/**
//	 * Add a connection listener to the ConnectionShape. 
//	 * @param listener the listenr to add. 
//	 */
//	public void addConnectionListener(ConnectionListener listener);
//	
//	/**
//	 * Remove a connection listener. 
//	 * @param listener - the connection listener to remove. 
//	 * @return  true if listener is removed. False if no listener exists. 
//	 */
//	public boolean removeListener(ConnectionListener listener);

}
