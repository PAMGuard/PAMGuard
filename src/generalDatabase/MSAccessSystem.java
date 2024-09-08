package generalDatabase;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
//import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

//import sun.jdbc.odbc.JdbcOdbcDriver;








//import sun.jdbc.odbc.JdbcOdbcConnection;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import generalDatabase.layoutFX.SystemDialogPaneFX;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;

public class MSAccessSystem extends BaseAccessSystem implements PamSettings {

	private DBControl dbControl;
	
	private MSAccessDialogPanel dialogPanel;
	
	private SQLTypes sqlTypes = new MSAccessSQLTypes();
	
	public static final String SYSTEMNAME = "Microsoft Access Database";
	
	private static final String driverClass = "sun.jdbc.odbc.JdbcOdbcDriver";	
	
	private static ArrayList<File> recentDatabases = new ArrayList<File>();
	
	private PamConnection connection;

	/**
	 * @return the recentDatabases
	 */
	@Override
	public ArrayList<File> getRecentDatabases() {
		return recentDatabases;
	}

	public MSAccessSystem(DBControl dbControl, int settingsStore) {
		super();
		this.dbControl = dbControl;
		PamSettingManager.getInstance().registerSettings(this, settingsStore);
		if (recentDatabases == null) {
			recentDatabases = new ArrayList<File>();
		}
	}

	@Override
	public String browseDatabases(Component parent) {
		PamFileFilter fileFilter = new PamFileFilter("Microsoft Access Database", ".mdb");
		fileFilter.addFileType(".accdb");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		if (getDatabaseName() != null) {
			fileChooser.setSelectedFile(new File(getDatabaseName()));
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(parent);
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
			return currFile.getAbsolutePath();
		}
		return null;
	}

	@Override
	public boolean canCreate() {
		return false;
	}

	@Override
	public boolean create() {
		return false;
	}

	@Override
	public PamCursor createPamCursor(EmptyTableDefinition tableDefinition) {
		return new NonScrollablePamCursor(tableDefinition);
	}

	@Override
	public boolean exists() {

		File file = new File(getDatabaseName());
		if (file == null) return false;
		return file.exists();
		
	}

