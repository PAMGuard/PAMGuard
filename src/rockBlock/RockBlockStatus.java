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



package rockBlock;

import PamguardMVC.debug.Debug;

/**
 * @author mo55
 *
 */
public class RockBlockStatus {

	/** String indicating which COM port we're trying to use */
	private String comPort = null;
	
	/** flag indicating whether or not we have successfully established communication with the RockBlock */
	private boolean commReady = false;
	
	/** String containing the port error, or null if no error */
	private String portError = null;

	/** the signal strength of the last check, from 0-5 */
	private int signalStrength = 0;

	/** number of outgoing messages in queue */
	private int numOutgoingMess = 0;

	/** number of incoming messages in queue */
	private int numIncomingMess = 0;

	/**
	 * @param comPort Com Port we're trying to use
	 * @param commReady boolean indicating whether or not we are communicating with RockBlock
	 * @param portError error code, if any
	 * @param signalStrength int 0-5
	 * @param numOutgoingMess number of messages in the outgoing queue
	 * @param numIncomingMess number of incoming messages waiting to be received
	 */
	public RockBlockStatus(String comPort, 
			boolean commReady, 
			String portError, 
			int signalStrength, 
			int numOutgoingMess,
			int numIncomingMess) {
		super();
		this.comPort = comPort;
		this.commReady = commReady;
		this.signalStrength = signalStrength;
		this.numOutgoingMess = numOutgoingMess;
		this.numIncomingMess = numIncomingMess;
	}

	public String getComPort() {
		return comPort;
	}

	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	public boolean isCommReady() {
		return commReady;
	}

	public void setCommReady(boolean commReady) {
		this.commReady = commReady;
	}

	public String getPortError() {
		return portError;
	}

	public void setPortError(String portError) {
		Debug.out.println(portError);
		this.portError = portError;
	}

	public int getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	public int getNumOutgoingMess() {
		return numOutgoingMess;
	}

	public void setNumOutgoingMess(int numOutgoingMess) {
		this.numOutgoingMess = numOutgoingMess;
	}
	
	/**
	 * Add 1 to the number of outgoing messages in the queue
	 */
	public void incOutgoingCount() {
		this.numOutgoingMess++;
	}

	/**
	 * Subtract 1 to the number of outgoing messages in the queue
	 */
	public void decOutgoingCount() {
		this.numOutgoingMess--;
	}

	public int getNumIncomingMess() {
		return numIncomingMess;
	}

	public void setNumIncomingMess(int numIncomingMess) {
		this.numIncomingMess = numIncomingMess;
	}
	
}
