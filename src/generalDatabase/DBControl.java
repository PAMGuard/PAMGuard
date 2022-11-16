package generalDatabase;

import generalDatabase.external.CopyManager;
import generalDatabase.layoutFX.DBGuiFX;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.postgresql.PostgreSQLSystem;
import generalDatabase.sqlite.SqliteSystem;
import loggerForms.FormsControl;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.io.FilenameUtils;

import offlineProcessing.DataCopyTask;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import warnings.PamWarning;
import warnings.WarningSystem;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamSettingsGroup;
import PamController.PamSettingsSource;
import PamModel.SMRUEnable;
import PamView.PamSidePanel;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.userforms.UserFormAnnotationType;

/**
 * Database system for accessing data in just about any type of odbc database.
 * <p>
 * This gets used in two slightly different ways in Pamguard. The first is 
 * the obvious reading and writing of data to a variety of tables. The second is the 
 * loading of settings from the PAmguard_settings and the PamguardModules tables tables
 * in which all program settings were serialised and stored as 6 bit ascii strings each time
 * PAMGUARD started collecting data. 
 * <p>
 * So that an instance of DBControl can be made that doesn't load settings, two sub classes
 * have been made: DBContorlUnit for normal use and DBControlSettings for reading in settings
 * information.
 *  
 * @author Doug Gillespie
 * @see DBControlSettings
 * @see DBControlUnit
 */

