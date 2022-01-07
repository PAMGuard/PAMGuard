package PamUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to format time ranges, particularly when they get quite large and doing them 
 * in seconds no longer makes sense.<br> 
 * Want something along the lines of <br>
 * if an integer number of days, then just days, <br>
 * if an integer number of hours, then days and hours <br>
 * if an integer numeber of minutes, then days, hours, minutes <br>
 * if an integer number of seconds, then days, hours, minutes, seconds <br>
 * etc. 
 * 
 * @author Doug Gillespie
 *
 */
public class TimeRangeFormatter {

	private static final long MILLISPERSECOND = 1000L;
	private static final long MILLISPERMINUTE = MILLISPERSECOND * 60L;
	private static final long MILLISPERHOUR = MILLISPERMINUTE * 60L;
	private static final long MILLISPERDAY = MILLISPERHOUR * 240L;
	
	static public String formatTime(long millis) {
		boolean isIntDays = (millis % MILLISPERDAY == 0);
		boolean isIntHours = (millis % MILLISPERHOUR == 0);
		boolean isIntMinutes = (millis % MILLISPERMINUTE == 0);
		long days = millis / MILLISPERDAY;
		long hours = (millis%MILLISPERDAY) / MILLISPERHOUR;
		long minutes = (millis % MILLISPERHOUR) / MILLISPERMINUTE;
		long seconds = (millis % MILLISPERMINUTE) / MILLISPERSECOND;
		long milliseconds = (millis % MILLISPERSECOND);
		String timeString = "";
		String keyString = "";
		if (days > 0) {
			timeString += String.format("%d", days);
			keyString = "D";
		}
		if (isIntDays) {
			return removeLeadZero(timeString, keyString);
		}
		if (hours > 0 || timeString.length() > 0) {
			if (timeString.length() > 0) {
				timeString += " ";
				keyString += " ";
			}
			timeString += String.format("%02d",hours);
			keyString += "h";
		}
		if (isIntHours) {
			return removeLeadZero(timeString, keyString);
		}
		if (minutes > 0 || timeString.length() > 0) {
			if (timeString.length() > 0) {
				timeString += ":";
				keyString += ":";
			}
			timeString += String.format("%02d", minutes);
			keyString += "m";
		}
		if (isIntMinutes) {
			return removeLeadZero(timeString, keyString);
		}
		if (seconds > 0 || timeString.length() > 0) {
			if (timeString.length() > 0) {
				timeString += ":";
				keyString += ":";
			}
			timeString += String.format("%02d", seconds);
			keyString += "s";
		}
		if (milliseconds > 0) {
			if (timeString.length() > 0) {
				timeString += ".";
				keyString += "";
			}
			else {
				timeString = "0";
				keyString = "s";
			}
			timeString += String.format("%03d", milliseconds);
		}

		return removeLeadZero(timeString, keyString);
	}
	
	private static String removeLeadZero(String timeString, String keyString) {
//		System.out.println(timeString + "   " + keyString);
		while (timeString.length() > 0 && timeString.charAt(0) == '0') {
			timeString = timeString.substring(1);
		}
		return timeString + " " + keyString;
	}
	
	public static long readTime(String timeString) {
		/*
		 *  read a time string formatted with formatTime, or possibly typed !
		 *  Can hopefully find the units section, separate it out and then use 
		 *  it in a standard data read function to get the milliseconds. 
		 */
		// find the first digit which isn't a ":"
		long millis = 0;
		int len = timeString.length();
		char aChar;
		int keyStart = 0;
		for (int i = 0; i < len; i++) {
			aChar = timeString.charAt(i);
			if (Character.isLetter(aChar)) {
				keyStart = i;
			}
		}
		if (keyStart == 0) {
			// it seems to just be a number, so assume it's in seconds, try to read the rest of it.
			try {
				millis = (long) (Double.valueOf(timeString) * 1000.);
			}
			catch( NumberFormatException e) {
				return 0;
			}
		}
		else {
			String formatString = timeString.substring(keyStart);
			// replace h with H to make it 24 hour format. Unclear what will happen if it goes over 24 hours
			formatString = formatString.replaceAll("h", "H");
			timeString = timeString.substring(0, keyStart);
			DateFormat df = new SimpleDateFormat(formatString);
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date d;
			try {
				d = df.parse(timeString);
			} catch (ParseException e) {
				System.out.println("Invalid time format " + e.getLocalizedMessage());
				return 0;
			}
			Calendar cl = Calendar.getInstance();
			cl.setTime(d);
			return cl.getTimeInMillis();
		}
		return millis;
	}
}
