package generalDatabase.parameterstore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

/**
 * Store parameters from a managed parameter set in the PAMGuard database. These go into a dead simple table, which has
 * two columns. The first is a name, the second a string value. Each parameter can only appear once. 
 * This works with ManagedParameters using the same names and field names that go into the xml output. 
 * @author dg50
 *
 */
public class ParameterDatabaseStore {

	
	private EmptyTableDefinition tableDef;
	private PamTableItem nameItem, dataItem;
	
	public ParameterDatabaseStore(String tableName) {
		tableDef = new EmptyTableDefinition(tableName);
		tableDef.addTableItem(nameItem = new PamTableItem("ParameterName", Types.VARCHAR));
		tableDef.addTableItem(dataItem = new PamTableItem("Value", Types.VARCHAR));
	}
	
	public boolean saveParameterSet(ManagedParameters managedParameters) {
		if (managedParameters == null) {
			return false;
		}
		return saveParameterSet(managedParameters.getClass().getSimpleName(), managedParameters);
	}

	private boolean saveParameterSet(String name, ManagedParameters managedParameters) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		PamConnection con = dbControl.getConnection();
		if (checkTable(con) == false) {
			return false;
		}
		
		String prefix;
		if (name == null) {
			prefix = "";
		}
		else {
			prefix = name + ".";
		}
		PamParameterSet paramSet = managedParameters.getParameterSet();
		Collection<PamParameterData> params = paramSet.getParameterCollection();
		for (PamParameterData paramData : params) {
			String paramName = paramData.getFieldName();
			paramName = prefix + paramName;
			Object data = null;
			try {
				data = paramData.getData();// .getField().get(managedParameters);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
//			System.out.printf("Store param \"%s\" as \"%s\"\n", paramName, data);
			saveToDatabase(con, paramName, data);
		}
		dbControl.commitChanges();
		
		return true;
	}
	
	private boolean saveToDatabase(PamConnection con, String name, Object data) {
		int[] existing = findExistingRows(con, name);
		boolean ok = true;
		if (existing == null || existing.length == 0) {
			ok |= newRecord(con, name, data);
		}
		else {
			ok |= updateRecord(con, existing[0], name, data);
			if (existing.length > 1) {
				for (int i = 1; i < existing.length; i++) {
					ok |= deleteDuplicateRow(con, existing[i]);
				}
			}
		}
		return true;
	}
	
	private int[] findExistingRows(PamConnection con, String name) {
		/**
		 * Find existing rows with that name. 
		 */
		int[] rows = new int[0];
		if (con == null) {
			return rows;
		}
		String qStr = String.format("SELECT Id FROM %s WHERE %s='%s'", tableDef.getTableName(), nameItem.getName(), name);
		try {
			Statement stmt = con.getConnection().createStatement();
			ResultSet res = stmt.executeQuery(qStr);
			while (res.next()) {
				int rowId = res.getInt(1);
				rows = Arrays.copyOf(rows, rows.length+1);
				rows[rows.length-1] = rowId;
			}
			res.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	private boolean newRecord(PamConnection con, String name, Object data) {
		
		String insertStr = tableDef.getSQLInsertString(con.getSqlTypes());
		try {
			PreparedStatement stmt = con.getConnection().prepareStatement(insertStr);
			stmt.setString(1, name);
			if (data == null) {
				stmt.setNull(2, Types.VARCHAR);
			}
			else {
				stmt.setString(2, data.toString());
			}
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private boolean updateRecord(PamConnection con, int iRow, String name, Object data) {
		SQLTypes st = con.getSqlTypes();
		String updateString = String.format("UPDATE %s SET %s = '%s' WHERE Id = %d", tableDef.getTableName(), 
			st.formatColumnName(dataItem.getName()), data, iRow);

		try {
			PreparedStatement stmt = con.getConnection().prepareStatement(updateString);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private boolean deleteDuplicateRow(PamConnection con, int rowId) {
		
		String delStr = String.format("DELETE FROM %s WHERE Id=%d", tableDef.getTableName(), rowId);

		try {
			PreparedStatement stmt = con.getConnection().prepareStatement(delStr);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean checkTable(PamConnection con) {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		dbControl.commitChanges();
		return dbControl.getDbProcess().checkTable(tableDef);
	}

}
