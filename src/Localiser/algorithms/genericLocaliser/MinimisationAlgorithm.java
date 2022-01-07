package Localiser.algorithms.genericLocaliser;

import Localiser.LocaliserPane;
import Localiser.algorithms.locErrors.LocaliserError;

/**
 * Minimisation algorithm. Designed to be used with MinimisationFunction; 
 * @author Jamie Macaulay
 *
 */
public interface MinimisationAlgorithm {
	
	//status of algorithm
	
	/**
	 * Preparing the algorithm
	 */
	public static final int PREPARING_ALGORTIHM=0; 

	/**
	 * The algorithm is running
	 */
	public static final int ALGORITHM_RUNNING=1; 
	
	/**
	 * The algorithm has finished and a result is present
	 */
	public static final int ALGORITHM_FINISHED=2; 

	/**
	 * The algorithm has finished but no result is present
	 */
	public static final int ALGORITHM_FINISHED_ERR=3; 
	
	/**
	 * Set the minimisation function. This sets up the problem for the algorithm to solve. 
	 * @param minFunc - the minimisation function specific to the problem whihc needs solved
	 */
	public void setMinimisationFunction(MinimisationFunction minFunc);
	
	/**
	 * Run the algorithm to find a solution to the set minimisation function. This 
	 * @return true if the algorithm competed sucessfully. 
	 */
	public boolean runAlgorithm();
	
	/**
	 * Get the result. There maybe multiple results if ambiguities exist. 
	 * The number of dimensions is defined in the minimisation function.
	 * @return the result
	 */
	public double[][] getResult();
	
	/**
	 * The chi2 value for each result
	 * @return the chi2 value for each result. 
	 */
	public double[] getChi2();
	
	/**
	 * Get the errors. Should be the same number of dimensions as the result. 
	 * If errors have not been calculated leave as null
	 * The number of dimensions is defined in the minimisation function.
	 * @return the errors in the result
	 */
	public LocaliserError[] getErrors(); 
	
	/**
	 * A progress function which can be used to update different threads. 
	 * @param status - the current status flag. 
	 * @param progress- the progress of the algorithm from 0 to 1.0. 
	 */
	public void notifyStatus(int status, double progress);
	
	/**
	 * A progress function which can be used to update different threads. 
	 * @param status - the current status falg. 
	 * @param progress- the progress of the algorithm from 0 to 1.0. 
	 */
	public boolean hasParams(); 
	
	/**
	 * A settings pane for the algorithm if it has user changeable parameters. 
	 * @return a settings pane. 
	 */
	public LocaliserPane<?> getSettingsPane();


}
