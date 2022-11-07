package generalDatabase.sqlite;

import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;
import javafx.stage.FileChooser;
import pamguard.GlobalArguments;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.sqlite.SQLiteConfig;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamFolders;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.warn.WarnOnce;
import generalDatabase.DBControl;
import generalDatabase.DBSystem;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;
import generalDatabase.SystemDialogPanel;
import generalDatabase.layoutFX.SystemDialogPaneFX;

/**
 * PAMGuard database system to make use of Sqlite databases: https://www.sqlite.org/
 * Specifically, this database system makes use of the xerial sqlite-jdbc library,
 * https://github.com/xerial/sqlite-jdbc.
 * 
 * NB: The default sqlite-jdbc from xerial does not support JDBC escape syntax, which
 * is used by PAMGuard to interpret timestamps in mixed and viewer modes. Until this 
 * is addressed, sqlite database will fail in mixed and viewer mode. 
 *   
 * @author brian_mil
 */
public class SqliteSystem extends DBSystem implements PamSettings {
	/* TODO: Fix issues with sqlite-jdbc timestamps and 
	 * JDBC escape syntax so that sqlite database can be 
	 * used in mixed and viewer mode 
	 */
	private DBControl dbControl;

	private SqliteDialogPanel dialogPanel;

	private SQLTypes sqlTypes = new SqliteSQLTypes();

	public static final String SYSTEMNAME = "Sqlite Database";

	private static final String driverClass = "org.sqlite.JDBC";

	//	private static final boolean USEAUTOCOMMIT = true;	

	private SQLiteParameters sqliteParameters = new SQLiteParameters();

	/**
	 * The JavaFX settings pane.
	 */
	private SqlitePaneFX sqlPane;

	/**
	 * @return the recentDatabases
	 */
	public ArrayList<File> getRecentDatabases() {
		return sqliteParameters.getRecentDatabases();
	}

	public SqliteSystem(DBControl dbControl, int settingsStore) {
		super();
		this.dbControl = dbControl;
		PamSettingManager.getInstance().registerSettings(this, settingsStore);
		if (sqliteParameters.getRecentDatabases() == null) {
			sqliteParameters.setRecentDatabases(new ArrayList<File>());
		}
		checkCommandLineOption();
	}

	/**
	 * Check to see if the database name was included as a command line option, 
	 * in which case, put it at the top of the list. this is better than just using 
	 * it,since it then gets stored within it's own settings. 
	 */
	private void checkCommandLineOption() {
		// TODO Auto-generated method stub		
		/*
		 * If a database name was passed as a global argument, then use the passed name instead of 
		 * the name in the list of recent databases. 
		 */
		String commandName = GlobalArguments.getParam(DBControl.GlobalDatabaseNameArg);
		if (commandName == null) {
			return;
		}
		setDatabaseName(commandName);
	}

	/**
	 * Set the database name, check it exists, check it's end
	 * and add to top of list of databases. 
	 * @param databaseName
	 */
	public void setDatabaseName(String databaseName) {
		// see if this file exists in the list and if it does, remove it
		for (int i = 0; i < getRecentDatabases().size(); i++) {
			if (getRecentDatabases().get(i).toString().equalsIgnoreCase(databaseName)) {
				getRecentDatabases().remove(i);
			}
		}
		// then insert the file at the top of the list.
		File newFile = new File(databaseName);
		// if the file doesn't exit, consider creating it.
		if (newFile.exists() == false) {
			boolean ask = GlobalArguments.getParam(DBControl.GlobalDatabaseNameArg) == null;
			newFile = createNewDatabase(databaseName, null, ask);
			if (newFile == null) {
				System.out.println("Unable to create "+newFile);
				return;
			}
			
		}
		
		getRecentDatabases().add(0, newFile);
		
	}

