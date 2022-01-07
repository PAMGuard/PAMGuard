package PamView.sliders;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JSlider;

public class PamLogSlider extends JSlider {

	private static final long serialVersionUID = 1L;

	private PamSliderScale pamSliderScale;

	public PamLogSlider() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PamLogSlider(BoundedRangeModel brm) {
		super(brm);
		// TODO Auto-generated constructor stub
	}

	public PamLogSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		// TODO Auto-generated constructor stub
	}

	public PamLogSlider(int min, int max, int value) {
		super(min, max, value);
		// TODO Auto-generated constructor stub
	}

	public PamLogSlider(int min, int max) {
		super(min, max);
		// TODO Auto-generated constructor stub
	}

	public PamLogSlider(int orientation) {
		super(orientation);
		// TODO Auto-generated constructor stub
	}

	/**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new PamSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }


    /**
     * Get a scaled value from the slider. It wasn't possible to override 
     * getValue since it messes up the slider GUI, so call this instead 
     * if you're using scaled values. 
     * @return Scaled value. 
     */
	public double getScaledValue() {
		if (pamSliderScale == null) {
			return super.getValue();
		}
		else {
			return pamSliderScale.getValue(super.getValue(), getMinimum(), getMaximum());
		}
	}

	/**
	 * @return the pamSliderScale
	 */
	public PamSliderScale getPamSliderScale() {
		return pamSliderScale;
	}

	/**
	 * @param pamSliderScale the pamSliderScale to set
	 */
	public void setPamSliderScale(PamSliderScale pamSliderScale) {
		this.pamSliderScale = pamSliderScale;
	}
    
	
    
    
}
