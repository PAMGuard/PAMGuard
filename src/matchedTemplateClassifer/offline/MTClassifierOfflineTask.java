package matchedTemplateClassifer.offline;

import PamController.PamController;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.DataUnitFileInformation;
import clickDetector.ClickDetection;
import dataMap.OfflineDataMapPoint;
import matchedTemplateClassifer.MTClassifierControl;
import offlineProcessing.OfflineTask;

/**
 * Offline task for reclassifying clicks.
 * @author Jamie Macaulay
 *
 */
public class MTClassifierOfflineTask extends OfflineTask<PamDataUnit<?,?>> {

	/**
	 * Reference to the click train control 
	 */
	private MTClassifierControl mtClassifierControl;

	private int count=0; 

	/** 
	 * Constructor for the click train offline processing
	 * @param clickTrainContol - the click train control. 
	 */
	public MTClassifierOfflineTask(MTClassifierControl mtClassifierControl){
		super(mtClassifierControl.getParentDataBlock());
		this.mtClassifierControl=mtClassifierControl; 
		//		setParentDataBlock(clickTrainControl.getParentDataBlock());
	}

	@Override
	public String getName() {
		return "Matched Template Classifier";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {

		try {
//			System.out.println("MT new data unit: " + dataUnit); 


			count++;
			mtClassifierControl.getMTProcess().newClickData(dataUnit); 

			//since an annotation has been added might need to do this so that the data unit is actually saved. 
			DataUnitFileInformation fileInfo = dataUnit.getDataUnitFileInformation();

//			System.out.println("file info: " + fileInfo); 
			if (fileInfo != null) {
				fileInfo.setNeedsUpdate(true);
			}
			dataUnit.updateDataUnit(System.currentTimeMillis());	


			return true;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}


	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {	
		count=0; 
		mtClassifierControl.getMTProcess().prepareProcess();
		mtClassifierControl.update(MTClassifierControl.PROCESSING_START);
		//		System.out.println("Waveform match: " + mtClassifierControl.getMTParams().classifiers.get(0).waveformMatch.toString());
		//		System.out.println("Waveform reject: " + mtClassifierControl.getMTParams().classifiers.get(0).waveformReject.toString());
	
		//moved below from can
		
		mtClassifierControl.getMTProcess().prepareProcess();
		//had to put this in here for some reason??
		this.setParentDataBlock(mtClassifierControl.getParentDataBlock());
		
		/*
		 * As of PG version 2.02 this is required so that annotation are saved. 
		 */
		this.addAffectedDataBlock(mtClassifierControl.getParentDataBlock());
	}

	/**
	 * Called at the end of the thread which executes this task. 
	 */
	@Override
	public void completeTask() { 
		mtClassifierControl.update(MTClassifierControl.PROCESSING_END);
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		mtClassifierControl.update(MTClassifierControl.PROCESSING_START);
		// called whenever new data is loaded. 

	}

	@Override
	public void loadedDataComplete() {
		// called whenever the data load is complete
		//clickTrainControl.update(ClickTrainControl.PROCESSING_START);
	}

	/**
	 * task has settings which can be called
	 * @return true or false
	 */
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		mtClassifierControl.showSettingsDialog(PamController.getMainFrame());
		return true;
	}

	/**
	 * can the task be run ? This will generally 
	 * be true, but may be false if the task is dependent on 
	 * some other module which may not be present.  
	 * @return true if it's possible to run the task. 
	 */
	public boolean canRun() {
	
		//System.out.println("Datablock: " + getDataBlock() + mtClassifierControl.getParentDataBlock()); 
		boolean can = getDataBlock() != null; 
		return can;

	}

}

