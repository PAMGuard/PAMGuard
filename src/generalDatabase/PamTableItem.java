package generalDatabase;

import java.sql.Types;

//http://java.sun.com/j2se/1.4.2/docs/api/java/sql/Types.html
	
/**
 * Defines a single item (column) for a Pamguard database table
 * These are listed in PamTableDefinition for each table.
 * 
 * @author Doug Gillespie
 * @see generalDatabase.PamTableDefinition
 * 
 */
public class PamTableItem implements Cloneable {

	/**
	 * name of the database column
	 */
	private String name;
	
	/**
	 * the SQL type (as defined in java.sql.Types) for the database column
	 */
	private int sqlType;
	
	/**
	 * lengh of character type fields.
	 */
	private int length;
	
	/**
	 * required field (cannot be null)
	 */
	private boolean required;
	
	/**
	 * Is a primary key
	 */
	private boolean primaryKey = false;
	
	/** 
	 * IS an autoincrementing counter
	 * can only be used if sqlType is integer
	 */
	private boolean isCounter = false;
	
	/**
	 * Contains the last value logged to or read from the database. 
	 */
	private Object value;
	
	/*
	 * Reference to another PamTableItem in a different
	 * table. This must be of the same sqlType as this
	 * PamTableItem. When this PamTableItem is written 
	 * to the database, it's value will automatically
	 * be taken as the last value referenced by the
	 * crossREferenceItm.
	 */
	private PamTableItem crossReferenceItem;

	public PamTableItem(String name, int sqlType) {
		super();
		this.name = name;
		this.sqlType = sqlType;
		this.length = 0;
		this.required = false;
	}
	
	public PamTableItem(String name, int sqlType, int length) {
		super();
		this.name = name;
		this.sqlType = sqlType;
		this.length = length;
		this.required = false;
	}

