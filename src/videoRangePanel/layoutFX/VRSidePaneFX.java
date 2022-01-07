package videoRangePanel.layoutFX;

import java.awt.Point;
import java.io.File;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import videoRangePanel.VRControl;
import videoRangePanel.pamImage.PamImage;

/**
 * Holds side controls. 
 * @author Jamie Macaulay
 *
 */
public class VRSidePaneFX extends PamBorderPane {
	
	private FileChooser fileChooser;
	private VRDisplayFX vrDisplay;
	private List<File> list;
	
	/**
	 * Combo box which sets the measurment type. 
	 */
	private ComboBox<String> measurementType;
	
	/**
	 * Pane which  holds the method specific pane for each method. 
	 */
	private PamBorderPane methodHolder;
	
	/**W
	 * The current directory. 
	 */
	private DirectoryChooser directoryChooser;
	
	
	/**
	 * Shows the on image location of the position
	 */
	private Label mousePosition;
	
	/**
	 * The text area shwos the image name 
	 */
	private Label imageNameLabel;
	
	/**
	 * The text area shwos the image name 
	 */
	private Label imageTimeLabel;

	/**
	 * 
	 */
	public VRSidePaneFX(VRDisplayFX vrDisplay){
		//this.setStyle("-fx-background-color: -fx-darkbackground");
		
		this.vrDisplay=vrDisplay; 
		this.setTop(createSidePane());
	}
	
	
	/**
	 * Create the side pane. 
	 * @return the side pane controls 
	 */
	private Pane createSidePane() {
		
	    //the holder for everything
		final PamVBox holder= new PamVBox(); 
        holder.setSpacing(5);
        
        //create pane to import images. 
		
        final Label selectLabel = new Label("Select Image");
        PamGuiManagerFX.titleFont2style(selectLabel);
//		selectLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
		fileChooser = new FileChooser();
		directoryChooser = new DirectoryChooser(); 

		//opens a single image or video. 
        final PamButton openButton = new PamButton();
//        openButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_IMAGE, PamGuiManagerFX.iconSize));
        openButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-file-image", PamGuiManagerFX.iconSize));
        openButton.setTooltip(new Tooltip("Open a single image or video file"));
        openButton.setOnAction((action)->{
        	if (vrDisplay.getVrControl().getVRParams().currentImageFile!=null && vrDisplay.getVrControl().getVRParams().currentImageFile.exists()) {
        		fileChooser.setInitialDirectory(vrDisplay.getVrControl().getVRParams().currentImageFile.getParentFile());
        	}
        	fileChooser.setTitle("Open a single Image file or Video file");
        	File file = fileChooser.showOpenDialog(null);
        	if (file != null) {
        		openFile(file);
        	}
        });
        
        //button for opening a folder of images and/or videos. 
        final PamButton openMultipleButton = new PamButton();
//        openMultipleButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.FOLDER, PamGuiManagerFX.iconSize));
        openMultipleButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-folder", PamGuiManagerFX.iconSize));
        openMultipleButton.setTooltip(new Tooltip("Open multiple images and video files "));
        openMultipleButton.setOnAction((action)->{
        	if (vrDisplay.getVrControl().getVRParams().currentImageFile!=null  && vrDisplay.getVrControl().getVRParams().currentImageFile.exists()) {
        		directoryChooser.setInitialDirectory(vrDisplay.getVrControl().getVRParams().currentImageFile.getParentFile());
        	}
        	File file = directoryChooser.showDialog(null);
        	if (file != null) {
        		openFile(file);
        	}
        });
 
        //pastes and image or video from clip board. 
        final PamButton paste = new PamButton();
//        paste.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.CONTENT_PASTE, PamGuiManagerFX.iconSize));
        paste.setGraphic(PamGlyphDude.createPamIcon("mdi2c-content-paste", PamGuiManagerFX.iconSize));
        paste.setTooltip(new Tooltip("Open multiple images and video files "));
        paste.setOnAction((action)->{
        	pasteFromClipboard(); 
        });
 
        //holder for file selection buttons
        final PamGridPane inputGridPane = new PamGridPane();
        inputGridPane.prefWidthProperty().bind(holder.widthProperty());

        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.add(new Label("Select File(s)"), 1, 0);
        inputGridPane.add(openButton, 2, 0);
        inputGridPane.add(openMultipleButton, 3, 0);
        inputGridPane.add(paste, 4, 0);

        //arrows to move to next or previous image. 
        
        final PamHBox arrowBox= new PamHBox(); 
        arrowBox.prefWidthProperty().bind(holder.widthProperty());

        
        final PamButton arrowRight = new PamButton();
//        arrowRight.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.SKIP_NEXT, PamGuiManagerFX.iconSize));
        arrowRight.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-next", PamGuiManagerFX.iconSize));
        arrowRight.setTooltip(new Tooltip("Move to next image/video"));
        arrowRight.getStyleClass().add("square-button-trans");
        arrowRight.prefWidthProperty().bind(arrowBox.widthProperty().divide(2));

        arrowRight.setOnAction((action)->{
        	nextImage(true) ;
        });
        
        final PamButton  arrowLeft = new PamButton();
//        arrowLeft.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.SKIP_PREVIOUS, PamGuiManagerFX.iconSize));
        arrowLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-previous", PamGuiManagerFX.iconSize));
        arrowLeft.setTooltip(new Tooltip("Move to previous image/video"));
        arrowLeft.getStyleClass().add("square-button-trans");
        arrowLeft.prefWidthProperty().bind(arrowBox.widthProperty().divide(2));

        arrowLeft.setOnAction((action)->{
        	nextImage(false); 
        });
       
        HBox.setHgrow(arrowRight, Priority.ALWAYS);
        HBox.setHgrow(arrowLeft, Priority.ALWAYS);
        arrowBox.getChildren().addAll(arrowLeft, arrowRight); 
        
        //image label
        imageNameLabel = new Label(); 
        imageNameLabel.setWrapText(true);
        
        imageTimeLabel= new Label(); 
        imageTimeLabel.setWrapText(true);

        //settings
        final Label settingsLabel = new Label("General Settings");
        PamGuiManagerFX.titleFont2style(settingsLabel);
//		settingsLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
//		final PamButton settingsButton = new PamButton("Settings", PamGlyphDude.createPamGlyph(MaterialDesignIcon.SETTINGS, PamGuiManagerFX.iconSize));
		final PamButton settingsButton = new PamButton("Settings", PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		settingsButton.setTooltip(new Tooltip("Quick access to video range settings"));
		settingsButton.prefWidthProperty().bind(holder.widthProperty());
		settingsButton.setStyle("-fx-border-color: -fx_border_col;"); 
		settingsButton.setOnAction((action)->{
			vrDisplay.settingsButton(); 
		});
		
		mousePosition = new Label("-"); 
   
        holder.getChildren().addAll(selectLabel, inputGridPane, arrowBox, imageNameLabel, imageTimeLabel, createMethodPane(), settingsLabel, settingsButton, mousePosition); 
        holder.setPadding(new Insets(5,5,5,5));
        
        this.update(VRControl.METHOD_CHANGED);
        this.update(VRControl.IMAGE_CHANGE);

        
        return holder; 
        
	}
	
	/**
	 * Paste the current image from clipboard. 
	 */
	private void pasteFromClipboard() {
		this.vrDisplay.pasteImage(); 
	}


	/**
	 * Create a pane to allow users to change method. 
	 */
	private Pane createMethodPane() {
	
        final Label selectLabel = new Label("Select Method");
        PamGuiManagerFX.titleFont2style(selectLabel);

//		selectLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
		methodHolder= new PamBorderPane(); 
		
		measurementType = new ComboBox<String>(); 
		
		for (int i=0; i<vrDisplay.getVrControl().getMethods().size(); i++){
			measurementType.getItems().add(vrDisplay.getVrControl().getMethods().get(i).getName());
		}
		measurementType.getSelectionModel().select(vrDisplay.getVrControl().getVRParams().methodIndex);//set to the correct index before action listner. 
		measurementType.setOnAction((action)->{
			int index= measurementType.getSelectionModel().getSelectedIndex(); 
			vrDisplay.getVrControl().setVRMethod(index);
			//when the method changes need to change the pane
		});
		
		PamVBox methodsPane= new PamVBox(); 
		methodsPane.setSpacing(5);
		measurementType.prefWidthProperty().bind(methodsPane.widthProperty());
		methodsPane.getChildren().addAll(selectLabel, measurementType , methodHolder); 
		
		return methodsPane;
		
	}
	
	/**
	 * Set the label which shows the image name 
	 * @param file - the current image file. Null if image is pasted. 
	 */
	private void setImageLabel(PamImage pamImage) {
		if (pamImage!=null && pamImage.getImageFile()!=null) {
			this.imageNameLabel.setText(pamImage.getImageFile().getName()); 
		}
		else {
			this.imageNameLabel.setText("Pasted Image:"); 
		}
	}
	
	/**
	 * Set the label which shows the image name 
	 * @param file - the current image file. Null if image is pasted. 
	 */
	private void setImageTimeLabel() {
			this.imageTimeLabel.setText(vrDisplay.getVrControl().getImageTimeString()); 
	}

	
	/**
	 * Open a new image/media file. 
	 * @param file - the file to open
	 */
	private void openFile(File file) {
		vrDisplay.openFile(file);
	}
	
	/**
	 * Move to the next media file. 
	 */
	private void nextImage(boolean next) {
		vrDisplay.nextImage(next);
	}
	
	
	public void update(int updateType) {
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			if (this.vrDisplay.getCurrentImage()!=null) {
				setImageTimeLabel();
			}
			break;
		case VRControl.IMAGE_CHANGE:
			if (this.vrDisplay.getCurrentImage()!=null) {
				setImageLabel(this.vrDisplay.getCurrentImage());
				setImageTimeLabel();
			}
			break;
		case VRControl.METHOD_CHANGED:
			changeMethod();
			break;		
		case VRControl.IMAGE_TIME_CHANGE:
			if (this.vrDisplay.getCurrentImage()!=null) {
				setImageTimeLabel();
			}
			break;
		}
	}

	/**
	 * Called whenever the VRMethod changes. 
	 */
	private void changeMethod() {
		if (vrDisplay.getVrControl().getCurrentMethod().getOverlayFX()!=null){
//		System.out.println("VRSidePaneFX: The VR Method has changed: " + vrDisplay.getVrControl().getCurrentMethod().getOverlayFX().getSidePaneFX() + " " +
//				vrDisplay.getVrControl().getCurrentMethod().getName());
		methodHolder.setCenter(vrDisplay.getVrControl().getCurrentMethod().getOverlayFX().getSidePaneFX());
		}
		else {
			methodHolder.setCenter(null); 
		} 
	}


	
	/**
	 * Called whenever the mouse is moved within the image pane. 
	 * @param event
	 */
	public void updateMouse(MouseEvent event) {
		
		Point imagePoint = vrDisplay.localToImage(new Point2D(event.getX(), event.getY())); 
		
		mousePosition.setText(String.format("Image location: x: %.2f y: %.2f", imagePoint.getX(), imagePoint.getY())); 
	}


}
