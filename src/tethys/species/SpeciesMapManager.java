package tethys.species;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;

/**
 * Master manager for species maps which will eventually allow for export and import from XML
 * documents, databases and other things ...
 * @author dg50
 *
 */
public class SpeciesMapManager implements PamSettings {
	
	private static SpeciesMapManager singleInstance = null;
	
	private static Object synch = new Object(); 
	
	private GlobalSpeciesMap globalSpeciesMap;

	private SpeciesMapManager() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Get an instance of the global species manager. This handles look up tables 
	 * for each datablock to convert from internal PAMGuard names to ITIS species codes and
	 * usefully call types for output to Tethys. 
	 * @return
	 */
	public static SpeciesMapManager getInstance() {
		if (singleInstance == null) {
			synchronized (synch) {
				if (singleInstance == null) {
					singleInstance = new SpeciesMapManager();
				}
			}
		}
		return singleInstance;
	}

	@Override
	public String getUnitName() {
		return "Global Species Codes";
	}

	@Override
	public String getUnitType() {
		return "Global Species Codes";
	}

	@Override
	public Serializable getSettingsReference() {
		gatherSpeciesMaps();
		return globalSpeciesMap;
	}

	/**
	 * Get species maps from all PAMGuard datablocks which have such a map
	 */
	private void gatherSpeciesMaps() {
		if (globalSpeciesMap == null) {
			globalSpeciesMap = new GlobalSpeciesMap();
		}
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aBlock : allDataBlocks) {
			DataBlockSpeciesManager spManager = aBlock.getDatablockSpeciesManager();
			if (spManager == null) {
				continue;
			}
			DataBlockSpeciesMap speciesMap = spManager.getDatablockSpeciesMap();
			globalSpeciesMap.put(aBlock, speciesMap);
		}
	}
	
	public DataBlockSpeciesMap getSpeciesMap(PamDataBlock pamDataBlock) {
		if (globalSpeciesMap == null) {
			return null;
		}
		return globalSpeciesMap.get(pamDataBlock);
	}

	@Override
	public long getSettingsVersion() {
		return GlobalSpeciesMap.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		Object obj = pamControlledUnitSettings.getSettings();
		if (obj instanceof GlobalSpeciesMap) {
			this.globalSpeciesMap = (GlobalSpeciesMap) obj;
			return true;
		}
		else {
			return false;
		}
	}
}
