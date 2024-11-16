package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamUtils.PamCalendar;
import javafx.util.StringConverter;

/**
 * Converts a string into a time. 
 */
public class DurationStringConverter extends StringConverter<Number> {
	
	
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
			timeVal = PamCalendar.formatDuration((long) durationMillis);
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
			else if (durationString.contains("s")){
				//find number 
				formatted = durationString.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1000.); 

			}
			else if(durationString.contains("m")){
				formatted = durationString.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1.); 

			}
			else if(durationString.contains(":")){
				String[] vals=durationString.split(":");

				int days=0; 
				int minutes=0; 
				int seconds=0; 
				if (vals.length==2){
					minutes=Integer.valueOf(vals[0]); 
					seconds=Integer.valueOf(vals[1]); 
				}
				else if (vals.length==3){
					days=Integer.valueOf(vals[0]); 
					minutes=Integer.valueOf(vals[1]); 
					seconds=Integer.valueOf(vals[2]); 
				}

				double totalSeconds=days*60*60 + minutes*60 + seconds; 
				millis=(totalSeconds*1000); 

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
