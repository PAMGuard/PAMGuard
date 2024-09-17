package PamguardMVC;

import java.util.ListIterator;

import PamguardMVC.debug.Debug;

/**
 * An iterator that has a bit more functionality than a basic iterator. <br>
 * It's main enhancement is to use a channel map match to PamDataUnits, so that it only returns
 * data units that have an overlap in channels with the given channel map (which could be 0xFFFFFFFF).
 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.
 * @author Doug Gillespie
 *
 * @param <E>
 */
public abstract class PamDataUnitIterator<E extends PamDataUnit> implements ListIterator<E> {

	protected PamDataBlock<E> pamDataBlock;
	protected int chanOrSeqMap;
	protected long firstUnitTime = Long.MAX_VALUE; // start with these at opp ends so that
	protected long lastUnitTime = Long.MIN_VALUE;  // if there are no data, hasNext and hasPrev both fail. 
	protected long currentUnitTime = 0;
	protected ListIterator<E> listIterator;
	protected Object synchObject;
	
	/**
	 * An iterator that has a bit more functionality than a basic iterator. <br>
	 * It's main enhancement is to use a channel map match to PamDataUnits, so that it only returns
	 * data units that have an overlap in channels with the given channel map (which could be 0xFFFFFFFF).
	 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.  
	 * @param pamDataBlock Datablock containing the data
	 * @param chanOrSeqMap channel map (requires overlap, ot exact match)
	 * @param whereFrom start at beginning or end. 0 = beginning; -1 (or PamDatablock.ITERATOR_END) for the end
	 */
	public PamDataUnitIterator(PamDataBlock<E> pamDataBlock, int chanOrSeqMap, int whereFrom) {
		super();
		this.pamDataBlock = pamDataBlock;
		this.synchObject = pamDataBlock;
		this.chanOrSeqMap = chanOrSeqMap;
		// get the first and last unit times for this channel map 
		// so that the iterator knows that there is / isn't a previous / next. 
		E dataUnit = getFirstUnit(chanOrSeqMap);
		if (dataUnit != null) {
			firstUnitTime = dataUnit.getTimeMilliseconds();
		}
		dataUnit = getLastUnit(chanOrSeqMap);
		if (dataUnit != null) {
			lastUnitTime = dataUnit.getTimeMilliseconds();
		}
		listIterator = pamDataBlock.getListIterator(whereFrom);
		if (whereFrom == PamDataBlock.ITERATOR_END) {
			currentUnitTime = Long.MAX_VALUE;
		}
		else {
			currentUnitTime = Long.MIN_VALUE;
		}
	}

	/**
	 * Get the first unit for a specific channel or sequence map (any match of channels allowed). 
	 * @param chanOrSeqMap channel/sequence map
	 * @return first data unit with at least one channel matching, or null. 
	 */
	abstract public E getFirstUnit(int chanOrSeqMap);
	
	/**
	 * Get the last unit for a specific channel or sequence map (any match of channels allowed). 
	 * @param chanOrSeqMap channel/sequence map
	 * @return last data unit with at least one channel matching, or null. 
	 */
	abstract public E getLastUnit(int chanOrSeqMap);
	
	/**
	 * Return the next PamDataUnit, or null if there is no 'next' PamDataUnit
	 */
	@Override
	abstract public E next();

	/**
	 * Return the previous PamDataUnit, or null if there is no 'previous' PamDataUnit
	 */
	@Override
	abstract public E previous();
	

	
	@Override
	public boolean hasNext() {
		return (listIterator.hasNext() && currentUnitTime < lastUnitTime);
	}

	@Override
	public boolean hasPrevious() {
		return (listIterator.hasPrevious() && currentUnitTime > firstUnitTime);
	}

