package tethys.species;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Species map for a specified data block <p>
 * This is the bit that can be serialised, and is primarily (only) a hash table of 
 * SpeciesMapItems which relate String species codes for a detector to more 
 * details itis and scientific name information.   
 * @author dg50
 *
 */
public class DataBlockSpeciesMap implements Serializable {

	public static final long serialVersionUID = 1L;
		
	private HashMap<String, SpeciesMapItem> speciesTable = new HashMap<>();

	protected HashMap<String, SpeciesMapItem> getSpeciesTable() {
		return speciesTable;
	}
	
	protected void setSpeciesTable(HashMap<String, SpeciesMapItem> speciesTable) {
		this.speciesTable = speciesTable;
	}
	
	public void putItem(String key, SpeciesMapItem speciesMapItem) {
		speciesTable.put(key, speciesMapItem);
	}
	
	public SpeciesMapItem getItem(String key) {
		return speciesTable.get(key);
	}
	
	public void removeItem(String key) {
		speciesTable.remove(key);
	}
	
	public void clearMap() {
		speciesTable.clear();
	}

}
