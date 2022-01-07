package videoRangePanel.importTideData;

import java.util.List;

import PamUtils.LatLong;

/**
 * Converts a string to tide data
 * @author Jamie Macaulay 
 *
 */
public interface TideParser {
	
	/**
	 * Convert delimtted strings to the actual data. 
	 * @param tideData
	 * @param location
	 * @return
	 */
	public TideDataUnit parseTideLine(String line, LatLong location); 
	
	/**
	 * Get the location for the tide. Probably in metadata of the file. 
	 * @param tideData. All tide data. 
	 * @return
	 */
	public LatLong getLocation(List<String> txtData);

	/**
	 * Description of the parser. 
	 * @return
	 */
	public String getName(); 


}
