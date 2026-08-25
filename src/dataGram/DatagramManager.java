package dataGram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import PamController.AWTScheduler;
import PamController.OfflineDataStore;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.CancelObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamViewFX.pamTask.PamTaskUpdate;
import pamViewFX.pamTask.SimplePamTaskUpdate;
import pamguard.GlobalArguments;

public class DatagramManager {

	private DatagramSettings datagramSettings = new DatagramSettings();

	private OfflineDataStore offlineDataStore;

	private DatagramSettingsStore settingsStore;

	/**
	 * Name used when registering the datagram settings. Different data stores must use
	 * different names or their settings will overwrite each other.
	 */
	private String settingsName;

	/**
	 * @param offlineDataStore the data store these datagrams belong to.
	 */
	public DatagramManager(OfflineDataStore offlineDataStore) {
		this(offlineDataStore, offlineDataStore.getDataSourceName());
	}

	/**
	 * @param offlineDataStore the data store these datagrams belong to.
	 * @param settingsName name to register the datagram settings under. Several data
	 * stores can share a data source name (e.g. every OfflineFileServer is called
	 * "Sound Files"), in which case they must pass something unique here.
	 */
	public DatagramManager(OfflineDataStore offlineDataStore, String settingsName) {
		super();
		this.offlineDataStore = offlineDataStore;
		this.settingsName = settingsName;
		PamSettingManager.getInstance().registerSettings(settingsStore = new DatagramSettingsStore());
	}

	/**
	 * 
	 * @return a list of data blocks which have a DatagramProvider
	 */
	public ArrayList<PamDataBlock> getDataBlocks() {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		int n = allDataBlocks.size();
		ArrayList<PamDataBlock> usedBlocks = new ArrayList<PamDataBlock>();
		for (int i = 0; i < n; i++) {
			if (allDataBlocks.get(i).getDatagramProvider() != null) {
				usedBlocks.add(allDataBlocks.get(i));
			}
		}
		return usedBlocks;
	}

	/**
	 * 
	 * @return a list of datablocks which need their datagrams updated. 
	 */
	public ArrayList<PamDataBlock> checkAllDatagrams() {
		ArrayList<PamDataBlock> grammedDataBlocks = getDataBlocks();
		ArrayList<PamDataBlock> updateBlocks = new ArrayList<PamDataBlock>();
		boolean isBatch = GlobalArguments.getParam("-batch") != null;
		/**
		 * Check to see if any of the blocks have an existing datagram
		 */
		if (!datagramSettings.validDatagramSettings && !isBatch) {
			/*
			 *  this is the first time this has been run, so ask
			 *  the user what datagram size they want.  
			 */
			if (showDatagramDialog(true)) {
				// no need to do anything here. 
			}
		}


//		System.out.println("Checking all datagrams for 1"); 
		
		for (int i = 0; i < grammedDataBlocks.size(); i++) {
			if (checkDatagram(grammedDataBlocks.get(i))) {
				updateBlocks.add(grammedDataBlocks.get(i));
			}
		}
		
//		System.out.println("Checking all datagrams for 2 "); 

		return updateBlocks;
	}

