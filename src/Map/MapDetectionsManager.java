package Map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;

/**
 * Holds information about all things plotted on the map,
 * primarily how long they should plot for and whether they
 * are currently enabled or not. 
 * <br>
 * Will try to have an ever increasing list of data blocks, identified 
 * by name. 
 * 
 * @author Douglas Gillespie
 *
 */
public class MapDetectionsManager implements PamSettings {

	private SimpleMap mapControl;
	
	private MapDetectionsParameters mapDetectionsParameters = new MapDetectionsParameters();
	
	ArrayList<PamDataBlock> plottableBlocks = new ArrayList<PamDataBlock>();
	
	private int defaultTime;

	private boolean initialisationComplete;

	public MapDetectionsManager(SimpleMap mapControl) {
		super();
		this.mapControl = mapControl;
		PamSettingManager.getInstance().registerSettings(this);
		
		defaultTime = mapControl.mapParameters.dataShowTime;
	}
	
	public void notifyModelChanged(int changeType) {
		
//		if (changeType == PamController.ADD_DATABLOCK || changeType == PamController.ADD_CONTROLLEDUNIT) {
//			addDataBlock();
//		}
//		if (changeType == PamController.ADD_DATABLOCK || changeType == PamController.REMOVE_DATABLOCK) {
//			createBlockList();
//		}
		/*
		 * Need to action on add and remove controlled unit, perhaps more than adding / removing datablocks
		 * since when a new unit is added, the blocks are probably created before the unit is added, so when the 
		 * createblocksList searches units for plottable blocks, it can't actually find them !
		 */
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (initialisationComplete) {
				createBlockList();
			}
		}
	}
	
	/*
	 * See if we're already holding data about this data block 
	 * and add to list if not, with default values. 	 *
	 */
	public void createBlockList() {
		plottableBlocks = PamController.getInstance().
				getPlottableDataBlocks(mapControl.mapPanel.getRectProj()); 
		MapDetectionData mdd;
//		want to know which detection data are associated with data blocks and which 
//		are just left overs from previous existences. 
		for (int i = 0; i < mapDetectionsParameters.mapDetectionDatas.size(); i++) {
			mapDetectionsParameters.mapDetectionDatas.get(i).dataBlock = null;
		}
		for (int i = 0; i < plottableBlocks.size(); i++) {
			if ((mdd=findDetectionData(plottableBlocks.get(i))) == null) {
				addDataBlock(plottableBlocks.get(i));
			}
			else {
				mdd.dataBlock = plottableBlocks.get(i); 
			}
		}
		
		/*
		 *  really need to resort this now since the click detector has changes some block names
		 *  which means that some of the blocks dont' come out in the correct sequence since they
		 *  are added to the end. Should be able to sort in place - will also deal with re-ordering of modules.
		 *  
		 *  2021/04/26 wrap the sort procedure inside of a synch'd block.  createBlockList gets called
		 *  by ThreadedObserver.noteNewSettings, and the fact that we have now included that in the ThreadedObserver update
		 *  means it was getting called simultaneously from different threads and resulting in a
		 *  ConcurrentModificationsException error.  
		 */
		synchronized (mapDetectionsParameters.mapDetectionDatas) {
			Collections.sort(mapDetectionsParameters.mapDetectionDatas, new MapSortComparitor());
		}
	}
	
	class MapSortComparitor implements Comparator<MapDetectionData>{

		private ArrayList<PamDataBlock> allDataBlocks;

		/**
		 * 
		 */
		public MapSortComparitor() {
			super();
			allDataBlocks = PamController.getInstance().getDataBlocks();
		}

		@Override
		public int compare(MapDetectionData o1, MapDetectionData o2) {
			try {
				PamDataBlock db1 = o1.dataBlock;
				PamDataBlock db2 = o2.dataBlock;
				int ind1 = allDataBlocks.indexOf(db1);
				int ind2 = allDataBlocks.indexOf(db2);
				return ind1-ind2;
			}
			catch (Exception e) {
				return 0;
			}
		}
		
	}

	
	private void addDataBlock(PamDataBlock pamDataBlock) {
		MapDetectionData mdd = new MapDetectionData(new String(pamDataBlock.getLongDataName()));
//		mdd.dataName = new String(pamDataBlock.getLongDataName());
		mdd.setDisplayMilliseconds(mapControl.mapParameters.dataShowTime * 1000);
//		mdd.forcedStart = 0;
		mdd.select = false;
		mdd.dataBlock = pamDataBlock;
		addDetectionData(mdd);
	}
	
	public MapDetectionData findDetectionData(PamDataBlock pamDataBlock) {
		return findDetectionData(pamDataBlock.getLongDataName());
	}
	
	private MapDetectionData findDetectionData(String dataName) {
		int n = mapDetectionsParameters.mapDetectionDatas.size();
		for (int i = 0; i < n; i++) {
			if (dataName.equals(mapDetectionsParameters.mapDetectionDatas.get(i).dataName)){
				return mapDetectionsParameters.mapDetectionDatas.get(i);
			}
		}
		MapDetectionData newMDD = new MapDetectionData(dataName);
		mapDetectionsParameters.mapDetectionDatas.add(newMDD);
		return newMDD;
	}
	
	private void addDetectionData(MapDetectionData mapDetectionData) {
		mapDetectionsParameters.mapDetectionDatas.add(mapDetectionData);
	}
	
	public ArrayList<MapDetectionData> getMapDetectionDatas() {
		return mapDetectionsParameters.mapDetectionDatas;
	}
	
	public void setShouldPlot(String pamDataBlock, boolean shouldPlot) {
		MapDetectionData mapDetectionData = findDetectionData(pamDataBlock);
		if (mapDetectionData == null) {
			return;
		}
		mapDetectionData.select = shouldPlot;
	}
	
	public void setShouldPlot(PamDataBlock pamDataBlock, boolean shouldPlot) {
		setShouldPlot(pamDataBlock.getLongDataName(), shouldPlot);
	}
	
	public boolean isShouldPlot(String pamDataBlock) {
		MapDetectionData mapDetectionData = findDetectionData(pamDataBlock);
		if (mapDetectionData == null) {
			return false;
		}
		return mapDetectionData.select;
	}
	
	public boolean isShouldPlot(PamDataBlock pamDataBlock) {
		return isShouldPlot(pamDataBlock.getLongDataName());
	}

	/**
	 * functions for storing of settings ... 
	 */
	
	@Override
	public Serializable getSettingsReference() {
		return mapDetectionsParameters;
	}

	@Override
	public long getSettingsVersion() {
		return MapDetectionsParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return mapControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Map Detections Manager";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mapDetectionsParameters = ((MapDetectionsParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	

	public MapDetectionsParameters getMapDetectionsParameters() {
		return mapDetectionsParameters;
	}

	public void setMapDetectionsParameters(
			MapDetectionsParameters mapDetectionsParameters) {
		this.mapDetectionsParameters = mapDetectionsParameters;
	}

	public int getDefaultTime() {
		return defaultTime;
	}

	public void setDefaultTime(int defaultTime) {
		this.defaultTime = defaultTime;
	}
}
