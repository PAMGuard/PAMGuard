package bearinglocaliser.tethys;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.LocalizationType;
import tethys.localization.LocalizationBuilder;
import tethys.localization.LocalizationCreator;

public class BearingLocalisationCreator implements LocalizationCreator {

	public BearingLocalisationCreator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkDocument(LocalizationBuilder localizationBuilder) {
		// TODO Auto-generated method stub
		return false;
	}

}
