package cpod.dataSelector;

import cpod.CPODClick;

/**
 * Basic filter for CPOD data.
 * 
 * @author Jamie Macaulay 
 *
 */
public interface CPODDataFilter {
	
	/**
	 * Score the data from a CPOD click. 
	 * @param cpodClick - the CPOD click. 
	 * @param cpodFilterParams - parameters for scoring the data. 
	 * @return the score. 0 usually indicates false. 
	 */
	public int scoreData(CPODClick cpodClick, StandardCPODFilterParams cpodFilterParams);

}
