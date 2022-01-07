/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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



package PamguardMVC;

/**
 * @author mo55
 *
 */
public class ObservedObject {
	
	public static final int ADDDATA = 0;
	public static final int UPDATEDATA = 1;
	public static final int NOTENEWSETTINGS = 2;
	public static final int SETSAMPLERATE = 3;
	public static final int MASTERCLOCKUPDATE = 4;
	public static final int SOURCENOTIFICATION = 5;
	
	/** The Pamguard Time (in millis) when the object was created */
	private long timeMillis;
	
	/** the action to perform */
	private int action;

	/** a list of objects to pass (the type of objects will
	 * depend on what the action is  */
	private Object[] observed;
	
	/**
	 * @param observerd
	 * @param action
	 */
	public ObservedObject(long time, int action, Object ...observed) {
		super();
		this.timeMillis = time;
		this.observed = observed;
		this.action = action;
	}

	public Object[] getObserved() {
		return observed;
	}

	public void setObserved(Object ...observed) {
		this.observed = observed;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public long getTimeMillis() {
		return timeMillis;
	}

	public void setTimeMillis(long time) {
		this.timeMillis = time;
	}

}
