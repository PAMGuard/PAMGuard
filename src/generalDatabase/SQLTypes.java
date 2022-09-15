package generalDatabase;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.TimeZone;

import PamUtils.PamCalendar;

/**
 * Utilities for converting between java.sql.types numeric
 * and text formats.
 * <p>
 * SQL format can be slightly different between different databases. 
 * For example, MS Access allows you to put column names in "" which 
 * then enables you to use otherwise reserved words. MySQL on the 
 * other hand, will not let you do this.  
 * <p>
 * This base class contains some default behaviours, but expect them 
 * to be overridden in many instances.  
 * @author Doug Gillespie
 *
 * @see java.sql.Types
 *
 */
public class SQLTypes {
	
	private boolean allowBlanks = false; 

	public String typeToString(PamTableItem tableItem){
		return typeToString(tableItem.getSqlType(),tableItem.getLength(),tableItem.isCounter());
	}

	/**
	 * Converts a numeric SQL type and length to a text string
	 * that can be used in SQL statements. 
	 * The length parameter is generally only required by text and
	 * character types. 
	 * @param sqlType SQL type as defined in java.sql.Types 
	 * @param length length of character and text fields 
	 * @return string representation of the type
	 */
	public String typeToString(int sqlType, int length) {
		return typeToString(sqlType, length, false);
	}



	public String typeToString(int sqlType, int length, boolean counter) {
		switch (sqlType) {
		case Types.ARRAY:
			return "ARRAY";
		case Types.BIGINT:
			return "BIGINT";
		case Types.BINARY:
			return "BINARY";
		case Types.BIT:
			return "BIT";
		case Types.BLOB:
			return "BLOB";
		case Types.BOOLEAN:
			return "BOOLEAN";
		case Types.CHAR:
			return "CHAR(" + length + ")";
		case Types.CLOB:
			return "CLOB";
		case Types.DATALINK:
			return "DATALINK";
		case Types.DATE:
			return "DATE";
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.DISTINCT:
			return "DISTINCT";
		case Types.DOUBLE:
			return "DOUBLE";
		case Types.FLOAT:
			return "DOUBLE";
		case Types.INTEGER:
			if (counter) {
				return "COUNTER";
			}
			return "INTEGER";
		case Types.JAVA_OBJECT:
			return "JAVA_OBJECT";
		case Types.LONGVARBINARY:
			return "LONGVARBINARY(" + length + ")";
		case Types.LONGVARCHAR:
			return "LONGVARCHAR(" + length + ")";
		case Types.NULL:
			return "NULL";
		case Types.NUMERIC:
			return "NUMERIC";
		case Types.OTHER:
			return "OTHER";
		case Types.REAL:
			return "REAL";
		case Types.REF:
			return "REF";
		case Types.SMALLINT:
			return "SMALLINT";
		case Types.STRUCT:
			return "STRUCT";
		case Types.TIME:
			return "TIME";
		case Types.TIMESTAMP:
			return "TIMESTAMP";
		case Types.TINYINT:
			return "TINYINT";
		case Types.VARBINARY:
			return "VARBINARY(" + length + ")";
		case Types.VARCHAR:
			return "VARCHAR(" + length + ")";
		}
		return null;
	}

