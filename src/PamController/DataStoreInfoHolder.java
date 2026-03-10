package PamController;

import PamUtils.worker.PamWorkMonitor;

public interface DataStoreInfoHolder {
	
	/**
	 * Get information about the input store (e.g. start times of all files). 
	 * @param workerMonitor monitor for work progress, should be called whenever a task
	 * is likely to take a long time so that a progress bar can update. 
	 * @param detail get detail of all files. 
	 * @return information about data input. 
	 */
	public InputStoreInfo getStoreInfo(PamWorkMonitor workerMonitor, boolean detail);
}
