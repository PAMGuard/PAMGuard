package analoginput;

public interface AnalogInputObserver {

	/**
	 * Flag to say configuration has changed. 
	 */
	public void changedConfiguration();
	
	/**
	 * Send out a summary of all data about a read channel. 
	 * @param itemData
	 */
	public void changedData(ItemAllData itemData);
}
