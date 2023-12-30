package whistlesAndMoans.species;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.ITISTypes;
import tethys.species.SpeciesMapItem;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class WhistleSpeciesManager extends DataBlockSpeciesManager<ConnectedRegionDataUnit> {
	
	private String defaultName = "Tonal";
	

	public WhistleSpeciesManager(PamDataBlock<ConnectedRegionDataUnit> dataBlock) {
		super(dataBlock);
		setDefaultDefaultSpecies(new SpeciesMapItem(180404, "Tonal", "Tonal"));
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
//		String spList[] = {"Unknown"};
//
//		DataBlockSpeciesCodes whistleSpeciesTypes = new DataBlockSpeciesCodes("Tonal", spList);
//		return whistleSpeciesTypes;
		return null;
	}

	@Override
	public String getSpeciesCode(ConnectedRegionDataUnit dataUnit) {
		return defaultName;
	}

}
