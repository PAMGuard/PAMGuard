package pamViewFX.fxNodes.internalNode;

import org.controlsfx.glyphfont.Glyph;

import pamViewFX.fxNodes.PamButton;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * A PamInternalNode is a resizable node which can be resized within another node. 
 * @author Jamie Macaulay
 *
 */
public class PamInternalPane extends Group {
	

	/**
	 * The main pane. 
	 */
	private Region mainPane;

	/**
	 * Pane which holds the this PamInternalPane. 
	 */
	private Region holderPane; 

	/**
	 * Controls to resize the pane. 
	 */
	private Group resizeNodes; 
	
	/**
	 * Controls to resize the pane. 
	 */
	private Group decorations; 

	/**
	 * The min dimension 
	 */
	double minDim=30;

	/**
	 * Size in pixels of the anchors. 		
	 */
	double anchorSize=20;
	
	/*Need to store relative positions for window resizing*/
	
	//fraction of x layout
	double layoutXFraction=0; 
	
	//fraction of y layout
	double layoutYFraction=0;

	//fraction of width 
	double widthFraction=1;

	//fraction of height
	double heightFraction=1;

	/**
	 * True of resize nodes are added or false otherwise. 
	 */
	private boolean editable=false;

	/**
	 * Indicates whether the internal pane can be resized as well as dragged when control nodes are added. 
	 */
	private boolean resizeable=true; 

	
	/**
	 * Create a new internal pane. 
	 * @param mainPane- the pane to display and allow resizing. 
	 */
	public PamInternalPane(Region mainPane, Region holderPane){
		super();
		this.mainPane=mainPane; 
		this.holderPane=holderPane; 
		this.getChildren().add(mainPane);
		//DragResizer.makeResizable(this);
//		getPaneScaleFractions(); 
	    
		resizeNodes=createResizeDragNodes();
		setHolderPaneScaling();
	    
	    showResizeControls(false);
	}
	
	/**
	 * Set so that the pane scale with the holder pane. e.g. if the holder pane reduced in width by half then the 
	 * internal pane also reduces in with by half. 
	 */
	private void setHolderPaneScaling(){
		holderPane.widthProperty().addListener((change)->{
//			System.out.println("Holder width changed: "+ holderPane.getWidth());
			scaleInternalPane();
		});
	    holderPane.heightProperty().addListener((change)->{
//			System.out.println("Holder height changed: "+ holderPane.getHeight());
	    	scaleInternalPane();
		});
	}
	
	/**
	 * Resize this pane based on X and Y fractions
	 */
	private void scaleInternalPane(){
		if (holderPane==null) return;
		
		if (Double.isNaN(layoutXFraction) || Double.isNaN(layoutYFraction) || 
				Double.isNaN(widthFraction)  || Double.isNaN(heightFraction)){
			this.getPaneScaleFractions();
		}
		
		double newX=layoutXFraction*holderPane.getWidth();
		double newY=layoutYFraction*holderPane.getHeight();
		double newWidth=widthFraction*holderPane.getWidth(); 
		double newHeight=heightFraction*holderPane.getHeight(); 
		
		newX=Math.max(0,Math.min(newX,holderPane.getWidth()-anchorSize)); 
		newY=Math.max(0,Math.min(newY,holderPane.getHeight()-anchorSize)); 
		newWidth=Math.max(minDim, newWidth);
		newHeight=Math.max(minDim, newHeight);

//		System.out.println("Has properties: "+mainPane.getPrefWidth() + " newX "+newX+ 
//				" newY "+newY+ " newWidth "+ newWidth + " newHeight "+newHeight);
		
		//BUG: sometimes NaN values appear which 'destroys' the displays. 
		if (!Double.isNaN(layoutXFraction) && !Double.isNaN(layoutYFraction) && 
				!Double.isNaN(widthFraction)  && !Double.isNaN(heightFraction)){
			mainPane.setLayoutX(newX);
			mainPane.setLayoutY(newY);
			mainPane.setPrefWidth(newWidth);
			mainPane.setPrefHeight(newHeight);
		}
	} 
	
	/**
	 * Calculate window fraction which allow for scaled resizing
	 */
	private void getPaneScaleFractions(){
		//hmmmm....need to fix NaN values. 
		layoutXFraction=mainPane.getLayoutX()/holderPane.getWidth();
		layoutYFraction=mainPane.getLayoutY()/holderPane.getHeight();
		widthFraction=mainPane.getPrefWidth()/holderPane.getWidth();
		heightFraction=mainPane.getPrefHeight()/holderPane.getHeight();
	}
	
