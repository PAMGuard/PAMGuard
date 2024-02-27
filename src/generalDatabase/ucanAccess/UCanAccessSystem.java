package generalDatabase.ucanAccess;

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

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import generalDatabase.BaseAccessSystem;
import generalDatabase.DBControl;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.MSAccessDialogPanel;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;
import generalDatabase.SystemDialogPanel;
import generalDatabase.layoutFX.SystemDialogPaneFX;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

public class UCanAccessSystem extends BaseAccessSystem implements PamSettings {

	private DBControl dbControl;
	private int settingsStore;

	private SQLTypes sqlTypes = new UCanAccessSqlTypes();
		
	private MSAccessDialogPanel dialogPanel;

	private static ArrayList<File> recentDatabases = new ArrayList<File>();

	/**
	 * @return the recentDatabases
	 */
	public ArrayList<File> getRecentDatabases() {
		return recentDatabases;
	}
	
	public UCanAccessSystem(DBControl dbControl, int settingsStore) {
		this.dbControl = dbControl;
		this.settingsStore = settingsStore;
		PamSettingManager.getInstance().registerSettings(this, settingsStore);
		if (recentDatabases == null) {
			recentDatabases = new ArrayList<>();
		}
	}

	@Override
	public String getSystemName() {
		return "MS Access (using UCanAccess)";
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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getDatabaseName() {
		if (recentDatabases == null) return null;
		if (recentDatabases.size() < 1) return null;
		return recentDatabases.get(0).getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see generalDatabase.DBSystem#getShortDatabaseName()
	 */
	@Override
	public String getShortDatabaseName() {
		if (recentDatabases == null) return null;
		if (recentDatabases.size() < 1) return null;
		return recentDatabases.get(0).getName();
	}

	@Override
	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}

	@Override
	public boolean exists() {
		File file = new File(getDatabaseName());
		if (file == null) return false;
		return file.exists();
	}

	@Override
	public boolean create() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PamConnection getConnection(String latest) {
		try {
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		} catch (ClassNotFoundException e) {
			System.out.println(e.getLocalizedMessage());
			System.out.println("Check your UcanaccessDriver classpath ! ");
//			System.exit(1);
			return null;
		} catch (Exception genEx) {
			genEx.printStackTrace();
		}
		
		String passwordEntry = "";
		if (latest == null) {
			return null;
		}
		File fl = new File(latest);
		long size = fl.length();
		String noMem=size>30000000?";memory=true":"";
		noMem = "";
		
		Connection conn = null;
		if (fl.exists() == false) {
			System.out.println("file " + fl.getAbsolutePath() + " does not exist");
			return null;
		}
		try {
			String conStr = "jdbc:ucanaccess://"
					+ fl.getAbsolutePath() + passwordEntry+noMem;
			System.out.println("UCanAccess connection string = " + conStr);
			conn = DriverManager.getConnection(conStr,"","");
			conn.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		PamCursorManager.setCursorType(PamCursorManager.NON_SCROLLABLE);
		if (conn == null) {
			return null;
		}
		return new PamConnection(this, conn, sqlTypes);
	}

	@Override
	public SystemDialogPanel getDialogPanel(Component parent) {
		if (dialogPanel == null) {
			dialogPanel = new MSAccessDialogPanel(parent, this);
		}
		return dialogPanel;
	}

	@Override
	public PamCursor createPamCursor(EmptyTableDefinition tableDefinition) {
		return new NonScrollablePamCursor(tableDefinition);
	}

	@Override
	public boolean hasDriver() {
		try {
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public String getUnitName() {
		return dbControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "UCan Access DBMS";
	}

	@Override
	public Serializable getSettingsReference() {
		return recentDatabases;
	}

	@Override
	public long getSettingsVersion() {
		return 0;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		recentDatabases = (ArrayList<File>) (pamControlledUnitSettings.getSettings());
		return true;
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
		// TODO Auto-generated method stub
		return false;
	}
}
