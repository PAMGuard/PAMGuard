package rocca.tethys;

import rocca.RoccaControl;
import rocca.RoccaLoggingDataBlock;
import rocca.RoccaLoggingDataUnit;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;

public class RoccaSpeciesManager extends DataBlockSpeciesManager<RoccaLoggingDataUnit> {
	
	private RoccaSpeciesCodes roccaSpeciesCodes;
	private RoccaControl roccaControl;

	public RoccaSpeciesManager(RoccaControl roccaControl, RoccaLoggingDataBlock dataBlock) {
		super(dataBlock);
		this.roccaControl = roccaControl;
		roccaSpeciesCodes = new RoccaSpeciesCodes(roccaControl);
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return roccaSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(RoccaLoggingDataUnit dataUnit) {
		return dataUnit.getClassifiedSpecies();
	}

}
