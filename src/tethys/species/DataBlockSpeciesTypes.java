package tethys.species;

import java.util.ArrayList;

/**
 * Class to return lists of species codes or names for a datablock. 
 * This information will then get incorporated into a more complicated translation table to 
 * provide PAMGuard data on it's way to Tethys with more rigid species code definitions.  
 * @author dg50
 *
 */
public class DataBlockSpeciesTypes {

	/**
	 * List of species names / codes associated with this data block. 
	 */
	private ArrayList<String> speciesNames;
	
	/**
	 * constructor to use with a array of String names. 
	 * @param speciesList
	 */
	public DataBlockSpeciesTypes(String[] speciesList) {
		if (speciesList == null) {
			speciesNames = new ArrayList<>();
		}
		else {
			speciesNames = new ArrayList<String>(speciesList.length);
			for (int i = 0; i < speciesList.length; i++) {
				speciesNames.add(speciesList[i]);
			}
		}
	}

	/**
	 * @return the speciesNames
	 */
	public ArrayList<String> getSpeciesNames() {
		return speciesNames;
	}

	
}
