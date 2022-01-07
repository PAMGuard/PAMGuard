package PamUtils;

import java.sql.Types;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

/**
 * Set of functions to set up reading and writing of 
 * latitude longitude and depth data to a database table. 
 * Saves repeating lots of code lots of time. 
 * @author dg50
 *
 */
public class LatLongDatabaseSet {
	
	public static final int VERTICAL_NONE = 0;
	public static final int VERTICAL_HEIGHT = 1;
	public static final int VERTICAL_DEPTH = 2;
	
	private String prefix;
	private int verticalOption;
	private boolean shortNames;
	
	private String longLatName = "Latitude";
	private String shortLatName = "Lat";
	private String longLongName = "Longitude";
	private String shortLongName = "Lon";
	private String verticalName = null;
	
	private PamTableItem latTableItem, longTableItem, vertTableItem;
	private int sqlType;

	/**
	 * Construct a set of PamTableItems for writing and reading LatLong data from 
	 * a database. 
	 * @param prefix prefix to add to start of each column name. 
	 * @param verticalOption can be one of VERTICAL_NONE, VERTICAL_HEIGHT or VERTCAI_DEPTH
	 * @param useDoubles write double values, otherwise will be float which has an effort of about 1.5m at 180 deg. E or W. 
	 * @param shortNames short names ("Lat", "Lon" instead of "Latitude", "Longitude").
	 */
	public LatLongDatabaseSet(String prefix, int verticalOption, boolean useDoubles, boolean shortNames) {
		this.verticalOption = verticalOption;
		this.shortNames = shortNames;
		this.prefix = prefix;
		verticalName = makeBaseVerticalName(); 
		sqlType = Types.REAL;
		if (useDoubles) {
			sqlType = Types.DOUBLE;
		}
	}
	
	/**
	 * Add the table items to a PAM Table Definition
	 * @param tableDefinition table definition
	 * @return added table items (you should never need to access these directly). 
	 */
	public PamTableItem[] addTableItems(EmptyTableDefinition tableDefinition) {
		PamTableItem[] tableItems;
		if (verticalOption > 0) {
			tableItems = new PamTableItem[3];
		}
		else {
			tableItems = new PamTableItem[2];
		}

		latTableItem = new PamTableItem(makeLatName(), sqlType);
		tableDefinition.addTableItem(tableItems[0] = latTableItem);
		longTableItem = new PamTableItem(makeLongName(), sqlType);
		tableDefinition.addTableItem(tableItems[1] = longTableItem);
		if (verticalOption > 0) {
			vertTableItem = new PamTableItem(makeVerticalName(), sqlType);
			tableDefinition.addTableItem(tableItems[2] = vertTableItem);
		}
		return tableItems;
	}

	/**
	 * Set the position data in the table items. Generally 
	 * this will be called from SQLLogging.setTableData(...)
	 * @param latLong lat long, can be null in which case null will be written to each column.
	 */
	public void setLatLongData(LatLong latLong) {
		if (latLong == null) {
			latTableItem.setValue(null);
			longTableItem.setValue(null);
			if (vertTableItem != null) {
				vertTableItem.setValue(null);
			}
			return;
		}
		else if (sqlType == Types.REAL) {
			latTableItem.setValue((float) latLong.getLatitude());
			longTableItem.setValue((float) latLong.getLongitude());
			if (vertTableItem != null) {
				vertTableItem.setValue((float) makeVerticalValue(latLong.height));
			}
			return;
		}
		else if (sqlType == Types.DOUBLE) {
			latTableItem.setValue(latLong.getLatitude());
			longTableItem.setValue(latLong.getLongitude());
			if (vertTableItem != null) {
				vertTableItem.setValue(makeVerticalValue(latLong.height));
			}
			return;
		}
		else {
			setLatLongData(null);
		}
	}
	
