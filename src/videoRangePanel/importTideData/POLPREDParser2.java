package videoRangePanel.importTideData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.TxtFileUtils;

/**
 * Polpred tide file
 * @author Jamie Macaulay 
 *
 */
public class POLPREDParser2 implements TideParser {

	@Override
	public TideDataUnit parseTideLine(String line, LatLong location){
		ArrayList<String> txtData= TxtFileUtils.parseTxtLine(line, getIntitalDelimeter()); 
		return convertToTideDataPOLPRED1(txtData, location, true); 
	}


	public String getIntitalDelimeter() {
		return "\\t";
	}

	@Override
	public LatLong getLocation(List<String> tideData) {
		return null; 
	}

	/**
	 * Convert string data from a POLPRED output file.  
	 * @param tideData - string of the tide data values. SHould be date, time height
	 * @param location - the lat long for the data unit
	 * @param americanDateFormat - true if american data format i.e the month before the day...GAAAHHHH 
	 * @return the tide data unit. 
	 */
	public static TideDataUnit  convertToTideDataPOLPRED1(ArrayList<String> tideData, LatLong location, boolean americanDateFormat ){			

		//System.out.println("Line data: ");
//		for (int i=0; i<tideData.size(); i++ ){
//			System.out.print(tideData.get(i) +  " | ");
//		}

		if (tideData.size()==3){
			////date/////
			//day
			//parse the string
			int day, month, year, hours, minutes, seconds; 
			String[] dates = tideData.get(0).split("/"); 
			if (dates.length==3){
				if (americanDateFormat) {
					//In the Amrerican data format with the MONTH first. Why would anyone think that's a sensible date format!? 
					day=Integer.valueOf(dates[1]);
					//month
					month=Integer.valueOf(dates[0]);
				}
				else {
					//In the Amrerican data format with the MONTH first. Why would anyone think that's a sensible date format!? 
					day=Integer.valueOf(dates[0]);
					//month
					month=Integer.valueOf(dates[1]);
				}
				//year
				year=Integer.valueOf(dates[2]);

				//if before the first ever photograph then probably using 00 format for
				//21st century. e.g. 2017->0017
				if (year<1826){
					year=year+2000; 
				}
			}
			else return null; 


			String[] times = tideData.get(1).split(":"); 
			if (times.length==2 || times.length==3){

				hours=Integer.valueOf(times[0]);

				minutes=Integer.valueOf(times[1]);

				seconds=0; 
				if (times.length==3){
					seconds=Integer.valueOf(times[2].substring(0, 2));

					//deal with potential am and pm problems 
					if (times[2].contains("PM") && hours!=12){
						hours=hours+12; //add 12 hours but also 12pm stays as 12 and != 24
					}
					else if (times[2].contains("AM") && hours==12){
						hours=0; //12 AM -> 00
					}

				}


			}
			else return null; 

			long timeMillis= PamCalendar.msFromDate(year, month, day, hours, minutes, seconds, 0); 

			//now convert other data
			double level=Double.valueOf(tideData.get(2));

			//System.out.println("Parsed data: " + 	PamCalendar.formatDateTime2(timeMillis) + level);

			TideDataUnit tideDataUnit=new TideDataUnit(timeMillis, level, 0, 0, location);

			return tideDataUnit;
		}
		return null; 

	}


	@Override
	public String getName() {
		return "POLPRED 2";
	}

}
