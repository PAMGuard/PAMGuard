package tethys.species;

import java.util.ArrayList;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tethys.species.swing.DataBlockSpeciesDialog;

/**
 * Manage species conversion for a single datablock.
 *
 * there seem to be three types of manager:<br>
 * 1. Datablocks which have a totally free list of species codes, such as the click detector, or whistle classifier <br>
 * 1a. A slight variation on this is blocks which have a list and also a default for data units which aren't classified.<br>
 * 2. Datablocks which have a single type which may be unknown or partially known, but we want the possibility of overriding it.
 * e.g. the whistle detector may default to odontocete, but may be overridden to a mystecete species.<br> 
 * 3. Datablocks with no information, or where the list from (1.) is empty. <p>
 * In all cases, we need to handle this reasonably sensibly. The code list is always the start of this and must 
 * always return something, even if it's 'unknown'. 
 *
 * @author dg50
 *
 */
abstract public class DataBlockSpeciesManager<T extends PamDataUnit> {
	
	/**
	 * The serialised bit. Always exists (or should be created) even if there
	 * are no real species, via a defaultdefaultSpecies. 
	 * Don't keep a local copy though since it may have been 
	 */
	private DataBlockSpeciesMap datablockSpeciesMap;
	
	private PamDataBlock<T> dataBlock;
	
	/*
	 * Below are three ways of getting species codes. At least ONE of these 
	 * must return something. 
	 */

	/**
	 * Object that contains a list of species codes. This may be fluid
	 * between configurations and may change during a session, e.g. through
	 * the addition of a new click type or changes to the whistle classifier settings. 
	 * @return object containing a list of species types. 
	 */
	public abstract DataBlockSpeciesCodes getSpeciesCodes();
	
	/**
	 * A default species code. Can be null, in which case it won't be used or 
	 * can be set to something like 'Unknown'
	 */
	private String defaultSpeciesCode = null;
	
	/**
	 * For use in detectors that have a strong default species, but no 
	 * real list of different species codes. Can be left null. 
	 */
	private SpeciesMapItem defaultDefaultSpecies = null;

	
	/**
	 * Gets a species string for a specific data unit, This is abstracted
	 * since different detectors store this in non standard ways. The result of 
	 * this should be within the set provided by getSpeciesCodes() which can then 
	 * be used in the DataBlockSpeciesMap to look up an itis code. 
	 * @param dataUnit
	 * @return A species code for a specific data unit. May be null (e.g. for an unclassified click)
	 */
	public abstract String getSpeciesCode(T dataUnit);

	public DataBlockSpeciesManager(PamDataBlock<T> dataBlock) {
		super();
		this.dataBlock = dataBlock;
		datablockSpeciesMap = SpeciesMapManager.getInstance().getSpeciesMap(dataBlock);
		if (datablockSpeciesMap == null) {
			datablockSpeciesMap = new DataBlockSpeciesMap();
		}
//		datablockSpeciesMap.clearMap();
		clearMapNulls();
		checkMapDefault();
	}
	
	/**
	 * Clear up some old maps which have got a null null default. 
	 */
	private void clearMapNulls() {
		if (datablockSpeciesMap == null) {
			return;
		}
		SpeciesMapItem nullVal = datablockSpeciesMap.getItem(null);
		if (nullVal == null) {
			datablockSpeciesMap.removeItem(null);
		}
	}

	public SpeciesMapItem getSpeciesItem(T dataUnit) {
		String speciesString = getSpeciesCode(dataUnit);
		if (speciesString == null) {
			SpeciesMapItem def = getDefaultDefaultSpecies();
			if (def != null) {
				speciesString = def.getPamguardName();
			}
		}
		DataBlockSpeciesMap speciesMap = getDatablockSpeciesMap();
		if (speciesMap == null) {
			return null;
		}
		return speciesMap.getItem(speciesString);
	}
	
