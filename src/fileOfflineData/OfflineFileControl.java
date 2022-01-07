package fileOfflineData;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import dataGram.DatagramManager;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;
import PamController.AWTScheduler;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FileList;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

public abstract class OfflineFileControl extends PamControlledUnit implements OfflineDataStore {

	protected OfflineFileParams fileParams = new OfflineFileParams();

	private OfflineFileDataMap offlineFileDataMap;

	private OfflineFileProcess offlineFileProcess;

	private OfflineFileProgressDialog mapProgressDialog;
	
	private DatagramManager datagramManager;

	public OfflineFileControl(String unitType, String unitName) {
		super(unitType, unitName);
		offlineFileProcess = new OfflineFileProcess(this);
		offlineFileDataMap = new OfflineFileDataMap(this, offlineFileProcess.getPamDataBlock());
		offlineFileProcess.getPamDataBlock().addOfflineDataMap(offlineFileDataMap);
		addPamProcess(offlineFileProcess);

		PamSettingManager.getInstance().registerSettings(new OfflineFileSettings(this));

	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			createOfflineDataMap(getGuiFrame());
			break;
		}
	}

	@Override
	public void createOfflineDataMap(Window parentFrame) {
		if ((PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW)) {
			return;
		}
		// launch the map creation off into a new thread. 
		D3MapMaker mapMaker = new D3MapMaker(this);
		AWTScheduler.getInstance().scheduleTask(mapMaker);

	}

	private class D3MapMaker extends SwingWorker<Integer, OfflineMapProgress> {

		public D3MapMaker(OfflineFileControl d3SensorControl) {
		}

		@Override
		protected Integer doInBackground() throws Exception {
			try {
				// work through the xml files in the D3 folder extracting relevant information about 
				// start and end times.  
				publish(new OfflineMapProgress(true));
				FileList fileList = new FileList();
				String[] fileEnd = new String[1];
				fileEnd[0] = getOfflineFileType();
//				fileEnd[0] = "cp1";
				ArrayList<File> files = fileList.getFileList(fileParams.offlineFolder, fileEnd, fileParams.subFolders);
				if (files == null) {
					return null;
				}
				//			System.out.println("Number of xml files found = " + files.size());
				int nDone = 0;
				int totalFiles = files.size();
				for (File aFile:files) {
					publish(new OfflineMapProgress(totalFiles, nDone));
					processMapFile(aFile, nDone);
					nDone++;
				}
				publish(new OfflineMapProgress(totalFiles, totalFiles));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;

		}


		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			super.done();
			if (mapProgressDialog != null) {
				mapProgressDialog.setVisible(false);
				mapProgressDialog = null;
			}
			PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_OFFLINE_DATASTORE);
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<OfflineMapProgress> chunks) {
			if (mapProgressDialog == null) {
				mapProgressDialog = OfflineFileProgressDialog.showDialog(getGuiFrame());
			}
			mapProgressDialog.setProgress(chunks.get(chunks.size()-1));
		}

	}

	/**
	 * Called for each file when making the data map. 
	 * Concrete class should examine the file and create 
	 * a datamap point for whatever is in there. 
	 * @param aFile
	 * @param fileIndex 
	 */
	protected abstract void processMapFile(File aFile, int fileIndex);

	/**
	 * Get the file type for the files to be listed 
	 * @return file type (e.g. xml)
	 */
	protected abstract String getOfflineFileType();
	
	/**
	 * Get the datablock - need to be abstract so that 
	 * it can be created for specific types in the concrete classes. 
	 * @param offlineFileProcess2 
	 * @return
	 */
	protected abstract PamDataBlock createOfflineDataBlock(OfflineFileProcess offlineFileProcess);

	@Override
	public String getDataSourceName() {
		return getUnitName();
	}
	

	class OfflineFileSettings implements PamSettings {

		OfflineFileControl offlineFileControl;
		public OfflineFileSettings(OfflineFileControl offlineFileControl) {
			this.offlineFileControl = offlineFileControl;
		}

		@Override
		public String getUnitName() {
			return offlineFileControl.getUnitName();
		}

		@Override
		public String getUnitType() {
			return offlineFileControl.getUnitType()+"FileControl";
		}

		@Override
		public Serializable getSettingsReference() {
			return fileParams;
		}

		@Override
		public long getSettingsVersion() {
			return OfflineFileParams.serialVersionUID;
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			fileParams = ((OfflineFileParams) pamControlledUnitSettings.getSettings()).clone();
			return true;
		}
		
	}
	

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(this.getUnitName() + " settings");
		menuItem.addActionListener(new DetectionMenuAction(parentFrame));
		return menuItem;
	}

	private class DetectionMenuAction implements ActionListener {
		private Frame parentFrame;

		public DetectionMenuAction(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			detectionMenuAction(parentFrame);
		}
	}

	public void detectionMenuAction(Frame parentFrame) {
		OfflineFileParams newParams = OfflineFilesDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			fileParams = newParams.clone();
			createOfflineDataMap(parentFrame);
		}
	}

	/**
	 * @return the offlineFileDataMap
	 */
	public OfflineFileDataMap getOfflineFileDataMap() {
		return offlineFileDataMap;
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		/*
		 * Make a list of used data map points and pass this off to an abstract function
		 * which can be overridden by concrete modules. 
		 */
		ArrayList<OfflineFileMapPoint> usedMapPoints = new ArrayList<>();
		OfflineDataMap dataMap = dataBlock.getOfflineDataMap(this);
		if (dataMap == null) {
			dataMap = offlineFileDataMap;
		}
		Iterator<OfflineFileMapPoint> mapIt = dataMap.getListIterator();
		OfflineFileMapPoint mapPoint;
		while (mapIt.hasNext()) {
			mapPoint = mapIt.next();
			if (mapPoint.getEndTime() < offlineDataLoadInfo.getStartMillis()) {
				continue;
			}
			if (mapPoint.getStartTime() > offlineDataLoadInfo.getEndMillis()) {
				break;
			}
			usedMapPoints.add(mapPoint);
		}
		
		return loadData(dataBlock, usedMapPoints, offlineDataLoadInfo,
				loadObserver);
	}

	/**
	 * Load data from the files and pack into appropriate datablocks. 
	 * @param dataBlock dataBlock for data
	 * @param usedMapPoints list of mappoints which may have data in the load time
	 * @param dataStart start of data load
	 * @param dataEnd end of data load
	 * @param cancellationObject cancellation object for threading loading
	 * @param loadObserver update observer for loading data. 
	 * @return true hopefully !
	 */
	abstract public boolean loadData(PamDataBlock dataBlock,
			ArrayList<OfflineFileMapPoint> usedMapPoints, 
			OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver);

	/**
	 * @return the fileParams
	 */
	public OfflineFileParams getFileParams() {
		return fileParams;
	}

	/**
	 * @return the offlineFileProcess
	 */
	public OfflineFileProcess getOfflineFileProcess() {
		return offlineFileProcess;
	}

	/* (non-Javadoc)
	 * @see PamController.OfflineDataStore#rewriteIndexFile(PamguardMVC.PamDataBlock, dataMap.OfflineDataMapPoint)
	 */
	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock,
			OfflineDataMapPoint dmp) {
		return false;
	}

	/* (non-Javadoc)
	 * @see PamController.OfflineDataStore#getDatagramManager()
	 */
	@Override
	public DatagramManager getDatagramManager() {
		return datagramManager;
	}

	/**
	 * @param datagramManager the datagramManager to set
	 */
	public void setDatagramManager(DatagramManager datagramManager) {
		this.datagramManager = datagramManager;
	}

	/**
	 * @param fileParams the fileParams to set
	 */
	public void setFileParams(OfflineFileParams fileParams) {
		this.fileParams = fileParams;
	}

}
