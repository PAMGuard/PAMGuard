package PamguardMVC.dataOffline;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;

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
	 * Threads which have been cancelled are set to load again.
	 */
	private ArrayList<OfflineDataLoadInfo> waitingDataLoads; 

	/**
	 * Constructor for the offline data loader. 
	 * @param pamDataBlock - the datablock. 
	 */
	public OfflineDataLoading(PamDataBlock<T> pamDataBlock){
		this.pamDataBlock=pamDataBlock; 
		waitingDataLoads=new  ArrayList<OfflineDataLoadInfo>(); 
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
	 * Clears all raw and FFT data blocks prior to a new load. 
	 */
	private void clearAllFFTBlocks() {
		ArrayList<PamDataBlock> datablocks = PamController.getInstance().getFFTDataBlocks();
		for (int i=0; i<datablocks.size(); i++) {
			datablocks.get(i).clearAll();
			datablocks.get(i).clearDeletedList();
		}

		datablocks = PamController.getInstance().getRawDataBlocks();
		for (int i=0; i<datablocks.size(); i++) {
			datablocks.get(i).clearAll();
			datablocks.get(i).clearDeletedList();
		}
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
	 * 	<p>OFFLINE_DATA_CANCEL - give up and return

	 * @param offlineDataInfo the OfflineDataLoadInfo object which stores all parameters for the data load. 

	 */
	public void orderOfflineData(OfflineDataLoadInfo offlineDataInfo) {
		//		System.out.println("Start order lock");
		synchronized (orderLock) {
			//			System.out.println("Past order lock");
			long t1 = System.currentTimeMillis();
			long t2 = System.currentTimeMillis();
			long t3 = t2;
			long t4 = t2;
			//			String orderDates = String.format(" %s to %s", 
			//					PamCalendar.formatDateTime(startMillis), PamCalendar.formatDateTime(endMillis));
			//			System.out.printf("Offline data order in %s %s from %s to %s\n", pamDataBlock.getDataName(), offlineDataInfo.toString(),
			//					PamCalendar.formatDBDateTime(offlineDataInfo.getStartMillis()), 
			//					PamCalendar.formatDBDateTime(offlineDataInfo.getEndMillis()));
			//			if (offlineDataInfo.getEndMillis()-offlineDataInfo.getStartMillis() > 3600000L) {
			//				System.out.printf("Stupid long load time !");
			//				return;
			//			}
			try {
				if (orderData != null) {
					//				System.out.println("order Data is not null");
					if (orderData.isDone() == false) {
						switch (offlineDataInfo.getInterrupt()) {
						case OFFLINE_DATA_INTERRUPT:
							//						System.out.println("Request order cancelling");
							int giveUp = 0;

							if (orderData.cancelOrder()) {			

								while ((orderData!=null || !orderData.isDone()) & giveUp++ < 300) {
									try {
										Thread.sleep(10);
									} catch (InterruptedException e) {
										e.printStackTrace();
										return;
									}
								}
							}
							else {
								//							System.out.println("Old order could not be cancelled");
							}
							break;
						case OFFLINE_DATA_CANCEL:
							//						System.out.println("Don't order new data " + orderDates);
							return;
						case OFFLINE_DATA_WAIT:
							int waitCount = 0;
							t3 = System.currentTimeMillis();
							waitingDataLoads.add(offlineDataInfo);
							return;
							//						//						System.out.println("Wait for old lot to complete " + orderDates);
							//						while (true) {
							//							if (orderData==null || orderData.isDone() || orderData.isCancelled()) {
							//								break;
							//							}
							//							waitCount++;
							//							try {
							//								Thread.sleep(10, 0);
							//							} catch (InterruptedException e) {
							//								e.printStackTrace();
							//							}
							//						}
						}
						t4 = System.currentTimeMillis() - t3;
					}
				}
			}
			catch (NullPointerException e) {
				// happens when orderData is set null in a different thread. 
			}
			//System.out.println(String.format("%s waited %d, %d, %d, %d during ordering load", 
			//		getDataName(), t2-t1, t3-t2, t4-t3, t5-t4));

			orderData = new OrderData(offlineDataInfo);
			//			requestCancellationObject.cancel = true;
			orderData.execute();

			//t = new Timer(1000, new StartOrderOnTimer(orderData));
		} // end of order lock
	}


	/**
	 * Cancels the current order. 
	 */
	public void cancelDataOrder() {
		cancelDataOrder(false);
	}


	/**
	 * 
	 * @param que
	 */
	public void cancelDataOrder(boolean que) {
//		threadMessage("Calling cancelDataOrder");
		synchronized (orderLock) {
//			threadMessage("cancelDataOrder is in synchronized oderLock");
			if (orderData != null) {
				try {
					boolean isCancelled = orderData.cancelOrder();
//					threadMessage("candelDataOrder returned: isCancelled = " + isCancelled);
				}
				catch (NullPointerException e) {
					System.err.println("Null pointer in Cancel data order " + e.getMessage());
				}
			}
		}
		if (que) this.waitingDataLoads.clear(); 
	}
	/**
	 * 
	 * @return true if an order for data is currently still being processed. 
	 */
	public boolean getOrderStatus() {
		return (orderData != null);
	}

	volatile private OrderData orderData;

	public Object orderLock = new Object();


	class OrderData extends SwingWorker<Integer, Integer> {

		/**
		 * The current offline data info 
		 */
		OfflineDataLoadInfo offlineDataInfo;

		public OfflineDataLoadInfo getOfflineDataInfo() {
			return offlineDataInfo;
		}

		/**
		 * 
		 */
		String dateStr;

		OrderData(OfflineDataLoadInfo offlineDataInfo) {
			this.offlineDataInfo=offlineDataInfo; 
			dateStr = String.format(" %s to %s", 
					PamCalendar.formatDateTime(offlineDataInfo.getStartMillis()), PamCalendar.formatDateTime(offlineDataInfo.getEndMillis()));
		}

		public boolean cancelOrder() {
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
//			threadMessage("Start background");
			try {
				//								System.out.println("Enter get offline data " + pamDataBlock.getDataName() + " Thread " + Thread.currentThread().getName());

				clearAllFFTBlocks();
//				threadMessage("Called clearAllFTBlocks");
				int ans = getOfflineData(offlineDataInfo);
				//								System.out.println("Leave get offline data " + pamDataBlock.getDataName());

				if (this == orderData) {
					orderData = null;
				}
//				threadMessage("End background exit code " + ans);
				return ans;
			}
			catch (Exception e) {
				e.printStackTrace();
				if (this == orderData) {
					orderData = null;
				}
				return 0;
			}
		}
		

		@Override
		protected void done() {
			if (this == orderData) {
				orderData = null;
			}
			else {
				//				System.out.println("Data order ending which wasn't the latest created");
			}
			Integer status = 0;
			try {
				if (isCancelled()) {
					status = REQUEST_INTERRUPTED;
					//check whether the cancelled thread should be saved for later loading. 
					//					System.out.println("The load has been cancelled: " + offlineDataInfo.getCurrentObserver().getObserverName());
					checkDataReloadQue(offlineDataInfo);
				}
				else {
					status = get();
					//					System.out.println(getDataName() + " Order done - not cancelled " + dateStr + " status " + status);
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


			//			System.out.println("WAITING DATA LOAD");
			for (int i=0; i<waitingDataLoads.size(); i++){
				//				System.out.println("OfflineDataLoading.Done(): " + waitingDataLoads.get(i).getCurrentObserver().getObserverName());
			}
			//			System.out.println("WAITING DATA END");

			if (!isCancelled()) launchQuedReloadThread();

		}

		@Override
		protected void process(List<Integer> chunks) {
			// TODO Auto-generated method stub
			super.process(chunks);
		}

	}

//	private void threadMessage(String message) {
//		String name = Thread.currentThread().getName();
//		String now = PamCalendar.formatDBDateTime(System.currentTimeMillis(), true);
//		System.out.printf("Thread %s load for %s at %s: %s\n", name, pamDataBlock.getDataName(), now, message);
//	}

	private void launchQuedReloadThread(){
		//		System.out.println(" launchQuedReloadThread(): ");
		if (waitingDataLoads.size()>=1){
			OfflineDataLoadInfo offlineDataInfo=waitingDataLoads.get(0);
			//			System.out.println("Removing: " + offlineDataInfo.getCurrentObserver().getObserverName());
			waitingDataLoads.remove(0);
			this.orderOfflineData(offlineDataInfo);
		}
	}

	/**
	 * Check whether a a
	 * @param offlineDataInfo
	 */
	private void checkDataReloadQue(OfflineDataLoadInfo offlineDataInfo){
		if (offlineDataInfo.getPriority()==OfflineDataLoadInfo.PRIORITY_CANCEL_RESTART){
			//check that there is not a something in the que with the same dataLoading observer. If there is remove it and replace with current
			for (int i=0; i< this.waitingDataLoads.size(); i++){
				if (offlineDataInfo.getCurrentObserver().getObserverName().equals(
						waitingDataLoads.get(i).getCurrentObserver().getObserverName())){
					this.waitingDataLoads.remove(i);
					i=i-1;
				}
			}
			//the thread should be saved for loading later. 
			this.waitingDataLoads.add(offlineDataInfo);
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

		this.currentOfflineLoadKeep = offlineDataInfo.getLoadKeepLayers() > 0;

		//need to keep a record of these as they change through the upstream data request process. 
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
