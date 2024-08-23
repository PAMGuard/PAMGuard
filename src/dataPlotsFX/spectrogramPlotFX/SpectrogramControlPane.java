package dataPlotsFX.spectrogramPlotFX;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.comboBox.ColorComboBox;
import pamViewFX.fxNodes.sliders.ColourRangeSlider;
import pamViewFX.fxNodes.sliders.PamRangeSlider;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * A general pane with a frequency slider, colour bar slider and colour combo
 * box. These nodes are interconnected so when colour combo box is changed
 * colours change. Other than that this pane does nothing but all slider value
 * properties are available to be used in another class or subclass. This means
 * this control pane should be available to be used for any spectrogram/3D
 * colour image.
 * 
 * @author Jamie Macaulay
 *
 */
public class SpectrogramControlPane extends PamBorderPane {
	
	/**
	 * Range slider to change frequencies. 
	 */
	private PamRangeSlider frequencySlider;
	
	/**
	 * Slider to change colour limits. 
	 */
	private  ColourRangeSlider colourSlider;
	
	/**
	 * Allows the selection of colours. 
	 */
	private ColorComboBox colorBox; 
	
	/**
	 * The current colour array type of the  colour slider. Also indicates what 
	 */
	private ColourArrayType colourArrayType=ColourArrayType.RED;

	/**
	 * The frequency label. 
	 */
	private Label freqLabel;

	
	private Label ampLabel;
	
	/**
	 * Constructor for the spectrogram control pane. 
	 */
	public SpectrogramControlPane(){
		createSpectrogramControlPane(Orientation.VERTICAL,  true, true,true, true);
	}
	
	/**
	 * Constructor for the spectrogram control pane. 
	 * @param orientation default orientation of the controls. 
	 * @param showComboColour true to show the colour selection combobox. 
	 * @param showFreqSlider true to show the frequency slider.
	 * @param showColSlider true to show the colour scale slider. 
	 */
	public SpectrogramControlPane(Orientation orientation, boolean showComboColour, 
			boolean showFreqSlider, boolean showColSlider){
		createSpectrogramControlPane(orientation,  showComboColour, showFreqSlider,showColSlider, true);
	}
	
	/**
	 * Constructor for the spectrogram control pane. 
	 * @param orientation default orientation of the controls. 
	 * @param showComboColour true to show the colour selection combobox. 
	 * @param showFreqSlider true to show the frequency slider.
	 * @param showColSlider true to show the colour scale slider. 
	 * @param labels label the sliders e.g. Frequency, Amplitude
	 */
	public SpectrogramControlPane(Orientation orientation, boolean showComboColour, 
			boolean showFreqSlider, boolean showColSlider, boolean labels){
		createSpectrogramControlPane(orientation,  showComboColour, showFreqSlider,showColSlider, labels);
	}
	
	/**
	 * Create the pane. 
	 * @param orientation default orientation of the controls. 
	 * @param showComboColour true to show the colour selection combobox. 
	 * @param showFreqSlider true to show the frequency slider.
	 * @param showColSlider true to show the colour scale slider. 
	 */
	private void createSpectrogramControlPane(Orientation orientation, boolean showComboColour, 
			boolean showFreqSlider, boolean showColSlider, boolean labels){
		
	
		Pane sliderPane;
		if (orientation==Orientation.VERTICAL){
			PamHBox hBox=new PamHBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.setSpacing(5);
			sliderPane=hBox;
		}
		else {
			PamVBox hBox=new PamVBox();
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.setSpacing(5);
			sliderPane=hBox;
		}
							 		
		//create frequency slider
		if (showFreqSlider){
			//frequency label
			freqLabel = new Label("Frequency (kHz)"); 
			//need to have in group to measure rotation properly. 
			Group labelG = new Group(freqLabel); 
			if (orientation==Orientation.VERTICAL) freqLabel.setRotate(-90);
			
			//the frequency slider
			frequencySlider=new PamRangeSlider();
			frequencySlider.setShowTickMarks(true);
			frequencySlider.setShowTickLabels(true);
			frequencySlider.setOrientation(orientation);
			if (labels ) sliderPane.getChildren().add(new Group(freqLabel));
			sliderPane.getChildren().add(frequencySlider); 

		}

		//create colour slider
		if (showColSlider){
			//colour slider 
			colourSlider=new ColourRangeSlider();
			colourSlider.setShowTickMarks(true);
			colourSlider.setShowTickLabels(true);
			colourSlider.setOrientation(orientation);
			//amplifier label
//			String dBRef = GlobalMedium.getdBRefString(PamController.getInstance().getGlobalMediumManager().getCurrentMedium());
			String dBRef = "dB re \u00B5Pa/\u221AHz";
			ampLabel = new Label("Amplitude (" + dBRef + ")"); 
			Group ampG = new Group(ampLabel); 
			if (orientation==Orientation.VERTICAL){
				ampLabel.setRotate(-90);
				colourSlider.setPadding(new Insets(0,0,0,20));
			}
			if (labels ) sliderPane.getChildren().add(ampG);
			
			sliderPane.getChildren().add(colourSlider); 
			
			//for testing
//			sliderPane.getChildren().add(new ColourRangeSlider(Orientation.VERTICAL)); 
			
//			RangeSlider slider  = new RangeSlider(); 
//			slider.setOrientation(Orientation.VERTICAL);
//			sliderPane.getChildren().add(slider); 


		}
	
		
		if (showComboColour) {
			colorBox=new ColorComboBox(ColorComboBox.COLOUR_ARRAY_BOX);
			if (orientation==Orientation.VERTICAL) colorBox.setPrefWidth(150);
			else colorBox.setPrefWidth(50);//only set a width to keep vertical looking OK. 
			//Change the colour of the colour slider when combo box is changed. 
			colorBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(ObservableValue<? extends String> ov, String t, String t1) {                
					//change the colour of the colour range slider.     
					setColours();
				}
			});
			
