package generalDatabase;

import generalDatabase.layoutFX.SystemDialogPaneFX;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;
import generalDatabase.pamCursor.ScrollablePamCursor;

import java.awt.Component;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;







import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

abstract public class ServerBasedSystem extends DBSystem implements PamSettings {
	
	private MySQLDialogPanel mySQLDialogPanel;
	
	protected MySQLParameters mySQLParameters;
	
	private Connection serverConnection;
	
	private PamConnection connection;
	
	private String openDatabase = "";

	private DBControl dbControl;
	
	/**
	 * JavaFX settings pane
	 */
	private MySQLPaneFX mySQLPaneFX;

	/**
	 * to be able to share passwords, etc. across instances
	 * when backing up
	 */
	private static MySQLParameters goodUserParameters;

	
	public ServerBasedSystem(DBControl dbControl, int settingsStore) {
		super();
		this.dbControl = dbControl;
		mySQLParameters = makeDefaultParams();
		PamSettingManager.getInstance().registerSettings(this, settingsStore);
		if (mySQLParameters == null) {
			mySQLParameters = makeDefaultParams();
		}
	}

	/**
	 * Make default parameters for this system
	 * @return default prameters (particularly username and port)
	 */
	protected MySQLParameters makeDefaultParams() {
		MySQLParameters params = new MySQLParameters();
		params.portNumber = getDefaultPort();
		params.userName = getDefaultUser();
		return params;
	}

	@Override
	public String browseDatabases(Component parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public boolean create() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}
	
	abstract public String getDriverClassName();
	
	abstract public String getDefaultUser();

	private String getUserName(MySQLParameters sqlParams) {
		if (goodUserParameters != null) {
			return goodUserParameters.userName;
		}
		else {
			return sqlParams.userName;
		}
	}
	private String getUserPassword(MySQLParameters sqlParams) {
		if (goodUserParameters != null) {
			return goodUserParameters.passWord;
		}
		else {
			return sqlParams.passWord;
		}
	}
	@Override
	public PamConnection getConnection(String databaseName) {
		Connection con = null;
		boolean isOpen = false;
		if (connection != null) {
			con = connection.getConnection();
			if (con != null) {
				try {
					isOpen = !con.isClosed();
				} catch (SQLException e) {
					isOpen = false;
				}
			}
		}
		if (isOpen == false || openDatabase.equalsIgnoreCase(databaseName) == false) {
			if (serverConnect(mySQLParameters) == false) return null;
			if (databaseName == null) return null;

			String databaseURL = buildDatabaseUrl(mySQLParameters.ipAddress, mySQLParameters.portNumber, databaseName);
			try {
				con = DriverManager.getConnection(
						databaseURL, 
						getUserName(mySQLParameters), 
						getUserPassword(mySQLParameters));
				con.setAutoCommit(dbControl.getDbParameters().getUseAutoCommit());
			}
			catch (SQLException e) {
				e.printStackTrace();
				con = null;
				goodUserParameters = null;
			}
			goodUserParameters = mySQLParameters;
			openDatabase = databaseName;
			PamCursorManager.setCursorType(PamCursorManager.SCROLLABLE);
			if (con == null) {
				return null;
			}
			connection = new PamConnection(this, con, getSqlTypes());
		}
		return connection;
	}

	@Override
	abstract public String getSystemName();

