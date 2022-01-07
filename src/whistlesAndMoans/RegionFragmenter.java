package whistlesAndMoans;

/**
 * Method for breaking up connected regions which contain multiple
 * crossing whistles into sensible parts. 
 * @author Doug Gillespie
 *
 */
public interface RegionFragmenter {

	/**
	 * Fragment a connected region
	 * @param connectedRegion region to fragment
	 * @return number of fragments. 
	 */
	public int fragmentRegion(ConnectedRegion connectedRegion);
	
	/**
	 * Get the number of fragments
	 * @return the number of fragments
	 */
	public int getNumFragments();
	
	/**
	 * Get a specific fragment
	 * @param iFragment fragment number (0 to getNumFragments);
	 * @return the ith fragment
	 */
	public ConnectedRegion getFragment(int iFragment);
	
}
