package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 * Line which connects a plug or socket to a ConnectionRectangle or other ConnectionLine.
 * @author Jamie Macaulay
 *
 */
public class ConnectionLine extends Line implements ConnectorNode {

	/**
	 * Normal colour of polygon. 
	 */
	private Color normalColour=Color.DODGERBLUE;

	/**
	 * Colour when polygon is highlighted
	 */
	private Color highLightColour=Color.CYAN;

	/**
	 * Color of line when error in connection occurs. 
	 */
	private Color errorColor=Color.RED;

	/**
	 * Boolean to indicate whether a connection line can connect to other shapes i.e. should register a collision of another shape comes near. 
	 */
	private int connectionState=0; 

	/**
	 * The plug connection has some sort of error. 
	 */
	boolean isError=false;

	/**
	 * A temporary socket which shows when a plug is nearby. 
	 */
	private StandardConnectionSocket tempSocket=null;

	/**
	 * Allow branch sockets to connect to connection lines.
	 */
	private boolean allowBranchSockets=false;

	/**
	 * Each line connects the node to a connection shape (the parentConnectionShape); 
	 */
	private ConnectorNode parentConnectionShape;

	/**
	 * Refrence to the connection node. 
	 */
	private StandardConnectionNode connectionNode;

	
	public ConnectionLine(StandardConnectionNode connectionNode, ConnectorNode connectionShape){		
		this.parentConnectionShape=connectionShape; 
		this.connectionNode=connectionNode; 
		setLineNormalAppearance();
		//by default lines are disabled i.e. plugs cannot connect to them.  
		setConnectionStatus(CONNECTION_DISABLED, null);
		//set connection line branch sockets to whatever setting for whole node is. 
		allowBranchSockets=connectionNode.isAllowBranchSockets();
	}

	/**
	 * Set the line to show error appearance. 
	 */
	void setLineErrorAppearance() {
		setStroke(errorColor);
		setStrokeWidth(3);
		getStrokeDashArray().addAll(3.0,7.0,3.0,7.0);
	}

	/**
	 * Set the line to show error appearance. 
	 */
	private void setLineNormalAppearance() {
		setStroke(normalColour);
		setStrokeWidth(3);
		getStrokeDashArray().removeAll(getStrokeDashArray());
	}

	/**
	 * Set the colour of the line
	 * @param color - colour of the line
	 */
	public void setNormalColor(Color color){
		this.normalColour=color; 
		setStroke(normalColour);
	}

	/**
	 * Set the highlight appearance of the line. 
	 */
	@SuppressWarnings("unused")
	private void setHighlightAppearance(){
		setLineNormalAppearance();
		this.setStroke(this.highLightColour);
	}

	@Override
	public double getDistance(Shape shape) {
		//if branch sockets are not allowed then just make this line 'unconnectable'. 
		if (!(allowBranchSockets && checkBranchSocketParent((ConnectorNode) shape))) return Double.MAX_VALUE; 
		//otherwise figure out distance to shape. 
		Point2D closestPoint=ConnectionNodeUtils.findClosestPoint(new Point2D(this.getStartX(), this.getStartY()), new Point2D(this.getEndX(), this.getEndY()), new Point2D(shape.getLayoutX(), shape.getLayoutY()));
		double thisdist=closestPoint.distance(new Point2D(shape.getLayoutX(), shape.getLayoutY()));
		//			this.setFill(normalColour);
		//			this.setStroke(normalColour);
		return thisdist;
	}

	@Override
	public Shape getShape() {
		return this;
	}

	/**
	 * Check whether a connection line can connect to another ConnectionShape or not; i.e. will register a collision of another shape comes near. 
	 * @return flag for connection type. 
	 */
	public int getConnectionStatus() {
		return connectionState;
	}

