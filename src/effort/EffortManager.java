package effort;

import java.util.ArrayList;

import PamController.PamController;
import PamguardMVC.PamDataBlock;

/**
 * Singleton class to provide management for all types of Effort in PAMGuard. 
 * @author dg50
 *
 */
public class EffortManager {

	private static EffortManager singleInstance;
	
	private EffortManager() {
		
	}
	
	/**
	 * Get singleton instance of EffortManager
	 * @return
	 */
	public static EffortManager getEffortManager() {
		if (singleInstance == null) {
			singleInstance = new EffortManager();
		}
		return singleInstance;
	}
	
	/**
	 * Get a list of all data blocks that might provide effort data. 
	 * @return
	 */
	public ArrayList<PamDataBlock> getEffortDataBlocks() {
		ArrayList<PamDataBlock> allBlocks = getPamController().getDataBlocks();
		ArrayList<PamDataBlock> effortBlocks = new ArrayList();
		for (PamDataBlock aBlock : allBlocks) {
			if (aBlock.getEffortProvider() != null) {
				effortBlocks.add(aBlock);
			}
		}
		return effortBlocks;
	}
	
	private PamController getPamController() {
		return PamController.getInstance();
	}
}
