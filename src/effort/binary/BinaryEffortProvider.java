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

public class BinaryEffortProvider extends EffortProvider {
	
	private BinarySymbolManager binarySymbolManager;

	public BinaryEffortProvider(PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		binarySymbolManager = new BinarySymbolManager(parentDataBlock);
	}

	@Override
	public EffortDataUnit getEffort(long timeMilliseconds) {
		OfflineDataMap dataMap = findBinaryMap();
		if (dataMap == null) {
			return null;
		}
		OfflineDataMapPoint foundPt = dataMap.findMapPoint(timeMilliseconds);
		if (foundPt != null) {
			return new BinaryEffortThing(getParentDataBlock(), foundPt.getStartTime(), foundPt.getEndTime());
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
		BinaryEffortThing currentThing = null;
		while (it.hasNext()) {
			OfflineDataMapPoint pt = it.next();
			if (currentThing == null || pt.getStartTime() > currentThing.getEffortEnd()) {
				currentThing = new BinaryEffortThing(getParentDataBlock(), pt.getStartTime(), pt.getEndTime());
				allPoints.add(currentThing);
			}
			else {
				currentThing.setEffortEnd(pt.getEndTime());
			}
		}
		
		return allPoints;
	}
	
	private OfflineDataMap findBinaryMap() {
		BinaryStore binaryStore = (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.class, null);
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
