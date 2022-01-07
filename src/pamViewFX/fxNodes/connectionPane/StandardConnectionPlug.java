package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Plug connected to a collision rectangle. Plugs can connect to sockets or, if
 * allowed, connection lines. Plugs can also be dragged without moving the
 * ConnectionRectangle.
 * 
 * @author Jamie Macaulay
 */
public class StandardConnectionPlug extends AbstractConnector implements  ConnectorNode {

	private double dragX=0; 
	private double dragY=0;

	/**
	 * If plug is dragged close to rectangle and then released it is deleted. deleteBarrier is how close
	 * it has to come in pixels before delete trigger is activated.
	 */
	private double deleteBarrier=10.; 

	/**
	 * Colour to show plug and lines can be deleted
	 */
	private Color deleteColour=new Color(1,0,0,0.5); 

	/**
	 * Colour to show plug and lines when illegal connection occurs
	 */
	private Color errorColour=Color.RED; 

	/**
	 * Lines which connect the plug to the ConnectionRectangle 
	 */
	private ArrayList<ConnectionLine> plugConnectionLines; 

	/**
	 * Indicates whether the plug is bound to the rectangle or not. 
	 */
	private boolean boundToRectangle=true; 

	/**
	 * Reference to the last ConnectionShape the plug connected to. 
	 */
	private ConnectorNode lastConnectionShape;

	/**
	 * Reference to the current rotation transform. 
	 */
	private Rotate rotation=new Rotate(0,0,0);

	/**
	 * The plug connection has some sort of error. 
	 */
	boolean isError=false;

	/**
	 * Number of milliseconds the rotation animation takes. 
	 */
	private long rotationDuration=50;

	/**
	 * Default x position of plug in relation to top of rectangle.
	 */
	DoubleProperty plugX=new SimpleDoubleProperty(20);

	/**
	 * Default y position of plug in relation to right of rectangle.
	 */
	DoubleProperty plugY=new SimpleDoubleProperty(20);

	/**
	 * The standard connection node. 
	 */
	private StandardConnectionNode connectionNode;

	//plug dimensions
	@SuppressWarnings("unused")
	private double plugBodyHeight;
	private double plugEndWidth;
	@SuppressWarnings("unused")
	private double plugEndHeight;
	private double plugBodyWidth; 

	public StandardConnectionPlug(StandardConnectionNode connectionNode, double plugBodyWidth, double plugEndWidth, 
			double plugBodyHeight, double plugEndHeight, double cornerRadius, Orientation orientation){
		super(connectionNode, orientation);
		this.connectionNode=connectionNode; 
		createPlug(plugBodyWidth, plugEndWidth, plugBodyHeight,plugEndHeight, cornerRadius, orientation);
	}