	/**
	 * Add or remove the resizing nodes
	 * @param editable - true to add resize controls, false to remove resize controls.
	 */
	public void showResizeControls(boolean editable) {
		if (this.editable==editable) return;
		this.editable=editable;
		
		if (editable){
		this.getChildren().addAll(resizeNodes);
		}
		else{
			this.getChildren().remove(resizeNodes);
		}
		
	}
	
	/**
	 * Set whether the internal pane can be resized or not. If not then the pane can be draged if resize nodes are added but not resized. 
	 * @param resizable -true to allow the pane to be resized and dragged. False to only allow the pane to be dragged. 
	 */
	public void setResizable(boolean resizable){
		this.resizeable=resizable;
	}

	/**
	 * Check whether the internal pane can be resized or not. If not then the pane can be draged if resize nodes are added but not resized. 
	 * @return true if the internal pane is allowed to be resized and dragged. False if the pane is only allowed to be dragged. 
	 */
	public boolean isResizable(){
		return this.resizeable;
		
	}

	/**
	 * Create nodes which resize, drag, flip and decorate the pane. 
	 * @return Group of nodes to resize the pane. 
	 */
	public Group createResizeDragNodes(){
		
		//create the new group
		Group resizeNodes=new Group();
		
		/*center drag*/
		Anchor centreDrag= new Anchor(Color.DODGERBLUE, Pos.CENTER);
		centreDrag.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().divide(2)));
		centreDrag.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty().divide(2)));
		Node arrows=Glyph.create("FontAwesome|ARROWS").size(22).color(Color.WHITE); 
		arrows.layoutXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().divide(2).subtract(11)));
		arrows.layoutYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty().divide(2).subtract(13)));


		//first create a set of circle shapes- these will be our drag nodes for resizing. 
		/*Corner Drag Nodes*/
		Anchor topLeft= new Anchor(Color.DODGERBLUE, Pos.TOP_LEFT);
		topLeft.centerXProperty().bind(mainPane.layoutXProperty());
		topLeft.centerYProperty().bind(mainPane.layoutYProperty());
		
		Anchor topRight= new Anchor(Color.DODGERBLUE, Pos.TOP_RIGHT);
		topRight.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		topRight.centerYProperty().bind(mainPane.layoutYProperty());

		
		Anchor bottomLeft= new Anchor(Color.DODGERBLUE, Pos.BOTTOM_LEFT);
		bottomLeft.centerXProperty().bind(mainPane.layoutXProperty());
		bottomLeft.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));
		
		Anchor bottomRight= new Anchor(Color.DODGERBLUE, Pos.BOTTOM_RIGHT);
		bottomRight.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		bottomRight.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));
		
		/*Side Drag Nodes*/
		Anchor bottomCenter= new Anchor(Color.DODGERBLUE, Pos.BOTTOM_CENTER);
		bottomCenter.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().divide(2)));
		bottomCenter.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));
		
		Anchor topCenter= new Anchor(Color.DODGERBLUE, Pos.TOP_CENTER);
		topCenter.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().divide(2)));
		topCenter.centerYProperty().bind(mainPane.layoutYProperty());
		
		Anchor leftCenter= new Anchor(Color.DODGERBLUE, Pos.CENTER_LEFT);
		leftCenter.centerXProperty().bind(mainPane.layoutXProperty());
		leftCenter.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty().divide(2)));
		
		Anchor rightCenter= new Anchor(Color.DODGERBLUE, Pos.CENTER_RIGHT);
		rightCenter.centerXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		rightCenter.centerYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty().divide(2)));
	
		/*Create buttons*/
		PamButton expand=new PamButton();
		expand.setGraphic(Glyph.create("FontAwesome|EXPAND").size(22).color(Color.WHITE));
		expand.layoutXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().subtract(anchorSize*2).subtract(expand.widthProperty())));
		expand.layoutYProperty().bind(mainPane.layoutYProperty().add(10));
		expand.setOnAction((action)->{
			mainPane.setLayoutX(0);
			mainPane.setLayoutY(0);
			mainPane.setPrefWidth(holderPane.getWidth());
			mainPane.setPrefHeight(holderPane.getHeight());
			getPaneScaleFractions();
		});
		expand.getStyleClass().add("button-internal");

		PamButton flip=new PamButton("->");
		flip.setOnAction((action)->{
		});
		flip.layoutXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty().subtract(anchorSize*2).subtract(expand.widthProperty()).subtract(flip.widthProperty())));
		flip.layoutYProperty().bind(mainPane.layoutYProperty().add(10));
		flip.getStyleClass().add("button-internal");

		/*Create decoration lines*/
		Line line1=new Line();
		setLineStyle(line1);
		line1.startXProperty().bind(mainPane.layoutXProperty());
		line1.startYProperty().bind(mainPane.layoutYProperty());		
		line1.endXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		line1.endYProperty().bind(mainPane.layoutYProperty());	
		
		Line line2=new Line();
		setLineStyle(line2);
		line2.startXProperty().bind(mainPane.layoutXProperty());
		line2.startYProperty().bind(mainPane.layoutYProperty());		
		line2.endXProperty().bind(mainPane.layoutXProperty());
		line2.endYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));	
		
		Line line3=new Line();
		setLineStyle(line3);
		line3.startXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		line3.startYProperty().bind(mainPane.layoutYProperty());		
		line3.endXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		line3.endYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));	
		
		Line line4=new Line();
		setLineStyle(line4);
		line4.startXProperty().bind(mainPane.layoutXProperty());
		line4.startYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));		
		line4.endXProperty().bind(mainPane.layoutXProperty().add(mainPane.widthProperty()));
		line4.endYProperty().bind(mainPane.layoutYProperty().add(mainPane.heightProperty()));	
		
