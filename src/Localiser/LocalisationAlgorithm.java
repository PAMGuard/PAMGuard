package Localiser;

import java.io.Serializable;

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
	 * Get information about the localisation algorithm. 
	 * @return algorithm information. 
	 */
	public LocalisationAlgorithmInfo getAlgorithmInfo();
	
}
