package pamViewFX.fxNodes.connectionPane.structures;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import pamViewFX.fxNodes.connectionPane.ConnectionNodeBody;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;

/**
 * The main rectangle nose for the ConnectionGroupNode. 
 * This rectangle is larger and can contain ConnectionNodes
 * 
 * @author Jamie Macaulay
 *
 */
public class ConnectionGroupBody extends ConnectionNodeBody {

	/**
	 * The size of the rectangle when it is minimized
	 */
	public static double RECTANGLE_SIZE_CONTRACTED= 150; 
	
	
	public static double RECTANGLE_SIZE_EXPANDED= 500; 
	
	/**
	 * The % expand. 
	 */
	public static double HIGHLIGHT_EXPAND = 1.05;

	/**
	 * Reference to the group structure. 
	 */
	private ConnectionGroupStructure groupStructure; 
	
	/**
	 * The sub pane for the connection node 
	 */
	private ConnectionGroupSubPane connectionSubPane; 
	
	/**
	 * The border and background colour of the rectangle. 
	 */
	private Color backGroundCol = Color.DODGERBLUE; 

	/**
	 * The transparency of the rectangle
	 */
	private double tranparancy = 0.3;


	private boolean isExpanded = false; 


	/**
	 * Check whether the node is highlighted e.g.. if  another node has been dragged over it. 
	 */
	private boolean isHighlighted = false;


	private Scale scaleTransform;
	
    private static final double BLUR_AMOUNT = 10;
	
