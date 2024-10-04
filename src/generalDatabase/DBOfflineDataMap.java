package generalDatabase;

import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;

public class DBOfflineDataMap extends OfflineDataMap<DBOfflineDataMapPoint> {

	private DBControlUnit databaseControl;
	
	public DBOfflineDataMap(DBControlUnit databaseControl, PamDataBlock parentDataBlock) {
		super(databaseControl, parentDataBlock);
		this.databaseControl = databaseControl;
	}

}
