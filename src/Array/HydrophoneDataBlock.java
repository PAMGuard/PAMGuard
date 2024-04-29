package Array;

import java.util.ListIterator;

import GPS.NavDataSynchronisation;
import pamScrollSystem.ViewLoadObserver;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

public class HydrophoneDataBlock extends PamDataBlock<HydrophoneDataUnit> {

	private HydrophoneProcess hydrophoneProcess;


	public HydrophoneDataBlock(String dataName,
			HydrophoneProcess hydrophoneProcess, int channelMap) {
		super(HydrophoneDataUnit.class, dataName, hydrophoneProcess, channelMap);
		this.hydrophoneProcess = hydrophoneProcess;
		setSynchLock(NavDataSynchronisation.getSynchobject());
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public void clearAll() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			super.clearAll();
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		synchronized (getSynchLock()) {
			clearChannelIterators();
		}
		return 0;
	}

	@Override
	public boolean shouldNotify() {
		return false;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getNumRequiredBeforeLoadTime()
	 */
	@Override
	public int getNumRequiredBeforeLoadTime() {
		return ArrayManager.getArrayManager().getCurrentArray().getHydrophoneCount()*2;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getShouldLog(PamguardMVC.PamDataUnit)
	 */
	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		return true;
	}

	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		
		if (offlineDataLoadInfo!=null) {
		System.out.print("Load Hydrophones: " + ((offlineDataLoadInfo.getEndMillis() - offlineDataLoadInfo.getStartMillis())/1000/60/60 + " hours"));
		System.out.print("From: " +PamCalendar.formatDateTime(offlineDataLoadInfo.getStartMillis()) + " to " +  PamCalendar.formatDateTime(offlineDataLoadInfo.getEndMillis()));
		}
		/**
		 * Always put in default data units at time zero. 
		 */
		hydrophoneProcess.createDefaultHydrophoneUnits(0);
		return super.loadViewerData(offlineDataLoadInfo, loadObserver);
	}

	/**
	 * Find the closest hydrophone unit to a given time. 
	 * @param startTime Start time of data unit (milliseconds)
	 * @param channelBitMap Channel map - must be some overlap, not an exact match. 
	 * @return closest data unit
	 */
	public HydrophoneDataUnit getClosestHydrophone(long startTime, int ihydrophone) {
		synchronized (getSynchLock()) {
			if (pamDataUnits.size() == 0)
				return null;
			//check the time is within range of data in datablock. 
			//		if (getFirstUnit().getTimeMillis()>startTime) return null; 
			//		if (getLastUnit().getTimeMillis()<startTime) return null; 
			/*
			 * start at the last unit, the work back and if the interval starts
			 * getting bigger again, stop
			 */
			HydrophoneDataUnit unit = null;
			HydrophoneDataUnit preceedingUnit = null;
			long newdifference;
			long difference;

			ListIterator<HydrophoneDataUnit> listIterator = getListIterator(ITERATOR_END);
			if (listIterator.hasPrevious() == false) {
				return null;
			}
			unit = listIterator.previous();
			difference = Math.abs(startTime	- unit.getTimeMilliseconds());
			
			while (listIterator.hasPrevious()) {
				preceedingUnit = listIterator.previous();
				if (preceedingUnit.getHydrophone().getID()!=ihydrophone) {
					continue;
				}
				newdifference = Math.abs(startTime- preceedingUnit.getTimeMilliseconds());
				if (newdifference > difference) {
//					System.out.println("Hydrophone datablock: newDifference: " + newdifference + " " + unit.getHydrophone().getZ()); 
					return unit;
				}
				else {
					unit = preceedingUnit;
					difference = newdifference;
				}
			}
			return unit;
		}
	}



}