	public PamTableItem(String name, int sqlType, int length, boolean required) {
		super();
		this.name = name;
		this.sqlType = sqlType;
		this.length = length;
		this.required = required;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Get the column name, any spaces in the name will be filled with 
	 * underscore characters
	 * @return a deblanked name
	 */
	public String getName() {
		return EmptyTableDefinition.deblankString(name);
	}
	
	/**
	 * Get the name with any spaces left in place.
	 * Should only be used when copying in data from old databases 
	 * and requires careful formatting before use in an SQL query. 
	 * @return the name with the spaces left in place
	 */
	public String getNameWithBlanks() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isCounter() {
		return isCounter;
	}

	public void setCounter(boolean isCounter) {
		this.isCounter = isCounter;
	}

	/**
	 * Gets the most recently used value written to or read
	 * from the database.
	 * @return data read from or written to the database column
	 */
	public Object getValue() {
//		if (value == null) {
//			return null;
//		}
		/**
		 * Deal with the timestamp problem here
		 */
//		switch (sqlType) {
//		case Types.TIMESTAMP:
//			return getTimestampValue();
//		}
		return value;
	}
//	
//	public Timestamp getTimestampValue(){
//		if (value instanceof Timestamp)
//			return (Timestamp) value;
//		if (value instanceof Long)
//			return new Timestamp((long) value);
//		return null;
//	}
	
	public String getDeblankedStringValue() {
		if (sqlType != Types.CHAR || value == null) {
			return null;
		}
		return ((String) value).trim();
	}

	/**
	 * Sets the value of data to be written to the
	 * database column.
	 * @param value
	 */
	public void setValue(Object value) {
		/*
		 * It doesn't seem to like empty strings - causes a total SQL crash !!!
		 * also need to check double types for NaN - which should be changed to null. 
		 */
		if (value == null) {
			this.value = null;
			return;
		}
		switch (sqlType) {
		case Types.CHAR:
			if (value != null) {
				String charObj = (String) value;
				if (charObj.length() == 0) {
//					this.value = new String(" ");
					this.value = null;
					return;
				}
				if (charObj.length() > this.length) {
					// string is too long, so truncate it.
//					try {
						value = charObj.substring(0, this.length-1);
//					}
//					catch(Exception e){
//						System.out.println("String: !!!!: " + this.name + "   " + charObj);
//						e.printStackTrace();
//					}
				}
//				else if (charObj.length() < this.length) {
//					value = charObj.
//				}
			}
			break;
		case Types.DOUBLE:
			Double d = checkDoubleType(value);
			if (d == null) {
//				System.out.println("Null value");
				return;
			}
			if (d == Double.NaN || d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY) {
				this.value = null;
				return;
			}
			break;
		}
		this.value = value;
	}
	
	/**
	 * Had some trouble with some old database which seems to have muddled some float and
	 * some Double columns. Check all float types and convert them to double - primatives 
	 * can be cast easily, but not the Object types.  
	 * @param value Numeric object of any type. 
	 * @return Numeric object converted to a Double. 
	 */
	private Double checkDoubleType(Object value) {
		if (value == null) {
			return null;
		}
		else if (value.getClass() == Double.class) {
			return (Double) value;
		}
		else if (value.getClass() == Float.class) {
			double primVal = (Float) value;
			return primVal;
		}
		else if (value.getClass() == Integer.class) {
			double primVal = (Integer) value;
			return primVal;
		}
		else if (value.getClass() == Long.class) {
			double primVal = (Long) value;
			return primVal;
		}
		else if (value.getClass() == Short.class) {
			double primVal = (Short) value;
			return primVal;
		}
		return null;
	}

	private String xRefTable, xRefColumn;
	/**
	 * Gets the cross reference item. If the item reference is null
	 * then the function searches for it based on previously set
	 * table and column names. 
	 * @return Table item the current item is cross referenced to
	 */
	public PamTableItem getCrossReferenceItem() {
		if (crossReferenceItem != null) return crossReferenceItem;
		else if (xRefTable != null && xRefColumn != null) {
			return setCrossReferenceItem(xRefTable, xRefColumn);
		}
		else return null;
	}

	/**
	 * Sets the cross reference item. Data from the crossREferenceItem
	 * will automatically be used when data are written to the database. 
	 * @param crossReferenceItem
	 * @return reference to the crossREferenceItem.
	 */
	public PamTableItem setCrossReferenceItem(PamTableItem crossReferenceItem) {
		this.crossReferenceItem = crossReferenceItem;
		return crossReferenceItem;
	}

	/**
	 * Sets the cross reference item. Data from the crossREferenceItem
	 * will automatically be used when data are written to the database.
	 * @param tableName name of the table to cross reference to
	 * @param columnName name of the column to cross reference to
	 * @return reference to the PamTableItem, or null if it can't be found. 
	 * If the cross reference item cannot be found it will be searched for 
	 * again when data are next required in getCrossReferenceItem
	 */
	public PamTableItem setCrossReferenceItem(String tableName, String columnName) {
		setCrossReferenceItem(findTableItem(tableName, columnName));
		if (crossReferenceItem == null) {
			xRefTable = tableName;
			xRefColumn = columnName;
		}
		return crossReferenceItem;
	}
//	
//	public void findCrossReferenceItem() {
//		
//		if (xRefTable == null || xRefColumn == null) return;
//		
//		setCrossReferenceItem(xRefTable, xRefColumn);
//		
//	}
	
	/**
	 * Searches all Pamguard datablocks and SQLLoggers for a named table and
	 * column for use in cross referencing. 
	 */
	public static PamTableItem findTableItem(String tableName, String columnName) {
		PamTableDefinition tableDef = EmptyTableDefinition.
			findTableDefinition(EmptyTableDefinition.deblankString(tableName));
		if (tableDef == null) return null;
		return tableDef.findTableItem(EmptyTableDefinition.deblankString(columnName));
	}

	public Short getShortValue() {
		if (value == null) {
			return null;
		}
		if (value.getClass() == String.class) {
			try {
				value = Integer.valueOf((String) value);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
		if (value.getClass() == Short.class) {
			return (Short) value;
		}
		else if (value.getClass() == Integer.class) {
			int intVal = (Integer) value;
			return new Short((short) intVal);
		}
		return (Short) value;
	}
	
	/**
	 * Returns an int, or 0 if the value is null
	 * @return
	 */
	public int getIntegerValue() {
		if (value == null) {
			return 0;
		}
		try {
		return (Integer) value;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Return an Integer object, or null if the value is null
	 * @return
	 */
	public Integer getIntegerObject() {
		if (value == null) {
			return null;
		}
		try {
		return (Integer) value;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	
	public long getLongValue() {
		Long longObj = getLongObject();
		if (longObj == null) {
			return 0;
		}
		return (Long) longObj;
	}
	
	/**
	 * Database is clever and returns Longs as ints if they are amll enough
	 * and as Doubles if they are too big !
	 * @return
	 */
	public Long getLongObject() {
		if (value == null) {
			return null;
		}
		if (value.getClass() == Long.class) {
			return (Long) value;
		}
		if (value.getClass() == Integer.class) {
			return Long.valueOf((int) value);
		}
		System.out.printf("PamTableItem having trouble converting number type %s to Long\n", value.getClass().getName());
		return null;
	}
	

	/**
	 * 
	 * @return the double value or Double.NaN if the value is null
	 */
	public double getDoubleValue() {
		if (value == null) {
			return Double.NaN;
		}
		return (Double) value;
	}
	
	
	public float getFloatValue() {
		if (value == null) {
			return Float.NaN;
		}
		if (value.getClass() == Double.class) {
			double dVal = (Double) value;
			return (float) dVal;
		}
		return (Float) value;
	}

	public boolean getBooleanValue() {
		if (value == null) {
			return false;
		}
//		System.out.println("Value class = " + value.getClass());
		if (value.getClass() == Integer.class) {
			return ((Integer)value > 0);
		}
		else if (value.getClass() == Boolean.class) {
			return (Boolean) value;
		}
		else if (value.getClass() == String.class) {
			String v = (String) value;
			if (v.length() < 1) {
				return false;
			}
			Character c = v.charAt(0);
			int i = c;
			return (i != 0);
		}
		else {
			return (value != null);
		}
	}
	
	public String getStringValue() {
		if (value == null) {
			return null;
		}
		return ((String) value).trim();
	}

	public Object getPackedValue() {
		if (getValue() == null || getSqlType() != Types.CHAR) {
			return getValue();
		}
		String strVal = (String) getValue();
		if (strVal.length() == length) {
			return value;
		}
		// otherwise pack it !
		StringBuffer sb = new StringBuffer((String) value);
		while (sb.length() < length) {
			sb = sb.append(' ');
		}
		return new String(sb);
		
	}

	@Override
	protected PamTableItem clone() {
		try {
			return (PamTableItem) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}
