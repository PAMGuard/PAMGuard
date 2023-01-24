package clickDetector.offlineFuncs;

import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamSubtableData;
import generalDatabase.external.crossreference.CrossReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import Localiser.detectionGroupLocaliser.GroupDetection;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import pamScrollSystem.ViewLoadObserver;
//import staticLocaliser.StaticLocaliserControl;
//import staticLocaliser.StaticLocaliserProvider;
//import staticLocaliser.panels.AbstractLocaliserControl;
//import staticLocaliser.panels.ClickEventLocaliserControl;
import clickDetector.ClickDetector;
import clickDetector.ClickTrainDetection;
import clickDetector.dataSelector.ClickTrainDataSelectorCreator;
import dataMap.OfflineDataMap;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import autecPhones.AutecGraphics;

/**
 * PamDataBlock for offline events. 
 * <p>
 * As with RainbowClick, this will (probably) hold all offline events from the 
 * database so that the operator can navigate through them with ease. However, only
 * clicks associated with the currently loaded period will be loaded into memory and associated
 * with the OfflineEventDataUnits. 
 * @author Doug
 *
 */

public class OfflineEventDataBlock extends SuperDetDataBlock<OfflineEventDataUnit, PamDataUnit> {
	

	private ClickDetector clickDetector;
	private ClickTrainDataSelectorCreator clickTrainDataSelectorCreator;
	
	public OfflineEventDataBlock(String dataName,
			ClickDetector parentProcess, int channelMap) {
		super(OfflineEventDataUnit.class, dataName, parentProcess, channelMap, SuperDetDataBlock.ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING);
		this.clickDetector = parentProcess;
		/*
		 * Use a vector rather than the default linked list since
		 * this will be faster for sorting and insert / retrieval. 
		 */
		pamDataUnits = new Vector<OfflineEventDataUnit>();
		
		setOverlayDraw(new OfflineEventGraphics(this));
		this.setPamSymbolManager(new StandardSymbolManager(this, OfflineEventGraphics.defSymbol, true));
		setNaturalLifetime(120);
		
	}
	
	/**
	 * Quick event id for new data units. Generally, this will get overwritten when there is a database
	 * with the database index, but will stick if there is no database. 
	 * @return
	 */
	public int getLastEventId() {
		return unitsAdded;
	}
	
	@Override
	public int getChannelMap(){
		return clickDetector.getClickDataBlock().getChannelMap();
	}
	
	@Override
	public void addPamData(OfflineEventDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
		Collections.sort(pamDataUnits);
		/*
		 *  also need to immediately save the event, so that it picks up it's 
		 *  event id number right away
		 */
		if (pamDataUnit.getDatabaseIndex() == 0) {
			//		getLogging().saveOfflineData(dbControlUnit, connection)
			saveViewerData();
		}
	}

	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		boolean isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		return (pamDataUnit.getDatabaseIndex() == 0 || isViewer == false);
	}

//	/**
//	 * LoadViewerData works very differently for offline events since all events for 
//	 * the entire data set are always held in memory. <p>
//	 * Therefore, they only need be loaded once at the start of analysis and should
//	 * never be deleted.  They should however be saved as often as is reasonably possible. 
//	 */
//	@Override
//	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
//		// if no data are in memory, then load ...
//		/*
//		 *  otherwise no loading is done at all. This totally messes up the linking of 
//		 *  clicks since the subtable is no longer loaded ! 
//		 */
//		if (getUnitsCount() == 0) {
//			// need to find the data map and load all data from the entire
//			// data set. 
//			OfflineDataMap dataMap = getPrimaryDataMap();
//			if (dataMap == null) {
//				return false;
//			}
//			//want to load all data. 
//			long origStart = offlineDataLoadInfo.getStartMillis();
//			long origEnd = offlineDataLoadInfo.getEndMillis();
//			long dataStart = Math.min(offlineDataLoadInfo.getStartMillis(), dataMap.getFirstDataTime() - 3600000L);
//			long dataEnd = Math.max(offlineDataLoadInfo.getEndMillis(), dataMap.getLastDataTime() + 3600000L);
//			offlineDataLoadInfo.setStartMillis(dataStart);
//			offlineDataLoadInfo.setEndMillis(dataEnd);
//			
//			boolean retVal = super.loadViewerData(offlineDataLoadInfo, loadObserver);
//			Debug.out.println(String.format("%d events loaded between %s and %s", 
//					getUnitsCount(), PamCalendar.formatDateTime(dataStart), 
//					PamCalendar.formatDateTime(dataEnd)));
//			offlineDataLoadInfo.setStartMillis(origStart);
//			offlineDataLoadInfo.setEndMillis(origEnd);
//
//
//			return retVal;
//		}
//		else {
//			saveViewerData();
//
//			/*
//			 * Fudge the loading of the subtable data here. IF we don't save and re-read, then when 
//			 * clicks are reloaded (e.g. afterscrolling) then the subtable data will be out of date and 
//			 * not correclty added to the fresh copies of the same clicks. Hopefully we can get away with just loading
//			 * the clicks in the immediate time window which should speed things up somewhat. 
//			 */
//			PamViewParameters pvp = new PamViewParameters(offlineDataLoadInfo.getStartMillis()-1000, 
//					offlineDataLoadInfo.getEndMillis()+1000);
//			PamConnection pamCon = DBControlUnit.findConnection();
//			if (pamCon != null) {
//				ArrayList<PamSubtableData> subtableData = getLogging().getSubLogging().loadSubtableData(pamCon, getLogging(), pvp);
//				setSubtableData(subtableData);
//				Debug.out.printf("Reloaded %d subtable items from %s\n", subtableData.size(), getDataName());
//			}
//			
//			return true;
//		}
//	}


	/**
	 * This is generally only called from loadViwerData and since
	 * LoadviewerData only ever operates once, it should never get called !
	 */
	@Override
	public synchronized void clearAll() {
		super.clearAll();
	}

