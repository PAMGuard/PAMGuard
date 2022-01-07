package PamView.sliders;

import java.awt.Color;

import javax.swing.JSlider;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>Note that RangeSlider makes use of the default BoundedRangeModel, which 
 * supports an inner range defined by a value and an extent.  The upper value
 * returned by RangeSlider is simply the lower value plus the extent.</p>
 * 
 *<P> PamRangeSlider is based on code from Ernie Yu. 
 *<b>
 *http://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/ (28/09/2013)
 *</b>
 */
public class PamRangeSlider extends JSlider {

    /**
     * Constructs a RangeSlider with default minimum and maximum values of 0
     * and 100.
     */
    public PamRangeSlider() {
        initSlider(HORIZONTAL);
    }
    
    /**
     * Constructs a RangeSlider with default minimum and maximum values of 0
     * and 100 and specified orientation.
     */
    public PamRangeSlider(int orientation) {
        initSlider(orientation);
    }


    /**
     * Constructs a RangeSlider with the specified default minimum and maximum 
     * values.
     */
    public PamRangeSlider(int min, int max) {
        super(min, max);
        initSlider(HORIZONTAL);
    }
    
    
    /**
     * Constructs a RangeSlider with the specified default minimum and maximum 
     * values and specified orientation. 
     */
    public PamRangeSlider(int min, int max, int orientation) {
        super(min, max);
        initSlider(orientation);
    }

    /**
     * Initializes the slider by setting default properties.
     */
    private void initSlider(int type) {
        setOrientation(type);
    }

    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new PamRangeSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }

    /**
     * Returns the lower value in the range.
     */
    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        // Compute new value and extent to maintain upper value.
        int oldExtent = getExtent();
        int newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
        int newExtent = oldExtent + oldValue - newValue;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(newValue, newExtent, getMinimum(), 
            getMaximum(), getValueIsAdjusting());
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue() {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(int value) {
        // Compute new extent.
        int lowerValue = getValue();
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
        
        // Set extent to set upper value.
        setExtent(newExtent);
    }

	public void setRangeColour(Color frequencyBarColour) {
		((PamRangeSliderUI) this.getUI()).setRangeSliderColour(frequencyBarColour);
	}
	
	public void setThumbSizes(int size) {
		((PamRangeSliderUI) this.getUI()).setThumbSizes(size,size);
	}
	
	/**
	 * Allow the track between thumbs to be dragged. 
	 */
	public void setTrackDragging(boolean dragging){
		((PamRangeSliderUI) this.getUI()).setTrackDragging(dragging);
	}
	
	
}