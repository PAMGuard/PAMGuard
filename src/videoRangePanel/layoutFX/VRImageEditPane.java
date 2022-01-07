package videoRangePanel.layoutFX;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.sliders.PamSlider;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Pane for editing an image or video 
 * @author Jamie Macaulay 
 *
 */
public class VRImageEditPane extends PamBorderPane {
	
	/**
	 * Slider to change the brightness of the image.
	 */
	private PamSlider brightness;

	/**
	 * Slider to change the contrast of the image.
	 */
	private PamSlider contrast;

	/**
	 * Slider to change the hue of the image.
	 */
	private PamSlider hue;

	/**
	 * Slider to change the saturation of the image.
	 */
	private PamSlider saturation;
	
	/**
	 * 
	 * The effect for the image view. Adjusts image colours.  
	 */
	private  ColorAdjust colorAdjust = new ColorAdjust();
	
	
	
	public VRImageEditPane() {
		this.setCenter(createImageEditPane());
	}

    
    /**
     * Create the image edit pane. This has controls to change the brightness, contrast, 
     * hue and saturation of the image.
     * @return the image edit pane. 
     */
    private Pane createImageEditPane() {
    	
    	VBox vBox = new VBox(); 
    	vBox.setPadding(new Insets(15,25,0,25));
    	vBox.setSpacing(10);

    	Label brightnessLabel = new Label("Brightness");
		PamGuiManagerFX.titleFont2style(brightnessLabel);
    	//brightnessLabel.setFont(PamGuiManagerFX.titleFontSize2);
    	brightness= new PamSlider(-1,1, 0); 
    	brightness.setTrackColor(Color.GRAY);
    	brightness.valueProperty().addListener((obsVal, oldVal, newVal)->{
    		colorAdjust.setBrightness(newVal.doubleValue());
    	});

    	Label contrastLabel = new Label("Contrast");
		PamGuiManagerFX.titleFont2style(contrastLabel);
    	//contrastLabel.setFont(PamGuiManagerFX.titleFontSize2);
    	contrast= new PamSlider(-1,1, 0); 
    	contrast.setTrackColor(Color.GRAY);
    	contrast.valueProperty().addListener((obsVal, oldVal, newVal)->{
    		colorAdjust.setContrast(newVal.doubleValue());
    	});

    	Label hueLabel = new Label("Hue");
		PamGuiManagerFX.titleFont2style(hueLabel);
    	//hueLabel.setFont(PamGuiManagerFX.hueLabel);
    	hue= new PamSlider(-1,1, 0); 
    	hue.setTrackColor(Color.GRAY);
    	hue.valueProperty().addListener((obsVal, oldVal, newVal)->{
    		colorAdjust.setHue(newVal.doubleValue());
    	});

    	Label saturationLabel = new Label("Saturation");
		PamGuiManagerFX.titleFont2style(saturationLabel);
    	//saturationLabel.setFont(PamGuiManagerFX.titleFontSize2);
    	saturation= new PamSlider(-1,1, 0); 
    	saturation.setTrackColor(Color.GRAY);
    	saturation.valueProperty().addListener((obsVal, oldVal, newVal)->{
    		colorAdjust.setSaturation(newVal.doubleValue());
    	});
    	
    	PamButton button = new PamButton(); 
//    	button.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.BACKUP_RESTORE, PamGuiManagerFX.iconSize));
    	button.setGraphic(PamGlyphDude.createPamIcon("mdi2b-backup-restore", PamGuiManagerFX.iconSize));
    	button.setText("Reset");
    	button.setOnAction((action)->{
    		brightness.setValue(0);
    		contrast.setValue(0);
    		hue.setValue(0);
    		saturation.setValue(0);
    	});
    	button.getStyleClass().add("square-button-trans");
    	StackPane.setAlignment(button, Pos.CENTER_RIGHT);
    	//button.layoutXProperty().bind(vBox.widthProperty().subtract(button.widthProperty()));

    	
    	vBox.getChildren().addAll(brightnessLabel, brightness, contrastLabel,
    			contrast, hueLabel, hue, saturationLabel, saturation, new StackPane(button)); 

    	return vBox; 

    }
    
	/**
	 * Get the colour adjust which is chnaged with the slider controls. 
	 * @return the colour adjust. 
	 */
	public ColorAdjust getColorAdjust() {
		return colorAdjust;
	}   
    
    /**
     * Set the colour of the slider bars to the average colour of the image. 
     */
    public void setSliderColours(Image image) {
    	Color col=PamUtilsFX.getDominantColor(image); 
    	contrast.setTrackColor(col); 
    	brightness.setTrackColor(col); 
    	hue.setTrackColor(col); 
    	saturation.setTrackColor(col); 
    }


}
