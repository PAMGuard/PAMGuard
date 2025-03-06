package whistleClassifier.tethys;

import PamguardMVC.PamDataBlock;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import whistleClassifier.WhistleClassificationDataBlock;
import whistleClassifier.WhistleClassificationDataUnit;
import whistleClassifier.WhistleClassifierControl;

public class WslClassSpeciesManager extends DataBlockSpeciesManager<WhistleClassificationDataUnit> {

	private WhistleClassifierControl whistleClassifierControl;
	
	private WhistleClassifierSpeciesCodes whistleClassifierSpeciesCodes;

	public WslClassSpeciesManager(WhistleClassifierControl whistleClassifierControl, WhistleClassificationDataBlock dataBlock) {
		super(dataBlock);
		this.whistleClassifierControl = whistleClassifierControl;
		whistleClassifierSpeciesCodes = new WhistleClassifierSpeciesCodes(whistleClassifierControl);
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return whistleClassifierSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(WhistleClassificationDataUnit dataUnit) {
		return dataUnit.getSpecies();
	}


}
