package tethys.localization;

import java.awt.Window;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.LocalizationType;
import tethys.swing.export.LocalizationOptionsPanel;

/**
 * Creator thingy that Localisation algorithms can use to overwrite any standard
 * behaviour in LocalizatinBuilder. This interface should be added to localization
 * algorithms to provide Tethys compatibility. 
 * @author dg50
 *
 */
public interface LocalizationCreator {

	/**
	 * Sort out localization coordinates and set them in the current document in the
	 * Localization builder. e.g. the TMA module should be able to work out at this point
	 * if it's working in 2 or 3 dimensions and whether it's outputting in cartesian or 
	 * WGS84 coordinate systems. Whatever it chooses to do needs to be put into the 
	 * current localization document / Effort / CoordinateReferenceSystem, which must 
	 * then match whatever is output to the actual Localizations. 
	 * @param localizationBuilder holder of current document and other information about the export. 
	 * @param dataBlock Current datablock (though the localiser will probably know this)
	 * @return true if everything makes sense. false if it's not been possible to sort the coordinated, in which case 
	 * export probably shouldn't proceed.  
	 */
	public boolean sortLocalisationCoordinates(LocalizationBuilder localizationBuilder, PamDataBlock dataBlock) ;

	/**
	 * Create a localization record for export. The coordinate used in the localization must match what what
	 * set in the call to sortLocalisationCoordinates
	 * @param localizationBuilder holder of current document and other information about the export. 
	 * @param dataUnit data unit containing the localization to export
	 * @return Localization record. Can be null if the data unit didn't have a localization, in which case nothing is written. 
	 */
	public LocalizationType createLocalization(LocalizationBuilder localizationBuilder, PamDataUnit dataUnit);	
	
	/**
	 * Called after all localizations have been exported to check the document. Particularly regarding 
	 * coordinate types. In principle, a lot of the functions of sortLocalisationCoordinates could be sorted
	 * out here if it's only possible to work out the coordinate system after going through all the data. 
	 * @param localizationBuilder holder of current document and other information about the export. 
	 * @return true if everything OK and the document can be written to Tethys.
	 */
	public boolean checkDocument(LocalizationBuilder localizationBuilder);
	
	

}
