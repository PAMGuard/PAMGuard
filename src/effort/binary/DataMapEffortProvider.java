package effort.binary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import effort.EffortDataUnit;
import effort.EffortProvider;

/**
 * Effort provider for most types of datablock that bases itself off one of the
 * datamaps, binary (preferred) or database. Binary better since database doesn't really 
 * have proper effort data for most blocks.
 * However, we want this to work online too, so will need to have quite different functionality
 * for real time and viewer operations. 
 * @author dg50
 *
 */
public class DataMapEffortProvider extends EffortProvider {
	
	private DataMapSymbolManager binarySymbolManager;
	
	private Class<?> dataStoreClass;
	
	private long maxGap = 0;
	
	private ArrayList<EffortDataUnit> allEfforts;

	/**
	 * 
	 * @param parentDataBlock
	 * @param dataStoreClass
	 * @param maxGapMillis
	 */
	public DataMapEffortProvider(PamDataBlock parentDataBlock, Class<?> dataStoreClass, long maxGapMillis) {
		super(parentDataBlock);
		this.dataStoreClass = dataStoreClass;
		this.maxGap = maxGapMillis;
		binarySymbolManager = new DataMapSymbolManager(parentDataBlock);
	}
	
	/**
	 * 
	 * @param parentDataBlock
	 * @param dataStoreClass
	 */
	public DataMapEffortProvider(PamDataBlock parentDataBlock, Class dataStoreClass) {
		this(parentDataBlock, dataStoreClass, 0);
	}

	@Override
	public String getName() {
		return getParentDataBlock().getDataName();
	}

//	@Override
//	public EffortDataUnit getEffort(long timeMilliseconds) {
//		OfflineDataMap dataMap = findDataMap();
//		if (dataMap == null) {
//			return null;
//		}
//		OfflineDataMapPoint foundPt = dataMap.findMapPoint(timeMilliseconds);
//		if (foundPt != null) {
//			return new DataMapEffortThing(this, getParentDataBlock(), foundPt.getStartTime(), foundPt.getEndTime());
//		}
//		return null;
//	}

	@Override
	public List<EffortDataUnit> getAllEffortThings() {
		return allEfforts;		
	}
	
	private OfflineDataMap findDataMap() {
		if (dataStoreClass == null) {
			return getParentDataBlock().getPrimaryDataMap();
		}
		try {
			OfflineDataStore dataStore = (OfflineDataStore) PamController.getInstance().findControlledUnit(dataStoreClass, null);
			if (dataStore != null) {
				return getParentDataBlock().getOfflineDataMap(dataStore);
			}
		}
		catch (Exception e) {
		}
		return null;
	}

	@Override
	public DataSelector getDataSelector(String selectorName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamSymbolManager getSymbolManager() {
		return binarySymbolManager;
	}

	@Override
	public void realTimeStart(long timeMilliseconds) {
		if (allEfforts == null) {
			allEfforts = new ArrayList<>();
		}
		EffortDataUnit newEffort = new EffortDataUnit(this, null, timeMilliseconds, EffortDataUnit.ONGOINGEFFORT);
		allEfforts.add(newEffort);
	}

	@Override
	public void realTimeStop(long timeMilliseconds) {
		if (allEfforts == null || allEfforts.size() == 0) {
			return;
		}
		EffortDataUnit lastEff = allEfforts.get(allEfforts.size()-1);
		lastEff.setEffortEnd(timeMilliseconds);
		
	}

	@Override
	public void newData(PamDataUnit pamDataUnit) {
		// do nothing for individual data units. 
	}

	@Override
	public void viewerLoadData() {
		// should merge continuous binary data into one big lump or several big lumps if duty cycled. 		
		OfflineDataMap dataMap = findDataMap();
		if (dataMap == null) {
			return;
		}
		ArrayList<EffortDataUnit> allPoints = new ArrayList<>();
		Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
		DataMapEffortThing currentThing = null;
		while (it.hasNext()) {
			OfflineDataMapPoint pt = it.next();
			if (currentThing == null || pt.getStartTime() - currentThing.getEffortEnd() >  maxGap) {
				currentThing = new DataMapEffortThing(this, getParentDataBlock(), pt.getStartTime(), pt.getEndTime());
				allPoints.add(currentThing);
			}
			else {
				currentThing.setEffortEnd(pt.getEndTime());
			}
		}
		
		allEfforts = allPoints;
		
	}

}
