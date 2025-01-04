package clickTrainDetector.offline;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickDetector.ClickDetection;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskGroupParams;

/**
 * Run the click train detector in viewer mode, including the click train classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickTrainOfflineTask extends OfflineTask<PamDataUnit<?,?>> {
	
	/**
	 * Reference to the click train control 
	 */
	private ClickTrainControl clickTrainControl;
	
	private int count=0; 
	
	ClickTrainDataBlock<CTDataUnit> ctDataBlock;

	/** 
	 * Constructor for the click train offline processing
	 * @param clickTrainContol - the click train control. 
	 */
	public ClickTrainOfflineTask(ClickTrainControl clickTrainControl){
		super(clickTrainControl, (PamDataBlock<PamDataUnit<?, ?>>) clickTrainControl.getParentDataBlock());
		//System.out.println("Offline task: Click train parent datablock: " + clickTrainControl.getParentDataBlock());
		this.clickTrainControl=clickTrainControl; 
//		setParentDataBlock(clickTrainControl.getParentDataBlock());
		super.addAffectedDataBlock(ctDataBlock = clickTrainControl.getClssfdClickTrainDataBlock());
		
	}

	@Override
	public String getName() {
		return "Click Train Detector";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
//		System.out.println("ClickTrainOfflineTask: click detection: " + dataUnit.getUID() + " time:  " 
//	+ dataUnit.getTimeMilliseconds() + " amplitude: " + dataUnit.getAmplitudeDB() + " channel: " + dataUnit.getChannelBitmap());
		
		//---------
		//if (count>10) return true; //TEMP //TEMP
		//---------
		count++;
		clickTrainControl.getClickTrainProcess().newClickData(dataUnit); 
		
		return true;
	}
	

	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {	
		super.getOfflineTaskGroup().setPrimaryDataBlock(clickTrainControl.getParentDataBlock());

		setParentDataBlock(clickTrainControl.getParentDataBlock());
		count=0; 

		//clear all the current click trains loaded in memory. 
		clearCurrentTrains();
		
		clickTrainControl.getClickTrainProcess().prepareProcess();
		clickTrainControl.setNotifyProcesses(true);
		clickTrainControl.update(ClickTrainControl.PROCESSING_START);

	}
	
	/**
	 * Clear all the data units including sub detections before processing. 
	 */
	private void clearCurrentTrains() {
		Debug.out.println("------Clear the current click train datablocks!!!------"); 
		//clear unconfirmed click trains just in case. 
		clickTrainControl.getClickTrainProcess().getUnconfirmedCTDataBlock().clearAll();
		clickTrainControl.getClickTrainProcess().getClickTrainDataBlock().clearAll();
		clickTrainControl.getClickClassifierProccess().getClssfdClickTrainDataBlock().clearAll(); 

		//this is carried out automatically in the super class. 
//		Debug.out.println("ctDataBlock " + ctDataBlock.getUnitsCount());
//
//		synchronized (ctDataBlock.getSynchLock()) {
//			ListIterator<CTDataUnit> ctIterator = ctDataBlock.getListIterator(0);
//			
//			
//			while (ctIterator.hasNext()) {
//				CTDataUnit ct = ctIterator.next();
//				Debug.out.println("Clear the data unit: " + ct);
//				
//				//must clear both sub detections from super detection and 
//				//super detection from sub detection!
//				for (int i=0; i<ct.getSubDetectionsCount(); i++) {
//					Debug.out.println("Clear the data unit: 1" + ct.getSubDetection(i).getSuperDetectionsCount());
//					ct.getSubDetection(i).removeSuperDetection(ct);
//					Debug.out.println("Clear the data unit: 2" + ct.getSubDetection(i).getSuperDetectionsCount());
//				}
//				
//				ct.removeAllSubDetections();
//				ct.clearSubdetectionsRemoved();
//				
//				ctIterator.remove();
//			}
//		}
	}
	/**
	 * Called at the end of the thread which executes this task. 
	 */
	@Override
	public void completeTask() { 
		clickTrainControl.update(ClickTrainControl.PROCESSING_END);
		clickTrainControl.setNotifyProcesses(false);
		//update the data map - needed for proper linking?
		if (super.getOfflineTaskGroup().getTaskGroupParams().dataChoice!=TaskGroupParams.PROCESS_LOADED) {
			//FIXME - a bit of a hack here. Calling updateDataMap means the the datamap reverts to loading the first
			//section of data. This is really annoying when analysing loaded data becase you have to find your way back.
			//For now just disabled for loaded data.
			PamController.getInstance().updateDataMap();
		}
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		clearCurrentTrains(); //clear the current click trains. Needed to prevent memory leak when analysing large
		//datasets in viewer mode. 
		clickTrainControl.update(ClickTrainControl.PROCESSING_START);
		// called whenever new data is loaded. 
//		clickTrainControl.getClssfdClickTrainDataBlock().
	}

	@Override
	public void loadedDataComplete() {
		// called whenever the data load is complete
		//clickTrainControl.update(ClickTrainControl.PROCESSING_START);
	}
	
	
	@Override
	public boolean hasSettings() {
		return true;
	}
	
	@Override
	public boolean callSettings() {
		clickTrainControl.getSwingGUI().showSettingsDialog(PamController.getMainFrame(), false);
		return true;
	}
	
	@Override
	public void deleteOldData(TaskGroupParams taskGroupParams) {
		//only delete data if the click train task is selected- otherwise
		//there may be other tasks that do not want to delete click train
		///e.g. the click train classifier task. 
		if (super.isDoRun()) super.deleteOldData(taskGroupParams);
	}

	@Override
	public PamControlledUnit getTaskControlledUnit() {
		return clickTrainControl;
	}

}
