package PamView.tables;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

public class TableColumnWidthData implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private Integer[] widthData;
	
	private Hashtable<String, Integer> namedWidths = new Hashtable<String, Integer>();
	
	public Integer getColumnWidth(int iColumn, String columnName) {
		/*
		 * First try the hashtable, then if that fails to return, get the 
		 * number from the array. 
		 */
		if (columnName != null && namedWidths != null) {
			Integer w = namedWidths.get(columnName);
			if (w != null) {
				return w;
			}
		}
		if (widthData == null) {
			return null;
		}
		if (widthData.length <= iColumn) {
			return null;
		}
		return widthData[iColumn];
	}
	
	public void setColumnWidth(int iColumn, String columnName, Integer columnWidth) {
		if (widthData == null) {
			widthData = new Integer[iColumn+1];
		}
		if (widthData.length <= iColumn) {
			widthData = Arrays.copyOf(widthData, iColumn+1);
		}
		widthData[iColumn] = columnWidth;
		if (namedWidths == null) {
			namedWidths = new Hashtable<String, Integer>();
		}
		if (columnName != null) {
			namedWidths.put(columnName, columnWidth);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected TableColumnWidthData clone() {
		try {
			return (TableColumnWidthData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
