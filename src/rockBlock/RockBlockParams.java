package rockBlock;
import java.io.Serializable;

import rocca.RoccaParameters;

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

/**
 * @author mo55
 *
 */
public class RockBlockParams implements Serializable, Cloneable {

    /** for serialization */
    private static final long serialVersionUID = 1;

	/** default baud rate */
	private int baud = 19200;
	
	/** default port name */
	private String portName = "COM5";
	
	/** default communications timing, in milliseconds */
	private int commTiming = 10000;
	
	/** number of seconds to wait after a message is added to the queue before trying to send, in milliseconds */
	private int sendDelayMillis = 0;
	

	/**
	 * 
	 */
	public RockBlockParams() {
	}

	public int getBaud() {
		return baud;
	}

	public void setBaud(int baud) {
		this.baud = baud;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public int getCommTiming() {
		return commTiming;
	}

	/**
	 * Sets the communication timer by passing a value in milliseconds
	 * 
	 * @param commTiming the desired time, in milliseconds
	 */
	public void setCommTimingMillis(int commTiming) {
		this.commTiming = commTiming;
	}

	/**
	 * Sets the communication timer by passing a value in seconds
	 * 
	 * @param commTiming the desired time, in seconds
	 */
	public void setCommTimingSeconds(int commTiming) {
		this.commTiming = commTiming * 1000;
	}

	@Override
    public RockBlockParams clone() {
		try {
			RockBlockParams n = (RockBlockParams) super.clone();
			return n;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

	public int getSendDelayMillis() {
		return sendDelayMillis;
	}

	public void setSendDelayMillis(int sendDelayMillis) {
		this.sendDelayMillis = sendDelayMillis;
	}

	public void setSendDelaySeconds(int sendDelaySeconds) {
		this.sendDelayMillis = sendDelaySeconds*1000;
	}


}
