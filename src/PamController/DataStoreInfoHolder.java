package PamController;

public interface DataStoreInfoHolder {
	
	/**
	 * Get information about the input store (e.g. start times of all files). 
	 * @param detail
	 * @return information about data input. 
	 */
	public InputStoreInfo getStoreInfo(boolean detail);
}
