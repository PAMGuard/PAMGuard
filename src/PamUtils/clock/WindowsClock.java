package PamUtils.clock;

import java.util.Calendar;
import java.util.TimeZone;

public class WindowsClock extends OSClock {


	private static boolean loadOK = false;
	
	private static boolean loadTried = false;

	private static final String SILIB = "PamTiming";
	
	public WindowsClock(String osName) {
		super(osName);
		load();
	}
	public static void load() {
		
		try  {
			System.loadLibrary(SILIB);
			loadOK = true;
		}
		catch (UnsatisfiedLinkError e)
        {
            System.out.println ("native lib '" + SILIB + "' not found in 'java.library.path': "
            + System.getProperty ("java.library.path"));
            loadOK = false;
        }
		
		loadTried = true;
	}

	@Override
	public boolean setSystemTime(long timeMilliseconds) {
		if (!loadTried) {
			load();
		}
		if (!loadOK) return false;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMilliseconds);
		return setSystemTime(c);
	}
	
	private static boolean setSystemTime(Calendar c) {
		if (!loadOK) {
			return false;
		}
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sysSetSystemTime(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
	}

	@Override
	protected long getProcessCPUTime() {
		if (!loadOK) {
			return -1;
		}
		else {
			return sysGetProcessCPUTime();
		}
	}

	/**
	 * 
	 * @return
	 */
	private static native long sysGetProcessCPUTime ();
	
	/**
	 * Set the computers system time
	 * @param year Year
	 * @param month Month (1 - 12)
	 * @param day Day (1 - 31)
	 * @param hour Hour (0 - 23)
	 * @param minute Minute (0 - 59)
	 * @param second Second (0 - 59)
	 * @param millisecond Millisecond (0 - 999)
	 * @return true if successful
	 */
	private static native boolean sysSetSystemTime(int year, int month, int day, int hour, int minute, int second, int millisecond);

}
