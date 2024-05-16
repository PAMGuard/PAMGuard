package pamViewFX.fxNodes.hidingPane;

import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Animation.Status;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;

/**
 * Hiding pane which can be added to any node. 
 * <p>
 * A hiding pane can either be overlaid in which case it appears over the parent node or it can push the parent node out of the way. Panes can
 * be orientated to appear from the LEFT, RIGHT, TOP or BOTTOM. 
 * <p>
 * 
 * @author Jamie Macaulay
 *
 */
public class HidingPane extends StackPane {

	/**
	 * The opacity of the button when the mouse is outside the show button
	 */
	private double showButtonOpacity = 0.25;


	/**
	 * Which side the pane is hiding on. 
	 */
	private Side side;

	/**
	 * The pane shown inside the hiding pane. 
	 */
	private Region hidePane;

	/**
	 * The pane in which the hiding panel sits. 
	 */
	private Pane holderPane;

	/**
	 * Expanded size of the panel. When panel is aligned right/left this is essentially the panel 
	 * width when shown and when aligned top.button it is the height shown. 
	 */
	private double expandedSize=300;

	/**
	 * Show/Hide animation time in milliseconds. 
	 */
	private long duration=200;

	/**
	 * Time line which shows the pane
	 */
	private Timeline timeLineShow;

	/**
	 * Time line which hides the pane
	 */
	private Timeline timeLineHide;

	/**
	 * Button which sits in the hiding pane. Pressed to hide pane. 
	 */
	private PamButton hideButton;

	/**
	 * The button which shows the pane. This button isn't actually on the pane. 
	 */
	private PamButton showButton;

	/**
	 * Indicate whether the hiding panel should appear on top of the node or move the 
	 * node to the side. 
	 */
	private boolean overlay;

	/**
	 * 1 or -1 depending on side. A useful index for determining which directions translation occur in 
	 */
	private int sideIndex;

	/**
	 * The translate property, depends on side and whether the hiding panel is overlaid or moves it's parent node. 
	 */
	private DoubleProperty paneTanslateProperty;

	/**
	 * The binding property for the show button. Only used when the panel is overlayed on another panel. 
	 */
	private DoubleBinding buttonTranslateProperty; 

	/**
	 * Indicates whether the panel is showing or not;
	 */
	private BooleanProperty showing=new SimpleBooleanProperty(false);

	/**
	 * If true then the pane only shows once the hide pane animation has finished 
	 */
	private boolean visibleImmediatly=true;

	/**
	 * The default size of the icons 
	 */
	private int iconSize=PamGuiManagerFX.iconSize;

	/***
	 * Moves the pane an extra offset in the opening and closing direction. Note that this will create a gap in the pane. 
	 * 
	 */
	private double offset=0; 

	/**
	 * Constructor top create a new hiding pane. 
	 * @param side - the side the hiding pane appears from. 
	 * @param hidePane - content for hiding pane. 
	 * @param holderPane - the pane holding the hiding pane. 
	 * @param overlay - whether the pane is overlaid or not. Overlaid means the pane appears over the node rather than pushing it to one side.  
	 * @param offset - set the offset. 
	 */
	public HidingPane(final Side side, Region hidePane, Pane holderPane, boolean overlay, double offset) {
		this.side=side;
		this.hidePane=hidePane;
		this.holderPane=holderPane;
		this.overlay=overlay;
		this.offset=offset; 

		//		//CSS styling		 
		//		this.getStylesheets().add(getClass().getResource("pamSettingsCSS.css").toExternalForm());
		//		this.getStyleClass().add("pane");

		//set the size the panel will expand to
		setDefaultExpandedSize(hidePane);

		//set the translate property and side index. 
		setTranlsateProperty();

		//create a button which hides the side panel
		hideButton=createShowButton(false);
		styleHideButton(hideButton);
		hideButton.setOnAction(new HideButtonPressed());
		hideButton.setVisible(false);
		//		hideButton.setStyle("close-button-right");

		//create a show button. The show button is not displayed on the hiding panel but caqn be used in other panels 
		showButton=createShowButton(true);

		//if overlay true bind the show button to the side pane.
		if (overlay) showButton.translateXProperty().bind(buttonTranslateProperty);

		//set up the animation for panel showing/hiding
		setAnimation();

		holderPane.setMinHeight(5); //needed for hiding panes to appear outside holder pane. 


		//		this.setMinHeight(500);
		//		holderPane.heightProperty().addListener((oldVall, newVal, obsVsal)->{
		//			if (this.getHeight()<500){
		//				this.setLayoutY(0);
		//				this.setMinHeight(500);
		//				holderPane.setStyle("-fx-background: red;");
		//			}
		//			System.out.println("HidingPane: LayoutY: "+this.getLayoutY() + " height "+this.getHeight());
		//		});

		//		PamBorderPane mainPane=new PamBorderPane();
		//		mainPane.setCenter(hidePane);

		this.getChildren().add(hidePane);
		setHideButtonPos(side);

	} 


