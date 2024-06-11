package PamguardMVC;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import PamController.PamController;
import PamModel.PamModel;
import PamUtils.PamCalendar;
import PamguardMVC.debug.Debug;

/**
 * This is a decorator class for PamObservers which intercepts any
 * data being sent to that observer and puts the data into a list
 * which will then be re-read in a separate thread, rather than
 * sending if for direct execution.
 * <p>
 * There is a bit of jiggledy piggledy to get the data history times
 * right since they may need to be extended slightly to allow for 
 * data that are not yet used. 
 *  
 * @author Doug Gillespie
 *
 */
public class ThreadedObserver implements PamObserver {

	private PamObserver singleThreadObserver;
	private NewObserverThread newObserverThread;
	private Thread observerThread;
	private volatile boolean killThread = false;
	private volatile boolean emptyRead = true;
	private long maxJitter; 
	private int jitterSleep;
	private boolean stopFlag;


	private Object synchLock = new Object();
	
	private List<ObservedObject> toDoList;

	/**
	 * Make an intermediate observer of a PamObservable (i.e. datablock)
	 * which will queue the data for analysis in a different thread. 
	 * @param pamObservable
	 * @param singleThreadObserver
	 */
	public ThreadedObserver(PamObservable pamObservable,
			PamObserver singleThreadObserver) {
		super();
		this.singleThreadObserver = singleThreadObserver;
		maxJitter = pamObservable.getMaxThreadJitter();
		jitterSleep = getPreferredJitter(maxJitter);
		toDoList = Collections.synchronizedList(new LinkedList<ObservedObject>());
		String threadName = "Threaded Observer for " + singleThreadObserver.getClass().getName();
		try {
			threadName = singleThreadObserver.getObserverName() + " ThreadedObserver";
		}
		catch (Exception e) {
			/*
			 *  had trouble with some modules not being fully initialised, and throwing a null
			 *  pointer exception when the above got called.   
			 */
		}
		observerThread = new Thread(newObserverThread = new NewObserverThread(), threadName);
		observerThread.setPriority(Thread.MAX_PRIORITY);
		observerThread.start();
	}
	
	/**
	 * This is the sleep time NOT the jitter. Should rename this. 
	 * If it's a real time operations, set to about 10millis. 
	 * Otherwise, set it to 2ms for faster offline file analysis. 
	 * @return sleep time when buffers are full. 
	 */
	private int getPreferredJitter(long maxJitter) {
		int defJitter = 10;
		try {
			AcquisitionControl daqCtrl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
			if (daqCtrl != null) {
				DaqSystem daqSystem = daqCtrl.findDaqSystem(null);
				if (daqSystem != null && daqSystem.isRealTime() == false) {
					defJitter = 2;
				}
			}
		}
		catch (Exception e) {
		}
		return defJitter;
	}

	@Override
	public PamObserver getObserverObject() {
		return singleThreadObserver;
	}

	@Override
	public String getObserverName() {
		if (canMultiThread()) {
			return singleThreadObserver.getObserverName() + " (MT)";
		}
		else {
			return singleThreadObserver.getObserverName();
		}
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		/**
		 * if there is nothing in the list, then the observing thread
		 * is up to date, and can use the normal history time.
		 * <p>
		 * If there is data in the buffer, then the history has to
		 * go back by an amount equivalent to the delay from the
		 * first data to now. 
		 */
		long h = singleThreadObserver.getRequiredDataHistory(o, arg);
		synchronized (synchLock) { 
			if (toDoList.size() > 0) {
				long firstTime = 0;
				try {
					firstTime = toDoList.get(0).getTimeMillis();
					long now = PamCalendar.getTimeInMillis();
					h += (now - firstTime);
				} catch (IndexOutOfBoundsException e) {
					// if the list has already emptied out, the toDoList.get(0) above will throw an
					// IndexOutOfBoundsException.  If that's the case, just move along because there's
					// no delay to add anymore
				}
			}
		}
		h += PamModel.getPamModel().getPamModelSettings().getThreadingJitterMillis()*2;

		return h;
	}

