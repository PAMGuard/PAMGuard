package dataMap;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsSource;
import PamView.PamTabPanel;
import PamView.PamView;
import PamView.WrapperControlledGUISwing;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
import dataMap.layoutFX.DataMapGUIFX;
import generalDatabase.DBControlUnit;
import pamScrollSystem.AbstractScrollManager;

/**
 * The data map appears only during PAMGUARD viewer mode and is the root of
 * a navigation system allowing the operator to scroll easily through large
 * amounts of data. 
 * <p>
 * The primary role of the map is to show a time line of data rate for each 
 * module, which may extend from minutes to years, and to relate that map to 
 * the positions of the various scroll bars used to navigate through the data. 
 * <p>
 * Data may be loaded from the database and the binary storage system. 
 * Data in each will have been mapped into either hour units or units representing
 * the individual files in the binary store. 
 * <p>
 *  
 * @author Doug Gillespie
 * @see DBControlUnit
 * @see BinaryStore
 * @see OfflineDataMap
 * @see OfflineDataMapPoint 
 */
public class DataMapControl extends PamControlledUnit implements PamSettings {

	///this should be in the swing GUI. 
	private DataMapPanel dataMapPanel;

	public DataMapParameters dataMapParameters = new DataMapParameters();

	private boolean initialisationComplete = false;

	private ArrayList<PamDataBlock> mappedDataBlocks;
	
	private static DataMapControl dataMapControl;

	private long firstTime;
	private long lastTime;
//	private long firstDatabaseTime, lastDatabaseTime;
//	private long firstBinaryTime, lastBinaryTime;

	/**
	 * @param firstTime the firstTime to set
	 */
	public void setFirstTime(long firstTime) {
		this.firstTime = firstTime;
	}

	/**
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	/**
	 * The FX GUI for the data map
	 */
	private DataMapGUIFX dataGramGUIFX;

	/**
	 * The Swing GUI for the datagram. 
	 */
	private WrapperControlledGUISwing dataGramGUISwing;

	public DataMapControl(String unitName) {
		super("Data map", unitName);
		
		dataMapPanel = new DataMapPanel(this);
		PamSettingManager.getInstance().registerSettings(this);
		dataMapPanel.newSettings();
		
		dataMapControl = this;
	}

	public static DataMapControl getDataMapControl() {
		return dataMapControl;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#setPamView(PamView.PamView)
	 */
	@Override
	public void setPamView(PamView pamView) {
		super.setPamView(pamView);
		if (pamView != null && pamView.getGuiFrame() != null) {
			pamView.getGuiFrame().addComponentListener(new FrameListener());
		}
	}

	private class FrameListener extends ComponentAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentResized(ComponentEvent arg0) {
			//			frameResized();
			SwingUtilities.invokeLater(new ResizeLater());
		}

	}

