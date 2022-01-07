package generalDatabase.lookupTables;

/**
 * Change listener for PAMGuard Lookup items. 
 * @author dg50
 *
 */
public interface LookupChangeListener {

	/**
	 * Change has occurred. 
	 * @param selectedItem Currently selected item. 
	 */
	public void lookupChange(LookupItem selectedItem);
	
}
