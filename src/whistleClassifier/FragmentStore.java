package whistleClassifier;

import pamMaths.PamHistogram;

/**
 * Store for lots of whistle fragments during whistle classification
 * @author Doug Gillespie
 *
 */

public interface FragmentStore {

	/**
	 * Add a whistle fragment to the store
	 * @param newFragment fragment
	 * @param time fragment time in milliseconds
	 */
	public void addFragemnt(WhistleFragment newFragment, long time);

	/**
	 * Prepare the store to receive new data. 
	 */
	public void prepareStore();
	
	/**
	 * Clear the store of existing data. 
	 */
	public void clearStore();
	
	/**
	 * Remove a fraction of the fragments from the store
	 * @param scaleFactor fraction of fragments ot leave
	 */
	public void scaleStoreData(double scaleFactor);
	
	/**
	 * Get the parameters (9 of them) describing the fragments in this store. 
	 * @return
	 */
	public double[] getParameterArray();
	
	/**
	 * Get one of the histograms for mean, STD and Skew
	 * @param iFit histogram number (0, 1 or 2)
	 * @return histogram object. 
	 */
	public PamHistogram getFitHistogram(int iFit);
	
	/**
	 * 
	 * @return the negative inflections histogram
	 */
	public PamHistogram getNegInflectionsHistogram();
	
	/**
	 * 
	 * @return the positive inflections histogram. 
	 */
	public PamHistogram getPosInflectionsHistogram();
	
	public double getFragmentCount();
	
}
