package tethys;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class TethysTimeFuncs {

	/*
	 * Copied from http://www.java2s.com/Code/Java/Development-Class/ConvertsagiventimeinmillisecondsintoaXMLGregorianCalendarobject.htm
	 */
	public static XMLGregorianCalendar xmlGregCalFromMillis(long millis) {
		try {
            final GregorianCalendar calendar = new GregorianCalendar();
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
		return gc2.getTimeInMillis();
	}
}
