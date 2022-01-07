package generalDatabase.sqlite;

import generalDatabase.SQLTypes;

import java.sql.Types;

import org.sqlite.SQLiteConfig;

import PamUtils.PamCalendar;

public class SqliteSQLTypes extends SQLTypes {
	
	protected static final SQLiteConfig.DateClass dateClass = SQLiteConfig.DateClass.TEXT;

	@Override
	public String typeToString(int sqlType, int length, boolean counter) {
		if (sqlType == Types.INTEGER && counter) {
			return "INTEGER NOT NULL";
		}
//		if (sqlType == Types.TIMESTAMP){
//			if (TIMEASSTRING) {
//				return super.typeToString(Types.CHAR, 30);
//			}
//			else {
//				return "TIMESTAMP";
//			}
//		}
		return super.typeToString(sqlType, length, counter);
	}
	/* (non-Javadoc)
	 * @see generalDatabase.SQLTypes#formatColumnName(java.lang.String)
	 */
	@Override
	public synchronized String formatColumnName(String columnName) {
		return "\"" + super.formatColumnName(columnName) + "\"";
	}
	
	@Override
	public int systemSqlType(int sqlType) {
		switch (sqlType) {
		case Types.BOOLEAN:
			return Types.BIT;
		}
		return super.systemSqlType(sqlType);
	}
	
	@Override
	public String formatDBDateTimeQueryString(long timeMilliseconds) {
		switch (dateClass) {
		case TEXT:
			int millis = (int) (timeMilliseconds%1000);
			return String.format("'%s.%03d'", PamCalendar.formatDBDateTime(timeMilliseconds), millis);
		case INTEGER:
			return Long.toString(timeMilliseconds);
		case REAL:
			// TODO. Work out what format get used here should we ever offer up choices as to what format
			// the database should take. For now though assume we're sticking rigidly with TEXT. 
			return "Unsupported Date Format. Contact Developers";
		}
		return null;
//		if (TIMEASSTRING) {
//			return "'" + PamCalendar.formatDBDateTime(timeMilliseconds) + "'";
//		}
//		else {
//			return Long.toString(timeMilliseconds);
//		}
//		return "'" + PamCalendar.formatDBDateTime(timeMilliseconds) + "'";
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLTypes#formatDateTimeMatchString(long)
	 */
	@Override
	public String formatDateTimeMatchString(long timeMilliseconds) {
		switch (dateClass) {
		case TEXT:
			long millis = timeMilliseconds%1000;
			return String.format("'%s.%03d'", PamCalendar.formatDBDateTime(timeMilliseconds), millis);
		case INTEGER:
			return Long.toString(timeMilliseconds);
		case REAL:
			// TODO. Work out what format get used here should we ever offer up choices as to what format
			// the database should take. For now though assume we're sticking rigidly with TEXT. 
			return "Unsupported Date Format. Contact Developers";
		}
		return null;
	}
	
//	@Override
//	public Object getTimeStamp(long timeMillis) {
//		if (TIMEASSTRING) {
//			return PamCalendar.formatDBDateTime(timeMillis);
//		}
//		else {
//			return super.getTimeStamp(timeMillis);
////			return (Long) timeMillis;
//		}
//	}
//	
//	@Override
//	public Object getLocalTimeStamp(long timeMillis) {
//		if (TIMEASSTRING) {
//			return PamCalendar.formatLocalDBDateTime(timeMillis);
//		}
//		else {
//			return super.getLocalTimeStamp(timeMillis);
////			return (Long) timeMillis;
//		}
//	}
}
