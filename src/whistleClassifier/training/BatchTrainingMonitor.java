package whistleClassifier.training;

import java.util.List;

public interface BatchTrainingMonitor {

	void setBatchTrainingProgress(int totalRuns, BatchTrainingProgress batchTrainingProgress);
	
	void doneTraining(List<BatchResultSet> batchResults);
	
}
