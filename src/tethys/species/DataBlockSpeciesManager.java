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
			return null;
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
		}
		return datablockSpeciesMap;
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
}
