package Acquisition;

abstract public class AudioFileFuncs {

	public static final int FILE_OK = 0;
	public static final int FILE_ERROR = 1;
	public static final int FILE_REPAIRED = 2;
	public static final int FILE_CANTREPAIR = 3;
	public static final int FILE_DOESNTEXIST = 4;
	public static final int FILE_CANTOPEN = 5;
	public static final int FILE_UNKNOWNTYPE = 6;

	public static String getMessage(int m) {
		switch(m) {
		case FILE_OK:
			return "Ok";
		case FILE_ERROR:
			return "Error";
		case FILE_REPAIRED:
			return "Repaired";
		case FILE_CANTREPAIR:
			return "Can't Repair";
		case FILE_DOESNTEXIST:
			return "Doesn't Exist";
		case FILE_CANTOPEN:
			return "Can't Open";
		case FILE_UNKNOWNTYPE:
			return "Unknown Type";
		}
		return null;
	}
}
