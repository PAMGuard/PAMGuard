package pamguard;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

import generalDatabase.sqlite.SqliteSQLTypes;

public class PAMGuard_sqlite {
	
	public static void main(String[] args) {
		
		String dbName = "/Users/jdjm/Desktop/section2_cpod/hyskeir_pamguard.sqlite3";
		/*
		 * Don't use the driver manager, but open from the built in command in
		 * SQLiteConfig. This will then correctly set the dateformat of the database. 
		 */
		SQLiteConfig config = new SQLiteConfig();
		config.setSharedCache(true);
		config.enableRecursiveTriggers(true);
		config.enableLoadExtension(true);
		config.setDateClass(SqliteSQLTypes.dateClass.getValue());
		config.setDateStringFormat(SQLiteConfig.DEFAULT_DATE_STRING_FORMAT);

		Connection con = null;
		try {
			con = config.createConnection("jdbc:sqlite:" + dbName);
			con.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Connection: " + con);
		
	}

}
