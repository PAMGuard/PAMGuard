package rawDeepLearningClassifier.offline;

import java.util.ListIterator;

import PamController.PamController;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import matchedTemplateClassifer.MTClassifierControl;
import offlineProcessing.OfflineTask;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

public class DLOfflineTask extends OfflineTask<PamDataUnit<?,?>>{

	/**
	 * The DL control. 
	 */
	private DLControl dlControl;

	/**
	 * Keep a track of the number of data units processed. 
	 */
	private int count =0; 

	public DLOfflineTask(DLControl dlControl) {
		super(dlControl, dlControl.getParentDataBlock()); 
		this.dlControl= dlControl; 
		this.dlControl.getDLClassifyProcess().clearOldData();	

		super.addAffectedDataBlock(this.dlControl.getDLClassifyProcess().getDLDetectionDatablock());
		super.addAffectedDataBlock(this.dlControl.getDLClassifyProcess().getDLGroupDetectionDataBlock()); //important so data are deleted. 

		//prediction data block may also be affected. 
		super.addAffectedDataBlock(this.dlControl.getDLClassifyProcess().getDLPredictionDataBlock());

		//		//group detections are a little difficult because they only appear after
		//		dlControl.getDLClassifyProcess().getDLGroupDetectionDataBlock().addInstantObserver(new GroupObserver(this));
	}



	@Override
	public String getName() {
		return "Deep Learning Classification";
	}