			colorBox.setValue(ColourArray.getName(getColourArrayType()));
			if (orientation==Orientation.VERTICAL){
				this.setTop(colorBox);
				if (showFreqSlider){
					//colorBox.setPadding(new Insets(0,0,0,20));
					//colorBox.layoutXProperty().bind(this.frequencySlider.layoutXProperty());
				}
				else BorderPane.setAlignment(colorBox, Pos.TOP_CENTER);
				sliderPane.setPadding(new Insets(10,0,0,0));
			}
			else{
				this.setRight(colorBox);
				//need to set up alignment properly. //FIXME- a bit messy 
				BorderPane.setAlignment(colorBox, Pos.CENTER);
				//sliderPane.setPadding(new Insets(10,0,0,0));
				BorderPane.setMargin(colorBox, new Insets(0,5,0,5));
			}
		}
		
		this.setPrefWidth(300);
		this.setCenter(sliderPane);
		
		setColours();
	}
	
	/**
	 * Set colours depending on current colour selection in combo box. 
	 */
	public void setColours(){
    	this.setColourArrayType(ColourArray.getColourArrayType(colorBox.getValue()));
    	if (colourSlider!=null) colourSlider.setColourArrayType(getColourArrayType());
    	if (frequencySlider!=null) frequencySlider.setTrackColor(getTrackColour(getColourArrayType())); 
	}
	
	/**
	 * Get the track colour for the frequency slider. This is based on the colour array shown in the colour slider. 
	  * @return - the colour that the frequency slider should show. 
	 */
	public Color getTrackColour(){
		return getTrackColour(getColourArrayType());
	}
	
	/**
	 * Get the track colour for the frequency slider. This is based on the colour array shown in the colour slider. 
	 * @param colourArrayType - the colour array of the colour slider
	 * @return - the colour that the frequency slider should show. 
	 */
	private  Color getTrackColour(ColourArrayType colourArrayType){
		switch(colourArrayType){
		case BLUE:
				return Color.BLUE; 
		case GREEN:
			return Color.GREEN; 
		case GREY:
			return Color.GREY; 
		case HOT:
			return Color.CYAN; 
		case RED:
			return Color.RED; 
		case REVERSEGREY:
			return Color.GRAY; 
		case FIRE:
			return Color.YELLOW; 
		default:
			return Color.GRAY; 
			
		}
	}
	

	/**
	 * Get the range slider, usually used to change frequency. 
	 * @return range slider usually sued to change frequency.  
	 */
	public PamRangeSlider getFrequencySlider() {
		return frequencySlider;
	}
	
	

	/**
	 * Get the colour range slider usually used to change limits of colour map; 
	 * @return the colour range slider.  
	 */
	public ColourRangeSlider getColourSlider() {
		return colourSlider;
	}

	/**
	 * Get the colour combo box. Used to change the colur sliders
	 * @return the colour combo box which controls spectrogram colour array. 
	 */
	public ColorComboBox getColorBox() {
		return colorBox;
	}

	/**
	 * Get the current colourArrayType used. 
	 * @return the current colourArrayType used. 
	 */
	public ColourArrayType getColourArrayType() {
		return colourArrayType;
	}

	/**
	 * Set the current colourArrayType used. 
	 * @param the current colourArrayType used. 
	 */
	public void setColourArrayType(ColourArrayType colourArrayType) {
		this.colourArrayType = colourArrayType;
		if (colorBox!=null){
			colorBox.setValue(ColourArray.getName(getColourArrayType()));
		}
	}
	
	/**
	 * Get the frequency label pane
	 */
	public Label getFreqLabel() {
		return freqLabel; 
	}

	/**
	 * Set listeners, min max amplitude, amplitude low and high values. 
	 * @param amplitudeLimits. Double properties for amplitude limits. amplitudeLimits[0]=min amplitude (dB). amplitudeLimits[1]=max amplitude (dB)
	 */
	public void setAmplitudeProperties(DoubleProperty[] amplitudeLimits, double[] amplitudeRange){

		//set the limits for the amplitude slider. 
		setAmplitudeRange(amplitudeLimits, amplitudeRange);

		//set up the amplitude properties to change with slider. 
		setAmplitudeBinding(amplitudeLimits);

	} 

	/**
	 * Set the Amplitude binding. 
	 * @param amplitudeLimits- the amplitude values to bind to the colour slider min max. 
	 */
	private void setAmplitudeBinding(DoubleProperty[] amplitudeLimts){
		amplitudeLimts[0].bind(getColourSlider().lowValueProperty());
		amplitudeLimts[1].bind(getColourSlider().highValueProperty());
	}

	/**
	 * Set the amplitude range of the colour slider. 
	 * @param amplitudeLimits- the amplitude values to set the slider min max to. 
	 * @param maxAmplitudeLimits- the range of amplitude limits of the colour bar. 
	 */
	private void setAmplitudeRange(DoubleProperty[] amplitudeLimits, double [] amplitudeMaxRange){
		if (getColourSlider() == null) {
			return;
		}
		double minVal=amplitudeLimits[0].get();
		double maxVal=amplitudeLimits[1].get();
		double min=amplitudeMaxRange[0]; 
		double max=amplitudeMaxRange[1]; 
		setAmplitudeRange(minVal, maxVal, min, max); 

	}
	
	/**
	 * Set the amplitude range of the colour slider. 
	 * @param amplitudeLimits- the amplitude values to set the slider min max to. 
	 * @param maxAmplitudeLimits- the range of amplitude limits of the colour bar. 
	 */
	public void setAmplitudeRange(double[] amplitudeLimits, double [] amplitudeMaxRange){
		if (getColourSlider() == null) {
			return;
		}
		double minVal=amplitudeLimits[0];
		double maxVal=amplitudeLimits[1]; 
		double min=amplitudeMaxRange[0]; 
		double max=amplitudeMaxRange[1]; 
		setAmplitudeRange(minVal, maxVal, min, max); 
	}
	
	/**
	 * Set the amplitudes and limits for the colour slider
	 * @param minAmplitude - the minimum amplitude value
	 * @param maxAmplitude - the maximum amplitude value
	 * @param minAmplitudeLimit - the minimum limit
	 * @param maxAmplitudeLimts - the maximum limit
	 */
	private void setAmplitudeRange(double minAmplitude, double maxAmplitude, double minAmplitudeLimit, double maxAmplitudeLimts) {
		if (minAmplitude>=maxAmplitudeLimts) minAmplitude=0;
		if (maxAmplitude>maxAmplitudeLimts) maxAmplitude=maxAmplitudeLimts; 
		getColourSlider() .setMin(minAmplitudeLimit);
		getColourSlider() .setMax(maxAmplitudeLimts); 
		getColourSlider() .setLowValue(minAmplitude);
		getColourSlider() .setHighValue(maxAmplitude);
	}
	
	/**
	 * Convenience class to get low value from colour slider. 
	 * @return the low value from the colour slider
	 */
	public double getLowValue() {
		return 	getColourSlider().getLowValue(); 
	}
	
	/**
	 * Convenience class to get high value from colour slider. 
	 * @return the high value from the colour slider
	 */
	public double getHighValue() {
		return 	getColourSlider().getHighValue(); 
	}

}
