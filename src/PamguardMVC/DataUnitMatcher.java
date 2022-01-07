package PamguardMVC;

/**
 * Class for matching data units to a set of other 
 * criteria. Is used by DataUnitFinder
 * @see DataUnitFinder
 * @see DefaultUnitMatcher
 * @author Doug Gillespie
 *
 */
public interface DataUnitMatcher {

	/**
	 * Return an int, so we can see if we should move forwrard of back. Similar
	 * behaviour to a Java Comparator, where >0 means that the criteria are 
	 * greater than the dataUnit, probably meaning we're too far into the list, so 
	 * should back up, <= meaning we're not far enough, and 0 meaning a match. 
	 * @param dataUnit
	 * @param criteria
	 * @return
	 */
	int match(PamDataUnit dataUnit, Object... criteria);
	
}