	/**
	 * Get the polygon point array for the plug.
	 * @param plugBodyWidth - the width of the plug body.
	 * @param plugEndWidth - the width of the end section of the plug (that fits into the socket).
	 * @param plugBodyHeight - the height of the plug body.
	 * @param plugEndHeight -  the height of the end section of the plug (that fits into the socket).
	 * @param cornerRadius - the corner radius of the plug 
	 * @return list of points that make a polygon of the plug shape. 
	 */
	public static ArrayList<Double> getPlugPolygon(double plugBodyWidth, double plugEndWidth, 
			double plugBodyHeight, double plugEndHeight,  double cornerRadius) {

		ArrayList<Double> points = new ArrayList<Double>();

		points.addAll(Arrays.asList(new Double[]{
				0.,0.,
				0.,plugEndHeight/2,
				0-plugEndWidth,plugEndHeight/2,
				0-plugEndWidth,plugBodyHeight/2,
				0-plugEndWidth-plugBodyWidth+cornerRadius,plugBodyHeight/2}));

		double circValx;
		double circValy;
		int nVals=(int) (cornerRadius*2);
		//create bottom left corner
		for (double i=0; i<nVals; i++){
			circValx=cornerRadius*Math.sin((i/nVals)*(Math.PI/2));
			circValy=cornerRadius*Math.cos((i/nVals)*(Math.PI/2));
			points.addAll(Arrays.asList(new Double[]{0-plugEndWidth-plugBodyWidth+cornerRadius-circValx, plugBodyHeight/2-(cornerRadius-circValy)}));
		}

		points.addAll(Arrays.asList(new Double[]{		
				0-plugEndWidth-plugBodyWidth,plugBodyHeight/2-cornerRadius,
				0-plugEndWidth-plugBodyWidth,-plugBodyHeight/2+cornerRadius}));

		//create top left corner
		for (double i=0; i<nVals; i++){
			circValx=cornerRadius*Math.sin((i/nVals)*(Math.PI/2));
			circValy=cornerRadius*Math.cos((i/nVals)*(Math.PI/2));
			points.addAll(Arrays.asList(new Double[]{0-plugEndWidth-plugBodyWidth+(cornerRadius-circValy), -plugBodyHeight/2+(cornerRadius-circValx)}));
		}

		//finish off the plug
		points.addAll(Arrays.asList(new Double[]{	
				0-plugEndWidth-plugBodyWidth+cornerRadius,-plugBodyHeight/2,
				0-plugEndWidth,-plugBodyHeight/2,
				0-plugEndWidth,-plugEndHeight/2,
				0.,-plugEndHeight/2
		}));
		
		return points;
	}
	/**
	 * Create a connection plug. 
	 * @param plugBodyWidth - the width of the plug body
	 * @param plugEndWidth - the width of the plug connection area- the bit which fits into the socket. 
	 * @param plugBodyHeight - the height of the main plug body.
	 * @param plugEndHeight - the height of the plug connection area- the bit which fits into the socket. 
	 */
	private void createPlug(double plugBodyWidth, double plugEndWidth, 
			double plugBodyHeight, double plugEndHeight,  double cornerRadius, Orientation orientation){

		this.plugBodyWidth=plugBodyWidth;
		this.plugEndHeight=plugEndHeight;
		this.plugEndWidth=plugEndWidth;
		this.plugBodyHeight=plugBodyHeight;

		plugConnectionLines= createPlugLines(this, orientation);

		this.getPoints().addAll(getPlugPolygon( plugBodyWidth, plugEndWidth, 
				plugBodyHeight,  plugEndHeight,   cornerRadius));

		//rotate shape by 90 degrees about 0,0
		if (orientation==Orientation.VERTICAL) rotation=new Rotate(90,0,0);
		this.getTransforms().add(rotation); //add to transform

		//initiate drag variables
		this.setOnMousePressed((event)->{

			Point2D dragStart = StandardConnectionPlug.this.sceneToLocal(event.getSceneX(), event.getSceneY());
			dragX = dragStart.getX();
			dragY = dragStart.getY();
			//			System.out.println("START: DragX: " + dragX + " event.getX(): " + event.getSceneX()); 

		});

		this.setOnMouseReleased((event)->{
			super.checkConnection();
			//check if plug is in the delete zone. If so delete
			if (checkPlugDelete()){
				//self-destruct!
				connectionNode.removeConnectionPlug(this,true); 
			};
		});

		this.setOnMouseDragged((event)->{

			/**
			 * Note that the layout x/y property has a listener in super class which check for connections as plug moves. 
			 */

			//set all the rest of the shapes to have no possible collisions
			setConnectorNodesNull();

			//may need to un-bind from rectangle if new - if so then need to add another plug to rectangle. 
			if (boundToRectangle){
				enableFreePlug(); 
				connectionNode.addDefaultPlug();
			}

			//error only happen when plug is connected
			setError(false);
			if (this.getConnectedShape()!=null) getConnectedShape().setError(false);

			//if dragging then the plug must have been disconnected. 
			//				this.setConnectionStatus(ConnectionShape.NO_CONNECTION, null);

			/**
			 * Note- it's import to only trigger function in parent class. Otherwise the NO_CONNECTION flag is sent to
			 * the this subclass function and keeps calling the NO_CONNECTION animation when in fact the shape can still be
			 * in close enough proximity to a socket so that it's status is POSSIBLE_CONNECTION. By constantly calling NO_CONNECTION
			 * and POSSIBLE_CONNECTION produces a weird jitter in the rotate animation. 
			 */
			super.setConnectionStatus(ConnectorNode.NO_CONNECTION, null);

			//the shape it was attached to must now be disconnected
			if (lastConnectionShape!=null){
				lastConnectionShape.setConnectionStatus(ConnectorNode.NO_CONNECTION, this);
				lastConnectionShape=null; 
			}

			//check if plug is in the delete zone
			if (checkPlugDelete()) setPlugDeleteAppearance(); 

			//unbind from anything the plug is bound to. 				
			//set correct position

			//			System.out.println("DragX: " + dragX + " event.getX(): " + event.getX()); 

			Point2D newPos=getLocalCords(event.getSceneX(), event.getSceneY());

			//			Point2D newPos = new Point2D(event.getX(), event.getY());


			//dragX is the offset on where the shape was clicked
			//getX is simply the X relative ot the shape - thus get Scene X is vital. 

			double dragPosX=newPos.getX();
			double dragPosY=newPos.getY();

			this.layoutXProperty().unbind();
			this.setLayoutX(dragPosX + dragX);
			this.layoutYProperty().unbind();
			this.setLayoutY(dragPosY + dragY);

			//trigger any connection node listeners. 
			//			connectionNode.triggerConnectorNodeListeners(this, ConnectorNode.CONNECTOR_MOVED);

			event.consume(); //important for complex scenes like this. 
		});			

		this.layoutXProperty().addListener((obsVal, oldVal, newVal)->{
			connectionNode.triggerConnectorNodeListeners(this, ConnectorNode.CONNECTOR_MOVED);
		}); 
		this.layoutYProperty().addListener((obsVal, oldVal, newVal)->{
			connectionNode.triggerConnectorNodeListeners(this, ConnectorNode.CONNECTOR_MOVED);
		}); 
	}


