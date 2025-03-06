package whistleClassifier.offline;

import dataMap.OfflineDataMapPoint;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import offlineProcessing.OfflineTask;
import whistleClassifier.WhistleClassifierControl;
import whistleClassifier.WhistleClassifierProcess;
import whistlesAndMoans.AbstractWhistleDataUnit;

public class ReclassifyTask extends OfflineTask<AbstractWhistleDataUnit>{

	WhistleClassifierControl whistleClassifierControl;
	WhistleClassifierProcess whistleClassifierProcess;
	
	long dataEndTime;

	public ReclassifyTask(WhistleClassifierControl whistleClassifierControl) {
		super(whistleClassifierControl, whistleClassifierControl.getWhistleClassifierProcess().getParentDataBlock());
		this.whistleClassifierControl = whistleClassifierControl;
		whistleClassifierProcess = whistleClassifierControl.getWhistleClassifierProcess();
		setParentDataBlock(whistleClassifierProcess.getParentDataBlock());
		addAffectedDataBlock(whistleClassifierProcess.getOutputDataBlock(0));
	}

	@Override
	public String getName() {
		return "Whistle Classification";
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		dataEndTime = endTime;
		if (whistleClassifierProcess.isTraining()) {
			String fileName;
			String species = "UNK";
			if (mapPoint == null) {
				fileName = String.format("Offline_%s", PamCalendar.formatFileDateTime(startTime, false));
			}
			else {
				FileParts fp = new FileParts(mapPoint.getName());
				fileName = fp.getFileName();
			}
			whistleClassifierProcess.prepareTrainingStore(fileName, species);
		}
		else {
			// for DCL workshop will need to reset the training between each file.
//			whistleClassifierProcess.clearFragmentStore(startTime);
		}
	}

	@Override
	public void loadedDataComplete() {
		if (whistleClassifierProcess.isTraining()) {
			whistleClassifierProcess.writeTrainingStoreData();
			whistleClassifierProcess.closeTrainingStore();
		}
		else {
			// may want to do a classification anyway, even if data are insufficient. 
			if (whistleClassifierControl.getWhistleClassificationParameters().alwaysClassify) {
				whistleClassifierProcess.runClassification(dataEndTime);
				whistleClassifierProcess.clearFragmentStore(dataEndTime);
			}
			
		}
	}

	@Override
	public boolean processDataUnit(AbstractWhistleDataUnit dataUnit) {
		whistleClassifierProcess.runTimingFunctions(dataUnit.getTimeMilliseconds());
		whistleClassifierProcess.newData(getDataBlock(), dataUnit);
		return false;
	}

	@Override
	public boolean callSettings() {
		whistleClassifierControl.settingsDialog(null);
		return true;
	}


	@Override
	public boolean hasSettings() {
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#prepareTask()
	 */
	@Override
	public void prepareTask() {
		whistleClassifierProcess.resetClassifier();
	}

	@Override
	public boolean canRun() {
		return true;
	}

}
