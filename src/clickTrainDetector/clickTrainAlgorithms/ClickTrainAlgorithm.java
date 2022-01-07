package clickTrainDetector.clickTrainAlgorithms;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.layout.CTDetectorGraphics;

/**
 * Interface for a click train detection algorithm. 
 * @author Jamie Macaulay 
 *
 */
public interface ClickTrainAlgorithm {

	/**
	 * The name of the algorithm 
	 * @return the name of the algorithm 
	 */
	public String getName(); 
	
	/**
	 * Adds a data unit to the click train detector algorithm 
	 * @param dataUnit - data unit to be considered for click train detection. 
	 */
	public void newDataUnit(PamDataUnit<?, ?> dataUnit); 

	
	/**
	 * Get the class which handles GUI components for the click train
	 * detector algorithm 
	 * @return the click train graphics. 
	 */
	public CTDetectorGraphics getClickTrainGraphics();

	/**
	 * Send update flag to the algorithm 
	 * @param flag - the flag. 
	 */
	public void update(int i);

	/**
	 * Get logging class for click algorithm specific information. 
	 * @return the ct algorithm logging. 
	 */
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging(); 
	
	
}