	/**
	 * Set all connector nodes null. This means possible conenctions that are out of range etc are removed 
	 */
	public void setConnectorNodesNull(){
		//set all the rest of the shapes to have no possible collisions
		connectionNode.setCollisionShapesNull();
	}

	/**
	 * Get the local co-ordinates for the plug when given the scene co-ordinates. 
	 * Note this is here just in case this requires overriding in special cases. 
	 * @return the local co-ordinates in for plug. 
	 */
	public Point2D getLocalCords(double x, double y) {
		return connectionNode.sceneToLocal(x, y);
	}


	/**
	 * Create a set of lines to which connect the rectangle to the plug. Square
	 * lines consist of three lines, two horizontal and one vertical. This results
	 * in right angled lines to the plug.
	 * 
	 * @param connectionPlug - the plug these lines are associated with.
	 * @return an array of lines to connect the rectangle to the male plug.
	 */
	private ArrayList<ConnectionLine> createPlugLines(StandardConnectionPlug connectionPlug, Orientation orientation){

		boolean vertical=orientation==Orientation.VERTICAL;


		ConnectionLine line1= new ConnectionLine(this.connectionNode, connectionPlug);

		if (vertical){
			line1.startXProperty().bind(getConnectionNodeBody().layoutXProperty().add(getConnectionNodeBody().widthProperty().divide(2))); 
			line1.startYProperty().bind(getConnectionNodeBody().layoutYProperty().add(getConnectionNodeBody().heightProperty())); 
			line1.endXProperty().bind(line1.startXProperty()); 
			line1.endYProperty().bind(connectionPlug.layoutYProperty().subtract(getConnectionNodeBody().layoutYProperty())
					.divide(new DivideLineProperty(connectionPlug, orientation)).add(getConnectionNodeBody().layoutYProperty())
					.subtract(plugBodyWidth+plugEndWidth)); 
		}
		else {
			line1.startXProperty().bind(getConnectionNodeBody().layoutXProperty().add(getConnectionNodeBody().widthProperty())); 
			line1.startYProperty().bind(getConnectionNodeBody().layoutYProperty().add(plugY));
			line1.endXProperty().bind(connectionPlug.layoutXProperty().subtract(getConnectionNodeBody().layoutXProperty())
					.divide(new DivideLineProperty(connectionPlug, orientation)).add(getConnectionNodeBody().layoutXProperty())
					.subtract(plugBodyWidth+plugEndWidth));
			line1.endYProperty().bind(line1.startYProperty());
		}

		ConnectionLine line2= new ConnectionLine(this.connectionNode, connectionPlug);
		line2.startXProperty().bind(line1.endXProperty());
		line2.startYProperty().bind(line1.endYProperty());
		line2.endXProperty().bind(vertical ? connectionPlug.layoutXProperty() : line1.endXProperty());
		line2.endYProperty().bind(vertical ? line1.endYProperty(): connectionPlug.layoutYProperty());

		ConnectionLine line3= new ConnectionLine(this.connectionNode, connectionPlug);
		line3.startXProperty().bind(line2.endXProperty());
		line3.startYProperty().bind(line2.endYProperty());
		line3.endXProperty().bind(vertical ? line3.startXProperty() : connectionPlug.layoutXProperty().subtract(plugBodyWidth+plugEndWidth));
		line3.endYProperty().bind(vertical ? connectionPlug.layoutYProperty().subtract(plugBodyWidth+plugEndWidth) : line3.startYProperty());

		//add lines to array list
		ArrayList<ConnectionLine> plugLines=new ArrayList<ConnectionLine>();
		plugLines.add(line1);
		plugLines.add(line2);
		plugLines.add(line3);

		return plugLines;

	}


