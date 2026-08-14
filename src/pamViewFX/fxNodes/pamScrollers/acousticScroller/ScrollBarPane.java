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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pamViewFX.fxGlyphs.PamGlyphDude;
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
	 * Style class giving the scroller the plot window colour of the current colour
	 * scheme.
	 */
	public static final String SCROLLER_PANE_STYLE_CLASS = "pam-scroller-pane";

	/**
	 * Style class for the draggable visible-range rectangle.
	 */
	public static final String SCROLLER_BODY_STYLE_CLASS = "pam-scroller-body";

	/**
	 * Style class for the grab handles at each end of the visible-range rectangle.
	 */
	public static final String SCROLLER_HANDLE_STYLE_CLASS = "pam-scroller-handle";

	/**
	 * Style class for the grip lines drawn on the drag handles.
	 */
	public static final String SCROLLER_GRIP_STYLE_CLASS = "pam-scroller-grip";

	/**
	 * Style class for the drag icon which appears on top of the visible-range
	 * rectangle when the rectangle is too narrow to grab.
	 */
	public static final String SCROLLER_DRAG_ICON_STYLE_CLASS = "pam-scroller-drag-icon";

	/**
	 * Style class for the glyph drawn inside the drag icon.
	 */
	public static final String SCROLLER_DRAG_GLYPH_STYLE_CLASS = "pam-scroller-drag-glyph";

	/**
	 * The width, in pixels, of the resize handles at either end of the rectangle.
	 */
	private static final double DRAG_HANDLE_WIDTH = 8;

	/**
	 * The minimum width, in pixels, of the visible range rectangle.
	 */
	private static final double MIN_PIXEL_WIDTH = 10;

	/**
	 * The width, in pixels, of the drag icon which appears on a narrow rectangle.
	 */
	private static final double DRAG_ICON_WIDTH = 14;

	/**
	 * The height, in pixels, of the drag icon. Kept short so that the icon floats
	 * above the text box instead of sitting on top of the range it is showing.
	 */
	private static final double DRAG_ICON_HEIGHT = 13;

	/**
	 * The inset, in pixels, of the drag icon from the top of the scroll bar.
	 */
	private static final double DRAG_ICON_INSET = 1;

	/**
	 * The number of pixels the mouse must move away from the press position before a
	 * press on one of the resize handles counts as a drag. The handles are only a few
	 * pixels wide so it is very easy to wobble the mouse by a pixel or two whilst
	 * clicking on one - without this a click would resize the visible range to
	 * whatever the rectangle happens to measure on screen.
	 */
	private static final double DRAG_THRESHOLD = 3;


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

	/**
	 * The position, in scroll bar pane co-ordinates, at which a resize handle was
	 * pressed. Resize drags work in deltas from this position so that the rectangle
	 * does not jump when a drag begins.
	 */
	private double pressPosX;

	/**
	 * The layout x of the rectangle when a resize handle was pressed.
	 */
	private double pressLayoutX;

	/**
	 * The width of the rectangle when a resize handle was pressed.
	 */
	private double pressWidth;

	/**
	 * True once a press on a resize handle has moved far enough to count as a drag.
	 * Until it does the visible range is left alone.
	 */
	private boolean handleDragging = false;

	/**
	 * The scrollBar. The bit which actually moves.
	 */
	private Pane rectangle;

	/**
	 * Icon shown on top of the rectangle when it is too narrow to grab, so that the
	 * user has something obvious to drag.
	 */
	private Pane dragIcon;

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
		//the outer pane takes the plot window colour as well as the inner one: callers
		//pad this pane (the detection display insets it by 5px top and bottom), and
		//without a background of its own those strips show whatever is behind.
		this.getStyleClass().add(SCROLLER_PANE_STYLE_CLASS);
		this.setCenter(scrollBarPane);
	}


	/**
	 * Create the scroll bar pane. Contains a resizable rectangle which can be dragged within the pane.
	 */
	private void createScrollBarPane(){

		scrollBarPane=new Pane();
		//gives the scroller the plot window colour of the current colour scheme. Without
		//it the pane has no background of its own and shows whatever is behind, which
		//stayed light in the dark and night schemes.
		scrollBarPane.getStyleClass().add(SCROLLER_PANE_STYLE_CLASS);

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
			/*
			 * Only clicks on the trough itself (the background canvas or the pane) page the
			 * scroller. Clicks which land on the scroller controls - the rectangle, its
			 * resize handles, the text box or the drag icon - are the start of a
			 * press/drag of the scroller and must not also move it. This matters because
			 * the text box and drag icon overhang a narrow rectangle, so without this a
			 * click on them jumped the scroller a page to the left or right.
			 */
			if (event.getTarget()!=scrollBarPane && event.getTarget()!=drawCanvas) {
				return;
			}
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

		//the drag icon must be created after the text box - it only shows when the
		//rectangle is narrower than the text box.
		dragIcon=createDragIcon();

		scrollBarPane.getChildren().add(drawCanvas);
		scrollBarPane.getChildren().add(rectangle);
		scrollBarPane.getChildren().add(textBox);
		//added last so the icon sits on top of the (mostly transparent) text box.
		scrollBarPane.getChildren().add(dragIcon);




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

		double dragWidth=DRAG_HANDLE_WIDTH;
		double lineInset=4;

		/*
		 * The body and handle colours come from the style sheet rather than being set
		 * here, so that they can be inverted for the dark and night schemes - a
		 * translucent dark grey body is invisible on a dark scroller.
		 */

		Pane rectangle=new Pane();
		//rectangle.setStrokeWidth(10);
		rectangle.layoutYProperty().setValue(0);
		rectangle.prefHeightProperty().bind(scrollBarPane.heightProperty());
		rectangle.getStyleClass().add(SCROLLER_BODY_STYLE_CLASS);
		rectangle.setPrefWidth(100);

		//create two panes either side of this pane.
		Pane leftDrag=new Pane();
		leftDrag.setCursor(Cursor.W_RESIZE);
		leftDrag.prefHeightProperty().bind(rectangle.heightProperty());
		//the layout of the left rectangle is 0,0;
		leftDrag.setPrefWidth(dragWidth);
		leftDrag.getStyleClass().add(SCROLLER_HANDLE_STYLE_CLASS);

		//add grip line decoration to left drag.
		addGripLines(leftDrag, lineInset);
		rectangle.getChildren().add(leftDrag);

		Pane rightDrag=new Pane();
		rightDrag.setCursor(Cursor.E_RESIZE);
		rightDrag.prefHeightProperty().bind(rectangle.heightProperty());
		rightDrag.layoutXProperty().bind(rectangle.widthProperty().subtract(rightDrag.widthProperty()));
		rightDrag.setPrefWidth(dragWidth);
		rightDrag.getStyleClass().add(SCROLLER_HANDLE_STYLE_CLASS);
		//add grip line decoration to right drag.
		addGripLines(rightDrag, lineInset);
		rectangle.getChildren().add(rightDrag);

		rectangle.setCursor(Cursor.OPEN_HAND); //Change cursor to hand

		/*
		 * Note that neither pressing nor releasing a resize handle changes the visible
		 * range - only an actual drag does (see handleDrag). Pressing a handle used to
		 * set the range from the measured width of the rectangle, which at small visible
		 * ranges is the width of the two handles rather than the width the range really
		 * represents, so a click on a handle jumped the range.
		 */
		leftDrag.setOnMousePressed((event)->{
			handlePressed(event);
		});

		leftDrag.setOnMouseReleased((event)->{
			handleReleased(event);
		});

		//left drag
		leftDrag.setOnMouseDragged((event)->{
			if (!handleDrag(event)) return;

			isChanging.set(true);

			//work in deltas from where the handle was pressed, using the geometry recorded
			//at the press, so that the rectangle does not jump as the drag starts.
			double newLayoutX=pressLayoutX+(dragPosX(event)-pressPosX);
			double rightEdge=pressLayoutX+pressWidth;

			double widthVal;
			double currentVal;
			if (newLayoutX<0){
				//don;t let the drag go past left
				currentVal=0;
				widthVal=rightEdge;
			}
			else if (newLayoutX>scrollBarPane.getWidth()-MIN_PIXEL_WIDTH) {
				//don;t let the drag go past the right hand end of the pane
				widthVal=MIN_PIXEL_WIDTH;
				currentVal=scrollBarPane.getWidth()-widthVal;
			}
			else{
				widthVal=rightEdge-newLayoutX;
				currentVal=newLayoutX;
			}

			//the width cannot be less than the minimum
			widthVal=Math.max(minDragWidth(), widthVal);

			double visAmount=calcScrollBarVal(widthVal)-this.minValueProperty.get();
			//set the visible amount property;
			this.visibleAmountProperty.setValue(visAmount);
			this.currentValueProperty.setValue(calcScrollBarVal(currentVal));

			//need to consume the event so does not pass through node.
			event.consume();
		});

		//now set behaviours
		rightDrag.setOnMousePressed((event)->{
			rectangle.setCursor(Cursor.CLOSED_HAND); //Change cursor to hand
			handlePressed(event);
		});

		rightDrag.setOnMouseReleased((event)->{
			rectangle.setCursor(Cursor.OPEN_HAND); //Change cursor to hand
			handleReleased(event);
		});

		//right drag handle
		rightDrag.setOnMouseDragged((event)->{
			if (!handleDrag(event)) return;

			isChanging.set(true);

			//as for the left handle, the new width is the width at the press plus however
			//far the mouse has moved since.
			double widthVal=pressWidth+(dragPosX(event)-pressPosX);

			if (pressLayoutX+widthVal>scrollBarPane.getWidth()){
				widthVal=scrollBarPane.getWidth()-pressLayoutX;
			}

			widthVal=Math.max(minDragWidth(), widthVal);

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


	/**
	 * Add a pair of vertical grip lines to a drag handle so the resize handles are
	 * more obvious to the user.
	 * @param dragPane - the handle pane to decorate.
	 * @param lineInset - the vertical inset of the lines from the top and bottom of the handle.
	 */
	private void addGripLines(Pane dragPane, double lineInset){
		double offset = 1.8; //horizontal spacing of the grip lines either side of centre.
		for (int i = -1; i <= 1; i += 2){
			Line gripLine = new Line();
			//stroke comes from the style sheet so it can be dark on the light handles of
			//the dark schemes and light on the dark handles of the day scheme.
			gripLine.getStyleClass().add(SCROLLER_GRIP_STYLE_CLASS);
			gripLine.setStrokeWidth(1.4);
			gripLine.startXProperty().bind(dragPane.widthProperty().divide(2).add(i*offset));
			gripLine.startYProperty().setValue(lineInset);
			gripLine.endXProperty().bind(dragPane.widthProperty().divide(2).add(i*offset));
			gripLine.endYProperty().bind(dragPane.heightProperty().subtract(lineInset));
			dragPane.getChildren().add(gripLine);
		}
	}


	/**
	 * Called when one of the resize handles is pressed. Records the geometry of the
	 * rectangle and the press position so that any subsequent drag can be worked out
	 * as a movement from that position. Note that nothing about the scroller is
	 * changed here - a press which is not followed by a drag must leave the visible
	 * range exactly as it was.
	 * @param event - the mouse pressed event.
	 */
	private void handlePressed(MouseEvent event){
		pressPosX=dragPosX(event);
		pressLayoutX=rectangle.getLayoutX();
		pressWidth=rectangle.getWidth();
		handleDragging=false;
		//need to consume so does not pass to underlying node.
		event.consume();
	}

	/**
	 * Called when one of the resize handles is released.
	 * @param event - the mouse released event.
	 */
	private void handleReleased(MouseEvent event){
		handleDragging=false;
		isChanging.set(false);
		dragging(event);
		event.consume();
	}

	/**
	 * Check whether a mouse drag on one of the resize handles has moved far enough
	 * from the press position to count as a genuine drag. Until it has, the drag is
	 * ignored so that clicking on a handle (which is only a few pixels wide, and so
	 * easy to wobble whilst clicking) does not change the visible range.
	 * @param event - the mouse dragged event.
	 * @return true if the handle is being dragged and the visible range should change.
	 */
	private boolean handleDrag(MouseEvent event){
		if (!handleDragging && Math.abs(dragPosX(event)-pressPosX)<DRAG_THRESHOLD){
			//consume so the part-drag does not pass to the underlying node.
			event.consume();
			return false;
		}
		handleDragging=true;
		return true;
	}

	/**
	 * The smallest width, in pixels, a resize drag may set the rectangle to. This is
	 * normally MIN_PIXEL_WIDTH, but a rectangle which is already narrower than that
	 * (the visible range can be set to anything from the text box) must not be made
	 * wider by a drag which is trying to make it narrower.
	 * @return the minimum width in pixels for the drag in progress.
	 */
	private double minDragWidth(){
		return Math.max(1, Math.min(MIN_PIXEL_WIDTH, pressWidth));
	}

	/**
	 * The x position of a mouse event in scroll bar pane co-ordinates. Note that this
	 * must go via the scene because the pane is rotated when the scroller is vertical
	 * and because events arrive from the handles, the text box and the drag icon.
	 * @param event - the mouse event.
	 * @return the x position of the event within the scroll bar pane.
	 */
	private double dragPosX(MouseEvent event){
		Point2D newPos=scrollBarPane.sceneToLocal(event.getSceneX(), event.getSceneY());
		return newPos.getX();
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
	 * Create the icon which sits on top of the visible range rectangle and lets the
	 * user drag it when the rectangle is too narrow to grab.
	 * <p>
	 * When the visible range is small the two resize handles meet and the text box,
	 * which is centred on the rectangle, covers whatever is left of it. The only way
	 * to move the scroller then is to drag the text box, which works but is not at all
	 * obvious. The icon appears whenever the rectangle is narrower than the text box
	 * and gives the user something clear to grab hold of.
	 * @return the drag icon.
	 */
	private Pane createDragIcon(){

		//a 3 x 3 grid of dots - the classic drag symbol.
		Text glyph = PamGlyphDude.createPamIcon("mdi2d-dots-grid", 14);
		glyph.getStyleClass().add(SCROLLER_DRAG_GLYPH_STYLE_CLASS);

		StackPane dragIcon = new StackPane(glyph);
		//colours come from the style sheet so the icon inverts with the colour scheme,
		//just like the rectangle and its handles.
		dragIcon.getStyleClass().add(SCROLLER_DRAG_ICON_STYLE_CLASS);
		dragIcon.setMinSize(DRAG_ICON_WIDTH, DRAG_ICON_HEIGHT);
		dragIcon.setPrefSize(DRAG_ICON_WIDTH, DRAG_ICON_HEIGHT);
		dragIcon.setMaxSize(DRAG_ICON_WIDTH, DRAG_ICON_HEIGHT);
		dragIcon.setCursor(Cursor.OPEN_HAND);

		//the icon floats at the top of the rectangle rather than in the middle of it, so
		//that it is clear of the text box and the range can still be read.
		dragIcon.layoutXProperty().bind(rectangle.layoutXProperty().add(rectangle.widthProperty().divide(2))
				.subtract(DRAG_ICON_WIDTH/2.));
		dragIcon.setLayoutY(DRAG_ICON_INSET);

		//only show the icon when the rectangle is narrower than the text box, i.e. when
		//the text box covers the rectangle and there is no obvious way to drag it.
		dragIcon.visibleProperty().bind(rectangle.widthProperty().lessThan(textBox.widthProperty())
				.and(rectangle.visibleProperty()));

		//dragging the icon moves the rectangle in exactly the same way as dragging its body.
		dragIcon.setOnMousePressed((event)->{
			dragIcon.setCursor(Cursor.CLOSED_HAND);
			rectanglePressed(event);
		});

		dragIcon.setOnMouseReleased((event)->{
			dragIcon.setCursor(Cursor.OPEN_HAND);
			rectangleReleased(event);
		});

		dragIcon.setOnMouseDragged((event)->{
			rectangleDragged(event);
		});

		dragIcon.setOnMouseDragReleased((event)->{
			isChanging.set(false);
		});

		//the icon sits on top of the rectangle and text box, so it must keep the text box
		//showing whilst the mouse is over it.
		dragIcon.setOnMouseEntered((event)->{
			setTextBoxVisible(true);
		});

		dragIcon.setOnMouseExited((event)->{
			if (!rectangle.contains(rectangle.sceneToLocal(event.getSceneX(), event.getSceneY()))){
				setTextBoxVisible(false);
			}
		});

		return dragIcon;
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
		/*
		 * Record a delta distance for the drag and drop operation. Note that this can be
		 * outside the node - the text box and the drag icon both overhang a narrow
		 * rectangle - and keeping the true (even negative) offset is what stops the
		 * rectangle jumping out from under the mouse when the drag begins.
		 */
		dragX = newPos.getX();
		//need to consume so does not pass to underlying node.
		event.consume();
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
	 * Get the drag icon which appears on top of the scroll rectangle when the
	 * rectangle is too narrow to grab.
	 * @return the drag icon.
	 */
	public Pane getDragIcon(){
		return this.dragIcon;
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

