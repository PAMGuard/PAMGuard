/**
 * 
 */
package offlineProcessing.legacyremotestuff;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import offlineProcessing.OfflineTask;

/**
 * @author GrahamWeatherup
 * only run in Viewer mode to run all Offline Tasks
 */
public class OfflineProcessingProcess extends PamProcess{

	OfflineTask task;
	/**
	 * @param pamControlledUnit
	 * @param parentDataBlock
	 * @param task 
	 */
	public OfflineProcessingProcess(PamControlledUnit pamControlledUnit,
			PamDataBlock parentDataBlock, OfflineTask task) {
		super(pamControlledUnit, parentDataBlock);
		this.task=task;
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#pamStart()
	 */
	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#pamStop()
	 */
	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
}
