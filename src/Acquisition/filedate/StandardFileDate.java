package Acquisition.filedate;

import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import Acquisition.KETime;
import Acquisition.layoutFX.FileDatePane;
import Acquisition.layoutFX.StandardFileDatePane;
import Acquisition.sud.SUDFileTime;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import d3.D2TextTime;
import d3.D3XMLFile;
import d3.SoundTrapTime;


/**
 * Implementation of FileDate to read the standard file
 * date format that I use in Logger. I'd have thought that
 * others would be keen to add alternatives, but no one has !
 * 
 * @author Doug Gillespie
 *
 */
public class StandardFileDate implements FileDate, PamSettings {

	private PamSettings parentSetting;

	private StandardFileDateSettings settings = new StandardFileDateSettings();
	
	/**
	 * JavaFX settings pane for a standard file date. 
	 */
	private StandardFileDatePane standardFileDatePane; 
	
	/**
	 * StandardSettings needs to implement PamSettings so that it can store it's own configuration settings. 
	 * These settings however need to be coupled to some kind of master settings, e.g. the Acquisition module
	 * so that these settings get stored with the same unit name. 
	 * @param parentSetting
	 */
	public StandardFileDate(PamSettings parentSetting) {
		super();
		if (parentSetting != null) {
			this.parentSetting = parentSetting;
			PamSettingManager.getInstance().registerSettings(this);
		}
		//		String[] ids = TimeZone.getAvailableIDs();
		//		TimeZone tz;
		//		for (int i = 0; i < ids.length; i++) {
		//			tz = TimeZone.getTimeZone(ids[i]);
		//			System.out.println(String.format("raw offset %3.1fh (%3.1fh): %s (%s)", tz.getRawOffset()/3600000.,tz.getOffset(System.currentTimeMillis())/3600000., ids[i], tz.getDisplayName()));
		//		}
	}

	final String[] formats = {
			"HH_mm_ss'__DMY_'dd_MM_yy", // Inez DSG files egCopy of _RAWD_HMS_01_27_00__DMY_09_05_2010.wav
			"HH'h'mm'm'ss's'ddMMMyyyy", // Inez CRSS files
			"yyyy_MM_dd_HH_mm_ss", //lime kiln data
			"yyyyMMdd_HHmmss_SSS", // PAMGuard format with additional milliseconds. 
			"yyyy.MM.dd_HH.mm.ss", 
			"yyyy.MM.dd-HH.mm.ss",
			"yyyyMMdd_HHmmss", 
			"yyyyMMdd$HHmmss", //wildlife Acoustics
			"yyyyMMdd-HHmmss", //Aurel
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
			"yyyy-MM-dd HH-mm-ss" // RS Orca recorder. index 32. Must remain at this position !!!!
	};

	private D2TextTime d2TextTime = new D2TextTime();

	private String lastFormat = "Auto format";

	public void setLastFormat(String lastFormat) {
		this.lastFormat = lastFormat;
	}

	@Override
	public String getFormat() {
		if (lastFormat == null) {
			lastFormat = "Auto format";
		}
		return lastFormat;
	}

	public boolean doSettings(Window parent) {
		StandardFileDateSettings newSettings = FileDateDialog.showDialog(parent, settings, true);
		if (newSettings != null) {
			settings = newSettings.clone();
			return true;
		}
		else {
			return false;
		}
	}

	public String getDescription() {
		return "Standard yyyymmdd_hhmmss file dates as used by Pamguard and Logger recorders";
	}

	public String getName() {
		return "Standard Data Format";
	}

	@Override
	public long getTimeFromFile(File file) {
		
//		System.out.println("Get time from file: getTimeFromFile" ); 
		
		// if the user wants to force the local PC time, return immediately
		if (settings.isForcePCTime()) return 0;
		
		// true to figure out the time from the filename
		long time = doTheWork(file);
		if (time == 0) {
			return 0;
		}
		else {
			long offset = settings.getTimeOffset(time);
			return time + offset;
		}
	}
	
