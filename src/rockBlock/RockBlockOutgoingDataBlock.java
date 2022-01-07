package rockBlock;
import java.util.ListIterator;

import PamguardMVC.PamProcess;

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
public class RockBlockOutgoingDataBlock extends RockBlockDataBlock<RockBlockOutgoingMessage> {

	/** index of the first message that needs to be sent */
	private int firstMessageToSend = 0;
	
	/**
	 * @param unitClass
	 * @param dataName
	 * @param parentProcess
	 * @param channelMap
	 */
	public RockBlockOutgoingDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
	}
	
	/**
	 * Set the index counter to the first data unit that hasn't been sent yet.  If
	 * all units have been set, set the index to -1
	 */
	private void findFirstToSend() {
		int startingPoint = Math.max(0, firstMessageToSend);
		firstMessageToSend = -1;
		RockBlockOutgoingMessage messOut;
		ListIterator<RockBlockOutgoingMessage> iterator = getListIterator(startingPoint);
		while (iterator.hasNext()) {
			messOut = iterator.next();
			if (!messOut.isMessageSent()) {
				firstMessageToSend = iterator.previousIndex();
				break;
			}
		}
	}
	
	/**
	 * Returns the index number of the first message that hasn't been sent yet.  If all
	 * messages have been sent, will return a -1
	 * 
	 * @return the index number of the first message that hasn't been sent yet, or a -1 if all messages have been sent
	 */
	public int getFirstMessageToSend() {
		findFirstToSend();
		return firstMessageToSend;
	}

	/**
	 * Count up the number of unsent messages in the outgoing queue
	 * @return
	 */
	public int countUnsent() {
		int numUnsent = 0;
		RockBlockOutgoingMessage messOut;
		ListIterator<RockBlockOutgoingMessage> iterator = getListIterator(0);
		while (iterator.hasNext()) {
			messOut = iterator.next();
			if (!messOut.isMessageSent()) {
				numUnsent++;
			}
		}
		return numUnsent;
	}
}