    private static final Effect frostEffect =
            new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3);
	

	public ConnectionGroupBody(ConnectionGroupStructure connectionGroupStructure)	{
		super(connectionGroupStructure);

		groupStructure=connectionGroupStructure; 
			
		connectionSubPane = new ConnectionGroupSubPane(connectionGroupStructure);
//		connectionSubPane.prefWidthProperty().bind(this.widthProperty());
//		connectionSubPane.prefHeightProperty().bind(this.heightProperty());
//		connectionSubPane.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");		
		
		connectionSubPane.setPrefWidth(RECTANGLE_SIZE_EXPANDED);
		connectionSubPane.setPrefHeight(RECTANGLE_SIZE_EXPANDED);
		
		connectionSubPane.setMaxWidth(RECTANGLE_SIZE_EXPANDED);
		connectionSubPane.setMaxHeight(RECTANGLE_SIZE_EXPANDED);
		

		scaleTransform = new Scale(1,1);
		connectionSubPane.getTransforms().add(scaleTransform); 
		
		Pane blurPane = new Pane();
		Color color = Color.color(1, 1, 1, 0.8);
		blurPane.setBackground(new Background(new BackgroundFill(color,new CornerRadii(15), new Insets(5,5,5,5))));
		blurPane.setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.NONE, new CornerRadii(15), new BorderWidths(5))));

		
		blurPane.prefWidthProperty().bind(this.widthProperty());
		blurPane.prefHeightProperty().bind(this.heightProperty());
		

		
		this.getChildren().addAll(blurPane, connectionSubPane); 
		blurPane.toBack();

		
		//		//want to make sure the rectangle resizes with the node. 
		//		((Rectangle) this.getCustomShape()).widthProperty().bind(this.prefWidthProperty());
		//		((Rectangle) this.getCustomShape()).heightProperty().bind(this.prefHeightProperty());
		
		this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,CornerRadii.EMPTY,Insets.EMPTY)));
		connectionSubPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,CornerRadii.EMPTY,Insets.EMPTY)));


		this.setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(5))));
		connectionSubPane.setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.NONE, new CornerRadii(15), new BorderWidths(5))));


		this.setPrefSize(RECTANGLE_SIZE_EXPANDED, RECTANGLE_SIZE_EXPANDED);
		this.setMaxSize(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		
//		setTofrontOnDrag(false);  
		
		//initial state
		expandStructure(false); 
		connectionSubPane.setDisable(true);

//		connectionSubPane.setMouseTransparent(true);

	}
	
	@Override
	public void setupMouseBehaviour(){
		
		this.setOnMousePressed((event)->{
			
			if (this.groupStructure.isMouseDisable()) return; 
			
			//System.out.println(" Mouse pressed. Rectangle: "+ event.getX()+"   "+event.getY()+" "+name);
			dragStarted(event);
        	//record dragX/dragY variables for all plugs so they can drag with rectangle. 
        	
        	event.consume(); //need to consume. 

		});
		

        this.setOnMouseDragged((event)->{
        	
			if (this.groupStructure.isMouseDisable()) return; 
        	
        	//System.out.println("getSceneX " +event.getSceneX() +" getSceneY "+event.getSceneY()+" getX "+event.getX()+ " getY "+event.getY()); 
        	
        	Point2D newPos=groupStructure.sceneToLocal(event.getSceneX(), event.getSceneY());
        	double dragPosX=newPos.getX();
        	double dragPosY=newPos.getY();
    
        	
        	Point2D point2D=groupStructure.checkWithinPane(dragPosX, dragPosY);
        	if (point2D!=null){
        		dragPosX=point2D.getX();
        		dragPosY=point2D.getY();
        	}      	        	
        	groupStructure.setCollisionShapesNull();
        	
        	//only move rectangle if the mouse has started dragging from inside. 
        	if (isDraggingOK()){
        		//move the rectangle
	        	this.setLayoutX(dragPosX-getDragX());
	        	this.setLayoutY(dragPosY-getDragY());
        	}
        	
        	this.groupStructure.getConnectionPane().notifyChange(StandardConnectionNode.DRAGGING, this.groupStructure); 
        	
        	event.consume(); //need to consume. 

        });
                

        this.setOnMouseReleased((event)->{        	
    
        	
			if (groupStructure.isMouseDisable()) return; 
        	
//        	for (int i=0; i<groupStructure.getConnectionPlugs().size() ;i++){
//        		groupStructure.getConnectionPlugs().get(i).checkConnection();
//        	}
        	
        	for (int i=0; i<groupStructure.getConnectionSockets().size() ;i++){
        		groupStructure.getConnectionSockets().get(i).checkConnection();
        	}
        	
        	this.groupStructure.getConnectionPane().notifyChange(StandardConnectionNode.DRAG_DROP, groupStructure); 
        	
        	event.consume(); //need to consume. 
        	
        });
        

		//add ability for structure to expand. 
        this.setOnMouseClicked((mouseEvent) -> {
        	if (isTofrontOnDrag()) toFront(); //bring the pane to the front
        	if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
        		if(mouseEvent.getClickCount() == 2){
        			//		            	//expand the structure on a double click 
        			if (isExpanded) {
        				expandStructureTransition(false);
        				connectionSubPane.setDisable(true);
        			}
        			else {
        				expandStructureTransition(true);
        				connectionSubPane.setDisable(false);
        			}


        			mouseEvent.consume(); //need to consume. 
        		}
        	}
        });
		
	}

	
	/****Extra connection plug behaviour****/

	
	/**
	 * Get the connection sub pane. 
	 * @return the connection sub pane. 
	 */
	public ConnectionGroupSubPane getConnectionSubPane() {
		return connectionSubPane;
	}
	



	
	/**
	 * Expand the connection node with an animation. 
	 * @param expand - true to expand. false to contract. 
	 */
	private void expandStructureTransition(boolean expand) {
		this.isExpanded= expand;
		if (expand) {
			this.changeSizeTransition(ConnectionGroupBody.RECTANGLE_SIZE_EXPANDED, ConnectionGroupBody.RECTANGLE_SIZE_EXPANDED); 
		}
		else {
			this.changeSizeTransition(ConnectionGroupBody.RECTANGLE_SIZE_CONTRACTED, ConnectionGroupBody.RECTANGLE_SIZE_CONTRACTED); 
		}
	}
	
	/**
	 * Expand the structure without an animation
	 * @param -true to expand, false to contract. 
	 */
	private void expandStructure(boolean expand) {
		this.isExpanded= expand;
		if (expand) {
			this.changeSize(ConnectionGroupBody.RECTANGLE_SIZE_EXPANDED, ConnectionGroupBody.RECTANGLE_SIZE_EXPANDED); 
		}
		else {
			this.changeSize(ConnectionGroupBody.RECTANGLE_SIZE_CONTRACTED, ConnectionGroupBody.RECTANGLE_SIZE_CONTRACTED); 
		}
	}
	
	/**
	 * Highlights the structure by increasing the size slightly. 
	 * @param highlight - true to highlight and false to go back to original state. 
	 */
	void highlightStructure(boolean highlight) {
		
//		if (!isExpanded) return; 

		double expandSize = isExpanded ? RECTANGLE_SIZE_EXPANDED : RECTANGLE_SIZE_CONTRACTED; 
		if (highlight && !isHighlighted) {
//			System.out.println("Highlight!: "); 
			changeSizeTransition(expandSize*HIGHLIGHT_EXPAND, expandSize*HIGHLIGHT_EXPAND, 50); 
		}
		else if(!highlight && isHighlighted) {
//			System.out.println("De highlight!"); 
			changeSizeTransition(expandSize, expandSize, 100); 
		}
		this.isHighlighted = highlight;

	}

	
	
	/**
	 * Change the size of the node with scaling but with no animation 
	 * @param width - width to change to.
	 * @param height - height to change to. 
	 */
	private void changeSize(double width, double height) {
		scaleTransform.xProperty().setValue(width/RECTANGLE_SIZE_EXPANDED);
		scaleTransform.yProperty().setValue(height/RECTANGLE_SIZE_EXPANDED);
		
		this.layoutXProperty().setValue(this.getLayoutX()-(width-this.getWidth())/2);
		this.layoutYProperty().setValue(this.getLayoutY()-(height-this.getHeight())/2);
		
		this.prefWidthProperty().setValue(width);
		this.prefHeightProperty().setValue(height);
	}


	/**
	 * Changes the size of a node with an animation. 
	 * @param pane - the input pane to change size. 
	 * @param width - width to change to.
	 * @param height - height to change to. 
	 */
	private void changeSizeTransition(double width, double height) {
		changeSizeTransition( width,  height,  200);
	}


	/**
	 * Changes the size of a node with an animation. 
	 * @param pane - the input pane to change size. 
	 * @param width - width to change to.
	 * @param height - height to change to. 
	 */
	private void changeSizeTransition(double width, double height, double millisDuration) {

		Duration cycleDuration = Duration.millis(millisDuration);
		
		/**
		 *There was some relaly strnge resizing going on here with the scale factor. Realised this was to do with the fact that main pane 
		 *was a StackPane. These have always been a little strange so changed to a pane. 
		 */
		Timeline timeline = new Timeline(
				new KeyFrame(cycleDuration,
						new KeyValue(scaleTransform.xProperty(),width/RECTANGLE_SIZE_EXPANDED,Interpolator.EASE_BOTH)),
				new KeyFrame(cycleDuration,
						new KeyValue(scaleTransform.yProperty(),height/RECTANGLE_SIZE_EXPANDED,Interpolator.EASE_BOTH)),

				new KeyFrame(cycleDuration,
						new KeyValue(this.layoutXProperty(),this.getLayoutX()-(width-this.getWidth())/2,Interpolator.EASE_BOTH)),
				new KeyFrame(cycleDuration,
						new KeyValue(this.layoutYProperty(),this.getLayoutY()-(height-this.getHeight())/2,Interpolator.EASE_BOTH)),
				
				new KeyFrame(cycleDuration,
						new KeyValue(this.prefWidthProperty(),width,Interpolator.EASE_BOTH)),
				new KeyFrame(cycleDuration,
						new KeyValue(this.prefHeightProperty(),height,Interpolator.EASE_BOTH))

				);
		
		timeline.play();
		timeline.setOnFinished(event->{
			groupStructure.resizeFinished(); 
		});
	}

	
	//Used for ICON showing now, not in the actual ConnectionNode itself. 
	/**
	 * Get the symbol for the connection structure. 
	 * @return the connection rectangle. 
	 */
	public static Rectangle getGroupStructureIcon(double size) {
		return createGroupRectangleNode(size);		
	}


	/**
	 * Create a node. 
	 * @return the node.
	 */
	private static Rectangle createGroupRectangleNode(double size) {
		//create the rectangle
		Rectangle rect = new Rectangle();
		rect.setFill(Color.TRANSPARENT);
		rect.setWidth(size);
		rect.setHeight(size);
		rect.setStrokeWidth(10);
		rect.setStroke(Color.DODGERBLUE);
		rect.setArcHeight(15);
		rect.setArcWidth(15);
		return rect; 
	}

	/**
	 * Get the fill colour
	 * @return the fill colour. 
	 */
	@SuppressWarnings("unused")
	private  Paint getFillCol() {
		return Color.color(backGroundCol.getRed(), backGroundCol.getGreen(), backGroundCol.getBlue(), tranparancy);
	}

	/**
	 * Check whether the structure is expanded or not
	 * @return if the connection group structure is expanded. 
	 */
	public boolean isExpanded() {
		return isExpanded;
	}


}
