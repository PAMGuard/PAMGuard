package export;

import java.awt.Component;
import java.io.File;
import java.util.List;


import PamguardMVC.PamDataUnit;
import javafx.scene.layout.Pane;

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
	 * @param true to append the data to the current file - otherwise a new file is written. 
	 * @return true if exported successfully. 
	 */
	public boolean exportData(File fileName, List<PamDataUnit> dataUnits, boolean append);
	
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
	 * Get the name of the exporter.
	 * @return the name of the exporter.
	 */
	public String getName();

	/**
	 * Close the exporter.
	 */
	public void close();

	/**
	 * Check whether and exporter needs a new file
	 * @return true if we need a new file. 
	 */
	public boolean isNeedsNewFile();
	
	/**
	 * An optional panel that displays additional options for the user. 
	 * @return additional options panel - can be null.
	 */
	public Component getOptionsPanel(); 

	/**
	 * An optional JavaFX pane that displays additional options for the user.
	 * @return pane with additonal options - can be null. 
	 */
	public Pane getOptionsPane();

	/**
	 * Called whenever a new export run is prepared. 
	 */
	public void prepareExport(); 


}
