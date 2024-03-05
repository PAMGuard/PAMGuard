package Acquisition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class KETime {

//
//	/**
//	 * Unpack the KE buoy data format. This has a decimal number of 
//	 * seconds, but is otherwise quite rational. 
//	 * @param numPart - the date string
//	 * @return time in millis
//	 */
//	public static long getKEBuoyTime(String numPart) {
//		int dotPos = numPart.indexOf('.', 0);
//		
//		if (dotPos < 0) {
//			return Long.MIN_VALUE;
//		}
//	
//		String mainBit = numPart.substring(0, dotPos);
//		String fracPart = numPart.substring(dotPos);
//		String fmt = "yyyyMMdd_HHmmss";
//
//		SimpleDateFormat df = new SimpleDateFormat(fmt);
//		df.setTimeZone(TimeZone.getTimeZone("GMT"));
//		Date d = null;
//		try {
//			d = df.parse(mainBit);
//		} catch (ParseException e) {
//			return Long.MIN_VALUE;
//		}
//		if (d == null) {
//			return Long.MIN_VALUE;
//		}
//
//		Calendar cl = Calendar.getInstance();
//		cl.setTimeZone(TimeZone.getTimeZone("GMT"));
//		cl.setTime(d);
//		long millis = cl.getTimeInMillis();
//		
//		try {
//			double fracBit = Double.valueOf(fracPart);
//			millis += (long) (fracBit*1000.);z
//		}
//		catch (NumberFormatException e) {
//			
//		}
//
//		return millis;
//	}
//	
//	/**
//	 * Unpack the PLA buoy data format (superseeds KE Buoy). This has a nanosecond value after the seconds
//	 * e.g. would be ddmmyyyy_HHMMSS_FFFFF in MATLAB format. 
//	 * @param numPart - the date string
//	 * @return time in millis
//	 */
//	public static long getPLABuoyTime(String numPart) {
//		String[] timeSections=numPart.split("_");
//		
//		//got have a data section, time section and then nanosecond section 
//		if (timeSections.length!=3 || timeSections[2].length()!=6){
//			return Long.MIN_VALUE;
//		}
//	
//		String mainBit = timeSections[0]+timeSections[1];
//		String fracPart = timeSections[2];
//		String fmt = "yyyyMMddHHmmss";
//
//		SimpleDateFormat df = new SimpleDateFormat(fmt);
//		df.setTimeZone(TimeZone.getTimeZone("GMT"));
//		Date d = null;
//		try {
//			d = df.parse(mainBit);
//		} catch (ParseException e) {
//			return Long.MIN_VALUE;
//		}
//		if (d == null) {
//			return Long.MIN_VALUE;
//		}
//
//		Calendar cl = Calendar.getInstance();
//		cl.setTimeZone(TimeZone.getTimeZone("GMT"));
//		cl.setTime(d);
//		long millis = cl.getTimeInMillis();
//		
//		try {
//			double fracBit = Double.valueOf(fracPart);
//			millis += (long) (fracBit/1000.);
//		}
//		catch (NumberFormatException e) {
//			
//		}
//
//		return millis;
//	}
}
