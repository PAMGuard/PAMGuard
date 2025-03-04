package clickDetector.localisation;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.LocalizationType;
import tethys.localization.CoordinateName;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;

public class GroupTethysLocCreator implements LocalizationCreator {

	private GeneralGroupLocaliser generalGroupLocaliser;

	public GroupTethysLocCreator(GeneralGroupLocaliser generalGroupLocaliser) {
		this.generalGroupLocaliser = generalGroupLocaliser;
	}

	@Override
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		LocalizationType loc = localizationBuilder.makeBaseLoc(dataUnit);
		return loc;
	}

	@Override
	public boolean checkDocument(LocalizationBuilder localizationBuilder) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Get the coordinate type, setting to WGS84 if it's undefined. 
	 * @return Coordinate name
	 */
	private CoordinateName getCoordinateName() {
		return CoordinateName.WGS84; // always this I think. I don't see a possibility of options in this one. 
	}

}
