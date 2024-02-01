package rawDeepLearningClassifier.dlClassification.orcaSpot;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.DLStatus;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;
import rawDeepLearningClassifier.dlClassification.DLDetection;
import rawDeepLearningClassifier.dlClassification.PredictionResult;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;
import rawDeepLearningClassifier.segmenter.SegmenterProcess.GroupedRawData;
import warnings.PamWarning;

/**
 * Calls python.exe to run a python script and then returns a result. 
 * 
 * OrcaSpot has been replaced by AnimalSpot - a more generic, faster and easier to setup and use version of the classifier. 
 * 
 * @author Jamie Macaulay
 *
 */
@Deprecated 
public class OrcaSpotClassifier implements DLClassiferModel, PamSettings {

	/**
	 * The maximum allowed queue size;
	 */
	public final static int MAX_QUEUE_SIZE = 10 ; 

	/**
	 * The data model control 
	 */
	private DLControl dlControl;

	/**
	 * The OrcaSpot worker that does all the heavy lifting. 
	 */
	//	private OrcaSpotWorkerExe2 orcaSpotWorker;

	/**
	 * The parameters for the OrcaSpot classifier. 
	 */
	private OrcaSpotParams2 orcaSpotParams;

	/**
	 * User interface components for the OrcaSpot classifier. 
	 */
	private OrcaSpotClassifierUI orcaSpotUI;

	/**
	 * The last prediciton
	 */
	private OrcaSpotModelResult lastPrediction; 

	//	private static final int THREAD_POOL_SIZE = 1; //keep this for now

	/**
	 * Hiolds a list of segmeneted raw data units which need to be classified. 
	 */
	private List<GroupedRawData> queue = Collections.synchronizedList(new ArrayList<GroupedRawData>());

	/**
	 * The current task thread.
	 */
	private TaskThread workerThread;


	public OrcaSpotClassifier(DLControl dlControl) {
		this.dlControl = dlControl; 
		orcaSpotParams = new OrcaSpotParams2(); 
		this.orcaSpotUI = new OrcaSpotClassifierUI(this); ;
		//load the previous settings
		PamSettingManager.getInstance().registerSettings(this);	
	}

	@Override
	public String getName() {
		return "OrcaSpot";
	}

	@Override
	public DLCLassiferModelUI getModelUI() {
		return orcaSpotUI;
	}

	@Override
	public Serializable getDLModelSettings() {
		return orcaSpotParams;
	}

	@Override
	public ArrayList<PredictionResult> runModel(ArrayList<GroupedRawData> rawDataUnits) {

		for (GroupedRawData groupedRawData: rawDataUnits){
			if (queue.size()>MAX_QUEUE_SIZE) {
				//we are not doing well - clear the buffer
				queue.clear();
			}
			queue.add(groupedRawData);


		}
		this.orcaSpotUI.notifyUpdate(-1);

		return null; 
	}


	@Override
	public void prepModel() {
		//make sure the parameters have the right sequence length set up. 
		double windowSize = dlControl.getDLParams().rawSampleSize/dlControl.getSegmenter().getSampleRate(); 
		orcaSpotParams.seq_len = String.valueOf(windowSize);
		orcaSpotParams.hop_size  = String.valueOf(windowSize); 

		//set the sample rate - decimation is done in Python code if needed
		orcaSpotParams.sampleRate = String.format("%d", (int) dlControl.getSegmenter().getSampleRate());

		//set the correct mode
		String mode = null;

		//use both detector and classifier
		if (orcaSpotParams.useClassifier & orcaSpotParams.useDetector) mode="1"; 
		//use the classifier only 
		if (orcaSpotParams.useClassifier & !orcaSpotParams.useDetector) mode = "2";
		//use detector only 
		if (!orcaSpotParams.useClassifier & orcaSpotParams.useDetector) mode = "0";

		if (mode==null) {
			System.err.print("ORCASPOT: something very wrong: Mode is null??");
		}
		orcaSpotParams.mode = mode; 


		if (workerThread!=null) {
			workerThread.stopTaskThread();
		}

		workerThread = new TaskThread();
		workerThread.setPriority(Thread.MAX_PRIORITY);
		workerThread.start();
	}

	/**
	 * Get OrcaSpot params
	 * @return the orca spot params. 
	 */
	public OrcaSpotParams2 getOrcaSpotParams() {
		return this.orcaSpotParams;
	}

	/**
	 * Set the OrcaSpot parameters. 
	 * @param orcaSpotParams2 - the new parameters to set. 
	 */
	public void setOrcaSpotParams(OrcaSpotParams2 orcaSpotParams2) {
		this.orcaSpotParams = orcaSpotParams2;
	}


