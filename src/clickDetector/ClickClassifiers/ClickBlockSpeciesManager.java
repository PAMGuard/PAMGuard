package clickDetector.ClickClassifiers;

import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesTypes;
import tethys.species.ITISTypes;
import tethys.species.SpeciesMapItem;

public class ClickBlockSpeciesManager extends DataBlockSpeciesManager<ClickDetection> {

	private ClickControl clickControl;
	
	public ClickBlockSpeciesManager(ClickControl clickControl, ClickDataBlock clickDataBlock) {
		super(clickDataBlock);
		this.clickControl = clickControl;
		setDefaultDefaultSpecies(new SpeciesMapItem(ITISTypes.UNKNOWN, "Unknown", "Unknown"));
		setDefaultName("Unknown");
	}

	@Override
	public DataBlockSpeciesTypes getSpeciesTypes() {
		ClickTypeMasterManager masterManager = clickControl.getClickTypeMasterManager();
		if (masterManager == null) {
			return null;
		}
		String[] speciesList = masterManager.getSpeciesList();
		// add the default
		String[] fullList = new String[speciesList.length+1];
		fullList[0] = getDefaultName();
		for (int i = 0; i < speciesList.length; i++) {
			fullList[i+1] = speciesList[i];
		}
		
		return new DataBlockSpeciesTypes("Click", fullList);
	}

	@Override
	public String getSpeciesString(ClickDetection dataUnit) {
		ClickTypeMasterManager masterManager = clickControl.getClickTypeMasterManager();
		if (masterManager == null) {
			return null;
		}
		int listIndex = masterManager.codeToListIndex(dataUnit.getClickType());
		if (listIndex < 0) {
			return null;
		}
		return masterManager.getSpeciesList()[listIndex];
	}

}
