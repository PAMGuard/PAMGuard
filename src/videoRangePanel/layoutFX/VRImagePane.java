package videoRangePanel.layoutFX;

import java.awt.Point;
import java.io.File;
import java.net.URLConnection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
import videoRangePanel.VRControl;
import videoRangePanel.pamImage.PamImage;

/** 
 * 
 * Pane for showing an image or video.
 * @author Jamie Macaulay 
 *
 */
public class VRImagePane extends PamBorderPane {

	/**
	 * An image is currently showing
	 */
	public static final int IMAGE= 0;

	/**
	 * The current file is the video. 
	 */
	public static final int VIDEO = 1;

	/**
	 * SHowing an image extracted from a video. 
	 */
	public static final int IMAGE_VIDEO = 2;


	/**
	 * This pane shows an image.
	 */
	private VRImageView vrImagePane;

	/**
	 * 
	 * Pane which shows video viewer. 
	 */
	private VRMediaView vrMediaPane; 

	/**
	 * The current 'type if image is displayed. 
	 */
	private int currentImageType=IMAGE; 

	/**
	 * Stack holder- this is the main display. 
	 */
	private PamStackPane stackHolder;


	/**
	 * Reference to the main display.
	 */
	private VRDisplayFX vrDisplayFX;

	/**
	 * The current image or video file. Note that this is never a folder. Folder navigation is handles by the VRDsiplayFX. 
	 */
	private File currentFile;

	/**
	 * Holds the media pane and the overlay canvas. Important pane as all zooming and image to pixels , vice versa is handles here. 
	 */
	private ZoomableScrollPane scrollPane;

	/**
	 * Holds the zoomable stuff in the scroll pane. 
	 */
	private Group zoomGroup;

	/**
	 * The stack pane. 
	 */
	private StackPane mainHolder;

	/**
	 * Hiding pane for image controls 
	 */
	private PamBorderPane hidingBottomHolder;

	/**
	 * Hiding pane for extra stuff such as image metadata and image editing options (darken/brighten etc.)
	 */
	private PamBorderPane hidingRightHolder;

	/**
	 * Pane which contains controls to edit image. 
	 */
	private Pane editPane;

	/**
	 * Shows image information. 
	 */
	private VRMetaDataPaneFX infoPane;

	/**
	 * Timeline for automatic hiding of the button hiding pane. Delays hiding for a few seconds. 
	 */
	private Timeline timeline;

	/**
	 * The amount of time the button pane stays in place before hiding (millis); 
	 */
	private double diff = 1000;

	/**
	 * The overlay canvas. This is where lines/shapes etc are drawn. 
	 */
	private Canvas drawCanvas;

	/**
	 * Pane which shows some type of image or movie. 
	 */
	private VRImage vrImage;

	/**
	 * 
	 * The hidden control pane. 
	 */
	private HidingPane hiddenControlPane;

	/**
	 * Holds the ribbon pane. 
	 */
	private PamBorderPane ribbonHolder;

	/**
	 * The right hiding pane. Holds metadata, image manipulation and settings panes. 
	 */
	private HidingPane hiddenRightPane;

	/**
	 * Holds image controls, e.g. metadata, image edit and vr settings buttons which open corresponding 
	 * hiding side panes. 
	 */
	private HBox imageControls; 

	protected final static int iconSize=PamGuiManagerFX.iconSize; 

	protected final static double rightHidingPaneWidth=250; 

	/**
	 * Amount to zoom in by;
	 */
	private final static double zoomIncrement=20.; 

	public VRImagePane(VRDisplayFX vrDisplayFX){
		this.vrDisplayFX=vrDisplayFX; 

		this.vrImagePane= new VRImageView(vrDisplayFX); 
		this.vrMediaPane= new VRMediaView(vrDisplayFX); 
		
		

		createPane();

	}

