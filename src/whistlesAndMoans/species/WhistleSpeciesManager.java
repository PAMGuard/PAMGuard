package whistlesAndMoans.species;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesTypes;
import tethys.species.ITISTypes;
import tethys.species.SpeciesMapItem;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class WhistleSpeciesManager extends DataBlockSpeciesManager<ConnectedRegionDataUnit> {
	
	private String defaultName = "Tonal";
	

	public WhistleSpeciesManager(PamDataBlock<ConnectedRegionDataUnit> dataBlock) {
		super(dataBlock);
		setDefaultDefaultSpecies(new SpeciesMapItem(180404, "Tonal", "Odontocete"));
	}

	@Override
	public DataBlockSpeciesTypes getSpeciesTypes() {
		String spList[] = {"Unknown"};

		DataBlockSpeciesTypes whistleSpeciesTypes = new DataBlockSpeciesTypes("Tonal", spList);
		return whistleSpeciesTypes;
	}

	@Override
	public String getSpeciesString(ConnectedRegionDataUnit dataUnit) {
		return defaultName;
	}

}
