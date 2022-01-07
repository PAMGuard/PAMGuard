package PamUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import java.util.Vector;


public class PamSort {


	/**
	 * Function for sorting when a single data unit has been added to 
	 * the end of a list and needs to be moved to it's correct position. <br>
	 * This requires a maximum of a single pass through the data and can exit as 
	 * soon as the correct position is found. this has only been implemented for 
	 * Lists that implement RandomAccess, for other (Linked) lists, this would be
	 * very slow, so for those lists, it's reverting to a full sort. <br> 
	 * Primarily, this was written to more efficiently add new data units to super detections.
	 * @param anyList list with a single new data at it's end that must be sorted into position. Elements 
	 * must implement the Comparable interface.
	 */
	public static void oneIterationBackSort(Vector anyList) {
		Vector<Comparable> list = anyList;
		if (list == null || list.size() < 2) {
			return;
		}
		int pos = list.size()-1;
		Comparable theOne = list.get(pos);
		Comparable theOther;
		for (int p2 = pos-1; p2>=0; p2--, pos--) {
			theOther = list.get(p2);
			if (theOther.compareTo(theOne) < 0) {
				break;
			};
			list.set(pos, theOther);
		}
		list.set(pos, theOne);
	}
	
//	public static void oneIterationBackSort(ArrayList<Comparable> list) {
//		if (list == null || list.size() < 2) {
//			return;
//		}
//		int pos = list.size()-1;
//		Comparable theOne = list.get(pos);
//		Comparable theOther;
//		for (int p2 = pos-1; p2>=0; p2--, pos--) {
//			theOther = list.get(p2);
//			if (theOther.compareTo(theOne) < 0) {
//				break;
//			};
//			list.set(pos, theOther);
//		}
//		list.set(pos, theOne);
//	}
}
