package tethys.species;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tethys.species.swing.DataBlockSpeciesDialog;

/**
 * Manage species conversion for a single datablock.
 * @author dg50
 *
 */
abstract public class DataBlockSpeciesManager<T extends PamDataUnit> {
	
	private DataBlockSpeciesMap datablockSpeciesMap;
	
	private PamDataBlock<T> dataBlock;
	
	private String defaultName = null;
	
	private SpeciesMapItem defaultDefaultSpecies = null;

	public abstract DataBlockSpeciesTypes getSpeciesTypes();
	
	public abstract String getSpeciesString(T dataUnit);

	public DataBlockSpeciesManager(PamDataBlock<T> dataBlock) {
		super();
		this.dataBlock = dataBlock;
		datablockSpeciesMap = SpeciesMapManager.getInstance().getSpeciesMap(dataBlock);
	}
	
	public SpeciesMapItem getSpeciesItem(T dataUnit) {
		String speciesString = getSpeciesString(dataUnit);
		if (speciesString == null) {
			return getDefaultDefaultSpecies();
		}
		DataBlockSpeciesMap speciesMap = getDatablockSpeciesMap();
		if (speciesMap == null) {
			return null;
		}
		return speciesMap.getItem(speciesString);
	}
	
	public DataBlockSpeciesMap getDatablockSpeciesMap() {
		if (datablockSpeciesMap == null) {
			datablockSpeciesMap = new DataBlockSpeciesMap();
			checkMapDefault();
		}
		return datablockSpeciesMap;
	}

	private void checkMapDefault() {
		SpeciesMapItem defaultItem = datablockSpeciesMap.getItem(getDefaultName());
		if (defaultItem == null) {
			datablockSpeciesMap.putItem(getDefaultName(), getDefaultDefaultSpecies());
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
	}

	/**
	 * @return the defaultName
	 */
	public String getDefaultName() {
		return defaultName;
	}

	/**
	 * @param defaultName the defaultName to set
	 */
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}
}