abstract public class DBControl extends PamControlledUnit implements PamSettings, 
PamSettingsSource {

	ArrayList<DBSystem> databaseSystems;

	DBSystem databaseSystem;

	private PamConnection connection;

	DBParameters dbParameters = new DBParameters();

	private DBProcess dbProcess;

	DBSidePanel dbSidePanel;

	private DBSettingsStore dbSettingsStore;

	/**
	 * Do full check of all database tables, not just specieals for
	 * controller. 
	 */
	private boolean fullTablesCheck = false;

	static private String dbUnitType = "Pamguard Database";
	
	static public final String GlobalDatabaseNameArg = "-databasefile";

	private DBControl THIS;

	private JMenuItem openDatabaseMenu;

	private PamWarning databaseWarning;

	private CopyManager copyDatabase;

	/**
	 * THE FX GUI for the database 
	 */
	private DBGuiFX dbGUIFX;

	private int lastErrorCount;

	public DBControl(String unitName, int settingsStore, boolean openImmediately) {
		super(dbUnitType, unitName);
		THIS = this;

		databaseWarning = new PamWarning(getUnitName(), "Database error", 2);
		databaseWarning.setWarningTip("Check that the database driver is correclty installed and that the database file exists");
		if (isViewer() && DBControlUnit.class.isAssignableFrom(this.getClass())) {
			copyDatabase = new CopyManager((DBControlUnit) this);
		}
		createDBControl(settingsStore, openImmediately);
	}

	protected boolean addDatabaseSystem(DBSystem dbSystem) {
		if (dbSystem.hasDriver()) {
			databaseSystems.add(dbSystem);
		}
		else {
			System.out.println(String.format("%s Database system is unavailable on this platform", 
					dbSystem.getSystemName()));
			return false;
		}
		return true;
	}

	void createDBControl(int settingsStore, boolean openImmediately) {

		databaseSystems = new ArrayList<DBSystem>();
		addDatabaseSystem(new SqliteSystem(this, settingsStore));
		addDatabaseSystem(new MySQLSystem(this, settingsStore));
		addDatabaseSystem(new MSAccessSystem(this, settingsStore));
		//		addDatabaseSystem(new UCanAccessSystem(this, settingsStore));
		if (SMRUEnable.isEnable()) {
//			addDatabaseSystem(new PostgreSQLSystem(this, settingsStore));
			//						addDatabaseSystem(new UCanAccessSystem(this, settingsStore));
			//			addDatabaseSystem(new SQLServerSystem());
			//			addDatabaseSystem(new OOoDBSystem(this, settingsStore));
		}

		addPamProcess(dbProcess = new DBProcess(this));

		dbSidePanel = new DBSidePanel(this);

		if (settingsStore != 0) {
			PamSettingManager.getInstance().registerSettings(this, settingsStore);
		}

		//		selectDatabase(null);

		if (databaseSystem == null){
			selectSystem(dbParameters.getDatabaseSystem(), openImmediately);
		}

		// not needed - this happens in selectSystem anyway. 
		//		if (isViewer == false) {
		//			if (databaseSystem != null){
		//				//			databaseSystem.setDatabaseName(dbParameters.databaseName);
		//				connection = databaseSystem.getConnection();
		//			}
		//		}
	}

	public boolean selectSystem(Class systemClass, boolean openDatabase, String forcedName) {
		int systemNumber = -1;
		for (int i = 0; i < databaseSystems.size(); i++) {
			if (databaseSystems.get(i).getClass() == systemClass) {
				systemNumber = i;
				break;
			}
		}
		return selectSystem(systemNumber, openDatabase, forcedName);
	}

	/**
	 * Select a database system
	 * @param systemNumber index of the database system
	 * @param openDatabase flag to immediately open the database
	 * @return true if all ok
	 */
	public boolean selectSystem(int systemNumber, boolean openDatabase) {
		return selectSystem(systemNumber, openDatabase, null);
	}
	
	/**
	 * Select a database system
	 * @param systemNumber index of the database system
	 * @param openDatabase flag to immediately open the database
	 * @return true if all ok
	 */
	public boolean selectSystem(int systemNumber, boolean openDatabase, String forcedName) {
		closeConnection();
		if (systemNumber >= databaseSystems.size() || systemNumber < 0) return false;
		databaseSystem = databaseSystems.get(systemNumber);
		if (openDatabase) {
			openDatabase(forcedName);
		}
		if (openDatabaseMenu != null) {
			openDatabaseMenu.setEnabled(databaseSystem != null && databaseSystem.canOpenDatabase());
		}
		dbSidePanel.updatePanel();
		return (connection != null);
	}
	
	public boolean openDatabase(String forcedName) {
		//			databaseSystem.setDatabaseName(dbParameters.databaseName);
		// Do a quick check here to see if the database exists.  If not, warn the user before creating a new one.  Note that if
		// the database name is null, the user is creating a brand new database so skip the check
		boolean checkExists = databaseSystem.checkDatabaseExists(forcedName);
		if (checkExists == false && forcedName != null) {
			databaseSystem.createNewDatabase(forcedName);
			checkExists = databaseSystem.checkDatabaseExists(forcedName);
		}

		if (checkExists == false) {
			String title = "Database not found";
			String msg = "PAMGuard is unable to access the following database:<br><br><b>" +
					forcedName +
					"</b><br><br>If you expected this database to exist, it may have been moved or PAMGuard may " +
					"be configured to be looking in a different folder.  Please check the expected folder location, as well " +
					"as the PAMGuard settings.";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
			return false;
		}
		if (forcedName == null) {
			connection = databaseSystem.getConnection();
		}
		else {
			connection = databaseSystem.getConnection(forcedName);
		}
		if (connection != null) {
			try {
				System.out.println("Database controller : " + getUnitName());
				System.out.println("Database system     : " + databaseSystem.getSystemName());
				DatabaseMetaData metaData = connection.getConnection().getMetaData();
				System.out.println("Driver              : " + metaData.getDriverName());
				System.out.println("ANSI92EntryLevelSQL : " + metaData.supportsANSI92EntryLevelSQL());
				System.out.println("Keywords            : " + metaData.getSQLKeywords());
				System.out.println("Add Column          : " + metaData.supportsAlterTableWithAddColumn());
				System.out.println("Auto Commit         : " + connection.getConnection().getAutoCommit());
				System.out.println("Updatable resultset : " + metaData.supportsResultSetConcurrency(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE));
			}
			catch (SQLException ex) {

			}
			WarningSystem.getWarningSystem().removeWarning(databaseWarning);
		}
		else {
			String warning = String.format("%s database system can't open Database %s", databaseSystem.getSystemName(), databaseSystem.getDatabaseName());
			databaseWarning.setWarningMessage(warning);
			WarningSystem.getWarningSystem().addWarning(databaseWarning);
			System.out.println(warning);
			//				System.out.println("Database system     : " + databaseSystem.getSystemName() + 
			//				" is not available");
		}
		return true;
	}

	/**
	 * Get the specified database system.
	 * @param systemNumber - the index of the system.
	 * @return the database systems at the specified index. 
	 */
	public DBSystem getSystem(int systemNumber) {
		if (systemNumber < 0 || systemNumber >= databaseSystems.size()) return null;
		return databaseSystems.get(systemNumber); 
	}



	@Override
	public boolean canClose() {
		return true;
	}

	/**
	 * Commit any unsaved database changes. 
	 * @return true if successful. false if no database or exception thrown. 
	 */
	public boolean commitChanges() {
		if (connection == null) {
			return false;
		}
		if (connection.getConnection() == null) {
			return false;
		}
		synchronized (DBControl.class) {
			try {
				if (connection.getConnection().getAutoCommit() == false) {
					connection.getConnection().commit();
				}
			} catch (SQLException e) {
				System.out.println("Unable to commit database changes: " + e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	@Override
	public void pamClose() {
		commitChanges();
		//this is very important for OOoDBs at least databaseSystem.closeConnection(); is
		closeConnection();
	}

	protected synchronized void closeConnection() {
		if (databaseSystem != null) {
			databaseSystem.closeConnection(connection);
			connection = null;
			databaseSystem = null;
		}
	}

	public String browseDatabases(Component parent) {
		if (databaseSystem == null) {
			return null;
		}
		return databaseSystem.browseDatabases(parent);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (PamController.getInstance().isInitializationComplete()) {
			switch(changeType) {
			case PamControllerInterface.ADD_CONTROLLEDUNIT:
			case PamControllerInterface.INITIALIZATION_COMPLETE:
			case PamControllerInterface.RENAME_CONTROLLED_UNIT:
				dbProcess.checkTables();
				dbProcess.updateProcessList();
				if (isViewer) {
					fillSettingsStore();
				}
				dbSidePanel.updatePanel();
			}
		}
	}

	/**
	 * Read all the settings in from storage. 
	 */
	public void fillSettingsStore() {
		if (dbProcess == null || dbProcess.getLogSettings() == null || connection == null) {
			return;
		}		
		dbSettingsStore = dbProcess.getLogSettings().loadSettings(connection);		
	}

	public PamConnection getConnection() {
		return connection;
	}

	public Serializable getSettingsReference() {
		dbParameters.setDatabaseName(getLongDatabaseName());
		return dbParameters;
	}

	public long getSettingsVersion() {
		return DBParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		DBParameters np = (DBParameters) pamControlledUnitSettings.getSettings();
		dbParameters = np.clone();
		//		selectSystem(dbParameters.databaseSystem, true);
		return true;
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		JMenu menu = new JMenu("Database");
		JMenuItem fileMenu = new JMenuItem("Database Selection ...");
		fileMenu.addActionListener(new DatabaseFileMenuAction(this, parentFrame));
		menu.add(fileMenu);

		openDatabaseMenu = new JMenuItem("View Database ...");
		openDatabaseMenu.addActionListener(new OpenDatabaseMenu(parentFrame));
		menu.add(openDatabaseMenu);

		if (isViewer) {
			JMenu copyMenu = new JMenu("Export Binary Data ...");
			ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
			int nCopy = 0;
			for (PamDataBlock aBlock:dataBlocks) {
				if (aBlock.getLogging() == null) continue;
				if (aBlock.getBinaryDataSource() == null) continue;
				JMenuItem menuItem = new JMenuItem(aBlock.getDataName());
				menuItem.addActionListener(new ExportDataBlock(parentFrame, aBlock));
				copyMenu.add(menuItem);
				nCopy ++;
			}
			copyMenu.setEnabled(nCopy > 0);
			menu.add(copyMenu);

			// now the database copy stuff. 
			if (copyDatabase != null) {
				copyDatabase.addMenuItems(menu, parentFrame);
			}

		}

		if (dbParameters.getUseAutoCommit() == false) {
			JMenuItem commitItem = new JMenuItem("Commit Changes");
			commitItem.setToolTipText("Immediately commit recent changes to the database");
			commitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					commitChanges();
				}
			});
			menu.add(commitItem);
		}

		if (SMRUEnable.isEnable()) {
			JMenuItem speedMenu = new JMenuItem("Test database speed");
			speedMenu.addActionListener(new SpeedMenu(parentFrame));
			menu.add(speedMenu);
			
			JMenuItem exSchema = new JMenuItem("Export database schema");
			exSchema.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					exportDatabaseSchema(parentFrame);
				}
			});
			menu.add(exSchema);
		}
		return menu;
	}


	protected void exportDatabaseSchema(JFrame parentFrame) {
		dbProcess.exportDatabaseSchema(parentFrame);
	}


	class DatabaseFileMenuAction implements ActionListener {

		private Frame frame;

		private DBControl dBControl;

		public DatabaseFileMenuAction(DBControl dbControl, Frame frame) {
			this.dBControl = dbControl;
			this.frame = frame;
		}

		public void actionPerformed(ActionEvent e) {

			selectDatabase(frame, null);

		}

	}

	class OpenDatabaseMenu implements ActionListener {
		private Frame frame;

		public OpenDatabaseMenu(Frame frame) {
			super();
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {	
			if (databaseSystem != null) {
				databaseSystem.openCurrentDatabase();
			}
		}
	}

	class SpeedMenu implements ActionListener {
		private Frame frame;


		public SpeedMenu(Frame frame) {
			super();
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {	
			DatabaseSpeedDialog.showDialog(frame);
		}

	}

	/**
	 * Action for general tansfer of data from binary store to 
	 * database. 
	 * @author Doug Gillespie
	 *
	 */
	class ExportDataBlock implements ActionListener {
		private Frame  frame;
		private PamDataBlock dataBlock;
		private OfflineTaskGroup offlineTaskGroup;
		private OLProcessDialog olProcessDialog;

		public ExportDataBlock(Frame frame, PamDataBlock dataBlock) {
			this.frame = frame;
			this.dataBlock = dataBlock;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// do the processing here so that some stuff can be saved.

			if (offlineTaskGroup == null) {
				offlineTaskGroup = new OfflineTaskGroup(THIS, dataBlock.getDataName());
				offlineTaskGroup.setPrimaryDataBlock(dataBlock);
				offlineTaskGroup.addTask(new DataCopyTask<PamDataUnit>(dataBlock));
				//				DbHtSummaryTask task = new DbHtSummaryTask(THIS, dataBlock);
				//				offlineTaskGroup.addTask(task);
			}
			if (olProcessDialog == null) {
				olProcessDialog = new OLProcessDialog(getPamView().getGuiFrame(), offlineTaskGroup, 
						dataBlock.getDataName() + " Export");
			}
			olProcessDialog.setVisible(true);
		}
	}


	protected void setWriteCount(int dbWriteOKs, int dbWriteErrors) {
		if (dbSidePanel == null) {
			return;
		}
		lastErrorCount = dbWriteErrors;
		dbSidePanel.writeCount(dbWriteOKs, dbWriteErrors);
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (isViewer) {
			return null;
		}
		else {
			return dbSidePanel;
		}
	}

	public static String getDbUnitType() {
		return dbUnitType;
	}

	public DBProcess getDbProcess() {
		return dbProcess;
	}

	public boolean saveSettingsToDB() {
		//		return false;
		return dbProcess.saveSettingsToDB();
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettingsSource#saveStartSettings(long)
	 */
	@Override
	public boolean saveStartSettings(long timeNow) {
		return dbProcess.saveStartSettings();
	}


	@Override
	public int getNumSettings() {
		if (dbSettingsStore == null) {
			return 0;
		}
		return dbSettingsStore.getNumGroups();
	}

	@Override
	public PamSettingsGroup getSettings(int settingsIndex) {
		if (dbSettingsStore == null) {
			return null;
		}
		return dbSettingsStore.getSettingsGroup(settingsIndex);
	}

	@Override
	public String getSettingsSourceName() {
		return getUnitName();
	}

	public boolean isFullTablesCheck() {
		return fullTablesCheck;
	}

	public void setFullTablesCheck(boolean fullTablesCheck) {
		this.fullTablesCheck = fullTablesCheck;
	}
	protected ArrayList<PamDataBlock> getLoggingDataBlocks() {
		ArrayList<PamDataBlock> loggingBlocks = new ArrayList<PamDataBlock>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (int i = 0; i < allDataBlocks.size(); i++) {
			if (allDataBlocks.get(i).getLogging() != null) {
				loggingBlocks.add(allDataBlocks.get(i));
			}
		}
		return loggingBlocks;
	}

	/**
	 * 
	 * @return the name of the current database
	 */
	public String getDatabaseName() {
		if (databaseSystem == null) {
			return null;
		}
		return databaseSystem.getShortDatabaseName();
	}

	/**
	 * 
	 * @return the name of the current database
	 */
	public String getLongDatabaseName() {
		if (databaseSystem == null) {
			return null;
		}
		return databaseSystem.getDatabaseName();
	}

	public PamCursor createPamCursor(EmptyTableDefinition tableDefinition) {
		if (databaseSystem == null) {
			return null;
		}
		return databaseSystem.createPamCursor(tableDefinition);
	}

	/**
	 * @return the current databaseSystem
	 */
	public DBSystem getDatabaseSystem() {
		return databaseSystem;
	}

	/**
	 * Reopen a database connection. This has a default action of 
	 * doing absolutely nothing since it's only actually required 
	 * by SqLite before it writes after doing some reading.  
	 * <p>
	 * Turned out this was never needed, but leave in in case it get's handy lter
	 * for some reason. e.g. see http://www.sqlite.org/lockingv3.html
	 * @param connection existing connection
	 * @return new connection. 
	 */
	public PamConnection reOpenConnection() {
		if (databaseSystem == null) {
			return null;
		}
		connection = databaseSystem.reOpenConnection(connection);
		return connection;
	}

	/**
	 * @return the dbParameters
	 */
	public DBParameters getDbParameters() {
		return dbParameters;
	}

	/**
	 * Get all the available database systems 
	 * @return an arrya of all available database systems . 
	 */
	public ArrayList<DBSystem> getDatabaseSystems() {
		return this.databaseSystems;
	}


	/**
	 * Open the database dialog. 
	 * @param frame - the GUI frame
	 * @param selectTitle - the title of the dialog
	 * @return - true if a database has been successfully selected. 
	 */
	public boolean selectDatabase(Frame frame, String selectTitle) {

		//this is a bit messy but difficult to figure this out in controller framework because
		//this is called before the controller has initialised properly. 
		if (PamGUIManager.getGUIType()==PamGUIManager.FX) {
			//open FX
			return ((DBGuiFX) getGUI(PamGUIManager.FX)).selectDatabase(PamController.getMainStage(), selectTitle, true); 
		}
		else if (PamGUIManager.getGUIType()==PamGUIManager.SWING) {
			//open 

			// First save the name of the existing database, by getting the correct database system from the dbParameters
			// object and retrieving the name of the first database in the recently-used list.  Double-check the connection
			// field - if it's null, it means we don't actually have the database loaded so just clear the name and continue
			// (this happens when starting Viewer mode)
			String currentDB = databaseSystems.get(dbParameters.getDatabaseSystem()).getDatabaseName();
			if (connection==null) {
				currentDB = null;
			}

			DBParameters newParams = DBDialog.showDialog(this, frame, dbParameters, selectTitle);
			if (newParams != null) {
				// first, check if there is a Lookup table.  If so, make sure to copy the contents over before
				// we lose the reference to the old database
				EmptyTableDefinition dummyTableDef = new EmptyTableDefinition("Lookup");
				boolean thereIsALookupTable = dbProcess.tableExists(dummyTableDef);
				LookupList lutList = null;
				if (thereIsALookupTable) {
					lutList = LookUpTables.getLookUpTables().getLookupList(null);
				}

				// Make sure we have info for the Logger (UDF) tables.  The getFormsControl call will
				// create a dummy FormsControl object if it doesn't already exist, and then load
				// any UDF tables it finds in the current database into memory.
				// This is important, because the user doesn't necessarily need a User Forms module
				// in their setup.  If they are only using the forms for annotations (say, for the
				// detection group localiser) then all they need is the tables in the database.  But
				// without a FormsControl object, the repopulateLoggerTables call below will fail.
				FormsControl formsControlTemp = UserFormAnnotationType.getFormsControl();
				formsControlTemp.readUDFTables();

				// Now get the name of the new database.  If they are the same, just exit.  This
				// is to prevent things like the Lookup table duplicating all of the rows because it
				// thinks the user wants to copy everything over to a new database, when all the user
				// really wants to do is open the dialog to have a quick look and make sure the database
				// loaded is the correct one.
				String newDB = databaseSystems.get(0).getDatabaseName();
				if (newDB != null && newDB.equals(currentDB)) return true;

				// if a new database, copy over important tables and initialize				
				dbParameters = newParams.clone();
				selectSystem(dbParameters.getDatabaseSystem(), true);
				dbProcess.checkTables();
				dbProcess.repopulateLoggerTables(formsControlTemp);
				if (thereIsALookupTable && lutList!=null) {
					LookUpTables.getLookUpTables().addListToDB(lutList);
				}
				fillSettingsStore();
				PamController.getInstance().getUidManager().runStartupChecks();	// if we've loaded a new database, synch the datablocks with the UID info
				return true;
			}
		}
		return false;
	}

	/**
	 * Set dB paramaters
	 * @param dbParameters
	 */
	public void setDBParameters(DBParameters dbParameters) {
		this.dbParameters=dbParameters;
	}

	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (dbGUIFX ==null) {
				dbGUIFX= new DBGuiFX(this);
			}
			return dbGUIFX;
		}
		//TODO swing
		return null;
	}

	/**
	 * @return the lastErrorCount
	 */
	public int getLastErrorCount() {
		return lastErrorCount;
	}


}
