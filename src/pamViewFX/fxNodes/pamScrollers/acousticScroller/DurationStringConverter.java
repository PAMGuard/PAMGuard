package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamUtils.PamCalendar;
import javafx.util.StringConverter;

/**
 * Converts a string into a time. 
 */
public class DurationStringConverter extends StringConverter<Number> {
	
	public static final double DAY = 60*60*24*1000;
	
	
	private boolean showMillis = false;

	@Override
	public String toString(Number object) {
		return getDurationMillisString(object.doubleValue(), showMillis);
	}

	@Override
	public Number fromString(String string) {
		return  getDurationMillisValue(string, showMillis);
	}
	
	
	/**
	 * Get a string from a millisecond time. Note that this returns a duration and NOT a date. 
	 * @param durationMillis - the duration in millis. 
	 * @param showMillis - true if the string is in milliseconds only and does not follow the normal formatting
	 * @return string representaiton of the duration. 
	 */
	public static String getDurationMillisString(double durationMillis, boolean showMillis) {
		String timeVal = "";
		if (showMillis){
			timeVal = String.format("%.2fms", durationMillis); 
		}
		else {
			if (durationMillis%DAY==0) {
				//no point in putting hours if exactly a day
				if (durationMillis==DAY) timeVal = "1 day";
				else timeVal =String.format("%.0f days", durationMillis/DAY);
			}
			else {
				timeVal = PamCalendar.formatDuration((long) durationMillis);
			}
		}
		
		return timeVal;
	}
	
	/**
	 * Get the value of the duration from a time string. 
	 * @param durationString - the string represenation of the duration.
	 * @param isMillis - true if the input duration is in milliseconds.
	 * @return -1 if the format is incorrect. Otherwise the duration in milliseconds. 
	 */
	public static double getDurationMillisValue(String durationString, boolean isMillis) {

		double millis=-1; 

		/**
		 * Three possible inputs. 1) number in seconds e.g. number with letter e.g.10s for 10 seconds or 10m for 10 millis) time e.g. 00:00:01
		 */
		String formatted = null; 
		try {
			if (durationString.contains("ms")){
				//find number 
				formatted = durationString.replaceAll("[^.?0-9]+", " ");
				millis= Double.valueOf(formatted); 

			}
			else if (durationString.contains("s") && !durationString.contains("days")){
				//find number 
				formatted = durationString.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1000.); 

			}
			else if(durationString.contains("m")){
				formatted = durationString.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1.); 

			}
			else if(durationString.contains(":")){
				
				//check that 
				
				String[] vals=durationString.split(":");
				
				double day = 0; 
				if (vals[0].toLowerCase().contains("day")) {
					//we have a day string aswell
					String[] dayHour;
					if (vals[0].toLowerCase().contains("days")) {
						dayHour = vals[0].split("days");
					}
					else {
						dayHour = vals[0].split("day");
					}
					
					day = Double.valueOf(dayHour[0]); 
					
					//replace the hour in the string
					vals[0] = dayHour[1];
				}

				int hours=0; 
				int minutes=0; 
				int seconds=0; 
				if (vals.length==2){
					minutes=Integer.valueOf(vals[0]); 
					seconds=Integer.valueOf(vals[1]); 
				}
				else if (vals.length==3){
					hours=Integer.valueOf(vals[0]); 
					minutes=Integer.valueOf(vals[1]); 
					seconds=Integer.valueOf(vals[2]); 
				}

				double totalSeconds=day*60*60*24 + hours*60*60 + minutes*60 + seconds; 
				millis=(totalSeconds*1000); 
			}
			else if (durationString.toLowerCase().contains("day")) {
				//the value contains only days
				//remove all non alpha numeric values
				String daysString = durationString.replaceAll("[^\\d.]", "");
				double days = Double.valueOf(daysString);
				millis=(days*24*60*60*1000); 
			}
			else {
				// the value in seconds. 
				millis=(isMillis ? Double.valueOf(durationString) :  Double.valueOf(durationString)*1000); 
				return millis; 
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.; 
		}

		return millis;
	}

	public void setShowMillis(boolean showMillis) {
		this.showMillis = showMillis;
	}

}