	/**
	 * If dragged then a new disabled plug is made in default positions and the new plug becomes something which can
	 * be dragged around the connection pane. Sets plug to free mode and adds new disabled plug to node.
	 */
	public void enableFreePlug(){
		//			if (getLayoutX()!=connectionRectangle.getLayoutX()+plugX+connectionRectangle.getWidth() ||
		//					getLayoutX()!=connectionRectangle.getLayoutY()+plugY){
		boundToRectangle=false; 
		//enable the lines connecting the plug to the connection rectangle. 
		enableConnectionLines(); 
		toFront();
		//			}
	}

	/**
	 * Enable the connection lines for the plug. 
	 */
	private void enableConnectionLines(){
		for (int i=0; i<this.plugConnectionLines.size(); i++){
			plugConnectionLines.get(i).setConnectionStatus(NO_CONNECTION, null);
		}
	}

	/**
	 * Begin animation to rotate the plug to the rotate value;
	 */
	private void rotateToShape(ConnectorNode connectionShape){
		if (connectionShape!=null && connectionShape instanceof StandardConnectionSocket){
			if (this.getOrientation()!=connectionShape.getOrientation())	{
				//now this function is accessed a so don't want to start intensive animation if not necessary
				//					if ((connectionShape.getOrientation()==Orientation.VERTICAL ? 90 : 0)==rotation.getAngle()) return; 
				if (((this.getOrientation()==Orientation.VERTICAL) ? 90 : 0)==rotation.getAngle()) {
					//rotate the plug to a new angle. 
					rotatePlug((connectionShape.getOrientation()==Orientation.VERTICAL ? 90 : 0), rotationDuration); 
					setLineRotation( connectionShape.getOrientation()==Orientation.VERTICAL ? 90 : 0);
				}
			}
		}
	}

	/**
	 * Rotate the plug from current angle- shows an animation of the rotation
	 * @param angle - the angle to rotate to.
	 * @param duration - the amouint of time for the roation to take in millis, 
	 */
	private void rotatePlug(double angle, long duration){
		Timeline rotateAnimation=createAnimationTimeLine(angle, duration);
		if (rotateAnimation!=null) {
			rotateAnimation.play(); 
		}
	}


	/**
	 * Lines need to be slightly changed when plug rotates. Note this this is only for default plug lines.
	 * The function re arranges some high level bindings depedning on default and current orientation. 
	 * @param- the position the plug has moved to. 
	 */
	private void setLineRotation(double angle){
		//default back to normal
		boolean vertical=getOrientation()==Orientation.VERTICAL;
		if ((angle==90 && vertical) || (angle==0 && !vertical)){
			plugConnectionLines.get(1).endXProperty().bind(vertical ? this.layoutXProperty() : plugConnectionLines.get(0).endXProperty());
			plugConnectionLines.get(1).endYProperty().bind(vertical ? plugConnectionLines.get(0).endYProperty(): this.layoutYProperty());

			plugConnectionLines.get(2).endXProperty().bind(vertical ? plugConnectionLines.get(1).endXProperty() : this.layoutXProperty().subtract(plugBodyWidth+plugEndWidth));
			plugConnectionLines.get(2).endYProperty().bind(vertical ? this.layoutYProperty().subtract(plugBodyWidth+plugEndWidth) : plugConnectionLines.get(1).endYProperty());
		}
		//default vertical has switched to horizontal
		if ((angle==0 && vertical) ) {
			plugConnectionLines.get(1).endXProperty().bind(this.layoutXProperty().subtract(plugBodyWidth+plugEndWidth));
			plugConnectionLines.get(2).endYProperty().bind(this.layoutYProperty());
		}
		//horizontal has switched to vertical
		if (angle==90 && !vertical) {
			plugConnectionLines.get(1).endYProperty().bind(this.layoutYProperty().subtract(plugBodyWidth+plugEndWidth));
			plugConnectionLines.get(2).endXProperty().bind(this.layoutXProperty());
		}
	}

