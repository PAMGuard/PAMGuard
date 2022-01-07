package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;

import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * Basic implementation of ConnectionPlugShape
 * 
 * @author Jamie Macaulay 
 *
 */
abstract class AbstractConnector extends Polygon implements ConnectorNode {

	/**
	 * Normal colour of polygon. 
	 */
	private Color normalColour=Color.LIMEGREEN;

	/**
	 * Colour when polygon is highlighted.
	 */
	private Color highLightColour=Color.CHARTREUSE;
	
	/**
	 * Colour when plug is disabled. 
	 */
	private Color disableColour=new Color(0.2,0.2,0.2,0.2);

	/**
	 * Reference to the C onnectionPane this shape belongs to. 
	 */
	private StandardConnectionNode connectionNode;

	/**
	 * The minimum distance in pixels a shape has to be before a connection is possible. 
	 */
	private double min_dist=75.;
	
	/**
	 * Current status of the shape. 
	 */
	private int currentConnectionStatus=NO_CONNECTION;

	/**
	 * Orientation of the shape
	 */
	private Orientation orientation; 
	
	/**
	 * The shape currently connected to this shape; null if no shape is connected. 
	 */
	private ConnectorNode connectedShape=null; 
	

	public AbstractConnector(StandardConnectionNode connectionNode, Orientation orientation){
		this.connectionNode=connectionNode; 
		this.orientation=orientation;
		
		setFill(getNormalColour());
		setStroke(getNormalColour().darker());

		//add listeners to layout x and y. Means that 
		layoutXProperty().addListener( (change)->{
			checkPossibleConnection(true);
		});
		
		layoutYProperty().addListener( (change)->{
			checkPossibleConnection(true);
		});

		setOnMouseReleased((event)->{
			checkConnection();
		});
		//Touch Actions
		//TODO; 
	}
	
	/**
	 * Get the connectionNode associated with the shape. 
	 * @return the ConnectionNode associated with the shape. 
	 */
	public StandardConnectionNode getConnectionNode() {
		return connectionNode;
	}


	public void setConnectionNode(StandardConnectionNode connectionNode) {
		this.connectionNode = connectionNode;
	}


	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * Check if the shape can connect to another shape and connect if so. 
	 */
	public void checkConnection(){
		ConnectorNode foundShape=checkForCollision(this, getPossibleConnectionShapes(), min_dist);
		//if (foundShape!=null) System.out.println(" currentConnectionStatus "+currentConnectionStatus+ "  foundShape.getConnectionStatus()" + foundShape.getConnectionStatus());  
		if (foundShape!=null && canShapeConnect(foundShape)) {
			//triggerConnectionListeners(foundShape,CONNECTED);
			this.setConnectionStatus(CONNECTED, foundShape);
			foundShape.setConnectionStatus(CONNECTED, this);
		}
	}
	
	/**
	 * Check whether a shape can connect and send appropriate flag to itself and the shape it can connect to. 
	 * @param send POSSIBLE_CONNECTION to shape and found shape if found. 
	 * @return true if a possible connection has been found. 
	 */
	public boolean checkPossibleConnection(boolean notify){
		boolean possiblConnection=false;
		ConnectorNode foundShape=checkForCollision(this, getPossibleConnectionShapes(), min_dist);
//		if (foundShape!=null) System.out.println(" can connect "+canShapeConnect( foundShape) 
//				+" connection status "+currentConnectionStatus+ "  foundShape.getConnectionStatus() " + foundShape.getConnectionStatus() +" found shape "+ foundShape);
		if (foundShape!=null &&  canShapeConnect(foundShape)) {
			//triggerConnectionListeners(foundShape,POSSIBLE_CONNECTION);
			possiblConnection=true;
			if (notify) {
				this.setConnectionStatus(POSSIBLE_CONNECTION, foundShape);
				foundShape.setConnectionStatus(POSSIBLE_CONNECTION, this);
			}
		}
		return possiblConnection; 
	}
	