	private void createPane() {

		mainHolder = new PamStackPane(); 

		stackHolder= new PamStackPane(); 

		//		 stackHolder.prefWidthProperty().bind(this.widthProperty());
		//		 stackHolder.prefHeightProperty().bind(this.heightProperty()); 

		//		 stackHolder.setStyle("-fx-background-color: cyan;");

		scrollPane = new VRZoomScrollPane(stackHolder); 
		scrollPane.setPannable(true);
		scrollPane.getStylesheets().add(getDarkStyle() );

		scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.prefWidthProperty().bind(mainHolder.widthProperty());
		scrollPane.prefHeightProperty().bind(mainHolder.widthProperty());

		hidingBottomHolder= new PamBorderPane(); 
		hidingBottomHolder.setPrefHeight(100);
		//hidingBottomHolder.setCenter(new Label("I am the new hide pane see me roar")); 
		hidingBottomHolder.setStyle("-fx-background-color:  -fx-darkbackground-trans"); //from dark stylesheet
		//Bit of a hack here. Have set the offset to 15 pixels so that the hiding pane does not cover the scroll bar. 
		hiddenControlPane = new HidingPane(Side.BOTTOM , hidingBottomHolder , mainHolder, true, 15); 
		hiddenControlPane.getStylesheets().add(getDarkStyle() );
		hiddenControlPane.showHidePane(true);
		hiddenControlPane.getChildren().remove( hiddenControlPane.getHideButton()); 

		hidingRightHolder= new PamBorderPane(); 
		hidingRightHolder.setPrefWidth(500);
		hidingRightHolder.setCenter(null); 
		hidingRightHolder.setStyle("-fx-background-color:  -fx-darkbackground-trans"); //from dark stylesheet
		hiddenRightPane = new HidingPane(Side.RIGHT , hidingRightHolder , mainHolder, true); 
		hiddenRightPane.getStylesheets().add(getDarkStyle() );

		//****add main image pane to stack pane****//
		mainHolder.getChildren().add(scrollPane); 

		//****add additional controls****//

		editPane= createEditPane(); 
		infoPane= createInfoPane(); 

		//add hiding panes. 
		mainHolder.getChildren().add(hiddenControlPane); 
		StackPane.setAlignment(hiddenControlPane, Pos.BOTTOM_LEFT);
		mainHolder.getChildren().add(hiddenRightPane); 
		StackPane.setAlignment(hiddenRightPane, Pos.TOP_RIGHT);

		//set up buttons 
		imageControls = new HBox(); 
		imageControls.setSpacing(10);
		imageControls.setPadding(new Insets(10,10,10,10));
		imageControls.setMaxWidth(300);
		imageControls.setMaxHeight(35);
		imageControls.getStylesheets().add(getDarkStyle() );
		imageControls.setStyle("-fx-background-color:  -fx-darkbackground-trans"); //from dark stylesheet

		
		PamButton infoButton = new PamButton(); 
//		infoButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.INFO_OUTLINE, Color.WHITE, iconSize)); 
		infoButton.setGraphic(PamGlyphDude.createPamIcon("mdi2i-information-outline", Color.WHITE, iconSize)); 
		infoButton.setOnAction((action)->{
			imageControls.setVisible(false);
			infoPane.setPadding(new Insets(0,0,0,30)); 
			hidingRightHolder.setCenter(infoPane);
			hiddenRightPane.showHidePane(true);
		});
		infoButton.getStyleClass().add("square-button-trans");

		
		PamButton editButton = new PamButton(); 
//		editButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.TUNE, Color.WHITE, iconSize)); 
		editButton.setGraphic(PamGlyphDude.createPamIcon("mdi2t-tune", Color.WHITE, iconSize)); 
		editButton.setOnAction((action)->{
			imageControls.setVisible(false);
			editPane.setPadding(new Insets(22,0,0,0)); 
			hidingRightHolder.setCenter(editPane);
			hiddenRightPane.showHidePane(true);
		});
		editButton.getStyleClass().add("square-button-trans");
		
		
		PamButton settingsButton = new PamButton();
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.SETTINGS, Color.WHITE, iconSize)); 
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", Color.WHITE, iconSize)); 
		settingsButton.setOnAction((action)->{
			imageControls.setVisible(false);
			hidingRightHolder.setCenter(vrDisplayFX.getVrSettingsPane().getContentNode());
			hiddenRightPane.showHidePane(true);
		});
		settingsButton.getStyleClass().add("square-button-trans");


		PamButton zoomInButton = new PamButton(); 
