package Acquisition;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import dataMap.OfflineDataMap;
import effort.EffortProvider;
import effort.binary.DataMapEffortProvider;
import generalDatabase.DBControlUnit;

public class DaqStatusDataBlock extends PamDataBlock<DaqStatusDataUnit> {

	public DaqStatusDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(DaqStatusDataUnit.class, dataName, parentProcess, channelMap);
	}

	@Override
	public EffortProvider autoEffortProvider() {
		// make a provider which will pick up on the database data. 
		// db data are written every 60s, so put 62s as max gap. 
		return new DataMapEffortProvider(this, DBControlUnit.class, 62000L);
	}
	
}
