package dataMap.filemaps;

import java.awt.Window;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

//import org.kc7bfi.jflac.FLACDecoder;
//import org.kc7bfi.jflac.PCMProcessor;
//import org.kc7bfi.jflac.metadata.Metadata;
//import org.kc7bfi.jflac.metadata.StreamInfo;
//import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
//import org.kc7bfi.jflac.util.ByteData;

import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.Metadata;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.util.ByteData;

import dataGram.DatagramManager;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamScrollSystem.ViewLoadObserver;
import PamController.AWTScheduler;
import PamController.DataInputStore;
import PamController.DataStoreInfoHolder;
import PamController.InputStoreInfo;
import PamController.OfflineDataStore;
import PamController.OfflineFileDataStore;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.worker.PamWorkMonitor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * Functionality for handling data from files offline.
 * Was just for sound files, now made more generic for any file type. 
 * @author Doug Gillespie
 *
 */
public abstract class OfflineFileServer<TmapPoint extends FileDataMapPoint> implements OfflineDataStore, PamSettings, DataStoreInfoHolder {

	private OfflineFileDataStore offlineRawDataStore;

	private OfflineFileParameters offlineFileParameters = new OfflineFileParameters();

	private OfflineDataMap<TmapPoint> dataMap;

	private PamDataBlock rawDataBlock;

	private FileMapMakingdialog mapDialog;

	private MapMaker mapMaker;

	private List<TmapPoint> existingMapPoints;

	/**
	 * @param offlineRawDataStore master controlled unit - acquisition or decimator usually. 
	 * @param fileDate file date - passed around so that settings can be shared. 
	 */
	public OfflineFileServer(OfflineFileDataStore offlineRawDataStore) {
		super();
		this.offlineRawDataStore = offlineRawDataStore;
		rawDataBlock = offlineRawDataStore.getRawDataBlock();
		PamSettingManager.getInstance().registerSettings(this);
//		dataMap = new WavFileDataMap(this, rawDataBlock);
		dataMap = createDataMap(this, rawDataBlock);
		rawDataBlock.addOfflineDataMap(dataMap);
	}
	
	public abstract OfflineDataMap<TmapPoint> createDataMap(OfflineFileServer<TmapPoint> offlineFileServer, PamDataBlock pamDataBlock);

	@Override
	public void createOfflineDataMap(Window parentFrame) {
		if ((PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW)) {
			return;
		}
		
		dataMap.clear();
		/**
		 * then search through all files and folders, checking the 
		 * size and duration of every file (could take quite some time !)
		 */
		if (offlineFileParameters.enable == false) {
			System.out.println("Offline Sound Files are not enabled: " + offlineFileParameters.enable);
			return;
		}
		mapMaker = new MapMaker(this);
		AWTScheduler.getInstance().scheduleTask(mapMaker);

	}

	/**
	 * Create the offline data map. 
	 */
	public void createOfflineDataMap() {
		createOfflineDataMap(null);
	}
		

	protected class MapMaker extends SwingWorker<Integer, FileMapProgress> {

		private OfflineFileServer fileServer;

		public MapMaker(OfflineFileServer offlineFileServer) {
			fileServer = offlineFileServer;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			System.out.println("Offline file server:  start map making ");
			try {
				mapMaker.pPublish(new FileMapProgress(FileMapProgress.STATE_LOADINGMAP, 0, 0, ""));
				loadSerialisedMap();
				long t = System.currentTimeMillis();
				addToMap(new File(offlineFileParameters.folderName), offlineFileParameters.includeSubFolders);
				dataMap.sortMapPoints();
				long t2 = System.currentTimeMillis();
				mapMaker.pPublish(new FileMapProgress(FileMapProgress.STATE_DONECOUNTINGFILES, dataMap.getNumMapPoints(), 0, ""));
				/**
				 * Now have a list of files in the folder + a previous list read in from the 
				 * serialised data file. Compare the two, then patch up the old list with 
				 * new additions. 
				 */
				if (existingMapPoints != null) {
					dataMap.compareLists(dataMap.getMapPoints(), existingMapPoints);
					// next remove any unlinked points from existingMapPoints:
					Iterator<TmapPoint> it = existingMapPoints.iterator();
					while (it.hasNext()) {
						TmapPoint mp = it.next();
						if (mp.getMatchedPoint() == null) {
							it.remove();
						}
					}
					// then add any unlinked points from the new map
					it = dataMap.getListIterator();
					while (it.hasNext()) {
						TmapPoint mp = it.next();
						if (mp.getMatchedPoint() == null) {
							existingMapPoints.add(mp);
						}
						else {
							((TmapPoint) mp.getMatchedPoint()).setSoundFile(mp.getSoundFile());
						}
					}
					// now sort the existing list
					Collections.sort(existingMapPoints);
					// now put this list in in place of the newer list
					dataMap.setMapPoints(existingMapPoints);
				}

				
				sortMapEndTimes(mapMaker);
				mapMaker.pPublish(new FileMapProgress(FileMapProgress.STATE_CHECKINGFILES, dataMap.getDataCount(), dataMap.getDataCount(), ""));
				
				long t3 = System.currentTimeMillis();
//				System.out.println("Listing files took " + new Long(t2-t) + " millis");
//				System.out.println("Getting file times took " + new Long(t3 - t2) + " millis");
				mapMaker.pPublish(new FileMapProgress(FileMapProgress.STATE_SAVINGMAP, 0, 0, ""));
				saveSerialisedMap();
			}
			catch (Exception e) {
				mapDialog.setVisible(false);
				e.printStackTrace();
			}
			return 0;
		}

