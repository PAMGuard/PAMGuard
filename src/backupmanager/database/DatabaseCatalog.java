package backupmanager.database;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import backupmanager.action.ActionMaker;
import backupmanager.action.BackupAction;
import backupmanager.stream.BackupStream;
import backupmanager.stream.BasicStreamItem;
import backupmanager.stream.StreamItem;
import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

public class DatabaseCatalog extends BackupCatalog {

	private HashMap<BackupAction, Integer> actionMap;

	private static final String tablePrefix = "Backup Items ";

	private EmptyTableDefinition fullTableDef;

	private static final int MAX_NAME_LENGTH = 255;

	private static final String sutc = "UTC";
	private static final String screated = "Created";
	private static final String smodified = "Modified";
	private static final String sname = "Name";
	private static final String ssize = "Size";
	private PamTableItem utc, created, modified;
	private PamTableItem name, size;

	private PamTableItem[] actionItems;
	
	private PamTableItem[] filterItems;
	
	private DBControlUnit dbControlUnit;

	private PamConnection pamConnection;

	public DatabaseCatalog(BackupStream backupStream, String source) {
		super(backupStream, source);

		// map the actions. This is needed for getting table names. 
		actionMap = new HashMap<BackupAction, Integer>();
		ArrayList<BackupAction> actions = backupStream.getActions();
		int ind = 0;
		for (BackupAction action : actions) {
			actionMap.put(action, ++ind);
		}
		makeTableDefinition();

		dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit != null) {
			pamConnection = dbControlUnit.getConnection();
			DBProcess dbProcess = dbControlUnit.getDbProcess();
			boolean tableOk = dbProcess.checkTable(fullTableDef);
			if (tableOk == false) {
				System.err.println("Error checking backup table " + fullTableDef.getTableName());
			}
		}
	}

	@Override
	public List<StreamItem> catalogNewItems(List<StreamItem> allSourceItems) {
		/**
		 * Source items will have their fill path. existing items will be without
		 * their root (saving path length in database). 
		 */
		HashMap<String, StreamItem> exMap = getExistingItems();
		List<StreamItem> newItems = new ArrayList<StreamItem>();
		for (StreamItem srcItem : allSourceItems) {
			String storeString  = sourceToStoredString(srcItem);
			StreamItem exItem = exMap.get(storeString);
			if (isSame(exItem, srcItem)) {
				continue; // item with same name and sze already in database. 
			}
			newItems.add(srcItem);
		}
		/*
		 * Save in one go, easier and quicker with a prepared statement. 
		 */
		saveNewItems(newItems);
		return newItems;
	}
	
	private boolean isSame(StreamItem exItem, StreamItem srcItem) {
		if (exItem == null) {
			return false;
		}
		Long exSz = exItem.getSize();
		Long srcSz = srcItem.getSize();
		if (exSz == null || srcSz == null) {
			return true;
		}
		return (exSz.compareTo(srcSz) == 0); // force as numbers.
	}

	/**
	 * Save new items into the database. 
	 * @param newStreamItems
	 * @return number saved. 
	 */
	private int saveNewItems(List<StreamItem> newStreamItems) {
		if (newStreamItems == null || newStreamItems.size() == 0) {
			return 0;
		}
		SQLTypes sqlTypes = pamConnection.getSqlTypes();
		String insert = makeBaseInsertString(pamConnection);
		int nInsert = 0;
		try {
			PreparedStatement stmt = pamConnection.getConnection().prepareStatement(insert);
			for (StreamItem streamItem : newStreamItems) {
				stmt.setObject(1, sqlTypes.getTimeStamp(System.currentTimeMillis()));
				if (streamItem.getStartUTC() != null) {
					stmt.setObject(2, sqlTypes.getTimeStamp(streamItem.getStartUTC()));					
				}
				else {
					stmt.setObject(2, null);
				}
				if (streamItem.getEndUTC() != null) {
					stmt.setObject(3, sqlTypes.getTimeStamp(streamItem.getEndUTC()));					
				}
				else {
					stmt.setObject(3, null);
				}
				stmt.setObject(4, sourceToStoredString(streamItem));
				stmt.setObject(5, streamItem.getSize());
//				stmt.get
				stmt.execute();
			}
			if (pamConnection.getConnection().getAutoCommit() == false) {
				pamConnection.getConnection().commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		
		return nInsert;
	}

	private String makeBaseInsertString(PamConnection pamConnection) {
		SQLTypes sqlTypes = pamConnection.getSqlTypes();
		String str = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", 
				fullTableDef.getTableName(), 
				sqlTypes.formatColumnName(utc.getName()),
				sqlTypes.formatColumnName(created.getName()),
				sqlTypes.formatColumnName(modified.getName()),
				sqlTypes.formatColumnName(name.getName()),
				sqlTypes.formatColumnName(size.getName()));
		return str;
	}
	
	/**
	 * Takes the source path off of a full path name, so we can just 
	 * deal with the latter part. 
	 * @param item
	 * @return
	 */
	private String sourceToStoredString(StreamItem item) {
		return toStoredString(getSource(), item);
	}
//	/**
//	 * Takes the destination path off of a full path name, so we can just 
//	 * deal with the latter part. 
//	 * @param item
//	 * @return
//	 */
//	private String destToStoredString(StreamItem item) {
//		return toStoredString(getDestination(), item);
//	}
	
	private String toStoredString(String root, StreamItem item) {
		int indOfSrc = item.getName(). indexOf(root);
		if (indOfSrc < 0) {
			return item.getName();
		}
		else {
			return item.getName().substring(indOfSrc + root.length());
		}
	}

	@Override
	public List<StreamItem> getUnactedItems(List<StreamItem> sourceItems, BackupAction action) {
		String colName = getActionColumnName(action);
		ArrayList<StreamItem> items = new ArrayList<StreamItem>();
		String selString = String.format("SELECT Id, Created, Modified, Name, Size FROM %s WHERE %s IS NULL ORDER BY Name", 
				fullTableDef.getTableName(), colName);
		Long lastId;
		try {
			Statement stmt = pamConnection.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(selString);
			while (result.next()) {
//				long utc = SQLTypes.millisFromTimeStamp(result.getObject(1));
				long id = result.getLong(1);
				Long created = SQLTypes.millisFromTimeStamp(result.getObject(2));
				Long end = SQLTypes.millisFromTimeStamp(result.getObject(3));
				String name = result.getString(4).trim();
				Long size = result.getLong(5);
				// need to reattach the source path to this if we're going to find it. 
				File fullFile = new File(getSource(), name);
				items.add(new BasicStreamItem(id, fullFile.getAbsolutePath(), created, end, size));
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return items;
	}

	@Override
	public boolean updateItem(StreamItem streamItem, BackupAction action) {
		String filterName = getFilterColumnName(action);
		String colName = getActionColumnName(action);
		String sql = String.format("UPDATE %s SET %s='%s', %s='%s' WHERE Id = %d", fullTableDef.getTableName(), filterName, streamItem.getFilterMessage(), 
				colName, streamItem.getActionMessage(), streamItem.getDatabaseIndex());

		int rows = 0;
		try {
			Statement stmt = pamConnection.getConnection().createStatement();
			rows = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows == 1;
	}
	
	/**
	 * Get everything in the table as a hashmap. 
	 * @param pamConnection
	 * @return
	 */
	private HashMap<String, StreamItem> getExistingItems() {
		HashMap<String, StreamItem> items = new HashMap<String, StreamItem>();
		String selString = String.format("SELECT Id, Created, Modified, Name, Size FROM %s ORDER BY Name", fullTableDef.getTableName());
		Long lastId;
		try {
			Statement stmt = pamConnection.getConnection().createStatement();
			ResultSet result = stmt.executeQuery(selString);
			while (result.next()) {
//				long utc = SQLTypes.millisFromTimeStamp(result.getObject(1));
				lastId = result.getLong(1);
				Long created = SQLTypes.millisFromTimeStamp(result.getObject(2));
				Long mod = SQLTypes.millisFromTimeStamp(result.getObject(3));
				String name = result.getString(4).trim();
				Long size = result.getLong(5);
				items.put(name, new BasicStreamItem(lastId, name, created, mod, size));
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return items;
	}
	

	private void makeTableDefinition() {
		BackupStream backupStream = getBackupStream();
		String tableName = tablePrefix + backupStream.getName();
		fullTableDef = new EmptyTableDefinition(tableName);
		fullTableDef.addTableItem(utc = new PamTableItem(sutc, Types.TIMESTAMP));
		fullTableDef.addTableItem(created = new PamTableItem(screated, Types.TIMESTAMP));
		fullTableDef.addTableItem(modified = new PamTableItem(smodified, Types.TIMESTAMP));
		fullTableDef.addTableItem(name = new PamTableItem(sname, Types.CHAR, MAX_NAME_LENGTH));
		fullTableDef.addTableItem(size = new PamTableItem(ssize, Types.BIGINT));

		ArrayList<BackupAction> actions = backupStream.getActions();
		filterItems = new PamTableItem[actions.size()];
		actionItems = new PamTableItem[actions.size()];
		//	for (int i = 0; i < decisionItems.length; i++) {
		//		decisionItems[i] = new PamTableItem("Decision " + decisions.get(i).getName(), Types.CHAR, DECISION_ITEM_LENGTH);
		//		tableDef.addTableItem(decisionItems[i]);
		//	}
		for (int i = 0; i < actionItems.length; i++) {
			filterItems[i] = new PamTableItem(getFilterColumnName(actions.get(i)), Types.CHAR, MAX_NAME_LENGTH);
			actionItems[i] = new PamTableItem(getActionColumnName(actions.get(i)), Types.CHAR, MAX_NAME_LENGTH);
			fullTableDef.addTableItem(filterItems[i]);
			fullTableDef.addTableItem(actionItems[i]);
		}
	}	

//	public EmptyTableDefinition getTableDef() {
//		return fullTableDef;
//	}

	/*
	 * Column name for the actio. 
	 */
	public String getActionColumnName(BackupAction action) {
		Integer iAction = actionMap.get(action);
		if (iAction == null) iAction = 99;
		ActionMaker maker = action.getActionMaker();
		String name = String.format("Act_%d_%s", iAction, maker.getName());
		name = name.replace(' ', '_');
		return name;
	}
	
	/*
	 * Column name for the action filter. 
	 */
	public String getFilterColumnName(BackupAction action) {
		Integer iAction = actionMap.get(action);
		if (iAction == null) iAction = 99;
		ActionMaker maker = action.getActionMaker();
		String name = String.format("Act_%d_Filter", iAction, maker.getName());
		name = name.replace(' ', '_');
		return name;
	}

	@Override
	public void backupComplete() {
		dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit != null) {
			dbControlUnit.commitChanges();
		}		
	}
}
