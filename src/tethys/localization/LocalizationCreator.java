package tethys.localization;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.LocalizationType;

/**
 * Creator thingy that Localisation algorithms can use to overwrite any standard
 * behaviour in LocalizatinBuilder. 
 * @author dg50
 *
 */
public interface LocalizationCreator {

	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) ;

	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit);	
	
	public boolean checkDocument(LocalizationBuilder localizationBuilder);

}
