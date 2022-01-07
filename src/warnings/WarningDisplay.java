package warnings;

/**
 * Interface for warning displays.<p> 
 * Warning displays will receive a notification whenever warnings
 * are added, removed or updated and should then update.<p>
 * When accessing the list of warnings via the list iterator, code
 * should be contained within a block synchronized on the warning system
 * to avoid array list concurrent modification exceptions.     
 * @author Doug Gillespie
 *
 */
public interface WarningDisplay {

	public void updateWarnings();

}
