package Localiser;

import tethys.localization.LocalizationCreator;

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
	
	/**
	 * Get something that can make LocalisationType objects of a form
	 * a bit bespoke to the type of localiser. This may be better than having
	 * the standard functions in LocalizationBuilder guess what's best. 
	 * @return can be null in which case standard functions will do the best they can. 
	 */
	public LocalizationCreator getTethysCreator();
	
}