	@Override
	public String browseDatabases(Component parent) {
		PamFileFilter fileFilter = new PamFileFilter("Sqlite Database", ".sqlite");
		fileFilter.addFileType(".sqlite3");
		fileFilter.addFileType(".db");
		fileFilter.addFileType(".db3");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		File startPlan = PamFolders.getFileChooserPath(getDatabaseName());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (startPlan != null) {
			if (startPlan.isDirectory()) {
				fileChooser.setCurrentDirectory(startPlan);
			}
			else {
				fileChooser.setSelectedFile(startPlan);
			}
		}
		int state = fileChooser.showOpenDialog(parent);
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
			return currFile.getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Create a new empty database file. 
	 * @param newDB full path for database file (can be missing .sqlit3 if you like - this will get checked and added). 
	 * @param parent window (for confirm dialog, can be null)
	 * @param askFirst show a confirm dialog before creating the database file. 
	 * @return a path to the file, whether created or no. 
	 */
	public File createNewDatabase(String newDB, Component parent, boolean askFirst) {

		File newFile = new File(newDB);
		newFile = PamFileFilter.checkFileEnd(newFile, ".sqlite3", true);

		if (askFirst) {
			int ans = JOptionPane.showConfirmDialog(parent, "Create blank database " + newFile.getAbsolutePath() + " ?", "Sqlite", JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.CANCEL_OPTION) {
				return null;
			}
		}
		Connection connection = null;

		try {
			// create a database connection;
			// Sqlite will automatically create file if it does not exist; 
			connection = DriverManager.getConnection("jdbc:sqlite:" + newFile);

		}
		catch(SQLException e)
	    {
	      System.err.println(e.getMessage());
	    }
	    finally
	    {
	      try
	      {
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e)
	      {
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
		return newFile;
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

		Connection con = null;
		try {
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

			con = config.createConnection("jdbc:sqlite:" + dbName);
			con.setAutoCommit(dbControl.getDbParameters().getUseAutoCommit());			    

		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}

		if (con == null) {
			System.out.println("Unable to open database " + dbName);
		}

		if (con == null) {
			return null;
		}
		//			System.out.println("-------------------------------     OpenedSQLite database " + dbName + " handle " + con);
		return new PamConnection(this, con, sqlTypes);
	}

	@Override
	public void closeConnection(PamConnection connection) {
		try
		{
			if(connection != null){
				//	        	if (USEAUTOCOMMIT == false) {
				connection.getConnection().commit();
				//	        	}
				connection.getConnection().close();
			}
		}
		catch(SQLException e)
		{
			// connection close failed.
			System.err.println(e);
		}
		//		 if (connection != null) {
		//			 System.out.println("-------------------------------     Closed SQLite database  handle " + connection.getConnection());
		//		 }
	}


	@Override
	public boolean hasDriver() {
		try {
			// return false if it's a 64 bit JVM
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	@Override
	public String getDatabaseName() {
		/*
		 * If a database name was passed as a global argument, then use the passed name instead of 
		 * the name in the list of recent databases. 
		 */
		String commandName = GlobalArguments.getParam(DBControl.GlobalDatabaseNameArg);
		if (commandName != null) {
			return commandName;
		}
		
		if (getRecentDatabases() == null) return null;
		if (getRecentDatabases().size() < 1) return null;
		return getRecentDatabases().get(0).getAbsolutePath();
	}
	
	@Override
	public String getShortDatabaseName() {
		if (getRecentDatabases() == null) return null;
		if (getRecentDatabases().size() < 1) return null;
		return getRecentDatabases().get(0).getName();
	}

	@Override
	public SystemDialogPanel getDialogPanel(Component parent) {
		if (dialogPanel == null) {
			dialogPanel = new SqliteDialogPanel(parent, this);
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

	public String getUnitName() {
		return dbControl.getUnitName();
	}

	public String getUnitType() {
		return "Sqlite Database System";
	}

	//
	//
	//    PAM SETTINGS METHODS
	//
	//

	public Serializable getSettingsReference() {
		return sqliteParameters;
	}

	public long getSettingsVersion() {
		return 0;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		Object settings = pamControlledUnitSettings.getSettings();
		if (settings instanceof ArrayList) { // deal with old format which just stored a list. 
			sqliteParameters.getRecentDatabases().clear();
			sqliteParameters.getRecentDatabases().addAll((ArrayList) settings);
		}
		else if (settings instanceof SQLiteParameters){
			this.sqliteParameters = ((SQLiteParameters) settings).clone();
		}
		else {
			return false;
		}
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
		File dbFile = new File(databaseName);
		if (dbFile.exists() == false) {
			String msg = "Database " + databaseName + " does not exist";
			WarnOnce.showWarning("Missing database", msg, WarnOnce.WARNING_MESSAGE, null);
			return false;
		}
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(dbFile);
			}
		} catch (IOException ioe) {
			//		     ioe.printStackTrace();
			//			   System.out.println("Unable to open ");
		}

		return true;
	}
	
	/**
	 * File chooser for JavaFX 
	 */
	private FileChooser fileChooser=new FileChooser();
	/**
	 * FX methods to browse for an sqlite file. 
	 */
	@Override
	public String browseDatabasesFX(int type) {
		//JavaFX version
		configureFileChooser(fileChooser);
		//set initial directory as last open database file. 
		if (getRecentDatabases()!=null && getRecentDatabases().size()>0 && getRecentDatabases().get(0).exists() 
				&& getRecentDatabases().get(0).getParentFile().isDirectory()){
			fileChooser.setInitialDirectory(
					getRecentDatabases().get(0).getParentFile()
					);               
		}
		//open file chooser
		File file=null;
		if (type==0)  file = fileChooser.showOpenDialog(PamController.getMainStage());
		//if (type==1)  file = fileChooser.showOpenMultipleDialog(PamController.getMainStage());
		if (type==2)  file = fileChooser.showSaveDialog(PamController.getMainStage());
		
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Set parameters for the file chooser.
	 * @param fileChooser - filechooser to configure. 
	 */
	protected static void configureFileChooser(
			final FileChooser fileChooser) {      
		fileChooser.setTitle("Open SQLite Database File");
		fileChooser.setInitialDirectory(
				new File(PamFolders.getDefaultProjectFolder())
				);                 
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("SQLite Database File", "*.sqlite", "*.sqlite3", "*.db", "*.db3")
		);
	}


	@Override
	public SystemDialogPaneFX getDialogPaneFX() {
		if (sqlPane==null) sqlPane=new SqlitePaneFX(this);
		return sqlPane;
	}

	@Override
	protected boolean createNewDatabase(String forcedName) {
		// TODO Auto-generated method stub
		PamConnection con = getConnection(forcedName);
		if (con == null) return false;
		try {
			con.getConnection().close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return false;
		}
		return true;
	}

	//	@Override
	//	protected PamConnection reOpenConnection(PamConnection connection) {
	//		closeConnection(connection);
	//		return getConnection();
	//	}	
}