	@Override
	public void setConnectionStatus(int type, ConnectorNode connectionShape) {
		//System.out.println("ConnectionLine: "+type);
		//			//lines have no functionality if branch sockets are disabled.  
		//			if (!isAllowBranchSockets()) return;

		//we never want the lines current state flag to say it is connected as by default this stops other shapes from connecting
		//therefore if the type is CONNECTION change to no connection. This mean multiple sockets can attach to the line. 
		this.connectionState = (type==CONNECTED ? NO_CONNECTION : type); 
		switch (type){
		case NO_CONNECTION:
			if (allowBranchSockets){
				//remove any temporary socket
				removeTempSocket();
			}
			//set line back to normal colour
			break;
		case POSSIBLE_CONNECTION:
			if (allowBranchSockets && checkBranchSocketParent(connectionShape)){
				//set colour of line to highlighted
				removeTempSocket();
				//add a temp socket connection to show that a plug can connect. 
				if (tempSocket==null) addTempSocket(connectionShape);
			}
			break;
		case CONNECTED:
			if (allowBranchSockets && checkBranchSocketParent(connectionShape)){
				//setLineNormalAppearance();
				//remove temporary socket
				removeTempSocket();
				//add new static socket with plug bound to it. 
				addBranchNewSocket(connectionShape); 
			}
			break;
		}
		if (isError) setLineErrorAppearance();
	}

	/**
	 * Check whether the current connection shape has a parent ConnectionNode whihc already has a branch plug on the line. 
	 * A parent node cannot have two branch connections. 
	 * @param connectionShape - shape attempting to connect via branch socket.
	 * @return true if the parent ConnectionNodde of the shape is not already connected to the line. 
	 */
	public boolean checkBranchSocketParent(ConnectorNode connectionShape){
		ConnectionNode parentNode=connectionShape.getConnectionNode(); 
		for (int i=0; i<connectionNode.getBranchConnectionSockets().size(); i++){
			if (connectionNode.getBranchConnectionSockets().get(i).getConnectedShape()!=null && 
					connectionNode.getBranchConnectionSockets().get(i).getConnectedShape().getConnectionNode()==parentNode){
				return false;
			}
		}
		return true; 
	}

	/**
	 * Remove any temporary socket from the line. 
	 */
	public void removeTempSocket(){
		if (tempSocket!=null){
			//need to remove binding in order to prevent weird effects with plugs still thinking they have removed sockets attached. 
			tempSocket.layoutXProperty().unbind(); 
			tempSocket.layoutYProperty().unbind();

			boolean remove=connectionNode.getChildren().remove(tempSocket); 
			connectionNode.getChildren().removeAll(tempSocket.getSocketConnectionLines());
			if (!remove){
				System.err.println("ConnectionNode. Temp socket was not removed!");
			}
		}
		tempSocket=null;
	}

	/**
	 * Add a temporary socket to the line. 
	 */
	public void addTempSocket(ConnectorNode plugShape){
		tempSocket=createTempSocket(plugShape);
		connectionNode.getChildren().add(tempSocket);
		connectionNode.getChildren().addAll(tempSocket.getSocketConnectionLines());
	}

	/**
	 * Add a new socket to the line. 
	 * @param plugShape - plug to connect this new socket to. 
	 */
	public void addBranchNewSocket(ConnectorNode plugShape){
		StandardConnectionSocket newSocket=createBranchSocket(plugShape);
		connectionNode.getChildren().add(newSocket);
		connectionNode.getChildren().addAll(newSocket.getSocketConnectionLines());
		//			
		//set plug to connected
		plugShape.setConnectionStatus(CONNECTED, newSocket);
		plugShape.setConnectedShape(newSocket);

		//need to set the flag of socket to CONNECTION and set correct shape. 
		newSocket.setConnectionStatus(CONNECTED, plugShape);
		newSocket.setConnectedShape(plugShape); //- this causes stack overflow error?
		//			
		//			/**
		//			 * A branch sockets has been connected to this line. Now things get a bit complicated. The plug connecting 
		//			 * to the branch socket is really connected to the socket that this line connects to i.e. the plug ius not
		//			 * an input for this ConnectionNode but the node it connects to. 
		//			 */
		triggerBranchConnectionListener(plugShape);

	}			