	/**
	 * Pull the time from the file name. 
	 * @param file
	 * @return time in millis UTC. 
	 */
	private long doTheWork(File file) {
//		allowCustomFormats = false;
		if (settings.isUseBespokeFormat() && settings.getForcedDateFormat() != null) {
			return forcedDataFormat(file, settings.getForcedDateFormat());
		}

		long sudTime = SUDFileTime.getSUDFileTime(file);
		if (sudTime != Long.MIN_VALUE) {
			return sudTime;
		}
		
		/*
		 * Dtag files have an accompanying XML file which 
		 * has the same name. Try to find such a file and get
		 * some time information from it. 
		 */
		long dTagTime = D3XMLFile.getXMLStartTime(file);
		if (dTagTime != Long.MIN_VALUE) {
			setLastFormat("D3 time from xml file");
			return dTagTime;
		}
		

		long stTime = SoundTrapTime.getSoundTrapTime(file, settings.getDateTimeFormatToUse());
		if (stTime != Long.MIN_VALUE) {
			setLastFormat("Soundtrap file format \"" + settings.getDateTimeFormatToUse() + "\"");
//			allowCustomFormats = true;
			return stTime;
		}

		long d2Time = d2TextTime.getTimeFromFile(file);
		if (d2Time != Long.MIN_VALUE) {
			setLastFormat("D2 time from txt file");
			return d2Time;
		}

		// find the first and last numeric characters in the file name
		String fileName = file.getName();
		if (fileName.length() == 0) return 0;
		int firstNum = -1, lastNum = -1;
		char c;
		for (int i = 0; i < fileName.length(); i++) {
			c = fileName.charAt(i);
			if (c >= '0' && c <= '9') {
				if (firstNum == -1) {
					firstNum = i;
				}
				lastNum = i;
			}
		}
		if (lastNum < 0) return 0;
		
		String numbers = fileName.substring(firstNum, lastNum+1);
		//		System.out.println(numbers);
		
		
		
		//Custom time stamps	
		long keTime = KETime.getKEBuoyTime(numbers);
		if (keTime != Long.MIN_VALUE) {
			setLastFormat("KE Buoy \"yyyyMMdd_HHmmss\"");
			return keTime;
		}

		long plaBuoyTime = KETime.getPLABuoyTime(numbers);
		if (plaBuoyTime != Long.MIN_VALUE) {
			setLastFormat("PLA Buoy \"yyyyMMdd_HHmmss_FFFFFF\"");
			return plaBuoyTime;
		}

		//Default time stamps
		Date dt = tryParseDate(numbers);
		
		//Last modified time of file
//		if (dt==null) {
//			dt=tryUseFileCreate(file);
//			if (dt != null) {
//				setLastFormat("Using file time");
//			}
//		}
		
		//Computer time
		//use the computer time- this does not make much sense if analysis used multiple files is
		//going faster than real time. 
		if (dt != null) {
			Calendar cl = Calendar.getInstance();
			cl.setTimeZone(TimeZone.getTimeZone("GMT"));
			cl.setTime(dt);
			return cl.getTimeInMillis();
		}
		
		//None of the formats worked.
		setLastFormat("Unparsable date format: " + numbers);
		return 0;
	}

	private long forcedDataFormat(File file, String forcedDateFormat) {
		if (file == null) {
			return 0;
		}
		String name = file.getName();
		name = removeWildChars(name, forcedDateFormat);
		String redFormat = forcedDateFormat.replace("#", ""); 
		// see if it's only all milliseconds, i.e. format is only 'S's and > 12 of them
		if (allSSSS(redFormat)) {
			// try pulling a number from the name.
			try {
				long millis = Long.valueOf(name);
				return millis;
			}
			catch (NumberFormatException e) {
				
			}
		}
		
		SimpleDateFormat sdf = null;
		try {
		 sdf = new SimpleDateFormat(redFormat);
		}
		catch (IllegalArgumentException e) {
			System.out.println("Invalid date format " + forcedDateFormat + " " + e.getMessage());
			return 0;
		}
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d = null;
		try {
			d = sdf.parse(name);
//			System.out.println(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("StandardfileDate.forcedDataFormat:" + e.getMessage());
		}  //throws ParseException if no match
		setLastFormat(forcedDateFormat);
		if (d == null) {
			return 0;
		}
		return d.getTime();
	}

	private boolean allSSSS(String redFormat) {
		for(int i = 0; i < redFormat.length(); i++) {
			if (redFormat.charAt(i) != 'S') {
				return false;
			}
		}
		return true;
	}

	private String removeWildChars(String name, String forcedDateFormat) {
//		# is the wild field i s#
		if (name == null || forcedDateFormat == null) {
			return name;
		}
		byte[] nb = name.getBytes();
		byte[] df = forcedDateFormat.getBytes();
		byte wc = '#';
		int l = Math.min(name.length(), forcedDateFormat.length());
		for (int i = 0; i < l; i++) {
			if (df[i] == wc) {
				nb[i] = wc;
			}
		}
		name = new String(nb);
		name = name.replaceAll("#", "");
		return stripEndChars(name);
	}
	
