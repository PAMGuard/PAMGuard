package Acquisition.filedate;

import java.awt.Window;
import java.io.File;

import Acquisition.layoutFX.FileDatePane;

/**
 * Extract file dates from the file name
 * <p>
 * Should be possible to make many different implementations 
 * of this for handling different file name formats. 
 * 
 * @author Doug Gillespie
 *
 */
public interface FileDate {

	/**
	 * Get a time in milliseconds from a file date. 
	 * @param file file
	 * @return time in milliseconds or 0 if can't work it out. 
	 */
	public long getTimeFromFile(File file);
	
	/**
	 * True if the file date subclass has associated settings
	 * @return true if it has settings 
	 */
	public boolean hasSettings();
	
	/*
	 * Show the Swing settings dialog and/or update settings. 
	 * @return true if setting have changed.
	 */
	public boolean doSettings(Window parent);
	
	/**
	 * Get the JavaFX settings pane for the file date. 
	 * @return the settings pane or null if there are no settings. 
	 */
	public FileDatePane doSettings();

	/**
	 * Get the name of the file date implementation. 
	 * @return the name of the file date implementation. 
	 */
	public String getName();
	
	/**
	 * Get a description of the file date implementation. 
	 * @return a description of the file date implementation. 
	 */
	public String getDescription();
		
	
	public String getFormat();
	
}