	/**
	 * Check that every data map point associated with this datablock has 
	 * a datagram and that the datagram entries are of the right length. <br>
	 * Return true if the datagram needs updating
	 * @param pamDataBlock datablock to check
	 * @return true if Datagram needs updating, false otherwise. 
	 */
	private boolean checkDatagram(PamDataBlock pamDataBlock) {
		DatagramProvider datagramProvider = pamDataBlock.getDatagramProvider();
//		System.out.println("Checking datagram for " + pamDataBlock.getDataName() + " "+datagramProvider);
		if (datagramProvider == null) {
			return false;
		}
		OfflineDataMap dm = pamDataBlock.getOfflineDataMap(offlineDataStore);
		if (dm == null) {
			return false;
		}

		OfflineDataMapPoint dmp;
		Iterator<OfflineDataMapPoint> it = dm.getListIterator();
		while (it.hasNext()) {
			dmp = it.next();
			if (isDatagramValid(dmp, getDatagram(dmp)) == false) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the datagram held by a data map point, or null if this type of map point
	 * can't hold one.
	 * @param dmp data map point
	 * @return the datagram, or null.
	 */
	protected Datagram getDatagram(OfflineDataMapPoint dmp) {
		if (dmp instanceof DatagramPoint) {
			return ((DatagramPoint) dmp).getDatagram();
		}
		return null;
	}

	/**
	 * Is an existing datagram still valid, or does it need recalculating ? Subclasses
	 * may override to add additional checks (e.g. on the number of points in each
	 * datagram line).
	 * @param dmp the data map point the datagram came from
	 * @param datagram existing datagram, may be null
	 * @return true if the datagram is up to date and doesn't need recalculating.
	 */
	protected boolean isDatagramValid(OfflineDataMapPoint dmp, Datagram datagram) {
		if (datagram == null) {
			return false;
		}
		return datagram.getIntervalSeconds() == datagramSettings.datagramSeconds;
	}

	/**
	 * Fix the datagram bin size without asking the user, so long as they haven't
	 * already chosen one themselves.
	 * <p>
	 * Some modules know what bin size suits their data and there is nothing useful
	 * for the user to decide - the CPOD/FPOD importer is the obvious case, where the
	 * settings dialog would otherwise appear part way through an import. Calling this
	 * marks the settings as valid, so the dialog which
	 * {@link #checkAllDatagrams()} would normally show the first time round is not
	 * shown at all.
	 * <p>
	 * A bin size the user has already settled on - whether through that dialog or
	 * through the binary store's datagram options - is never overwritten, since
	 * changing it would silently throw away and recalculate every datagram they
	 * have.
	 *
	 * @param datagramSeconds the bin size to use, in seconds.
	 * @return true if the settings were changed.
	 */
	public boolean setDefaultDatagramSeconds(int datagramSeconds) {
		if (datagramSeconds <= 0 || datagramSettings.validDatagramSettings) {
			return false;
		}
		datagramSettings.datagramSeconds = datagramSeconds;
		datagramSettings.validDatagramSettings = true;
		return true;
	}

	public boolean showDatagramDialog(boolean firstCall) {
		if(PamGUIManager.getGUIType()==PamGUIManager.NOGUI) {
			return false;
		}
		DatagramSettings newSettings = DatagramDialog.showDialog(PamController.getMainFrame(), this);
		if (newSettings != null) {
			if (firstCall) {
				datagramSettings = newSettings;
				datagramSettings.validDatagramSettings = true;
				return true;
			}
			else if (newSettings.datagramSeconds != datagramSettings.datagramSeconds) {
				int ans = JOptionPane.showConfirmDialog(PamController.getMainFrame(),
						"<html>Recalculating the datagram for a large dataset may take a long time<br>" +
								"Are you sure you want to continue ?</html>", "Warning", JOptionPane.YES_NO_OPTION);
				if (ans == JOptionPane.NO_OPTION) {
					return false;
				}
				else {
					datagramSettings = newSettings;
					updateDatagrams();
					return true;
				}
			}
			else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update all datagrams (which need it)
	 */
	public void updateDatagrams() {
		ArrayList<PamDataBlock> updateList = checkAllDatagrams();
		System.out.println("Update all datagrams: " + 1);
		if (updateList != null && updateList.size() > 0) {
			updateDatagrams(updateList);
		}
		System.out.println("Update all datagrams: "+ 2);
	}

	private volatile DatagramProgressDialog datagramProgressDialog;

	private volatile DatagramCreator datagramCreator;


	/**
	 * update a list of datagrams. This should be done in a 
	 * worker thread ...
	 * @param updateList
	 */
	public void updateDatagrams(ArrayList<PamDataBlock> updateList) {
		datagramCreator = new DatagramCreator(updateList);
		AWTScheduler.getInstance().scheduleTask(datagramCreator);
	}
	
	/**
	 * Get true if the datagram worker is running. 
	 * @return
	 */
	public boolean getStatus() {
		return datagramCreator != null;
	}


	/**
	 * Process a single data map point, creating a datagram for it and attaching it
	 * to the map point.
	 * <p>
	 * The default implementation loads the data units for the map point back out of
	 * the data store and passes them to the data block's {@link DatagramProvider}.
	 * Subclasses which can summarise their data more directly (e.g. by reading sound
	 * files) should override this and must NOT call super.
	 * 
	 * @param dataBlock data block being summarised
	 * @param dmp data map point to process
	 * @param workMonitor progress reporting and cancellation
	 */
	protected void processDataMapPoint(PamDataBlock dataBlock, OfflineDataMapPoint dmp, DatagramWorkMonitor workMonitor) {
		
		long startTime = dmp.getStartTime();
		long endTime = dmp.getEndTime();

		DatagramProvider datagramProvider = dataBlock.getDatagramProvider();
		int nPoints = datagramProvider.getNumDataGramPoints();
		float[] tempData = new float[nPoints]; // temp holder - gets converted to float later on
		float[] gramData;

		/**
		 * Create a new datagram class which will be given to this individual dmp
		 */
		Datagram datagram = new Datagram(datagramSettings.datagramSeconds);
		long datagramMillis = datagramSettings.datagramSeconds*1000;
		long currentStart = startTime;
		long currentEnd = currentStart + datagramMillis;
		DatagramDataPoint datagramPoint;
		/*
		 * first load all the data from a single file ...
		 */
		dataBlock.clearAll();
		Runtime.getRuntime().gc();
		dataBlock.loadViewerData(new OfflineDataLoadInfo(startTime, endTime), null);
		int totalUnits = dataBlock.getUnitsCount();
		ListIterator<PamDataUnit> li = dataBlock.getListIterator(0);
		PamDataUnit dataUnit;
		int usedDataUnits = 0;
		int doneUnits = 0;
		workMonitor.publishProgress(new DatagramProgress(DatagramProgress.STATUS_UNITCOUNT, totalUnits, doneUnits));
		while (currentStart <= endTime) {
			datagramPoint = new DatagramDataPoint(datagram, currentStart, currentStart+datagramMillis, nPoints);
			usedDataUnits = 0;
			long t = System.currentTimeMillis();
			while (li.hasNext()) {
				dataUnit = li.next();
				if (dataUnit.getTimeMilliseconds() >= currentEnd) {
					break;
				}
				datagramProvider.addDatagramData(dataUnit, tempData);
				usedDataUnits++;
				doneUnits++;
				long t2 = System.currentTimeMillis();
				if (t2-t > 500) {
					t = t2;
					workMonitor.publishProgress(new DatagramProgress(DatagramProgress.STATUS_UNITCOUNT, totalUnits, doneUnits));
				}

				if (workMonitor.isWorkCancelled()) {
					return;
				}
			}
			/**
			 * end the current datagram
			 */
			if (usedDataUnits > 0) {
				gramData = datagramPoint.getData(); 
				for (int i = 0; i < nPoints; i++) {
					gramData[i] = (float) tempData[i];
					tempData[i] = 0;
				}
				datagramPoint.setData(gramData, usedDataUnits);
			}
			//System.out.println("Process Point: " + currentStart + " " + endTime); 
			datagram.addDataPoint(datagramPoint);
			currentStart = currentEnd;
			currentEnd = currentStart+datagramMillis;
		}
		if (DatagramPoint.class.isAssignableFrom(dmp.getClass())) {
			((DatagramPoint) dmp).setDatagram(datagram);
		}

		offlineDataStore.rewriteIndexFile(dataBlock, dmp);

	}

	class DatagramCreator extends SwingWorker<Integer, DatagramProgress> implements CancelObserver, DatagramWorkMonitor {

		private ArrayList<PamDataBlock> updateList;

		private volatile boolean cancelNow = false;
		/**
		 * @param updateList
		 */
		public DatagramCreator(ArrayList<PamDataBlock> updateList) {
			super();
			this.updateList = updateList;
		}

		@Override
		protected Integer doInBackground() {
			try {
				publish(new DatagramProgress(DatagramProgress.STATUS_BLOCKCOUNT, updateList.size()));

				for (int i = 0; i < updateList.size(); i++) {
					processDataBlock(updateList.get(i));
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		private void processDataBlock(PamDataBlock pamDataBlock) {
			DatagramProvider datagramProvider = pamDataBlock.getDatagramProvider();
			if (datagramProvider == null) {
				return;
			}
			OfflineDataMap dm = pamDataBlock.getOfflineDataMap(offlineDataStore);
			if (dm == null) {
				return;
			}

			OfflineDataMapPoint dmp;
			/**
			 * Loop through a first time to see now many map points need updating. 
			 */
			Iterator<OfflineDataMapPoint> it = dm.getListIterator();
			int nToUpdate = 0;
			while (it.hasNext()) {
				dmp = it.next();
				if (isDatagramValid(dmp, getDatagram(dmp)) == false) {
					nToUpdate++;
				}
			}
			publish(new DatagramProgress(DatagramProgress.STATUS_STARTINGBLOCK, pamDataBlock, nToUpdate));
			/*
			 * then loop through again and update the actual map points...
			 */
			it = dm.getListIterator();
			int iPoint = 0;
			while (it.hasNext()) {
				dmp = it.next();
				if (isDatagramValid(dmp, getDatagram(dmp)) == false) {
					publish(new DatagramProgress(DatagramProgress.STATUS_STARTINGFILE, dmp, ++iPoint));
					processDataMapPoint(pamDataBlock, dmp, this);
					publish(new DatagramProgress(DatagramProgress.STATUS_ENDINGFILE, dmp, iPoint));
				}
				if (cancelNow) {
					break;
				}
			}

			publish(new DatagramProgress(DatagramProgress.STATUS_ENDINGBLOCK, pamDataBlock, nToUpdate));
		}

		@Override
		protected void process(List<DatagramProgress> chunks) {
			for (int i = 0; i < chunks.size(); i++) {
				showProgress(chunks.get(i), this);
			}
		}

		@Override
		protected void done() {
			//System.out.println("DATAGRAM is DONE!!!"); 
			if (PamGUIManager.isSwing()) {
				if (datagramProgressDialog != null) {
					datagramProgressDialog.setVisible(false);
					datagramProgressDialog = null;
				}
			}
			else {
				PamController.getInstance().notifyTaskProgress(
						new SimplePamTaskUpdate("Finished Datagram Mapping", PamTaskUpdate.STATUS_DONE));
				publish(new DatagramProgress(PamTaskUpdate.STATUS_DONE, 1, 1));

			}
			datagramCreator = null;
			datagramsComplete();
			/*
			 * Nothing else tells the data map that there are new datagrams to draw, so
			 * give it a nudge.
			 */
			PamController.getInstance().notifyModelChanged(PamControllerInterface.OFFLINE_DATA_LOADED);
		}

		@Override
		public boolean cancelPressed() {
			cancelNow = true;
			return true;
		}

		@Override
		public void publishProgress(DatagramProgress datagramProgress) {
			publish(datagramProgress);
		}

		@Override
		public boolean isWorkCancelled() {
			return cancelNow;
		}

	}

	public void showProgress(DatagramProgress datagramProgress, DatagramCreator datagramCreator) {
		if (PamGUIManager.isSwing()) {
			if (datagramProgressDialog == null) {
				datagramProgressDialog = DatagramProgressDialog.showDialog(null, datagramCreator);
			}
			datagramProgressDialog.setProgress(datagramProgress);
		}
		else {
			PamController.getInstance().notifyTaskProgress(datagramProgress);
		}
	}

	/**
	 * Called on the AWT thread once all datagram creation has finished (or been
	 * cancelled). Subclasses can override to save whatever they've created.
	 */
	protected void datagramsComplete() {
	}

	/**
	 * @return the data store these datagrams belong to.
	 */
	public OfflineDataStore getOfflineDataStore() {
		return offlineDataStore;
	}

	/**
	 * @return the datagramSettings
	 */
	public DatagramSettings getDatagramSettings() {
		return datagramSettings;
	}

	/**
	 * @param datagramSettings the datagramSettings to set
	 */
	public void setDatagramSettings(DatagramSettings datagramSettings) {
		this.datagramSettings = datagramSettings;
	}


	/**
	 * Get min and max values for an entire datagram. 
	 * @param dataBlock datablock owning datagram
	 * @param includeZeros include data that have zero value - often a 
	 * good idea to exclude these if your calculating log scale limits
	 * @return 2 el array of min and max values. 
	 */
	public synchronized double[] getMinAndMax(PamDataBlock dataBlock, boolean includeZeros) {
		OfflineDataMap binaryDataMap = dataBlock.getOfflineDataMap(offlineDataStore);
		if (binaryDataMap == null) {
			return null;
		}
		DatagramProvider dp = dataBlock.getDatagramProvider();
		if (dp == null) return null;
		Iterator<OfflineDataMapPoint> iterator = binaryDataMap.getListIterator();
		OfflineDataMapPoint mapPoint;
		DatagramPoint datagramPoint;
		double[] minMaxVal = {Double.MAX_VALUE, Double.MIN_VALUE};
		int nDataPoints;
		Datagram datagram;
		DatagramDataPoint dataPoint;
		float[] data;
		while (iterator.hasNext()) {
			mapPoint = iterator.next();
			if (!DatagramPoint.class.isAssignableFrom(mapPoint.getClass())) {
				continue;
			}
			datagramPoint = (DatagramPoint) mapPoint;
			datagram = datagramPoint.getDatagram();
			if (datagram == null) {
				continue;
			}
			//			System.out.println("Add map point starting at " + PamCalendar.formatDateTime(mapPoint.getStartTime()));
			nDataPoints = datagram.getNumDataPoints();
			for (int i = 0; i < nDataPoints; i++) {
				dataPoint = datagram.getDataPoint(i);
				data = dataPoint.getData();
				for (int j = 0; j < data.length; j++) {
					if (Double.isNaN(data[j])) {
						continue;
					}
					if (includeZeros || data[j] > 0) {
						minMaxVal[0] = Math.min(minMaxVal[0], data[j]);
					}
					minMaxVal[1] = Math.max(minMaxVal[1], data[j]);
				}
			}
		}
		return minMaxVal;
	}
	/**
	 * Create the raw data to go into a dataGram. 
	 * Fill with NaN wherever data are unavailable.  
	 * @param dataBlock
	 * @param startTimeMills
	 * @param endTimeMillis
	 * @param maxPixels
	 * @return a 2D array of double precision data to go into an image. 
	 */
	public synchronized DatagramImageData getImageData(PamDataBlock dataBlock, long startTimeMillis, long endTimeMillis, int maxPixels) {

		OfflineDataMap offlineDataMap = dataBlock.getOfflineDataMap(offlineDataStore);
		if (offlineDataMap == null) {
			return null;
		}
		long datagramMillis = datagramSettings.datagramSeconds * 1000;
		// round the start and end times up and down a little ...
		startTimeMillis /= datagramMillis;
		startTimeMillis *= datagramMillis;
		endTimeMillis += datagramMillis - 1;
		endTimeMillis /= datagramMillis;
		endTimeMillis *= datagramMillis;
		

		/**
		 * Need to work out the resolution of the datagram data for this 
		 * to work, if files are hsort it may be<< datagramMillis
		 */
		Iterator<OfflineDataMapPoint> iterator = offlineDataMap.getListIterator();
		long minLen = Long.MAX_VALUE;
		long maxLen = 0;
		long meanLen = 0;
		long nLen = 0;
		boolean first = true;
		while (iterator.hasNext()) {
			OfflineDataMapPoint mapPoint = iterator.next();
			if (first) {
				first = false;
				startTimeMillis = Math.max(startTimeMillis, mapPoint.getStartTime());
			}
			
			long ptLen = mapPoint.getEndTime()-mapPoint.getStartTime();
			minLen = Math.min(minLen, ptLen);
			maxLen = Math.max(maxLen, ptLen);
			meanLen += ptLen;
			nLen ++;
		}
		if (nLen > 0) {
			meanLen /= nLen;
//			datagramMillis = Math.max(meanLen,1000);
		}

		// first work out how many points there should naturally be ...
		// this doesn't work if the files are shorter in length than the datagramMillis since
		// there will be one datagram point per file (I think). 
		int nTimePoints = (int) ((endTimeMillis-startTimeMillis)/datagramMillis);
		if (nTimePoints > maxPixels) {
			nTimePoints = maxPixels;
		}
		if (nTimePoints < 1) {
			nTimePoints = 1;
		}
		double xScale = (endTimeMillis - startTimeMillis) / nTimePoints;
		// work out the number of points in y
		DatagramProvider dp = dataBlock.getDatagramProvider();
		if (dp == null) return null;
		int nYPoints = dp.getNumDataGramPoints();
		double[][] datagramArray = new double[nTimePoints][nYPoints];
		int[][] scaleCount = new int[nTimePoints][nYPoints];


//		Iterator<OfflineDataMapPoint> 
		iterator = offlineDataMap.getListIterator();
		OfflineDataMapPoint mapPoint;
		DatagramPoint datagramPoint;
		long pointStart, pointEnd;
		int xBin;
		Datagram datagram;
		DatagramDataPoint dataPoint;
		int nDataPoints;
		float[] data;
		first = true;
		/**
		 * will need to get the real image times, which may not be the full 
		 * requested range depending on data availability. 
		 */
		long imageStart = startTimeMillis;
		long imageEnd = endTimeMillis;
		while (iterator.hasNext()) {
			mapPoint = iterator.next();
			if (first) {
				first = false;
				imageStart = mapPoint.getStartTime();
			}
			imageEnd = mapPoint.getEndTime();
			if (!DatagramPoint.class.isAssignableFrom(mapPoint.getClass())) {
				continue;
			}
			datagramPoint = (DatagramPoint) mapPoint;
			pointStart = mapPoint.getStartTime();
			pointEnd = mapPoint.getEndTime();
			if (pointEnd < startTimeMillis - datagramSettings.datagramSeconds*5000) {
				continue;
			}
			if (pointStart > endTimeMillis) {
				break;
			}
			datagram = datagramPoint.getDatagram();
			if (datagram == null) {
				continue;
			}
			//			System.out.println("Add map point starting at " + PamCalendar.formatDateTime(mapPoint.getStartTime()));
			nDataPoints = datagram.getNumDataPoints();
			for (int i = 0; i < nDataPoints; i++) {
				dataPoint = datagram.getDataPoint(i);
				//				System.out.println("   .... add Datagram point starting at " + 
				//						PamCalendar.formatDateTime(dataPoint.getStartTime()));
				xBin = (int)((dataPoint.getStartTime()-startTimeMillis)/xScale);
				if (xBin < 0) continue;
				if (xBin >= nTimePoints) break;
				data = dataPoint.getData();
				for (int j = 0; j < Math.min(nYPoints, data.length); j++) {
					datagramArray[xBin][j] += data[j];
					scaleCount[xBin][j] ++;
				}
			}
		}

		/**
		 * now scale any bins with > 1 entry and set empty ones to -1.
		 */
		int n;
		for (int i = 0; i < nTimePoints; i++) {
			for (int j = 0; j < nYPoints; j++) {
				n = scaleCount[i][j];
				if (n == 0) {
					datagramArray[i][j] = -1;
				}
				else if (n > 0) {
					datagramArray[i][j] = datagramArray[i][j]/n;
				}
			}
		}

		return new DatagramImageData(startTimeMillis, endTimeMillis, datagramArray);
	}

	private class DatagramSettingsStore implements PamSettings {

		@Override
		public String getUnitName() {
			return settingsName;
		}

		@Override
		public String getUnitType() {
			return "Datagram Settings";
		}

		@Override
		public Serializable getSettingsReference() {
			return datagramSettings;
		}

		@Override
		public long getSettingsVersion() {
			return DatagramSettings.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			DatagramSettings newSettings = ((DatagramSettings) pamControlledUnitSettings.getSettings()).clone();
			if (newSettings != null) {
				datagramSettings = newSettings;
				return true;
			}
			else {
				return false;
			}
		}

	}

	/**
	 * @return the binaryStore
	 */
	protected OfflineDataStore getBinaryStore() {
		return offlineDataStore;
	}
}
