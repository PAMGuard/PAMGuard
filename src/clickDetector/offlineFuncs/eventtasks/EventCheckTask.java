package clickDetector.offlineFuncs.eventtasks;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.DatabaseCheckObserver;
import clickDetector.offlineFuncs.DatabaseChecks;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

/**
 * Event check repair task, so it can be called from batch processor
 * since events are probably all in memory, ignore normal ways of 
 * accessing data and do own things. 
 * @author dg50
 *
 */
public class EventCheckTask extends OfflineTask implements DatabaseCheckObserver {

	private DatabaseChecks databaseChecks;
	
	public EventCheckTask(ClickControl clickControl, PamDataBlock parentDataBlock) {
		super(clickControl, parentDataBlock);
		databaseChecks = new DatabaseChecks(clickControl, this);
	}

	@Override
	public String getName() {
		return "Offline Event Checks";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		return databaseChecks.checkEvent(dataUnit.getDatabaseIndex(), true);
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkProgress(String text, int totalTasks, int taskNumber, int percent) {
	}

	@Override
	public void checkOutputText(String text, int warnLevel) {
		if (text != null) {
			System.out.println(text);
		}
	}

	@Override
	public boolean stopChecks() {
		// TODO Auto-generated method stub
		return false;
	}

}
