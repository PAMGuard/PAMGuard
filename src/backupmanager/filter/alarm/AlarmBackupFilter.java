package backupmanager.filter.alarm;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamViewParameters;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.debug.Debug;
import alarm.AlarmControl;
import alarm.AlarmDataBlock;
import alarm.AlarmDataUnit;
import alarm.AlarmLogging;
import backupmanager.BackupManager;
import backupmanager.action.BackupAction;
import backupmanager.filter.BackupFilter;
import backupmanager.filter.BackupFilterParams;
import backupmanager.stream.StreamItem;
import generalDatabase.DBControlUnit;

public class AlarmBackupFilter extends BackupFilter {
	
	private AlarmFilterParams alarmFilterParams = new AlarmFilterParams();

	public AlarmFilterParams getAlarmFilterParams() {
		return alarmFilterParams;
	}

	public AlarmBackupFilter(BackupAction backupAction, String filterName) {
		super(backupAction, filterName);
	}

	@Override
	public boolean runFilter(BackupManager backupManager, List<StreamItem> streamItems) {
		/**
		 * Go through every alarm and if it's enabled in this filter, then 
		 * query it to get a list of times of interest, then amalgamate those lists. 
		 */
		if (streamItems == null || streamItems.size() == 0) {
			return true; // nothing to do, so can say that's OK. 
		}
		if (alarmFilterParams.isPassEverything()) {
			passEverything(streamItems, "No Filter");
			return true;
		}
		// can we assume that these are never null ? they shouldn't be. Perhaps the last one ? 
		Long tStart = streamItems.get(0).getStartUTC();
		Long tEnd = streamItems.get(streamItems.size()-1).getEndUTC();
		if (tStart == null || tEnd == null) {
			passEverything(streamItems, "No Stream Times");
			return false;
		}
		/*
		 *  Don't make a master list, but treat each alarm separately and label streamItems accordingly 
		 *  so that a record can be kept of which alarms triggered actions on which files. 
		 *  If there are multiple alarms, then 
		 */
		int usedFilters = 0;
		unPassEverything(streamItems, null);
		ArrayList<PamControlledUnit> alarmControls = PamController.getInstance().findControlledUnits(AlarmControl.class);
		for (int i = 0; i < alarmControls.size(); i++) {
			AlarmControl alarmControl = (AlarmControl) alarmControls.get(i);
			AlarmParamSet alarmParams = alarmFilterParams.getAlarmParamSet(alarmControl.getUnitName()); 
			if (alarmParams.useAlarm == false) {
				continue;
			}
			usedFilters++;
			List<AlarmDataUnit> alarmData = getAlarmData(alarmControl, tStart, tEnd);
			labelItems(alarmControl, alarmParams, streamItems, alarmData);
		}
		
		if (usedFilters == 0) {
			passEverything(streamItems, "No filter");
		}
		
		return true;
	}


	private void labelItems(AlarmControl alarmControl, AlarmParamSet alarmParams, List<StreamItem> streamItems, List<AlarmDataUnit> alarmData) {
		ListIterator<StreamItem> streamIt = streamItems.listIterator();
		ListIterator<AlarmDataUnit> alarmIt = alarmData.listIterator();
		StreamItem currentItem = streamIt.next();
		while (alarmIt.hasNext() && currentItem != null) {
			AlarmDataUnit adu = alarmIt.next();
			long t1 = adu.getTimeMilliseconds() - alarmParams.prePeriod;
			long t2 = adu.getEndTimeInMilliseconds() + alarmParams.postPeriod;
			while (currentItem.getStartUTC() > t2) {
				if (streamIt.hasPrevious()) {
					currentItem = streamIt.previous();
				}
				else {
					break;
				}
			}
			while (currentItem != null) {
				if (currentItem.getStartUTC() < t2 && currentItem.getEndUTC() >= t1) {
					currentItem.setProcessIt(true);
					currentItem.addFilterMessage(alarmControl.getUnitName());
				}
				else if (currentItem.getStartUTC() > t2) {
					break;
				}
				if (streamIt.hasNext()) {
					currentItem = streamIt.next();
				}
				else {
					currentItem = null;
				}
			}			
		}
	}

	/**
	 * Get the alarm data for a specific datablock. 
	 * @param alarmControl
	 * @param tStart
	 * @param tEnd
	 * @return
	 */
	private List<AlarmDataUnit> getAlarmData(AlarmControl alarmControl, long tStart, long tEnd) {
		AlarmParamSet alarmParams = alarmFilterParams.getAlarmParamSet(alarmControl.getUnitName());
		if (alarmParams == null || alarmParams.useAlarm == false) {
			return null;
		}
		// query the database to get a list of alarm control units. 
		AlarmDataBlock alarmDataBlock = alarmControl.getAlarmProcess().getAlarmDataBlock();
		alarmDataBlock = new AlarmDataBlock(null, alarmDataBlock.getDataName()); // make a datablock with the same name, should be OK with null process
		AlarmLogging alarmLogging = new AlarmLogging(alarmDataBlock);
		tStart -= alarmParams.prePeriod;
		tEnd += alarmParams.postPeriod;
		/*
		 *  should really make up a better PVP that uses some of the end time info rather than just the UTC value
		 *  in case something started long before the time we're interested in, and carried on.  
		 */
		PamViewParameters pvp = new PamViewParameters(tStart, tEnd);
		Debug.out.println(pvp.getSelectClause(DBControlUnit.findConnection().getSqlTypes()));
		alarmLogging.loadViewData(pvp, null);
		return alarmDataBlock.copyDataList();
	}

	@Override
	public PamDialogPanel getDialogPanel(Window window) {
		return new AlarmFilterDialogPanel(this);
	}

	@Override
	public void setFilterParams(BackupFilterParams backupFilterParams) {
		if (backupFilterParams instanceof AlarmFilterParams) {
			this.alarmFilterParams = (AlarmFilterParams) backupFilterParams;
		}
		
	}

	@Override
	public BackupFilterParams getFilterParams() {
		return alarmFilterParams;
	}

}
