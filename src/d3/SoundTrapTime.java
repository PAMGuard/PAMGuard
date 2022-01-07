package d3;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import soundtrap.STXMLFile;

public class SoundTrapTime {

	/**
	 * Get a time string for a sound trap. 
	 * @param file
	 * @return valid time or Long.MIN_VALUE
	 */
	public static long getSoundTrapTime(File file, String dateTimeFormat) {
		if (dateTimeFormat == null || dateTimeFormat.isBlank()) {
			dateTimeFormat = STXMLFile.defaultDateFormat;
		}
		long t = getTimeFromXMLFile(file, dateTimeFormat);
		if (t == Long.MIN_VALUE) {
			return getTimeFromFileName(file);
		}
		else {
			return t;
		}
	}

	public static long getTimeFromXMLFile(File file, String dateTimeFormat) {
		if (file == null) {
			return Long.MIN_VALUE;
		}
		File xFile = STXMLFile.findXMLFile(file);
		
		STXMLFile stFile = STXMLFile.openXMLFile(xFile, dateTimeFormat);
		if (stFile == null) {
			return Long.MIN_VALUE;
		}
		try {
			long startTime = stFile.getWavInfo().getTimeInfo().samplingStartTimeUTC;
			if (startTime > 0) {
				return startTime;
			}
		}
		catch (Exception e) {
			System.out.println("Error finding UTC time in Sound Trap xml file " + xFile.getName() + " " + e.getMessage());
		}
		return Long.MIN_VALUE;
	}
	public static long getTimeFromFileName(File file) {
		if (file == null) {
			return Long.MIN_VALUE;
		}
		String name = file.getName();
		// should have two decimal points in it. 
		int nDot = 0;
		int[] dotPos = new int[2];
		int pos = -1;
		while (true) {
			pos = name.indexOf('.', pos+1);
			if (pos == -1) {
				break;
			}
			if (nDot == 2) {
				return Long.MIN_VALUE; // too many !
			}
			dotPos[nDot++] = pos;
		}
		if (nDot != 2) {
			return Long.MIN_VALUE;
		}
		String dateStr = name.substring(dotPos[0]+1, dotPos[1]);
		//		System.out.println(name);
		String fmt = "yyMMddHHmmss";
		try {
			SimpleDateFormat df = new SimpleDateFormat(fmt);
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date d = df.parse(dateStr);  //throws ParseException if no match
			if (d != null) {
				Calendar cl = Calendar.getInstance();
				cl.setTimeZone(TimeZone.getTimeZone("UTC"));
				cl.setTime(d);
				return cl.getTimeInMillis();
			}
			//			return d.getTime();
		}
		catch (java.text.ParseException ex) {
			return Long.MIN_VALUE;
		}
		return Long.MIN_VALUE;
	}

}
