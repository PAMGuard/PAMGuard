package Array.layoutFX;

public interface ArrayChangeListener {
	
	int STREAMER_CHANGE = 0;
	int HYDROPHONE_CHANGE = 1;

	/**
	 * Called whenever a hydrophone or streamer changes.
	 * @param type - the type of change e.g. ArrayChangeListener.HYDROPHONE_CHANGE
	 * @param changedObject - the changed object - hydrophone or streamer property.
	 */
	public void arrayChanged(int type, Object changedObject);

}