	/**
	 * Called whenever branch sockets have been connected to this line. Now things get a bit complicated. The plug connecting 
	 * to the branch socket is really connected to the socket that this line connects to i.e. the plug in not
	 * an input for this ConnectionNode but the node it connects to. 
	 */
	private void triggerBranchConnectionListener(ConnectorNode plugShape){
		//TODO only tested for plug shapes 
		if (this.parentConnectionShape instanceof StandardConnectionPlug){
			StandardConnectionPlug connectionPlug = (StandardConnectionPlug) parentConnectionShape;
			if (connectionPlug.getConnectedShape()!=null){
				//trigger listener between plug which connected to line and socket this parent plug is connected to.
				connectionPlug.getConnectedShape().getConnectionNode().notifyConnectionListeners(connectionPlug.getConnectedShape(),
						plugShape, ConnectorNode.CONNECTED);
			}
		}
		else {
			connectionNode.triggerConnectionListeners(plugShape, null, ConnectorNode.CONNECTED);
		}

	}


	/**
	 * Create a new socket to connect to the line. The new socket belongs to the node the line connects to
	 * @param plugShape - the plug which connects to the socket
	 * @return a new branch ConnectionSocket.
	 */
	private StandardConnectionSocket createBranchSocket(ConnectorNode plugShape){
		
		System.out.println("Connection Line create branch: " + this.connectionNode); 
		
		//create new socket
		StandardConnectionSocket newSocket=new StandardConnectionSocket(connectionNode, StandardConnectionNode.plugBodyWidth-5, StandardConnectionNode.plugEndWidth, 
				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight, StandardConnectionNode.cornerRadius, plugShape.getOrientation(), this);
		
		newSocket.setBranch(true);
		newSocket.setLayoutX(plugShape.getShape().getLayoutX());
		newSocket.setLayoutY(plugShape.getShape().getLayoutY());

		//create line for the socket.
		ArrayList<ConnectionLine> newLines=new ArrayList<ConnectionLine>();

		//create new lines for the socket
		//check if the parent line is vertical
		final boolean vertical=(getStartX()-getEndX())==0;

		//find closest point of plug to line. 
		Point2D closestPoint=ConnectionNodeUtils.findClosestPoint(new Point2D(this.getStartX(), this.getStartY()), new Point2D(this.getEndX(), this.getEndY()), 
				new Point2D(plugShape.getShape().getLayoutX(), plugShape.getShape().getLayoutY()));


		final ConnectionLine newLine2=new ConnectionLine(connectionNode, newSocket);

		//how far along the line is the plug from start of line (remember line can be backwards).
		final double pixel_fraction;
		if (vertical) pixel_fraction=(closestPoint.getY()-Math.min(this.getStartY(), this.getEndY()))/Math.abs(this.getStartY()-this.getEndY()); 
		else pixel_fraction=(closestPoint.getX()-Math.min(this.getStartX(), this.getEndX()))/Math.abs(this.getStartX()-this.getEndX()); 

		//create low level binding to work out where the end of line connecting socket to this.line should be- even if
		//this.line length change. 
		DoubleBinding connectionPoint = new DoubleBinding(){
			{
				super.bind(ConnectionLine.this.startXProperty(), ConnectionLine.this.startYProperty(), 
						ConnectionLine.this.endXProperty(), ConnectionLine.this.endYProperty()); //initial bind
			}

			@Override
			protected double computeValue() {
				Point2D position=ConnectionNodeUtils.getLinePosition(ConnectionLine.this.getStartX(), ConnectionLine.this.getEndX(), 
						ConnectionLine.this.getStartY(), ConnectionLine.this.endYProperty().get(), pixel_fraction);
				if (vertical){
					return position.getY();
				}
				else return position.getX()+30+StandardConnectionNode.plugBodyWidth; 
			}
		};

		//System.out.println("pixels "+pixel_fraction + " vertical "+ vertical+" startX " +this.startXProperty().get() +" startY " +this.startYProperty().get());

		//high level bindings to set socket position
		newLine2.startXProperty().bind(vertical ? this.startXProperty() : connectionPoint);
		newLine2.startYProperty().bind(vertical ? connectionPoint : this.startYProperty());
		newLine2.endXProperty().bind(vertical ? newLine2.startXProperty().subtract(newLine2.getStartX()-newSocket.getLayoutX()-StandardConnectionNode.plugBodyWidth) : newLine2.startXProperty());
		newLine2.endYProperty().bind(vertical ? newLine2.startYProperty() : newLine2.startYProperty().add(newSocket.getLayoutY()-newLine2.getStartY()));
		newLines.add(newLine2);

		//need extra bit for horizontal. 
		if (!vertical){
			ConnectionLine newLine1=new ConnectionLine(connectionNode, newSocket);
			newLine1.startXProperty().bind(newLine2.endXProperty());
			newLine1.startYProperty().bind(newLine2.endYProperty());
			newLine1.endXProperty().bind(newLine1.startXProperty().subtract(30));
			newLine1.endYProperty().bind(newLine1.startYProperty());
			newLines.add(newLine1);
		}

		//high level binding to make sure socket stays with new branch line
		newSocket.layoutXProperty().bind(newLine2.endXProperty().subtract(StandardConnectionNode.plugBodyWidth+(vertical ? 0 : 30)));
		newSocket.layoutYProperty().bind(newLine2.endYProperty());
		newSocket.setSocketConnectionLines(newLines);

		//add socket to correct array of branch sockets
		connectionNode.getBranchConnectionSockets().add(newSocket);

		return newSocket;
	}

