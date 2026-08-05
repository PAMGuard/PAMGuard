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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import PamModel.PamModel;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PanelOverlayDraw;

/**
 * @author Doug Gillespie
 *         <p>
 *         Similar functionality to the Observable Class, but works with the
 *         PamObserver interface which has a bit more functionality.
 *         <p>
 *         e.g. The PamDataBlock, which is the most common class of
 *         PamObservable asks the observers what the first required sample is
 *         for each observing process or view.
 */
public class PamObservable {//extends PanelOverlayDraw {

	static private final long LARGEST_SAMPLE = Long.MAX_VALUE;


	/**
	 * List of PamObservers. 
	 * Sadly, this has had to turn into two lists to allow for instant observers, which 
	 * get notified first when data are added to a data block and observers which get called
	 * later. Direct access to the lists has been removed
	 */
	private List<PamObserver> pamObservers, instantObservers;

	private long cpuUsage[];
	private long lastCPUCheckTime = System.currentTimeMillis();
	private double cpuPercent[];

	protected boolean objectChanged = true;

	long totalCalls;

	private PamObserver longestObserver;

	/**
	 * Reference to a class that knows how to draw these things on any of the
	 * standard displays in the view, e.g. whistle contours, particular shapes
	 * on the map, etc.
	 */
	protected PanelOverlayDraw overlayDraw;

//	protected PamProfiler pamProfiler;

	/**
	 * Sample numbers are now passed around to all observers. 
	 */
	protected long masterClockSample = 0;

	private ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();

	public PamObservable() {
		pamObservers = new ArrayList<PamObserver>();
		instantObservers = new ArrayList<PamObserver>();
//		pamProfiler = PamProfiler.getInstance();
		cpuTimer.start();
	}

	/**
	 * Adds a PamObserver, which will then receive notifications when data is
	 * added. This is for single thread ops only 
	 * 
	 * @param o Reference to the observer
	 */
	public void addObserver(PamObserver o) {
		// check each observer only observes once.
		synchronized (pamObservers) {
			if (!pamObservers.contains(o)) {
				pamObservers.add(o);
				if (cpuUsage == null || cpuUsage.length < countObservers()) {
					cpuUsage = new long[countObservers()];
					cpuPercent = new double[countObservers()];
				}
			}
		}
	}

	/**
	 * Add an observer which will always get called before data are saved or 
	 * sent on to 'normal' processes. 
	 * @param o Observer
	 */
	public void addInstantObserver(PamObserver o) {
		// check each observer only observes once.
		synchronized (pamObservers) {
			if (!instantObservers.contains(o)) {
				instantObservers.add(o);
				if (cpuUsage == null || cpuUsage.length < countObservers()) {
					cpuUsage = new long[countObservers()];
					cpuPercent = new double[countObservers()];
				}
			}
		}
	}

	public void addObserver(PamObserver observer, boolean reThread) {
		//		reThread = false;
		if (!reThread) {
			addObserver(observer);
			return;
		}

		/*
		 * need to check more carefully for a multi threaded observer. 
		 * if findThreadedObserver returns non null, then the observable
		 * is already being observed in some class or other. 
		 * <p>
		 * If we get null back, then add the new observer. 
		 */
		if (findThreadedObserver(observer) == null) {
			addObserver(new ThreadedObserver(this, observer));
		}
	}

