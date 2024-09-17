package PamguardMVC;

import java.util.ListIterator;

/**
 * An iterator that has a bit more functionality than a basic iterator. <br>
 * It's main enhancement is to use a channel map match to PamDataUnits, so that it only returns
 * data units that have an overlap in channels with the given channel map (which could be 0xFFFFFFFF).
 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.
 * <br>This class specifically matches the channelMap field.  To match the sequenceMap, use
 * the SequenceIterator class instead.
 * @author Doug Gillespie
 */
public class ChannelIterator<E extends PamDataUnit> extends PamDataUnitIterator<E> {

	/**
	 * An iterator that has a bit more functionality than a basic iterator. <br>
	 * It's main enhancement is to use a channel map match to PamDataUnits, so that it only returns
	 * data units that have an overlap in channels with the given channel map (which could be 0xFFFFFFFF).
	 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.  
	 * <br>This class specifically matches the channelMap field.  To match the sequenceMap, use
	 * the SequenceIterator class instead.
	 * @param pamDataBlock Datablock containing the data
	 * @param channelMap channel map (requires overlap, or exact match)
	 * @param whereFrom start at beginning or end. 0 = beginning; -1 (or PamDatablock.ITERATOR_END) for the end
	 */
	public ChannelIterator(PamDataBlock<E> pamDataBlock, int channelMap, int whereFrom) {
		super(pamDataBlock, channelMap, whereFrom);
	}

	@Override
	synchronized public E getFirstUnit(int channelMap) {
		if (channelMap == 0) {
			return pamDataBlock.getLastUnit();
		}
		ListIterator<E> it = pamDataBlock.getListIterator(0);
		E unit;
		while (it.hasNext()) {
			unit = it.next();
			if ((unit.getChannelBitmap() & channelMap) != 0) {
				return unit;
			}
		}
		return null;
	}
	
	@Override
	synchronized public E getLastUnit(int channelMap) {
		if (channelMap == 0) {
			return pamDataBlock.getLastUnit();
		}
		ListIterator<E> it = pamDataBlock.getListIterator(pamDataBlock.getUnitsCount());
		E unit;
		while (it.hasPrevious()) {
			unit = it.previous();
			if ((unit.getChannelBitmap() & channelMap) != 0) {
				return unit;
			}
		}
		return null;
	}
	
	@Override
	public E next() {
		E pamDataUnit;
		while (listIterator.hasNext()) {
			pamDataUnit = listIterator.next();
			if ((pamDataUnit.getChannelBitmap() & this.chanOrSeqMap) != 0) {
				currentUnitTime = pamDataUnit.getTimeMilliseconds();
				return pamDataUnit;
			}
		}
		return null;
	}

	@Override
	public E previous() {
		E pamDataUnit;
		while (listIterator.hasPrevious()) {
			pamDataUnit = listIterator.previous();
			if ((pamDataUnit.getChannelBitmap() & this.chanOrSeqMap) != 0) {
				currentUnitTime = pamDataUnit.getTimeMilliseconds();
				return pamDataUnit;
			}
		}
		return null;
	}

}
