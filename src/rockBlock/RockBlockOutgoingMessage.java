package rockBlock;
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
 * Data unit containing messages that are being sent from the local RockBlock.  These are typically
 * termed 'Mobile Originated' or MO messages indicating they are being sent from the RockBlock to
 * the Iridium network
 * 
 * @author mo55
 *
 */
public class RockBlockOutgoingMessage extends RockBlockMessage {

	/** Flag indicating whether message has been sent */
	private boolean messageSent = false;
	
	/** The number of times we've attempted to send this message */
	private int numberOfAttempts = 0;
	
	/** For delayed send attempt, need to wait until at least this time before sending */
	private long doNotSendBefore = 0;
	
	/**
	 * @param timeMilliseconds
	 * @param message
	 */
	public RockBlockOutgoingMessage(long timeMilliseconds, String message) {
		super(timeMilliseconds, message);
	}

	public boolean isMessageSent() {
		return messageSent;
	}

	public void setMessageSent(boolean messageSent) {
		this.messageSent = messageSent;
	}
	
	public void clearSendAttempts() {
		numberOfAttempts = 0;
	}
	
	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}
	
	public void incAttempt() {
		numberOfAttempts++;
	}
	
	public void setAttempts(int attempts) {
		numberOfAttempts = attempts;
	}

	public long getDoNotSendBefore() {
		return doNotSendBefore;
	}


	/**
	 * @param doNotSendBefore
	 */
	public void setDoNotSendBefore(long doNotSendBefore) {
		this.doNotSendBefore = doNotSendBefore;
	}

}