//		topLeft.centerXProperty().addListener(change-> {
//			System.out.println("topLeft.getLayoutX() "+topLeft.getLayoutX());
//			Point2D newPoint=this.localToParent(topLeft.getCenterX(), topLeft.getCenterY());
//			this.setLayoutX(newPoint.getX());
//			this.setLayoutY(newPoint.getY());
//
//		});

		/*Note: the order here is important for z - order*/ 
		
		resizeNodes.getChildren().addAll(line1, line2, line3, line4);
		
		if (resizeable){
			//can resize and drag
		resizeNodes.getChildren().addAll(arrows, centreDrag, topLeft, topRight, bottomLeft,bottomRight,
				bottomCenter,topCenter, leftCenter, rightCenter);
		resizeNodes.getChildren().addAll(expand);
		}
		else {
			//can only drag
			resizeNodes.getChildren().addAll(centreDrag);
		}
		
	
		return resizeNodes;	
	}
	
	/**
	 * Set style of line. 
	 * @param line - line t5o set style for. 
	 */
	private void setLineStyle(Line line){
		line.getStyleClass().add("line-internal"); 

//		line.setStroke(Color.DODGERBLUE);
//		line.setStrokeWidth(5);
//		line.getStrokeDashArray().addAll(3.0,7.0,3.0,7.0);
	}
	
	
	/**
	 * 
	 * @param pos - position of drag node
	 * @param oldX
	 * @param oldY
	 * @param newX
	 * @param newY
	 */
	private void resizePane(Pos pos, double oldX, double oldY, double newX, double newY, double mouseX, double mouseY){
		//System.out.println("PamInternalPane: oldX: "+oldX + " oldY: "+oldY+" newX: "+newX+" newY: "+newY);
		switch(pos){		
		case BASELINE_CENTER:
			break;
		case BASELINE_LEFT:
			break;
		case BASELINE_RIGHT:
			break;
		case BOTTOM_CENTER:
			//only Y
			dragBottomY(newY);
			break;
		case BOTTOM_LEFT:
			//note- make sure pref width is used here. 
			//X
			dragLeftX(newX);
			//Y
			dragBottomY(newY);
			break;
		case BOTTOM_RIGHT:
			//X
			dragRightX(newX); 
			//Y
			dragBottomY(newY);
			break;
		case CENTER:
			//just drags entire node around. 
			mainPane.layoutXProperty().setValue(Math.max(0,Math.min(newX,holderPane.getWidth()-anchorSize))-mainPane.getWidth()/2);
			mainPane.layoutYProperty().setValue(Math.max(0,Math.min(newY,holderPane.getHeight()-anchorSize))-mainPane.getHeight()/2);
			break;
		case CENTER_LEFT:
			//only X
			dragLeftX(newX);
			break;
		case CENTER_RIGHT:
			//only X
			dragRightX(newX);
			break;
		case TOP_CENTER:
			//only Y
			dragTopY(newY);
			break;
		case TOP_LEFT:
			//X
			dragLeftX(newX);
			//Y
			dragTopY(newY); 
			break;
		case TOP_RIGHT:
			//X
			dragRightX(newX);
			//Y
			dragTopY(newY);
			break;
		default:
			break;
		}
		
		getPaneScaleFractions();
	}
	
	/*Drag behaviours are replicated in different combinations for each corner*/ 
	
	/**
	 * Dragging from left of pane for x dimension
	 * @param newX - newX.  
	 */
	private void dragLeftX(double newX){
		double oldXRight=mainPane.getLayoutX()+mainPane.getPrefWidth();
		mainPane.layoutXProperty().setValue(getAllowedLayoutX(newX));
		double width=oldXRight-getAllowedLayoutX(newX);
		mainPane.setPrefWidth(Math.max(minDim, width));
	}
	
	private void dragRightX(double newX){
		double width = getAllowedLayoutX(newX)-mainPane.getLayoutX();
		mainPane.setPrefWidth(Math.max(minDim, width));
	}
	
	private void dragTopY(double newY){
		double oldYBottom=mainPane.getLayoutY()+mainPane.getPrefHeight(); 
		mainPane.layoutYProperty().setValue(newY);
		double height=oldYBottom-newY;
		mainPane.setPrefHeight(Math.max(minDim, height));
	}
	
	private void dragBottomY(double newY){
		double height = newY-mainPane.getLayoutY();
		mainPane.setPrefHeight(Math.max(minDim, height));
	}
	
	private double getAllowedLayoutX(double newX){
		if (newX<(-mainPane.getWidth()/2)) return -mainPane.getWidth()/2;
		if (newX>holderPane.getWidth()+mainPane.getWidth()/2) return holderPane.getWidth()+mainPane.getWidth()/2;
		return newX;
	}
	
	public void setPaneSize(double x, double y){
		mainPane.setPrefWidth(Math.max(minDim, x));
		mainPane.setPrefHeight(Math.max(minDim, y));
		getPaneScaleFractions();
	}
	
	public void setPaneLayout(double x, double y) {
		mainPane.setLayoutX(getAllowedLayoutX(x));
		mainPane.setLayoutY(y);
		getPaneScaleFractions();
	}

		
	//a draggable anchor displayed around a point.
	class Anchor extends Circle {

	/**
	 * Position of pane. 
	 */
	private Pos pos;
	
	Anchor(Color color, Pos pos) {
	    super(0, 0, anchorSize);

	    this.pos=pos;
	    this.getStyleClass().add("circle-internal"); 

	    enableDrag();
	  }

	//make a node movable by dragging it around with the mouse.
	  private void enableDrag() {
	    final Delta dragDelta = new Delta();
	    setOnMousePressed(new EventHandler<MouseEvent>() {
	      @Override public void handle(MouseEvent mouseEvent) {
	        // record a delta distance for the drag and drop operation.
	        dragDelta.x = getCenterX() - mouseEvent.getX();
	        dragDelta.y = getCenterY() - mouseEvent.getY();
	        getScene().setCursor(Cursor.MOVE);
	      }
	    });
	    setOnMouseReleased(new EventHandler<MouseEvent>() {
	      @Override public void handle(MouseEvent mouseEvent) {
	        getScene().setCursor(Cursor.HAND);
	      }
	    });
	    setOnMouseDragged(new EventHandler<MouseEvent>() {
	      @Override public void handle(MouseEvent mouseEvent) {
	    	  
	        double newX = mouseEvent.getX() + dragDelta.x;
	        if (newX > 0 && newX < getScene().getWidth()) {
	         // setCenterX(newX);
	        }
	        double newY = mouseEvent.getY() + dragDelta.y;
	        if (newY > 0 && newY < getScene().getHeight()) {
	          //setCenterY(newY);
	        }
	        
        	resizePane( pos,  dragDelta.x,  dragDelta.y,  newX,  newY,mouseEvent.getX(),mouseEvent.getY());
        	
	      }
	    });
	    setOnMouseEntered(new EventHandler<MouseEvent>() {
	      @Override public void handle(MouseEvent mouseEvent) {
	        if (!mouseEvent.isPrimaryButtonDown()) {
	          getScene().setCursor(Cursor.HAND);
	        }
	      }
	    });
	    setOnMouseExited(new EventHandler<MouseEvent>() {
	      @Override public void handle(MouseEvent mouseEvent) {
	        if (!mouseEvent.isPrimaryButtonDown()) {
	          getScene().setCursor(Cursor.DEFAULT);
	        }
	      }
	    });
	  }
	//records relative x and y co-ordinates.
	  private class Delta { double x, y; }

	}
	
	/**
	 * Get the pane which is to be resized, the internal pane. 
	 * @return the resizable region.
	 */
	public Region getInternalRegion(){
		return mainPane;
	}
	
	/**
	 * Set location of the internal pane within its holder. 
	 * @param x x pixel position within holder	
	 * @param y y pixel location within holder. 
	 */
	public void setLocation(double x, double y){
		mainPane.setLayoutX(x);
		mainPane.setLayoutY(y);
	}
	
	/**
	 * Set size of the internal pane within its holder. 
	 * @param w - width to set the pane to. 
	 * @param h - height to set pane to. 
	 */
	public void setSize(double w, double h) {
		mainPane.setPrefWidth(w);
		mainPane.setPrefHeight(h);
		
	}
}
