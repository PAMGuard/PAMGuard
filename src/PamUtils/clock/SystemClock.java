package PamUtils.clock;

import PamUtils.PlatformInfo;
import PamUtils.PlatformInfo.OSType;

/**
 * functions for setting the system clock. Doesn't really work on Windows any more, but OK on Linux. 
 */
public class SystemClock {

	private static SystemClock singleInstance;
	
	private OSClock osClock;
	
	private SystemClock() {
		osClock = makeOSClock();
	}
	
	private OSClock makeOSClock() {
	    String osName = System.getProperty("os.name").toLowerCase();
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			return new WindowsClock(osName);
		}
		else {
			return new LinuxClock(osName);
		}
	}

	public static SystemClock getSystemClock() {
		if (singleInstance == null) {
			singleInstance = new SystemClock();
		}
		return singleInstance;
	}
	public long getProcessCPUTime() {
		if (osClock == null) {
			return -1;
		}
		else {
			return osClock.getProcessCPUTime();
		}
	}
	
	public boolean setSystemTime(long timeMilliseconds) {
		if (osClock == null) {
			return false;
		}
		return osClock.setSystemTime(timeMilliseconds);
	}
}