	/**
	 * Creates a temporary socket which connects to the line. 
	 * The temporary socket is grey and transparent and connects to the line.  
	 */
	private StandardConnectionSocket createTempSocket(ConnectorNode plugShape){

		//check if the line is vertical
		boolean vertical=false;
		if (getStartX()-getEndX()==0) vertical=true;

		StandardConnectionSocket tempSocket=new StandardConnectionSocket(connectionNode, StandardConnectionNode.plugBodyWidth-5, StandardConnectionNode.plugEndWidth, 
				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight, StandardConnectionNode.cornerRadius, plugShape.getOrientation());
		tempSocket.setBranch(true);

		//create the socket.
		tempSocket.layoutXProperty().bind(plugShape.getShape().layoutXProperty());
		tempSocket.layoutYProperty().bind(plugShape.getShape().layoutYProperty());
		tempSocket.setFill(tempSocket.getDisableColour());
		tempSocket.setStroke(Color.TRANSPARENT);

		//create the line connecting the socket to the main line.
		ConnectionLine tempLine1=new ConnectionLine(connectionNode,tempSocket);
		tempLine1.startXProperty().bind(tempSocket.layoutXProperty().add(StandardConnectionNode.plugBodyWidth)); 
		tempLine1.startYProperty().bind(tempSocket.layoutYProperty());
		tempLine1.endXProperty().bind(tempLine1.startXProperty().add(vertical ? 0 :30)); 
		tempLine1.endYProperty().bind(tempLine1.startYProperty());
		tempLine1.setStroke(tempSocket.getDisableColour());
		tempLine1.setStrokeWidth(3);

		ConnectionLine tempLine2=new ConnectionLine(connectionNode,tempSocket);
		tempLine2.startXProperty().bind(tempLine1.endXProperty()); 
		tempLine2.startYProperty().bind(tempLine1.endYProperty());
		tempLine2.endXProperty().bind(new ClosestXPointProperty(this, plugShape).add(vertical ? 0 :30).add(vertical ? 0 : StandardConnectionNode.plugBodyWidth));
		tempLine2.endYProperty().bind(new ClosestYPointProperty(this, plugShape));
		tempLine2.setStroke(tempSocket.getDisableColour());
		tempLine2.setStrokeWidth(3);

		ArrayList<ConnectionLine> connectionLines=new ArrayList<ConnectionLine>();
		connectionLines.add(tempLine1);
		connectionLines.add(tempLine2);
		tempSocket.setSocketConnectionLines(connectionLines);

		return tempSocket; 
	}

