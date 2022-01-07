package Spectrogram;

import javax.swing.JSlider;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.sliders.PamRangeSlider;

public class ColourRangeSlider extends PamRangeSlider {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ColourRangeSlider(){
		super(JSlider.VERTICAL);
	}
	
	public ColourRangeSlider(int min, int max){
		super(min,max, JSlider.VERTICAL);
	}
	
	public void setColourMap(ColourArrayType colourMap){
		((ColourRangeSliderUI) getUI()).setColourMap(colourMap);
	}
	
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
