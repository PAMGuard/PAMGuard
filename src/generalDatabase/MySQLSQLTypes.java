package generalDatabase;

import java.sql.Types;


public class MySQLSQLTypes extends SQLTypes {

	@Override
	public String typeToString(int sqlType, int length, boolean counter) {
		if (sqlType == Types.INTEGER && counter) {
			return "INTEGER NOT NULL AUTO_INCREMENT";
		}
		return super.typeToString(sqlType, length, counter);
	}


	@Override
	public synchronized String formatColumnName(String columnName) {
		/**
		 * Wow ! Read at http://stackoverflow.com/questions/4716201/alternative-to-column-name-order-in-mysql
		 * that you can put "`" round  a keyword, tried with "'" from the keyboard and it failed, but of course
		 * the two characters are totally different !
		 * This is ascii character 96, whereas the normal ' is ascii 39. 
		 */
//		int a = '\'';
		return "`" + super.formatColumnName(columnName) + "`";
//		return columnName;
	}

}
