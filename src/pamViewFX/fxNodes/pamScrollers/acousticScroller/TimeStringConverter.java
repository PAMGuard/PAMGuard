package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import javafx.util.StringConverter;

public class TimeStringConverter extends StringConverter<Long> {

	@Override
	public String toString(Long object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long fromString(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/**
	 * 
	 * @param textBoxText
	 * @return the visible range to set  in millis 
	 */
	public static double getTextBoxValue(String textBoxText, boolean showMillis) {

		double millis=-1; 

		/**
		 * Three possible inputs. 1) number in seconds e.g. number with letter e.g.10s for 10 seconds or 10m for 10 millis) time e.g. 00:00:01
		 */
		String formatted = null; 
		try {
			if (textBoxText.contains("ms")){
				//find number 
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= Double.valueOf(formatted); 

			}
			else if (textBoxText.contains("s")){
				//find number 
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1000.); 

			}
			else if(textBoxText.contains("m")){
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1.); 

			}
			else if(textBoxText.contains(":")){
				String[] vals=textBoxText.split(":");

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
				millis=(showMillis ? Double.valueOf(textBoxText) :  Double.valueOf(textBoxText)*1000); 
				return millis; 
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.; 
		}

		return millis;
	}

}
