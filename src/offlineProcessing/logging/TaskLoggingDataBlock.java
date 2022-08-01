package offlineProcessing.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Dummy datablock to use with some dummy SQLLogging classes in TaskLogging. 
 * @author dg50
 *
 */
public class TaskLoggingDataBlock extends PamDataBlock {

	public TaskLoggingDataBlock() {
		super(TaskLoggingData.class, "Logging data", null, 0);
	}

}
