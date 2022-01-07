package generalDatabase.postgresql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.postgresql.jdbc.PgConnection;

import PamController.PamController;
import PamView.dialog.warn.WarnOnce;

public class PostgreSQLTest {


	private String usr = "postgres";
	private String pwd = "doug";
	private String dbName = "example3";
	String driverClass = "org.postgresql.Driver";


	public static void main(String[] args) {
		PostgreSQLTest tst = new  PostgreSQLTest();
		tst.connect();

	}

	private void connect() {

		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return ;
		}

		//		try {
		String url = "jdbc:postgresql://localhost:5432/postgres";
		//			System.out.println("Open connection " + url);
		PgConnection conn = null;
		Statement stmt;
		try {
			conn = (PgConnection) DriverManager.getConnection(url, usr, pwd);
			System.out.println("Connection success " + conn.getDBVersionNumber());
			//						stmt = conn.createStatement();
			//						//createResult = 
			//						int ans = stmt.executeUpdate("CREATE DATABASE " + dbName);
			//						System.out.println("Return from createdatabae = " + ans);
			//						stmt.close();
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false;");
			while(result.next()) { // process results one row at a time
				String val;
				val = result.getString(1);
				System.out.println(val);
				//Ignore the "information_schema" database, as this holds metadata on all the server's databases
				//				if((!val.equalsIgnoreCase(schemaName) & (!val.equalsIgnoreCase("mysql")))){  	
				//					availableDatabases.add(val);
				//				}	
			} 
			stmt.close();

		} catch (SQLException e) {
			//		WarnOnce.showWarning(PamController.getMainFrame(), "Server Error", "Unable to Connect to ALFA sperm whale database", WarnOnce.WARNING_MESSAGE);
			e.printStackTrace();
		}

		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public PostgreSQLTest() {
		// TODO Auto-generated constructor stub
	}
}
