package PamguardMVC;

import java.util.ListIterator;

/**
 * Class for finding data units in a reasonably controlled and
 * organised way. 
 * <p>
 * It sets up a list iterator at the start of the data block and 
 * then searches for each requested data unit from the unit after
 * the last unit requested. This will generally be an efficient way
 * of matching two almost ordered lists of units. 
 * <p>If the iterator gets to the end of the list without finding a match
 * it restarts at the beginning, only returning null (nothing found) when 
 * it has been right through the list exactly once. 
 * <p>The finder can work with a default mather of time and channel or with 
 * user created matchers which can use any criteria the user wants 
 * 
 * @see DataUnitMatcher
 * @see DefaultUnitMatcher;
 * @author Doug Gillespie
 *
 */
public class DataUnitFinder<Tunit extends PamDataUnit> {

	private DataUnitMatcher dataUnitMatcher;
	
	private PamDataBlock<Tunit> pamDataBlock;
	
	private ListIterator<Tunit> li;
	
	private Tunit lastUnit;
	
	private long firstUTC, lastUTC;
	
	/**
	 * Create a data unit finder that uses the default matcher. 
	 * @param pamDataBlock parent PamDataBLock
	 */
	public DataUnitFinder(PamDataBlock<Tunit> pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
		this.dataUnitMatcher = new DefaultUnitMatcher();
		setupList();
	}

	/**
	 * Create a data unit finder that uses a user created matcher
	 * @param pamDataBlock parent PamDataBlock
	 * @param dataUnitMatcher user created matcher
	 */
	public DataUnitFinder(PamDataBlock<Tunit> pamDataBlock, DataUnitMatcher dataUnitMatcher) {
		this.pamDataBlock = pamDataBlock;
		this.dataUnitMatcher = dataUnitMatcher;
		setupList();
	}
	
	protected void setupList() {
		li = pamDataBlock.getListIterator(0);
		Tunit firstUnit = pamDataBlock.getFirstUnit();
		if (firstUnit != null) {
			firstUTC = firstUnit.getTimeMilliseconds();
		}
		Tunit lastUnit = pamDataBlock.getLastUnit();
		if (lastUnit != null) {
			lastUTC = lastUnit.getTimeMilliseconds();
		}
	}
	
	/**
	 * Find a data unit which satisfies the criteria in 
	 * criteria. 
	 * <p>
	 * For the default search, the criteria are either just
	 * the time in milliseconds OR both the time AND the channel
	 * bitmap. 
	 * <p>
	 * For user defined matchers, the criteria will have to 
	 * match whatever the  matcher expects. 
	 * @param criteria variable number of matching criteria.
	 * @return the next data unit in the data block that matches
	 * those criteria. 
	 */
	public Tunit findDataUnit(Object... criteria) {
		Tunit currentUnit = lastUnit;
		int loops = 0;
		while (li.hasNext()) {
			lastUnit = li.next();
			if (li.hasNext() == false) {
				setupList();
				loops++;
				if (loops > 1) {
					lastUnit = null;
					return null;
				}
			}
			if (dataUnitMatcher.match(lastUnit, criteria) == 0) {
				return lastUnit;
			}
			if (lastUnit == currentUnit) {
				// this means it's been all the way round the loop. 
				return null;
			}
		}
		return null;
	}

	/**
	 * @return the dataUnitMatcher
	 */
	public DataUnitMatcher getDataUnitMatcher() {
		return dataUnitMatcher;
	}

	/**
	 * @param dataUnitMatcher the dataUnitMatcher to set
	 */
	public void setDataUnitMatcher(DataUnitMatcher dataUnitMatcher) {
		this.dataUnitMatcher = dataUnitMatcher;
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock<Tunit> getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the lastUnit
	 */
	public Tunit getLastUnit() {
		return lastUnit;
	}

	/**
	 * @return the list iterator
	 */
	protected ListIterator<Tunit> getListIterator() {
		return li;
	}

	/**
	 * @param li the list iterator to set
	 */
	protected void setListIterator(ListIterator<Tunit> li) {
		this.li = li;
	}

	/**
	 * Any chance at all that the data unit at this time might exist ? The 
	 * data in the block should be sorted by time, so all that's needed is a 
	 * quick compare with the times of the first and last units. 
	 * @param childUTC milisecond time of interest. 
	 * @return true if it's within the range of the first and last units. 
	 */
	public boolean inUTCRange(long childUTC) {
		return childUTC >= firstUTC && childUTC <= lastUTC;
	}
	
}
