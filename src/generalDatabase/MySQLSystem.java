package generalDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLSystem extends ServerBasedSystem {

	private final String driverClass = "com.mysql.cj.jdbc.Driver";	//Driver string
	
	private final String availableDatabaseString = "SELECT SCHEMA_NAME AS `Database` FROM INFORMATION_SCHEMA.SCHEMATA";

	private final String schemaName = "information_schema";

	public static final String SYSTEMNAME = "MySQL Databases";

	private SQLTypes sqlTypes = new MySQLSQLTypes();
	
	public MySQLSystem(DBControl dbControl, int settingsStore) {
		super(dbControl, settingsStore);
	}

	@Override
	public String getDriverClassName() {
		return driverClass;
	}

	@Override
	public String getDefaultUser() {
		return "root";
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
		if((!val.equalsIgnoreCase(schemaName) & (!val.equalsIgnoreCase("mysql")))){  	
			return val;
		}	
		return null;
	}

	@Override
	public String buildDatabaseUrl(String ipAddress, int portNumber, String databaseName){	
		return "jdbc:mysql://" + ipAddress + ":" + portNumber + "/" + databaseName +
		"?jdbcCompliantTruncation=true";
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}

	@Override
	public String getUnitType() {
		return "MySQL Database System";
	}

	@Override
	public String getSystemName() {
		return SYSTEMNAME;
	}
	
	@Override
	public int getDefaultPort() {
		return 3306;
	}

	@Override
	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}


}