	/**
	 * Constructor top create a new hiding pane. 
	 * @param side - the side the hiding pane appears from. 
	 * @param hidePane - content for hiding pane. 
	 * @param holderPane - the pane holding the hiding pane. 
	 * @param overlay - whether the pane is overlaid or not. Overlaid means the pane appears over the node rather than pushing it to one side.  
	 */
	public HidingPane(final Side side, Region hidePane, Pane holderPane, boolean overlay) {
		this(side, hidePane, holderPane, overlay, 0); 
	}

	public void setShowButton(PamButton showButton) {
		this.showButton = showButton;
	}

	public boolean isHorizontal(){
		if (side==Side.TOP || side==Side.BOTTOM) return true; 
		return false; 
	}


	public void resetHideAnimation(){
		//set the size the panel will expand to
		setDefaultExpandedSize(hidePane);

		//set the translate property and side index. 
		setTranlsateProperty();
		//set up the animation for panel showing/hiding
		setAnimation();
	}

	/**
	 * Style the button to have an image of arrow.
	 * @param button-button to style
	 */
	public void styleHideButton(PamButton button){
		styleHideButton(button, side); 
	}


	/**
	 * Style the button to have an image of arrow.
	 * @param button-button to style
	 * @param side - the side the button should be styled for. 
	 */
	public void styleHideButton(PamButton button, Side side){
		switch (side){
		case RIGHT:
			//button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Resources/SidePanelShow2.png"))));
			//			button.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_RIGHT, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-right", PamGuiManagerFX.iconSize));
			button.getStyleClass().add("close-button-right");
			break;
		case LEFT:
			//button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Resources/SidePanelHide2.png"))));
			//			button.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
			button.getStyleClass().add("close-button-left");
			break;
		case TOP:
			//button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Resources/SidePanelUp2.png"))));

			//			button.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_UP, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-up", PamGuiManagerFX.iconSize));
			button.setPrefWidth(60); //horizontal buttons are slightly wider
			button.getStyleClass().add("close-button-top");
			break;
		case BOTTOM:
			//utton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Resources/SidePanelDown2.png"))));
			//			button.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_DOWN, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-down", PamGuiManagerFX.iconSize));
			button.setPrefWidth(60); //horizontal buttons are slightly wider
			button.getStyleClass().add("close-button-bottom");
			break;
		}
	}

	/**
	 * Set the default expanded size for the hiding panel. If the pane has a preferred size then uses this but if not then uses
	 * the field expandedSize to set expanded Size; 
	 * @param hidePane- the pane to hide/show. 
	 */
	private void setDefaultExpandedSize(Region hidePane){
		boolean isHorizontal=isHorizontal();

		//check if the panel has a preferred width or height depending on side position. If so set the expanded size to that;
		if (!isHorizontal && hidePane.getPrefWidth()>0) expandedSize=hidePane.getPrefWidth();
		else if (isHorizontal && hidePane.getPrefHeight()>0) expandedSize=hidePane.getPrefHeight();

		//now set the correct preferred width for the panel
		if (!isHorizontal){
			hidePane.setPrefWidth(expandedSize);
			setMaxWidth(hidePane.getPrefWidth());
		}
		else{
			hidePane.setPrefHeight(expandedSize);
			setMaxHeight(hidePane.getPrefHeight());
		}

		this.setMinWidth(0);
		this.setMinHeight(0);
	}

