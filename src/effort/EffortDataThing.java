package effort;

/**
 * Most effort things should be an extension of a PAMDataUnit, but work via
 * this interface just incase we can't always use a data unit. 
 * @author dg50
 *
 */
public interface EffortDataThing {

	public long getEffortStart();
	
	public long getEffortEnd();
	
}
