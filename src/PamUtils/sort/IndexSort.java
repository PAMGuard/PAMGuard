package PamUtils.sort;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

/**
 * get sorted indices of data, but leave underlying data unchanged. 
 */
public class IndexSort {

	/**
	 * Sort array in ascending order, but leave data in the array in place
	 * and return an index array of the order the data are in. 
	 * data must implement the Comparable interface, otherwise use sortArray(Object[] data, Comparator comparator)
	 * and provide a suitable comparator. 
	 * @param data
	 * @return index array
	 */
	public static int[] sortArray(Object[] data) {
		return sortArray(data, null);
	}
	
	/**
	 * Sort array in ascending order, but leave data in the array in place
	 * and return an index array of the order the data are in. 
	 * @param data
	 * @param comparator Comparator for the underlying data. 
	 * @return index array
	 */
	public static int[] sortArray(Object[] data, Comparator comparator) {
		ArrayIndexComparator aic = new ArrayIndexComparator(data, comparator);
		Integer[] inds = aic.makeIndices();
		Arrays.sort(inds, aic);
		// convert back to primitives
		return ArrayUtils.toPrimitive(inds);
	}
	
	/**
	 * Sort array of integers in ascending order, but leave data in the array in place
	 * and return an index array of the order the data are in. 
	 * @param data
	 * @return index array
	 */
	public static int[] sortArray(int[] data) {
		Integer[] objData = ArrayUtils.toObject(data);
		return sortArray(objData);
	}
	
	/**
	 * Sort array of doubles in ascending order, but leave data in the array in place
	 * and return an index array of the order the data are in. 
	 * @param data
	 * @return index array
	 */
	public static int[] sortArray(double[] data) {
		Double[] objData = ArrayUtils.toObject(data);
		return sortArray(objData);
	}
	
	/**
	 * Sort array of floats in ascending order, but leave data in the array in place
	 * and return an index array of the order the data are in. 
	 * @param data
	 * @return index array
	 */
	public static int[] sortArray(float[] data) {
		Float[] objData = ArrayUtils.toObject(data);
		return sortArray(objData);
	}
	
	
	
}
