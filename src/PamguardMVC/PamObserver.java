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
package PamguardMVC;

/**
 * @author Doug Gillespie
 *         <p>
 *         PamObserver is similar to the Java Observer interface but with
 *         additional functionality to that the Observed class (generally a
 *         PamDataBlock) can query the PamObserver as to which of the
 *         PamDataUnits may be deleted.
 * 
 */
public interface PamObserver {


	/**
	 * @param o
	 *            PamObservable class requiring the data
	 * @param arg
	 *            optional argument from PamObservable
	 * @return time in milliseconds required by data held in PamObservable
	 */
	long getRequiredDataHistory(PamObservable observable, Object arg);

	/**
	 * Informs the PamObserver that new data have been added to the Observable
	 * class
	 * 
	 * @param o  Reference to the Observable (a PamDataBlock)
	 * @param arg Reference to the new PamDataUnit
	 */
	void addData(PamObservable observable, PamDataUnit pamDataUnit);
	
	/**
	 * Informs the PamObserver that existing data have been updated
	 * 
	 * @param o  Reference to the Observable (a PamDataBlock)
	 * @param arg Reference to the new PamDataUnit
	 */
	 void updateData(PamObservable observable, PamDataUnit pamDataUnit);

	/**
	 * called when an Observable (PamDataBlock) is removed from the system
	 *
	 */ 
	void removeObservable(PamObservable observable);

	/**
	 * New sample rate
	 * 
	 * @param sampleRate
	 * @param notify
	 *            Notify other PamObservers and PamObservables in the chain.
	 */
	void setSampleRate(float sampleRate, boolean notify);

	public void noteNewSettings();

	public String getObserverName();

	public void masterClockUpdate(long milliSeconds, long sampleNumber);

	/**
	 * 
	 * @return the actual observer. In most cases concrete
	 * classes will just return 'this' in response. The exception
	 * is the Threaded observer, which will return the single thread
	 * observer.  
	 */
	public PamObserver getObserverObject();
	
	/**
	 * Receive a notification from the data source - typically a change in DAQ status.  See the
	 * constants listed in AcquisitionProcess for potential change types.
	 * 
	 * @param type the type of change
	 * @param object generic object added here so that we can include anything in the future
	 */
	public void receiveSourceNotification(int type, Object object);

}