	@Override
	public PamConnection getConnection(String dbName) {
			if (dbName == null) return null;
			
			connection = null;
			Exception mdbException = null;
			Exception accdbException = null;
			if (dbName.endsWith(".mdb")) {
				try {
					connection = getMDBConnection(this, dbName);
				}
				catch (Exception e) {
					mdbException = e;
				}
			}
			if (connection == null) {
				try {
					connection = getACCDBConnection(this, dbName);
				}
				catch (Exception e) {
					accdbException = e;
				}
			}
			if (connection == null) {
				System.out.println("Unable to open database " + dbName);
//				if (mdbException != null) {
//					mdbException.printStackTrace();
//				}
//				if (accdbException != null) {
//					accdbException.printStackTrace();
//				}
				return null;
			}
//			JdbcOdbcConnection jCon = (JdbcOdbcConnection) connection;
//			System.out.println("JDBC connection: " + jCon.toString());
			try {
				connection.getConnection().setAutoCommit(dbControl.dbParameters.getUseAutoCommit());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return connection;
	}
	
	public static PamConnection getMDBConnection(DBSystem dbSystem, String dbName) throws Exception {
		if (dbName == null) return null;
		try {
			Class.forName(driverClass);
			// only works for an existing Access database. 
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
			database += dbName.trim() + ";DriverID=22;READONLY=false}"; // add on to the end 
			// now we can get the connection from the DriverManager
			Connection connection = DriverManager.getConnection( database ,"",""); 
			
			return new PamConnection(dbSystem, connection, new MSAccessSQLTypes());
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	public static PamConnection getACCDBConnection(DBSystem dbSystem, String dbName) throws Exception {
		if (dbName == null) return null;
		try {
			Class.forName(driverClass);
			// only works for an existing Access database. 
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=";
			database += dbName.trim() + ";DriverID=22;READONLY=false}"; // add on to the end 
			// now we can get the connection from the DriverManager
			Connection connection = DriverManager.getConnection( database ,"",""); 
			return new PamConnection(dbSystem, connection, new MSAccessSQLTypes());
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public void closeConnection(PamConnection connection) {
		if (connection == null || connection.getConnection() == null) {
			return;
		}
		try {
			connection.getConnection().commit();
			connection.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		connection =null;
		this.connection = null;
	}


	@Override
		public boolean hasDriver() {
			try {
				// return false if it's a 64 bit JVM
				Class.forName(driverClass);
			} catch (ClassNotFoundException e) {
				return false;
			}
//			String arch = System.getProperty("sun.arch.data.model");
//			return (arch.contains("32"));
			return true;
		}

	private String[] findAccessDrivers() {
	//		Enumeration<Driver> d = DriverManager.getDrivers();
	//		JdbcOdbcDriver jod = null;
	//		try {
	//			jod = (JdbcOdbcDriver) DriverManager.getDriver("jdbc:odbc:Driver");
	//		} catch (SQLException e1) {
	//			jod = null;
	//			e1.printStackTrace();
	//		}
	//		if (jod != null) {
	////			jod.
	//		}
	//		try {
	//			Driver od = DriverManager.getDriver("jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)}");
	//			System.out.println("Found standard access mdb driver");
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		try {
	//			Driver od2 = DriverManager.getDriver("jdbc:odbc:Driver");
	//			System.out.println("Found access mdb / accdb driver");
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		Driver ad;
	//		while (d.hasMoreElements()) {
	//			ad = d.nextElement();
	//			System.out.println("Database Driver name: " + ad.toString());
	//		}
			return null;
		}

	@Override
	public String getDatabaseName() {
		if (recentDatabases == null) return null;
		if (recentDatabases.size() < 1) return null;
		return recentDatabases.get(0).getAbsolutePath();
	}
	@Override
	public String getShortDatabaseName() {
		if (recentDatabases == null) return null;
		if (recentDatabases.size() < 1) return null;
		return recentDatabases.get(0).getName();
	}

	@Override
	public SystemDialogPanel getDialogPanel(Component parent) {
		if (dialogPanel == null) {
			dialogPanel = new MSAccessDialogPanel(parent, this);
		}
		return dialogPanel;
	}

	@Override
	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}

	
	
	
	//
	//
	//    PAM SETTINGS METHODS
	//
	//
	
	@Override
	public String getSystemName() {
		return SYSTEMNAME;
	}

	@Override
	public String getUnitName() {
		return dbControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "MS Access Database System";
	}

	//
	//
	//    PAM SETTINGS METHODS
	//
	//
	
	@Override
	public Serializable getSettingsReference() {
		return recentDatabases;
	}

	@Override
	public long getSettingsVersion() {
		return 0;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		recentDatabases = (ArrayList<File>) (pamControlledUnitSettings.getSettings());
		return true;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.DBSystem#getKeywords()
	 */
	@Override
	public String getKeywords() {
		// no restrictions at all in MS Access since names
		// are wrapped in "" 
		return null;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.DBSystem#canOpenDatabase()
	 */
	@Override
	public boolean canOpenDatabase() {
		return true;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.DBSystem#openCurrentDatabase()
	 */
	@Override
	public boolean openCurrentDatabase() {
		String databaseName = getDatabaseName();
		if (databaseName == null) return false;
		databaseName = databaseName.trim();
		 try {
		     if (Desktop.isDesktopSupported()) {
		       Desktop.getDesktop().open(new File(databaseName));
		     }
		   } catch (IOException ioe) {
//		     ioe.printStackTrace();
//			   System.out.println("Unable to open ");
		  }

		return true;
	}

	@Override
	public SystemDialogPaneFX getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String browseDatabasesFX(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean createNewDatabase(String forcedName) {
		return false;
	}
}