//	@Override
//	public boolean saveViewerData() {
//
////		/*
////		 * First it is necessary to save all the updated click information
////		 * to the database since some of the events may need to check some 
////		 * information from the updated click table before they save themselves. 
////		 */
//		OfflineClickLogging offlineClickLogging = 
//			clickDetector.getClickDataBlock().getOfflineClickLogging();
//		
////		***** Don't need to do this anymore as it's handled by the subtable now *****
////		boolean ok = offlineClickLogging.saveViewerData();
////		
//		
//		if (offlineClickLogging==null){
//			System.err.println("OfflineEventDataBlock: could not find offlineClickLogging");
//			//return false;
//		}
//		offlineClickLogging.checkSuspectEventTimes(this);
//				
//		return super.saveViewerData();
//	}

//	@Override
//	public AbstractLocaliserControl getSLControlDialog(
//			StaticLocaliserControl staticLocaliserControl) {
//		ClickEventLocaliserControl clickEventcontrol=new ClickEventLocaliserControl(staticLocaliserControl);
//		return clickEventcontrol;
//	}



	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		return super.removeOldUnitsS(mastrClockSample);
	}

	@Override
	synchronized protected int removeOldUnitsT(long currentTimeMS) {
		int unitsRemoved = 0;
		if (pamDataUnits.isEmpty())
			return 0;
		GroupDetection clickTrain;
		long firstWantedTime = currentTimeMS - this.naturalLifetime * 1000;
		firstWantedTime = Math.min(firstWantedTime, currentTimeMS - getRequiredHistory());
		
		int i = 0;

		while (i < pamDataUnits.size()) {
			clickTrain = pamDataUnits.get(i);
			if (clickTrain.getEndTimeInMilliseconds() < firstWantedTime) {
				pamDataUnits.remove(clickTrain);
			}
			else if ((clickTrain).getStatus() == ClickTrainDetection.STATUS_BINME) {
				pamDataUnits.remove(clickTrain);
			}
			else {
				i++;
			}
		}
		return unitsRemoved;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#updatePamData(PamguardMVC.PamDataUnit, long)
	 */
	@Override
	public void updatePamData(OfflineEventDataUnit pamDataUnit, long updateTimeMillis) {
//		System.out.println("Updating Offline data unit");
		super.updatePamData(pamDataUnit, updateTimeMillis);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelector(java.lang.String, boolean)
	 */
	@Override
	public synchronized DataSelectorCreator getDataSelectCreator() {
		if (clickTrainDataSelectorCreator == null) {
		clickTrainDataSelectorCreator = new ClickTrainDataSelectorCreator(clickDetector.getClickControl(), this);
		}
		return clickTrainDataSelectorCreator;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getCrossReferenceInformation()
	 */
	@Override
	public CrossReference getCrossReferenceInformation() {
		try {
			String eventTable = clickDetector.getOfflineEventLogging().getTableDefinition().getTableName();
			String clickTable = clickDetector.getClickDataBlock().getOfflineClickLogging().getTableDefinition().getTableName();
			return new CrossReference(eventTable, "Id", "CopyId", clickTable, "EventId");
		}
		catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public double[] getDurationRange() {
		return clickDetector.getDurationRange();
	}

	@Override
	public boolean canSuperDetection(PamDataBlock subDataBlock) {
		return (clickDetector.getClickDataBlock() == subDataBlock || clickDetector.getTrackedClicks() == subDataBlock);
	}

//	int nName = 0;
//	@Override
//	public String getDataName() {
//		// TODO Auto-generated method stub
//		System.out.println("Call into getDataName " + ++nName);
//		if (nName == 58) {
//
//			System.out.println("Call into getDataName " + ++nName);
//		}
//		return super.getDataName();
//	}

}
	
