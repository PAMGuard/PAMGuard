package PamguardMVC;

import java.util.ListIterator;;

/**
 * Iterator for going backwards through a datablock, but only selecting data untits that 
 * have a specific channel. 
 * @author Doug Gillespie
 *
 * @param <Tunit>
 */
public class ReverseChannelIterator<Tunit extends PamDataUnit> implements ListIterator<Tunit> {

	private ListIterator<Tunit> listIterator;
	
	private Tunit previousUnit;

	private int channelMap;
	
	public ReverseChannelIterator(PamDataBlock<Tunit> dataBlock, int channelMap) {
		listIterator = dataBlock.getListIterator(PamDataBlock.ITERATOR_END);
		this.channelMap = channelMap;
		previousUnit = findPrevious();
	}

	/**
	 * find the previous unit with an overlapping channel bitmap. 
	 * @return
	 */
	private Tunit findPrevious() {
		Tunit aUnit;
		while (listIterator.hasPrevious()) {
			aUnit = listIterator.previous();
			if ((aUnit.getChannelBitmap()&channelMap) != 0) {
				return aUnit;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Tunit next() {
		return null;
	}

	@Override
	public boolean hasPrevious() {
		return (previousUnit != null);
	}

	@Override
	public Tunit previous() {
		Tunit thisUnit = previousUnit;
		if (thisUnit != null) {
			previousUnit = findPrevious();
		}
		return thisUnit;
	}

	@Override
	public int nextIndex() {
		return 0;
	}

	@Override
	public int previousIndex() {
		return 0;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(Tunit e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Tunit e) {
		// TODO Auto-generated method stub
		
	}
}
