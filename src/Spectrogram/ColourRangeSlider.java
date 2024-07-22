package Spectrogram;

import javax.swing.JSlider;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.sliders.PamRangeSlider;

/**
 * A range slider which shows a colour gradient between two thumbs. 
 * 
 * @author Jamie Macaulay
 */
public class ColourRangeSlider extends PamRangeSlider {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ColourRangeSlider(){
		super(JSlider.VERTICAL);
	}
	
	public ColourRangeSlider(int min, int max){
		super(min, max,JSlider.VERTICAL);
	}
	
	public ColourRangeSlider(int min, int max, int orientation){
		super(min, max,orientation);
	}
	
	public ColourRangeSlider(int orientation){
		super(orientation);
	}
	
	/**
	 * Set the colour map type to show between the two thumbs. 
	 * @param colourMap - the colour map to show. 
	 */
	public void setColourMap(ColourArrayType colourMap){
		((ColourRangeSliderUI) getUI()).setColourMap(colourMap);
	}
	
	/**
	 * Set a custom colour map type to show between the two thumbs. 
	 * @param colourMap - the colour map to show. 
	 */
	public void setColourMap(ColourArray colourMap){
		((ColourRangeSliderUI) getUI()).setColourMap(colourMap);
	}
	
	 /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new ColourRangeSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }
    
    
    


}
