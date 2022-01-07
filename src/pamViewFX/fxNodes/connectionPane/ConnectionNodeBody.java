package pamViewFX.fxNodes.connectionPane;


import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * The main body of the connection node, i.e. everything bar the plugs and bits
 * and pieces around the outside. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ConnectionNodeBody extends Pane {
	
	//data for dragging modules between panes. 
	public static final String CONNECTION_NODE_DRAG_KEY="connection_node";

	//keep track of drag events
	double dragX=0; 
	
	
	double dragY=0;
	
	/**
	 * True if dragging is OK. 
	 */
	boolean draggingOK=false;
	
	/**
	 * Reference to the connection node the rectangle belongs to. 
	 */
	private StandardConnectionNode connectionNode;

	/**
	 * The main shape. 
	 */
	private Node mainShape;
	
	/**
	 * True to bring the shape to the front. 
	 */
	private boolean tofrontOnDrag = true;

	/**
	 * Allow the body to be dragged outside the bounds of the connection pane
	 */
	private boolean isDragOutsidePane; 

	/**
	 * Constructor for the connection node shape with default grey rectange as shape. 
	 * @param connectionNode - the connection node the shape is associated with 
	 */
	public ConnectionNodeBody(StandardConnectionNode connectionNode){
		super();
		this.connectionNode=connectionNode; 
		
		//default decorations
		createConnectionRectangle();
		
        setupMouseBehaviour();
	}
	
	/**
	 * Constructor for the connection node shape using a custom shape. 
	 * @param connectionNode - the connection node the shape is associated with 
	 * @param shape - the shape of the connection node. 
	 */
	public ConnectionNodeBody(StandardConnectionNode connectionNode, Node shape){
		super();
		this.connectionNode=connectionNode; 
		//use a custom shape 
		mainShape = shape; 
		this.getChildren().add(shape);
        setupMouseBehaviour();
	}
	
	/**
	 * Gets the custom set custom shape. Can be null. 
	 * @return the custom shape. 
	 */
	public Node getCustomShape(){
		return mainShape; 
	}


	/**
	 * Setup the rectangle. 
	 */
	private void createConnectionRectangle(){
		this.setPrefSize(StandardConnectionNode.DEFUALT_PREF_WIDTH, StandardConnectionNode.DEFUALT_PREF_HEIGHT);	
		this.setMaxSize(StandardConnectionNode.DEFUALT_PREF_WIDTH, StandardConnectionNode.DEFUALT_PREF_HEIGHT);
		this.setBackground(new Background(
				new BackgroundFill(Color.LIGHTGRAY,CornerRadii.EMPTY,Insets.EMPTY)));
	}

	/**
	 * Set the default mouse behaviour for the rectangle.
	 */
	public void setupMouseBehaviour(){
		
		this.setOnMouseClicked((event)->{
			event.consume(); //need to consume. 
		});
		
		this.setOnMousePressed((event)->{
			
			if (this.connectionNode.isMouseDisable()) return; 
			
			//System.out.println(" Mouse pressed. Rectangle: "+ event.getX()+"   "+event.getY()+" "+name);
			dragStarted(event);
        	//record dragX/dragY variables for all plugs so they can drag with rectangle. 
        	for (int i=0; i<connectionNode.getConnectionPlugs().size(); i++){
        		connectionNode.getConnectionPlugs().get(i).setDragX(connectionNode.getConnectionPlugs().get(i).getLayoutX()-this.getLayoutX());
        		connectionNode.getConnectionPlugs().get(i).setDragY(connectionNode.getConnectionPlugs().get(i).getLayoutY()-this.getLayoutY());
        	}

        	if (tofrontOnDrag) {
        	connectionNode.toFront(); //make sure during dragging this is in the front of the pane.
        	}
        	
        	event.consume(); //need to consume. 

		});
		

        this.setOnMouseDragged((event)->{
        	
			if (this.connectionNode.isMouseDisable()) return; 
        	
        	//System.out.println("getSceneX " +event.getSceneX() +" getSceneY "+event.getSceneY()+" getX "+event.getX()+ " getY "+event.getY()); 
        	
        	Point2D newPos=connectionNode.sceneToLocal(event.getSceneX(), event.getSceneY());
        	double dragPosX=newPos.getX();
        	double dragPosY=newPos.getY();
    

        	if (!isDragOutsidePane) {
        		Point2D point2D=connectionNode.checkWithinPane(dragPosX, dragPosY);
        		if (point2D!=null){
        			dragPosX=point2D.getX();
        			dragPosY=point2D.getY();
        		}      	        	
        	}

        	connectionNode.setCollisionShapesNull();
        	
        	//only move rectangle if the mouse has started dragging from inside. 
        	if (draggingOK){
        		//move the rectangle
	        	this.setLayoutX(dragPosX-dragX);
	        	this.setLayoutY(dragPosY-dragY);
	        	//move all plugs by the same amount
	        	for (int i=0; i<connectionNode.getConnectionPlugs().size(); i++){
	        		if (!connectionNode.getConnectionPlugs().get(i).layoutXProperty().isBound() &&
	        				!connectionNode.getConnectionPlugs().get(i).layoutYProperty().isBound()){
		        		connectionNode.getConnectionPlugs().get(i).setLayoutX(dragPosX-dragX+connectionNode.getConnectionPlugs().get(i).getDragX());
		        		connectionNode.getConnectionPlugs().get(i).setLayoutY(dragPosY-dragY+connectionNode.getConnectionPlugs().get(i).getDragY());
	        		}
	        	}
        	}
        	
        	this.connectionNode.getConnectionPane().notifyChange(StandardConnectionNode.DRAGGING, this.connectionNode); 
        	
        	event.consume(); //need to consume. 

        });
                

        this.setOnMouseReleased((event)->{
        	
			if (this.connectionNode.isMouseDisable()) return; 
        	
        	for (int i=0; i<connectionNode.getConnectionPlugs().size() ;i++){
        		connectionNode.getConnectionPlugs().get(i).checkConnection();
        	}
        	
        	for (int i=0; i<connectionNode.getConnectionSockets().size() ;i++){
        		connectionNode.getConnectionSockets().get(i).checkConnection();
        	}
        	
        	this.connectionNode.getConnectionPane().notifyChange(StandardConnectionNode.DRAG_DROP, this.connectionNode); 
        	
        	event.consume(); //need to consume. 
        	
        });
        
	}
	
	
	/**
	 * Called whenever a drag is started- e.g. when mouse is pressed. Need to record where in the rectangle the mouise click happened. 
	 * Otherwise drag always ends up at (0,0)
	 * @param event- the mouse event. 
	 */
	public void dragStarted(MouseEvent event){
    	if (event.getX()>=0 && event.getX()<this.getWidth() && 
    			event.getY()>0 &&  event.getY()<this.getHeight()){
		    // record a delta distance for the drag and drop operation.
			dragX = event.getX();
		    dragY = event.getY();
		    draggingOK=true;;
    	}
	}
	

	/**
	 * Get the start drag position in the local frame i.e. where in the body the drag started
	 * @return the drag X position
	 */
	public double getDragX() {
		return dragX;
	}

	/**
	 * Get the start drag position in the local frame i.e. where in the body the drag started
	 * @return the drag Y position
	 */
	public double getDragY() {
		return dragY;
	}

	
	/**
	 * Check whether the current drag is OK to move the body (e.g. started within body)
	 * @return true if OK
	 */
	public boolean isDraggingOK() {
		return draggingOK;
	}

	/**
	 * Check whether to front on drag.
	 * @return true if the node is brought to front on a drag
	 */
	public boolean isTofrontOnDrag() {
		return tofrontOnDrag;
	}

	/**
	 * Sets whether a node is brought to front (z order). 
	 * @param tofrontOnDrag - true to bring node to front on drag
	 */
	public void setTofrontOnDrag(boolean tofrontOnDrag) {
		this.tofrontOnDrag = tofrontOnDrag;
	}

	/**
	 * Check whether the connection node body can drag outside the pane. 
	 * @return true if the node can be dragged outsided the bounds of the pane. 
	 */
	public boolean isDragOutsidePane() {
		return isDragOutsidePane;
	}


	/**
	 * Set whether the connection node body can drag outside the pane. 
	 * @return true if the node can be dragged outside the bounds of the pane. 
	 */
	public void setDragOutsidePane(boolean isDragOutsidePane) {
		this.isDragOutsidePane = isDragOutsidePane;
	}
	
	

//	/**
//	 * Called whenever a drag is started- e.g. when mouse is pressed. 
//	 * @param event- the mouse event. 
//	 */
//	public void dragStarted(MouseEvent event){
//    	if (event.getX()>=0 && event.getX()<this.getWidth() && event.getY()>0 &&  event.getY()<this.getHeight()){
//		    // record a delta distance for the drag and drop operation.
//		    initialPointXY=connectionPane.localToScreen(getLayoutX(), getLayoutY());
//			dragX = initialPointXY.getX()*connectionPane.getScaleTransform().getX();// - event.getSceneX();
//		    dragY = initialPointXY.getY()*connectionPane.getScaleTransform().getX();; //- event.getSceneY();   
//		    draggingOK=true; 
//    	}
//	}
//	
//	/**
//	 * Set the default mouse behaviour for the rectangle.
//	 */
//	public void setDefaultMouseBehaviour(){
//		
//		this.setOnMousePressed((event)->{
//			
//			dragStarted(event);
//			System.out.println(" Mouse pressed. Rectangle: "+ initialPointXY.getX()+"   "+initialPointXY.getY()+" ");
//        	//record dragX/dragY variables for all plugs so they can drag with rectangle. 
//        	for (int i=0; i<connectionNode.getConnectionPlugs().size(); i++){
//        		connectionNode.getConnectionPlugs().get(i).setDragX(connectionNode.getConnectionPlugs().get(i).getLayoutX()-event.getSceneX());
//        		connectionNode.getConnectionPlugs().get(i).setDragY(connectionNode.getConnectionPlugs().get(i).getLayoutY()-event.getSceneY());
//        	}
//
//		});
//		
//
//        this.setOnMouseDragged((event)->{
//        	
//        	System.out.println("getSceneX " +event.getSceneX() +" getSceneY "+event.getSceneY()+" getX "+event.getX()+ " getY "+event.getY()); 
//        	
//        	double dragPosX=event.getScreenX();
//        	double dragPosY=event.getScreenY(); 
//    
////        	//check within boundry 
////        	Point2D point2D=connectionNode.checkWithinPane(dragPosX, dragPosY);
////        	if (point2D!=null){
////        		dragPosX=point2D.getX();
////        		dragPosY=point2D.getY();
////        	}
//        	    
//        	
//        	connectionNode.setCollisionShapesNull();
//        	//only move rectangle if the mouse has started dragging from inside. 
//        	if (draggingOK){
//        		//move the rectangle
//        		Point2D newPoint=connectionPane.screenToLocal(dragPosX, dragPosY);
//	        	this.setLayoutX(newPoint.getX()*(1/connectionPane.getScaleTransform().getX()));
//	        	this.setLayoutY(newPoint.getY()*(1/connectionPane.getScaleTransform().getX()));
//	        	
//	        	//move all plugs by the same amount
//	        	for (int i=0; i<connectionNode.getConnectionPlugs().size(); i++){
//	        		if (!connectionNode.getConnectionPlugs().get(i).layoutXProperty().isBound() &&
//	        				!connectionNode.getConnectionPlugs().get(i).layoutYProperty().isBound()){
//		        		connectionNode.getConnectionPlugs().get(i).setLayoutX(dragPosX+connectionNode.getConnectionPlugs().get(i).getDragX());
//		        		connectionNode.getConnectionPlugs().get(i).setLayoutY(dragPosY+connectionNode.getConnectionPlugs().get(i).getDragY());
//	        		}
//	        	}
//        	}        
//        });
//        
//
//        this.setOnMouseReleased((event)->{
//        	
//        	for (int i=0; i<connectionNode.getConnectionPlugs().size() ;i++){
//        		connectionNode.getConnectionPlugs().get(i).checkConnection();
//        	}
//        	
//        	for (int i=0; i<connectionNode.getConnectionSockets().size() ;i++){
//        		connectionNode.getConnectionSockets().get(i).checkConnection();
//        	}
//        });
//	}


}