	/**
	 * If the preferred size is set for the hiding pane then the expanded size, the animation, the show 
	 * button binding property and position of the pane must be reset.
	 */
	@Override	
	public void setPrefSize(double prefWidth, double prefHeight){
		super.setPrefSize(prefWidth, prefHeight);

		if (!isHorizontal()){
			expandedSize=prefWidth; 
			hidePane.setPrefWidth(expandedSize);
			setMaxWidth(hidePane.getPrefWidth());
		}
		else{
			expandedSize=prefHeight; 
			hidePane.setPrefHeight(expandedSize);
			setMaxHeight(hidePane.getPrefHeight());
		}

		//set the translate property and side index. 
		setTranlsateProperty();
		//bind the show button to the side of the pane
		if (overlay) showButton.translateXProperty().bind(buttonTranslateProperty);
		setAnimation();
		//set the correct translation for the pane
		if (showing.get()) paneTanslateProperty.set(overlay ? 0 : expandedSize+offset);
		else paneTanslateProperty.set(overlay ? (expandedSize+offset)*sideIndex: 0);

	}


	/**
	 * Create a blank button which contains all the functionality to open or close the hiding panel, including the ability
	 * to drag the panel open. 
	 * @return a button with no styling which can be used to drag and open the hiding panel. 
	 */
	double dragX=0;
	double dragY=0;
	double distance=0;


	private PamButton createShowButton(final boolean show){

		final PamButton pamButton=new PamButton();

		pamButton.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragX =mouseEvent.getSceneX();
				dragY =mouseEvent.getSceneY();
			}
		});

		pamButton.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				if (distance==0) return; 
				distance=sideIndex*distance;
				//			  System.out.println("Mouse released: HidePanel distance: "+distance);
				if (!overlay) distance=Math.abs(distance);
				//need to see where the drag has ended-if greater than 50% then open but if less then close. 
				if (!isHorizontal()){
					if (distance<expandedSize/2) showHidePane(overlay? true : false);
					if (distance>=expandedSize/2) showHidePane(overlay? false : true);
				}
				else{
					if (distance<expandedSize/2) showHidePane(overlay? true : false);
					if (distance>=expandedSize/2) showHidePane(overlay? false : true);
				}
				//reset the distance
				distance=0;
			}
		});

		pamButton.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				if (visibleImmediatly) hidePane.setVisible(true);
				else hidePane.setVisible(false);
				// hideButton.setVisible(true);
				/**
				 * Work out the distance the panel is to be dragged; 
				 */
				if (!isHorizontal()){
					if (!show) distance=(mouseEvent.getSceneX()-dragX);
					else distance=(mouseEvent.getSceneX()-dragX)+sideIndex*expandedSize;
					if (!overlay) distance=expandedSize-sideIndex*distance;
				}
				else{
					if (!show) distance=(mouseEvent.getSceneY()-dragY);
					else distance=(mouseEvent.getSceneY()-dragY)-sideIndex*expandedSize;
					if (!overlay) distance=expandedSize+sideIndex*distance;
				}
				//			  if (show && Math.abs(distance)>expandedSize) return; 

				translatePanel(distance);
			}
		});

		pamButton.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
//				System.out.println("HidingPane.showButton - mouse entered");
				pamButton.setOpacity(1.0);
				pamButton.setPadding(new Insets(2.,2.,2.,2.));
			}
		});

		pamButton.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
//				System.out.println("HidingPane.showButton - mouse exited");
				if (show) pamButton.setOpacity(showButtonOpacity);
				pamButton.setPadding(new Insets(0.,0.,0.,0.));
			}
		});

