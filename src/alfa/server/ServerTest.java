package alfa.server;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.jdbc.PgConnection;

public class ServerTest {

	public ServerTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		ServerTest st = new ServerTest();
		st.run();

	}

	private void run() {
		/*
		 * https://jdbc.postgresql.org/documentation/head/connect.html
		 */
		//		String ip_address = "157.230.166.95";
		String db_user = "alfa_db_user";
		String db_pw = "spermwhale";
		String dbName =  "alfa_sw_db";
		//		String conStr = "http://157.230.166.95:8000/api/raw_data_string/";

		String driverClass = "org.postgresql.Driver";
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			String url = "jdbc:postgresql://157.230.166.95:5432/"+dbName;
			System.out.println("Open connection " + url);
			PgConnection conn = (PgConnection) DriverManager.getConnection(url, db_user, db_pw);
			System.out.println("connection open");
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, null, null);

			//			if (tables.next()){
			//				haveTable = true;
			//			}
			//			if (databaseControll.databaseSystem.getSystemName().equals(OOoDBSystem.SYSTEMNAME)){
			String dataTable = "data_manager_rawposteddata";
			ResultSet oodbTables = dbm.getTables(null, null, dataTable, null);

			while (oodbTables.next()){
				String tableName =  oodbTables.getString(3).trim();
				//				if (tableName.startsWith("pg_")) {
				//					continue;
				//				}
				//				System.out.println("Found table named " + oodbTables.getString(3).trim());

				//					if (oodbTables.getString(3).trim().equalsIgnoreCase(tableDef.getTableName())){
				//						//						System.out.println("Table Found: "+oodbTables.getString(3));
				//						tableDef.setTableName(oodbTables.getString(3).trim().toUpperCase());
				//						haveTable = true;
				//					}

				// try the table data_manager_rawposteddata_id_seq to see what's in it ...
				ResultSet columns = dbm.getColumns(null, null, tableName, null);
				int ncol = 0;
				boolean interesting = false;
				while (columns.next()) {

					// now check the format
					String colName = columns.getString(4);
					int colType = columns.getInt(5);
//					if (colName.toLowerCase().contains("iridium_latitude")) {
						System.out.printf("Table %s has column %s type %d\n",tableName, colName, colType);
						interesting = true;
//					}
					//					//			if (colType == tableItem.getSqlType()) return true;
					//					//			//String strColType = columns.getString(6);
					//					if (columnName.equalsIgnoreCase(colName)) {
					//						haveColumn = true;
					//					}
					ncol++;
				}
				if (interesting)
					System.out.printf("found %d columns in table %s\n", ncol, tableName);

				//				System.out.println("Table Not Found: "+tableDef.getTableName().toUpperCase());
				//			}
			}
			tables.close();
			
//			ResultSet dResult = conn.execSQLQuery("SELECT id, imei, date_received, decoded_message, datetime_sent, raw_posted_data, hist_string FROM data_manager_rawposteddata WHERE decoded_message LIKE '%$PGSTA%' ORDER BY id");
			ResultSet dResult = conn.execSQLQuery("SELECT id, imei, date_received, decoded_message, datetime_sent, raw_posted_data, "
					+ "hist_string FROM data_manager_rawposteddata ORDER BY raw_posted_data");
				while (dResult.next()) {
				int id = dResult.getInt(1);
				long imei = dResult.getLong(2);
				String date = dResult.getString(3);
				Object dateObject = dResult.getObject(5);
				String strData = dResult.getString(4);
				String rawData = dResult.getString(6);
				String histString = dResult.getString(7);
				if (dateObject == null) dateObject = "no Date";
				System.out.println(rawData);
				System.out.printf("%d %d %s \"%s\" %s\n", id, imei, date, dateObject.toString(), strData);
			}
			dResult.close();
			System.out.println(conn);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
