package PamguardMVC.dataOffline;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.PamRawDataBlock;

/**
 * Handles the loading of offline data from a PamDataBlock. 
 * @author Jamie Macaulay 
 *
 */
public class OfflineDataLoading<T extends PamDataUnit> {

	/**
	 * The current thread will be cancelled. 
	 */
	public static final int OFFLINE_DATA_INTERRUPT = 0x1;

	/**
	 * The current thread will continu and the next thread will be executed. 
	 */
	public static final int OFFLINE_DATA_WAIT = 0x2;

	/**
	 * If the current thread is not null then the new order will not be executed. 
	 */
	public static final int OFFLINE_DATA_CANCEL = 0x4;

	/**
	 * Data loading results 
	 */
	/**
	 * No data available for offline loading. 
	 */
	static public final int REQUEST_NO_DATA = 0x1;
	/**
	 * Data loaded for requested time period. 
	 */
	static public final int REQUEST_DATA_LOADED = 0x2;
	/**
	 * Data partially loaded for requested time period
	 */
	static public final int REQUEST_DATA_PARTIAL_LOAD = 0x4;
	/**
	 * this is exactly the same data as requested last time. 
	 * <p>
	 * This flag will be used with one of the other three. 
	 */
	static public final int REQUEST_SAME_REQUEST = 0x8;
	/**
	 * The request was interrupted (in multi thread load)
	 */
	static public final int REQUEST_INTERRUPTED = 0x10;
	/**
	 * The request threw an exception of some sort. 
	 */
	static public final int REQUEST_EXCEPTION = 0x20;

	/**
	 * Reference to the datablock
	 */
	private PamDataBlock<T> pamDataBlock;


	private boolean currentOfflineLoadKeep = true;


	/**
	 * Used in offine analysis when data arebeing reloaded. 
	 * this list gets used to distribute data beingloaded 
	 * from upstream processes. 
	 */
	private Vector<PamObserver> requestingObservers;

	/**
	 * Orders placed with OFFLINE_DATA_WAIT while their owner already had an order
	 * running. Guarded by orderLock. 
	 */
	private final ArrayList<OfflineDataLoadInfo> waitingDataLoads = new ArrayList<OfflineDataLoadInfo>(); 

	/**
	 * Orders with restart priority which were cancelled by someone other than their
	 * owner and should be loaded again. Guarded by orderLock. 
	 */
	private final ArrayList<OfflineDataLoadInfo> restartDataLoads = new ArrayList<OfflineDataLoadInfo>(); 

	/**
	 * Number of calls to getOfflineData currently running in this block, i.e. whether
	 * this block is part of a load in progress, including loads ordered further downstream. 
	 */
	private final AtomicInteger loadDepth = new AtomicInteger(); 

	/**
	 * Constructor for the offline data loader. 
	 * @param pamDataBlock - the datablock. 
	 */
	public OfflineDataLoading(PamDataBlock<T> pamDataBlock){
		this.pamDataBlock=pamDataBlock; 
	}



	/**
	 * Similar functionality to getOfflineData, but this will launch a separate worker thread to 
	 * do the actual work getting the data. The worker thread will call getOfflineData. <p>
	 * getOfflineData will probably (if not overridden) be sending data to the update member function of the
	 * observer, but from the worker thread. Once it's complete, it will send a message to say that data are
	 * loaded. <p>
	 * It's possible that the user will do something which causes this to be called again before the previous
	 * thread completed execution, in which case there are three options:
	 * <p>OFFLINE_DATA_INTERRUPT - previous thread will be terminated
	 * <p>OFFLINE_DATA_WAIT - wait for previous thread to terminate, then start this load
	 * <p>OFFLINE_DATA_CANCEL - give up and return
	 * @param dataObserver observer of the loaded data
	 * @param loadObserver observer to get status information on the load. 
	 * @param startMillis data start time in milliseconds
	 * @param endMillis data end time in milliseconds. 
	 * @param loadKeepLayers Number of layers of datablock which should hang on to loaded data rather than delete it immediately. 
	 * @param interrupt interrupt options. 
	 * @param allowRepeats allow repeated loads of exactly the same data. 
	 * 
	 */
	public void orderOfflineData(PamObserver dataObserver, LoadObserver loadObserver,
			long startMillis, long endMillis, int loadKeepLayers, int interrupt, boolean allowRepeats) {

		//build the data info from the function inputs
		OfflineDataLoadInfo offlineDataInfo=new OfflineDataLoadInfo(dataObserver, loadObserver, 
				startMillis, endMillis, loadKeepLayers, interrupt, allowRepeats); 
		//order the data,. 
		this.orderOfflineData(offlineDataInfo);

	}