	class ResizeLater implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			frameResized();
		}

	}

	public void frameResized() {
		dataMapPanel.frameResized();
	}

	@Override
	public PamTabPanel getTabPanel() {
		if (dataMapPanel == null) {
			dataMapPanel = new DataMapPanel(this);
		}
		return dataMapPanel;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
//			System.out.println("DataMap notification " + changeType);
			initialisationComplete = true;
			break;
		case PamControllerInterface.CHANGED_OFFLINE_DATASTORE:
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.INITIALIZE_LOADDATA:
		case PamControllerInterface.EXTERNAL_DATA_IMPORTED:
			if (initialisationComplete) {
//				System.out.println("DataMap notification " + changeType);
				if (getDataMapGUI() == null) {
					break;
				}
				findDataSources();
				dataMapPanel.createDataGraphs();
				getDataMapGUI().createDataGraphs();
				dataMapPanel.repaintAll();
			}
//			break;
		case PamControllerInterface.OFFLINE_DATA_LOADED:
//			System.out.println("DataMap notification " + changeType);
			dataMapPanel.repaintAll();
		}
		
		if (dataGramGUIFX!=null) {
			dataGramGUIFX.notifyModelChanged(changeType); 
		}
	}

	private void findDataSources() {
		if (getPamController() == null) {
			return;
		}
		
		/*
		 * Create a list of PamDataBlocks that have a database or a binary map
		 */
		ArrayList<PamDataBlock> allBlocks = PamController.getInstance().getDataBlocks();
		mappedDataBlocks = new ArrayList<PamDataBlock>();
		PamDataBlock aBlock;
		firstTime = Long.MAX_VALUE;
		lastTime = Long.MIN_VALUE;
		int nMaps; 
		OfflineDataMap aMap;
		for (int i = 0; i < allBlocks.size(); i++) {
			aBlock = allBlocks.get(i);
			nMaps = aBlock.getNumOfflineDataMaps();
			if (nMaps > 0) {
				mappedDataBlocks.add(aBlock);
			}
			for (int iMap = 0; iMap < nMaps; iMap++) {
				aMap = aBlock.getOfflineDataMap(iMap);
				firstTime = Math.min(firstTime, aMap.getFirstDataTime());
				
				//don't let crazy times from corrupted data make giant datamaps
				if (aMap.getLastDataTime()<PamConstants.MAX_DATE_TIME) {
					lastTime = Math.max(lastTime, aMap.getLastDataTime());
				}
			}
		}
		// also check settings for first and last times. 
		ArrayList<PamSettingsSource> settingsSources = PamController.getInstance().findSettingsSources();
		PamSettingsSource aSettingsSource;
		int nSets;
		for (int i = 0; i < settingsSources.size(); i++) {
			aSettingsSource = settingsSources.get(i);
			if ((nSets=aSettingsSource.getNumSettings()) > 0) {
				
				firstTime = Math.min(firstTime, aSettingsSource.getSettings(0).getSettingsTime());
				
				//don't let crazy times from corrupted data make giant datamaps
				/* 
				 * ALSO, DON'T ALLOW REANALYSIS TIME IN THE FAR FUTURE OVER EXTEND THE DISPLAY TIME
				 * SINCE WHEN ANALYSING OLD DATA, IT'S INCREASINGLY SQUISHING THE SCROLL BAR AND MAKING IT HARD TO USE 
				 * SO SET A MAX TIME WHICH IS ABOUT 10% BIGGER THAN THE MAX IN THE DATA. 
				 */
				long maxTime = lastTime + (lastTime-firstTime)/10;
//				if (aSettingsSource.getSettings(nSets-1).getSettingsTime()<PamConstants.MAX_DATE_TIME) {
//				if (aSettingsSource.getSettings(nSets-1).getSettingsTime()<maxTime) {
					lastTime = Math.max(lastTime, Math.min(maxTime, aSettingsSource.getSettings(nSets-1).getSettingsTime()));
//				}
			}
		}

		dataMapPanel.newDataSources();
	}

	public ArrayList<PamDataBlock> getMappedDataBlocks() {
		return mappedDataBlocks;
	}
	
	/**
	 * Update a single data map. Useful when bringing in data 
	 * from 'non standard' sources. Check overall time limits and 
	 * redraw, but no total map creation. 
	 * @param singleDataMap
	 */
	public void updateSingleDataMap(OfflineDataMap<?> singleDataMap) {
		long newLastTime = singleDataMap.getLastDataTime();
		long newFirstTime = singleDataMap.getFirstDataTime();
		if (mappedDataBlocks == null) {
			return;
		}
		for (PamDataBlock aBlock : mappedDataBlocks) {
			int nMaps = aBlock.getNumOfflineDataMaps();
			for (int iMap = 0; iMap < nMaps; iMap++) {
				OfflineDataMap aMap = aBlock.getOfflineDataMap(iMap);
				newFirstTime = Math.min(newFirstTime, aMap.getFirstDataTime());
				newLastTime = Math.max(newLastTime, aMap.getLastDataTime());
			}
		}
		firstTime = newFirstTime;
		lastTime = newLastTime;
		dataMapPanel.repaintAll();
		dataMapPanel.getSummaryPanel().newDataSources();
	}

	/**
	 * @return the firstTime for any data in any data block
	 */
	public long getFirstTime() {
		return firstTime;
	}

	/**
	 * @return the lastTime for any data in any data block
	 */
	public long getLastTime() {
		return lastTime;
	}
	
	
	
	/**
	 * Get the start and end times for all data associated with 
	 * a particular data source
	 * @param dataSource datasource
	 * @return start and end times in milliseconds. 
	 */
	public long[] getDataExtent(OfflineDataStore dataSource) {
		long[] extent = new long[2];
		extent[0] = Long.MAX_VALUE;
		extent[1] = Long.MIN_VALUE;
		OfflineDataMap aMap;
		for (int i = 0; i < mappedDataBlocks.size(); i++) {
			aMap = mappedDataBlocks.get(i).getOfflineDataMap(dataSource);
			if (aMap == null) {
				continue;
			}
			long lastTime;
			extent[0] = Math.min(extent[0], aMap.getFirstDataTime());
			
			//don't let crazy times from corrupted data make giant datamaps
			if (aMap.getLastDataTime()<PamConstants.MAX_DATE_TIME) {
				extent[1] = Math.max(extent[1], lastTime=aMap.getLastDataTime());
			}
			

			
//			if (lastTime > System.currentTimeMillis()) {
//				System.out.println("Stupid large time in block " + mappedDataBlocks.get(i).getDataName());
//				aMap.getLastDataTime();
//			}
		}
		return extent;
	}
