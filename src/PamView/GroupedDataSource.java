package PamView;

/**
 * Interface to add to datablocks which have channel grouped data. 
 * This will allow the beam former or other down stream processes
 * to learn about channel grouping in detectors they are monitoring. 
 * @author Doug Gillespie
 *
 */
public interface GroupedDataSource {

	/**
	 * @return the current group source parameters for this data source. 
	 */
	public GroupedSourceParameters getGroupSourceParameters();
}
