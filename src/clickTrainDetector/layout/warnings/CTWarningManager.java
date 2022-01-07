package clickTrainDetector.layout.warnings;

import java.util.ArrayList;

import clickTrainDetector.ClickTrainControl;
import warnings.PamWarning;

/**
 * Manages warning for the click train detector
 * @author Jamie Macaulay 
 *
 */
public class CTWarningManager {
	
	/**
	 * List of all current warning. 
	 */
	ArrayList<PamWarning> currentWarnings = new ArrayList<PamWarning>();
	
	/**
	 * Reference to the click train control.
	 */
	private ClickTrainControl clickTrainControl; 
	
	public CTWarningManager(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl; 
	}
	
	/**
	 * Add a warning to the click train detector.
	 * @param pamWarning - warning to add.
	 */
	public void addWarning(PamWarning pamWarning) {
		currentWarnings.add(pamWarning); 
	}
	
	/**
	 * Get all current warnings
	 * @return the current warning. 
	 */
	public ArrayList<PamWarning> getWarnings() {
		return currentWarnings; 
	}

}
