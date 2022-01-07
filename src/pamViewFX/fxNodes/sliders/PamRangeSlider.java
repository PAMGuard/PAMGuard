package pamViewFX.fxNodes.sliders;

import org.controlsfx.control.RangeSlider;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.sliders.skin.DefaultSliderLabel;
import pamViewFX.fxNodes.sliders.skin.RangeSliderLabel;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * A range slider with some extra functionality such as changing the track colour and 
 * text labels for the thumbs. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamRangeSlider extends RangeSlider {
	
	/**
	 * The low thumb
	 */
	private Pane lowThumb;

	/**
	 * The high thumb
	 */
	private Pane highThumb;

	/**
	 * The track 
	 */
	private Pane track;

	/**
	 * The range bar (node between the thumbs)
	 */
	private Pane rangeBar;

	/**
	 * The track colour.
	 */
	private Color trackColour=Color.PALEGREEN;

	/**
	 * Low label text field.
	 */
	private Label lowLabel;
	
	/**
	 * The high label. 
	 */
	private Label highLabel;

	/**
	 * The format for the lower thumb label
	 */
	private RangeSliderLabel lowLabelFormat = new DefaultSliderLabel();

	/**
	 * The format for the higher thumb label
	 */
	private RangeSliderLabel highLabelFormat = new DefaultSliderLabel();


	/**
	 * True to show value labels when thumbs are moved. 
	 */
	private boolean showChangingLabels=true; 
	
	
	public PamRangeSlider() {
		super(); 
	}
	
	public PamRangeSlider(Orientation orientation) {
		super(); 
		this.setOrientation(orientation);
	}

	
	@Override
	public void layoutChildren() {
		super.layoutChildren();
		//initialise the childrne we need. 
		if (lowThumb==null || highThumb==null 
				|| track==null || rangeBar==null) {
			initColorTracks();
			//don;t try anything else if the track is null. 
			if (track ==null) return;
			initLowLabel();
			initHighLabel(); 
			setTrackColor(trackColour); 
		}
	}
	
	/**
	 * Extract the tracks from CSS. 
	 */
	protected void initColorTracks() {
		this.lowThumb = (Pane) this.lookup(".low-thumb"); 
		this.highThumb = (Pane) this.lookup(".high-thumb");
		this.track = (Pane)  this.lookup(".track"); 
		this.rangeBar = (Pane) this.lookup(".range-bar"); 
	}
		
	/**
	 * Set the colour of the range bar. 
	 * @param trackColour
	 */
	public void setTrackColor(Color trackColour) {
		//System.out.println("Hello setTrackColour()"); 
		this.trackColour=trackColour; 
		if (getRangeBar()!=null) {
			getRangeBar().setStyle("-fx-background-color: " + PamUtilsFX.color2Hex(trackColour));
		}
	}
	

	protected void initLowLabel(){
		lowLabel = new Label(); 
		lowLabel.setVisible(true);

		final StackPane lowLabelPane=new StackPane(lowLabel);
		getChildren().add(lowLabelPane);
		//lowLabel.setMinWidth(50);
		//lowLabel.setPrefWidth(50);
		lowLabelPane.toBack();
		lowLabel.setMinWidth(Region.USE_PREF_SIZE);
		lowLabel.setVisible(false);

		lowLabelPane.layoutXProperty().bind(lowThumb.layoutXProperty().subtract(lowLabel.widthProperty()).add(10));
		lowLabelPane.layoutYProperty().bind(lowThumb.layoutYProperty().add(lowThumb.heightProperty().divide(2)));
		lowLabelPane.layoutYProperty().addListener((obs, oldval, newval)->{
			lowLabel.setText(lowLabelFormat.getText(getLowValue()));
		});
		
		this.lowValueChangingProperty().addListener((obsVal, oldVal, newVal)->{
			if (showChangingLabels) {
				lowLabel.setVisible(newVal);
			}
			else lowLabel.setVisible(false);
		});
		
		requestLayout();
	}
	
	
	protected void initHighLabel(){
		highLabel = new Label(); 
		highLabel.setVisible(true);

		final StackPane highLabelPane=new StackPane(highLabel);
		getChildren().add(highLabelPane);
		//highLabel.setMinWidth(50);
		//highLabel.setPrefWidth(50);
		highLabelPane.toBack();
		highLabel.setMinWidth(Region.USE_PREF_SIZE);
		highLabel.setVisible(false);

		highLabelPane.layoutXProperty().bind(highThumb.layoutXProperty().subtract(highLabel.widthProperty()).add(10));
		highLabelPane.layoutYProperty().bind(highThumb.layoutYProperty().add(highThumb.heightProperty().divide(2)));
		highLabelPane.layoutYProperty().addListener((obs, oldval, newval)->{
			highLabel.setText(highLabelFormat.getText(getHighValue()));
		});
		
		this.highValueChangingProperty().addListener((obsVal, oldVal, newVal)->{
			if (showChangingLabels) {
				highLabel.setVisible(newVal);
			}
			else highLabel.setVisible(false);
		});
		

		requestLayout();
	}

	public void setHighLabelFormat(RangeSliderLabel rangSliderLabel){
		this.highLabelFormat=rangSliderLabel;
	}	


	public void setLowLabelFormat(RangeSliderLabel rangSliderLabel){
		this.lowLabelFormat=rangSliderLabel;
	}

	/**
	 * Get the track- the background note of the slider track. 
	 * @return the track. 
	 */
	protected Pane getTrack(){
		return track; 
	}


	protected Pane getHighThumb(){
		return highThumb; 
	}

	protected Pane getLowThumb(){
		return lowThumb; 
	}
	
	/**
	 * @return the track
	 */
	public Pane getRangeBar() {
		return rangeBar;
	}

}
