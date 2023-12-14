package videoRangePanel.layoutFX;


import java.awt.Point;
import java.io.File;
import java.util.List;

import PamView.dialog.PamDialog;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import videoRangePanel.VRControl;
import videoRangePanel.VRPane;
import videoRangePanel.pamImage.ImageFileFilter;
import videoRangePanel.pamImage.PamImage;

/**
 * The main display. 
 * @author Jamie Macaulay
 *
 */
public class VRDisplayFX extends PamBorderPane implements VRPane {

	/**
	 * The left hiding pane. 
	 */
	private HidingPane hidingPaneLeft;

	/**
	 * Settings pane.
	 */
	private Pane sidePaneHolder;

	/**
	 * Holds the images, mouse overlay canvas, hiding pane buttons etc. 
	 */
	private StackPane mainImageHolder;

	/**
	 * Pane which holds the image. 
	 */
	private VRImagePane vrImagePane;

	/**
	 * Pane which holds a video 
	 */
	private VRMediaView vrVideoPane;

	/**
	 * The side pane where most controls are located
	 */
	private VRSidePaneFX vrSidePane;

	/**
	 * Reference to the video range controller. 
	 */
	private VRControl vrControl; 

	/**
	 * Filter for image files 
	 */
	private ImageFileFilter imageFileFilter; 

	/**
	 * The main settings pane. 
	 */
	private VRSettingsPane vrSettingsPane;

	/**
	 * The mian holder. Allows overlaid panes to be added without layout
	 * contraints of StackPane. 
	 */
	private Pane mainHolder; 

	public VRDisplayFX(VRControl vrControl) {
		//this.setCenter(new Label("I'm a JavaFX display")); 
		this.vrControl=vrControl; 
		imageFileFilter= new ImageFileFilter(); 

		createDisplay();

		vrSettingsPane= new VRSettingsPane(vrControl, mainHolder); 
		vrSettingsPane.setParams(vrControl.getVRParams());
		vrSettingsPane.addSettingsListener(()->{
			// need to update settings. 
			vrControl.setVRParams(vrSettingsPane.getParams(vrControl.getVRParams()));
			vrControl.update(VRControl.SETTINGS_CHANGE);
		});


		((Region) vrSettingsPane.getContentNode()).setPadding(new Insets(10,10,10,25));
	}