	/**
	 * Clears the raw and FFT data blocks which are not currently being loaded, prior to
	 * a new load. Blocks which have an order of their own running, or which are part of
	 * the upstream chain of a load in progress, are left alone so that one display's
	 * load does not wipe data another display is in the middle of loading.
	 */
	private void clearIdleFFTBlocks() {
		PamController pamController = PamController.getInstance();
		if (pamController == null) {
			return;
		}
		clearIdleBlocks(pamController.getFFTDataBlocks());
		clearIdleBlocks(pamController.getRawDataBlocks());
	}

	private void clearIdleBlocks(ArrayList<PamDataBlock> datablocks) {
		if (datablocks == null) {
			return;
		}
		for (int i=0; i<datablocks.size(); i++) {
			PamDataBlock db = datablocks.get(i);
			OfflineDataLoading dl = db.getOfflineDataLoading();
			if (dl != null && !dl.isIdle()) {
				continue;
			}
			db.clearAll();
			db.clearDeletedList();
		}
	}

	/**
	 * @return true if this data block has no orders running and is not part of the chain
	 * of a load in progress.
	 */
	public boolean isIdle() {
		if (loadDepth.get() > 0) {
			return false;
		}
		synchronized (orderLock) {
			return activeOrders.isEmpty();
		}
	}


	/**
	 * Similar functionality to getOfflineData, but this will launch a separate worker thread to 
	 * do the actual work getting the data. The worker thread will call getOfflineData. <p>
	 * getOfflineData will probably (if not overridden) be sending data to the update member function of the
	 * observer, but from the worker thread. Once it's complete, it will send a message to say that data are
	 * loaded. <p>
	 * Each order belongs to an owner ({@link OfflineDataLoadInfo#getOrderOwner()}, by default the
	 * observer the data are for). Orders from different owners are independent: they never cancel
	 * each other and are run one after another where they meet at a common upstream source. 
	 * If the same owner orders again before its previous order has completed, there are three options:
	 * <p>OFFLINE_DATA_INTERRUPT - the owner's previous order will be cancelled
	 * <p>OFFLINE_DATA_WAIT - wait for the owner's previous order to finish, then start this load
	 * <p>OFFLINE_DATA_CANCEL - give up and return

	 * @param offlineDataInfo the OfflineDataLoadInfo object which stores all parameters for the data load. 

	 */
	public void orderOfflineData(OfflineDataLoadInfo offlineDataInfo) {
		synchronized (orderLock) {
			Object owner = offlineDataInfo.getOrderOwner();
			OrderData current = activeOrders.get(owner);
			if (current != null && !current.isDone()) {
				switch (offlineDataInfo.getInterrupt()) {
				case OFFLINE_DATA_INTERRUPT:
					/*
					 * Don't wait for the old order to stop. It will see its cancel flag and
					 * stop, and the new order can't overlap it in this block since 
					 * getOfflineData is synchronized. 
					 */
					current.cancelOrder(false);
					removeQueued(waitingDataLoads, owner);
					removeQueued(restartDataLoads, owner);
					break;
				case OFFLINE_DATA_CANCEL:
					return;
				case OFFLINE_DATA_WAIT:
					// only the newest waiting order from an owner is worth keeping. 
					removeQueued(waitingDataLoads, owner);
					waitingDataLoads.add(offlineDataInfo);
					return;
				}
			}
			startOrder(offlineDataInfo);
		}
	}

