package generalDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.swing.JOptionPane;

import PamUtils.PamCalendar;


public class DBSpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int ntrials = 1000;
		DBSpeedTest speedTest = new DBSpeedTest();
		long startTime = System.currentTimeMillis();
		speedTest.testSpeed2(ntrials, false);
		long taken = System.currentTimeMillis() - startTime;
		double perWrite = (double) taken / (double) ntrials;
		System.out.println(String.format("%d ms taken for %d writes = %3.2f ms per write", (int) taken, ntrials, perWrite));
	}

	/**
	 * Test database speed with nWrites, printing out how long things
	 * are taking every 1000 writes. 
	 * <p>
	 * Writes to a simple table containing the following columns: 
	 * <p>SystemTime - Data/Time current time from the PC clock
	 * <p>LastWriteTime - how many microseconds it took to write the previous record
	 * <p>PreviousIndex - Id from the previous entry (to show that I'm able to read the Id back after writing)
	 * <p>RunCount - how many writes this time the program executed. 
	 * @param nWrites number of recordsd to write. 
	 * @param useAccess true - use MS Access, false use MySQL. 
	 */
	private void testSpeed(int nWrites, boolean useAccess) {
		Connection con;
		if (useAccess) {
			con = openAccessDatabase();
		}
		else {
			con = openMySQLDatabase();
		}
		if (con == null) {
			System.out.println("No database connection available");
			return;
		}
		String selectString = "SELECT ID, RunCount FROM SpeedTable";

		try {

			PreparedStatement selectStatement = con.prepareStatement(selectString, 
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet idResult = selectStatement.executeQuery();

			int id;
			for (int i = 0; i < nWrites; i++) {
				idResult.moveToInsertRow();
				//				idResult.updateObject(1, null); // this line makes no difference whatsoever !
				idResult.updateInt(2, i);
				idResult.insertRow(); // throw java.sql.SQLException: [Microsoft][ODBC Microsoft Access Driver]Error in row
				//				id = idResult.getInt(1);
			}

			if (selectStatement != null) {
				selectStatement.close();
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void testSpeed3(int nWrites, boolean useAccess) {
		Connection con;
		if (useAccess) {
			con = openAccessDatabase();
		}
		else {
			con = openMySQLDatabase();
		}
		if (con == null) {
			System.out.println("No database connection available");
			return;
		}

		String insertString = "INSERT INTO SpeedTable (RunCount) VALUES (?)";
		//		String idString = "SELECT ID FROM SpeedTable ORDER BY ID DESC";
		//		
		try {
			ResultSet idResult = null;
			PreparedStatement preparedStatement = null;
			PreparedStatement idStatement = null;
			Statement statement = null;
			preparedStatement = con.prepareStatement(insertString,
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			//			preparedStatement.
			//			idStatement = con.prepareStatement(idString, 
			//					ResultSet.TYPE_FORWARD_ONLY,
			//					ResultSet.CONCUR_READ_ONLY);
			//			statement = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);

			//			prepareStatement(insertString,
			//			ResultSet.TYPE_FORWARD_ONLY,
			//			ResultSet.CONCUR_READ_ONLY);

			for (int i = 0; i < nWrites; i++) {
				//				insertString = String.format("INSERT INTO SpeedTable (RunCount) VALUES (%d)", i);
				//				statement.executeUpdate(insertString, Statement.RETURN_GENERATED_KEYS);

				// write the data into the database
				preparedStatement.setInt(1, i);
				preparedStatement.executeUpdate();
				// re-run the query to get teh index back from the database. 
				//				idResult = idStatement.executeQuery();
				//				idResult.next();
				//				int lastIndex = idResult.getInt(1);
				//				idResult.close();
			}

			System.out.println("Completed writing of " + nWrites + " records");

			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (idStatement != null) {
				idStatement.close();
			}
			if (statement != null) {
				statement.close();
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private void testSpeed2(int nWrites, boolean useAccess) {
		Connection con;
		if (useAccess) {
			con = openAccessDatabase();
		}
		else {
			con = openMySQLDatabase();
		}
		if (con == null) {
			System.out.println("No database connection available");
			return;
		}

		String insertString = "INSERT INTO SpeedTable (`UID`, `UTC`, `LastWriteTime`, `PreviousIndex`, `RunCount`) VALUES (?, ?, ?, ?, ?)";
		String idString = "SELECT ID FROM SpeedTable ORDER BY ID DESC";
		//		String selectString = "SELECT SystemTime, LastWriteTime, PreviousIndex, RunCount FROM SpeedTable WHERE Id = 1";
		String selectString = "SELECT RunCount FROM SpeedTable WHERE Id = 0";
		String createString = "CREATE TABLE SpeedTable (Id INTEGER NOT NULL AUTO_INCREMENT, UID BIGINT, `UTC` TIMESTAMP, LastWriteTime INTEGER, PreviousIndex INTEGER, RunCount INTEGER, PRIMARY KEY (Id))";

		int tryType = 0;
		boolean recreate = false;

		if (recreate) {
			try {
				PreparedStatement dropStmt = con.prepareStatement("drop table SpeedTable");
				dropStmt.execute();
			} catch (SQLException e) {
				//			e.printStackTrace();
				System.out.println("Error in DBSpeedTest testSpeed2:" + e.getMessage());
			}
		}
		try {
			if (recreate) {
				System.out.println(createString);
				PreparedStatement createStmt = con.prepareStatement(createString);
				createStmt.execute();
			}


			long lastTime = System.nanoTime();
			long now, nowNanos;
			long timeTaken;
			int lastIndex = 0;
			int iCol;
			ResultSet idResult = null;
			PreparedStatement preparedStatement = null, idStatement = null, selectStatement = null;
			if (tryType == 0) {
				System.out.println(insertString);
				preparedStatement = con.prepareStatement(insertString,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				idStatement = con.prepareStatement(idString, 
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
			}
			else {
				selectStatement = con.prepareStatement(selectString, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				idResult = selectStatement.executeQuery();
			}

			long uidVal = 1L<<33;

			for (int i = 0; i < nWrites; i++) {
				now = System.currentTimeMillis();
				nowNanos = System.nanoTime();
				timeTaken = (nowNanos-lastTime)/1000;
				lastTime = nowNanos;
				iCol = 1;

				if (tryType == 0) {
					//				preparedStatement.setObject(iCol++, 1, Types.INTEGER);
					preparedStatement.setLong(iCol++, uidVal++);
					preparedStatement.setObject(iCol++, new java.sql.Timestamp(now), Types.TIMESTAMP);
					preparedStatement.setInt(iCol++, (int) timeTaken);
					preparedStatement.setInt(iCol++, lastIndex);
					preparedStatement.setInt(iCol++, i);
					preparedStatement.execute();

					idResult = idStatement.executeQuery();
					idResult.next();
					lastIndex = idResult.getInt(1);
					idResult.close();
				}
				else {
					idResult.moveToInsertRow();
					//					idResult.updateObject(iCol++, new java.sql.Timestamp(now), Types.TIMESTAMP);
					idResult.updateInt(iCol++, (int) timeTaken);
					//					idResult.updateInt(iCol++, lastIndex);
					//					idResult.updateInt(iCol++, i);
					idResult.updateRow();
				}
				//				keys = preparedStatement.getResultSet();
				//				 keys = preparedStatement.getGeneratedKeys();
				if ((i+1)%1000 == 0) {
					System.out.println(String.format("Database write %d took %d microseconds", i+1, timeTaken));
				}
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (idStatement != null) {
				idStatement.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}

			if (con.getAutoCommit() == false) {
				con.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private Connection openAccessDatabase() {
		String dbName = "E:\\DatabaseTest\\TestDatabase.accdb";
		String driverClass = "sun.jdbc.odbc.JdbcOdbcDriver";		
		try {
			Class.forName(driverClass);
			// only works for an existing Access database. 
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=";
			database += dbName.trim() + ";DriverID=22;READONLY=false}"; // add on to the end 
			// now we can get the connection from the DriverManager
			Connection connection = DriverManager.getConnection( database ,"",""); 
			return connection;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Connection openMySQLDatabase() {
		String driverClass = "com.mysql.cj.jdbc.Driver";	//Driver string
		try {
			Class.forName(driverClass).newInstance();	//attempt to load the driver
		}
		catch( Exception e ) {
			e.printStackTrace( );
			return null;
		}
		String dbURL = buildDatabaseUrl("localhost", 3306, "databasespeed");
		Connection connection;
		try {
			connection = DriverManager.getConnection(
					dbURL, 
					"root", 
					"pamguard");
			connection.setAutoCommit(false);
		}
		catch (SQLException e) {
			e.printStackTrace();
			connection = null;
		}
		return connection;
	}	
	private String buildDatabaseUrl(String ipAddress, int portNumber, String databaseName){	
		return "jdbc:mysql://" + ipAddress + ":" + portNumber + "/" + databaseName +
				"?jdbcCompliantTruncation=true";
	}

	public DBSpeedTest() {
		super();
	}



}
