package videoRangePanel.importTideData;

import java.util.ArrayList;
import java.util.List;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.TxtFileUtils;

public class CSVTideParser implements TideParser {

	@Override
	public TideDataUnit parseTideLine(String line, LatLong location) {
		
		ArrayList<String> txtData= TxtFileUtils.parseTxtLine(line, ","); 
		
		String date = txtData.get(0); 
		
		long timeMillis = PamCalendar.unpackStandardDateTime(date); 
		
		double level = Double.valueOf( txtData.get(1));
		
		TideDataUnit tideDataUnit = new TideDataUnit(timeMillis,  level, 0, 0, null  ); 
		
		System.out.println("Tide data: " + PamCalendar.formatDateTime(timeMillis) + " level: " + level); 
		
		
		return tideDataUnit;
	}

	@Override
	public LatLong getLocation(List<String> txtData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "CSV Parser";
	}

}
