package generalDatabase;

import PamguardMVC.PamDataUnit;

/**
 *  common functionality to add on to an existing SQL logging object. 
 *  <p>Initially developed for the target motion analysis which may 
 *  get added to a variety of different things. 
 * @author Doug Gillespie
 *
 */
public interface SQLLoggingAddon {

	/**
	 * Add a load of columns to an existing table definition
	 * @param pamTableDefinition
	 */
	public void addTableItems(PamTableDefinition pamTableDefinition);
	
	/**
	 * Save data - that is transfer data from the pamDataUnit to the data objects
	 * within the table definition
	 * @param pamTableDefinition table definition
	 * @param pamDataUnit data unit
	 * @return true if successful
	 */
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit);
	
	/**
	 * Load data - that is read data from the table definition and turn it into something sensible
	 * within or attached to the data unit. 
	 * @param pamTableDefinition table definition
	 * @param pamDataUnit data unit
	 * @return true if successful
	 */
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit);
	
	/**
	 * Get a name for the SQLLogging Addon. this is used
	 * in identifying the Addon meaning that it should be possible
	 * to have two Addon's with the same class, but different fields. 
	 * @return the name of the Logging Addon
	 */
	public String getName();

}
