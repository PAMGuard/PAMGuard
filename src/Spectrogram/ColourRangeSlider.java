package Spectrogram;

import javax.swing.SwingConstants;

import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.sliders.PamRangeSlider;
import PamView.sliders.PamRangeSliderUI;

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

	/*
	 * As in PamRangeSlider, the colour map is held by the UI delegate, which is
	 * thrown away and rebuilt every time the look and feel changes - which now
	 * happens whenever the user changes colour scheme. Keep the last one set here
	 * so it can be put back on the new delegate. No field initialisers: updateUI()
	 * runs before them (see PamRangeSlider#restoreUIProperties).
	 */

	/**
	 * The standard colour map to show, or null if a custom one is in use.
	 */
	private ColourArrayType colourMapType;

	/**
	 * A custom colour map to show, or null if a standard one is in use.
	 */
	private ColourArray colourMapArray;

	public ColourRangeSlider(){
		super(SwingConstants.VERTICAL);
	}
	
	public ColourRangeSlider(int min, int max){
		super(min, max,SwingConstants.VERTICAL);
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
		this.colourMapType = colourMap;
		this.colourMapArray = null;
		((ColourRangeSliderUI) getUI()).setColourMap(colourMap);
	}

	/**
	 * Set a custom colour map type to show between the two thumbs.
	 * @param colourMap - the colour map to show.
	 */
	public void setColourMap(ColourArray colourMap){
		this.colourMapArray = colourMap;
		this.colourMapType = null;
		((ColourRangeSliderUI) getUI()).setColourMap(colourMap);
	}

	@Override
	protected PamRangeSliderUI createUI() {
		return new ColourRangeSliderUI(this);
	}

	@Override
	protected void restoreUIProperties() {
		super.restoreUIProperties();
		//null for both means nothing has been set yet, so leave the UI's own default.
		if (colourMapArray != null) {
			((ColourRangeSliderUI) getUI()).setColourMap(colourMapArray);
		}
		else if (colourMapType != null) {
			((ColourRangeSliderUI) getUI()).setColourMap(colourMapType);
		}
	}
    
    
    


}