	//		/**
	//		 * Check whether the plug is vertical; 
	//		 * @return true if the plug is vertical.
	//		 */
	//		private boolean isVertical(){
	//			if (rotation.getAngle()==90) return true;
	//			else return false;
	//		}

	/**
	 * Create animation to rotate the angle to a new position
	 * @param newAngle- new angle to rotate to
	 * @param duration - time it take to rotate in millis
	 * @return a Timeline for rotate animation. 
	 */
	private Timeline createAnimationTimeLine(double newAngle, double duration){
		if (rotation==null) return null; 
		Timeline timeLine= new Timeline();
		// Animation for scroll SHOW.
		timeLine.setCycleCount(1); //defines the number of cycles for this animation
		final KeyValue kvDwn = new KeyValue(rotation.angleProperty(), newAngle);
		//			final KeyValue kvDwn = new KeyValue(translateProperty, expandedSize);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(duration), kvDwn);
		timeLine.getKeyFrames().add(kfDwn);
		return timeLine;
	}

	@Override
	public void setConnectionStatus(int type, ConnectorNode foundShape) {
		//			System.out.println("PlugConnectionStatus: "+type + " no. plugs "+connectionPlugs.size()+" no. sockets "+
		//			connectionSockets.size()+" no. branch sockets " +branchConnectionSockets.size());
		//			System.out.println(connectionPane.toString());
		super.setConnectionStatus(type, foundShape);
		switch (type){

		case NO_CONNECTION:

			//need to rotate plug back to it's original position
			if (((this.getOrientation()==Orientation.VERTICAL) ? 0 : 90)==rotation.getAngle()){  //&& (rotation.getAngle()==90 || rotation.getAngle()==0)){
				rotatePlug((getOrientation()==Orientation.VERTICAL) ? 90 : 0  ,rotationDuration); 
				setLineRotation( getOrientation()==Orientation.VERTICAL ? 90 : 0);
			}
			this.setError(false); //if plug disconnected then must be no error. 

			//			//plug may have been disconnected via a function rather than dragging so should be unbound from socket. 
			//25/03/2020 - need to find alternative way for programmatically removed plugs as this messes group structure. 
			if (!this.boundToRectangle) {
				this.layoutXProperty().unbind();
				this.layoutYProperty().unbind();
			}

			break; 

		case POSSIBLE_CONNECTION:
			//may need to rotate plug to fit to node with different orientation
			rotateToShape(foundShape); 
			//ConnectionNode.this.getChildren().removeAll(this.plugConnectionLines);
			break; 

		case CONNECTED:
			if (foundShape!=null){
				lastConnectionShape=foundShape;
				this.layoutXProperty().unbind();
				this.layoutYProperty().unbind();

				//bind the connection plug. 
				if (foundShape instanceof StandardConnectionSocket){
					this.layoutXProperty().bind(foundShape.getShape().layoutXProperty());
					this.layoutYProperty().bind(foundShape.getShape().layoutYProperty());
				}
			}

			//if the plug is bound to the rectangle and dragging the node means it connections to something. 
			if (boundToRectangle){
				//free up plug
				enableFreePlug();
				//add a new disabled plug
				connectionNode.addDefaultPlug(); 
			}

			break;
		}
	}

	/**
	 * Check whether the plug is bound to the Connection Rectangle. If so then
	 * enableFreePlug() needs to be called to unbind. By default a new diabled plug
	 * will be added when called.
	 */
	public boolean isBound(){
		return boundToRectangle; 
	}

	/**
	 * Check whether a plug is close to rectangle and therefore should be deleted.
	 * @return true if the plug can be deleted
	 */
	public boolean checkPlugDelete(){
		double offsetX=this.getLayoutX()-getConnectionNodeBody().getLayoutX();
		double offsetY=this.getLayoutY()-getConnectionNodeBody().getLayoutY();
		if (offsetX>-deleteBarrier && offsetX<=deleteBarrier+getConnectionNodeBody().getWidth() &&
				offsetY>-deleteBarrier && offsetY<=deleteBarrier+getConnectionNodeBody().getHeight()){
			//System.out.println("Inside the delete barrier: " + offsetX);
			return true;
		}
		return false; 
	}

