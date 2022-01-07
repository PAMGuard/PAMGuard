package generalDatabase;

import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.sql.Statement;

public class DBTest {

	EmptyTableDefinition tableDef;
	
	PamCursor pamCursor;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBTest m = new DBTest();
		m.test();

	}

	void test() {
		System.out.println("Hello Database test");
		String dbName  = "c:\\DatabaseTest\\DatabaseTest.mdb";
		String tableName = "TestTable";
		Connection con = null;
		DatabaseMetaData metaData = null;
		SQLTypes sqlTypes = new MSAccessSQLTypes();
		
		tableDef = new EmptyTableDefinition("TestTable");
		tableDef.addTableItem(new PamTableItem("TestInt", Types.INTEGER));
		pamCursor = new NonScrollablePamCursor(tableDef);
//		pamCursor.setTableDefinition(tableDef);
		System.out.println("Select query: " + pamCursor.getSelectString(sqlTypes, true, true, true));
		System.out.println("Update query: " + pamCursor.getUpdateString(sqlTypes));
		System.out.println("Insert query: " + pamCursor.getInsertString(sqlTypes));

		try {
			Class jo = Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
//			Class jo = Class.forName("connect.microsoft.MicrosoftDriver");

			System.out.println("Driver class " + jo.toString());
			// only works for an existing Access database. 
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
			database += dbName.trim() + ";DriverID=22;READONLY=false;EXCLUSIVE=false}"; // add on to the end 
			// now we can get the connection from the DriverManager
			con = DriverManager.getConnection( database ,"",""); 
			metaData = con.getMetaData();
		}
		catch (Exception e) {
			System.out.println("Error: " + e);
		}

		System.out.println(dbName + " is open");
		
		
		
		try {
			System.out.println("supportsPositionedUpdate " + metaData.supportsPositionedUpdate());
			System.out.println("supportsResultSetConcurrency " + metaData.supportsResultSetConcurrency(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
//			System.out.println("supportsPositionedUpdate " + metaData.));
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		printTableData(con);

		PamCursor pamCursor = new NonScrollablePamCursor(tableDef);
//		pamCursor.setTableDefinition(tableDef);
		PamConnection pamCon = new PamConnection(null, con, new SQLTypes());
		pamCursor.openScrollableCursor(pamCon, true, true, "ORDER BY Id");
		
		int iD, testInt=0;
		while (pamCursor.next()) {
			iD = pamCursor.getInt(1);
			testInt = pamCursor.getInt(2);
			System.out.println(String.format("Cursor data: Id %d, Test Integer %d", iD, testInt));
		}
		
		pamCursor.beforeFirst();
		int iRow = 0;
		while (pamCursor.next()) {
			iD = pamCursor.getInt(1);
			testInt = pamCursor.getInt(2);
			if (iRow++ < 3) {
				pamCursor.deleteRow();
			}
			try {
				pamCursor.updateInt(2, testInt+2);
				pamCursor.updateRow();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			System.out.println(String.format("Cursor data: Id %d, Test Integer %d", iD, testInt));
		}
		pamCursor.moveToInsertRow();
		try {
			pamCursor.updateInt(2, testInt+1);
			pamCursor.insertRow(true);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		pamCursor.updateDatabase();

//		if (addToAll(con, 3)) {
//
//			printTableData(con);
//
//		}


		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Use a scrollable cursor to add i to all records in the table
	 * using a scrollable cursor. 
	 * @param con
	 * @param i
	 */
	private boolean addToAll(Connection con, int i) {
		String sqlStr = "SELECT TestInt from TestTable ORDER BY Id";
		System.out.println("Update result set with simple query: " + sqlStr);
		Statement stmt = null;
		ResultSet resultSet;
		int iD, testInt;
		int iRow = 0;
		try {
			/*
			 * In C I use the following flags on teh sursor !
  RetCode = SQLSetStmtAttr(hSQLStmt, SQL_ATTR_CURSOR_TYPE, (void*)SQL_CURSOR_KEYSET_DRIVEN, 0);
  RetCode = SQLSetStmtAttr(hSQLStmt, SQL_ATTR_ROW_BIND_TYPE, SQL_BIND_BY_COLUMN, 0);
  RetCode = SQLSetStmtAttr(hSQLStmt, SQL_ATTR_CONCURRENCY, (void*) SQL_CONCUR_LOCK, 0);
  
  Tehn it uses       RetCode = SQLSetPos(hSQLStmt, 1, SQL_UPDATE, SQL_LOCK_NO_CHANGE);
			 */
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
//			stmt.setFetchDirection(ResultSet.FETCH_UNKNOWN);


			stmt.executeQuery(sqlStr);
			resultSet = stmt.getResultSet();
			
//			resultSet.afterLast();
//			resultSet.moveToInsertRow();
//			resultSet.updateInt(1, 999);
////			resultSet.insertRow();			
//			resultSet.updateRow();
			
//			resultSet.beforeFirst();
			while (resultSet.next()) {
				testInt = resultSet.getInt(1);
				System.out.println(String.format("Update row %d, int %d", ++iRow, testInt));
				resultSet.updateInt(1, testInt+1);
				resultSet.updateRow();
//				resultSet.HOLD_CURSORS_OVER_COMMIT
			}

			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Dump all data in the TestTable with a simple quiery
	 * @param con
	 */
	private void printTableData(Connection con) {
		String sqlStr = "SELECT Id, TestInt from TestTable ORDER BY Id";
		System.out.println("Print result set with simple query: " + sqlStr);
		Statement stmt;
		ResultSet resultSet;
		int iD, testInt;
		int iRow = 0;
		try {
			stmt = con.createStatement();
			stmt.execute(sqlStr);
			resultSet = stmt.getResultSet();
			while (resultSet.next()) {
				iD = resultSet.getInt(1);
				testInt = resultSet.getInt(2);
				System.out.println(String.format("Row %d, Id = %d, testInt = %d", 
						++iRow, iD, testInt));
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
