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

import PamUtils.clock.SystemClock;

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

	

	/**
	 * 
	 * @return the amount of CPU used by the current process in 100ns intervals
	 * 
	 */
	public static long getProcessCPUTime () {
		return SystemClock.getSystemClock().getProcessCPUTime();
	}
	
	/**
	 * Set the system time. 
	 * @param timeMilliseconds
	 * @return
	 */
	public static boolean setSystemTime(long timeMilliseconds) {
		
		return SystemClock.getSystemClock().setSystemTime(timeMilliseconds);
		
	}

	
}