	/**
	 * Called whenever there is a new result. 
	 * @param modelResult - the new model result.
	 * @param groupedRawData - the raw data from which the result was calculated.
	 */
	private synchronized void newOrcaSpotResult(OrcaSpotModelResult modelResult, GroupedRawData groupedRawData) {

		this.orcaSpotUI.notifyUpdate(-1);


		//check whether to set binary classification to true - mainly for graphics and downstream filtering of data. 
		modelResult.setBinaryClassification(modelResult.getPrediction()[0]>Double.valueOf(this.orcaSpotParams.threshold));

		//the result is added later to the data block - we are dumping the raw sound data here. 
		DLDataUnit dlDataUnit = new DLDataUnit(groupedRawData.getTimeMilliseconds(), groupedRawData.getChannelBitmap(), groupedRawData.getStartSample(),
				groupedRawData.getSampleDuration(), modelResult); 
		//send the raw data unit off to be classified!
		dlDataUnit.setFrequency(new double[] {0, dlControl.getDLClassifyProcess().getSampleRate()/2});
		dlDataUnit.setDurationInMilliseconds(groupedRawData.getDurationInMilliseconds()); 


		dlControl.getDLClassifyProcess().getDLPredictionDataBlock().addPamData(dlDataUnit);

		if (dlDataUnit.getPredicitionResult().isBinaryClassification()) {
			//send off to localised datablock 
			ArrayList<PredictionResult> modelResults = new ArrayList<PredictionResult>(); 
			modelResults.add(modelResult); 
			dlControl.getDLClassifyProcess().getDLDetectionDatablock().addPamData(new DLDetection(groupedRawData.getTimeMilliseconds(), 
					groupedRawData.getChannelBitmap(), groupedRawData.getStartSample(),
					groupedRawData.getSampleDuration(), modelResults, null));
		}

		groupedRawData= null; //just in case 


	}


	public class TaskThread extends Thread {

		private AtomicBoolean run = new AtomicBoolean(true);

		private OrcaSpotWorkerExe2 orcaSpotWorker;

		TaskThread() {
			super("TaskThread");
			if (orcaSpotWorker == null) {
				//create the daemons etc...
				this.orcaSpotWorker = new OrcaSpotWorkerExe2(orcaSpotParams); 	
			}
		}

		public void stopTaskThread() {
			run.set(false);  
			//Clean up daemon.
			if (orcaSpotWorker!=null) {
				orcaSpotWorker.closeOrcaSpotWorker();
			}
			orcaSpotWorker = null; 
		}

		public void run() {
			while (run.get()) {
				//				System.out.println("ORCASPOT THREAD while: " + "The queue size is " + queue.size()); 

				try {


					if (queue.size()>0) {
						//						System.out.println("ORCASPOT THREAD: " + "The queue size is " + queue.size()); 
						GroupedRawData groupedRawData = queue.remove(0);
						double[] data = groupedRawData.getRawData()[0]; 

						long timestart = System.currentTimeMillis(); 

						OrcaSpotModelResult modelResult = orcaSpotWorker.runOrcaSpot(data);

						newOrcaSpotResult(modelResult, groupedRawData);

						lastPrediction = modelResult; 

						long timeEnd = System.currentTimeMillis(); 

						System.out.println("ORCASPOT THREAD: " + "Time to run OrcaSpot: " + (timeEnd-timestart)); 
					}
					else {
						//						System.out.println("ORCASPOT THREAD SLEEP: "); ; 
						Thread.sleep(250);
						//						System.out.println("ORCASPOT THREAD DONE: "); ; 
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void closeModel() {
		if (workerThread!=null) {
			workerThread.stopTaskThread();
		}
	}

	@Override
	public String getUnitName() {
		return dlControl.getUnitName()+"_OrcaSpot"; 
	}

	@Override
	public String getUnitType() {
		return dlControl.getUnitType()+"_OrcaSpot";
	}

	@Override
	public Serializable getSettingsReference() {
		if (orcaSpotParams==null) {
			orcaSpotParams = new OrcaSpotParams2(); 
		}
		return orcaSpotParams;

	}

	@Override
	public long getSettingsVersion() {
		return OrcaSpotParams2.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		OrcaSpotParams2 newParameters = (OrcaSpotParams2) pamControlledUnitSettings.getSettings();
		if (newParameters!=null) {
			orcaSpotParams = newParameters.clone();
		}
		else orcaSpotParams = new OrcaSpotParams2(); 
		return true;
	}

	/**
	 * Get the current number of raw data units in the queue. 
	 * @return the number of dtaa units in the queue. 
	 */
	public int getRawDataQueue() {
		return this.queue.size();
	}

	/**
	 * Get the last prediction. Convenience function as this could be 
	 * acquired from the data block. 
	 * @return the last prediction. 
	 */
	public OrcaSpotModelResult getLastPrediction() {
		return lastPrediction;
	}

	@Override
	public int getNumClasses() {
		return 1;
	}

	@Override
	public DLClassName[] getClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DLControl getDLControl() {
		return dlControl;
	}

	@Override
	public DLStatus getModelStatus() {
		return null;
	}

//	@Override
//	public ArrayList<PamWarning> checkSettingsOK() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean isModelType(URI uri) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setModel(URI model) {
		// TODO Auto-generated method stub
		
	}

}
