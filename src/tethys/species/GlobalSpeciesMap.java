package tethys.species;

import java.io.Serializable;
import java.util.HashMap;

import PamguardMVC.PamDataBlock;

public class GlobalSpeciesMap implements Serializable {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, DataBlockSpeciesMap> datablockMaps;

	/**
	 * @return the datablockMaps
	 */
	private synchronized HashMap<String, DataBlockSpeciesMap> getDatablockMaps() {
		if (datablockMaps == null) {
			datablockMaps = new HashMap<>();
		}
		return datablockMaps;
	}
	
	public void put(PamDataBlock pamDataBlock, DataBlockSpeciesMap dataBlockSpeciesMap) {
		getDatablockMaps().put(pamDataBlock.getLongDataName(), dataBlockSpeciesMap);
	}

	public DataBlockSpeciesMap get(PamDataBlock pamDataBlock) {
		return getDatablockMaps().get(pamDataBlock.getLongDataName());
	}

}