	/**
	 * Check if either this shape is connected or disabled. 
	 * @param foundShape - the found shape
	 * @return true if this shape is ready to be connected. 
	 */
	private boolean canShapeConnect(ConnectorNode foundShape){
		return !(currentConnectionStatus==CONNECTED || foundShape.getConnectionStatus()==CONNECTED ||
				currentConnectionStatus==CONNECTION_DISABLED || foundShape.getConnectionStatus()==CONNECTION_DISABLED );
	}
	
//	@Override
//	public ArrayList<ConnectionListener> getConnectionListeners() {
//		return plugListeners;
//	}
//
//	@Override
//	public void addConnectionListener(ConnectionListener listener) {
//		plugListeners.add(listener);
//	}
//
//	@Override
//	public boolean removeListener(ConnectionListener listener) {
//		return plugListeners.remove(listener);
//	}

	@Override
	public double getDistance(Shape shape) {
//		//stay in scene reference because some nodes may not be direct children of the connection pane
		
		if (shape.getParent()==null) return Double.MAX_VALUE; 
			
//		//this is the socket - it seems to be in the frame of reference of the scene. 
//		System.out.println("Socket 1 layout: " + localToScene(this.getLayoutX(), this.getLayoutY()));
//		System.out.println("Socket 1 scene: " + this.getParent().localToScene(this.getLayoutX(), this.getLayoutY()));
//
//		//this is the plug
//		System.out.println("Point 2 layout: " + new Point2D(shape.getLayoutX(), shape.getLayoutY()));
//		System.out.println("Point 2 scene : " + shape.getParent().localToScene(shape.getLayoutX(), shape.getLayoutY()) + " parent: " + shape.getParent());
//
//		System.out.println("------"); 
//		
		
		//works but ore complicated
//		double distance  = this.getParent().localToScene(this.getLayoutX(), this.getLayoutY()).
//				distance(shape.getParent().localToScene(shape.getLayoutX(), shape.getLayoutY())); 
				
		double distance  = new Point2D(this.getLayoutX(), this.getLayoutY())
				.distance(shape.getLayoutX(), shape.getLayoutY());
		
//		System.out.println("Distance: " + this.getParent().localToScene(this.getLayoutX(), this.getLayoutY()).distance(shape.getParent().localToScene(shape.getLayoutX(), shape.getLayoutY()))); 
//		System.out.println("Distance: " + distance); 
				
		return distance; 

	}

	public Color getNormalColour() {
		return normalColour;
	}

	public void setNormalColour(Color normalColour) {
		this.normalColour = normalColour;
	}

	public Color getHighLightColour() {
		return highLightColour;
	}

	public void setHighLightColour(Color highLightColour) {
		this.highLightColour = highLightColour;
	}
	
	public Color getDisableColour() {
		return disableColour;
	}

	public void setDisableColour(Color disableColour) {
		this.disableColour = disableColour;
	}

//	public void triggerConnectionListeners(ConnectionShape shape, int type){
//		//iterate through all lines and check for collisions;
//		if (shape!=null){
//			for (int i=0; i<shape.getConnectionListeners().size(); i++){
//				shape.getConnectionListeners().get(i).collisionEvent(this, type);
//			}
//		}
//	}

	/**
	 * Get a list of connection shapes which might connect with this shape. 
	 * @return a list of ConnectionShapes in a ConnectionPane which can connect with the AbstractConnectionShape;
	 */
	public abstract ArrayList<ConnectorNode> getPossibleConnectionShapes(); 


	@Override
	public Shape getShape() {
		return this;
	}
	
	@Override
	public int getConnectionStatus() {
		return currentConnectionStatus;
	}

