package Localiser.algorithms.genericLocaliser;

/**
 * Class which must be populated for various minimisation algorithms
 * <p>
 * Example. Might be used to create a chi2 value for a source at an [x y z]
 * location. The function would store observed time delays and then calculate
 * the time delays for a source at [x y z]. The observed and calculated time
 * delays would then be compared, generating a chi2 value.
 * 
 * @author Jamie Macaulay
 *
 */
public interface MinimisationFunction {
	
	/**
	 * Return the Chi2 value at a specified 'location'/ for a set of points which
	 * can be used to generated expected data that is then used to calculated a chi2
	 * value with observed data. Example location might be [x y z] location to
	 * generate time delays which are then compared to expected time delays.
	 * 
	 * @param location - the point at which chi2 value is to be calculated;
	 * @return the chi2 value.
	 */
	public double value(double[] location);
	
	/**
	 * Get the simulation dimensions e.g. if solving for [x y z] location getDim()=3; 
	 */
	public int getDim();
	
	/**
	 * Get starting position for search algorithm. 
	 * @return starting location with getDim() dimensions. 
	 */
	public double[] getStart();
	
	/**
	 * Get first search jump (often not needed)
	 * @return first jump location with getDim() dimensions. 
	 */
	public double[] getFirstStep(); 

}
