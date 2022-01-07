package PamguardMVC;

import java.util.Arrays;

/**
 * Class which can tell a data store what type of work is 
 * needed to re-save the data. 
 * @author Doug Gillespie
 *
 */
public class SaveRequirements {
	
	private PamDataBlock pamDataBlock;
	
	public SaveRequirements(PamDataBlock pamDataBlock) {
		super();
		this.pamDataBlock = pamDataBlock;
	}

	protected int numUpdates;

	protected long firstUpdateTime;
	
	protected long lastUpdateTime;
	
	protected long firstUpdateIndex;
	
	protected long lastUpdateIndex;

	protected long firstDeleteTime;
	
	protected long lastDeleteTime;
	
	protected long firstDeleteIndex;
	
	protected long lastDeleteIndex;
	
	protected int numAdditions;
		
	protected int[] deleteIndexes;
	
	protected int[] updatedIndexes = new int[0];

	/**
	 * @return the total number of changes - additions, updated and deletions.
	 */
	public int getTotalChanges() {
		return getNumAdditions() + getNumUpdates() + getNumDeletions();
	}
	/**
	 * @return the numUpdates
	 */
	public int getNumUpdates() {
		return numUpdates;
	}

	/**
	 * @param numUpdates the numUpdates to set
	 */
	public void setNumUpdates(int numUpdates) {
		this.numUpdates = numUpdates;
	}

	/**
	 * @return the firstUpdateTime
	 */
	public long getFirstUpdateTime() {
		return firstUpdateTime;
	}

	/**
	 * @param firstUpdateTime the firstUpdateTime to set
	 */
	public void setFirstUpdateTime(long firstUpdateTime) {
		this.firstUpdateTime = firstUpdateTime;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * @param lastUpdateTime the lastUpdateTime to set
	 */
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	/**
	 * @return the firstUpdateIndex
	 */
	public long getFirstUpdateIndex() {
		return firstUpdateIndex;
	}

	/**
	 * @param firstUpdateIndex the firstUpdateIndex to set
	 */
	public void setFirstUpdateIndex(long firstUpdateIndex) {
		this.firstUpdateIndex = firstUpdateIndex;
	}

	/**
	 * @return the lastUpdateIndex
	 */
	public long getLastUpdateIndex() {
		return lastUpdateIndex;
	}

	/**
	 * @param lastUpdateIndex the lastUpdateIndex to set
	 */
	public void setLastUpdateIndex(long lastUpdateIndex) {
		this.lastUpdateIndex = lastUpdateIndex;
	}

	/**
	 * @return the firstDeleteTime
	 */
	public long getFirstDeleteTime() {
		return firstDeleteTime;
	}
	/**
	 * @param firstDeleteTime the firstDeleteTime to set
	 */
	public void setFirstDeleteTime(long firstDeleteTime) {
		this.firstDeleteTime = firstDeleteTime;
	}
	/**
	 * @return the lastDeleteTime
	 */
	public long getLastDeleteTime() {
		return lastDeleteTime;
	}
	/**
	 * @param lastDeleteTime the lastDeleteTime to set
	 */
	public void setLastDeleteTime(long lastDeleteTime) {
		this.lastDeleteTime = lastDeleteTime;
	}
	/**
	 * @return the firstDeleteIndex
	 */
	public long getFirstDeleteIndex() {
		return firstDeleteIndex;
	}
	/**
	 * @param firstDeleteIndex the firstDeleteIndex to set
	 */
	public void setFirstDeleteIndex(long firstDeleteIndex) {
		this.firstDeleteIndex = firstDeleteIndex;
	}
	/**
	 * @return the lastDeleteIndex
	 */
	public long getLastDeleteIndex() {
		return lastDeleteIndex;
	}
	/**
	 * @param lastDeleteIndex the lastDeleteIndex to set
	 */
	public void setLastDeleteIndex(long lastDeleteIndex) {
		this.lastDeleteIndex = lastDeleteIndex;
	}
	/**
	 * @return the numAdditions
	 */
	public int getNumAdditions() {
		return numAdditions;
	}

	/**
	 * @param numAdditions the numAdditions to set
	 */
	public void setNumAdditions(int numAdditions) {
		this.numAdditions = numAdditions;
	}
	
	/**
	 * @return the number of items listed for deletion. 
	 */
	public int getNumDeletions() {
		if (deleteIndexes == null) {
			return 0;
		}
		return deleteIndexes.length;
	}

	/**
	 * @return the deleteIndexes
	 */
	public int[] getDeleteIndexes() {
		return deleteIndexes;
	}
	
	/**
	 * Add an update unit
	 * <p> Lists the index and works out the first and last 
	 * times and indexes. 
	 * @param aUnit
	 */
	public void addUpdateUnit(PamDataUnit aUnit) {
		if (firstUpdateIndex == 0) {
			firstUpdateIndex = lastUpdateIndex = aUnit.getDatabaseIndex();
			firstUpdateTime = lastUpdateTime = aUnit.getTimeMilliseconds();
			numUpdates = 1;
		}
		else {
			firstUpdateIndex = Math.min(firstUpdateIndex, aUnit.getDatabaseIndex());
			lastUpdateIndex = Math.max(lastUpdateIndex, aUnit.getDatabaseIndex());
			firstUpdateTime = Math.min(firstUpdateTime, aUnit.getTimeMilliseconds());
			lastUpdateTime = Math.max(lastUpdateTime, aUnit.getTimeMilliseconds());
			numUpdates++;
		}

		int nU = updatedIndexes.length;
		updatedIndexes = Arrays.copyOf(updatedIndexes, nU+1);
		updatedIndexes[nU] = aUnit.getDatabaseIndex();
	}
	
	/**
	 * @return the updatedIndexes - a list of indexes which have been updated. 
	 */
	public int[] getUpdatedIndexes() {
		return updatedIndexes;
	}
	/**
	 * Add a deleted unit
	 * <p> Lists the index and works out the first and last 
	 * times and indexes. 
	 * @param aUnit
	 */
	public void addDeletedUnit(PamDataUnit aUnit) {
		if (firstDeleteIndex == 0) {
			firstDeleteIndex = lastDeleteIndex = aUnit.getDatabaseIndex();
			firstDeleteTime = lastDeleteTime = aUnit.getTimeMilliseconds();
		}
		else {
			firstDeleteIndex = Math.min(firstDeleteIndex, aUnit.getDatabaseIndex());
			lastDeleteIndex = Math.max(lastDeleteIndex, aUnit.getDatabaseIndex());
			firstDeleteTime = Math.min(firstDeleteTime, aUnit.getTimeMilliseconds());
			lastDeleteTime = Math.max(lastDeleteTime, aUnit.getTimeMilliseconds());
		}
		addDeleteIndexes(aUnit.getDatabaseIndex());
	}

	/**
	 * @param deleteIndexes add a new index to the delete list
	 */
	public void addDeleteIndexes(int deleteIndex) {
		int n = getNumDeletions();
		if (n == 0) {
			deleteIndexes = new int[1];
		}
		else {
			deleteIndexes = Arrays.copyOf(deleteIndexes, n+1);
		}
		deleteIndexes[n] = deleteIndex;
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}
	
}
