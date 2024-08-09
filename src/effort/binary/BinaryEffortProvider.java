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
import effort.EffortDataThing;
import effort.EffortProvider;

public class BinaryEffortProvider extends EffortProvider {

	public BinaryEffortProvider(PamDataBlock parentDataBlock) {
		super(parentDataBlock);
		
	}

	@Override
	public EffortDataThing getEffort(long timeMilliseconds) {
		OfflineDataMap dataMap = findBinaryMap();
		if (dataMap == null) {
			return null;
		}
		OfflineDataMapPoint foundPt = dataMap.findMapPoint(timeMilliseconds);
		if (foundPt != null) {
			return new BinaryEffortThing(foundPt.getStartTime(), foundPt.getEndTime());
		}
		return null;
	}

	@Override
	public List<EffortDataThing> getAllEffortThings() {
		// should merge continuous binary data into one big lump or several big lumps if duty cycled. 		
		OfflineDataMap dataMap = findBinaryMap();
		if (dataMap == null) {
			return null;
		}
		ArrayList<EffortDataThing> allPoints = new ArrayList<>();
		Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
		BinaryEffortThing currentThing = null;
		while (it.hasNext()) {
			OfflineDataMapPoint pt = it.next();
			if (currentThing == null || pt.getStartTime() > currentThing.getEffortEnd()) {
				currentThing = new BinaryEffortThing(pt.getStartTime(), pt.getEndTime());
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
		// TODO Auto-generated method stub
		return null;
	}

}
