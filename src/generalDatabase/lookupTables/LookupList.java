package generalDatabase.lookupTables;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Handles information for a single list from the look up table
 * @author Doug Gillespie
 *
 */
public class LookupList implements Cloneable, Serializable, ManagedParameters {
	
	public static final long serialVersionUID = 1L;

	private Vector<LookupItem> lutList;
	
	private Vector<LookupItem> deletedItems;
	
	private String listTopic;
	
	public LookupList(String listTopic) {
		this.setListTopic(listTopic);
		lutList = new Vector<LookupItem>();
	}
	
	/**
	 * 
	 * @return the full list of LookupItems, including ones which are not currently selectable
	 */
	public Vector<LookupItem> getList() {
		return lutList;
	}
	
	/**
	 * This method was added only so that lutList will be automatically added to PamParameterSet
	 * in the getParameterSet method 
	 * @return
	 */
	public Vector<LookupItem> getLutList() {
		return lutList;
	}
	
	/**
	 * 
	 * @return the partial list of LookupItems, currently marked as selectable
	 */
	public Vector<LookupItem> getSelectedList() {
		Vector<LookupItem> s = new Vector<LookupItem>();
		LookupItem l;
		for (int i = 0; i < lutList.size(); i++) {
			l = lutList.get(i);
			if (l.isSelectable()) {
				s.add(l);
			}
		}
		return s;
	}
	
	/**
	 * Add an item to the list
	 * @param lutItem item to add to the list
	 */
	public void addItem(LookupItem lutItem) {
		lutList.add(lutItem);
	}
	
	/**
	 * Remove the first occurrence of an item from the list
	 * @param lutItem item to remove from the list
	 * @return true if the item was found and removed. 
	 */
	public boolean removeItem(LookupItem lutItem) {
		boolean b = lutList.remove(lutItem);
		if (b) {
			setDeleted(lutItem);
		}
		return b;
	}

	/**
	 * Remove an item at a specified index in the list
	 * @param lutItemIndex index of item to remove
	 * @return the removed item
	 */
	public LookupItem removeItem(int lutItemIndex) {
		LookupItem lutItem = lutList.remove(lutItemIndex);
		setDeleted(lutItem);
		return lutItem;
	}
	
	private void setDeleted(LookupItem lutItem) {
		if (lutItem == null) {
			return;
		}
		if (deletedItems == null) {
			deletedItems = new Vector<LookupItem>();
		}
		deletedItems.add(lutItem);
	}

	/**
	 * Sort the items by the database id value
	 */
	public void sortItemsById() {
		Collections.sort(lutList, new IdComparatator());
	}
	/**
	 * Sort the items by the order value in the look up table
	 */
	public void sortItemsByOrder() {
		Collections.sort(lutList, new OrderComparatator());
	}
	/**
	 * Sort the items by the code value in the look up table
	 * (case insensitive)
	 */
	public void sortItemsByCode() {
		Collections.sort(lutList, new CodeComparatator());
	}
	/**
	 * Sort the items by the text value in the look up table
	 * (case insensitive)
	 */
	public void sortItemsByText() {
		Collections.sort(lutList, new TextComparatator());
	}

	public void setListTopic(String listTopic) {
		this.listTopic = listTopic;
	}

	public String getListTopic() {
		return listTopic;
	}

	private class IdComparatator implements Comparator<LookupItem> {
		@Override
		public int compare(LookupItem o1, LookupItem o2) {
			return o1.getDatabaseId() - o2.getDatabaseId();
		}
	}
	private class OrderComparatator implements Comparator<LookupItem> {
		@Override
		public int compare(LookupItem o1, LookupItem o2) {
			return o1.getOrder() - o2.getOrder();
		}
	}
	private class CodeComparatator implements Comparator<LookupItem> {
		@Override
		public int compare(LookupItem o1, LookupItem o2) {
			return o1.getCode().compareToIgnoreCase(o2.getCode());
		}
	}
	private class TextComparatator implements Comparator<LookupItem> {
		@Override
		public int compare(LookupItem o1, LookupItem o2) {
			return o1.getText().compareToIgnoreCase(o2.getText());
		}
	}

	@Override
	protected LookupList clone() {
		try {
			LookupList newList = (LookupList) super.clone();
			newList.lutList = new Vector<LookupItem>();
			newList.lutList.addAll(lutList);
			return newList;
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	/**
	 * Check for repeated codes
	 * @return null if there are no repeats or the repeated code if it's repeated. 
	 */
	public String checkRepeatCodes() {
		int n = lutList.size();
		LookupItem i1, i2;
		for (int i = 0; i < n-1; i++) {
			i1 = lutList.get(i);
			if (i1.getCode() == null) {
				return null;
			}
			for (int j = i+1; j < n; j++) {
				i2 = lutList.get(j);
				if (i2.getCode() == null) {
					return null;
				}
				if (i1.getCode().equalsIgnoreCase(i2.getCode())) {
					return i1.getCode();
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Find the lookup item for a specified code. 
	 * @param code given code
	 * @return matching lookup item (case sensitive) 
	 */
	public LookupItem findSpeciesCode(String code) {
		for (LookupItem lutItem:lutList) {
			if (lutItem.getCode().equals(code)) {
				return lutItem;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param code species code.
	 * @return the index of a code. 
	 */
	public int indexOfCode(String code) {
		int i = 0;
		for (LookupItem lutItem:lutList) {
			if (lutItem.getCode().equals(code)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * Get a lookup item as a specified index
	 * @param index index of item
	 * @return lookup item
	 */
	public LookupItem getLookupItem(int index) {
		if (index < 0 || index >= lutList.size()) {
			return null;
		}
		return lutList.get(index);
	}

	/**
	 * @return the deletedItems
	 */
	public Vector<LookupItem> getDeletedItems() {
		return deletedItems;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
