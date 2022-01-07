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

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class for some system based time functions, primarily to get
 * CPU time for the process and to set the system time. These are 
 * bundled together here purely for the convenience of having the 
 * native code in a single dll. A windows DLL has been written - 
 * we need someone to write DLL's for other OS. 
 * 
 * @author Doug Gillespie
 * @see PamModel.PamProfiler
 *
 */
public class SystemTiming {

	private static final String SILIB = "PamTiming";
	
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
	
	private static boolean loadOK = false;
	
	private static boolean loadTried = false;
	
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

	/**
	 * 
	 * @return the amount of CPU used by the current process in 100ns intervals
	 * 
	 */
	public static long getProcessCPUTime () {
		if (loadTried == false) {
			load();
		}
		if (loadOK == false) return 0;

		return sysGetProcessCPUTime();
	}
	
	public static boolean setSystemTime(long timeMilliseconds) {
		if (loadTried == false) {
			load();
		}
		if (loadOK == false) return false;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMilliseconds);
		return setSystemTime(c);
	}

	public static boolean setSystemTime(Calendar c) {
		if (loadTried == false) {
			load();
		}
		if (loadOK == false) return false;
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sysSetSystemTime(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 
				c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
	}
}
