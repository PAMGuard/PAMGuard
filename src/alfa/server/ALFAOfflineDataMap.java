package alfa.server;

import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;

public class ALFAOfflineDataMap extends OfflineDataMap<ALFADataMapPoint>{

	public ALFAOfflineDataMap(ALFAServerLoader alfaServerLoader, PamDataBlock parentDataBlock) {
		super(alfaServerLoader, parentDataBlock);
	}


}
