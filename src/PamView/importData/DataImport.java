package PamView.importData;

import java.util.ArrayList;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * This class is used to define what data to import and how to import it. It is used in conjunction with ImportDataSystem to create a generic system to load any type of data into a data block. 
 * <p>
 * First getExtensionsStrings() defines which file types can be selected for import. 
 * <p>
 * Before loading in any data we need to perform  pre-checks with the function performPreChecks(). This function is called before the load thread is called. Often this will remain blank but say you needed extra user imput then this function could be used
 * to create another dialog box before the data is loaded. 
 * <p>
 * To create a new importer first we need to load a file into memory using the loadDataIntermediate(String filePath) function. This will create an ArrayList<T> were T is the type of data we're trying to save. So for example if you are loading in NMEA data then 
 * you would use create loadDataIntermediate(String filePath) which imports the file (filepath) and converts into an ArrayList of strings io.e ArrayList<String> such that T=String; 
 * <p>
 * Once the data has been loaded into memory it must be converted into a data unit and saved to a data block. The function createDataUnit(T dataLine) converts data into a PamDatUnit. This function would for example, convert an NMEA strings into a GPSDataUnit. The function
 * isDataFormatOK() needs to be used to check that each T is in the correct format. 
 * <p>
 *
 * @author Jamie Macaulay
 *
 * @param <T> - the data type loaded from a file. This could be a String, ArrayList<String>, ArrayList<ArrayList<Double>> etc.  
 */
public abstract class DataImport<T>   {

	public abstract ArrayList<T> loadDataIntermediate(String filePath);
		
	/**
	 * Check that a row of imported data is in the correct format. 
	 * @param dataLine-a row of data loaded from file
	 * @return true if the format is OK. 
	 */
	public abstract boolean isDataFormatOK(T dataLine);
	
	/**
	 * Use this function to perform any pre checks on data/ bring up extra dialog boxes before laoding data. 
	* @return true iof pre checks are OK. 
	 */
	public boolean performPreChecks(){ return true; }
	
	/**
	 * Create a data unit from the data loaded from the imported file. 
	 * @param dataLine-a row of data loaded from file
	 * @return a data unit to be saved to the datablock. 
	 */
	public abstract PamDataUnit createDataUnit(T dataLine);

	/**
	 * Get the data block to to save data to. 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public abstract PamDataBlock getDataBlock();

	/**
	 * Return the file extensions that can be loaded. 
	 * @return an array of file extensions that can be loaded. 
	 */
	public abstract String[] getExtensionsStrings();
	
	/**
	 * The name of the data unit to appear on the dialog boxes. 
	 * @return name of the data unit to be imported. 
	 */
	public String getDataUnitName(){
		return "Data Units";
	}


}
