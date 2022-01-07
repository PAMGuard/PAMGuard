package generalDatabase.postgresql;

import java.sql.Types;

import generalDatabase.SQLTypes;

public class PostgreSQLTypes extends SQLTypes {

	public PostgreSQLTypes() {
	}

	@Override
	public synchronized String formatColumnName(String columnName) {
		if (columnName == null) return null; 
		return columnName.toLowerCase();
	}
	
	@Override
	public String typeToString(int sqlType, int length, boolean counter) {
		if (sqlType == Types.INTEGER && counter) {
			return "SERIAL";
		} 
		else if (sqlType == Types.DOUBLE) {
			return "DOUBLE PRECISION";
		}
		if (sqlType == Types.BIT) {
			/* 
			 * most other DBMS we've tried didn't like BOOLEAN and used BIT, but this one
			 * doesn't like BIT and seems OK with BOOLEAN
			 */
			return "BOOLEAN";
		}

		return super.typeToString(sqlType, length, counter);
	}

	@Override
	public synchronized String formatTableName(String tableName) {
		if (tableName == null) return null;
		return tableName.toLowerCase();
	}
}
