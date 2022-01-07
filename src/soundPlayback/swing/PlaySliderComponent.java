package soundPlayback.swing;

import java.awt.Dimension;

import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

import PamView.PamSlider;

/**
 * Generic component that can be used for sliders
 * in play control side panel bits
 * @author dg50
 *
 */
public abstract class PlaySliderComponent {
	
	private PamSlider slider;

	public PlaySliderComponent() {
		slider = new PamSlider(0, getNSteps(), 1);
		setSliderSize();
	}
	
	public abstract double getMinValue();
	
	public abstract double getMaxValue();
	
	public abstract int getNSteps();
	
	/**
	 * Convert a value to a slider position
	 * @param real value
	 * @return slider position (0 - getNSteps)
	 */
	public int valueToPos(double value) {
		return (int) Math.round((value-getMinValue())/(getMaxValue()-getMinValue())*getNSteps());
	}
	
	/**
	 * Convert a slider position to a real value
	 * @param pos slider position (0 - getNSteps)
	 * @return real value
	 */
	public double posToValue(int pos) {
		return (double) pos / getNSteps() * (getMaxValue()-getMinValue()) + getMinValue();
	}

	/**
	 * @return the slider
	 */
	public PamSlider getSlider() {
		return slider;
	}

	/**
	 * Get the scaled value of the slider
	 * @return the real scaled value
	 */
	public double getDataValue() {
		return posToValue(slider.getValue());
	}
	
	/**
	 * Set the scaled value of the slider
	 * @param value the scaled value.
	 */
	public void setDataValue(double value) {
		slider.setValue(valueToPos(value));
	}

	/**
	 * Add a change listener. 
	 * @param changeListener
	 */
	public void addChangeListener(ChangeListener changeListener) {
		slider.addChangeListener(changeListener);
	}

	/**
	 * Adjust the slider preferred size
	 */
	private void setSliderSize() {
		/**
		 * Default behaviour is to set it to the same def' size as a progress bar component. 
		 * which i snarrower than a typical slider bar. 
//		 */
		JProgressBar pb = new JProgressBar(SwingConstants.HORIZONTAL);
		Dimension d = pb.getPreferredSize();
		Dimension sd = slider.getPreferredSize();
		sd.width = d.width;
		// setting the height just changes the size of the border around the control and doesn't 
		// change the siz of the too small slider control 
//		sd.height = 33;
		slider.setPreferredSize(sd);

	}
}
