package Localiser.detectionGroupLocaliser;

/**
 * Options that get passed into a detection group localiser. 
 * Created as an interface so that it can be added on to 
 * various othe roptions. 
 * @author dg50
 *
 */
public interface DetectionGroupOptions {

	
	/**
	 * Maximum number of clicks or other sound types in a TM analysis to 
	 * use in the optimisation algorithm. 
	 * @return Max number of datas to use. 
	 */
	public int getMaxLocalisationPoints();

	
}
