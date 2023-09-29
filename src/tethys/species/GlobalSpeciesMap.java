package tethys.species;

import java.io.Serializable;
import java.util.HashMap;

import PamguardMVC.PamDataBlock;

public class GlobalSpeciesMap implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, DataBlockSpeciesMap> datablockMaps;

	/**
	 * @return the datablockMaps
	 */
	public synchronized HashMap<String, DataBlockSpeciesMap> getDatablockMaps() {
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
	
	public DataBlockSpeciesMap removeBlock(PamDataBlock pamDataBlock) {
		return getDatablockMaps().remove(pamDataBlock.getLongDataName());
	}

	@Override
	public GlobalSpeciesMap clone() {
		GlobalSpeciesMap clone;
		try {
			clone = (GlobalSpeciesMap) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		clone.datablockMaps = new HashMap<>();
		clone.datablockMaps.putAll(this.getDatablockMaps());
		return clone;
	}

}