	/**
	 * Read the LatLong from the database columns. Generally this will be called
	 * from SQLLogging.createDataUnit(...)
	 * @param sqlTypes SQLTypes from database connection (probably not needed)
	 * @return LatLong object or null if either the Lat or Long columns have a null value. 
	 * If the vertical is null, the LatLong will still be returned with 0 height.  
	 */
	public LatLong getLatLongData(SQLTypes sqlTypes) {
		if (latTableItem.getValue() == null || longTableItem.getValue() == null) {
			return null;
		}
		try {
			double lat, lon;
			double vert = 0;
			if (sqlType == Types.REAL) {
				lat = latTableItem.getFloatValue();
				lon = longTableItem.getFloatValue();
				if (verticalOption > 0) {
					vert = vertTableItem.getFloatValue();
				}
			}
			else {
				lat = latTableItem.getDoubleValue();
				lon = longTableItem.getDoubleValue();
				if (verticalOption > 0) {
					vert = vertTableItem.getDoubleValue();
				}
			}
			if (verticalOption == VERTICAL_DEPTH) {
				vert = -vert;
			}
			return new LatLong(lat, lon, vert);
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	
	private double makeVerticalValue(double height) {
		if (verticalOption == VERTICAL_DEPTH) {
			return -height;
		}
		else {
			return height;
		}
	}

	private String makeVerticalName() {
		if (prefix != null) {
			return prefix + verticalName;
		}
		else {
			return verticalName;
		}
	}

	private String makeLatName() {
		if (shortNames) {
			if (prefix == null || prefix.length() == 0) {
				return shortLatName; 
			}
			else {
				return prefix + shortLatName;
			}
		}
		else {
			if (prefix == null || prefix.length() == 0) {
				return longLatName; // can't use "Long" since it's a reserved word !
			}
			else {
				return prefix + longLatName;
			}
		}
	}

	private String makeLongName() {
		if (shortNames) {
			if (prefix == null || prefix.length() == 0) {
				return shortLongName; // can't use "Long" since it's a reserved word !
			}
			else {
				return prefix + shortLongName;
			}
		}
		else {
			if (prefix == null || prefix.length() == 0) {
				return longLongName; // can't use "Long" since it's a reserved word !
			}
			else {
				return prefix + longLongName;
			}
		}
	}

	private String makeBaseVerticalName() {
		switch (verticalOption) {
		case VERTICAL_NONE:
			return null;
		case VERTICAL_DEPTH:
			return "Depth";
		case VERTICAL_HEIGHT:
			return "Height";
		}
		return null;
	}

	/**
	 * @return the longLatName
	 */
	public String getLongLatName() {
		return longLatName;
	}

	/**
	 * @param longLatName the longLatName to set
	 */
	public void setLongLatName(String longLatName) {
		this.longLatName = longLatName;
	}

	/**
	 * @return the shortLatName
	 */
	public String getShortLatName() {
		return shortLatName;
	}

	/**
	 * @param shortLatName the shortLatName to set
	 */
	public void setShortLatName(String shortLatName) {
		this.shortLatName = shortLatName;
	}

	/**
	 * @return the longLongName
	 */
	public String getLongLongName() {
		return longLongName;
	}

	/**
	 * @param longLongName the longLongName to set
	 */
	public void setLongLongName(String longLongName) {
		this.longLongName = longLongName;
	}

	/**
	 * @return the shortLongName
	 */
	public String getShortLongName() {
		return shortLongName;
	}

	/**
	 * @param shortLongName the shortLongName to set
	 */
	public void setShortLongName(String shortLongName) {
		this.shortLongName = shortLongName;
	}

	/**
	 * @return the verticalName
	 */
	public String getVerticalName() {
		return verticalName;
	}

	/**
	 * @param verticalName the verticalName to set
	 */
	public void setVerticalName(String verticalName) {
		this.verticalName = verticalName;
	}

	/**
	 * @return the verticalOption
	 */
	public int getVerticalOption() {
		return verticalOption;
	}

	/**
	 * @return the shortNames
	 */
	public boolean isShortNames() {
		return shortNames;
	}

	/**
	 * @return the latTableItem
	 */
	public PamTableItem getLatTableItem() {
		return latTableItem;
	}

	/**
	 * @return the longTableItem
	 */
	public PamTableItem getLongTableItem() {
		return longTableItem;
	}

	/**
	 * @return the vertTableItem
	 */
	public PamTableItem getVertTableItem() {
		return vertTableItem;
	}

	/**
	 * @return the sqlType
	 */
	public int getSqlType() {
		return sqlType;
	}
	
	

}
