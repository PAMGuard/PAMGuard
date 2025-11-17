/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import PamController.PamController;
import PamUtils.time.CalendarControl;


/**
 * @author Doug Gillespie
 * 
 * Date and time in Pamguard are critical. It's likely that we'll be taking time
 * from the GPS in the longer term, so for now, make sure that all calendar
 * functions come via this
 * 
 */
public class PamCalendar {

	
	public static TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");
	
	private static TimeZone localTimeZone = defaultTimeZone;// TimeZone.getDefault();

	public static final long millisPerDay = 1000L*24L*3600L;

	/**
	 * true if data are from a file based data source, false
	 * if the data are arriving in real time. If analysing 
	 * file data, times are based on the file start time (if known)
	 * and the position of the read pointer within the file. 
	 */
	private static boolean soundFile;

	/**
	 * time from the start of the file to the currentmoment. 
	 * This is updated every time data re read from the file, so is
	 * accurate to about 1/10 second. 
	 * For accurate timing within detectors, always try to use sample number
	 * and count samples from the start time for the detector.
	 */
	private static volatile long soundFileTimeInMillis;

	/**
	 * Time that data processing started - can be set to a file time
	 * when files are being processed, otherwise it's just the current time. 
	 */
	private static volatile long sessionStartTime;

	/**
	 * When running in viewer mode, use the sessionStartTime and the viewEndtime
	 * to control the calendar. 
	 */
	private static long viewEndTime;

	/** 
	 * view is controlled by a slider which sets the 
	 * viewPositions which is the number of milliseconds from the sessionsStartTime.
	 */
	private static long viewPosition;
		
	
	/**
	 * If files are being analysed, return the time based on the file
	 * position. Otherwise just take the normal system time.
	 * @return time in milliseconds
	 */
	static public long getTimeInMillis() {

		// Date d = new Date();
		// return d.getTime();
		if (PamController.getInstance() == null) {
			return System.currentTimeMillis();
		}
		switch (PamController.getInstance().getRunMode()) {
		case PamController.RUN_NORMAL:
		case PamController.RUN_MIXEDMODE:
			if (soundFile) {
				return sessionStartTime + soundFileTimeInMillis;
			} else {
				return System.currentTimeMillis() + timeCorrection;
			}
		case PamController.RUN_PAMVIEW:
			return sessionStartTime + viewPosition;
		case PamController.RUN_NETWORKRECEIVER:
			return System.currentTimeMillis();
		}
		return System.currentTimeMillis() + timeCorrection;
	}

	/**
	 * 	a formatted time string
	 */
	static public long getTime() {

		// Date d = new Date();
		// return d.getTime();
		return getTimeInMillis();

	}

	/**
	 * 
	 * @return a formatted date string
	 */
	static public String getDate() {

		//		Date d = new Date();
		//		return d.toString();
		return formatDateTime(getTimeInMillis());

	}

	/**
	 * Compares two times in milliseconds to see if they are on the same day or not. 
	 * @param t1 first time
	 * @param t2 second time
	 * @return true if times are on the same day
	 */
	public static final boolean isSameDay(long t1, long t2) {
		long d1 = t1/millisPerDay;
		long d2 = t2/millisPerDay;
		return d1 == d2;
	}

	/**
	 * Get the current date
	 * @return the date as a Calendar object (in GMT)
	 */
	static public Calendar getCalendarDate() {

		Calendar c =  Calendar.getInstance();
		c.setTimeZone(defaultTimeZone);
		c.setTimeInMillis(getTimeInMillis());
		return c;

	}	

	/**
	 * Get the date for a given time 
	 * @param timeInMillis time in milliseconds
	 * @return the date as a Calendar object (in GMT)
	 */
	public static Calendar getCalendarDate(long timeInMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(defaultTimeZone);
		return c;
	}

	public static TimeZone getDisplayTimeZone(boolean useLocal) {
//		return TimeZone.getTimeZone("UTC");
//		return useLocal ? CalendarControl.getInstance().getChosenTimeZone() : defaultTimeZone;
		return useLocal ? localTimeZone : defaultTimeZone;
	}

	public static String formatDateTime(Date date) {
		return formatDateTime(date, false);
	}

