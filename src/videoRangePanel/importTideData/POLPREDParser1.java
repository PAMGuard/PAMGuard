package videoRangePanel.importTideData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import PamUtils.LatLong;
import PamUtils.TxtFileUtils;

public class POLPREDParser1 implements TideParser {

	@Override
	public TideDataUnit parseTideLine(String line, LatLong location){
	
		ArrayList<String> txtData= TxtFileUtils.parseTxtLine(line, getIntitalDelimeter()); 
		return convertToTideDataPOLPRED1(txtData, location); 
	}

	
	public String getIntitalDelimeter() {
		return "\\s";
	}
	
	@Override
	public LatLong getLocation(List<String> tideData) {
		return getLocationPOLPRED(tideData);
	}

	
	
	public static LatLong getLocationPOLPRED(List<String> tideData){
		
		//System.out.println("Latlong: String: "+tideData);
		String latString=(tideData.get(1)+ tideData.get(2).substring(0, tideData.get(2).length()-1) + tideData.get(3).replace("\"", "'")+ " "+tideData.get(4));
		String longString=(tideData.get(5)+ tideData.get(6).substring(0, tideData.get(6).length()-1) + tideData.get(7).replace("\"", "'")+ " "+tideData.get(8));
		//System.out.println("Latlong: Formatted String:  "+latString+ "  "+longString);
		
		LatLong latLong=new LatLong((latString+","+longString));
		
		return latLong; 
	}
	

	
	/**
	 * Convert string data from a POLPRED output file.  
	 * @param tideData
	 * @return
	 */
	public TideDataUnit convertToTideDataPOLPRED1(ArrayList<String> tideData, LatLong location){			
			
		//System.out.println("POLPRED1: Size of data is: " +  tideData.size());
		if (tideData.size()==7) tideData.remove(0);
		if (tideData.size()==6){
			////date/////
			//day
			int length0=tideData.get(0).length();
			int day=Integer.valueOf(tideData.get(0).substring(0, length0-1));
			//month
			String[] splitString=tideData.get(1).split("/");
			int month=Integer.valueOf(splitString[0]);
			//year
			int year=Integer.valueOf(splitString[1]);
			
			/////time/////
			//hour
			String[] splitStringTime=tideData.get(2).split(":");
			int hour=Integer.valueOf(splitStringTime[0]);
			//minute
			int minute=Integer.valueOf(splitStringTime[0]);
			
			//convert to time (millis)
			Calendar cal = new GregorianCalendar ();
			cal.set(year,month-1,day,hour,minute,0);
			Date date=cal.getTime();
			long time=date.getTime();
			
			//now convert other data
			double level=Double.valueOf(tideData.get(3));
			double speed=Double.valueOf(tideData.get(4));
			double direction=Math.toRadians(Double.valueOf(tideData.get(5)));
			
			TideDataUnit tideDataUnit=new TideDataUnit(time, level, speed, direction, location);

			return tideDataUnit;
		}
		return null; 
		
	}


	@Override
	public String getName() {
		return "POLPRED 1";
	}



}
