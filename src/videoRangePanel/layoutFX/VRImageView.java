package videoRangePanel.layoutFX;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import videoRangePanel.pamImage.PamImage;

/**
 * Holds the image and can be used to PAN and zoom
 * @author Jamie Macaulay
 *
 */
public class VRImageView extends PamBorderPane implements VRImage {
	
	/**
	 * The minimum pixel size of the pixels. 
	 */
	public static final int MIN_PIXELS=10; 

	/**
	 * The image view
	 */
    ImageView imageView;


	private PamImage pamImage;


	private PamButton nextMedia;


	private PamButton prevMedia;

	/**
	 * Reference ot the main FX video range display. 
	 */
	private VRDisplayFX vrDisplayFX;

	/**
	 * The control pane which sits at the bottom of the image in a hiding pane. 
	 */
	private Pane controlPane; 

	/**
	 * The edit pane. Allows user to change brightness etc. 
	 */
	private VRImageEditPane editPane;



	
	public VRImageView(VRDisplayFX vrDisplayFX){
		this.vrDisplayFX=vrDisplayFX; 
		controlPane = createImageControls(); 
		editPane=new VRImageEditPane(); 
	}
	
	@Override
	public boolean setMedia(File currentFile) {
		return setImage(currentFile);
	}
	
	
	/**
	 * Set the current image from a file and creates a PamImage data unit which is added tot he control. 
	 * @param currentFile - the current image file. 
	 * @return true if the image is successfully loaded. 
	 */
	public boolean setImage(File currentFile) {
		//System.out.println("VRImagePaneFX: Set Image");
		
		Image image = null ; 
        try {
			image = new Image(currentFile.toURI().toURL().toExternalForm());
			
			this.pamImage = new PamImage(currentFile, vrDisplayFX.getVrControl().getImageTimeParser());
			vrDisplayFX.getVrControl().setCurrentImage(pamImage);
			//set slider colours. 
			editPane.setSliderColours( image);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false; 
		}
        
        setImage(image) ;
		return true; 
	}
	
	
	/**
	 * Set the image in the image pane. Only use if the im
	 * @param image
	 */
	public void setImage(PamImage image) {
//		System.out.println("VRImageView: Set a pamImage"); 
		 if (image.getImageFile()!=null) {
//				System.out.println("VRImageView: Set a pamImage (has a file)"); 
			 //use the file to load the image. 
			 setImage(image.getImageFile());
		 }
		 else {
//			System.out.println("VRImageView: Set a pamImage (has NO file)"); 
			this.setImageView(image.getImageFX());
		 }
	}
	
	/**
	 * Set the image in the imageview. 
	 * @param image - the image to set. 
	 */
	private void setImage(Image image) {
		this.setImageView(image);
	}

	/**
	 * Setup the imageview i.e. the may node which holds the image. 
	 * @param image - the image to set
	 * @return the ImageView. 
	 */
    private ImageView setImageView(Image image) {

        if (imageView==null) {
        	imageView = new ImageView(image);
        	imageView.setCache(true);
        	imageView.setCacheHint(CacheHint.SPEED);
    		this.setCenter(imageView); 
        }
        else imageView.setImage(image);
        
        imageView.setEffect(editPane.getColorAdjust());
   

        return imageView;
    }
    
    /**
     * Create controls to move to next picture 
     * @return move to next picture 
     */
    public Pane createImageControls() {
    	HBox imageControlPane = new HBox(); 
		imageControlPane.setSpacing(10);
		imageControlPane.setAlignment(Pos.CENTER);
		
		nextMedia= new PamButton(); 
		nextMedia.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-next",VRMediaView.controlIconSize));
		nextMedia.getStyleClass().add("square-button-trans");
		nextMedia.setOnAction((action)->{
			vrDisplayFX.nextImage(true);
		});
		
		prevMedia= new PamButton(); 
//		prevMedia.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SKIP_PREVIOUS		,VRMediaView.controlIconSize));
		prevMedia.setGraphic(PamGlyphDude.createPamIcon("mdi2s-skip-previous",VRMediaView.controlIconSize));
		prevMedia.getStyleClass().add("square-button-trans");
		prevMedia.setOnAction((action)->{
			vrDisplayFX.nextImage(false);
		});

		imageControlPane.getChildren().addAll(prevMedia, nextMedia); 
		
		return imageControlPane; 
    }
    

	@Override
	public Region getControlPane() {
		return controlPane;
	}

	@Override
	public Node getNode() {
		return this;
	}

	@Override
	public ArrayList<String> getMetaData() {
		if (pamImage!=null) {
			return pamImage.getMetaDataText();
		}
		else return null;
	}

	@Override
	public Region getImageEditPane() {
		return editPane;
	}

	@Override
	public int getImageHeight() {
		return (int) imageView.getImage().getHeight();
	}

	@Override
	public int getImageWidth() {
		return (int) imageView.getImage().getWidth();
	}


}