	/**
	 * Get a formatted date and time string. 
	 * @param date Date
	 * @return formatted String
	 */
	public static String formatDateTime(Date date, boolean useLocal) {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.UK);
		df.setTimeZone(getDisplayTimeZone(useLocal));
		return df.format(date);
	}
	/**
	 * Formats the time and data in a long format
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDateTime(long timeInMillis) {
		return formatDateTime(timeInMillis, false);
	}

	/**
	 * Formats the time and data in a long format
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDateTime(long timeInMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		return formatDateTime(c.getTime(), useLocal);
	}

	public static String formatTodaysTime(long timeInMillis) {
		return formatTodaysTime(timeInMillis, false);
	}

	public static String formatTodaysTime(long timeInMillis, boolean useLocal) {
		if (isToday(timeInMillis, useLocal)) {
			return "Today " + formatTime(timeInMillis, useLocal);
		}
		else {
			return formatDateTime(timeInMillis, useLocal);
		}
	}

	private static boolean isToday(long timeInMillis) {
		return isToday(timeInMillis, false);
	}

	private static boolean isToday(long timeInMillis, boolean useLocal) {
		long offset = 0;
		if (useLocal) {
			TimeZone tz = CalendarControl.getInstance().getChosenTimeZone();
			offset = tz.getOffset(timeInMillis);
		}
		long dayStart = System.currentTimeMillis()-offset;
		dayStart /= millisPerDay;
		dayStart *= millisPerDay;
		return timeInMillis > dayStart; 
	}

	/**
	 * Formats the local time and data in a long format
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatLocalDateTime(long timeInMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		return formatLocalDateTime(c.getTime());
	}

	/**
	 * Get a formatted local date and time string. 
	 * @param date Date
	 * @return formatted String
	 */
	public static String formatLocalDateTime(Date date) {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
		return df.format(date);
	}

	/**
	 * Formats the local time and date in a long format - identical to
	 * formatDateTime2(long), but using the local-PC time zone
	 * 
	 * @param timeInMillis time in milliseconds
	 * @return formated local date and time
	 */
	public static String formatLocalDateTime2(long timeInMillis) {
		return formatLocalDateTime2(timeInMillis, "dd MMM yyyy HH:mm:ss", false);
	}

	/**
	 * Get a formatted local date and time string  - identical to
	 * formatDateTime2(long, String), but using the local-PC time zone
	 * 
	 * @param timeInMillis time in milliseconds
	 * @param the output format. e.g. "dd MMM yyyy HH:mm:ss"
	 * @return formated date and time
	 */
	public static String formatLocalDateTime2(long timeInMillis, String format, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);

		DateFormat df = new SimpleDateFormat(format);
		Date d = c.getTime();

		return df.format(d);
	}


	/**
	 * Formats the time and data in a long format
	 * but without the GMT label at the end. 
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDateTime2(long timeInMillis) {
		return formatDateTime2(timeInMillis, false);
	}

	/**
	 * Formats the time and data in a long format
	 * but without the GMT label at the end. 
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDateTime2(long timeInMillis, boolean useLocal) {
		return formatDateTime2(timeInMillis, "dd MMM yyyy HH:mm:ss", useLocal);
	}

	/**
	 * Formats the time and data in a long format
	 * but without the GMT label at the end. 
	 * @param timeInMillis time in milliseconds
	 * @param the output format. e.g. "dd MMM yyyy HH:mm:ss"
	 * @return formated data and time
	 */
	public static String formatDateTime2(long timeInMillis, String format, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		//		c.setTimeZone(defaultTimeZone);
		c.setTimeZone(getDisplayTimeZone(useLocal));

		DateFormat df = new SimpleDateFormat(format);
		//		df.setTimeZone(defaultTimeZone);
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();

		return df.format(d);
	}

	/**
	 * Return the time in milliseconds from the passed String sing the format "dd MMM yyyy HH:mm:ss"
	 * 
	 * @param dateTime The date and time to parse
	 * @param stringInUTC true if the passed String is in UTC time, or false if in local PC time
	 * @return epoch time (elapsed time in milliseconds from Jan 1, 1970)
	 */
	public static long millisFromDateTimeString(String dateTime, boolean stringInUTC) {
		return millisFromDateTimeString(dateTime,"dd MMM yyyy HH:mm:ss", stringInUTC);
	}

	/**
	 * Return the time in milliseconds from the passed String, using the passed format
	 * 
	 * @param dateTime The date and time to parse
	 * @param format The date/time format to use
	 * @param stringInUTC true if the passed String is in UTC time, or false if in local PC time
	 * @return epoch time (elapsed time in milliseconds from Jan 1, 1970)
	 */
	public static long millisFromDateTimeString(String dateTime, String format, boolean stringInUTC) {
		DateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(getDisplayTimeZone(!stringInUTC));
		Date date;
		try {
			date = df.parse(dateTime);
		} catch (ParseException e) {
			return 0;
		}
		long millis = date.getTime();
		return millis;
	}

	/**
	 * Formats the date and time in the correct format for database output. 
	 * <p>"yyyy-MM-dd HH:mm:ss"
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDBDateTime(long timeInMillis) {
		return formatDBDateTime(timeInMillis, false);
	}
	/**
	 * Formats the date and time in the correct format for database output. 
	 * <p>"yyyy-MM-dd HH:mm:ss"
	 * @param timeInMillis time in milliseconds
	 * @param showMills also show millseconds. 
	 * @return formated data and time
	 */
	public static String formatDBDateTime(long timeInMillis, boolean showMillis) {
		return formatDBStyleTime(timeInMillis, showMillis, false);
	}

	public static String formatDBStyleTime(long timeInMillis, boolean showMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		DateFormat df;
		if (showMillis) {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
		else {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		//		return String.format("%tY-%<tm-%<td %<tH:%<tM:%<tS", d);

		return df.format(d);
	}


	/**
	 * Formats the date and time in the correct format for database output
	 * but in local time.
	 * <p>"yyyy-MM-dd HH:mm:ss"
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatLocalDBDateTime(long timeInMillis) {
		return formatDBStyleTime(timeInMillis, false, true);
		//		Calendar c = Calendar.getInstance();
		//		c.setTimeInMillis(timeInMillis);
		//
		//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//		Date d = c.getTime();
		//
		//		return df.format(d);
	}
	/**
	 * Formats the date and time in the correct format for database output. 
	 * <p>"yyyy-MM-dd"
	 * This will always be UTC since it's data !
	 * @param timeInMillis time in milliseconds
	 * @return formated data and time
	 */
	public static String formatDBDate(long timeInMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(defaultTimeZone);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(defaultTimeZone);
		Date d = c.getTime();
		//		return String.format("%tY-%<tm-%<td %<tH:%<tM:%<tS", d);

		return df.format(d);
	}

	//	/**
	//	 * Get a formatted string in the correct format to include in database queries
	//	 * <p>e.g. {ts '2012-06-25 17:22:54'}
	//	 * This works for most database systems,,, but may be overridden in
	//	 * some sub classes of SQLTyeps.  
	//	 * @param timeMillis time in milliseconds
	//	 * @return formatted string
	//	 */
	//	public static String formatDBDateTimeQueryString(long timeMillis) {
	//		return "{ts '" + formatDBDateTime(timeMillis) + "'}";
	//	}
	/**
	 * Format a time string in the format HH:MM:SS
	 * @param timeMillis time in milliseconds
	 * @return formatted string
	 */
	public static String formatTime(long timeMillis) {
		return formatTime(timeMillis, false, false);
	}
	/**
	 * Format a time string in the format HH:MM:SS
	 * @param timeMillis time in milliseconds
	 * @return formatted string
	 */
	public static String formatTime(long timeMillis, boolean showMills) {
		return formatTime(timeMillis, showMills, false);
	}

	/**
	 * Format a time string optionally showing the milliseconds with
	 * a given precision for UTC time zone
	 * @param timeMillis time in milliseconds
	 * @param millisDigits number of millisecond decimal places. 
	 * @return formatted time string. 
	 */
	public static String formatTime(long timeMillis, boolean showMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		String formatString = "HH:mm:ss";
		if (showMillis) {
			formatString += ".SSS";
		}
		DateFormat df = new SimpleDateFormat(formatString);
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format a time string optionally showing the milliseconds with
	 * a given precision for UTC time zone
	 * @param timeMillis time in milliseconds
	 * @param millisDigits number of millisecond decimal places. 
	 * @return formatted time string. 
	 */
	public static String formatLocalTime(long timeMillis, boolean showMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		String formatString = "HH:mm:ss";
		if (showMillis) {
			formatString += ".SSS";
		}
		DateFormat df = new SimpleDateFormat(formatString);
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format a time string optionally showing the milliseconds with
	 * a given precision.  The time string is formatted as HH:mm:ss.SSSSS.
	 * @param timeMillis time in milliseconds
	 * @param millisDigits number of millsecond decimal places.
	 * @return formatted time string.
	 */
	public static String formatTime(long timeMillis, int millisDigits) {
		return formatTime(timeMillis, millisDigits, false);
	}

	/**
	 * Format a time string optionally showing the milliseconds with
	 * a given precision.  The time string is formatted as HH:mm:ss.SSSSS.
	 * @param timeMillis time in milliseconds
	 * @param millisDigits number of millsecond decimal places.
	 * @return formatted time string.
	 */
	public static String formatTime(long timeMillis, int millisDigits, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		String formatString = "HH:mm:ss";
		if (millisDigits > 0) {
			formatString += ".";
			for (int i=1; i<=millisDigits; i++) {
				formatString += "S";
			}
		}
		DateFormat df = new SimpleDateFormat(formatString);
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format a time string optionally showing the milliseconds with
	 * a given precision.  The time string is formatted as HHmmss.SSSSS.
	 * @param timeMillis time in milliseconds
	 * @param millisDigits number of millsecond decimal places.
	 * @return formatted time string.
	 */
	public static String formatTime2(long timeMillis, int millisDigits, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		String formatString = "HHmmss";
		if (millisDigits > 0) {
			formatString += ".";
			for (int i=1; i<=millisDigits; i++) {
				formatString += "S";
			}
		}
		DateFormat df = new SimpleDateFormat(formatString);
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}



	/**
	 * Format a time in milliseconds as a number of days / seconds, etc. 
	 * @param timeInMillis time in milliseconds. 
	 * @return formatted time interval
	 */
	public static String formatDuration(long timeInMillis) {
		return formatDuration(timeInMillis, " day ", " days ");
	}

	public static String formatDuration(long timeInMillis, String middleString) {
		return formatDuration(timeInMillis, middleString, middleString);
	}

	public static String formatDuration(long timeInMillis, String middleString1, String middleString2) {
		long aDay = 3600 * 24 * 1000;
		if (timeInMillis < 60000) {
			return String.format("%.3fs", timeInMillis/1000.);
		}
		if (timeInMillis < aDay) {
			return formatTime(timeInMillis, false);
		}
		long days = (int) Math.floor(timeInMillis / aDay);
		long millis = timeInMillis - days * aDay;
		if (days == 1) {
			return String.format("%d%s%s", days, middleString1, formatTime(millis, false));
		}
		else {
			return String.format("%d%s%s", days, middleString2, formatTime(millis, false));
		}
	}

	/**
	 * Format the data in the dd MMMM yyyy format
	 * @param timeInMillis time in milliseconds
	 * @return formatted string. 
	 */
	public static String formatDate(long timeInMillis) {
		return formatDate(timeInMillis, false);
	}

	/**
	 * Format the data in the dd MMMM yyyy format
	 * @param timeInMillis time in milliseconds
	 * @return formatted string. 
	 */
	public static String formatDate(long timeInMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
		//		return String.format("%td %th \'%ty", c.getTime(),c.getTime(),c.getTime());
	}

	/**
	 * Format the data in the yyMMdd format
	 * @param timeInMillis time in milliseconds
	 * @return formatted string.
	 */
	public static String formatDate2(long timeInMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		DateFormat df = new SimpleDateFormat("yyMMdd");
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format date in format "yyyyMMdd" using UTC as the time zone
	 * @param timeMillis
	 * @return
	 */
	public static String formatCompactDate(long timeMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		//		Date date = c.getTime();
		//		String.format("%4.4d%2.2d%2.2d", date.getYear(), date.get)
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format date in format "yyyyMMdd" but using a time zone
	 * based on UTC or a local time depending on global PAMGuard options. 
	 * @param timeMillis
	 * @return formatted date string
	 */
	public static String formatFileDate(long timeMillis) {
		return formatFileDate(timeMillis, false);
	}
	/**
	 * Format date in format "yyyyMMdd" but using a time zone
	 * based on UTC or a local time depending on global PAMGuard options. 
	 * @param timeMillis
	 * @return formatted date string
	 */
	public static String formatFileDate(long timeMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		//		Date date = c.getTime();
		//		String.format("%4.4d%2.2d%2.2d", date.getYear(), date.get)
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format date and time in format "yyyyMMdd_HHmmss"
	 * @param timeMillis
	 * @return formatted time string
	 */
	public static String formatFileDateTime(long timeMillis, boolean useLocal) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		c.setTimeZone(getDisplayTimeZone(useLocal));
		//		Date date = c.getTime();
		//		String.format("%4.4d%2.2d%2.2d", date.getYear(), date.get)
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		df.setTimeZone(getDisplayTimeZone(useLocal));
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * Format date and time in format "yyyyMMdd_HHmmss", but using the local pc time zone
	 * @param timeMillis
	 * @return formatted time string
	 */
	public static String formatLocalFileDateTime(long timeMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date d = c.getTime();
		return df.format(d);
	}

	/**
	 * "H:m:s" added by Graham Weatherup for Logger Time control, another option is to add "01-01-1970 " in the TimeControl to the string first...
	 */
	private static String dateFormats[] = {"y-M-d H:m:s.S", "y-M-d H:m:s", "y-M-d H:m", "y-M-d H", "y-M-d"/*, "H:m:s"*/};

	/**
	 * Read a date string and turn it into a millisecond time.
	 * @param dateString
	 * @return time in milliseconds
	 */
	public static long msFromDateString(String dateString) {
		return msFromDateString(dateString, false);
	}
	/**
	 * Read a date string and turn it into a millisecond time.
	 * @param dateString
	 * @return time in milliseconds
	 */
	public static long msFromDateString(String dateString, boolean useLocal) {
		Date d = dateFromDateString(dateString, useLocal);
		if (d != null) {
			Calendar cl = Calendar.getInstance();
			cl.setTimeZone(getDisplayTimeZone(useLocal));
			cl.setTime(d);
			return cl.getTimeInMillis();
		}

		return -1;
	}

	/**
	 * Convert a date string into a millisecond time. Return null
	 * if the string cannot be interpreted as a date.  
	 * @param dateString Date string
	 * @return Millis from 1970 or null. 
	 */
	public static Long millisFromDateString(String dateString, boolean useLocal) {
		Date date = dateFromDateString(dateString, useLocal);
		if (date == null) {
			return null;
		}
		return date.getTime();
	}

	/**
	 * Read a date string and turn it into a Date
	 * @param dateString
	 * @return time as a Date object
	 */
	public static Date dateFromDateString(String dateString, boolean useLocal) {
		DateFormat df;
		Date d = null;
		for (int i = 0; i < dateFormats.length; i++) {

			df = new SimpleDateFormat(dateFormats[i]);
			df.setTimeZone(getDisplayTimeZone(useLocal));

			try {
				d = df.parse(dateString);

				return d;
			}
			catch (java.text.ParseException ex) {
				//								JOptionPane.showMessageDialog(this, "Invalid data format", "Error", JOptionPane.ERROR_MESSAGE);
				//								ex.printStackTrace();
				//				return null;
				d = null;
			}
		}
		return d;
	}

	private static String timeFormats[] = {"H:m:s", "H:m", "H"};

	/**
	 * time correction to apply to all calls to gettimeMillis();
	 */
	private static long timeCorrection;

	/**
	 * Read a time string and turn it into a millisecond time.
	 * @param timeString
	 * @return time in milliseconds
	 */
	public static long msFromTimeString(String timeString) {
		return msFromTimeString(timeString, false);
	}
	/**
	 * Read a time string and turn it into a millisecond time.
	 * @param timeString
	 * @param useLocal use local time, not UTC
	 * @return time in milliseconds
	 */
	public static long msFromTimeString(String timeString, boolean useLocal) {
		Date d = timeFromTimeString(timeString, useLocal);
		if (d != null) {
			Calendar cl = Calendar.getInstance();
			cl.setTimeZone(getDisplayTimeZone(useLocal));
			cl.setTime(d);
			return cl.getTimeInMillis();
		}

		return -1;
	}
	/**
	 * Read a time string and turn it into a Date
	 * @param timeString
	 * @return time as a Date object
	 */
	public static Date timeFromTimeString(String timeString, boolean useLocal) {
		DateFormat df;
		Date d = null;
		for (int i = 0; i < timeFormats.length; i++) {
			df = new SimpleDateFormat(timeFormats[i]);
			df.setTimeZone(getDisplayTimeZone(useLocal));

			try {
				d = df.parse(timeString);

				return d;
			}
			catch (java.text.ParseException ex) {
				//				JOptionPane.showMessageDialog(this, "Invalid data format", "Error", JOptionPane.ERROR_MESSAGE);
				//				ex.printStackTrace();
				//				return null;
				d = null;
			}
		}
		return d;
	} 

	/**
	 * 
	 * @return true if the sound source is a file
	 */
	public static boolean isSoundFile() {
		return soundFile;
	}

	/**
	 * 
	 * @param soundFile set whether the sound source is a file
	 */
	public static void setSoundFile(boolean soundFile) {
		PamCalendar.soundFile = soundFile;
	}

	/**
	 * 
	 * @return The time that processing started
	 */
	public static long getSessionStartTime() {
		return sessionStartTime;
	}

	/**
	 * 
	 * @param sessionStartTime the time that processing started
	 * And also set the file time to zero within that since both this
	 * and setSoundFileTime send out notifications, so this can really mess
	 * up timing, causing new binary files to be created and all sorts of 
	 * other problems. 
	 */
	public static void setSessionStartTime(long sessionStartTime) {
//		System.out.printf("Session start : %s\n", formatDBDateTime(sessionStartTime, true));
		setSessionStartTime(sessionStartTime, 0);
	}

	/**
	 * 
	 * @param sessionStartTime the time that processing started
	 * @param soundFileTimeMillis sound file time relative to start time. Good to set this zero right away. 
	 */
	public static void setSessionStartTime(long sessionStartTime, long soundFileTimeMillis) {
		PamCalendar.sessionStartTime = sessionStartTime;
		PamCalendar.soundFileTimeInMillis = soundFileTimeMillis;
		PamController.getInstance().updateMasterClock(getTimeInMillis());
	}

	/**
	 * 
	 * Relative time within a sound file. This is always just added to sessionStartTime
	 * to give an absolute time. 
	 * @param soundFileTimeMillis The relative time of a sound file. 
	 */
	public static void setSoundFileTimeInMillis(long soundFileTimeMillis) {
		PamCalendar.soundFileTimeInMillis = soundFileTimeMillis;
		PamController.getInstance().updateMasterClock(getTimeInMillis());
	}

	/**
	 * Create a file name based on a time and other information
	 * @param fileStartTime File time
	 * @param directory Directory / folder
	 * @param prefix file prefix (part of file name to inlcude before the time stamp)
	 * @param fileType file end
	 * @return File path and name, ending with a time stamp
	 */
	public static String createFileName(long fileStartTime, String directory, String prefix, String fileType) {
		return directory + FileParts.getFileSeparator() + createFileName(fileStartTime, prefix, fileType);
	}

	/**
	 * Create a file name based on a time and other information
	 * @param fileStartTime File time
	 * @param directory Directory / folder
	 * @param prefix file prefix (part of file name to include before the time stamp)
	 * @param fileType file end
	 * @return File path and name, ending with a time stamp
	 */
	public static String createFileNameMillis(long fileStartTime, String directory, String prefix, String fileType) {
		return directory + FileParts.getFileSeparator() + createFileNameMillis(fileStartTime, prefix, fileType);
	}

	final static String[] formats = {
			"HH_mm_ss'__DMY_'dd_MM_yy", // Inez DSG files egCopy of _RAWD_HMS_01_27_00__DMY_09_05_2010.wav
			"HH'h'mm'm'ss's'ddMMMyyyy", // Inez CRSS files
			"yyyy_MM_dd_HH_mm_ss", //lime kiln data
			"yyyyMMdd_HHmmss_SSS", // PAMGuard format with additional milliseconds. 
			"yyyy.MM.dd_HH.mm.ss", 
			"yyyy.MM.dd-HH.mm.ss",
			"yyyyMMdd_HHmmss", 
			"yyyyMMdd$HHmmss", //wildlife Acoustics
			"yyyyMMdd-HHmmss",
			"yy.MM.dd_HH.mm.ss", 
			"yy.MM.dd-HH.mm.ss",
			"yyMMdd_HHmmss", 
			"yyMMdd-HHmmss",
			"yyyy.MM.dd_HH.mm", 
			"yyyy.MM.dd-HH.mm",
			"yyyyMMdd_HHmm", 
			"yyyyMMdd-HHmm",
			"yy.MM.dd_HH.mm", 
			"yy.MM.dd-HH.mm",
			"yyMMdd_HHmm", 
			"yyMMdd-HHmm",
			"yy.DDD_HH.mm.ss", 
			"yy.DDD-HH.mm.ss",
			"yyDDD_HHmmss", 
			"yyDDD-HHmmss",
			"yy.DDD_HH.mm", 
			"yy.DDD-HH.mm",
			"yyDDD_HHmm", 
			"yyDDD-HHmm",
			"yyyy-MM-dd HH_mm_ss", // Avisoft.
			"yyyy-MM-dd_HH-mm-ss", // y2000 Cornell pop up data
			"yyyyMMddHHmmss", //Tanzania survey (recorder using 'bul filerename' program)
			"yyyy-MM-dd HH-mm-ss", // RS Orca recorder. index 32. Must remain at this position !!!!
			"dd/MM/yyyy HH:ss" //An excel standard
	};

	public static Long unpackStandardDateTime(String numstr) {
		Date dt = unpackUnknownDateString(numstr);
		if (dt != null) {
			Calendar cl = Calendar.getInstance();
			cl.setTimeZone(TimeZone.getTimeZone("UTC"));
			cl.setTime(dt);
			return cl.getTimeInMillis();
		}
		else {
			return null;
		}
	}
	
	public static Date unpackUnknownDateString(String numstr) {
		boolean prevWasDigit = false;
		for (int i = 0; i < numstr.length(); i++) {
			boolean isDigit = java.lang.Character.isDigit(numstr.charAt(i));
			if (isDigit && !prevWasDigit) {
				String str = numstr.substring(i);
j:				for (int j = 0; j < formats.length; j++) {
					String fmt = formats[j];
					//parse() doesn't check that all the digit format characters in
					//fmt line up with digit characters in 'numbers', so we have to.
					//First check that 'numbers' is long enough.
					if (j == 32 && numstr.length() > 10 && numstr.charAt(10) == 'T') {
						// get rid of the T in the time string
						str = str.replace('T', ' ');
					}
					if (j > 2) {
						if (str.length() < fmt.length())
							continue j;
						for (int k = 0; k < fmt.length(); k++) {
							if (java.lang.Character.isLetter(fmt.charAt(k))) {
								char ch = str.charAt(k);
								boolean t1 = (ch >= '0');
								boolean t2 = (ch <= '9');
								if (!(t1 && t2))
									continue j;
							}
						}
					}
					//Also, we don't like this format if the character in 'numbers'
					//just after the end of the formatted string is another digit;
					//that probably means the format is the wrong one.
					if (str.length() >= fmt.length()+1)
						if (java.lang.Character.isDigit(str.charAt(fmt.length())))
							continue j;
					//Now see if DateFormat can parse it.
					try {
						SimpleDateFormat df = new SimpleDateFormat(fmt);
						df.setTimeZone(TimeZone.getTimeZone("UTC"));
						Date d = df.parse(str);  //throws ParseException if no match
//						setLastFormat("Auto \"" + fmt + "\"");
						return d;     /////////////////////////////////found one!
					}
					catch (java.text.ParseException ex) {
						//No problem, just go on to next format to try.
					}
				}
			}
			prevWasDigit = isDigit;
		}
		return null;
	}

	/**
	 * Create a file name that doesn't contain a time
	 * @param directory Directory / folder
	 * @param prefix file prefix (part of file name to inlcude before the time stamp)
	 * @param fileType file end
	 * @return File path and name
	 */
	public static String createFileName(String directory, String prefix, String fileType) {
		return directory + FileParts.getFileSeparator() + createFileName(getTimeInMillis(), prefix, fileType);
	}

	/**
	 * Creates a file name containing the time and a user defined
	 * prefix and file end
	 * @param fileStartTime time
	 * @param prefix prefix for file name
	 * @param fileType file type (with or without the '.')
	 * @return file name
	 */
	public static String createFileName(long fileStartTime, String prefix, String fileType) {

		String fileName;
		if (fileType == null || fileType.length() < 1) {
			fileName = String.format("%s%s", prefix, fileTimeString(fileStartTime));
		}
		else {
			if (fileType.charAt(0) != '.') {
				fileType = "." + fileType;
			}
			fileName = String.format("%s%s%s",
					prefix, fileTimeString(fileStartTime),	fileType);
		}
		return fileName;
	}

	/**
	 * Like createFileName but the time now also includes milliseconds. 
	 * @param fileStartTime
	 * @param prefix
	 * @param fileType
	 * @return
	 */
	public static String createFileNameMillis(long fileStartTime, String prefix, String fileType) {

		String fileName;
		if (fileType == null || fileType.length() < 1) {
			fileName = String.format("%s%s", prefix, fileTimeStringms(fileStartTime));
		}
		else {
			if (fileType.charAt(0) != '.') {
				fileType = "." + fileType;
			}
			fileName = String.format("%s%s%s",
					prefix, fileTimeStringms(fileStartTime),	fileType);
		}
		return fileName;
	}

	private static Object fileTimeStringms(long fileStartTime) {
		String str = fileTimeString(fileStartTime);
		int millis = (int) (fileStartTime%1000);
		str += String.format("_%03d", millis);
		return str;
	}

	/**
	 * 
	 * @param fileStartTime tiem in milliseconds
	 * @return a date / time in a compressed format suitable to a file name
	 */
	private static String fileTimeString(long fileStartTime) {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(fileStartTime);
		c.setTimeZone(defaultTimeZone);

		//                              %3$tY%3$tm%3$td_%3$tH%3$tM%3$tS
		String fileStr = String.format("%1$tY%1$tm%1$td_%1$tH%1$tM%1$tS", c);

		return fileStr;

	}

	/**
	 * 
	 * @return a date string in a very compressed format (suitable for file names) 
	 */
	public static String getUnpunctuatedDate(boolean useLocal){
		//		 TODO Check that timezone is correct
		Calendar c = Calendar.getInstance();
		c.setTimeZone(getDisplayTimeZone(useLocal));
		Date date = new Date(c.getTimeInMillis());
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String dateStr = format.format(date) + "_UTC";
		return dateStr;
	}

	//	/**
	//	 * Get a GMT timestamp for output to a database
	//	 * @param millis time in milliseconds
	//	 * @return GMT timestamp
	//	 */
	//	public static Timestamp getTimeStamp(long millis) {
	//		TimeZone tz = TimeZone.getDefault();
	//		return new Timestamp(millis - tz.getOffset(millis));
	//	}

	//	/**
	//	 * Get a local timestamp using system default time zone. 
	//	 * @param millis time in milliseconds. 
	//	 * @return local timestamp. 
	//	 */
	//	public static Timestamp getLocalTimeStamp(long millis) {
	//		return new Timestamp(millis);
	//	}

	public static void setViewTimes(long start, long end) {
		setSessionStartTime(start);
		viewEndTime = end;
		viewPosition = (end-start) / 2;
	}

	public static long getViewEndTime() {
		return viewEndTime;
	}

	public static long getViewPosition() {
		return viewPosition;
	}

	public static void setViewPosition(long viewPosition) {
		PamCalendar.viewPosition = viewPosition;
	}

	/**
	 * Convert millis to MATLAB datenum. 
	 * @param timeMillis -  Java datetime millis
	 * @return MATLAB  datenum. 
	 */
	public static double millistoDateNum(long timeMillis){
		double datenum = ((double) timeMillis)/86400000.0+719529;
		return datenum; 
	}

	/**
	 * Convert MATLAB datenum to millis; 
	 * @param MATLAB datenum
	 * @return timeMillis equivalent of the MATLAB datenum. 
	 */
	public static long dateNumtoMillis(double datenum){
		long millis = (long) ((datenum-719529.0)*86400000.0);
		return millis; 
	}

	/**
	 * Convert to Unix Epoch. This is used by R. 
	 * @param millis -  Java datetime millis
	 */
	public static long millisToUnixEpoch(long millis) {
		return millis/1000L;
	}

	/**
	 * Converts millis to an excel serial data based on the Jan 1900 system. 
	 * @param timeMillis
	 * @return excel serial datenum. 
	 */
	public static double millistoExcelSerial(long timeMillis){
		return (millistoDateNum(timeMillis)-693960.0);
	}

	/**
	 * Converts excel Serial date number (Jan 1900 format) to millis. 
	 * @param excelSerial datenum
	 * @return timeMillis equivalent of the excel datenum. 
	 */
	public static long excelSerialtoMillis(double excelSerial){
		return (dateNumtoMillis(excelSerial+693960.0));
	}

	/**
	 * Convert date values to millisecond time
	 * @param dateString
	 * @return time in milliseconds
	 */
	public static long msFromDate(int year, int month, int day, int hour, int minute, int second, int millis) {
		//could have used date here but is a) old b) crap and c) doesn't include milliseconds. 
		LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute,second, millis*1000*1000); //must convert millis to nanosecond. 
		return ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	/**
	 * Read the duration and convert back to millis.
	 * @param duration - string representation of duration. Can be complete gobbly gook if inputed by user.
	 * @return the datenum of the duration in millis. 
	 */
	public static Long readTimeDuration(String duration){

		int days=0;
		int hours=0;
		int mins=0; 
		double seconds=0; 
		Long millis=null; 

		try {
			if (duration.contains("days") || duration.contains("days")){
				//remove all non numeric characters
				duration=duration.replaceAll("[^0-9]", "");
				//now we have the number of days
				days=Integer.valueOf(duration); 
			}

			if (duration.contains(":")){
				String[] parts=duration.split(":");
				if (parts.length==3){
					hours=Integer.valueOf(parts[0].replaceAll("[^0-9]", ""));
					mins=Integer.valueOf(parts[1].replaceAll("[^0-9]", ""));
					seconds=Double.valueOf(parts[2].replaceAll("[^0-9]", ""));
				}
			} 

			if (duration.contains("s")){
				int index=duration.indexOf("s");
				seconds=Double.valueOf(duration.substring(0,index));
			} 

			//System.out.println("days: "+days + " hours: "+ hours+ " minutes: "+ mins + " seconds: "+seconds);
			millis=(long) ((days*24*60*60 + hours*60*60 + mins*60 + seconds)*1000); 

		}
		catch (Exception e){
			System.err.println("Number format exception in spinner. Most likely duration entered incorrectly."); 
		}

		return millis;
	}

	/**
	 * Set a time correction to add to all calls to gettimemillis();
	 * @param correction time correction in millis. 
	 */
	public static void setTimeCorrection(long correction) {
		PamCalendar.timeCorrection = correction;
	}

	/**
	 * Get a time correction added to all calls to gettimemillis();
	 * @return time correction in millis. 
	 */
	public static long getTimeCorrection() {
		return timeCorrection;
	}


}
