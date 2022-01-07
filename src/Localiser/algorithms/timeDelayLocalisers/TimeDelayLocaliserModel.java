package Localiser.algorithms.timeDelayLocalisers;


public interface TimeDelayLocaliserModel {

	/**
	 * Runs the localisation algorithm an creates a results. The results are dependednt on the localisation class. 
	 */
	public void runAlgorithm();
	
	/**
	 * Change any settings in the algorithm. 
	 * @return
	 */
	public Boolean changeSettings();
	
	/**
	 * Stop the localiser. For MCMC, which can takes minutes to compute this is important. For other localisers, such as Simplex, this is unimportant and can be left blank. 
	 */
	public void stop();
	
}
