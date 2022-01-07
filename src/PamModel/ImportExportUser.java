package PamModel;

import java.io.Serializable;

import PamController.PamSettings;

public interface ImportExportUser {
	
	public static final int EXPORT_SERIALIZED = 1;
	
	public static final int EXPORT_XML = 2;
	
	/**
	 * Get the object to export
	 * @return object to export
	 */
	public Serializable getExportObject();
	
	/**
	 * Set the imported object. 
	 * @param importObject imported object
	 */
	public void setImportObject(Serializable importObject);
	
	/**
	 * Get the available types of export. Combination of 
	 * EXPORT_SERIALIZED and EXPORT_XML. <p>
	 * Serialised is a little simpler and safer in many ways, 
	 * but XML is human readable. 
	 * @return available types of export
	 */
	public int getExportTypes();
	
	/**
	 * Get the class of the object input or output
	 * @return Class of io object
	 */
	public Class getIOClass();
	
	/**
	 * Get a wrapper for PAM Settings, ideally a PAM Controlled unit which 
	 * is used to provide a module name and type for exported XML settings. 
	 * @return
	 */
	public PamSettings getSettingsWrapper();
	

}
