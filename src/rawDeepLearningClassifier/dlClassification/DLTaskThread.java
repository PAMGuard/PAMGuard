package rawDeepLearningClassifier.dlClassification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import rawDeepLearningClassifier.dlClassification.animalSpot.SoundSpotResult;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericPrediction;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;

/**
 * Creates a que for grouped data units for classiifcation. 
 * @author au671271
 *
 */
public abstract class DLTaskThread extends Thread {

	private AtomicBoolean run = new AtomicBoolean(true);
	
	/**
	 * The dl model worker. 
	 */
	private DLModelWorker dlModelWorker; 
	

	/**
	 * Holds a list of segmented raw data units which need to be classified. 
	 */
	private List<ArrayList<GroupedRawData>> queue = Collections.synchronizedList(new ArrayList<ArrayList<GroupedRawData>>());


	public DLTaskThread(DLModelWorker soundSpotWorker) {
		this.dlModelWorker=soundSpotWorker; 
	}

	public void stopTaskThread() {
		run.set(false);  
		//Clean up daemon.
		if (dlModelWorker!=null) {
			dlModelWorker.closeModel();
		}
		dlModelWorker = null; 
	}

	public void run() {
		while (run.get()) {
			//				System.out.println("ORCASPOT THREAD while: " + "The queue size is " + queue.size()); 
			try {
				if (queue.size()>0) {
					System.out.println("DL TASK THREAD: " + "The queue size is " + queue.size()); 
					ArrayList<GroupedRawData> groupedRawData = queue.remove(0);

					ArrayList<GenericPrediction> modelResult = dlModelWorker.runModel(groupedRawData, 
							groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); //TODO channel?

					for (int i =0; i<modelResult.size(); i++) {
//						modelResult.get(i).setClassNameID(getClassNameIDs()); 
//						modelResult.get(i).setBinaryClassification(isBinaryResult(modelResult.get(i))); 
						newResult(modelResult.get(i), groupedRawData.get(i));
					}

				}
				else {
					//						System.out.println("ORCASPOT THREAD SLEEP: "); ; 
					Thread.sleep(10);
					//						System.out.println("ORCASPOT THREAD DONE: "); ; 
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Called whenever there is a new result. 
	 * @param soundSpotResult - the new result.
	 * @param groupedRawData - the grouped data unit. 
	 */
	public abstract void newResult(GenericPrediction soundSpotResult, GroupedRawData groupedRawData); 

	/**
	 * Get the grouped data queue
	 * @return
	 */
	public List<ArrayList<GroupedRawData>> getQueue() {
		return queue;
	}

	public void setQueue(List<ArrayList<GroupedRawData>> queue) {
		this.queue = queue;
	}

}