package videoRangePanel.importTideData;

import java.util.ArrayList;

import PamUtils.LatLong;
import PamUtils.TxtFileUtils;

/**
 * PolPred parser which uses the standard dating system with day, month, year time stamps. 
 * @author Jamie Macaulay 
 *
 */
public class POLPREDParser3 extends POLPREDParser2 {
	
	@Override
	public TideDataUnit parseTideLine(String line, LatLong location){
		ArrayList<String> txtData= TxtFileUtils.parseTxtLine(line, getIntitalDelimeter()); 
		return convertToTideDataPOLPRED1(txtData, location, false); 
	}

}
