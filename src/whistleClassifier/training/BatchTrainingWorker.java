package whistleClassifier.training;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

public class BatchTrainingWorker extends SwingWorker<Integer, BatchTrainingProgress> {
	private ClassifierTrainingDialog classifierTrainingDialog; 
	private BatchTrainingParams batchTrainingParams;
	private BatchTrainingMonitor batchTrainingMonitor;

	private volatile int totalTrials = 0;
	private volatile boolean emergencyStop;
	private volatile boolean isRunning;
	private volatile int bootstrapStatusValue;
	private long lastRunTime = 0;
	private long timeRemaining = 0;

	private ArrayList<BatchResultSet> batchResultSets = new ArrayList<BatchResultSet>();;

	public BatchTrainingWorker(ClassifierTrainingDialog classifierTrainingDialog, 
			BatchTrainingParams batchTrainingParams, BatchTrainingMonitor batchTrainingMonitor) {
		this.classifierTrainingDialog = classifierTrainingDialog;
		this.batchTrainingParams = batchTrainingParams;
		this.batchTrainingMonitor = batchTrainingMonitor;

		// will need to make sure the steps are all non zero to make sure that 
		// things do progress !
		if (batchTrainingParams.fragmentLength[1] <= 0) {
			batchTrainingParams.fragmentLength[1] = 1;
		}
		if (batchTrainingParams.sectionLength[1] <= 0) {
			batchTrainingParams.sectionLength[1] = 1;
		}
		if (batchTrainingParams.minProbability[1] <= 0) {
			batchTrainingParams.minProbability[1] = 1;
		}

		totalTrials = 0;
		for (int iFL = batchTrainingParams.fragmentLength[0]; 
				iFL <= batchTrainingParams.fragmentLength[2]; 
				iFL += batchTrainingParams.fragmentLength[1] ) {

			for (int iSL = batchTrainingParams.sectionLength[0]; 
					iSL <= batchTrainingParams.sectionLength[2]; 
					iSL += batchTrainingParams.sectionLength[1] ) {


				for (double minP = batchTrainingParams.minProbability[0]; 
						minP <= batchTrainingParams.minProbability[2]; 
						minP += batchTrainingParams.minProbability[1] ) {
					totalTrials ++;
				}

			}

		}

		batchTrainingMonitor.setBatchTrainingProgress(totalTrials, null);
	}
	@Override
	protected Integer doInBackground() throws Exception {
		emergencyStop = false;
		batchResultSets.clear();
		int iRun = 0;
		for (int iFL = batchTrainingParams.fragmentLength[0]; 
				iFL <= batchTrainingParams.fragmentLength[2]; 
				iFL += batchTrainingParams.fragmentLength[1] ) {
//			publish(new BatchTrainingProgress("loop Frag length = " + iFL ));
			for (int iSL = batchTrainingParams.sectionLength[0]; 
					iSL <= batchTrainingParams.sectionLength[2]; 
					iSL += batchTrainingParams.sectionLength[1] ) {
//				publish(new BatchTrainingProgress("loop Section length = " + iSL ));
				for (double minP = batchTrainingParams.minProbability[0]; 
						minP <= batchTrainingParams.minProbability[2]; 
						minP += batchTrainingParams.minProbability[1] ) {
//					publish(new BatchTrainingProgress("loop Min Prob = " + minP ));
					runBootstrap(iRun, iFL, iSL, minP);
//					publish(new BatchTrainingProgress("lBootstrap complete for run " + iRun ));
					iRun ++;
					timeRemaining = lastRunTime * (totalTrials - iRun);
					publish(new BatchTrainingProgress(timeRemaining));
					if (emergencyStop) {
						break;
					}
				}
				if (emergencyStop) {
					break;
				}
			}
			if (emergencyStop) {
				break;
			}
		}
		return null;
	}

	private boolean runBootstrap(int iRun, int fragmentLength, int sectionLength, double minProbability) {
		long startTime = System.currentTimeMillis();
		publish(new BatchTrainingProgress(iRun, false));
		try {
			if (classifierTrainingDialog.startBatchBootstrap(fragmentLength, sectionLength, minProbability)) {
				isRunning = true;
			}
			else {
				return false;
			}
			// then wait for it to complete
			while (isRunning) {
				Thread.sleep(1000);
				if (timeRemaining > 1000) {
					timeRemaining -= 1000;
					publish(new BatchTrainingProgress(timeRemaining));
				}
			}
			// now get all the results back from the classifier trainer somehow !!!

			ClassifierTrainer ct = classifierTrainingDialog.getClassifierTrainer();
			BatchResultSet brs = new BatchResultSet(fragmentLength, sectionLength, minProbability, 
					ct.getMeanConfusion(), ct.getSTDConfusion());
			batchResultSets.add(brs);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		publish(new BatchTrainingProgress(iRun, true));
		lastRunTime = System.currentTimeMillis() - startTime;
		return true;
	}


	@Override
	protected void done() {
		super.done();
		batchTrainingMonitor.doneTraining(batchResultSets);

	}


	@Override
	protected void process(List<BatchTrainingProgress> chunks) {
		for (int i = 0; i < chunks.size(); i++) {
			BatchTrainingProgress btp = chunks.get(i);
			batchTrainingMonitor.setBatchTrainingProgress(totalTrials, btp);
		}
	}
	public void stopNow() {
		emergencyStop = true;
	}

	/**
	 * this should get status messages back from the main bootstrap dialog. 
	 * These probably arrive in the AWT thread, so the swing worker thread will 
	 * just have to monitor these flags and wait for them to change before it 
	 * can proceed. 
	 * @param statusMessage
	 * @param statusValue
	 */
	public void setStatus(ClassifierTrainingProgress bootstrapProgress) {
		int status = bootstrapProgress.status;
		switch (status) {
		case ClassifierTrainingProgress.ABORT:
			isRunning = false;
			emergencyStop = true;
			break;
		case ClassifierTrainingProgress.COMPLETE_ALL:
			isRunning = false;
		}
	}

}
