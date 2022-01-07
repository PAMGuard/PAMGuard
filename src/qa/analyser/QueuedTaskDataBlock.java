package qa.analyser;

import PamModel.PamModel;
import PamguardMVC.PamDataBlock;

public class QueuedTaskDataBlock extends PamDataBlock<QAQueuedTask> {

	private QAAnalyser qaAnalyser;

	public QueuedTaskDataBlock(QAAnalyser qaAnalyser) {
		super(QAQueuedTask.class, "Queued QA Analysis", qaAnalyser, 0);
		this.qaAnalyser = qaAnalyser;
	}

	@Override
	public boolean shouldNotify() {
		/**
		 * Override default behaviour so that notifications are also sent in viewer mode. 
		 */
		return true;
	}
	
	@Override
	public int getMaxThreadJitter() {
		return 3600000;
	}

}