	/**
	 * Strip any characters off the start and end of the string. 
	 * @param name
	 * @return
	 */
	private String stripEndChars(String name) {
		byte[] nb = name.getBytes();
		int f = 0;
		int l = name.length()-1;
		for (int i = 0; i < nb.length; i++) {
			if (nb[i] < '0' || nb[i] > '9') {
				f++;
			}
			else {
				break;
			}
		}
		for (int i = l; i >= 0; i--) {
			if (nb[i] < '0' || nb[i] > '9') {
				l--;
			}
			else {
				break;
			}			
		}
		if (l < f) {
			return null;
		}
		return name.substring(f, l+1);
	}

	/**
	 * Use the last modified meta data from a file if all else fails. 
	 * @param file - the file
	 * @return the date of the file.
	 */
	private Date tryUseFileCreate(File file) {
		try {
			long timeMillis = file.lastModified(); 
			Date date= new Date(timeMillis); 
			return date; 
		}
		catch (Exception e) {
			System.out.println(String.format("Could not find last modified in file %s", file.getName())); 	
			return null;
		}
	}

	/** 
	 * For each digit substring in the file name (i.e., substring starting
	 * with a digit that does not have a digit preceding), try a bunch of
	 * format strings and see if any of them match.  If there is a match,
	 * return the corresponding Date, else return null.
	 */
	private Date tryParseDate(String inputString)
	{
		/* Formats have various permutations of 4-digit vs. 2-digit year, 
		 * '-' vs. '_' as the separator between date and time, seconds or
		 * not, and dots between adjacent numbers or not.  Also yearday
		 * vs. month-and-day, though formats using yearday don't allow for
		 * 4-digit years.
		 */  
		String numstr = new String(inputString);
		
		/*
		 * Inez has some files in the format 0222.DSG_RAWD_HMS_21_50_ 0__DMY_ 1_ 7_10.wav.
		 * Look for the .DSG early in the string and get rid of it !
		 */
		int subInd = numstr.indexOf(".DSG");
		if (subInd >= 0) {
			// find the first number after that and hope it's OK from there ...
			numstr = numstr.substring(subInd); // 
		}
		/*
		 * The DCL 5 dataset has some long names which contain the date, but also some other numbers which 
		 * really screws this up. Search for multiple groups of numbers and keep the last two which are > 6 characters long.   
		 */
		numstr = findDateSubstring(numstr);


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
						df.setTimeZone(TimeZone.getTimeZone("GMT"));
						Date d = df.parse(str);  //throws ParseException if no match
						setLastFormat("Auto \"" + fmt + "\"");
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

	private String findDateSubstring(String numstr) {
		boolean prevWasDigit = false;
		boolean isDigit;
		char aChar;
		numstr += "_";
		ArrayList<String> stringBits = new ArrayList<String>();
		String aString = new String();
		String msString = null;;
		int n = numstr.length();
		for (int i = 0; i < n; i++) {
			isDigit = java.lang.Character.isDigit(aChar = numstr.charAt(i));
			if (isDigit) {
				aString += aChar;
			}
			else if (aString.length() > 0) {
				if (aString.length() == 6 || aString.length() == 8) {
					stringBits.add(aString);
				}
				else if (stringBits.size() == 2) {
					msString = new String(aString);
				}
				aString = new String();
			}
		}
		int nBits = stringBits.size();
		if (nBits < 2) {
			return numstr;
		}
		else {
			String finalString = stringBits.get(nBits-2) + "_" + stringBits.get(nBits-1); 
			if (msString != null) {
				finalString += "_" + msString;
			}
			return finalString;
		}

	}

	public boolean hasSettings() {
		return true;
	}

	@Override
	public String getUnitName() {
		return parentSetting.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Standard File Date";
	}

	@Override
	public Serializable getSettingsReference() {
		return settings;
	}

	@Override
	public long getSettingsVersion() {
		return StandardFileDateSettings.serialVersionUID;
	}
	
	public StandardFileDateSettings getSettings() {
		return settings;
	}


	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		settings = ((StandardFileDateSettings) pamControlledUnitSettings.getSettings()).clone();
		return settings != null;
	}

	
	@Override
	public FileDatePane doSettings() {
		if (standardFileDatePane ==null) {
			standardFileDatePane = new StandardFileDatePane(this);
		}
		return standardFileDatePane;
	}

	/**
	 * Set the settings. 
	 * @param settings - the settings to set. 
	 */
	public void setSettings(StandardFileDateSettings settings2) {
		this.settings=settings2; 
	}

}
