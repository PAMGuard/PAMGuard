package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * A custom scroll bar. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ScrollBarPane extends PamBorderPane {
	
	/**
	 * Have dsiplay units in ms instead of seconds. 
	 */
	private boolean showMillis = false; 
	

	/**
	 * Boolean property for dragging. True if dragging is occurred. 
	 */
	public BooleanProperty dragObserver= new SimpleBooleanProperty(false);
	
	/**
	 * The % amount the scroll bar move relative to visible range if the scroll pane.
	 */
	private double scrollClickIncrement = 0.8;


	//scroll bar data

	/**
	 * The minimum possible value of the scroll bar
	 */
	private DoubleProperty minValueProperty = new SimpleDoubleProperty(0); 

	/*
	 * Maximum possible value of the scroll bar. This the maximum possible currentValueProperty +  visibleAmountProperty
	 */
	private DoubleProperty maxValueProperty = new SimpleDoubleProperty(100); 

	/**
	 * Amount the scroll bar moves on an arrow click or click inside the
	 * scroll bar pane. 
	 */
	private DoubleProperty blockIncrementProperty =new SimpleDoubleProperty(2); 

	/**
	 * The position of the scroll bar. This the position of the left/bottom of the actual scroll bar. 
	 */
	private DoubleProperty currentValueProperty=new SimpleDoubleProperty(0);

	/**
	 * The visible amount the scroll bar represents. 
	 */
	private DoubleProperty visibleAmountProperty=new SimpleDoubleProperty(15);


	/**
	 * True if the scroller is being moved or visible range is changing. 
	 */
	private BooleanProperty isChanging = new SimpleBooleanProperty(false); 


	/**
	 * Pane which holds the scroll bar
	 */
	private Pane scrollBarPane;

	//values needed to be held for dragging
	private double dragX;

	private double leftLayoutX; 

	private double rightLayoutX;

	/**
	 * The scrollBar. The bit which actually moves. 
	 */
	private Pane rectangle;

	/*
	 *Canvas for drawing stuff on the scroll bar;  
	 */
	private Canvas drawCanvas; 

	/**
	 * Text field to enter visible time manually. 
	 */
	private ScrollTextBox textBox;


	/**
	 * Constructor to create a scroll bar pane. 
	 */
	public ScrollBarPane(){
		createScrollBarPane();
		//scrollBarPane.setStyle("-fx-background-color: pink"); //TODO- TEMP
		this.setCenter(scrollBarPane);
	}


	/**
	 * Create the scroll bar pane. Contains a resizable rectangle which can be dragged within the pane.
	 */
	private void createScrollBarPane(){

		scrollBarPane=new Pane(); 

		//create the rectangle which allows users to drag
		rectangle=createDragRectangle();


		//create the draw canvas. 
		drawCanvas=createDrawCanvas();

		//set up listeners for the scroll bar properties. Ensures layout changes in response to 
		//changes in property values. 
		setPropertyListeners();

		//make sure the rectangle shows on start up/.
		scrollBarPane.widthProperty().addListener((obsVal, oldVal, newVal)->{
			layoutRectangle();
		});

		scrollBarPane.heightProperty().addListener((obsVal, oldVal, newVal)->{
			layoutRectangle();
		});

		scrollBarPane.layoutBoundsProperty().addListener((obsVal, oldVal, newVal)->{
			layoutRectangle();
		});

		//if the mouse is clicked somewhere on the scroll pane then the scroll bar needs to move. 
		scrollBarPane.setOnMouseClicked((event)->{
			double newlayout; 
			//PamUtilsFX.nodeFlashEffect(rectangle, PamUtilsFX.addColorTransparancy(Color.WHITE, 0.5), 20d, 0.1);
			if (event.getX()<rectangle.getLayoutX()){
				newlayout=rectangle.getLayoutX()-rectangle.getWidth()*scrollClickIncrement; 
				rectangle.setLayoutX(Math.max(newlayout, 0));
			}
			else if (event.getX()>rectangle.getLayoutX()+rectangle.getWidth()){
				newlayout=rectangle.getLayoutX()+rectangle.getWidth()*scrollClickIncrement; 
				rectangle.setLayoutX(Math.min(newlayout, scrollBarPane.getWidth()-rectangle.getWidth()));
			}
			currentValueProperty.setValue(calcScrollBarVal(rectangle.getLayoutX()));
		});

		createTextField();

		scrollBarPane.getChildren().add(drawCanvas); 
		scrollBarPane.getChildren().add(rectangle); 
		scrollBarPane.getChildren().add(textBox); 




	}

	FadeTransition ft ;
	/**
	 * Set the text box visible. If false then the text box fades away. 
	 * @param visible
	 */
	private void setTextBoxVisible(boolean visible){
		if (visible){
			ft.stop();
			textBox.setOpacity(1);
		}
		else {
			ft.setFromValue(1.0);
			ft.setToValue(0.1);
			ft.setDelay(Duration.millis(2000));
			//ft.setCycleCount(Timeline.INDEFINITE);
			//ft.setAutoReverse(true);
			ft.play();
			ft.setOnFinished((action)->{
				//bit of a hack to defocus the text field. 
				rectangle.requestFocus();
			});
		}
	}

	/**
	 * Animation to show the user has a wrong value in the text box 
	 */
	private void  textBoxErrorFlash(Node textBox){

		PamUtilsFX.nodeFlashEffect(textBox, Color.RED, 20d, 0.3 );


		//			ColorInput effect = new ColorInput(0, 0, textBox.getWidth(), textBox.getHeight(), Paint.valueOf("#FFDDDD"));
		//			Timeline flash = new Timeline(
		//					  new KeyFrame(Duration.seconds(0.4), new KeyValue(effect.paintProperty(), Color.RED)),
		//					  new KeyFrame(Duration.seconds(0.8), new KeyValue(effect.paintProperty(), Paint.valueOf("#E0DDDD"))),
		//					  new KeyFrame(Duration.seconds(1.0), new KeyValue(effect.paintProperty(), Paint.valueOf("#DDDDDD"))));

		//			DropShadow shadow = new DropShadow();
		//			shadow.setColor(Color.RED);
		//			shadow.setSpread(0.5);
		//
		//			Timeline shadowAnimation = new Timeline(
		//					new KeyFrame(Duration.ZERO, new KeyValue(shadow.radiusProperty(), 0d)),
		//					new KeyFrame(Duration.seconds(0.3), new KeyValue(shadow.radiusProperty(), 20d)));
		//			shadowAnimation.setAutoReverse(true);
		//			shadowAnimation.setCycleCount(2);
		//
		//
		//			textBox.setEffect(shadow);
		//			shadowAnimation.setOnFinished(e -> textBox.setEffect(null));
		//			shadowAnimation.play();
	}



	/**
	 * Correctly sets the drag rectangle. 
	 */
	public void layoutRectangle(){
		double newWidth=calcPixelValue((visibleAmountProperty.doubleValue()+minValueProperty.get()));
		//			System.out.println("NEW WIDTH: " +newWidth + " visibleAmountProperty: "+visibleAmountProperty.doubleValue() + 
		//					" +minValueProperty: "  +minValueProperty.get() + " currentValueProperty: " + currentValueProperty.doubleValue());
		if (newWidth <= scrollBarPane.getWidth()){ //must be less than or equal to or the rectangle does not go to maximum of scroll bar. 
			rectangle.setPrefWidth(calcPixelValue((visibleAmountProperty.doubleValue()+minValueProperty.get())));
			rectangle.setLayoutX(calcPixelValue(currentValueProperty.doubleValue()));
		}
		//			rectangle.
	}

	/**
	 * Create the canvas which allows acoustic information to be drawn 
	 * @return  the canvas to draw datagram data on. 
	 */
	private Canvas createDrawCanvas() {
		Canvas canvas=new Canvas(); 
		canvas.widthProperty().bind(scrollBarPane.widthProperty());
		canvas.heightProperty().bind(scrollBarPane.heightProperty());
		return canvas; 
	}

	/**
	 * Create the rectangle which can be dragged to change time but also dragged to change the width of time
	 * shown. 
	 * @return a rectangle which can be dragged and changed size within the scroll bar pane. 
	 */
	private Pane createDragRectangle(){

		double dragWidth=5; 
		double lineInset=5; 
		double minPixelWidth=10; 

		String cursorColour = "-fx-background-color: rgba(255, 255, 255, 0.5)";

		Pane rectangle=new Pane(); 
		//rectangle.setStrokeWidth(10);
		rectangle.layoutYProperty().setValue(0);
		rectangle.prefHeightProperty().bind(scrollBarPane.heightProperty());
		rectangle.setStyle(cursorColour);
		rectangle.setPrefWidth(100);

		//create two panes either side of this pane. 
		Pane leftDrag=new Pane();
		leftDrag.setCursor(Cursor.W_RESIZE);
		leftDrag.prefHeightProperty().bind(rectangle.heightProperty());
		//the layout of the left rectangle is 0,0;
		leftDrag.setPrefWidth(dragWidth);
		leftDrag.setStyle(cursorColour); 

		//add line decoration to left drag. 
		Line leftdragLine=new Line(); 
		leftdragLine.startXProperty().bind(leftDrag.widthProperty().divide(2));
		leftdragLine.startYProperty().setValue(5);
		leftdragLine.endXProperty().bind(leftDrag.widthProperty().divide(2));
		leftdragLine.endYProperty().bind(leftDrag.heightProperty().subtract(lineInset));

		leftDrag.getChildren().add(leftdragLine); 
		rectangle.getChildren().add(leftDrag); 

		Pane rightDrag=new Pane();
		rightDrag.setCursor(Cursor.E_RESIZE);
		rightDrag.prefHeightProperty().bind(rectangle.heightProperty());
		rightDrag.layoutXProperty().bind(rectangle.widthProperty().subtract(rightDrag.widthProperty()));
		rightDrag.setPrefWidth(dragWidth);
		rightDrag.setStyle(cursorColour); 
		//add line decoration to left drag. 
		Line rightdragLine=new Line(); 
		rightdragLine.startXProperty().bind(rightDrag.widthProperty().divide(2));
		rightdragLine.startYProperty().setValue(5);
		rightdragLine.endXProperty().bind(rightDrag.widthProperty().divide(2));
		rightdragLine.endYProperty().bind(rightDrag.heightProperty().subtract(lineInset));

		rightDrag.getChildren().add(rightdragLine); 
		rectangle.getChildren().add(rightDrag); 

		rectangle.setCursor(Cursor.OPEN_HAND); //Change cursor to hand

		//now set behaviours 
		leftDrag.setOnMousePressed((event)->{
			rightLayoutX=rectangle.getLayoutX()+rectangle.getWidth();
			isChanging.set(true);
			dragStarted(event, leftDrag);
		});

		leftDrag.setOnMouseReleased((event)->{
			isChanging.set(false);
			dragging(event); 
		});

		//left drag
		leftDrag.setOnMouseDragged((event)->{

			isChanging.set(true);

			Point2D newPos=scrollBarPane.sceneToLocal(event.getSceneX(), event.getSceneY());
			double dragPosX=newPos.getX();

			double widthVal=rectangle.getWidth();
			double currentVal=rectangle.getLayoutX();
			//only move rectangle if the mouse has started dragging from inside. 
			if (dragPosX-dragX<0){
				//don;t let the drag go past left
				currentVal=0;
			}
			else if (dragPosX-dragX>scrollBarPane.getWidth()-minPixelWidth) {
				//don;t let the drag 
				//System.out.println("AcousticScrollerFX: 2 widthVal: "+widthVal+ " currentVal: "+(dragPosX-dragX)+ " rightLayoutX "+rightLayoutX); 
				widthVal=minPixelWidth;
				currentVal=scrollBarPane.getWidth()-widthVal;

			}
			else{
				//System.out.println("AcousticScrollerFX: 1 widthVal: "+widthVal+ " currentVal: "+(dragPosX-dragX)+ " rightLayoutX "+rightLayoutX); 
				widthVal=rightLayoutX-(dragPosX-dragX);
				currentVal=dragPosX-dragX;
			}

			//the width cannot be less than 1 pixel
			widthVal=Math.max(minPixelWidth, widthVal);

			double visAmount=calcScrollBarVal(widthVal)-this.minValueProperty.get(); 
			//set the visible amount property; 
			this.visibleAmountProperty.setValue(visAmount);
			this.currentValueProperty.setValue(calcScrollBarVal(currentVal));

			//need to consume the event so does not pass through node. 
			event.consume();
		}); 

		//now set behaviours 
		rightDrag.setOnMousePressed((event)->{			
			leftLayoutX=rectangle.getLayoutX();
			isChanging.set(true);
			rectangle.setCursor(Cursor.CLOSED_HAND); //Change cursor to hand
			dragStarted(event, rightDrag);
		});

		rightDrag.setOnMouseReleased((event)->{
			currentValueProperty.setValue(calcScrollBarVal(rectangle.getLayoutX()));
			isChanging.set(false);
			rectangle.setCursor(Cursor.OPEN_HAND); //Change cursor to hand
			dragging(event); 
		});

		//right drag handle
		rightDrag.setOnMouseDragged((event)->{
			isChanging.set(true);
			Point2D newPos=scrollBarPane.sceneToLocal(event.getSceneX(), event.getSceneY());
			double dragPosX=newPos.getX();

			double widthVal;

			if ((dragPosX-leftLayoutX)<leftDrag.getWidth()){
				widthVal=minPixelWidth;
			}
			else if (dragPosX>scrollBarPane.getWidth()){
				widthVal=scrollBarPane.getWidth()-leftLayoutX; 
			}
			else{
				widthVal=dragPosX+dragX-leftLayoutX;
			}

			widthVal=Math.max(minPixelWidth, widthVal);

			//	        	System.out.println("AcousticScrollerFX: visbleAmount: min: "+minValueProperty.get()+
			//	        			" max: "+maxValueProperty.get() + " visble: "+calcScrollBarVal(rectangle.getWidth())+
			//	        			" rectange width: "+rectangle.getWidth());

			//set the visible amount- the width of the rectangle is changed in the property listener. 
			//				System.out.println("AcousticScrollerFX: calcScrollBarVal(widthVal) " + calcScrollBarVal(widthVal)
			//					+  " this.minValueProperty.get() " + this.minValueProperty.get() + " this.maxValueProperty.get() " + this.maxValueProperty.get());
			double visAmount=calcScrollBarVal(widthVal)-this.minValueProperty.get(); 
			this.visibleAmountProperty.setValue(visAmount);
			//need to consume the event so does not pass through node. 
			event.consume();
		}); 


		//the rectangle itself. 
		rectangle.setOnMousePressed((event)->{
			rectanglePressed(event);
		});


		rectangle.setOnMouseReleased((event)->{
			rectangleReleased(event);

		});

		rectangle.setOnMouseDragged((event)->{
			rectangleDragged(event);
		});
		
		
		rectangle.setOnMouseDragReleased((event)->{
			isChanging.set(false);
		});

		
	

		return rectangle; 
	}


	private void rectanglePressed(MouseEvent event){
		isChanging.set(true);
		dragStarted(event, rectangle);
		dragging(event); 
	}


	private void rectangleReleased(MouseEvent event){
		isChanging.set(false);
		dragging(event); 
	}


	private void rectangleDragged(MouseEvent event){
		isChanging.set(true);
		dragging(event); 
		Point2D newPos=scrollBarPane.sceneToLocal(event.getSceneX(), event.getSceneY());
		double dragPosX=newPos.getX();
		//only move rectangle if the mouse has started dragging from inside. 
		//move the rectangle
		if (dragPosX-dragX<0) rectangle.setLayoutX(0);
		else if (dragPosX-dragX>scrollBarPane.getWidth()-rectangle.getWidth()) rectangle.setLayoutX(scrollBarPane.getWidth()-rectangle.getWidth());
		else rectangle.setLayoutX(dragPosX-dragX);

		//now must set the scroll bar value. 
		currentValueProperty.setValue(calcScrollBarVal(rectangle.getLayoutX()));
		event.consume(); 
	}

	/**
	 * Set the value in the text box
	 * @param visAmount - the visible amount
	 */
	private void setTextBoxValue(double visAmount) {
		textBox.setTextBoxMillis(visAmount);
	}

	/**
	 * Create the text field that allows to manually e the visible amount amount property. 
	 */
	private void createTextField(){
		//create the textbox
		textBox= new ScrollTextBox();
		textBox.layoutXProperty().bind(rectangle.layoutXProperty().add(rectangle.widthProperty().divide(2)).subtract(textBox.widthProperty().divide(2)));
		textBox.layoutYProperty().bind(rectangle.heightProperty().divide(2).subtract(textBox.heightProperty().divide(2)));
		textBox.getTextBox().setOnAction((action)-> {
			
			double millis=textBox.getTextBoxDuration().doubleValue();
			if (millis<=0 || millis>(this.maxValueProperty.get()-this.minValueProperty.get())){
				textBoxErrorFlash(textBox);
				this.setTextBoxValue(visibleAmountProperty.get());
			}
			else{
				visibleAmountProperty.setValue(millis);
			}
	
		});
	
		ft = new FadeTransition(Duration.millis(3000), textBox);
	
		textBox.getStyleClass().add("text_field_trans");
		textBox.setPrefWidth(70);
	
		//the rectangle itself. 
		textBox.setOnMousePressed((event)->{
			rectanglePressed(event);
		});
	
	
		textBox.setOnMouseReleased((event)->{
			rectangleReleased(event);
		});
	
		//text box needs to drag the rectangle so there isn't a drag 'dead space' 
		textBox.setOnMouseDragged((event)->{
			rectangleDragged(event);
		});
		
		
		textBox.setOnMouseDragReleased((event)->{
			isChanging.set(false);
		});
		

		textBox.getTextBox().setOnMouseReleased((event)->{
			rectangleReleased(event);
		});
		
		
		textBox.getTextBox().setOnMouseDragged((event)->{
			rectangleDragged(event);
		});
		
		textBox.getTextBox().setOnMouseDragReleased((event)->{
			isChanging.set(false);
		});
		
		
		//Text and visibility animations
	
		textBox.setOnMouseEntered((event)->{
			setTextBoxVisible(true);
		}); 
	
		textBox.setOnMouseExited((event)->{
			//only set invisible if not in rectangle. This is for fast mouse movements were the exist of the rectangle may not be called 
			if (!rectangle.contains(rectangle.sceneToLocal(event.getSceneX(), event.getSceneY()))){
				setTextBoxVisible(false);
			}
		}); 
	
		setTextBoxVisible(false); 
	
		//show and hide text box so keeps the scroll bar beautiful 
		rectangle.setOnMouseEntered((event)->{
			setTextBoxVisible(true);
		});
	
		rectangle.setOnMouseExited((event)->{
			setTextBoxVisible(false);
		});
	
		//make sure the text box chnages with visible amount.,
		this.visibleAmountProperty.addListener((obsVal, newVal, oldVal)->{
			setTextBoxValue(visibleAmountProperty.get());
		});
	
	
		setTextBoxValue(visibleAmountProperty.get());
	}


	/**
	 * Get the text box that shows the visible amount
	 * @return - the text field
	 */
	public ScrollTextBox getScrollBox() {
		return textBox;
	}


	/**
	 * Move the rectangle in the scroll bar pane by an increment which is a percentage of the overal width. 
	 * @param scrollArrowIncrement
	 */
	public void moveScrollRectangle(double scrollArrowIncrement) {


		double xMove=rectangle.getLayoutX()+rectangle.getWidth()*scrollArrowIncrement; 
		if (xMove<0) xMove=0; 
		if (xMove+rectangle.getWidth()>scrollBarPane.getWidth()) xMove= scrollBarPane.getWidth()-rectangle.getWidth();

		rectangle.setLayoutX(xMove);


		currentValueProperty.setValue(calcScrollBarVal(rectangle.getLayoutX()));

		//System.out.println("scrollArrowIncrement: " + scrollArrowIncrement + " layout x: " + rectangle.getLayoutX() + " currentValueProperty: " + currentValueProperty.get()); 

	}


	/**
	 * Set up listeners to deal with any changes if minVal, maxVal, visibleAmount or currentVal properties are changed
	 */
	private void setPropertyListeners(){

		//TODO- need to get these working properly. 

		//add listener to visible amount property.
		visibleAmountProperty.addListener((obsVal, oldVal, newVal)->{
			//rectangle.setPrefWidth(calcPixelValue((newVal.doubleValue()+minValueProperty.get())));
			layoutRectangle();
		});

		//add listener to current value amount property.
		currentValueProperty.addListener((obsVal, oldVal, newVal)->{
			//				System.out.println("New millis current value: "+calcPixelValue(newVal.doubleValue())+" original val: "+newVal.doubleValue());
			//rectangle.setLayoutX(calcPixelValue(newVal.doubleValue()));
			layoutRectangle();
		});

		//add listener to minimum value property
		minValueProperty.addListener((obsVal, oldVal, newVal)->{
			//				System.out.println("New millis current value: "+calcPixelValue(newVal.doubleValue())+" original val: "+newVal.doubleValue());
			//rectangle.setLayoutX(calcPixelValue(newVal.doubleValue()));
			layoutRectangle();
		});

		//add listener to maximum value property
		maxValueProperty.addListener((obsVal, oldVal, newVal)->{
			//				System.out.println("New millis current value: "+calcPixelValue(newVal.doubleValue())+" original val: "+newVal.doubleValue());
			//rectangle.setLayoutX(calcPixelValue(newVal.doubleValue()));
			layoutRectangle();
		});
	}


	private void dragging(MouseEvent event) {
		dragObserver.setValue(event.isDragDetect());
	}

	/**
	 * Called whenever a drag is started- e.g. when mouse is pressed. Need to record where in the rectangle the mouise click happened. 
	 * Otherwise drag always ends up at (0,0)
	 * @param event- the mouse event. 
	 */
	public void dragStarted(MouseEvent event, Node node){
		//need to do scene converstion and back so that controls with the rectangle can also drag the rectangle...
		Point2D newPos=node.sceneToLocal(event.getSceneX(), event.getSceneY());
		if (newPos.getX()>=0 && newPos.getX()<scrollBarPane.getWidth()){
			// record a delta distance for the drag and drop operation.
			dragX = newPos.getX();
			//need to consume so does not pass to underlying node. 
			event.consume();
		}
	}

	/**
	 * Calculate the corresponding value of the scroll bar based on min/max values. 
	 * @param pixelLocation - the location on the scroll bar in pixels. 
	 * @return the scroll bar value corresponding to the pixel location
	 */
	private double calcScrollBarVal(double pixelLocation){
		double fraction=pixelLocation/scrollBarPane.getWidth(); 
		return minValueProperty.get()+(fraction*(maxValueProperty.get()-minValueProperty.get()));
	}

	/**
	 * Calculate a pixel value based on a scroll bar value. 
	 * @param scrollBarValue the value of the scroll bar to calculate the pixel location for. 
	 * @return the pixel location corresponding to the scroll bar value
	 */
	private double calcPixelValue(double scrollBarValue){
		double frac=(scrollBarValue-minValueProperty.get())/(this.maxValueProperty.get()-this.minValueProperty.get());
		return frac*scrollBarPane.getWidth();
	}

	/**
	 * Get the draw canvas; 
	 * @return
	 */
	public Canvas getDrawCanvas() {
		return drawCanvas;
	}

	public Pane getScrollRectangle(){
		return this.rectangle;
	}

	/**
	 * Set the draw canvas
	 */
	public void setDrawCanvas(Canvas drawCanvas) {
		this.drawCanvas = drawCanvas;
	}

	/**
	 * The minimum value of the scroll pane. i.e. the minimum possible value of the current value. . 
	 * @return the minimum possible value of the scroll pane. 
	 */
	public double getMinVal() {
		return minValueProperty.get();
	}

	public void setMinVal(double minVal) {
		this.minValueProperty.setValue(minVal);
	}

	public double getMaxVal() {
		return maxValueProperty.get();
	}

	public void setMaxVal(double maxVal) {
		this.maxValueProperty.setValue(maxVal);
	}

	public double getBlockIncrement() {
		return blockIncrementProperty.getValue();
	}

	public void setBlockIncrement(double blockIncrement) {
		this.blockIncrementProperty.setValue(blockIncrement);
	}

	public double getCurrentValue() {
		return currentValueProperty.getValue();
	}

	public void setCurrentValue(double currentValue) {
		if (currentValue>this.getMaxVal())  this.currentValueProperty.setValue(getMaxVal());
		else this.currentValueProperty.setValue(currentValue);
	}

	public DoubleProperty currentValueProperty() {
		return this.currentValueProperty;
	}

	public double getVisibleAmount() {
		return visibleAmountProperty.get();
	}

	public void setVisibleAmount(double visibleAmount) {
		this.visibleAmountProperty.setValue(visibleAmount);
	}

	public DoubleProperty visibleAmountProperty() {
		return this.visibleAmountProperty;
	}

	/**
	 * The changing property. True if the scroll rectangle is moving. 
	 * @return true if the scroll rectangle is moving. 
	 */
	public BooleanProperty isChangingProperty() {
		return this.isChanging; 
	}
	
	
	/**
	 * Get the minimum possible value of the scroll bar in milliseoncss
	 */
	public DoubleProperty getMinValueProperty() {
		return minValueProperty;
	}

	/**
	 * Set the minimum possible value of the scroll bar in milliseoncds
	 * @param - the minimum value property
	 */
	public void setMinValueProperty(DoubleProperty minValueProperty) {
		this.minValueProperty = minValueProperty;
	}

	/*
	 * Get maximum possible value of the scroll bar. This the maximum possible currentValueProperty +  visibleAmountProperty
	 */
	public DoubleProperty getMaxValueProperty() {
		return maxValueProperty;
	}

	/*
	 * Set maximum possible value of the scroll bar. This the maximum possible currentValueProperty +  visibleAmountProperty
	 * @param - maximum value to set. 
	 */
	public void setMaxValueProperty(DoubleProperty maxValueProperty) {
		this.maxValueProperty = maxValueProperty;
	}


	/**
	 * Property indicating that the scroll bar is moving. 
	 * @return - indicates the scroll bar is moving. 
	 */
	public BooleanProperty scrollMovingProperty() {
		return this.scrollMovingProperty();
	}
	
	/**
	 * Check whether the scroll bar's default <b>display</b> units are millis 
	 * (note that stored units for calculations always remain milliseconds)
	 * @return true if the display units are millis
	 */
	public boolean isShowMillis() {
		return textBox.isShowMillis();
	}

	/**
	 * Set whether the scroll bar's default <b>display</b> units to milliseconds
	 * (note that stored units for calculations always remain milliseconds)
	 * @param true if the display units are millis
	 */
	public void setShowMillis(boolean showMillis) {
		this.textBox.isShowMillis();
	}


	/**
	 * Convenience function which adds a change listener to the current value and visible amount prooperty. 
	 * @param val - the change listener to add. 
	 */
	public void addValueListener(ChangeListener val) {
		//add listener to visible amount property.
		visibleAmountProperty.addListener(val);

		//add listener to current value amount property.
		currentValueProperty.addListener(val);
	}


	public void showVisibleRangeButton(boolean b) {
		this.textBox.setRangeButtonVisible(b);
		
	}

}