		public void pPublish(FileMapProgress mapProgress) {
			publish(mapProgress);
		}

		@Override
		protected void done() {
			if (PamGUIManager.isSwing()) {
				//TODO - messy, should implement in the task update in the controller and then through
				//the GUI. 
				if (mapDialog != null) {
					mapDialog.setVisible(false);
				}
			}


			/**
			 * 2025/03 - Very strange bug. whenever the Offline Sound data is loaded the
			 * whole of PAMGuard freezes. There are no thread locks, no infinite loops, no
			 * recursion etc. the bug is intermittent but removing notifymodelChnaged seems to
			 * sort it out. Have put the notifyModelChanged with invokeLater as an attempt to 
			 * sort this but not ideal. 
			 */
			SwingUtilities.invokeLater(()->{			
				PamController.getInstance().notifyTaskProgress(
						new FileMapProgress(FileMapProgress.STATUS_DONE, 0, 0, ""));
	
				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_OFFLINE_DATASTORE);
			});
			
			//this is how progress should be passed to the GUI?
			System.out.println("The offline wav file map has finished loading 2: ");

		}

		@Override
		protected void process(List<FileMapProgress> chunks) {
			if (PamGUIManager.isSwing()) {
				//TODO- messy, should implement task update in the controller and access
				//GUI from there. 
			if (mapDialog == null || mapDialog.isVisible() == false) {
				mapDialog = FileMapMakingdialog.showDialog(PamController.getMainFrame(), fileServer);;
			}
			mapDialog.setProgress(chunks.get(chunks.size()-1));
			}
			else {
				//this is how progress should be passed to the GUI
				PamController.getInstance().notifyTaskProgress(chunks.get(chunks.size()-1));
			}
			
		}

	}
	
	/**
	 * Get a file filter for the types of files in use. 
	 * @return file filter. 
	 */
	public abstract FileFilter getFileFilter();

	private void addToMap(File folderName, boolean includeSubFolders) {
		FileFilter audioFileFilter = getFileFilter();
		File[] files = folderName.listFiles(audioFileFilter);
		if (files == null) return;
		File file;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			if (file.isDirectory() && includeSubFolders) {
				//				System.out.println(file.getAbsoluteFile());
				addToMap(file.getAbsoluteFile(), includeSubFolders);
			}
			else if (file.isFile()) {
				addToMap(file);
			}
		}
	}

	public boolean loadSerialisedMap() {
		File mapFile = new File(getMapName());
		if (mapFile.exists() == false) {
			return false;
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapFile));
			existingMapPoints = (List<TmapPoint>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error loading sound file map: " + e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public void saveSerialisedMap() {
		File mapFile = new File(getMapName());
		if (dataMap == null) {
			if (mapFile.exists()) {
				mapFile.delete();
			}
			return;
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mapFile));
			oos.writeObject(dataMap.getMapPoints());
		} catch (IOException e) {
			System.err.println("Error saving sound file map: " + e.getLocalizedMessage());
		}		
	}

	private String getMapName() {
		String mapName = offlineFileParameters.folderName + File.separator + "serialisedSoundFileMap.data";
		return mapName;
	}
	/**
	 * Add a single sound file to the data map 
	 * @param file
	 */
	protected void addToMap(File file) {
		/* first need to try to get the time from the file name...
		 * Don't do this here - just build the list of files
		 * first, then go through and open all the files to get their durations 
		 * 
		 */
		long[] fileTimes = getFileStartandEndTime(file);
		long startTime = 0, endTime = 0;
		if (fileTimes != null) {
			if (fileTimes.length > 0) {
				startTime = fileTimes[0];
			}
			if (fileTimes.length > 1) {
				endTime = fileTimes[1];
			}
		}
		
		TmapPoint mapPoint = createMapPoint(file, startTime, endTime);
		if (mapPoint != null) {
			dataMap.addDataPoint(dataMap.castMapPoint(mapPoint));
		}

		mapMaker.pPublish(new FileMapProgress(FileMapProgress.STATE_COUNTINGFILES, dataMap.getNumMapPoints(), 0, file.getName()));

	}
	
	/**
	 * Get the file start and end times through whatever means are required. 
	 * @param file
	 * @return 2 element of times. Fill with 0 if no times available. 
	 */
	public abstract long[] getFileStartandEndTime(File file);
	
	/**
	 * Create the type specific data map point.
	 * @param file
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public abstract TmapPoint createMapPoint(File file, long startTime, long endTime);
	
	/**
	 * Get the end times of map points. In most cases the data will have come back from 
	 * the serialised file, so will already have this information so it can be skipped. 
	 * @param mapMaker 
	 */
	public abstract void sortMapEndTimes(OfflineFileServer.MapMaker mapMaker);
	
	@Override
	public String getDataSourceName() {
		// TODO Auto-generated method stub
		return "Sound Files";
	}

	@Override
	public String getDataLocation() {
		getOfflineFileParameters();
		return offlineFileParameters.folderName;
	}
	
	public TmapPoint findFirstMapPoint(Iterator<TmapPoint> mapIterator, long startMillis, long endMillis) {
		TmapPoint mapPoint, prevMapPoint = null;
		while (mapIterator.hasNext()) {
			mapPoint = mapIterator.next();
			if (mapPoint.getEndTime() < startMillis) {
				prevMapPoint = mapPoint;
				continue;
			}
			else if (mapPoint.getStartTime() > endMillis) {
				return null;
			}
			return mapPoint;
		}
		return null;
	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		// will never have to do this
		return false;
	}

	@Override
	public Serializable getSettingsReference() {
		return offlineFileParameters;
	}

	@Override
	public long getSettingsVersion() {
		return OfflineFileParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return offlineRawDataStore.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Offline sound file server";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		OfflineFileParameters n = (OfflineFileParameters) pamControlledUnitSettings.getSettings();
		offlineFileParameters = n.clone();
		return true;
	}

	/**
	 * @return the offlineFileParameters
	 */
	public OfflineFileParameters getOfflineFileParameters() {
		return offlineFileParameters;
	}

	/**
	 * @param offlineFileParameters the offlineFileParameters to set
	 */
	public void setOfflineFileParameters(OfflineFileParameters offlineFileParameters) {
		this.offlineFileParameters = offlineFileParameters;
	}

	/**
	 * @return the dataMap
	 */
	public OfflineDataMap<TmapPoint> getDataMap() {
		return dataMap;
	}

	/**
	 * @return the rawDataBlock
	 */
	public PamDataBlock getRawDataBlock() {
		return rawDataBlock;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock,
			OfflineDataMapPoint dmp) {
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the offlineRawDataStore
	 */
	public OfflineFileDataStore getOfflineRawDataStore() {
		return offlineRawDataStore;
	}


	@Override
	public InputStoreInfo getStoreInfo(PamWorkMonitor workMonitor, boolean detail) {
		if (dataMap == null || dataMap.getNumMapPoints() == 0) {
			return null;
		}
		long firstStart = dataMap.getFirstDataTime();
		long lastEnd = dataMap.getLastMapPoint().getEndTime();
		long lastStart = dataMap.getLastMapPoint().getStartTime();
		InputStoreInfo info = new InputStoreInfo(this, dataMap.getNumMapPoints(), firstStart, lastStart, lastEnd);
		if (detail) {
			// get all times of all files 
			long[][] startsEnds = dataMap.getAllStartsAndEnds();
			if (startsEnds != null) {
				info.setFileStartTimes(startsEnds[0]);
				info.setFileEndTimes(startsEnds[1]);
			}
		}
		return info;
	}


}
