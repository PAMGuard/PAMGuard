package generalDatabase.postgresql;

import java.sql.ResultSet;
import java.sql.SQLException;

import generalDatabase.DBControl;
import generalDatabase.SQLTypes;
import generalDatabase.ServerBasedSystem;

public class PostgreSQLSystem extends ServerBasedSystem {

	private final String driverClass = "org.postgresql.Driver";	//Driver string
	
	private final String availableDatabaseString = "SELECT datname FROM pg_database WHERE datistemplate = false;";

	private final String schemaName = "postgres";

	private SQLTypes sqlTypes = new PostgreSQLTypes();

	public static final String SYSTEMNAME = "PostgreSQL";
	
	/**
	 * System for PostgreSQL databases. Partially works, but some fundamental problems, e.g. with Types.BIT which just 
	 * doesn't work. Will need to do a fair bit of work to replace with a more standard Boolean before this can be used. 
	 * @param dbControl
	 * @param settingsStore
	 */
	public PostgreSQLSystem(DBControl dbControl, int settingsStore) {
		super(dbControl, settingsStore);
	}

	@Override
	public String getDriverClassName() {
		return driverClass;
	}

	@Override
	public String getDefaultUser() {
		return "postgres";
	}

	@Override
	public String getAvailableDatabaseString() {
		return availableDatabaseString;
	}

	@Override
	public String checkAvailableDatabaseResult(ResultSet result) {
		String val = null;
		try {
			val = result.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		//Ignore the "information_schema" database, as this holds metadata on all the server's databases
		if((!val.equalsIgnoreCase(getSchemaName()) & (!val.equalsIgnoreCase("postgresql")))){  	
			return val;
		}	
		return null;
	}
//	String url = "jdbc:postgresql://localhost:5432/postgres";
	public String buildDatabaseUrl(String ipAddress, int portNumber, String databaseName){	
		return "jdbc:postgresql://" + ipAddress + ":" + portNumber + "/" + databaseName;
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}

	public String getUnitType() {
		return "PostgreSQL System";
	}

	@Override
	public String getSystemName() {
		return SYSTEMNAME;
	}

	@Override
	public int getDefaultPort() {
		return 5432;
	}

	@Override
	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}


}