	/**
	 * Work out some column information from the column result set. 
	 * @param columnData
	 * @param cmd
	 * @return
	 */
	public PamTableItem createTableItem(ResultSet columnData, ResultSetMetaData cmd) {
		/**
		 * Unsurprisingly, different dbms's pack data in different ways, for instance for CHAR data SQLite
		 * always sets colLength to 20000 and includes the column length in the typeName (.e.g CHAR(20))
		 * wheras UCanAccess would have the typeName as VARCHAR without any length information and would
		 * have correctly set the column length information. Problem only seems to be with CHAR data, so 
		 * hopefully can just deal with here rather than having to overwrite entire method for each dbms. 
		 */
		String cName = null;
		String typeName = null;
		int colType = 0;
		int colLength = 0;
		Integer nullable = null;
		try {
			cName = columnData.getString(4);
			colType = columnData.getInt(5);
			typeName = columnData.getString(6);
			colLength= columnData.getInt(7);
			nullable = columnData.getInt(11);
			// print out all the otherones since it's all going a bit nuts for CAHR data ...
//			if (typeName.contains("CHAR")) {
//				cmd = columnData.getMetaData();
//				int nCol = cmd.getColumnCount();
//				int[] toSkip = {4,5,6,7,11};
//				System.out.printf("Meta data format information for cName %s, type %d, teypName %s, colLength %d, nullable %d\n",
//						cName, colType, typeName, colLength, nullable);
//				for (int i = 0; i < nCol; i++) {
//					System.out.printf("Column %d %s ", i+1, cmd.getColumnName(i+1));
//					for (int j = 0; j <toSkip.length; j++) {
//						if (i+1 == toSkip[j]) {
//							System.out.printf("\n");
//							continue;
//						}
//					}
//					System.out.printf(" " + columnData.getObject(i+1) + "\n");
//				}
//				System.out.println("End of Meta data format information for " + cName);
//			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		int type = stringToType(typeName);
		int length = stringToLength(typeName); // works for SQLite
		if (length <= 0) {
			length = colLength; // works for UCaAccess. 
		}
		boolean isNullable = true;
		if (nullable != null & nullable == 0) {
			isNullable = false;
		}
		
		PamTableItem tableItem = new PamTableItem(cName, type, length);
		if (type == Types.INTEGER && !isNullable) {
//			tableItem.setCounter(true);
		}

		if (cName.equals("Id")) {
			tableItem.setPrimaryKey(true);
			tableItem.setCounter(true);
		}

		return tableItem;
	}

	public int stringToLength(String typeName) {
		int bPos = typeName.indexOf('(');
		if (bPos < 0) {
			return -1;
		}
		int b2Pos = typeName.indexOf(')');
		if (b2Pos < 0) {
			return -1;
		}
		String numberBit = typeName.substring(bPos+1, b2Pos);
		try {
			return Integer.valueOf(numberBit);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	public int stringToType(String typeName) {
		// strip off anything after ( and get the text before it.
		int bPos = typeName.indexOf('(');
		if (bPos > 0) {
			typeName = typeName.substring(0, bPos);
		}
		switch (typeName) {
		case "ARRAY":
			return Types.ARRAY;
		case "BIGINT":
			return Types.BIGINT;
		case "BINARY":
			return Types.BINARY;
		case "BIT":
			return Types.BIT;
		case "BLOB":
			return Types.BLOB;
		case "BOOLEAN":
			return Types.BOOLEAN;
		case "CHAR":
			return Types.CHAR;
		case "CLOB":
			return Types.CLOB;
		case "DATALINK":
			return Types.DATALINK;
		case "DATE":
			return Types.DATE;
		case "DECIMAL":
			return Types.DECIMAL;
		case "DISTINCT":
			return Types.DISTINCT;
		case "DOUBLE":
			return Types.DOUBLE;
		case "FLOAT":
			return Types.DOUBLE;
		case "INTEGER":
		case "INT":
			return Types.INTEGER;
		case "COUNTER":
			// may need some extra code to distinguish counters from integers. 
			return Types.INTEGER;
		case "JAVA_OBJECT":
			return Types.JAVA_OBJECT;
		case "LONGVARBINARY":
			return Types.LONGVARBINARY;
		case "LONGVARCHAR":
			return Types.LONGVARCHAR;
		case "NULL":
			return Types.NULL;
		case "NUMERIC":
			return Types.NUMERIC;
		case "OTHER":
			return Types.OTHER;
		case "REAL":
			return Types.REAL;
		case "REF":
			return Types.REF;
		case "SMALLINT":
			return Types.SMALLINT;
		case "STRUCT":
			return Types.STRUCT;
		case "TIME":
			return Types.TIME;
		case "TIMESTAMP":
			return Types.TIMESTAMP;
		case "TINYINT":
			return Types.TINYINT;
		case "VARBINARY":
			return Types.VARBINARY;
		case "VARCHAR": // not used in PAMguard, always CHAR. 
			return Types.CHAR;
		}
		return 0;
	}
	
	/**
	 * Format a column name from a table item. N.B that this 
	 * functin can use the 'allowblanks' variable to make 
	 * column names with blanks - allow blanks can be used for reading and 
	 * converting old databases, but not recommended for anything being output
	 * from PAMGuard.  
	 * 
	 * @param tableItem
	 * @return a formatted column name. 
	 */
	public synchronized String formatColumnName(PamTableItem tableItem) {
		if (allowBlanks) {
			return formatColumnName(tableItem.getNameWithBlanks());
		}
		else {
			return formatColumnName(tableItem.getName());
		}
	}

	/**
	 * Format the column name. Formats may be slightly different for 
	 * different DBMS's. e.g. MS Access can put quotes around names. 
	 * OODB requires them to be all upper case, etc. 
	 * @param columnName
	 * @return formatted column name. 
	 */
	public synchronized String formatColumnName(String columnName) {
		return columnName;
	}
	
	/**
	 * Any bespoke formatting of table names
	 * @param tableName input table name
	 * @return modified table name (e.g. to lower case)
	 */
	public synchronized String formatTableName(String tableName) {
		return tableName;
	}

	/**
	 * Some SQL types are unavailable on some systems, so qutomatically
	 * swap them for something else. 
	 * @param sqlType
	 * @return
	 */
	public int systemSqlType(int sqlType) {
		return sqlType;
	}

	/**
	 * Get a time object in the appropriate format for whichever 
	 * database we're using. 
	 * @param timeMillis Time in milliseconds. 
	 * @return Appropriate object (Generally TimeStamp, but SWLite uses Long) 
	 */
	public Object getTimeStamp(Long timeMillis) {
		/**
		 * This has just got nasty WRT time zones. 
		 * When the TimeStamp is written to database it uses the default time zone correction, which of course 
		 * I don't want since I only do UTC. I was therefore subtracting this off before creating the ts 
		 * so that it all worked fine when it was added back on again. 
		 * This was fine for many years until someone processed data from exactly when the clocks went 
		 * forward in the spring. Because the data were just after the clocks going forward, it took off
		 * an hour, then failed to add it back on again since the time was now before daylight saving. 
		 * Amazed this has never happened before. Well done G and E ! I can fix it by setting the 
		 * default time zone to UTC when PAMGuard starts, but note that all future references to local time
		 * will then be UTC. If I try to change it temporarily it doesn't help since the Timestamp always 
		 * goes to the default, so it needs to be on UTC at the moment data are written to the database, not
		 * just in this function. 
		 */
//		return timeMillis;
		if (timeMillis == null) {
			return null;
		}
//		return PamCalendar.formatDBDateTime(timeMillis, false);
		TimeZone tz = TimeZone.getDefault();
		Timestamp ts = new UTCTimestamp(timeMillis - tz.getOffset(timeMillis));
		return ts;
	}

	/**
	 * Get a local timestamp using system default time zone. 
	 * @param millis time in milliseconds. 
	 * @return local timestamp. 
	 */
	public Object getLocalTimeStamp(long millis) {
		return new Timestamp(millis);
	}

	/**
	 * convert a time stamp read from a database into milliseconds, bu tonly rounded to the nearest second
	 * @param timestamp GMT timestamp
	 * @return time in milliseconds. 
	 */
	public static Long millisFromTimeStamp(Object timeValue) {
		// this is used when reading back data from database.
		// it's totally unclear to me whether I need to correct for time zone
		// based on the time my computer is at, or when the data were at !
		if (timeValue instanceof Timestamp) {
			Timestamp timestamp = (Timestamp) timeValue;
			TimeZone tz = TimeZone.getDefault();
			return timestamp.getTime() + tz.getOffset(timestamp.getTime());
		}
		else if (timeValue instanceof String) {
			return PamCalendar.millisFromDateString((String) timeValue, false);
		}
		if (timeValue instanceof Long) {
			return (Long) timeValue;
		}
		return null;
	}


	/**
	 * Format the time for insertion into a WHERE %s BETWEEN %s clause, mostly
	 * used by the Viewer. This has been wrapped here so that we can support a 
	 * different format for SQLLite
	 * @param timeMilliseconds Time in milliseconds
	 * @return String to insert into a where clause. 
	 */
	public String formatDBDateTimeQueryString(long timeMilliseconds) {
		return "{ts '" + PamCalendar.formatDBDateTime(timeMilliseconds) + "'}";
		//		return PamCalendar.formatDBDateTimeQueryString(timeMilliseconds);
	}
	
	/**
	 * Allow slightly different formatting when we're trying to get an exact 
	 * match of a milliseconds time. 
	 * @param timeMilliseconds time in milliseconds. 
	 * @return formatted time string. 
	 */
	public String formatDateTimeMatchString(long timeMilliseconds) {
		return formatDBDateTimeQueryString(timeMilliseconds);
	}

	/**
	 * Try to read a long value - which might be an Integer or 
	 * some other type depending on the database. 
	 * @param object Hopefully Long, but my be something else
	 * @return Long or null. 
	 */
	public Long getLongValue(Object object) {
		if (object == null) {
			return null;
		}
		if (Long.class.isAssignableFrom(object.getClass())) {
			return (Long) object;
		}
		else if (Integer.class.isAssignableFrom(object.getClass())) {
			int intVal = (Integer) object;
			return new Long(intVal);
		}
		else if (Double.class.isAssignableFrom(object.getClass())) {
			// SQLite seems to be returning this as a double but really the bytes are of a long
			double doubleVal = (Double) object;
			long longVal = (long) doubleVal;
//			long longVal = (long) (doubleVal+0.1);
			return longVal;
		}
		else {
			String strVal = object.toString();
			try {
				long lVal = Long.valueOf(strVal);
				return lVal;
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
	}
	
	/**
	 * Make a Float object from a Double
	 * @param value Double value
	 * @return Float value
	 */
	public Float makeFloat(Double value) {
		if (value == null) {
			return null;
		}
		return new Float(value);
	}
	
	/**
	 * Try to make a Float object out of any 
	 * type of object. 
	 * @param value value
	 * @return Float object or null. 
	 */
	public Float makeFloat(Object value) {
		if (value == null) {
			return null;
		}
		else if (value instanceof Float) {
			return (Float) value;
		}
		else if (value instanceof Double) {
			return new Float((Double) value);
		}
		else if (value instanceof Integer) {
			return new Float((Integer) value);
		}
		else if (value instanceof Long) {
			return new Float((Long) value);
		}
		else {
			try {
				return Float.valueOf(value.toString());
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
	}
	
	/**
	 * Make a Double object from a Float
	 * @param value Float value
	 * @return Double value
	 */
	public Double makeDouble(Float value) {
		if (value == null) {
			return null;
		}
		return new Double(value);
	}

	/**
	 * Try to make a Double value out of any 
	 * type of object. 
	 * @param value value
	 * @return Float object or null. 
	 */
	public Double makeDouble(Object value) {
		if (value == null) {
			return null;
		}
		else if (value instanceof Double) {
			return (Double) value;
		}
		else if (value instanceof Float) {
			return new Double((Float) value);
		}
		else if (value instanceof Integer) {
			return new Double((Integer) value);
		}
		else if (value instanceof Long) {
			return new Double((Long) value);
		}
		else {
			try {
				return Double.valueOf(value.toString());
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
	}

	/**
	 * @return the allowBlanks
	 */
	public boolean isAllowBlanks() {
		return allowBlanks;
	}

	/**
	 * @param allowBlanks the allowBlanks to set
	 */
	public void setAllowBlanks(boolean allowBlanks) {
		this.allowBlanks = allowBlanks;
	}

	/**
	 * Make an 'IN'list, without it's column name,e.g.
	 * IN ('SPW','DO','HP')
	 * @param array of objects to include in the list. 
	 * @return combined list. 
	 */
	public String makeInList(Object[] array) {
		String str = "IN (";
		for (int i = 0;i < array.length;i++) {
			if (i == 0) {
				str += String.format("'%s'", array[i].toString());
			}
			else {
				str += String.format(",'%s'", array[i].toString());
			}
		}
		str+=")";
		return str;
	}
}
