package PamView.sliders;

/**
 * Methods for changing the scale of sliders. Will do linear and log
 * but others also possible. 
 * @author dg50
 *
 */
abstract public class PamSliderScale {
	
	/**
	 * Interpret the old value and turn it into a new value. 
	 * @param oldValue old value from slider
	 * @param minValue min value from slider
	 * @param maxValue max value from slider
	 * @return interpreted value. 
	 */
	abstract public double getValue(int oldValue, int minValue, int maxValue);
	
}
