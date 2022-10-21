package clickTrainDetector.offline;

import PamController.PamController;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;
import offlineProcessing.TaskGroupParams;

/**
 * Runs only the click train classifier on the data (does not recalculate click trains)
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTClassifierOfflineTask  extends OfflineTask<CTDataUnit> {

	/**
	 * Reference to the click train control 
	 */
	private ClickTrainControl clickTrainControl;

	private int count=0; 

	/** 
	 * Constructor for the click train offline processing
	 * @param clickTrainContol - the click train control. 
	 */
	public CTClassifierOfflineTask(ClickTrainControl clickTrainControl){
		super(clickTrainControl.getClssfdClickTrainDataBlock());
		this.clickTrainControl=clickTrainControl; 
		// the parent data block is the click train detector block. 
		setParentDataBlock(clickTrainControl.getClssfdClickTrainDataBlock());
	}

	@Override
	public String getName() {
		return "Click Train Classifier";
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		//		ListIterator<CTDataUnit> iterator = clickTrainControl.getClssfdClickTrainDataBlock().getListIterator(0);
		//		CTDataUnit ctDataUnit; 
		//		while (iterator.hasNext()) {
		//			ctDataUnit= iterator.next();
		//			Debug.out.println("Hello: CTDataUnit: UID " + ctDataUnit.getUID() + "  " + ctDataUnit.getSubDetectionsCount()); 
		//		}

		clickTrainControl.getSwingGUI().showSettingsDialog(PamController.getMainFrame(), true);
		return true;
	}

	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {	
		count=0; 
		super.getOfflineTaskGroup().setPrimaryDataBlock(clickTrainControl.getClssfdClickTrainDataBlock());
	}



	@Override
	public boolean processDataUnit(CTDataUnit dataUnit) {
		count++;
		//sub detection linking can be an issue...need to fix. 
		if (dataUnit.getSubDetectionsCount()>0) {
			clickTrainControl.getClassifierManager().classifySpecies(dataUnit);
			return true;
		}
		else return false; 
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
	public void deleteOldData(TaskGroupParams taskGroupParams) {
		//never delete old data
	}



}