	/**
	 * Get the unit immediately before or at the given time. 
	 * @param timeMilliseconds time in milliseconds. 
	 * @param firstOk if this is true and no unit precedes the given time, it will 
	 * return the first unit, otherwise it will return null 
	 * @return a data unit, or null. 
	 */
	public E getPreceding(long timeMilliseconds, boolean firstOk) {
		
		Debug.out.println("-----Channel Iterator-----");

		/**
		 * First work forwards until we know we're after the unit
		 * we want
		 */
		E nextUnit = null;
		while (hasNext()) {
			nextUnit = next();
			Debug.out.println("Channel Iterator: next"  + nextUnit.getTimeMilliseconds());
			if (nextUnit.getTimeMilliseconds() > timeMilliseconds) {
				break;
			}
		}
		/**
		 * Then work backwards again until we're past it, but return the one
		 * extracted before the one we've found (if you get me ? ) 
		 */
		E prevUnit = null;
		while (hasPrevious()) {
			prevUnit = previous();
			Debug.out.println("Channel Iterator: prev"   + nextUnit.getTimeMilliseconds());
			if (prevUnit.getTimeMilliseconds() <= timeMilliseconds) {
				return prevUnit;
			}
		}
		
		Debug.out.println("---------------------");

		if (firstOk) {
			return getFirstUnit(chanOrSeqMap);
		}
		else {
			return null;
		}
	}

	/**
	 * Get the unit immediately following or at the given time. 
	 * @param timeMilliseconds time in milliseconds. 
	 * @param lastOk if this is true and no unit precedes the given time, it will 
	 * return the last unit, otherwise it will return null 
	 * @return a data unit, or null. 
	 */
	public E getFollowing(long timeMilliseconds, boolean lastOk) {
		/**
		 * First work backwards until we know we're before the unit
		 * we want
		 */
		E prevUnit = null;
		while (hasPrevious()) {
			prevUnit = previous();
			if (prevUnit.getTimeMilliseconds() < timeMilliseconds) {
				break;
			}
		}
		/**
		 * Then work forwards again. 
		 */
		E nextUnit = null;
		while (hasNext()) {
			nextUnit = next();
			if (nextUnit.getTimeMilliseconds() >= timeMilliseconds) {
				return nextUnit;
			}
		}
		// if it get's here it didn't find anything. 
		if (lastOk) {
			return getLastUnit(chanOrSeqMap);
		}
		else {
			return null;
		}
	}
	/**
	 * Get the unit closest to the given time. 
	 * @param timeMilliseconds time in milliseconds. 
	 * @param firstOrlastOk if this is true and no unit precedes the given time, it will 
	 * return the last unit, otherwise it will return null 
	 * @return a data unit, or null. 
	 */
	public E getClosest(long timeMilliseconds, boolean firstOrlastOk) {
		/**
		 * First work backwards until we know we're before the unit
		 * we want
		 */
		E prevUnit = null;
		while (hasPrevious()) {
			prevUnit = previous();
			if (prevUnit.getTimeMilliseconds() <= timeMilliseconds) {
				break;
			}
		}
		
		/**
		 * Then work forwards again. 
		 */
		E nextUnit = null;
		while (hasNext()) {
			nextUnit = next();
			if (nextUnit.getTimeMilliseconds() >= timeMilliseconds) {
				break;
			}
			else {
				prevUnit = nextUnit;
			}
		}
		if (prevUnit != null && nextUnit != null) {
			long t1 = Math.abs(prevUnit.getTimeMilliseconds() - timeMilliseconds);
			long t2 = Math.abs(nextUnit.getTimeMilliseconds() - timeMilliseconds);
			return (t1 < t2 ? prevUnit : nextUnit);
		}
		if (firstOrlastOk) {
			if (prevUnit != null) {
				return prevUnit;
			}
			else {
				return nextUnit;
			}
		}
		else {
			return null;
		}
	}
	@Override
	public int nextIndex() {
		return listIterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return listIterator.previousIndex();
	}

	@Override
	public void remove() {
		listIterator.remove();
	}

	@Override
	public void set(E e) {
		listIterator.set(e);
	}

	@Override
	public void add(E e) {
		listIterator.add(e);
	}

	/**
	 * Return the channel or sequence map specified during object instantiation
	 * @return the channel/sequence Map
	 */
	public int getChanOrSeqMap() {
		return chanOrSeqMap;
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock<E> getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the synchObject
	 */
	public Object getSynchObject() {
		return synchObject;
	}

	/**
	 * @param synchObject the synchObject to set
	 */
	public void setSynchObject(Object synchObject) {
		this.synchObject = synchObject;
	}

}