	/**
	 * Get the number of objects in the queue. 
	 * @return number of objests
	 */
	public int getInterThreadListSize() {
		return toDoList.size();
	}

	@Override
	public void noteNewSettings() {
		if (canMultiThread()) {
			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.NOTENEWSETTINGS, null, null);
			addToList(newObject);
		}
		else {
			singleThreadObserver.noteNewSettings();
		}
	}
	
	/**
	 * Terminate the threaded observer by setting the
	 * flag that causes the observing loop to drop out. 
	 */
	public void terminateThread() {
		killThread = true;
	}

	@Override
	public void removeObservable(PamObservable o) {
		killThread = true;
		singleThreadObserver.removeObservable(o);
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		if (canMultiThread()) {
			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.SETSAMPLERATE, sampleRate, notify);
			addToList(newObject);
		}
		else {
			singleThreadObserver.setSampleRate(sampleRate, notify);
		}
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		if (canMultiThread()) {
			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.MASTERCLOCKUPDATE, milliSeconds, sampleNumber);
			addToList(newObject);
		}
		else {
			singleThreadObserver.masterClockUpdate(milliSeconds, sampleNumber);
		}
	}

	@Override
	public void addData(PamObservable o, PamDataUnit newDataUnit) {
		if (canMultiThread()) {
			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.ADDDATA, o, newDataUnit);
			addToList(newObject);
		}
		else {
			singleThreadObserver.addData(o, newDataUnit);
		}

	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
//		if (canMultiThread()) {
//			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.ADDDATA, observable, pamDataUnit);
//			addToList(newObject);
//		}
//		else {
		/**
		 * This really doesn't like going through the queue with old new data, possibly because when 
		 * data units get added to a superdetection, they get an update called back into their datablock, so 
		 * will re-enter the queue. this seems to set a quite late time on the queue, so it blocks the input. 
		 * Might need to do something a bit mor eclever with the times in the queued objests. 
		 */
			singleThreadObserver.updateData(observable, pamDataUnit);
