package export.CSVExport;


import org.apache.commons.csv.CSVPrinter;

import PamguardMVC.PamDataUnit;

/**
 * Base class for CSV data unit export. Exports the generic info for a PAMDataUnit. Subclass this for 
 * specefic data units e.g. clicks. 
 * @author Jamie Macaulay 
 *
 */
public abstract class CSVDataUnitExport<T extends PamDataUnit> {

	/**
	 *Add detection specific fields  to a csvprinter object. .
	 *@param structure containing all generic info from PamDataUnit
	 *@param the data unit. 
	 */
	public abstract CSVPrinter addDetectionSpecificFields(CSVPrinter  csvPrinter, T dataUnit, int index);

	/**
	 * Get the unit class 
	 * @return
	 */
	public abstract Class<?> getUnitClass();

	/**
	 * Get the name of the structure 
	 * @return
	 */
	public abstract String getName();

}