	@Override
	public void setConnectionStatus(int type, ConnectorNode foundShape) {
		//System.out.println("SET_CONNECTION_STATUS: ConnectionSocket: type "+ type+ " foundShape "+foundShape);
		this.currentConnectionStatus=type; 
		this.setConnectedShape(null);
		switch (type){
		case NO_CONNECTION:
			setFill(getNormalColour());
			setStroke(getNormalColour().darker());
			break; 
		case POSSIBLE_CONNECTION:
			setFill(getHighLightColour());
			setStroke(getHighLightColour().darker());
			break;
		case CONNECTED:
			this.setConnectedShape(foundShape);
			setFill(getNormalColour());
			setStroke(getNormalColour().darker());
			break;
		case CONNECTION_DISABLED:
			setFill(getDisableColour()); 
			setStroke(Color.TRANSPARENT); 
			break; 
		}

		//need to trigger connection listener
		this.getConnectionNode().triggerConnectionListeners(this, foundShape, type); 
		
		/**
		 * FIXME -need to also trigger listener of possible connecting node. This is because
		 * the other node may require to be notified in order. e.g. may need to be notified after 
		 * connection status has been set on this shapes connection node. Not an elegent fix as 
		 * means more function calls but works. 
		 */
		if (foundShape!=null) foundShape.getConnectionNode().notifyConnectionListeners(this, foundShape, type);
	}

	
	/**
	 * Check whether a collision with another shape has occurred. 
	 * @return the shape a collision has occurred with. If no collision has occurred return null; 
	 */
	public ConnectorNode checkForCollision(ConnectorNode shape, ArrayList<? extends ConnectorNode> nodes, double min_dist){
		int n=-1;
		double minDist=Double.MAX_VALUE;
		double dist = 0; 
		for (int i=0; i<nodes.size(); i++){
			dist=nodes.get(i).getDistance(shape.getShape());
			if (dist<minDist){
				minDist=dist;
				n=i;
			}
			//old method. 
			//			Shape intersect = Shape.intersect(shape, nodes.get(i));
			//			if (intersect.getBoundsInLocal().getWidth() != -1) {
			//				n = i;
			//			}
		}

//		System.out.println("minDist "+minDist + " Node ID: " + connectionNode.getNodeId());

		if (minDist>min_dist){
//		if (minDist>min_dist || connectionNode.getNodeId() <0) {
			/**
			 * Need to check connectionNode.getNodeId() <0 to prevent nodes being partly connected when added to pane. 
			 * Haven't quite got to the bottom of why this happens. 
			 */
			n=-1;
		}
////		///TEMP/////
//		if (n>=0){
//			System.out.println(" checkForCollision.Number of nodes "+nodes.size());
//
//			if (nodes.get(n) instanceof StandardConnectionSocket){
//				System.out.println("Found a socket");
//			}
//			if (nodes.get(n) instanceof StandardConnectionSocket){
//				System.out.println("Found a plug "+ nodes.get(n) .getShape().getLayoutX()+ "  "+nodes.get(n) .getShape().getLayoutY() +" node "+connectionNode.getNodeId() );
//
//			}
//			if (nodes.get(n) instanceof ConnectionLine){
//				System.out.println("Found a line");
//
//			}
//			if (shape instanceof StandardConnectionSocket){
//				
//				System.out.println("from a socket " + ((StandardConnectionSocket) shape).isBranch() + "  "+shape.getShape().getLayoutX()+ " "+shape.getShape().getLayoutY()+" node "+connectionNode.getNodeId());
//				System.out.println(connectionNode.getConnectionPane().toString());
//			}
//			if (shape instanceof StandardConnectionSocket){
//				System.out.println("from  a plug");
//
//			}
//			if (shape instanceof ConnectionLine){
//				System.out.println("from a line");
//			}
//			
//		}
////		////////////
		return (n>=0) ? nodes.get(n): null;
	}
	
	@Override
	public void setConnectedShape(ConnectorNode connectionShape){
		this.connectedShape=connectionShape; 
	}
	
	@Override
	public ConnectorNode getConnectedShape( ){
		return this.connectedShape; 
	}
	
	public double getMinDist() {
		return min_dist;
	}


}