	@Override
	public boolean processDataUnit(PamDataUnit<?, ?> dataUnit) {
		//		System.out.println("--------------");
		//System.out.println("Offline task start: " + dataUnit.getUpdateCount() + " UID " + dataUnit.getUID() + " " + dlControl.getDLParams().enableSegmentation);
		boolean saveBinary = false; 
		try {


			if (dlControl.isGroupDetections()) {
				//once a segment is created we need to process it  but this is handled by the GroupObserver and not here....
				//Group segment data is saved to the database. 
				boolean newGroup = dlControl.getSegmenter().newGroupData(dataUnit);

				//				System.out.println("Added data to group: " + dataUnit.getUID()); 

				if (newGroup) {
					
					//run the classifier on the group data to classify species of the group
					runGroupClassifier(dlControl.getSegmenter().getSegmenteGroupDataBlock().getFirstUnit());

				}

				saveBinary = false; //very important or binary files become messed up and clicks/whistles are deleted. 
			}
			else  {
				//Classifying one detection - there may be multiple segments in one detection but once a 
				//detection has been added we force the classifier to run on all the segments generated from 
				//the raw data. 

				//Process a data unit within the segmenter
				dlControl.getSegmenter().newData(dataUnit); 

				//System.out.println("Segments: " + dlControl.getSegmenter().getSegmenterDataBlock().getUnitsCount());
				//need to add the segmenter data units into the classification buffer
				ListIterator<GroupedRawData> iterator = dlControl.getSegmenter().getSegmenterDataBlock().getListIterator(0); 
				while (iterator.hasNext()) {
					dlControl.getDLClassifyProcess().newData(dlControl.getSegmenter().getSegmenteGroupDataBlock(), iterator.next());
				}

				//force click data save
				dlControl.getDLClassifyProcess().forceRunClassifier(dataUnit);

				//must be called or can result in memory leak. 
				dlControl.getSegmenter().getSegmenterDataBlock().clearAll();

				//must be called or can result in memory leak. 
				dlControl.getDLClassifyProcess().getDLPredictionDataBlock().clearAll();

				saveBinary = true;

			};



			//		/**
			//		 * So the issue here is that the classification is not on the same thread...
			//		 */
			//		System.out.println("Offline task complete: " + dataUnit.getUpdateCount() + " data " + dataUnit +  " no. annotations: " + dataUnit.getNumDataAnnotations() );
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return saveBinary;
	}
	
	
	/**
	 * Run the group classifier on the group data. 
	 * This is called when a new group is created and the group data is ready to be classified.
	 * @param segmenterDetectionGroup - the group data to classify.
	 */
	private void runGroupClassifier(SegmenterDetectionGroup segmenterDetectionGroup) {
		//have to manually add this as the group data is a multiplex data block. 
		dlControl.getDLClassifyProcess().newData(null, dlControl.getSegmenter().getSegmenteGroupDataBlock().getFirstUnit());

		//run the classifier - if group detection data is present the classifier will run
		dlControl.getDLClassifyProcess().forceRunClassifier( dlControl.getSegmenter().getSegmenteGroupDataBlock().getFirstUnit());

		//			//clear the data block
		dlControl.getSegmenter().getSegmenteGroupDataBlock().clearAll();
	}

	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {	
		count=0; 
		prepProcess(); 

		//this is important so that the offline tasks knows that the 
		//parent datablock (e.g. if clicks or clips) needs to be saved because
		// an annotation has been added to it. 
		super.addAffectedDataBlock(dlControl.getParentDataBlock());

		super.getOfflineTaskGroup().setSummaryLists();

		//		for (int i=0; i<super.getNumAffectedDataBlocks(); i++) {
		//			System.out.println("AFFECTED DATA BLOCKS: " + super.getAffectedDataBlock(i));
		//		}

		this.setParentDataBlock(dlControl.getParentDataBlock());

		//dlControl.setNotifyProcesses(true);
		this.dlControl.getDLModel().prepModel(); 

		dlControl.update(MTClassifierControl.PROCESSING_START);

		//		System.out.println("Waveform match: " + mtClassifierControl.getMTParams().classifiers.get(0).waveformMatch.toString());
		//		System.out.println("Waveform reject: " + mtClassifierControl.getMTParams().classifiers.get(0).waveformReject.toString());
	}


	/**
	 * Called at the end of the thread which executes this task. 
	 */
	@Override
	public void completeTask() {
		//dlControl.setNotifyProcesses(false);
		//this.dlControl.getDLModel().closeModel();                                                                                                            
		dlControl.update(MTClassifierControl.PROCESSING_END);

		//need to finish of group detections
		if (dlControl.isGroupDetections()) {
			runGroupClassifier(dlControl.getSegmenter().getSegmenteGroupDataBlock().getFirstUnit());
		}
	}

@Override
public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {

	prepProcess();
	dlControl.update(MTClassifierControl.PROCESSING_START);
	// called whenever new data is loaded. 
}

@Override
public void loadedDataComplete() {
	// TODO Auto-generated method stub

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
	dlControl.showSettingsDialog(PamController.getMainFrame());
	return true;
}

/**
 * Prepare required processes. 
 */
private void prepProcess() {
	dlControl.getSegmenter().prepareProcess();
	dlControl.getDLClassifyProcess().prepareProcess();
}

/**
 * can the task be run ? This will generally 
 * be true, but may be false if the task is dependent on 
 * some other module which may not be present.  
 * @return true if it's possible to run the task. 
 */
public boolean canRun() {
	/**
	 * Removed prep process here because it caused the buffers result for click detections, 
	 */
	//had to put this in here for some reason??
	this.setParentDataBlock(dlControl.getParentDataBlock());
	//System.out.println("Datablock: " + getDataBlock() + " Control datablock" +  dlControl.getParentDataBlock()); 
	boolean can = getDataBlock() != null; 
	return can;
}


//	/**
//	 * An observer which waits until all detections within a segment have been added before passing the 
//	 * segment to a deep learning classifier. 
//	 */
//	public class GroupObserver extends PamObserverAdapter{
//		
//		private DLOfflineTask dlOfflinetask;
//
//		public GroupObserver( DLOfflineTask dlOfflinetask) {
//			this.dlOfflinetask=dlOfflinetask;
//		}
//		
//
//		@Override
//		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
//			SegmenterDetectionGroup segmenterDetectionGroup = (SegmenterDetectionGroup) pamDataUnit;
//			
//			System.out.println("HELLO NEW GROUP DATA!!" + segmenterDetectionGroup.getSubDetectionsCount()); 
//		}
//
//
//		@Override
//		public String getObserverName() {
//			return "Deep Learning Offline Group Process";
//		}
//
//
//
//		
//	}


}