//		}
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		if (canMultiThread()) {
			ObservedObject newObject = new ObservedObject(PamCalendar.getTimeInMillis(), ObservedObject.SOURCENOTIFICATION, type, object);
			addToList(newObject);
			if (type==AcquisitionProcess.LASTDATA) {
//				Debug.out.println("Thread " + getObserverName() +  "-" + System.identityHashCode(this) +
//						"(" + Thread.currentThread().getName() + 
//						"-" + Thread.currentThread().getId() +
//						") just received source notification to stop");
				stopFlag = true;
			}
			else if (type==AcquisitionProcess.FIRSTDATA) {
//				Debug.out.println("Thread " + getObserverName() + "-" + System.identityHashCode(this) +
//						"(" + Thread.currentThread().getName() + 
//						"-" + Thread.currentThread().getId() +
//						") just received source notification to start");
				stopFlag = false;
			}
		}
		else {
			singleThreadObserver.receiveSourceNotification(type, object);
		}
	}	

	/**
	 * Not possible to multi thread when data come from a sound file,
	 * so don't try. Can leave the objects wrapped up in the decorator
	 * but just pass the data on, rather than multi threading it. 
	 * @return
	 */
	private boolean canMultiThread() {
		return true;
	}
	
	/**
	 * Add the ObservedObject to the list for processing once the queue
	 * is empty enough to accept new data
	 * 
	 * @param theObject
	 */
	private void addToList(ObservedObject theObject) {
		waitForQueueToBeReady(theObject);
		
		/**
		 * Add the data into the list. 
		 */
		synchronized (synchLock) {
			toDoList.add(theObject);
		}	
	}
	
	/**
	 * Get the millisecond times of the first and last items in the queue. 
	 * @return first and last times, or null if the queue is empty. 
	 */
	public long[] getQueueLimits() {
		synchronized (synchLock) {
			if (toDoList.size() == 0) {
				return null;				
			}
			long[] limits = new long[2];
			limits[0] = toDoList.get(0).getTimeMillis();
			limits[1] = toDoList.get(toDoList.size()-1).getTimeMillis();
			return limits;
		}
	}
	
	/**
	 * Check to see if the queue is ready to accept a new Object.  If the queue is
	 * currently full, then wait <strong>inside</strong> this method until the queue has emptied out.<p>Note
	 * that the object <strong>will</strong> be added to the queue when this method returns, so all delays
	 * must be done in here.
	 * 
	 * @param theObject The Object waiting to be added to the queue
	 */
	protected void waitForQueueToBeReady(ObservedObject theObject) {
		boolean needSleep = true;
		int nSleeps = 0;
		ObservedObject oldestObject = null, newestObject = null;
		while(needSleep) {
			synchronized (synchLock) {
				int sz = toDoList.size();
				if (sz > 0) {
					long dt=0;
					try {
						oldestObject = toDoList.get(0);
						newestObject = toDoList.get(toDoList.size()-1);
						dt = theObject.getTimeMillis() - toDoList.get(0).getTimeMillis();
					} catch (IndexOutOfBoundsException e) {
						// if the list has already emptied out, the toDoList.get(0) above will throw an
						// IndexOutOfBoundsException.  If that's the case, just move along
						// because there's no need to sleep
					}
					if (++nSleeps > 100) {
//						System.out.printf("Bailing on sleep at %s for %d %s due to objects %s at %s to %s at %s\n", 
//								PamCalendar.formatDBDateTime(theObject.getTimeMillis()), theObject.getAction(), theObject.getObserved()[0],
//								oldestObject.getObserved()[0], PamCalendar.formatDBDateTime(oldestObject.getTimeMillis()),
//								newestObject.getObserved()[0], PamCalendar.formatDBDateTime(newestObject.getTimeMillis()));
						needSleep = false;
					}
					if (dt < maxJitter) {
						needSleep = false;
					}
				}
				else {
					needSleep = false;
				}
			}
			if (needSleep) { // sleep is outside the synchronized section 
				sleepWriter();
			}
		}
	}
	
	/**
	 * Wait for the queue to be empty. This REALLY must not get called from the NewObserverThread
	 * or you'll get a thread lock, so don't
	 * @param timeout maximum time to wait for thread. 
	 * @return +ve integer number of millis if it ended normally, negative for an error code.  
	 * -1 for a timeout or -2 for it calling within it's own observer thread. 
	 */
	public long waitToEmpty(long timeout) {
		if (Thread.currentThread() == observerThread) {
			return -2;
		}
		long start = System.currentTimeMillis();
		int n = 0;
		do {
			synchronized (synchLock) {
				n = toDoList.size();
			}
			if (n == 0) {
				return System.currentTimeMillis() - start;
			}
			else {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (System.currentTimeMillis() <= start + timeout);
		return -1;
	}
	
	/**
	 * Sleep the writing thread by jittersleep (default 10ms)
	 */
	public void sleepWriter() {
		try {
			Thread.sleep(jitterSleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @return
	 */
	public boolean isEmptyRead() {
		return emptyRead;
	}
	
	
	/**
	 * This is the maximum amount of data in milliseconds that can sit in the
	 * queue. Normally it's about a second.
	 * @return the maxJitter
	 */
	public long getMaxJitter() {
		return maxJitter;
	}

	/**
	 * @param maxJitter the maxJitter to set
	 */
	public void setMaxJitter(long maxJitter) {
		this.maxJitter = maxJitter;
	}


	/**
	 * New observer thread. 
	 * <p>
	 * Very simply, the observable had put the new action into a list
	 * and this thread takes that action back out of the list and passes
	 * it on to the real observer. 
	 * 
	 * @author Doug
	 *
	 */
	class NewObserverThread implements Runnable {

		@Override
		public void run() {
			boolean alreadyDisplayed = false;
			
			while (!killThread) {

				// if there's nothing in the queue, wait
				if (toDoList.isEmpty()) {
					emptyRead = true;
					
					if (stopFlag && !alreadyDisplayed) {
//						Debug.out.println("Thread " + getObserverName() +  "-" + System.identityHashCode(this) +
//								"(" + Thread.currentThread().getName() + 
//								"-" + Thread.currentThread().getId() +
//								") has emptied its buffer");
						alreadyDisplayed = true;
					}
					
					try {
						Thread.sleep(5);
					}
					catch(InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				
				// otherwise, process the first in the list
				else {
					emptyRead = false;
					int lc=0;
					ObservedObject observedObject;
					while (!toDoList.isEmpty()) {

//						if (stopFlag) {
//							if (++lc%250==0) {	//
//								Debug.out.println("Thread " + getObserverName() +  "-" + System.identityHashCode(this) +
//										"(" + Thread.currentThread().getName() + 
//										"-" + Thread.currentThread().getId() +
//										") still processing data");
//								lc=0;
//							}
//						}
						

						// get the first object, send it for processing and then remove from the list
						synchronized(synchLock) {
							if (toDoList.size() > 0) { 
								observedObject = toDoList.remove(0);
							}
							else {
								break;
							}
						}
						// need to do this bit outside of the synch block. 
						performAction(observedObject);
//						synchronized(synchLock) {
//							if (toDoList.size() > 0) { // list may have been cleared during a shut down. 
//								toDoList.remove(0);
//							}
//						}
					}
				}
			}			
//			Debug.out.println("End of runnable in ThreadedObserver " + singleThreadObserver.getObserverName());
		}
		
		/**
		 * Perform the action specified by the ObservedObject
		 * 
		 * @param observedObject
		 */
		private void performAction(ObservedObject observedObject) {
			int action = observedObject.getAction();

			switch (action) {

			// Case 0: add dataunit
			case ObservedObject.ADDDATA:
				singleThreadObserver.addData(
						(PamObservable) observedObject.getObserved()[0],
						(PamDataUnit) observedObject.getObserved()[1]);
				break;

			// Case 1: update dataunit
			case ObservedObject.UPDATEDATA:
				singleThreadObserver.updateData(
						(PamObservable) observedObject.getObserved()[0],
						(PamDataUnit) observedObject.getObserved()[1]);
				break;

			// Case 2: note new settings
			case ObservedObject.NOTENEWSETTINGS:
				singleThreadObserver.noteNewSettings();
				break;

			// Case 3: set sample rate
			case ObservedObject.SETSAMPLERATE:
				singleThreadObserver.setSampleRate(
						(float) observedObject.getObserved()[0],
						(boolean) observedObject.getObserved()[1]);
				break;

			// Case 4: master clock update
			case ObservedObject.MASTERCLOCKUPDATE:
				singleThreadObserver.masterClockUpdate(
						(long) observedObject.getObserved()[0],
						(long) observedObject.getObserved()[1]);
				break;
				
			// Case 5: notification from the data source
			case ObservedObject.SOURCENOTIFICATION:
				singleThreadObserver.receiveSourceNotification(
						(int) observedObject.getObserved()[0],
						observedObject.getObserved()[1]);
				break;

			default:
				throw new IllegalArgumentException("Unexpected value: " + action);
			}
		}
	}


	public void clearEverything() {
		synchronized (synchLock) { 
			System.out.printf("Clearing %d objects from todo list in %s\n", toDoList.size(), singleThreadObserver.getObserverName());
			toDoList.clear();
		}		
	}

	public void dumpBufferStatus(String message, boolean sayEmpties) {
		int n = toDoList.size();
		if (sayEmpties == false && n == 0) {
			return;
		}
		String name = singleThreadObserver.getObserverName();
		System.out.printf("Threaded observer %s has %d objects in queue\n", name, n);
	}

}
