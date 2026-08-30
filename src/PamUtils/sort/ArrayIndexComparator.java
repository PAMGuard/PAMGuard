package PamUtils.sort;

import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer>{

	private Object[] array;
	
	private Comparator comparator;
	
	/**
	 * @param array
	 */
	public ArrayIndexComparator(Object[] array) {
		super();
		this.array = array;
	}
	
	/**
	 * @param array
	 */
	public ArrayIndexComparator(Object[] array, Comparator comparator) {
		super();
		this.array = array;
		this.comparator = comparator;
	}
	
	protected Integer[] makeIndices() {
		Integer[] indices = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			indices[i] = i;
		}
		return indices;
	}

	@Override
	public int compare(Integer ind1, Integer ind2) {
		if (comparator != null) {
			return comparator.compare(array[ind1], array[ind2]);
		}
		else {
			return ((Comparable)array[ind1]).compareTo(array[ind2]);
		}
	}

}