	@Override
	public boolean hasDriver() {

		try {
			Class.forName(getDriverClassName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	@Override
	public SystemDialogPanel getDialogPanel(Component parent) {
		if (mySQLDialogPanel == null) {
			mySQLDialogPanel = new MySQLDialogPanel(parent, this);
		}
		return mySQLDialogPanel;
	}
	
	abstract public int getDefaultPort();
	
	boolean serverConnect(MySQLParameters params) {
		if (goodUserParameters != null) {
			params = goodUserParameters;
		}
		boolean ok = serverConnect(params.ipAddress, params.portNumber, getUserName(params), getUserPassword(params));
		if (ok == false) {
			goodUserParameters = null;
		}
		return ok;
	}
	
	boolean serverConnect(String ipAddress, int portNumber, String userName, String userPassword) {

		if (serverConnection != null) return true;
		
		try {
			Class.forName(getDriverClassName());	//atempt to load the driver
		}
		catch( Exception e ) {
			e.printStackTrace( );
			JOptionPane.showMessageDialog(null,	"Cannot Load: " + getDriverClassName(), mySQLParameters.databaseName,	JOptionPane.ERROR_MESSAGE);
//			serverConnected = false;
			return false;
		}
		String databaseURL = buildDatabaseUrl(ipAddress, portNumber, getSchemaName());
		

//		String url = "jdbc:postgresql://localhost:5432/postgres";
		try {
			// attempt to connect to the server
			serverConnection = DriverManager.getConnection(
					databaseURL, userName, userPassword);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
//			JOptionPane.showMessageDialog(null,"Database server connection error",(name + " error 2"),JOptionPane.ERROR_MESSAGE);   
//			databaseParams.databaseSettings.setConnectionValid(false);
			System.out.println("Cannot connect to " + databaseURL);
			serverConnection = null;
			return false;
		}
//		serverConnected = true;
		return true;
	}
	
	/**
	 * Get the database schema name - the name of the databse on the server which manages
	 * the other databases
	 * @return
	 */
	public abstract String getSchemaName();

	@Override
	public boolean createNewDatabase(String name) {

		try {
			if (serverConnection == null) {
				return false;
			}
			Statement stmt = serverConnection.createStatement();
			//createResult = 
			stmt.executeUpdate("CREATE DATABASE " + name);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		mySQLParameters.databaseName = name;
		return true;
	}

	private ArrayList<String> availableDatabases;

	ArrayList<String> getAvailableDatabases(boolean doUpdate) {
		if (serverConnection == null) return null;
		if (doUpdate == false && availableDatabases != null) {
			return availableDatabases;
		}
		availableDatabases = new ArrayList<String>();
		try {
			Statement stmt = serverConnection.createStatement();
			ResultSet result = stmt.executeQuery(getAvailableDatabaseString());
			while(result.next()) { // process results one row at a time
				String val;
				String checkedDB = checkAvailableDatabaseResult(result);
				if (checkedDB != null) {
					availableDatabases.add(checkedDB);
				}
			} 
			stmt.close();
		}
		catch (SQLException e) {
			
		}
		
		return availableDatabases;
	}
	
	abstract public String getAvailableDatabaseString();
	
	abstract public String checkAvailableDatabaseResult(ResultSet result);
	
	public abstract String buildDatabaseUrl(String ipAddress, int portNumber, String databaseName);
	
	void serverDisconnect() {
		if (serverConnection == null) return;
		try {
			serverConnection.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		serverConnection = null;
	}

	public boolean isServerConnected() {
		return (serverConnection != null);
	}

	@Override
	public String getDatabaseName() {
		return mySQLParameters.databaseName;
	}

	public Serializable getSettingsReference() {
		return mySQLParameters;
	}

	public long getSettingsVersion() {
		return MySQLParameters.serialVersionUID;
	}

	public String getUnitName() {
		return dbControl.getUnitName();
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mySQLParameters = ((MySQLParameters) pamControlledUnitSettings.getSettings()).clone();
		return (mySQLParameters != null);
	}

	@Override
	public PamCursor createPamCursor(EmptyTableDefinition tableDefinition) {
		return new ScrollablePamCursor(tableDefinition);
	}

	
	@Override
	public SystemDialogPaneFX getDialogPaneFX() {
		if (mySQLPaneFX==null) mySQLPaneFX=new MySQLPaneFX(this); 
		return mySQLPaneFX;
	}

	@Override
	public String browseDatabasesFX(int type) {
		// TODO Auto-generated method stub
		return null;
	};

	public boolean checkDatabaseExists(String dbName) {
		if (dbName == null) {
			dbName = mySQLParameters.databaseName;
		}
		boolean servOK = serverConnect(mySQLParameters);
		if (servOK == false) {
			return false;
		}
		ArrayList<String> dbs = getAvailableDatabases(true);
		for (String db:dbs) {
			if (db.equals(dbName)) {
				return true;
			}
		}
		return false;
	}
	
}