	/**
	 * Create the display. 
	 */
	private void createDisplay(){

		PamBorderPane holder = new PamBorderPane(); 

		mainImageHolder = new StackPane(); 

		this.vrImagePane= new VRImagePane(this); 
		mainImageHolder.getChildren().add(vrImagePane); 
		vrImagePane.maxWidthProperty().bind(mainImageHolder.widthProperty());

		hidingPaneLeft=new HidingPane(Side.LEFT, sidePaneHolder=createSidePane(), holder, false);
		hidingPaneLeft.showHidePane(false);
		hidingPaneLeft.setStyle("-fx-background-color: -fx-background;");

		//hidingPaneLeft.getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamCSS());

		PamButton showButtonLeft=hidingPaneLeft.getShowButton();
//		showButtonLeft.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.BARS, PamGuiManagerFX.iconSize));
		showButtonLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2m-menu", PamGuiManagerFX.iconSize));
		showButtonLeft.getStyleClass().add("close-button-right");
		showButtonLeft.setStyle(" -fx-background-radius: 0 0 0 0;");

		mainImageHolder.getChildren().add(showButtonLeft); 
		StackPane.setAlignment(showButtonLeft, Pos.TOP_LEFT);
		showButtonLeft.layoutYProperty().setValue(30);
		mainImageHolder.setStyle("-fx-background-color: black;");


		//		this.widthProperty().addListener((obsVal, oldVal, newVal)->{
		//			System.out.println("mainImageHolder: " + mainImageHolder.getWidth() 
		//			+ " max: " + mainImageHolder.getMaxWidth() + " holder: " + holder.getWidth() 
		//			+ " LeftHidingPane: " + hidingPaneLeft.widthProperty().get());
		//		});


		PamButton closeRightButton=hidingPaneLeft.getHideButton();
		closeRightButton.setPrefWidth(40);
		closeRightButton.getStyleClass().add("close-button-left-trans");
//		closeRightButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, PamGuiManagerFX.iconSize));
		closeRightButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));

		//add hiding pane to main pane. 
		holder.setLeft(hidingPaneLeft);
		//add center pane after to ensure it's on top
		holder.setCenter(mainHolder = new Pane(mainImageHolder));
		mainImageHolder.prefWidthProperty().bind(mainHolder.widthProperty());
		mainImageHolder.prefHeightProperty().bind(mainHolder.heightProperty());

		this.setCenter(holder); 

		//need to open image file here because the FX stuff takes a bit of time to load 
		//on the FX thread. If this is attempted in control constructor the FX GUI has not 
		//loaded and so a null pointer exception is thrown. 
		if (this.vrControl.getVRParams().currentImageFile!=null && vrControl.getVRParams().currentImageFile.exists()) {

			vrImagePane.openNewFile(this.vrControl.getVRParams().currentImageFile);
			this.vrSidePane.update(VRControl.IMAGE_CHANGE); 
		}


	}

	public HidingPane getHidingPaneLeft() {
		return hidingPaneLeft;
	}


	public Pane getMainHolder() {
		return mainHolder;
	}


	public void bindTest() {
		mainImageHolder.maxWidthProperty().bind(this.getScene().widthProperty().subtract(hidingPaneLeft.widthProperty()));

	}

	/**
	 * Create the side pane. 
	 * @return the side pane.
	 */
	private Pane createSidePane(){

		PamBorderPane settingsPane = new PamBorderPane(); 
		settingsPane.setPrefWidth(250);
		settingsPane.setCenter(vrSidePane = new VRSidePaneFX(this)); 

		return settingsPane; 

	}

	/**
	 * Open a new selected file or directory. 
	 * @param file any filder or folder. 
	 */
	public void openFile(File file) {
		vrControl.getVRParams().currentImageFile=null;
		//check if this is a folder in which case take the first image in that folder. 
		if (file.isDirectory()){
			File[] files=file.listFiles();
			for (int i=0; i<files.length; i++){
				if (this.imageFileFilter.accept(files[i])){
					vrControl.getVRParams().currentImageFile=files[i];
					vrControl.getVRParams().imageDirectory = file;  
					break;
				}
			}

		}
		else {
			vrControl.getVRParams().currentImageFile = file; 
			vrControl.getVRParams().imageDirectory = file.getParentFile(); 
		}

		if (vrControl.getVRParams().currentImageFile==null) {
			PamDialog.showWarning(vrControl.getGuiFrame(), "No image found", "The folder selected did not contain any compatible images");
			return;
		}
		//go back up a few levels to load file as update flags etc need to be triggerred. 
		vrControl.loadFile(vrControl.getVRParams().currentImageFile);
	}

	/**
	 * Load a an image of media file
	 * @param file - a single image or media file. Not a directory. This is handled earlier. 
	 * @return true if the file loads successfully
	 */
	public boolean openImageFile(File file) {
		this.vrImagePane.openNewFile(file);
		update(VRControl.IMAGE_CHANGE);
		return true;
	}

	/**
	 * Get VRControl
	 * @return the controller for the display. 
	 */
	public VRControl getVrControl() {
		return vrControl;
	}

	/**
	 * Move to the next or previous image; 
	 * @param next - true to move to the next image
	 */
	public void nextImage(boolean next) {
		//check file is not null;
		if (vrControl.getVRParams().currentImageFile==null) return;
		//check the file exists.
		if(!vrControl.getVRParams().currentImageFile.exists()) return; 

		File newFile=vrControl.findNextFile(vrControl.getVRParams().currentImageFile, new ImageFileFilter(), next);

		if (newFile!=null){
			vrControl.getVRParams().currentImageFile=newFile; 
			vrControl.loadFile(newFile);	
			vrControl.update(VRControl.IMAGE_CHANGE);
		}
	}


	@SuppressWarnings("unchecked")
	boolean pasteImage() {

		Clipboard cb = Clipboard.getSystemClipboard();
		try {
			if (cb.hasImage()) {
//				System.out.println("Pasting an image directly from the clipbaord"); 
				Image image = cb.getImage();
//				System.out.println("Pasting an image directly from the clipbaord"); 
				PamImage pamImage=new PamImage(image, 0);
				vrControl.setCurrentImage(pamImage);
				this.newImage();
			}
			else if (cb.hasFiles()) {
				List<File> imageFile = cb.getFiles(); 
//				System.out.println("Pasting image file: "+imageFile.get(0));
				return openImageFile(imageFile.get(0));   
			}
		}
		catch (Exception e) {

		}
		return false;

	}

	/**
	 * Receives update flags from the controller. 
	 * @param updateType - flag indicating the type of update. 
	 */
	public void update(int updateType) {
		//System.out.println("The VRDisplayFX has received an update flag: " + updateType);
		this.vrSidePane.update(updateType);
		this.vrImagePane.update(updateType); 
		this.vrSettingsPane.update(updateType); 
	}

	/**
	 * Open the settings dialog.
	 */
	public void settingsButton() {
		//show the old swing dialog
		//		SwingUtilities.invokeLater(()->{
		//			vrControl.settingsButtonAWT(null, -1);
		//		});
		vrImagePane.showSettingsPane();
	}

	/**
	 * Open the settings dialog.
	 * @param tab - the tab to select on setting dialog. 
	 */
	public void settingsButton(int tab) {
		//		SwingUtilities.invokeLater(()->{
		//			vrControl.settingsButtonAWT(null, tab);
		//		});
		this.vrSettingsPane.setTab(tab);
		vrImagePane.showSettingsPane();

	}

	/**
	 * Get the side pane 
	 * @return - the side pane.
	 */
	public VRSidePaneFX getSidePane() {
		return this.vrSidePane;
	}

	@Override
	public void repaint() {
		if (vrImagePane!=null) {
			vrImagePane.clearOverlay(); 
			if (this.vrControl.getCurrentMethod().getOverlayFX()!=null)
				this.vrControl.getCurrentMethod().getOverlayFX().paint(vrImagePane.getDrawCanvas().getGraphicsContext2D());
		}
	}

	@Override
	public int getImageWidth() {
		return this.vrImagePane.getImageWidth();
	}

	@Override
	public int getImageHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Point imageToScreen(Point point) {
		Point pointAWT= this.vrImagePane.imageToLocal(point);
		Point2D newPoint = this.vrImagePane.localToScreen(new Point2D(pointAWT.getX(), pointAWT.getY()));
		point.setLocation(newPoint.getX(), newPoint.getY()); 
		return point; 
	}

	@Override
	public Point screenToImage(Point point) {
		Point2D point2d=vrImagePane.screenToLocal(new Point2D(point.getX(), point.getY()));
		Point pointLocal = new Point();
		pointLocal.setLocation(point2d.getX(), point2d.getY());
		return this.vrImagePane.localToImage(pointLocal);
	}

	@Override
	public void newImage() {
		this.vrImagePane.openNewImage(this.vrControl.getCurrentImage()); 
		update(VRControl.IMAGE_CHANGE);
	}

	/**
	 * Convert local co-ordinates to image location. 
	 * @param point2d - the point 
	 * @return point on image. 
	 */
	public Point localToImage(Point2D point2d) {
		Point pointLocal = new Point();
		pointLocal.setLocation(point2d.getX(), point2d.getY());
		return this.vrImagePane.localToImage(pointLocal);
	}

	/**
	 * Get the image pane.
	 * @return the image pane. 
	 */
	public VRImagePane getImagePane() {
		return vrImagePane; 
	}

	/**
	 * Get the current image
	 * @return the current image. 
	 */
	public PamImage getCurrentImage() {
		return vrControl.getCurrentImage();
	}

	/**
	 * The main settings pane for VRParameters
	 * @return the vrSettingsPane 
	 */
	public VRSettingsPane getVrSettingsPane() {
		return vrSettingsPane;
	}



}
