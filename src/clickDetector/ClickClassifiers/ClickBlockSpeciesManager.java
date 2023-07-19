package clickDetector.ClickClassifiers;

import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesTypes;

public class ClickBlockSpeciesManager extends DataBlockSpeciesManager<ClickDetection> {

	private ClickControl clickControl;
	
	public ClickBlockSpeciesManager(ClickControl clickControl, ClickDataBlock clickDataBlock) {
		super(clickDataBlock);
		this.clickControl = clickControl;
	}

	@Override
	public DataBlockSpeciesTypes getSpeciesTypes() {
		ClickTypeMasterManager masterManager = clickControl.getClickTypeMasterManager();
		if (masterManager == null) {
			return null;
		}
		String[] speciesList = masterManager.getSpeciesList();
		
		return new DataBlockSpeciesTypes(speciesList);
	}

	@Override
	public String getSpeciesString(ClickDetection dataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

}
