package generalDatabase;

import dataMap.OfflineDataMap;
import PamguardMVC.PamDataBlock;

public class DBOfflineDataMap extends OfflineDataMap<DBOfflineDataMapPoint> {

	private DBControlUnit databaseControl;
	
	public DBOfflineDataMap(DBControlUnit databaseControl, PamDataBlock parentDataBlock) {
		super(databaseControl, parentDataBlock);
		this.databaseControl = databaseControl;
	}

}
