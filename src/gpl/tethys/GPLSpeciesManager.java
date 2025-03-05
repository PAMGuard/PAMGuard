package gpl.tethys;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;

public class GPLSpeciesManager extends DataBlockSpeciesManager {
	
	private GPLSpeciesCodes gplSpeciesCodes = new GPLSpeciesCodes();

	public GPLSpeciesManager(PamDataBlock dataBlock) {
		super(dataBlock);
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return gplSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(PamDataUnit dataUnit) {
		return GPLSpeciesCodes.name;
	}

}