	/**
	 * Set the plug to show whether it can be deleted or not. 
	 * @param delete - true to show plug is ready to be deleted 
	 */
	private void setPlugDeleteAppearance(){
		this.setStroke(deleteColour.brighter());
		this.setFill(deleteColour);
		for (int i=0; i<this.plugConnectionLines.size(); i++){
			plugConnectionLines.get(i).setStroke(deleteColour);
		}
		//else{
		////default back to usual colour depending on connection status. 
		//	super.setConnectionStatus(this.getConnectionStatus(), null);
		//	for (int i=0; i<this.plugConnectionLines.size(); i++){
		//		plugConnectionLines.get(i).setConnectionStatus(plugConnectionLines.get(i).getConnectionStatus(), null);
		//	}
		//}
	}

	/**
	 * Set the plug and lines to show an error.
	 **/
	private void setPlugErrorAppearance(){
		this.setStroke(errorColour.darker());
		this.setFill(errorColour);	
	}

	/**
	 * Set the plug and lines to normal appearance. 
	 */
	private void setPlugNormalAppearance() {
		this.setStroke(getNormalColour().darker());
		this.setFill(getNormalColour());
	}


	/**
	 * Get all branch sockets attached to the connectionLines of the plug. 
	 * @return all branch sockets attached to the connection lines of the plug. 
	 */
	public ArrayList<StandardConnectionSocket> getBranchSockets(){
		ArrayList<StandardConnectionSocket> branchSockets=new ArrayList<StandardConnectionSocket>(); 
		for (int i=0; i<connectionNode.getBranchConnectionSockets().size(); i++){
			if (connectionNode.getBranchConnectionSockets().get(i).getParentConnectionPlug()!=null && 
					connectionNode.getBranchConnectionSockets().get(i).getParentConnectionPlug().equals(this)){

				branchSockets.add(connectionNode.getBranchConnectionSockets().get(i));
			}
		}
		return branchSockets;
	}

	public ArrayList<ConnectionLine> getPlugConnectionLines() {
		return plugConnectionLines;
	}

	public void setPlugConnectionLines(ArrayList<ConnectionLine> plugConnectionLines) {
		this.plugConnectionLines = plugConnectionLines;
	}

	@Override
	public ArrayList<ConnectorNode> getPossibleConnectionShapes() {
		return connectionNode.getConnectionPane().getPlugAcceptingConnectionShapes(connectionNode);
	}

	public double getDragX() {		
		return dragX;
	}

	public void setDragX(double dragX) {
		this.dragX = dragX;
	}

	public double getDragY() {
		return dragY;
	}

	public void setDragY(double dragY) {
		this.dragY = dragY;
	}

	/**
	 * Check whether plug is set to show an error. 
	 * @return true if the plug shows an error like appearance. 
	 */
	@Override
	public boolean isError() {
		return isError;
	}

	/**
	 * Set whether plug is set to show an error. 
	 * @param true if the plug shows an error like appearance. 
	 */
	@Override
	public void setError(boolean isError) {
		this.isError = isError;
		if (isError) setPlugErrorAppearance();
		else setPlugNormalAppearance();
		for (int i=0; i<this.getPlugConnectionLines().size(); i++){
			this.getPlugConnectionLines().get(i).setError(isError);
		}
	}


	/**
	 * Simple double property to return 2 or 1 depending on position of the plug. 
	 *  
	 * @author Jamie Macaulay
	 */
	class DivideLineProperty extends SimpleDoubleProperty {

		private StandardConnectionPlug plug;

		private Orientation orientation; 

		DivideLineProperty(StandardConnectionPlug plug, Orientation orientation){
			this.plug=plug; 
			this.orientation=orientation; 
		}

		@Override
		public double get() {
			return (orientation==Orientation.HORIZONTAL) ? getXDividePos() : getYDividePos();
		}

		private double getXDividePos(){
			double plugXPos=plug.getLayoutX()-getConnectionNodeBody().getLayoutX();
			if (plugXPos>plugX.get()*3+getConnectionNodeBody().getWidth()){
				return 2.;
			}
			else if (plugXPos<0) return 0.7;
			else return 1.; 

		}

		private double getYDividePos(){
			double plugYPos=plug.getLayoutY()-getConnectionNodeBody().getLayoutY();
			if (plugYPos>plugY.get()*3+getConnectionNodeBody().getWidth()){
				return 2.;
			}
			else if (plugYPos<0) return 0.7;
			else return 1.; 
		}
	}

	/**
	 * Get the connection node body. 
	 * @return the connection node body
	 */
	public Pane getConnectionNodeBody() {
		return connectionNode.getConnectionNodeBody();
	}


}
