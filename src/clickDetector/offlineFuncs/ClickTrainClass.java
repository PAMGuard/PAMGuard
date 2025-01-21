package clickDetector.offlineFuncs;

import offlineProcessing.OfflineTask;

import java.awt.Frame;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickTrainDetector;
import clickDetector.clicktrains.ClickTrainIdParams;
import clickDetector.dialogs.ClickTrainIdDialog;
import dataMap.OfflineDataMapPoint;

public class ClickTrainClass extends OfflineTask<ClickDetection>{

	private ClickControl clickControl;
	
	private ClickTrainDetector clickTrainDetector;
	
	/**
	 * @param clickControl
	 */
	public ClickTrainClass(ClickControl clickControl) {
		super(clickControl, clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		clickTrainDetector = clickControl.getClickTrainDetector();
		addAffectedDataBlock(clickTrainDetector.getTrackedClickDataBlock());
	}

	@Override
	public String getName() {
		return "Click Train Detector";
	}

	@Override
	public boolean processDataUnit(ClickDetection dataUnit) {
		clickTrainDetector.addData(clickControl.getClickDataBlock(), dataUnit);
		boolean hasSuper = (dataUnit.getSuperDetectionsCount() > 0);
		return hasSuper;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#hasSettings()
	 */
	@Override
	public boolean hasSettings() {
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#callSettings()
	 */
	@Override
	public boolean callSettings() {
		ClickTrainIdParams newParams;
		Frame pf = clickControl.getGuiFrame();
		if ((newParams = ClickTrainIdDialog.showDialog(pf, clickTrainDetector.getClickTrainIdParameters())) != null) {
			clickTrainDetector.setClickTrainIdParameters(newParams.clone());
			return true;
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#canRun()
	 */
	@Override
	public boolean canRun() {
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#prepareTask()
	 */
	@Override
	public void prepareTask() {
		clickTrainDetector.pamStart();
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#completeTask()
	 */
	@Override
	public void completeTask() {
		clickTrainDetector.pamStop();
	}

}