	/**
	 * Go through the observer list and check inside any that
	 * are wrapped in threaded observers, 
	 * @param o
	 * @return reference to threadedobserver. 
	 */
	public ThreadedObserver findThreadedObserver(PamObserver o) {
		PamObserver pamObserver;
		ThreadedObserver threadedObserver;
		synchronized (pamObservers) {
			for (int i = 0; i < pamObservers.size(); i++) {
				pamObserver = pamObservers.get(i);
				if (pamObserver.getClass() == ThreadedObserver.class) {
					threadedObserver = (ThreadedObserver) pamObserver;
					if (threadedObserver.getObserverObject() == o) {
						return threadedObserver;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Go through all the threaded observers and wait for them to finish processing any
	 * data remaining in their lists. 
	 * @param timeOutms timeout in milliseconds
	 * @return false if the timeout time was reached before all threaded observers had
	 * emptied their queues. true if all queues had been emptied successfully. 
	 */
	public boolean waitForThreadedObservers(long timeOutms) {
		long startTime = System.currentTimeMillis();
		PamObserver pamObserver;
		ThreadedObserver threadedObserver;

		int waitingUnits;
		while(true) {
			waitingUnits = 0;
			for (int i = 0; i < pamObservers.size(); i++) {
				pamObserver = pamObservers.get(i);
				if (pamObserver.getClass() == ThreadedObserver.class) {
					threadedObserver = (ThreadedObserver) pamObserver;
					waitingUnits += threadedObserver.getInterThreadListSize();
					if (!threadedObserver.isEmptyRead()) {
						waitingUnits++;
					}
				}
			}
			//			System.out.println("Waiting data units = " + waitingUnits);
			if (waitingUnits == 0) {
				// no units waiting, so OK to get out.
				return true;
			}
			if (System.currentTimeMillis() - startTime > timeOutms) {
				// have taken too long, so return that we've failed. 
				System.out.println("Wait timeout in threaded observer");
				// and clear everything that's left. 
				for (int i = 0; i < pamObservers.size(); i++) {
					pamObserver = pamObservers.get(i);
					if (pamObserver.getClass() == ThreadedObserver.class) {
						threadedObserver = (ThreadedObserver) pamObserver;
						threadedObserver.clearEverything();
					}
				}
				return false;
			}
			try {
				// wait a little and go check again to see if threaded observers have caught up.
				Thread.sleep(2);
			}
			catch (InterruptedException e) {

			}
		}
	}


	/**
	 * Removes an observer from the list of things that get notified from this datablock<p>
	 * This will mess up if it's ever called directly from within PamProcess since a local 
	 * record of the source datablock will remain in memory and not be cleared. Instead you should call 
	 * PamProcess.setParentDataBlock(null) which will guarantee the necessary bookkeeping is 
	 * carried out with PamProcess before calling this function.
	 * 
	 * @param o
	 *            Observer to remove
	 */
	synchronized public void deleteObserver(PamObserver o) {
		/*
		 * Double check all instances are removed
		 * Though in principle, it should be impossible to add more than one
		 * instance of any observer
		 */
//		synchronized (pamObservers) {
		boolean removed = false;
			while (removed = pamObservers.remove(o));
			PamObserver threadedObserver = findThreadedObserver(o);
			if (threadedObserver != null) {
				deleteObserver(threadedObserver);
				if (threadedObserver instanceof ThreadedObserver) {
					ThreadedObserver tObs = (ThreadedObserver) threadedObserver;
					tObs.terminateThread();
				}
			}
			while (removed = instantObservers.remove(o));
//			if (removed && o != null) {
//				String name = this.toString();
//				if (this instanceof PamDataBlock) {
//					name = ((PamDataBlock) this).getLongDataName();
//				}
//				System.out.printf("Datablock %s removed observer %s\n", this.toString(), o.getObserverName());
//			}
//		}
		//		o.removeObservable(this);
	}

	/**
	 * Removes all observers from the list
	 */
	public void deleteObservers() {
		synchronized (pamObservers) {
			pamObservers.clear();
			instantObservers.clear();
		}
	}

	/**
	 * @return Count of PamObservers subscribing to this observable including instant and rethreaded
	 */
	public int countObservers() {
		synchronized (pamObservers) {
			return pamObservers.size() + instantObservers.size();
		}
	}

	/**
	 * Get a pam observer at the given index.
	 * @param ind
	 * @return
	 */
	public PamObserver getPamObserver(int ind) {
//		synchronized (pamObservers) {
			if (ind < instantObservers.size()) {
				return instantObservers.get(ind);
			}
			else {
				return pamObservers.get(ind - instantObservers.size());
			}
//		}
	}

	/**
	 * Instruction to notify observers that data have changed, but not to send
	 * them any information about the new data.
	 */
	public void notifyObservers() {
		notifyObservers(null);
	}

	/**
	 * Set flag to say the data have changed
	 */
	public void setChanged() {
		objectChanged = true;
	}

	/**
	 * Set flag to say the data have changed
	 */
	public void clearchanged() {
		objectChanged = false;
	}

	/**
	 * Notify observers that data have changed and send them a reference to the
	 * new Data
	 * 
	 * Have tried to synchronise this, but it can cuase a lockup - not 100% sure what to do
	 * 
	 * 
	 * @param o
	 *            Reference to the new PamDataUnit
	 */
	public void notifyObservers(PamDataUnit o) {
//		synchronized (pamObservers) {
			notifyObservers(o, 0, countObservers());
//		}
	}

	/**
	 * Notify just the instant observers.
	 * @param o PamDataUnit that's been updated. 
	 */
	public void notifyInstantObservers(PamDataUnit o) {
//		synchronized (pamObservers) {
			notifyObservers(o, 0, instantObservers.size());
//		}
	}

	/**
	 * Notify the 'normal' observes, i.e. the ones which aren't 
	 * instant. 
	 * @param o PamDataUnit that's been updated. 
	 */
	public void notifyNornalObservers(PamDataUnit o) {
//		synchronized (pamObservers) {
			notifyObservers(o, instantObservers.size(), countObservers());
//		}
	}

	/**
	 * Notify a subset of observers that new data are added
	 * @param o Data unit that's been updated
	 * @param first index of first observer
	 * @param last index of last observer (loop up to last-1)
	 */
	private void notifyObservers(PamDataUnit o, int first, int last) {
		long threadId = Thread.currentThread().getId();
		for (int i = first; i < last; i++) {
			// PR : This needs to be portable and currently isnt.
			//     TODO: catch the java.lang.UnsatisfiedLinkError
			//     maybe even test if running on windows, 
			//     if dll is available, and 
			//     perhaps running in a diagnostic mode.  			
			//     

			long cpuStart = tmxb.getThreadCpuTime(threadId);
			getPamObserver(i).addData(this, o);
			long cpuEnd = tmxb.getThreadCpuTime(threadId);
			cpuUsage[i] += (cpuEnd - cpuStart);
		}
		clearchanged();
	}
	
	/**
	 * Notify all observes of an update. 
	 * @param dataUnit
	 */
	public void updateObservers(PamDataUnit dataUnit) {
		int n = countObservers();
		for (int i = 0; i < n; i++) {
			getPamObserver(i).updateData(this, dataUnit);
		}
	}

	/**
	 * See if an observer is in the instant list. 
	 * @param o Pam Observer
	 * @return true if it's in the instant list. 
	 */
	public boolean isInstantObserver(PamObserver o) {
		synchronized (pamObservers) {
			return instantObservers.contains(o);
		}
	}

	private Timer cpuTimer = new Timer(4321, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			long now = System.currentTimeMillis();
			if (cpuUsage == null) return;
			if (lastCPUCheckTime == now) return;
			for (int i = 0; i < cpuUsage.length; i++){
				cpuPercent[i] = (double) cpuUsage[i] / (now - lastCPUCheckTime) / 10000.;
				cpuUsage[i] = 0;
			}
			lastCPUCheckTime = now;
		}
	});
	
	
	/**
	 * Had some issues with the Timer holding a reference to the underlying PamDataBlock 
	 * (RoccaContourDataBlock, in this case) and not releasing it for garbage collection.
	 * Added in this method to force the timer to stop and release it's hold.
	 */
	public void stopTimer() {
		cpuTimer.stop();
	}

	public double getCPUPercent(int objectIndex) {
		if (objectIndex < 0 || objectIndex >= cpuPercent.length) return -1;
		return cpuPercent[objectIndex];
	}

	public double getCPUPercent(PamObserver pamObserver) {
		return getCPUPercent(pamObservers.indexOf(pamObserver));
	}

	/**
	 * Goes through all observers and works out which requires data 
	 * the longest.
	 * @return Required data storage time in milliseconds. 
	 */
	public long getRequiredHistory() {

		long longestTime = 0;
		long rt;

		longestObserver = null;

//		synchronized (pamObservers) {
			for (int i = 0; i < countObservers(); i++) {
				PamObserver obs = getPamObserver(i);
				if ((rt = obs.getRequiredDataHistory(this, null)) > 0) {
					if (rt > longestTime) {
						longestObserver = obs;
					}
					longestTime = Math.max(longestTime, rt);
				}
			}
//		}
		totalCalls++;
		return longestTime;
	}

	public PamObserver getLongestObserver() {
		return longestObserver;
	}

	/**
	 * @param overlayDraw
	 *            Instance of a concrete class implementing the PanelIverlayDraw
	 *            interface.
	 *            <p>
	 *            Called to set the class which can draw overlays of the
	 *            PamDataUnits held in this data block.
	 */
	public void setOverlayDraw(PanelOverlayDraw overlayDraw) {
		this.overlayDraw = overlayDraw;
	}

	/**
	 * @param g
	 *            An awt Graphics object
	 *            <p>
	 * @param projectorInfo
	 *            Class implementing GeneralProjector which can convert data to
	 *            screen coordinates.
	 *            <p>
	 * @return a rectangle containing the pixels which will need to be
	 *         invalidated or redrawn by the calling process
	 *         <p>
	 *         Any PamObserver which has been notified by a PamObservable that a
	 *         new PamDatUnit has been created can call this function to draw
	 *         some arbitrary shape on the image, e.g. a whistle contour, a
	 *         picture of a whale, etc.
	 *         <p>
	 *         This should be overridden in the concrete class if it is to do
	 *         anything.
	 */
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector projectorInfo) {
		if (overlayDraw != null) {
			return overlayDraw.drawDataUnit(g, pamDataUnit, projectorInfo);
		}
		return null;
	}

	/**
	 * 
	 * @param generalProjector
	 * @return true if there is a projector and the data can draw on it. 
	 */
	public boolean canDraw(GeneralProjector generalProjector) {
		if (overlayDraw != null) {
			return overlayDraw.canDraw(generalProjector);
		}
		else {
			return false;
		}
	}

	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (overlayDraw != null) {
			return overlayDraw.canDraw(parameterTypes, parameterUnits);
		}
		return false;
	}

	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		if (overlayDraw != null) {
			return overlayDraw.getHoverText(generalProjector, dataUnit, iSide);
		}
		return null;
	}
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,int keyType) {
		if (overlayDraw != null) {
			return overlayDraw.createKeyItem(generalProjector, keyType);
		}
		return null;
	}

	/**
	 * @return the overlayDraw
	 */
	public PanelOverlayDraw getOverlayDraw() {
		return overlayDraw;
	}

	/**
	 * Get the maximum jitter allowable on output of an observable. 
	 * This used to be fixed at a single global value, but have made 
	 * it more configurable so I can increase it for certain data blocks which 
	 * have a very low data rate, but may take a lot of processing. 
	 * @return Maximum queue length in milliseconds. 
	 */
	public int getMaxThreadJitter() {
		return PamModel.getPamModel().getPamModelSettings().getThreadingJitterMillis();
	}

	/**
	 * @return the pamObservers
	 */
	protected List<PamObserver> getPamObservers() {
		return pamObservers;
	}

	/**
	 * @return the instantObservers
	 */
	protected List<PamObserver> getInstantObservers() {
		return instantObservers;
	}

	//	@Override
	//	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
	//		if (overlayDraw != null) {
	//			return overlayDraw.hasOptionsDialog(generalProjector);
	//		}
	//		return false;
	//	}
	//
	//	@Override
	//	public boolean showOptions(Window parentWindow,
	//			GeneralProjector generalProjector) {
	//		if (overlayDraw != null) {
	//			return overlayDraw.showOptions(parentWindow, generalProjector);
	//		}
	//		return false;
	//	}
}
