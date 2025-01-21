package tethys.tasks;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;
import tethys.TethysControl;
import tethys.dbxml.ServerStatus;

/**
 * Base class for offline tasks in Tethys. These probably won't be made available
 * as tasks in Tethys, but will be picked up by the batch processor which can then 
 * execute them across multiple datasets. <p>
 * These will have to be set up with their own complete sets of parameters, i.e. all the
 * information that was normally going into the export Wizards, so that they can be 
 * fully automated from the batch processor. 
 * @author dg50
 *
 */
public abstract class TethysTask extends OfflineTask {

	private TethysControl tethysControl;
	private TethysTaskManager tethysTaskManager;
	
	protected String whyNot = null;

	public TethysTask(TethysControl tethysControl, TethysTaskManager tethysTaskManager, PamDataBlock parentDataBlock) {
		super(tethysControl, parentDataBlock);
		this.tethysControl = tethysControl;
		this.tethysTaskManager = tethysTaskManager;
	}


	/**
	 * Check the Tethys server, reset the whyNot flag to null, return
	 * false is server not OK. Expect to override / extend in concrete export tasks. 
	 */
	@Override
	public boolean canRun() {
		whyNot = null;
		ServerStatus serverStatus = tethysControl.checkServer();
		if (serverStatus.ok == false) {
			whyNot = "Tethys Server not running";
		}
		return serverStatus.ok;
	}


	@Override
	public String whyNot() {
		return whyNot;
	}


	/**
	 * @return the tethysControl
	 */
	public TethysControl getTethysControl() {
		return tethysControl;
	}


	/**
	 * @return the tethysTaskManager
	 */
	public TethysTaskManager getTethysTaskManager() {
		return tethysTaskManager;
	}




}
