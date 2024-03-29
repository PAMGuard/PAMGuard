package tethys;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import PamUtils.PamCalendar;
import nilus.Helper;

public class TethysTimeFuncs {

	private static TimeZone timeZone = TimeZone.getTimeZone("UTC");

	/*
	 * Copied from http://www.java2s.com/Code/Java/Development-Class/ConvertsagiventimeinmillisecondsintoaXMLGregorianCalendarobject.htm
	 */
	public static XMLGregorianCalendar xmlGregCalFromMillis(long millis) {
		try {
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeZone(timeZone);
            calendar.setTimeInMillis(millis);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                calendar);
        }
        catch (final DatatypeConfigurationException ex) {
            System.out.println("Unable to convert date '%s' to an XMLGregorianCalendar object");
            return null;
        }
	}

	/**
	 * Convert a Gregorian calendar value back to milliseconds.
	 * @param xmlGregorian
	 * @return
	 */
	public static Long millisFromGregorianXML(XMLGregorianCalendar xmlGregorian) {
		if (xmlGregorian == null) {
			return null;
		}
	    GregorianCalendar gc2 = xmlGregorian.toGregorianCalendar();
	    gc2.setTimeZone(timeZone);
		return gc2.getTimeInMillis();
	}

	/**
	 * Make a Gregorian calendar object from a returned XML string.
	 * @param gregorianString
	 * @return
	 */
	public static XMLGregorianCalendar fromGregorianXML(String gregorianString) {
		
		try {
			XMLGregorianCalendar xmlCal = Helper.timestamp(gregorianString);
			return xmlCal;
		} catch (DatatypeConfigurationException e1) {
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
		/**
		 * Above should work just fine. If it doesn't use my own code below...
		 */
		
		// typical string is 2018-10-20T00:00:00Z
		if (gregorianString == null) {
			return null;
		}
//		GregorianCalendar gCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		gregorianString = gregorianString.replace("T", " ");
		gregorianString = gregorianString.replace("Z", "");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = null;
		try {
			date = df.parse(gregorianString);
		} catch (ParseException e) {
			System.out.printf("Unparsable date string:\"%s\"", gregorianString);
			e.printStackTrace();
			return null;
		}
		return xmlGregCalFromMillis(date.getTime());
//		gCal.setTimeInMillis(date.getTime());
////		gCal.se
//		return gCal;
	}

	public static String formatGregorianTime(XMLGregorianCalendar gregCal) {
		if (gregCal == null) {
			return null;
		}
		Long millis = millisFromGregorianXML(gregCal);
		if (millis == null) {
			return gregCal.toString();
		}
		return PamCalendar.formatDBDateTime(millis);
	}
}
