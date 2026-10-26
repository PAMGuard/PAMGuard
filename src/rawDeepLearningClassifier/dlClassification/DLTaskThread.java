package rawDeepLearningClassifier.dlClassification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import PamguardMVC.PamDataUnit;
import rawDeepLearningClassifier.dlClassification.genericModel.DLModelWorker;
import rawDeepLearningClassifier.dlClassification.genericModel.StandardPrediction;
import rawDeepLearningClassifier.segmenter.GroupedRawData;

/**
 * Creates a que for grouped data units for classification. 
 * @author Jamie Macaulay
 *
 */
public abstract class DLTaskThread extends Thread {

	private AtomicBoolean run = new AtomicBoolean(true);

	/**
	 * True while a batch taken from the queue is being classified. 
	 */
	private AtomicBoolean busy = new AtomicBoolean(false);
	
	/**
	 * The dl model worker. 
	 */
	private DLModelWorker dlModelWorker; 
	

	/**
	 * Holds a list of segmented raw data units which need to be classified. 
	 */
	private List<ArrayList<? extends PamDataUnit>> queue = Collections.synchronizedList(new ArrayList<ArrayList<? extends PamDataUnit>>());


	public DLTaskThread(DLModelWorker soundSpotWorker) {
		this.dlModelWorker=soundSpotWorker; 
	}

	public void stopTaskThread() {
		run.set(false);  
		//Clean up daemon.
//		if (dlModelWorker!=null) {
//			dlModelWorker.closeModel();
//		}
		dlModelWorker = null; 
	}

	public void run() {
		while (run.get()) {
			//				System.out.println("ORCASPOT THREAD while: " + "The queue size is " + queue.size()); 
			try {
				if (queue.size()>0) {
//					System.out.println("DL TASK THREAD: " + "The queue size is " + queue.size()); 
					//set busy before removing from the queue so isIdle() never sees an empty queue and an idle thread mid-task. 
					busy.set(true);
					ArrayList<? extends PamDataUnit> groupedRawData = queue.remove(0);

					DLModelWorker worker = dlModelWorker; 
					if (worker!=null) {
						ArrayList<StandardPrediction> modelResult = worker.runModel(groupedRawData, 
								groupedRawData.get(0).getParentDataBlock().getSampleRate(), 0); //TODO channel?

						newDLResult(modelResult, groupedRawData);
					}
//					for (int i =0; i<modelResult.size(); i++) {
////						modelResult.get(i).setClassNameID(getClassNameIDs()); 
////						modelResult.get(i).setBinaryClassification(isBinaryResult(modelResult.get(i))); 
//						newDLResult(modelResult.get(i), groupedRawData.get(i));
//					}

				}
				else {
					//						System.out.println("ORCASPOT THREAD SLEEP: "); ; 
					Thread.sleep(1); // was 10 ms
					//						System.out.println("ORCASPOT THREAD DONE: "); ; 
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				busy.set(false);
			}
		}
	}

	/**
	 * Check whether the thread has finished all queued work. 
	 * @return true if the queue is empty and no batch is being classified. 
	 */
	public boolean isIdle() {
		return !busy.get() && queue.isEmpty(); 
	}

	/**
	 * Wait for all queued data to be classified and the results passed on. 
	 * @param maxWaitMillis - the maximum time to wait in milliseconds. 
	 * @return true if the queue emptied, false if timed out or the thread is not running. 
	 */
	public boolean waitForIdle(long maxWaitMillis) {
		long endTime = System.currentTimeMillis() + maxWaitMillis; 
		while (!isIdle()) {
			if (!isAlive() || !run.get() || System.currentTimeMillis()>endTime) {
				return false; 
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false; 
			}
		}
		return true; 
	}
	
	/**
	 * Called whenever there is a new result. ArrayList<StandardPrediction> modelResult 
	 * @param soundSpotResult - the new result.
	 * @param groupedRawData - the grouped data unit. 
	 */
	public abstract void newDLResult(ArrayList<StandardPrediction> modelResult, ArrayList<? extends PamDataUnit> groupedRawData); 

	/**
	 * Get the grouped data queue
	 * @return
	 */
	public List<ArrayList<? extends PamDataUnit>> getQueue() {
		return queue;
	}

	public void setQueue(List<ArrayList<? extends PamDataUnit>> queue) {
		this.queue = queue;
	}

}