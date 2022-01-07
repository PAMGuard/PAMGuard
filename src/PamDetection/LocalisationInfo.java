package PamDetection;

/**
 * Holds information about a localisation. e.g. this might be the type of information calculated, the way it was calculated etc. 
 * @author Jamie Macaulay
 *
 */
public interface LocalisationInfo  {
	
	/**
	 * Check if the localisation info contains the integer flag. 
	 * @param type - the type of localisation. 
	 * @return true if the localisation contains the flag. 
	 */
	public boolean hasLocContent(int type);
	
	/**
	 * Add a flag to the loclaisation.
	 * @param type the flag to add. 
	 */
	public void addLocContent(int type);

	/**
	 * Remove a lfag from the loclaisation
	 * @param type the flag to remove. 
	 * @return new or remaining localisation content flags. 
	 */
	public int removeLocContent(int type);
	
	/**
	 * The integer speciofying whihc flags are present in the loclaisation. 
	 * @return bitmap of loclaisation contents. 
	 */
	public int getLocContent(); 
	
	/**
	 * Set the overall bitmap flag. This deletes all porevious flags. 
	 * @param locContents - the obverall bitmap flag. 
	 */
	public void setLocContent(int locContents);

}