//
//	/**
//	 * @return the firstDatabaseTime
//	 */
//	public long getFirstDatabaseTime() {
//		return firstDatabaseTime;
//	}
//
//	/**
//	 * @return the lastDatabaseTime
//	 */
//	public long getLastDatabaseTime() {
//		return lastDatabaseTime;
//	}
//
//	/**
//	 * @return the firstBinaryTime
//	 */
//	public long getFirstBinaryTime() {
//		return firstBinaryTime;
//	}
//
//	/**
//	 * @return the lastBinaryTime
//	 */
//	public long getLastBinaryTime() {
//		return lastBinaryTime;
//	}

	@Override
	public Serializable getSettingsReference() {
		return dataMapParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DataMapParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		dataMapParameters = ((DataMapParameters) pamControlledUnitSettings.getSettings()).clone();
		return (dataMapParameters != null);
	}

	/**
	 * Centre the data in all data streams at the given time
	 * @param dataBlock data block graph sending the command
	 * @param menuMouseTime time in milliseconds. 
	 */
	public void centreDataAt(PamDataBlock dataBlock, long menuMouseTime) {
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		if (scrollManager == null) {
			return;
		}
		scrollManager.centreDataAt(dataBlock, menuMouseTime);
	}

	/**
	 * Start the data in all data streams at the given time
	 * @param dataBlock data block graph sending the command
	 * @param menuMouseTime time in milliseconds. 
	 */
	public void startDataAt(PamDataBlock dataBlock, long menuMouseTime) {
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		if (scrollManager == null) {
			return;
		}
		scrollManager.startDataAt(dataBlock, menuMouseTime);
	}

	public void scrollToData(PamDataBlock dataBlock) {
		dataMapPanel.scrollingDataPanel.scrollToData(dataBlock);
	}
	

	public DataMapControlGUI getDataMapGUI() {
		return (DataMapControlGUI) getGUI(PamGUIManager.getGUIType()); 
	}
	
	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options 
	 * which are instantiated depending on the view type. 
	 * @param flag. The GUI type flag defined in PAMGuiManager. 
	 * @return the GUI for the PamControlledUnit unit. 
	 */
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (dataGramGUIFX ==null) {
				dataGramGUIFX= new DataMapGUIFX(this);
			}
			return dataGramGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (dataGramGUISwing ==null) {
				dataGramGUISwing= new DataMapGUISwing(this);	
			}
			return dataGramGUISwing;
		}
		return null;
	}
	
	/**
	 * Class to add some functions for eventual swing GUI. 
	 * @author Jamie Macaulay 
	 *
	 */
	class DataMapGUISwing extends WrapperControlledGUISwing implements DataMapControlGUI {

		public DataMapGUISwing(PamControlledUnit pamControlledUnit) {
			super(pamControlledUnit);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void newDataSources() {
			// TODO Auto-generated method stub
		}

		@Override
		public void createDataGraphs() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void repaintAll() {
			// TODO Auto-generated method stub
			
		}
		
	}


}
