package detectiongrouplocaliser.tethys;

import detectiongrouplocaliser.DetectionGroupDataBlock;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.SpeciesMapItem;

public class DetectionGroupSpeciesManager extends DataBlockSpeciesManager<DetectionGroupDataUnit> {
	
	private DetectionGroupSpeciesCodes detectionGroupSpeciesCodes;

	public DetectionGroupSpeciesManager(DetectionGroupDataBlock dataBlock) {
		super(dataBlock);
		detectionGroupSpeciesCodes = new DetectionGroupSpeciesCodes();
		setDefaultDefaultSpecies(new SpeciesMapItem(180403, DetectionGroupSpeciesCodes.name, DetectionGroupSpeciesCodes.name));
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return detectionGroupSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(DetectionGroupDataUnit dataUnit) {
		return DetectionGroupSpeciesCodes.name;
	}


}