	@Override
	public Orientation getOrientation() {
		if (this.getStartX()-this.getEndX()==0) return Orientation.VERTICAL;
		if (this.getStartY()-this.getEndY()==0) return Orientation.HORIZONTAL;
		return null; //null if line is not horizontal or vertical. 
	}

	@Override
	public void setConnectedShape(ConnectorNode connecionShape) {
		//Do nothing- lines do not have connected shapes- only branch sockets do. 

	}

	@Override
	public ConnectorNode getConnectedShape() {
		// never has a connected shape as many shapes may be connected. 
		return null;
	}

	@Override
	public StandardConnectionNode getConnectionNode() {
		return connectionNode;
	}

	/**
	 * Checkj whether the line is showing error status. 
	 * @return true if showing error status. 
	 */
	public boolean isError() {
		return isError;
	}

	/**
	 * Set line to show error. 
	 * @param isError - true to show error. 
	 */
	public void setError(boolean isError) {
		this.isError = isError;
		if (isError) setLineErrorAppearance();
		else  setLineNormalAppearance();
	}


	/**
	 * Chick if branch sockets can connect to line
	 * @return true if branch sockets can connect
	 */
	public boolean isAllowBranchSockets() {
		return allowBranchSockets;
	}

	/**
	 * Allow the line to accept branch sockets.
	 * @param allowBranchSockets
	 */
	public void setAllowBranchSockets(boolean allowBranchSockets) {
		this.allowBranchSockets = allowBranchSockets;
	}

	/**
	 * Get the parent connection shape. Each line connects a ConnectionShape to a ConnectionNode - the connectionShape
	 * the line connects to is the ParentConnectionShape.   
	 * @return the ConnectionShape the line connects the ConnectionNode to. 
	 */
	public ConnectorNode getParentConnectionShape() {
		return parentConnectionShape;
	}

	@Override
	public boolean checkPossibleConnection(boolean notify) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/**
	 * TODO- this should probably be replaced with low level binding instead of property. 
	 * Property to return the x co-ordinate of the closest point on a line to another connection shape. 
	 * @author Jamie Macaulay
	 *
	 */
	class ClosestXPointProperty extends SimpleDoubleProperty {

		/**
		 * Reference ot the connection shape
		 */
		private ConnectorNode connectionShape;

		/**
		 * Reference to the line on which to determine the closest point. 
		 */
		private Line line;

		ClosestXPointProperty(Line line, ConnectorNode connectionShape){
			this.line=line;
			this.connectionShape=connectionShape;
		}


		@Override
		public double get() {
			return getClosestPoint().getX();
		}

		protected Point2D getClosestPoint(){
			Point2D closestPoint=ConnectionNodeUtils.findClosestPoint(new Point2D(line.getStartX(), line.getStartY()), new Point2D(line.getEndX(), line.getEndY()), 
					new Point2D(connectionShape.getShape().getLayoutX(), connectionShape.getShape().getLayoutY()));
			return closestPoint;
		}

	}
	

	/**
	 * Property to return the y co-ordinate of the closest point on a line to another connection shape. 
	 * @author Jamie Macaulay
	 *
	 */
	class ClosestYPointProperty extends ClosestXPointProperty {

		ClosestYPointProperty(Line line, ConnectorNode connectionShape) {
			super(line, connectionShape);
		}

		@Override
		public double get() {
			return getClosestPoint().getY();
		}

	}


}
