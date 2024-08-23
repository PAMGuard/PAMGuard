package ravendata;

import java.util.ArrayList;
import java.util.HashMap;

public class RavenDataRow {
	
	private int iRow;
	private String[] data;
	private Integer selection;
	private String view;
	private Integer channel;
	private double beginT;
	private double endT;
	private double f1;
	private double f2;
//	private int[] dataIndexes;
	private boolean unpackOK;
	private HashMap<String, String> extraData = new HashMap<>();

	// data on a row of raven data from a table. 
	public RavenDataRow(RavenFileReader ravenReader, int iRow, String[] data) {
		this.iRow = iRow;
		this.data = data;
//		this.dataIndexes = dataIndexes;
		unpackOK = unpackRow(ravenReader);
	}

	/**
	 * @return the iRow
	 */
	protected int getiRow() {
		return iRow;
	}

	/**
	 * @return the data
	 */
	protected String[] getData() {
		return data;
	}

	/**
	 * Get a String value from the given column.
	 * @param iCol
	 * @return
	 */
	public String getString(int iCol) {
		return data[iCol];
	}

	/**
	 * Read an Integer value from the given column.
	 * @param iCol
	 * @return
	 */
	public Integer getInteger(int iCol) {
		try {
			return Integer.valueOf(data[iCol]);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	/**
	 * Read a double value from the given column.
	 * @param iCol
	 * @return
	 */
	public Double getDouble(int iCol) {
		try {
			return Double.valueOf(data[iCol]);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Unpack the row into more useful columns using the column indexes. 
	 * @param ravenReader
	 * @return
	 */
	private boolean unpackRow(RavenFileReader ravenReader) {
		int[] mainIndexes = ravenReader.getMainIndexes();
		ArrayList<RavenColumnInfo> extraColumns = ravenReader.getExtraColumns();
		try {
			selection = getInteger(mainIndexes[0]);
			view = getString(mainIndexes[1]);
			channel = getInteger(mainIndexes[2]);
			beginT = getDouble(mainIndexes[3]);
			endT = getDouble(mainIndexes[4]);
			f1 = getDouble(mainIndexes[5]);
			f2 = getDouble(mainIndexes[6]);
			// and add all the extra data into a HashMap
			if (extraColumns == null) {
				return true;
			}
			for (RavenColumnInfo col : extraColumns) {
				String data = getString(col.ravenTableIndex);
				if (data != null) {
					col.maxStrLength = Math.max(col.maxStrLength, data.length());
					extraData.put(col.name, data);
				}
			}
			
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get data from the extras map. 
	 * @param name column name. 
	 * @return
	 */
	public String getExtraData(String name) {
		if (extraData == null) {
			return null;
		}
		return extraData.get(name);
	}

	/**
	 * @return the selection
	 */
	protected Integer getSelection() {
		return selection;
	}

	/**
	 * @return the view
	 */
	protected String getView() {
		return view;
	}

	/**
	 * @return the channel
	 */
	protected Integer getChannel() {
		return channel;
	}

	/**
	 * @return the beginT
	 */
	protected double getBeginT() {
		return beginT;
	}

	/**
	 * @return the endT
	 */
	protected double getEndT() {
		return endT;
	}

	/**
	 * @return the f1
	 */
	protected double getF1() {
		return f1;
	}

	/**
	 * @return the f2
	 */
	protected double getF2() {
		return f2;
	}

	
	/**
	 * @return the unpackOK
	 */
	public boolean isUnpackOK() {
		return unpackOK;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RavenDataRow == false) {
			return false;
		}
		RavenDataRow oth = (RavenDataRow) obj;
		return this.channel == oth.channel && this.beginT == oth.beginT && this.endT == oth.endT && this.f1 == oth.f1 && this.f2 == oth.f2;
	}

	public HashMap<String, String> getExtraData() {
		return extraData;
	}

}
