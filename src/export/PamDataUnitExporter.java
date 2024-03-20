package export;

import java.io.File;
import java.util.List;


import PamguardMVC.PamDataUnit;

/**
 * Manages the exporting of data units to a particular type of file e.g. CSV, MATLAB, R, WAV files 
 */
public interface PamDataUnitExporter {
	
	/**
	 * Check whether a particular data unit class is compatible
	 * @param dataUnitType - the data unit type to test. 
	 * @return true if it can be exported. 
	 */
	public boolean hasCompatibleUnits(Class dataUnitType);
	
	/**
	 * Export the data to a folder. 
	 * @param fileName - the file to export to
	 * @param prefix - file prefix for filenames. 
	 * @param dataUnits - the data units to export. 
	 * @return true if exported successfully. 
	 */
	public boolean exportData(File fileName, List<PamDataUnit> dataUnits);
	
	/**
	 * Get the extension for the output file type
	 * @return the extension for the file type e.g. "mat" 
	 */
	public String getFileExtension();

	/**
	 * Get the ikonli icon string for the exporter. 
	 * @return the ikon string. 
	 */
	public String getIconString();

	/**
	 * Get the name of the exporter
	 * @return
	 */
	public String getName();


}
