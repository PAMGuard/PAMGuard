package generalDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;


/**
 * Slightly modified connection object to pass around - contains not 
 * only the database connection, but also an sqlTypes object which can
 * get's used to make a few slightly non standard mods to some column 
 * and data formats to keep the various dbms's happy together. 
 * @author Douglas Gillespie
 *
 */
public class PamConnection {
	
	private DBSystem dbSystem;
	
	private Connection connection;
	
	private SQLTypes sqlTypes;
	
	private String databaseName;

	/**
	 * Constructor needs a connection object and an sqlTypes object. 
	 * @param connection Database connection
	 * @param sqlTypes SQLTypes object. 
	 */
	public PamConnection(DBSystem dbSystem, Connection connection, SQLTypes sqlTypes) {
		super();
		this.dbSystem = dbSystem;
		this.connection = connection;
		this.sqlTypes = sqlTypes;
	}

	public DBSystem getDbSystem() {
		return dbSystem;
	}

	public Connection getConnection() {
		return connection;
	}

	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}
	
	/**
	 * Get a list of database sql keywords. 
	 * @return list of keywords or an error message.
	 */
	public String getKeywords() {
		if (this.connection == null) {
			return null;
		}
		try {
			DatabaseMetaData dm = connection.getMetaData();
			return dm.getSQLKeywords();
		} catch (SQLException e) {
			return "Database SQL keywords unavailable";
		}
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

}