//		zoomInButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ZOOM_IN, Color.WHITE, iconSize)); 
		zoomInButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-magnify-plus-outline", Color.WHITE, iconSize)); 
		zoomInButton.setOnAction((action)->{
			scrollPane.zoomIn(zoomIncrement);

		});
		zoomInButton.getStyleClass().add("square-button-trans");

		PamButton zoomOutButton = new PamButton(); 
//		zoomOutButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ZOOM_OUT, Color.WHITE, iconSize)); 
		zoomOutButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-magnify-minus-outline", Color.WHITE, iconSize)); 
		zoomOutButton.setOnAction((action)->{
			scrollPane.zoomOut(zoomIncrement);
		});
		zoomOutButton.getStyleClass().add("square-button-trans");
		
		PamButton zoomReset = new PamButton(); 
//		zoomReset.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.ARROW_EXPAND_ALL, Color.WHITE, iconSize)); 
		zoomReset.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-expand-all", Color.WHITE, iconSize)); 
		zoomReset.setOnAction((action)->{
			scrollPane.zoomResetScroll();
		});
		zoomReset.getStyleClass().add("square-button-trans");

		//show the image controls when the hide pane closes. 
		hiddenRightPane.showingProperty().addListener((obsVal, oldVal, newVal)->{
			imageControls.setVisible(!newVal);
		});

		imageControls.getChildren().addAll(zoomInButton, zoomOutButton, zoomReset, editButton, infoButton, settingsButton); 
		mainHolder.getChildren().add(imageControls); 
		StackPane.setAlignment(imageControls, Pos.TOP_RIGHT);

		//mainHolder.getChildren().add(hiddenControlPane.getShowButton()); 
		//		 scrollPane.setOnZoom((zoom)->{
		//			 System.out.println("Zooming");
		//			 scrollPane.setPrefViewportHeight(scrollPane.getPrefViewportHeight()+10);
		//			 scrollPane.setPrefViewportWidth(scrollPane.getPrefViewportWidth()+10);
		//
		//		 });

