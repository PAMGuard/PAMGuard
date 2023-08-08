package RightWhaleEdgeDetector.species;

import PamguardMVC.PamDataBlock;
import RightWhaleEdgeDetector.RWEDataUnit;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesTypes;

public class RWSpeciesManager extends DataBlockSpeciesManager<RWEDataUnit> {
	
	private RWSpeciesTypes rwSpeciesTypes = new RWSpeciesTypes();

	public RWSpeciesManager(PamDataBlock<RWEDataUnit> dataBlock) {
		super(dataBlock);
	}

	@Override
	public DataBlockSpeciesTypes getSpeciesTypes() {
		return rwSpeciesTypes;
	}

	@Override
	public String getSpeciesString(RWEDataUnit dataUnit) {
		return RWSpeciesTypes.onlyType;
	}

}
