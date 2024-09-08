package cpod;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import clickTrainDetector.CTDataUnit;
import pamScrollSystem.ViewLoadObserver;

/**
 * 
 * The click train detector data block. Holds all detected click trains in
 * ClickTrainDataUnits which themselves contain a list of detected data units
 * within a train.
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODClickTrainDataBlock extends SuperDetDataBlock<CPODClickTrainDataUnit, PamDataUnit> {

	/**
	 * Convenience reference to the click train control 
	 */
	private CPODControl2 cpodControl;
	
//	/**
//	 * The data selector for click trains. 
//	 */
//	private CTDataSelectCreator clickDataSelectCreator;


	public CPODClickTrainDataBlock(CPODControl2 cpodControl, PamProcess parentProcess, String name, int channelMap) {
		super(CTDataUnit.class, name, parentProcess, channelMap, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.cpodControl = cpodControl;
	}
	

//	public ClickTrainDataBlock(ClickTrainControl clickTrainControl, Class<T> class1, String name, PamProcess parentProcess, int channelMap) {
//		super(CTDataUnit.class, name, parentProcess, channelMap, SuperDetDataBlock.LOAD_VIEWERDATA);
//		this.clickTrainControl = clickTrainControl;
//	}


	@Override
	public boolean saveViewerData() {
		return super.saveViewerData();
	}
	
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		Debug.out.println("Loading click train viewer data: startMillis: " + PamCalendar.formatDateTime(offlineDataLoadInfo.getStartMillis()) +  
				" endMillis: " + PamCalendar.formatDateTime(offlineDataLoadInfo.getEndMillis()));
		return super.loadViewerData(offlineDataLoadInfo, loadObserver);
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public void addPamData(CPODClickTrainDataUnit pamDataUnit) {
		super.addPamData(pamDataUnit);
		//this.sortData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit, java.lang.Long)
	 */
	@Override
	public void addPamData(CPODClickTrainDataUnit pamDataUnit, Long uid) {
		super.addPamData(pamDataUnit, uid);
		//this.sortData();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#updatePamData(PamguardMVC.PamDataUnit, long)
	 */
	@Override
	public void updatePamData(CPODClickTrainDataUnit pamDataUnit, long updateTimeMillis) {
		super.updatePamData(pamDataUnit, updateTimeMillis);
		//this.sortData();
	}
	
	@Override
	public float getSampleRate() {		
		return super.getSampleRate();
	}
	
	
	@Override
	public boolean needViewerDataLoad(OfflineDataLoadInfo offlineDataLoadInfo) {
		// always reload these data to make sure everything gets linked up correctly. 
//		22/10/2019. Changed a bunch of viewer load stuff including have the click train detector 
//		load up with a longer scroll than click detector. Have disabled because it now really messes up
//		the click train detector. 
//		Debug.out.println("-- CLICK TRAIN DATABLOCK: Need viewer load: " + super.needViewerDataLoad(offlineDataLoadInfo));
		return super.needViewerDataLoad(offlineDataLoadInfo); 
	}
	
//	@Override
//	public void clearAll() {
//		Debug.out.println("-- CLICK TRAIN DATABLOCK: clear 1: ");
//
//		ListIterator<T> iterator  = this.getListIterator(0); 
//		int count = 0; 
//		T dataUnit; 
//		while (iterator.hasNext()) {
//			dataUnit= iterator.next(); 
//			dataUnit.removeAllSubDetections();
//			dataUnit.clearSubdetectionsRemoved();
//			if (count%100==0) {
//				Debug.out.println("-- CLICK TRAIN DATABLOCK: clear: " + count+++ "  " + dataUnit);
//			}
//		}
//		super.clearAll();
//		
//		Debug.out.println("-- CLICK TRAIN DATABLOCK: clear 2: ");
//	}




	public CPODControl2 getClickTrainControl() {
		return cpodControl; 
	}
	
//	/* (non-Javadoc)
//	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
//	 */
//	@Override
//	public synchronized  DataSelectorCreator getDataSelectCreator() {
//		if (clickDataSelectCreator == null) {
//			clickDataSelectCreator = new CTDataSelectCreator(clickTrainControl, this);
//		}
//		return clickDataSelectCreator;
//	
//	}

}