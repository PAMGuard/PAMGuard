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
		 * 
		 * Seems that for SQLite we can get away with a string, for MySQL we need a TimeStamp object still 
		 */
//		return timeMillis;
		if (timeMillis == null) {
			return null;
		}
		return PamCalendar.formatDBDateTime(timeMillis, true);
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
