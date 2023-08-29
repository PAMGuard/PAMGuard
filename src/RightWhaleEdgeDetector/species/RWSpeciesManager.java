package RightWhaleEdgeDetector.species;

import PamguardMVC.PamDataBlock;
import RightWhaleEdgeDetector.RWEDataUnit;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.SpeciesMapItem;

public class RWSpeciesManager extends DataBlockSpeciesManager<RWEDataUnit> {
	
	private RWSpeciesTypes rwSpeciesTypes = new RWSpeciesTypes();

	public RWSpeciesManager(PamDataBlock<RWEDataUnit> dataBlock) {
		super(dataBlock);
		setDefaultDefaultSpecies(new SpeciesMapItem(RWSpeciesTypes.eubalaena, RWSpeciesTypes.onlyType, RWSpeciesTypes.defaultName));
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return null;
	}

	@Override
	public String getSpeciesCode(RWEDataUnit dataUnit) {
		return RWSpeciesTypes.onlyType;
	}

}
