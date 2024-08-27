package generalDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Some general functions to do with table checks which I want to use outside of the 
 * normal dattabse interface. 
 * @author Doug Gillespie
 *
 */
public class TableUtilities {
	
	public static boolean columnExists(PamConnection con, String tableName, String columnName, int sqlType) {

		try {
			DatabaseMetaData dbm = con.getConnection().getMetaData();
			ResultSet columns = dbm.getColumns(null, null, tableName, columnName);
			while (columns.next()) {
				
				// now check the format
				String colName = columns.getString(4);
				int colType = columns.getInt(5);
				//			if (colType == tableItem.getSqlType()) return true;
				//			//String strColType = columns.getString(6);
				if (columnName.equalsIgnoreCase(colName)) {
					return true;
				}
			}
//			if (databaseControll.databaseSystem.getSystemName().equals(OODBSystem.SYSTEMNAME)){
//				columns = dbm.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase());
//				
//				while (columns.next()) {
//					// now check the format
//					String colName = columns.getString(4);
//					int colType = columns.getInt(5);
//					int colLength= columns.getInt(7);
//					System.out.println("collength="+colLength);
//					if (columnName.equalsIgnoreCase(colName)) {
//						String colTypeString=databaseControll.databaseSystem.getSqlTypes().typeToString(colType, colLength);
//						System.out.println("Found column " +colName +" type: "+colTypeString+" in "+tableName);
//						
//						
//						return true;
//					}
//					System.out.println("Col not found");
//				}
//			}
			
			
//			return false;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	public static boolean addColumn(Connection con, SQLTypes sqlTypes, String systemName, EmptyTableDefinition tableDef, PamTableItem tableItem) {
		
		String addString = "ALTER TABLE " + tableDef.getTableName();
		
		
		if (tableItem.isCounter() && systemName.equals(MSAccessSystem.SYSTEMNAME)) {
			//Access and MySQL may handle these differently as per create table
			
			addString += " ADD " + tableItem.getName() + " COUNTER ";
			
			
		}else {
			addString += " ADD COLUMN " + tableItem.getName() + " ";
			addString += sqlTypes.typeToString(tableItem.getSqlType(), tableItem.getLength(), tableItem.isCounter());
		}
		
		Statement stmt;
		
		try {
			stmt = con.createStatement();
			int addResult;
			addResult = stmt.executeUpdate(addString);
		}
		catch (SQLException ex) {
			System.out.println(addString);
			ex.printStackTrace();
			return false;
		}
		
		if (tableItem.isPrimaryKey()) {
			String primaryString = "ALTER TABLE "+tableDef.getTableName()+" ADD PRIMARY KEY ( " + tableItem.getName() + " )";
		
			try {
				stmt = con.createStatement();   
				int primaryResult;
//				System.out.println(primaryString);
				primaryResult = stmt.executeUpdate(primaryString); 
			}
			catch (SQLException ex) {
				System.out.println("Column added but could not be made primary key");
				System.out.println(primaryString);
				ex.printStackTrace();
				
				return false;
			}
		}
		return true;
		
	}
	public static boolean tableExists(PamConnection con, String systemName, EmptyTableDefinition tableDef) {
		try {
			DatabaseMetaData dbm = con.getConnection().getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableDef.getTableName(), null);
			
			if (tables.next()){
				 return true;
			}
//			if (systemName.equals(OOoDBSystem.SYSTEMNAME)){
//				ResultSet oodbTables = dbm.getTables(null, null, /*tableDef.getTableName().toUpperCase()*/null, null);
//				
//				while (oodbTables.next()){
//					
//					if (oodbTables.getString(3).trim().equalsIgnoreCase(tableDef.getTableName())){
////						System.out.println("Table Found: "+oodbTables.getString(3));
//						tableDef.setTableName(oodbTables.getString(3).trim().toUpperCase());
//						return true;
//					}
//					
//				}
//				
//				System.out.println("Table Not Found: "+tableDef.getTableName().toUpperCase());
//			}
			
		}
		catch (SQLException e) {
			System.out.println("Invalid table name " + tableDef.getTableName());
			e.printStackTrace();
		}
		
		return false;
	}
}
