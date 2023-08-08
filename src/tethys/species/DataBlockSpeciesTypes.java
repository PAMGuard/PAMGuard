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
	 * List of species names / codes associated with this data block. These can be translated, 
	 * via a HashMap to more detailed objects which include an ITIS code. 
	 */
	private ArrayList<String> speciesNames;
	
	/**
	 * Probably only to be used when there are no defined names, but helpful if it's set. 
	 */
	private int itisDefault = ITISTypes.UNKNOWN;
	
	/**
	 * A default sound type, which can be used for all 'species', but can get 
	 * overridden in other scenarios. e.g. 'Click', 'Whistle'
	 */
	private String defaultType;
	
	/**
	 * @param defaultType
	 */
	public DataBlockSpeciesTypes(String defaultType) {
		this.defaultType = defaultType;
	}

	/**
	 * @param itisDefault
	 * @param defaultType
	 */
	public DataBlockSpeciesTypes(int itisDefault, String defaultType) {
		this.itisDefault = itisDefault;
		this.defaultType = defaultType;
	}

	/**
	 * constructor to use with a array of String names. 
	 * @param speciesList
	 */
	public DataBlockSpeciesTypes(String defaultType, String[] speciesList) {
		this.defaultType = defaultType;
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

	/**
	 * @return the itisDefault
	 */
	public int getItisDefault() {
		return itisDefault;
	}

	/**
	 * @param itisDefault the itisDefault to set
	 */
	public void setItisDefault(int itisDefault) {
		this.itisDefault = itisDefault;
	}

	/**
	 * @return the defaultType
	 */
	public String getDefaultType() {
		return defaultType;
	}

	/**
	 * @param defaultType the defaultType to set
	 */
	public void setDefaultType(String defaultType) {
		this.defaultType = defaultType;
	}

	/**
	 * @param speciesNames the speciesNames to set
	 */
	public void setSpeciesNames(ArrayList<String> speciesNames) {
		this.speciesNames = speciesNames;
	}

	
}
