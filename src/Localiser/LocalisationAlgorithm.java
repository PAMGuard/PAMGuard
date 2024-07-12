package Localiser;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;

/**
 * Interface to attach to localisation algorithms which can provide basic information
 * (primarily for better book keeping and Tethys output)
 * @author dg50
 *
 */
public interface LocalisationAlgorithm {

	/**
	 * Get the likely content flags for this localiser. 
	 * @see LocalisationInfo
	 * @see LocContents
	 * @return localisation flags. 
	 */
	public int getLocalisationContents();
}