	/**
	 * Get all PAMGuard species codes, which may come from the DataBlockSpeciesCodes 
	 * object, or the defaultSpeciesCode, or the defaultDefaultSpecies name. Ideally, 
	 * at least one of these should have something, or we'll stick in an "Unknown"
	 * @return
	 */
	public ArrayList<String> getAllSpeciesCodes() {
		ArrayList<String> allCodes = new ArrayList<String>();
		if (defaultSpeciesCode != null) {
			allCodes.add(defaultSpeciesCode);
		}
		if (defaultDefaultSpecies != null) {
			allCodes.add(defaultDefaultSpecies.getPamguardName());
		}
		DataBlockSpeciesCodes codeList = getSpeciesCodes();
		if (codeList != null && codeList.getSpeciesNames() != null) {
			allCodes.addAll(codeList.getSpeciesNames());
		}
		if (allCodes.size() == 0) {
			allCodes.add("Unknown");
		}
		return makeUniqueList(allCodes);
	}
	
	/**
	 * Make sure all entries in an array list are unique. 
	 * @param list
	 * @return updated list. 
	 */
	public ArrayList<String> makeUniqueList(ArrayList<String> list) {
		if (list == null) {
			return null;
		}
		ArrayList<String> newList = new ArrayList();
		for (String aStr : list) {
			if (newList.contains(aStr)) {
				continue;
			}
			newList.add(aStr);
		}
		return newList;
	}
	
	public DataBlockSpeciesMap getDatablockSpeciesMap() {
		if (datablockSpeciesMap == null) {
			datablockSpeciesMap = new DataBlockSpeciesMap();
			checkMapDefault();
		}
		return datablockSpeciesMap;
	}

	private void checkMapDefault() {
//		SpeciesMapItem defaultItem = datablockSpeciesMap.getItem(getDefaultSpeciesCode());
//		if (defaultItem == null) {
//			datablockSpeciesMap.putItem(getDefaultSpeciesCode(), getDefaultDefaultSpecies());
//		}
		if (defaultDefaultSpecies == null || datablockSpeciesMap == null) {
			return;
		}
		SpeciesMapItem defaultItem = datablockSpeciesMap.getItem(defaultDefaultSpecies.getPamguardName());
		if (defaultItem == null) {
			datablockSpeciesMap.putItem(defaultDefaultSpecies.getPamguardName(), defaultDefaultSpecies);
		}
	}

	public void setDatablockSpeciesMap(DataBlockSpeciesMap datablockSpeciesMap) {
		this.datablockSpeciesMap = datablockSpeciesMap;
	}

	public void showSpeciesDialog() {
		DataBlockSpeciesDialog.showDialog(PamController.getMainFrame(), dataBlock);
	}

	/**
	 * @return the dataBlock
	 */
	public PamDataBlock<T> getDataBlock() {
		return dataBlock;
	}

	/**
	 * @return the defaultSpecies
	 */
	public SpeciesMapItem getDefaultDefaultSpecies() {
		return defaultDefaultSpecies;
	}

	/**
	 * @param defaultSpecies the defaultSpecies to set
	 */
	public void setDefaultDefaultSpecies(SpeciesMapItem defaultDefaultSpecies) {
		this.defaultDefaultSpecies = defaultDefaultSpecies;
		checkMapDefault();
	}

	/**
	 * @return the defaultName
	 */
	public String getDefaultSpeciesCode() {
		return defaultSpeciesCode;
	}

	/**
	 * @param defaultName the defaultName to set
	 */
	public void setDefaultSpeciesCode(String defaultName) {
		this.defaultSpeciesCode = defaultName;
	}
	
	/**
	 * Check the species map. Only return true if every species code
	 * has a map item. Otherwise it's not safe to export. 
	 * @return null if all codes have a lookup, otherwise some sort of useful error information
	 */
	public String checkSpeciesMapError() {
		ArrayList<String> codes = getAllSpeciesCodes();
		if (codes == null || codes.size() == 0) {
			return "No defined species codes"; // I guess that's OK ? 
		}
		DataBlockSpeciesMap spMap = getDatablockSpeciesMap();
		if (spMap == null) {
			return "No species map";
		}
		
		for (String aCode : codes) {
			SpeciesMapItem item = spMap.getItem(aCode);
			if (item == null) {
				return "No Species item for species code " + aCode;
			}
		}
		return null;
	}
}