//		StackPane stackPane = new StackPane();
		PamStackPane stackPane = new PamStackPane();

		//*****Create the draw canvas*******//		
		/**
		 * 05/09/2017. had real problems resizing the canvas. Seemed like it would stop the stack pane 
		 * from resizing properly and still not sure why. Overriding some of the min max height and width 
		 * functions in subclass seems to have somehow helped. 
		 */
		drawCanvas= new ResizableCanvas(); 
		stackPane.widthProperty().addListener((obsVal, oldVal, newVal)->{
			this.vrDisplayFX.repaint();
		});
		drawCanvas.setMouseTransparent(true); //mouse events are not handles here 
		
		
		/*****Ribbon Holder****/
		ribbonHolder = new PamBorderPane(); 
		ribbonHolder.setMouseTransparent(true); 
		StackPane.setAlignment(ribbonHolder, Pos.TOP_LEFT);
		ribbonHolder.setMaxHeight(35);
		ribbonHolder.maxWidthProperty().bind(stackPane.widthProperty().subtract(imageControls.widthProperty()));
		ribbonHolder.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
		//ribbonHolder.setStyle("-fx-background-color:  -fx-darkbackground-trans"); //from dark stylesheet
		mainHolder.getChildren().add(ribbonHolder);

		//*****set up mouse actions*******//
		setMouseActions(); 
		
		//*****put it all together********//
		StackPane.setAlignment(drawCanvas, Pos.TOP_LEFT);
		
		stackPane.getChildren().addAll(mainHolder, drawCanvas);
		//imageControls.setStyle("-fx-background-color: blue"); 

		this.setCenter(stackPane);
		
		KeyCombination cntrlZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
		KeyCombination cntrlX = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
		KeyCombination cntrlA = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
		this.setOnKeyPressed(new EventHandler<KeyEvent>(){
		    @Override
		    public void handle(KeyEvent event) {
		        if(cntrlA.match(event)){
		        	scrollPane.zoomResetScroll();
		        }
		        if(cntrlX.match(event)){
		        	scrollPane.zoomOut(zoomIncrement);
		        }
		        if(cntrlZ.match(event)){
		        	scrollPane.zoomIn(zoomIncrement);
		        }
		    }
		});
		
        this.update(VRControl.METHOD_CHANGED);

	}
	
	
	/**
	 * Show the settings pane. 
	 */
	protected void showSettingsPane() {
		imageControls.setVisible(false);
		hidingRightHolder.setCenter(vrDisplayFX.getVrSettingsPane().getContentNode());
		hiddenRightPane.showHidePane(true);
	}

	/**
	 * Set mouse actions. 
	 */
	public void setMouseActions() {

		mainHolder.setOnMousePressed((event)->{
			//System.out.println("mouse press detected! "+event.getSource());
			if (vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX()!=null)
			vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().mouseAction(event, false, drawCanvas); 
		});

		mainHolder.setOnMouseMoved((event)->{
			//System.out.println("mouse move detected! "+event.getSource());
			vrDisplayFX.getSidePane().updateMouse(event); 
			bottomHideMouseBehaviour(event); 
		});

		mainHolder.setOnZoom((event)->{
			//System.out.println("mouse zoom detected! "+event.getSource());			
		});


		mainHolder.setOnMouseClicked((event)->{
			//System.out.println("mouse click detected! "+event.getSource());
			if (vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX()!=null)
			vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().mouseAction(event, false, drawCanvas); 
		});
		
		mainHolder.setOnMouseDragged((event)->{
			//System.out.println("mouse click detected! "+event.getSource());
			if (vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX()!=null)
			vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().mouseAction(event, false, drawCanvas); 
		});
		
		//bit of a hack. Stops the zoom image pane from panning if the control key is down 
		//but still sends the mouse actions to the different VR Methods. 
		scrollPane.getZoomNode().addEventHandler(MouseEvent.ANY, event -> {
			//don't allow the pane to scroll if control key is down. This
			//means some overlays can use mouse dragging. 
			if(event.isControlDown()) {
				//OK, this is a bit weird. So because the event is consumed the correct event info is not passed to the mouseAction 
				//function. So have to convert co-ords to mainHolder co-ords as this is what the entire FX GUI uses for reference. 
				MouseEvent newEvent = PamUtilsFX.mouseEvent2NewNode(event, drawCanvas); 
				vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().mouseAction(newEvent, true, drawCanvas); 
				event.consume();
			}
		});

	}
	

	/**
	 * Set mouse behaviour which opens the button hide pane automatically if the mouse enters the bottom of the screen
	 * @param mouseMoved - the mouse event
	 */
	private void bottomHideMouseBehaviour(MouseEvent mouseMoved) {
		//need to open the hide pane. 
		if (!hiddenControlPane.showingProperty().get() && mouseMoved.getY()>mainHolder.getHeight()*0.9
				&& mouseMoved.getY()<mainHolder.getHeight()-10) {
			if (timeline!=null) timeline.stop();
			hiddenControlPane.showHidePane(true);
		}
		else if (hiddenControlPane.showingProperty().get() &&  mouseMoved.getY()<mainHolder.getHeight()*0.9) {
			//start a timer. If the mouse moves out of and back into the hiding pane then the timeline is cancelled and 
			//only restart once the mouse moves out again.
			if (timeline!=null) timeline.stop();
			timeline = new Timeline(new KeyFrame(
					Duration.millis(diff),
					ae ->  hiddenControlPane.showHidePane(false)));
			timeline.play(); 
		}
	}

	/**
	 * Create the image edit pane. 
	 * @return the image editing pane. 
	 */
	private Pane createEditPane() {
		PamVBox vBox = new PamVBox(); 
		//vBox.getChildren().add(new Label("This is the pane to EDIT picture")); 
		return vBox; 
	}

	/**
	 * Create pane which shows info on pictures. 
	 * @return the info pane. 
	 */
	private VRMetaDataPaneFX createInfoPane() {
		return new VRMetaDataPaneFX(); 
	}


	/**
	 * Layout the pane. The layout depends on the type of media which is being shown. 
	 * @param currentImageType - flag for the current type of media being shown. 
	 */
	public void layoutPane(int currentImageType) {
		stackHolder.getChildren().clear();
		vrImage = null;
		switch(currentImageType) {
		case IMAGE: 
			vrImage=vrImagePane;
			break;
		case IMAGE_VIDEO:
			vrImage=vrImagePane;
			break;
		case VIDEO:
			vrImage=vrMediaPane;
			break;
		}
		stackHolder.getChildren().add(vrImage.getNode()); 
		
		vrImage.setMedia(currentFile);
		
		//set the meta data. 
		infoPane.setMetaText(vrImage.getMetaData());
		//set the image edit pane. 
		editPane.getChildren().clear();
		editPane.getChildren().add(vrImage.getImageEditPane()); 
		//		editPane.getChildren().clear();
		//		editPane.getChildren().add(vrImage.getControlPane()); 

		//set the control pane. 
		hidingBottomHolder.setCenter(vrImage.getControlPane());
		

	}

	/**
	 * Open a pamImage. 
	 * @param currentImage
	 */
	public void openNewImage(PamImage currentImage) {
		this.currentImageType=IMAGE;
		layoutPane(currentImageType);
		vrImage=vrImagePane;
		//open image from file or if no file exists then
		//grabs the buffered image from the data unit. 
		vrImagePane.setImage(currentImage);
	}

	/**
	 * Open a new file. The 
	 * @param file
	 */
	public void openNewFile(File file) {
		if (isImageFile(file.getAbsolutePath())) {
			this.currentImageType=IMAGE;
		}
		else if (isVideoFile(file.getAbsolutePath())) {
			//TODO - doesn't work with video files. 
			this.currentImageType=VIDEO; 
		}
		else {
			PamDialogFX.showWarning("Could not open file: The file was not recognized as either an image or a video");
			return; 
		}
		this.currentFile= file; 
		layoutPane(currentImageType);
	}


	/**
	 * Check whether a file is an image. 
	 * @param path - the file path
	 * @return true if an image file. 
	 */
	private static boolean isImageFile(String path) {
		String mimeType = URLConnection.guessContentTypeFromName(path);
		return mimeType != null && mimeType.startsWith("image");
	}


	/**
	 * Check whether a file is a video. 
	 * @param path - the file path
	 * @return true if a video file. 
	 */
	private static boolean isVideoFile(String path) {
		//		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
		//		String contentType =   fileTypeMap.getContentType(path);
		//		System.out.println("VRIMAGEVIEW: content type: " + contentType);
		//		if (!MediaManager.canPlayContentType(contentType)) {
		//			return false; 
		//		}
		//		else return true; 
		return true; 
	}

	/**
	 * Get the overlay canvas fro drawing on. 
	 * @return the overlay cnavas for drawing on. 
	 */
	public Canvas getDrawCanvas() {
		return this.drawCanvas; 
	}

	/**
	 * Get the current width of the image
	 * @return the width of the image in pixels. -1 if there is no image.
	 */
	public int getImageWidth() {
		if (vrImage==null) return -1; 
		return this.vrImage.getImageWidth();
	}


	/**
	 * Get the current width of the image
	 * @return the width of the image in pixels. -1 if there is no image.
	 */
	public int getImageHeight() {
		if (vrImage==null) return -1; 
		return this.vrImage.getImageHeight();
	}

	/**
	 * Convert a local co-orodinate to an image co-ordinate. 
	 * @param point - point on scroll pane
	 * @return  the point on the image. 
	 */
	public Point localToImage(Point point) {
		// TODO Auto-generated method stub
		Point2D pointImg= scrollPane.getContentXY(point.getX(), point.getY());
		Point pointImgAWT= new Point(); 
		pointImgAWT.setLocation(pointImg.getX(), pointImg.getY());
		return pointImgAWT;
	}
	
	
	/**
	 * Local to image 
	 * @param point - point on the image
	 * @return  the point on the image. 
	 */
	public Point imageToLocal(Point point) {
		Point2D pointImg= scrollPane.getPaneXY(point.getX(), point.getY());
		Point pointImgAWT= new Point(); 
		pointImgAWT.setLocation(pointImg.getX(), pointImg.getY());
		return pointImgAWT;
	}
	
	
	/**
	 * Resizable Canvas class stop weird effects with the Stack Pane
	 * @author Jamie Macaulay. 
	 *
	 */
	public class ResizableCanvas extends Canvas {

		@Override
		public double minHeight(double width)
		{
		    return 0;
		}

		@Override
		public double maxHeight(double width)
		{
		    return 10000;
		}

		@Override
		public double minWidth(double height)
		{
		    return 0;
		}

		@Override
		public double maxWidth(double height)
		{
		    return 10000;
		}

		@Override
		public boolean isResizable()
		{
		    return true;
		}

		@Override
		public void resize(double width, double height)
		{
		    super.setWidth(width);
		    super.setHeight(height);
		}
	}

	/**
	 * Clear the draw canvas overlay. 
	 */
	public void clearOverlay() {
		this.drawCanvas.getGraphicsContext2D().clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
	}
	
	/**
	 * Simple subclass of ZoomableScrollPane which repaints on a zoom. 
	 * @author Jamie Macaulay 
	 *
	 */
	private class VRZoomScrollPane extends ZoomableScrollPane {

		public VRZoomScrollPane(Node target) {
			super(target);
			//want to repaint whenever the scroll pane pans. 
			super.hvalueProperty().addListener((obsVal, oldVal, newVal)->{
				vrDisplayFX.repaint(); 
			});
			super.vvalueProperty().addListener((obsVal, oldVal, newVal)->{
				vrDisplayFX.repaint(); 
			});
		}
		
		@Override
		public void onScroll(double wheelDelta, Point2D mousePoint) {
			super.onScroll(wheelDelta, mousePoint);
			vrDisplayFX.repaint(); 
		}
	}

	/**
	 * Recieves update flags from the controller anf GUi 
	 * @param updateType
	 */
	public void update(int updateType) {
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:

			break;
		case VRControl.IMAGE_CHANGE:

			break;
		case VRControl.METHOD_CHANGED:
			changeMethod();
			break;			
		}
	}

	/**
	 * Called whenever a method changes. 
	 */
	private void changeMethod() {
		if (drawCanvas!=null) drawCanvas.getGraphicsContext2D().clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
		if (vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX()!=null){
//						System.out.println("VRSidePaneFX: The VR Method has changed: " + vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().getSidePaneFX() + " " +
//								vrDisplayFX.getVrControl().getCurrentMethod().getName());
			this.ribbonHolder.setCenter(vrDisplayFX.getVrControl().getCurrentMethod().getOverlayFX().getRibbonPaneFX());
		}
		else {
			ribbonHolder.setCenter(null); 
		} 

	}
	
	/**
	 * Get dark style CSS. 
	 * @return dark style CSS. 
	 */
	private String getDarkStyle() {
		return PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS();
	}

	public VRMetaDataPaneFX getInfoPane() {
		return infoPane;
	}




	//    /**
	//     * Reset to the top left:
	//     * @param imageView
	//     * @param width
	//     * @param height
	//     */
	//    private void reset(ImageView imageView, double width, double height) {
	//        imageView.setViewport(new Rectangle2D(0, 0, width, height));
	//    }
	//	
	//    /**
	//     * Shift the viewport of the imageView by the specified delta, clamping so
	//     * the viewport does not move off the actual image:
	//     * @param imageView
	//     * @param delta
	//     */
	//    private void shift(ImageView imageView, Point2D delta) {
	//        Rectangle2D viewport = imageView.getViewport();
	//
	//        double width = imageView.getImage().getWidth() ;
	//        double height = imageView.getImage().getHeight() ;
	//
	//        double maxX = width - viewport.getWidth();
	//        double maxY = height - viewport.getHeight();
	//        
	//        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
	//        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);
	//
	//        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
	//    }
	//
	//    
	//    private double clamp(double value, double min, double max) {
	//
	//        if (value < min)
	//            return min;
	//        if (value > max)
	//            return max;
	//        return value;
	//    }
	//
	//    // convert mouse coordinates in the imageView to coordinates in the actual image:
	//    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
	//        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
	//        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();
	//
	//        Rectangle2D viewport = imageView.getViewport();
	//        return new Point2D(
	//                viewport.getMinX() + xProportion * viewport.getWidth(), 
	//                viewport.getMinY() + yProportion * viewport.getHeight());
	//    }

}
