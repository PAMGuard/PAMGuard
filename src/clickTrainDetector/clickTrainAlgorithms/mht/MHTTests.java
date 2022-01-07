package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Testing of some of the algorithms
 * @author Jamie Macaulay
 *
 */
public class MHTTests {

	public static List<Integer> sort(ArrayList<? extends Number> array) {
		TreeMap<Object, List<Integer>> map = new TreeMap<Object, List<Integer>>();
		for(int i = 0; i < array.size(); i++) {
		    List<Integer> ind = map.get(array.get(i));
		    if(ind == null){
		        ind = new ArrayList<Integer>();
		        map.put(array.get(i), ind);
		    }
		    ind.add(i);
		}

		// Now flatten the list
		List<Integer> indices = new ArrayList<Integer>();
		for(List<Integer> arr : map.values()) {
		    indices.addAll(arr);
		}
		
		return indices; 
	}
	
	
	public static <T> ArrayList<T> sort(ArrayList<? extends Number> arrayOrdered, ArrayList<T> arrayToSort) {
		List<Integer> indexSort = sort(arrayOrdered); 
		
		ArrayList<T> arraySorted = new ArrayList<T>(indexSort.size()); 
		//have to preallocate
		for (int i=0; i<indexSort.size(); i++) {
			arraySorted.add(arrayToSort.get(indexSort.get(i))); 
		}
		
		return arraySorted;
	}
	
	
	public static void main(String[] args) {
		
		ArrayList<Double> orderedList = new ArrayList<Double>(); 
		
		orderedList.add(4.1);
		orderedList.add(3.2);
		orderedList.add(2.1);
		orderedList.add(0.1);
		orderedList.add(1.3);
		
		List<Integer> index =  sort(orderedList); 
		for (int i=0; i<index.size(); i++) {
			System.out.print(index.get(i)+ " "); 
		}
		
		ArrayList<String> objectsToOrder = new ArrayList<String>(); 
		objectsToOrder.add("e");
		objectsToOrder.add("i");
		objectsToOrder.add("m");
		objectsToOrder.add("J");
		objectsToOrder.add("a");
		
		List<String> name =  sort(orderedList, objectsToOrder); 
		for (int i=0; i<name.size(); i++) {
			System.out.print(name.get(i)+ " "); 
		}
		
//		//using arrays 
//		final Integer[] idx = { 0, 1, 2, 3 };
//		final float[] data = { 1.7f, -0.3f,  2.1f,  0.5f };
//
//		Arrays.sort(idx, new Comparator<Integer>() {
//		    @Override public int compare(final Integer o1, final Integer o2) {
//		        return Float.compare(data[o1], data[o2]);
//		    }
//		});
//
//		for (int i=0; i<idx.length; i++) {
//			System.out.print(idx[i] + " "); 
//		}


	}

}
