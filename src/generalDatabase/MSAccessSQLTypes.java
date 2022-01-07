package generalDatabase;

import java.sql.Types;


public class MSAccessSQLTypes extends SQLTypes {

	@Override
	public String typeToString(int sqlType, int length, boolean counter) {
		String baseString;
		if (sqlType == Types.INTEGER && counter) {
		/*
		May need to remove the NOT NULL part of this when importing data from another
		table or some such (GW). 
		 */
			baseString = "COUNTER NOT NULL";//;
		}
		else if (sqlType == Types.BOOLEAN) {
			baseString = "BIT";
		}
		else {
			baseString = super.typeToString(sqlType, length, counter);
		}

		return baseString;
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
}
