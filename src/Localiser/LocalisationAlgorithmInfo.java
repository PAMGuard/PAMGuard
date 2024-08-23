package Localiser;

import java.io.Serializable;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;

public interface LocalisationAlgorithmInfo {
	
	/**
	 * Get the likely content flags for this localiser. 
	 * @see LocalisationInfo
	 * @see LocContents
	 * @return localisation flags. 
	 */
	public int getLocalisationContents();
	
	/**
	 * Get the algorithm name
	 * @return algorithm name
	 */
	public String getAlgorithmName();
	
	/**
	 * Get the algorithm parameters. Something else 
	 * can turn these into xml for Tethys. 
	 * @return algorithm parameters object. Might be null;
	 */
	public Serializable getParameters();
	
}
