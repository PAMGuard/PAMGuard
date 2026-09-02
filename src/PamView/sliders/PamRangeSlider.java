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

	/*
	 * The properties below live in the UI delegate, which updateUI() throws away and
	 * rebuilds - Swing calls updateUI() on every component whenever the look and
	 * feel changes, which now happens every time the user picks a different colour
	 * scheme. Keeping a copy on the slider itself means the new delegate can be
	 * given the same settings, rather than the slider silently reverting to the
	 * delegate's defaults.
	 *
	 * None of these may have a field initialiser: updateUI() is called from the
	 * JSlider constructor, before this class's initialisers run, so anything set
	 * here would overwrite what restoreUIProperties() had just used. The Java
	 * defaults (null / 0 / false) all mean "leave the delegate's own default".
	 */

	/**
	 * Colour of the bar between the two thumbs, or null to leave it to the UI.
	 */
	private Color rangeColour;

	/**
	 * Width and height of the thumbs, or 0 to leave it to the UI.
	 */
	private int thumbSize;

	/**
	 * Whether the track between the thumbs can be dragged.
	 */
	private boolean trackDragging;

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
        setUI(createUI());
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
        // the delegate is brand new, so anything previously set on one is gone.
        restoreUIProperties();
    }

	/**
	 * Create the UI delegate for this slider. Sub classes which need a different
	 * delegate should override this rather than {@link #updateUI()}, so that they
	 * still get the properties restored after a look and feel change.
	 *
	 * @return a new UI delegate for this slider.
	 */
	protected PamRangeSliderUI createUI() {
		return new PamRangeSliderUI(this);
	}

	/**
	 * Apply the properties which are held by the UI delegate to the delegate
	 * currently installed. Called whenever a new delegate is created, so that a
	 * change of look and feel doesn't quietly undo them.
	 * <p>
	 * Note that this is called from {@link #updateUI()}, and hence from the
	 * JSlider constructor, before sub class fields are initialised: overrides must
	 * cope with their own fields still being null.
	 */
	protected void restoreUIProperties() {
		PamRangeSliderUI ui = (PamRangeSliderUI) getUI();
		if (rangeColour != null) {
			ui.setRangeSliderColour(rangeColour);
		}
		if (thumbSize > 0) {
			ui.setThumbSizes(thumbSize, thumbSize);
		}
		ui.setTrackDragging(trackDragging);
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
		this.rangeColour = frequencyBarColour;
		restoreUIProperties();
	}

	public void setThumbSizes(int size) {
		this.thumbSize = size;
		restoreUIProperties();
	}

	/**
	 * Allow the track between thumbs to be dragged.
	 */
	public void setTrackDragging(boolean dragging){
		this.trackDragging = dragging;
		restoreUIProperties();
	}
	
	
}