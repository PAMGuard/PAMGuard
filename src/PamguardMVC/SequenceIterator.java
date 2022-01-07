package PamguardMVC;

import java.util.ListIterator;

import Array.StreamerDataUnit;
import Array.streamerOrigin.StaticOriginMethod;

/**
 * An iterator that has a bit more functionality than a basic iterator. <br>
 * It's main enhancement is to use a channel map match to PamDataUnits, so that it only returns
 * data units that have an overlap in channels with the given channel map (which could be 0xFFFFFFFF).
 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.
 * <br>This class matches the sequenceMap field, which may default to the channelMap if the
 * sequenceMap == null.  To match specifically to the channelMap, use the ChannelIterator class instead.
 * @author Doug Gillespie
 */
public class SequenceIterator<E extends PamDataUnit> extends PamDataUnitIterator<E> {

	/**
	 * An iterator that has a bit more functionality than a basic iterator. <br>
	 * It's main enhancement is to use a sequence map match to PamDataUnits, so that it only returns
	 * data units that have an overlap in sequence numbers with the given sequence map (which could be 0xFFFFFFFF).
	 * <br>It also has a couple of extra searches to getPreceeding() and getFollowing() data units.  
	 * <br>This class matches the sequenceMap field, which may default to the channelMap if the
	 * sequenceMap == null.  To match specifically to the channelMap, use the ChannelIterator class instead.
	 * @param pamDataBlock Datablock containing the data
	 * @param sequenceMap sequence map (requires overlap, or exact match)
	 * @param whereFrom start at beginning or end. 0 = beginning; -1 (or PamDatablock.ITERATOR_END) for the end
	 */
	public SequenceIterator(PamDataBlock<E> pamDataBlock, int sequenceMap, int whereFrom) {
		super(pamDataBlock, sequenceMap, whereFrom);
	}

	@Override
	synchronized public E getFirstUnit(int sequenceMap) {
		if (sequenceMap == 0) {
			return pamDataBlock.getLastUnit();
		}
		ListIterator<E> it = pamDataBlock.getListIterator(0);
		E unit;
		while (it.hasNext()) {
			unit = it.next();
			if ((unit.getSequenceBitmap() & sequenceMap) != 0) {
				return unit;
			}
		}
		return null;
	}
	
	@Override
	synchronized public E getLastUnit(int sequenceMap) {
		if (sequenceMap == 0) {
			return pamDataBlock.getLastUnit();
		}
		ListIterator<E> it = pamDataBlock.getListIterator(pamDataBlock.getUnitsCount());
		E unit;
		while (it.hasPrevious()) {
			unit = it.previous();
			if ((unit.getSequenceBitmap() & sequenceMap) != 0) {
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
			if ((pamDataUnit.getSequenceBitmap() & this.chanOrSeqMap) != 0) {
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
			if ((pamDataUnit.getSequenceBitmap() & this.chanOrSeqMap) != 0) {
				currentUnitTime = pamDataUnit.getTimeMilliseconds();
				return pamDataUnit;
			}
		}
		return null;
	}

}
