package generalDatabase.sqlServerExpress;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import generalDatabase.DBSystem;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;
import generalDatabase.SystemDialogPanel;
import generalDatabase.layoutFX.SystemDialogPaneFX;
import generalDatabase.pamCursor.NonScrollablePamCursor;
import generalDatabase.pamCursor.PamCursor;

public class SQLServerSystem extends DBSystem {
	
	private static final String name = "SQL Server";
	
	transient private SQLTypes sqlTypes = new SQLTypes();

	public SQLServerSystem() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSystemName() {
		return name;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public String getDatabaseName() {
		return name;
	}

	@Override
	public SQLTypes getSqlTypes() {
		return sqlTypes;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean create() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PamConnection getConnection(String name) {
		String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
				"databaseName=PamguardTest;user=GBLAPTOP-DG50\\dg50;";
		Connection con = null;
		try {
			con = DriverManager.getConnection(connectionUrl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return new PamConnection(this, con, sqlTypes);
	}

	@Override
	public String browseDatabases(Component parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SystemDialogPanel getDialogPanel(Component parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamCursor createPamCursor(EmptyTableDefinition tableDefinition) {
		return new NonScrollablePamCursor(tableDefinition);
	}

	@Override
	public boolean hasDriver() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		}
		catch (ClassNotFoundException e) {
			return false;
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
