package generalDatabase.external.crossreference;

import java.util.ArrayList;

/**
 * A set of cross reference items that may need to be updated. 
 * @author Doug
 *
 */
public class CrossReferenceSet {
	
	Object newReference;
	Object oldReference;
	ArrayList<Object> xItems;
	/**
	 * @param oldReference
	 * @param newReference
	 */
	public CrossReferenceSet(Object oldReference, Object newReference) {
		super();
		this.oldReference = oldReference;
		this.newReference = newReference;
		xItems = new ArrayList<>();
	}
	
	public void addItem(Object item) {
		xItems.add(item);
	}
	
	/**
	 * Get the number of groups of items. 
	 * @return number of groups of items. 
	 */
	public int getSize() {
		return xItems.size();
	}
	
}
