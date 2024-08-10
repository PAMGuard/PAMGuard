package effort.binary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import PamController.PamController;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import effort.EffortDataUnit;
import effort.EffortProvider;

public class DataMapEffortProvider extends EffortProvider {
	
	private DataMapSymbolManager binarySymbolManager;
	
	private Class dataStoreClass;
	
	private long maxGap = 0;

	/**
	 * 
	 * @param parentDataBlock
	 * @param dataStoreClass
	 * @param maxGapMillis
	 */
	public DataMapEffortProvider(PamDataBlock parentDataBlock, Class dataStoreClass, long maxGapMillis) {
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

	@Override
	public EffortDataUnit getEffort(long timeMilliseconds) {
		OfflineDataMap dataMap = findBinaryMap();
		if (dataMap == null) {
			return null;
		}
		OfflineDataMapPoint foundPt = dataMap.findMapPoint(timeMilliseconds);
		if (foundPt != null) {
			return new DataMapEffortThing(this, getParentDataBlock(), foundPt.getStartTime(), foundPt.getEndTime());
		}
		return null;
	}

	@Override
	public List<EffortDataUnit> getAllEffortThings() {
		// should merge continuous binary data into one big lump or several big lumps if duty cycled. 		
		OfflineDataMap dataMap = findBinaryMap();
		if (dataMap == null) {
			return null;
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
		
		return allPoints;
	}
	
	private OfflineDataMap findBinaryMap() {
		if (dataStoreClass == null) {
			return getParentDataBlock().getPrimaryDataMap();
		}
		BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(dataStoreClass, null);
		return getParentDataBlock().getOfflineDataMap(binaryStore);
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

}