	/**
	 * Start a worker for an order. Must be called holding orderLock. 
	 * @param offlineDataInfo the order. 
	 */
	private void startOrder(OfflineDataLoadInfo offlineDataInfo) {
		try {
			OrderData orderData = new OrderData(offlineDataInfo);
			activeOrders.put(orderData.owner, orderData);
			orderData.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all orders from an owner from a queue. 
	 */
	private void removeQueued(List<OfflineDataLoadInfo> queue, Object owner) {
		for (int i = queue.size()-1; i >= 0; i--) {
			if (queue.get(i).getOrderOwner() == owner) {
				queue.remove(i);
			}
		}
	}


	/**
	 * Cancels all current orders on this data block, whoever placed them. Orders with
	 * priority {@link OfflineDataLoadInfo#PRIORITY_CANCEL_RESTART} will be restarted
	 * once a later order completes. 
	 * <p>Consumers cancelling their own order should use {@link #cancelDataOrder(Object)}
	 * so that they don't cancel loads placed by other displays. 
	 */
	public void cancelDataOrder() {
		cancelDataOrder(false);
	}


	/**
	 * Cancels all current orders on this data block, whoever placed them. 
	 * @param que true to also clear all queued orders. 
	 */
	public void cancelDataOrder(boolean que) {
		synchronized (orderLock) {
			for (OrderData orderData : new ArrayList<OrderData>(activeOrders.values())) {
				orderData.cancelOrder(true);
			}
			if (que) {
				waitingDataLoads.clear(); 
				restartDataLoads.clear();
			}
		}
	}

	/**
	 * Cancel the current order from one owner, leaving orders from other owners running. 
	 * @param owner the owner of the order (normally the observer which ordered the data,
	 * see {@link OfflineDataLoadInfo#getOrderOwner()}). 
	 */
	public void cancelDataOrder(Object owner) {
		cancelDataOrder(owner, false);
	}

	/**
	 * Cancel the current order from one owner, leaving orders from other owners running. 
	 * @param owner the owner of the order (normally the observer which ordered the data,
	 * see {@link OfflineDataLoadInfo#getOrderOwner()}). 
	 * @param que true to also remove that owner's queued orders. 
	 */
	public void cancelDataOrder(Object owner, boolean que) {
		synchronized (orderLock) {
			OrderData orderData = activeOrders.get(owner);
			if (orderData != null) {
				orderData.cancelOrder(false);
			}
			if (que) {
				removeQueued(waitingDataLoads, owner);
				removeQueued(restartDataLoads, owner);
			}
		}
	}

	/**
	 * 
	 * @return true if an order for data is currently still being processed. 
	 */
	public boolean getOrderStatus() {
		synchronized (orderLock) {
			return !activeOrders.isEmpty();
		}
	}

	/**
	 * Current orders, one per owner. Guarded by orderLock. 
	 */
	private final Map<Object, OrderData> activeOrders = new IdentityHashMap<Object, OrderData>();

	public Object orderLock = new Object();


	class OrderData extends SwingWorker<Integer, Integer> {

		/**
		 * The current offline data info 
		 */
		OfflineDataLoadInfo offlineDataInfo;

		/**
		 * The owner of the order, fixed when the order is placed. 
		 */
		final Object owner;

		/**
		 * True if the order should be re-queued if it has restart priority and 
		 * gets cancelled. 
		 */
		private volatile boolean restartIfCancelled = false;

		public OfflineDataLoadInfo getOfflineDataInfo() {
			return offlineDataInfo;
		}

		/**
		 * 
		 */
		String dateStr;

		OrderData(OfflineDataLoadInfo offlineDataInfo) {
			this.offlineDataInfo=offlineDataInfo; 
			this.owner = offlineDataInfo.getOrderOwner();
			dateStr = String.format(" %s to %s", 
					PamCalendar.formatDateTime(offlineDataInfo.getStartMillis()), PamCalendar.formatDateTime(offlineDataInfo.getEndMillis()));
		}

		/**
		 * Cancel the order. 
		 * @param restart true if the order may be restarted later if it has
		 * restart priority (i.e. it was cancelled by someone other than its owner). 
		 * @return true if the worker was cancelled. 
		 */
		public boolean cancelOrder(boolean restart) {
			restartIfCancelled = restart;
			offlineDataInfo.cancel = true;
			return cancel(false); 
			/*
			 really bad idea to call these thread cancellation functions since it's guaranteed to leave objects and files in a
			 very indeterminate state. Find a better way of stopping the load thread. That's what all the cancellationobject was about. 
			 i..e telling load threads to shut down gracefully rather than terminating them in an uncontrlled manner. Get rid of this and
			 any other thread interrupts of the code will become totally unstable. . 
			 */
		}

		@Override
		protected Integer doInBackground()  {
			try {
				clearIdleFFTBlocks();
				if (offlineDataInfo.cancel) {
					return REQUEST_INTERRUPTED;
				}
				return getOfflineData(offlineDataInfo);
			}
			catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		

		@Override
		protected void done() {
			synchronized (orderLock) {
				if (activeOrders.get(owner) == this) {
					activeOrders.remove(owner);
				}
			}
			Integer status = 0;
			try {
				if (isCancelled()) {
					status = REQUEST_INTERRUPTED;
					//check whether the cancelled order should be saved for later loading. 
					if (restartIfCancelled) {
						checkDataReloadQue(offlineDataInfo);
					}
				}
				else {
					status = get();
				}
			} catch (InterruptedException e1) {
				status = REQUEST_INTERRUPTED;
				System.out.println(pamDataBlock.getDataName() + " Order done - REQUEST_INTERRUPTED " + dateStr);
			} catch (ExecutionException e1) {
				status = REQUEST_EXCEPTION;
				System.out.println(pamDataBlock.getDataName() + " Order done - ExecutionException " + dateStr);
			} catch (CancellationException e) {
				status = REQUEST_INTERRUPTED;
				System.out.println(pamDataBlock.getDataName() + " Order done - CancellationException " + dateStr);
			}
			if (offlineDataInfo.getLoadObserver() != null) {
				offlineDataInfo.getLoadObserver().setLoadStatus(status);
			}

			launchQuedReloadThreads(!isCancelled());
		}

		@Override
		protected void process(List<Integer> chunks) {
			super.process(chunks);
		}

	}

	/**
	 * Start queued orders whose owner no longer has an order running. 
	 * @param includeRestarts true to also restart orders which were cancelled
	 * with restart priority. These only restart after an order completes 
	 * normally, so that cancelling everything doesn't immediately restart them. 
	 */
	private void launchQuedReloadThreads(boolean includeRestarts) {
		synchronized (orderLock) {
			launchQueued(waitingDataLoads);
			if (includeRestarts) {
				launchQueued(restartDataLoads);
			}
		}
	}

	private void launchQueued(List<OfflineDataLoadInfo> queue) {
		for (int i = 0; i < queue.size(); i++) {
			OfflineDataLoadInfo info = queue.get(i);
			if (activeOrders.containsKey(info.getOrderOwner())) {
				continue;
			}
			queue.remove(i--);
			info.cancel = false;
			startOrder(info);
		}
	}

	/**
	 * Check whether a cancelled order should be restarted later and if so
	 * add it to the restart queue, replacing any earlier one from the same owner. 
	 * @param offlineDataInfo the cancelled order. 
	 */
	private void checkDataReloadQue(OfflineDataLoadInfo offlineDataInfo){
		if (offlineDataInfo.getPriority()==OfflineDataLoadInfo.PRIORITY_CANCEL_RESTART){
			synchronized (orderLock) {
				Object owner = offlineDataInfo.getOrderOwner();
				// a newer order from the same owner supersedes this one. 
				if (activeOrders.containsKey(owner)) {
					return;
				}
				removeQueued(waitingDataLoads, owner);
				removeQueued(restartDataLoads, owner);
				restartDataLoads.add(offlineDataInfo);
			}
		}
	}
	private int lastRequestAnswer;
	private long lastRequestStart = 0;
	private long lastRequestEnd = 0;
	private PamObserver lastRequestObserver = null;
	private PamObserver lastEndUser = null;

	/**
	 * Gets data for offline display, playback, etc.<p>
	 * This is used to get data from some upstream process
	 * which is quite different to the function
	 * loadViewerData(...) which loads data directly associated
	 * with this data block. 
	 * <p>For example, this might be called in the FFT data block by 
	 * the spectrogram which wants some data to display. The FFT data block 
	 * does not have this data, so it passes the request up to it's process
	 * which will in turn pass the request on until it reaches a module which 
	 * is capable of loading data into data units and sending them back 
	 * down the line.   
	 * 
	 * @param observer data observer which will receive the data
	 * @param startMillis start time in milliseconds
	 * @param endMillis end time in milliseconds
	 * @param loadKeepLayers 
	 * @param allowRepeats allow the same data to be loaded a second time. 
	 * @param cancellationObject 
	 * @return answer: . 
	 */
	synchronized public int getOfflineData(OfflineDataLoadInfo offlineDataInfo) {
		/*
		 * Several orders can queue on this lock. One which was cancelled while
		 * waiting shouldn't then go and load all its data. 
		 */
		if (offlineDataInfo.cancel) {
			return REQUEST_INTERRUPTED;
		}
		loadDepth.incrementAndGet();
		try {
			return getOfflineDataLocked(offlineDataInfo);
		}
		finally {
			loadDepth.decrementAndGet();
		}
	}

	private int getOfflineDataLocked(OfflineDataLoadInfo offlineDataInfo) {
		//prevent a load of exactly the same data.
		if (offlineDataInfo.getAllowRepeats() == false &&
				lastRequestStart == offlineDataInfo.getStartMillis() &&
				lastRequestEnd == offlineDataInfo.getEndMillis() &&
				lastRequestObserver ==offlineDataInfo.getCurrentObserver() &&
				lastEndUser == offlineDataInfo.getEndObserver()) {
			//			System.out.println(String.format("Don't load repeated data %s to %s",
			//					PamCalendar.formatDateTime(startMillis), PamCalendar.formatDateTime(endMillis)));
			return lastRequestAnswer | REQUEST_SAME_REQUEST;
		}

//		if (pamDataBlock instanceof PamRawDataBlock) {
//			System.out.printf("Keep layers in %s is %d\n", pamDataBlock.getLongDataName(), offlineDataInfo.getLoadKeepLayers());
//		}
		this.currentOfflineLoadKeep = true;//offlineDataInfo.getLoadKeepLayers() > 0;

		//need to keep a record of these as they change through the upstream data request process. 
		clearRequestingObservers();
		addRequestingObserver(offlineDataInfo.getCurrentObserver());

		pamDataBlock.clearAll();

		//		System.out.println("Start loading some offline data: from  " + PamCalendar.formatDateTime(offlineDataInfo.getStartMillis() ) +" to "+ 
		//				PamCalendar.formatDateTime(offlineDataInfo.getEndMillis() ) + " "+offlineDataInfo.getCurrentObserver().getObserverName() + 
		//				" "+offlineDataInfo.getLoadKeepLayers());

		lastRequestAnswer = pamDataBlock.getParentProcess().getOfflineData(offlineDataInfo);
		//		System.out.println(String.format("getOfflineData %s has %d units ",
		//				getDataName(), getUnitsCount()));

		//		System.out.println("Orderring done: " + offlineDataInfo.getCurrentObserver().getObserverName() + " " + offlineDataInfo.getLoadKeepLayers());

		//reset some of the changeable variables in offline data info. 
		offlineDataInfo.reset(); 

		removeRequestingObserver(offlineDataInfo.getCurrentObserver());

		lastRequestStart = offlineDataInfo.getStartMillis();
		lastRequestEnd = offlineDataInfo.getEndMillis();
		lastRequestObserver = offlineDataInfo.getCurrentObserver();
		lastEndUser = offlineDataInfo.getEndObserver();

		currentOfflineLoadKeep = true; //always set back to true at the end. 

		return lastRequestAnswer;
	}

	/**
	 * Clear all requesting observers from the list. Stops them 
	 * all adding up and everything getting everything !
	 */
	public void clearRequestingObservers() {
		if (requestingObservers != null) {
			requestingObservers.clear();
		}
	}
	/**
	 * Add observer to requesting observer list which is 
	 * used to distribute data to selected observers when it's
	 * reloaded in offline viewer mode. 
	 * @param observer observer
	 */
	private void addRequestingObserver(PamObserver observer) {
		if (requestingObservers == null) {
			requestingObservers = new Vector<PamObserver>();
		}
		if (requestingObservers.contains(observer) == false) {
			requestingObservers.add(observer);
		}
	}

	/**
	 * Remove observer from requesting observer list which is 
	 * used to distribute data to selected observers when it's
	 * reloaded in offline viewer mode. 
	 * @param observer observer
	 */
	private void removeRequestingObserver(PamObserver observer) {
		if (requestingObservers == null) {
			return;
		}
		requestingObservers.remove(observer);
	}


	public void notifyOfflineObservers(T pamDataUnit) {
		if (requestingObservers != null) {
			for (int i = 0; i < requestingObservers.size(); i++) {
				PamObserver obs = requestingObservers.get(i);
				if (obs != null) {
					obs.addData(pamDataBlock, pamDataUnit);
				}
			}
		}

	}


	public boolean isCurrentOfflineLoadKeep() {
		return currentOfflineLoadKeep;
	}


	public void setCurrentOfflineLoadKeep(boolean currentOfflineLoadKeep) {
		this.currentOfflineLoadKeep = currentOfflineLoadKeep;
	}

}
