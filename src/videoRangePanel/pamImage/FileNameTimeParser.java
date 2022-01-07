package videoRangePanel.pamImage;

import java.io.File;
import java.io.Serializable;

import PamUtils.PamCalendar;

/**
 * Parser which calculate the time from a file's name. 
 * @author Jamie Macaulay
 *
 */
public class FileNameTimeParser implements ImageTimeParser,  Serializable, Cloneable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static int VLC_SNAPSHOT_1 = 0; 

	private int parserType; 

	public FileNameTimeParser(int type) {
		parserType=type; 
	}

	@Override
	public String getName() {
		switch (parserType) {
		case VLC_SNAPSHOT_1:
			return "VLC_SNAPSHOT";
		}
		return "";
	}



	@Override
	public long getTime(File file) {

		switch (parserType) {
		case VLC_SNAPSHOT_1:
			return getVLCSnapShotTime(file); 
		}
		return 0;

	}

	/**
	 * 
	 * VLC snapshot format. 
	 * RandomName_YYYYMMDD_HH_MM_SS_AddHH_AddMM_AddSSNNNNN
	 *YYYYMMDD: date of recording
	 *HH_MM_SS: time of recording
	 *AddHH_AddMM_AddSS: time into the video added by VLC
	 *NNNNN: sequential number given by VLC
	 * @param file - the file 
	 * @return
	 */
	private long getVLCSnapShotTime(File file) {
		String filename = file.getName(); 

		try {

			//get to the first number
			int i = 0;
			while (i < filename.length() && !(filename.charAt(i) == '_')) i++;
			int j = i+1;


			//date 
			int years=Integer.valueOf(filename.substring(j, j+4)); 
			j=j+4; 
			int months=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+2; 
			int days=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+2;

			filename=filename.substring(j);
			i=0; 
			while (i < filename.length() && !Character.isDigit(filename.charAt(i))) i++;
			j = i;


			//time of video start
			int hoursStart=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+3; 
			int minutesStart=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+3; 
			int secondsStart=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+3; 

			filename=filename.substring(j);
			i=0;
			while (i < filename.length() && !Character.isDigit(filename.charAt(i))) i++;
			j = i;


			//time into recording
			int hoursIn=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+3; 
			int minutesIn=Integer.valueOf(filename.substring(j, j+2)); 
			j=j+3; 
			int secondsIn=Integer.valueOf(filename.substring(j, j+2)); 

			//			System.out.println("Date " + years + " " + months + " " + days);
			//			System.out.println("Time " + hoursStart + " " + minutesStart + " " + secondsStart);
			//			System.out.println("Time into video " + hoursIn + " " + minutesIn + " " + secondsIn);

			//		System.out.println(String.format("Time parser: File time info: year: %d month: %d day %d: File start time: %d hour:"
			//				+ " %d minute: %d: second: %d Recording: hours in: %d mins in: %d seconds in: %d ", year, month, day, hour, minute, 
			//				second, hoursIn, minutesIn, secondsIn));


			//compensate for values above 60s, 60m, 24hrs
			int seconds =  secondsStart + secondsIn; 
			int minutes =  minutesStart + minutesIn; 
			int hours =  hoursStart + hoursIn; 

			while (seconds>=60)  {
				minutes=minutes+1;
				seconds=seconds-60; 
			}

			while (minutes>=60)  {
				hours=hours+1;
				minutes=minutes-60; 
			}

			while (hours>=24)  {
				days=days+1;
				hours=hours-24; 
			}

			//get the time. 
			long millis = PamCalendar.msFromDate(years, months, days, hours, minutes, seconds, 0);

			return millis;
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}

		return 0; 
	}


}