//		pamButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
//			@Override public void handle(MouseEvent mouseEvent) {
//				System.out.println("HidingPane.showButton - mouse clicked");
//				showHidePane(show);
//			}
//		});

		pamButton.addEventHandler(ActionEvent.ACTION,new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("HidingPane.showButton - action event clicked");
				showHidePane(show);
			}
		});

		if (show) pamButton.setOpacity(showButtonOpacity);
		
		return pamButton;
	}
	
	/**
	 * Set the position of the hiding button. This will depend on the Side property of the panel.
	 * @param side- hiding pane side. 
	 */
	public void setHideButtonPos(Side side){
		this.getChildren().remove(hideButton);
		switch (this.side){
		case RIGHT:
			StackPane.setAlignment(hideButton,  Pos.TOP_LEFT);
			this.getChildren().add(hideButton);
			break;
		case LEFT:
			StackPane.setAlignment(hideButton,  Pos.TOP_RIGHT);
			this.getChildren().add(hideButton);
			break;
		case TOP:
			StackPane.setAlignment(hideButton,  Pos.BOTTOM_CENTER);
			this.getChildren().add(hideButton);
			break;
		case BOTTOM:
			this.getChildren().add(hideButton);
			StackPane.setAlignment(hideButton,  Pos.TOP_CENTER);
			break;
		}
	}

	/**
	 * Remove the hide button from the hiding pane. For example, used if the pane is non user controllable.
	 */
	public void removeHideButton(){
		this.getChildren().remove(hideButton);
	}

	/**
	 * The hiding panel will have a different side Index and translateProperty depending on what the side and overlay value combination is.
	 * This function sets the correct values for sideIndex and translateProperty which are used in opening/closing animations and dragging the panel to an open or c
	 * closed position. 
	 */
	private void setTranlsateProperty(){

		/**
		 *	Figure out the correct index and property. The sideIndex is used to tell the animation what direction to 
		 *	move the animation in. The property tells the panel either to translate in the x or y direction or 
		 * to change it's preferred size. Changing the preferred size means all other layouts change with panel. 
		 */
		switch (this.side){
		case RIGHT:
			sideIndex=1;
			/**
			 * Set the correct translate property for the pane depending on whether overlaid or not. If the pane
			 * is overlaid it simply moves form off screen onto on screen. If not overlaid then the pane has to change
			 * the layout of it's parent node, thus must change size rather than translate. 
			 */
			paneTanslateProperty= overlay ? this.translateXProperty() : this.maxWidthProperty();
			/**
			 * Bind the translate property of the show button to the panel if the panel is overlaid. This moves the show button with the pane.
			 */
			buttonTranslateProperty=paneTanslateProperty.subtract(sideIndex*(expandedSize+offset));
			break;
		case LEFT:
			sideIndex=-1;
			paneTanslateProperty= overlay ? this.translateXProperty() : this.maxWidthProperty();
			buttonTranslateProperty=paneTanslateProperty.subtract(sideIndex*(expandedSize+offset));
			break;
		case TOP:
			sideIndex=1;
			paneTanslateProperty= overlay ? this.translateYProperty() : this.maxHeightProperty();
			buttonTranslateProperty=paneTanslateProperty.subtract(sideIndex*(expandedSize+offset));
			break;
		case BOTTOM:
			sideIndex=1;
			paneTanslateProperty= overlay ? this.translateYProperty() : this.maxHeightProperty();
			buttonTranslateProperty=paneTanslateProperty.subtract(sideIndex*(expandedSize+offset));
			break;
		default:
			sideIndex=1;
			paneTanslateProperty=this.translateYProperty();
			buttonTranslateProperty=paneTanslateProperty.subtract(expandedSize+offset);
			break;
		}
	}

	/**
	 * Create a time line to show or hide a pane.
	 * @param paneTanslateProperty -the translate property e.g. whether to change the preferred height/width, max height/width etc.
	 * @param newSize - the new size of the property
	 * @param duration - the duration of the animation in millis
	 * @return the time line for the animation
	 */
	public static Timeline createAnimation(DoubleProperty paneTanslateProperty, double newSize, long duration){
		Timeline timeLine= new Timeline();
		// Animation for scroll SHOW.
		timeLine.setCycleCount(1); //defines the number of cycles for this animation
		final KeyValue kvDwn = new KeyValue(paneTanslateProperty, newSize);
		//		final KeyValue kvDwn = new KeyValue(translateProperty, expandedSize);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(duration), kvDwn);
		timeLine.getKeyFrames().add(kfDwn);
		return timeLine;
	}

	/**
	 * Set up the animation that moves the hiding panel to show and to hide. translate property and sideIndex must be set
	 * before calling this function. 
	 */
	private void setAnimation(){

		//Initially hiding the pane
		paneTanslateProperty.set((expandedSize)*sideIndex); //+for right

		//create the time lines. 
		// Animation for scroll SHOW.
		timeLineShow = createAnimation(paneTanslateProperty,  overlay ? 0-offset : expandedSize +offset ,  duration);
		// Animation for scroll HIDE
		timeLineHide = createAnimation(paneTanslateProperty,   overlay ? (expandedSize +offset)*sideIndex: 0-offset ,  duration);

		timeLineShow.setOnFinished(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				showFinished();
			}
		});

		timeLineHide.setOnFinished(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				hideFinished();
			}
		});

	}

	/**
	 * Called whenever the hid animation has finished. 
	 */
	public void hideFinished(){
		if (showButton.translateXProperty().getValue()!=0 && !showButton.translateXProperty().isBound()) showButton.setTranslateX(0); //check show button is in the original OK position. 
		showButton.setVisible(true);
		hidePane.setVisible(false);
		hideButton.setVisible(false);
		showButton.toFront();
		//		//TODO-delete
		//		System.out.println("ShowButton Size:"+showButton.getLayoutX() +" tranlateX: "+showButton.getTranslateX() + 
		//				" tranlateY: "+showButton.getTranslateY()+" show button layoutX "+ showButton.getLayoutX()+ " "
		//				+showButton.getLayoutY()+ "show button width: "+showButton.getWidth());
	}

	/**
	 * Called whenever the show animation has finished. 
	 */
	public void showFinished(){
		showButton.setVisible(false);
		hidePane.setVisible(true);
		hideButton.setVisible(true);
	}

	/**
	 * Move the hiding panel to show or hide a certain distance.
	 * @param distance. The distance in pixels to move the panel. Positive to open the panel and negative to move the panel towards closed. 
	 */
	public void translatePanel(double distance){
		paneTanslateProperty.set(distance);
	}

	/**
	 * Show or hide the pane. 
	 * @param showing-
	 */
	public synchronized void showHidePane(boolean show){
		if (show) {
			//the pref size may have been changed...hide pane shoulf follow this size. 			
			showing.setValue(true);
			if (visibleImmediatly) hidePane.setVisible(true);
			//hideButton.setVisible(true);
			//System.out.println("HidingPane: Open Hide Pane");
			//open the panel
			if (timeLineShow.getStatus()==Status.RUNNING) {
				//stops the issue with the hiding pane freezing.
				return;
			}

			timeLineShow.play();
		}
		else{
			showing.setValue(false);
			if (!visibleImmediatly) hidePane.setVisible(false);
			//System.out.println("HidingPane: Close Hide Pane");
			//close the panel
			timeLineHide.play();
		}
	}

	/**
	 * Show the hide pane
	 * @param show - tru to opne the pane. False to close the pnae
	 * @param animate - true to show and animation and false to show no animation.
	 */
	public void showHidePane(boolean show, boolean animate){
		if (animate){
			showHidePane(show); 
		}
		else {
			if (show){
				paneTanslateProperty.set(-(expandedSize+offset)*sideIndex);
			}
			else{
				paneTanslateProperty.set((expandedSize+offset)*sideIndex);
			}
		}
	}

	/**
	 * Called whenever the pin button is pressed. 
	 * @author Jamie Macaulay
	 */
	class HideButtonPressed implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			showHidePane(false);
		}
	}

	/**
	 * Get the button which hides the pane.
	 * @return the button which hides the pane.
	 */
	public PamButton getHideButton() {
		return hideButton;
	}

	/**
	 * Get the button which shows the hide pane. This is generally not on the pane itself but needs to be
	 * added to another pane. 
	 * @return the button which shows the pane. 
	 */
	public PamButton getShowButton() {
		return showButton;
	}

	/**
	 * Get the duration in milliseconds of the slide animation. 
	 * @return the slide animation duration in milliseconds. 
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Set the duration of the slide animation. (The animation which occurs when the panel is shown/hidden)
	 * @param duration- duration of the slide animation in milliseconds. 
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * Get the pane which contains this hiding pane.
	 * @return the pane the hiding pane is located in.
	 */
	public Pane getHolderPane() {
		return holderPane;
	}

	/**
	 * Get the timeline which opens hiding tab pane. 
	 * @return TimelIne which opens the hiding pane. 
	 */
	public Timeline getTimeLineShow() {
		return timeLineShow;
	}

	/**
	 * Get the timeline which closes the hiding pane
	 * @return TimeLine which closes the hiding pane. 
	 */
	public Timeline getTimeLineHide() {
		return timeLineHide;
	}


	/**
	 * Check whether the hide pane shows content at the start of the hide animation or end. 
	 * @return true if shows the pane at the start of the animation. 
	 */
	public boolean isVisibleImmediatly() {
		return visibleImmediatly;
	}

	/**
	 * Set whether the hide pane shows content at the start of the hide animation or end. 
	 * @param true if shows the pane at the start of the animation. 
	 */
	public void setVisibleImmediatly(boolean visibleImmediatly) {
		this.visibleImmediatly = visibleImmediatly;
	}

	/**
	 * The showing property for the hiding pane. 
	 * @return the showing property
	 */
	public BooleanProperty showingProperty(){
		return this.showing; 
	}
	
	/**
	 * Get the opacity of the show button when the mouse is outside the button. 
	 * @return the opacity of the show button.
	 */
	public double getShowButtonOpacity() {
		return showButtonOpacity;
	}


	/**
	 * Set the opacity of the show button when the mouse is outside.
	 * @return the opacity of the show button - set to 1.0 for no chnage when mouse outside the button. 
	 */
	public void setShowButtonOpacity(double showButtonOpacity) {
		this.showButtonOpacity = showButtonOpacity;
		this.showButton.setOpacity(showButtonOpacity);
	}


	//	hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
	//	@Override public void handle(ActionEvent actionEvent) {
	//		setVisible(false);
	//		controlButton.setText("Show");
	//		controlButton.getStyleClass().remove("hide-left");
	//		controlButton.getStyleClass().add("show-right");
	//	}
	//});
	//
	//
	//showSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
	//	@Override public void handle(ActionEvent actionEvent) {
	//		controlButton.setText("Collapse");
	//		controlButton.getStyleClass().add("hide-left");
	//		controlButton.getStyleClass().remove("show-right");
	//	}
	//});

	//	 // create an animation to hide sidebar.
	//    final Animation hideSidebar = new Transition() {
	//      { setCycleDuration(Duration.millis(50)); }
	//      protected void interpolate(double frac) {
	//        final double curWidth = expandedWidth * (1.0 - frac);
	//        setPrefWidth(curWidth);
	//        setTranslateX(curWidth);
	//      }
	//    };
	//    
	//    // create an animation to show a sidebar.
	//    final Animation showSidebar = new Transition() {
	//      { setCycleDuration(Duration.millis(duration)); }
	//      protected void interpolate(double frac) {
	//        final double curWidth = expandedWidth * frac;
	//        setPrefWidth(curWidth);
	//        setTranslateX(expandedWidth-curWidth);
	//      }
	//    };

	//    if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
	//    if (isVisible()) {
	//      hideSidebar.play();
	//    } else {
	//      setVisible(true);
	//      showSidebar.play();
	//    }
	//  }

}