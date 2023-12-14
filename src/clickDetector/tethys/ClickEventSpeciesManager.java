package clickDetector.tethys;

import java.util.Vector;

import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetector;
import clickDetector.offlineFuncs.ClicksOffline;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;

public class ClickEventSpeciesManager extends DataBlockSpeciesManager {

	private OfflineEventDataBlock eventDataBlock;
	private ClickDetector clickDetector;
	private ClickControl clickControl;
	private ClicksOffline clicksOffline;

	public ClickEventSpeciesManager(ClickDetector clickDetector, OfflineEventDataBlock eventDataBlock) {
		super(eventDataBlock);
		this.clickDetector = clickDetector;
		this.eventDataBlock = eventDataBlock;
		clickControl = clickDetector.getClickControl();
		clicksOffline = clickControl.getClicksOffline();
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		LookupList lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
		if (lutList == null || lutList.getLutList().size() == 0) {
			return new DataBlockSpeciesCodes("Unknown");
		}
		Vector<LookupItem> spList = lutList.getLutList();
		String[] spNames = new String[spList.size()];
		int i = 0;
		for (LookupItem lItem : spList) {
			spNames[i++] = lItem.getCode();
		}
		return new DataBlockSpeciesCodes("Unknown", spNames);
	}

	@Override
	public String getSpeciesCode(PamDataUnit dataUnit) {
		OfflineEventDataUnit eventDataUnit = (OfflineEventDataUnit) dataUnit;
		return eventDataUnit.getEventType();
	}

}
