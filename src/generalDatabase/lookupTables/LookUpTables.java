package generalDatabase.lookupTables;

import java.awt.Color;
import java.awt.Window;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Vector;

import PamController.PamController;
import PamView.dialog.warn.WarnOnce;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Singleton class for managing a common lookup table for 
 * many PAMGUARD modules (following from Logger format)
 * @author Doug Gillespie
 *
 */
public class LookUpTables {

	private static LookUpTables singleInstance;

	private EmptyTableDefinition lutTableDef;

	/**
	 * Maximum length of topic text for a lookup collection
	 */
	static public final int TOPIC_LENGTH = 50;
	/**
	 * Maximum length of a lookup code
	 */
	static public final int CODE_LENGTH = 12;
	/**
	 * Maximum length of a lookup item text
	 */
	static public final int TEXT_LENGTH = 50;

	private PamWarning lutWarning;

	private ArrayList<LookupComponent> updatableComponents = new ArrayList();

	private PamTableItem topicItem, codeItem, textItem, 
	selectableItem, borderColourItem, fillcolourItem, orderItem, symbolItem;

	private LookUpTables() {
		lutTableDef = new EmptyTableDefinition("Lookup");
		lutTableDef.addTableItem(topicItem = new PamTableItem("Topic", Types.CHAR, TOPIC_LENGTH));
		lutTableDef.addTableItem(orderItem = new PamTableItem("DisplayOrder", Types.INTEGER));
		lutTableDef.addTableItem(codeItem = new PamTableItem("Code", Types.CHAR, CODE_LENGTH));
		lutTableDef.addTableItem(textItem = new PamTableItem("ItemText", Types.CHAR, TEXT_LENGTH));
		selectableItem = new PamTableItem("isSelectable", Types.BIT);
		lutTableDef.addTableItem(selectableItem);
		lutTableDef.addTableItem(fillcolourItem = new PamTableItem("FillColour", Types.CHAR, 20));
		lutTableDef.addTableItem(borderColourItem = new PamTableItem("BorderColour", Types.CHAR, 20));
		lutTableDef.addTableItem(symbolItem = new PamTableItem("Symbol", Types.CHAR, 2));

		lutWarning = new PamWarning("Database Lookup Tables", "LUT Error", 0);
		checkTable();
	}

	/**
	 * Access the LookUpTables class
	 * @return reference to a single instance of the Look up table manager. 
	 */
	synchronized public static LookUpTables getLookUpTables() {
		if (singleInstance == null) {
			singleInstance = new LookUpTables();
		}
		return singleInstance;
	}


	private PamConnection checkedTableConnection;
	/**
	 * Check the database module is present and that 
	 * the lookup table exists. 
	 * @return true if the table exists and is correctly formatted with all the 
	 * right columns. 
	 */
	public boolean checkTable() {
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit == null) {
			return false;
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		if (con == checkedTableConnection) {
			return true;
		}
		checkedTableConnection = null;
		if (dbControlUnit.getDbProcess().checkTable(lutTableDef)) {
			checkedTableConnection = con;
		}
		
		checkTableRepeats(con, lutTableDef);
		
		return checkedTableConnection != null;
	}

	/**
	 * for some reason some repeats have got into the LUT and need to be removed
	 * or it really messes stuff up. So check it automatically. 
	 * @param con
	 * @param lutTableDef2
	 */
	private boolean checkTableRepeats(PamConnection con, EmptyTableDefinition lutTableDef) {
		/*
		 * first get a list of unique topics, then check them one at a time. 
		 */
		ArrayList<String> topics = getTopicList(con, lutTableDef);
		if (topics == null) {
			return false;
		}
		
		for (String topic : topics) {
			checkTopicRepeats(con, lutTableDef, topic);
		}
		return true;
	}
	
	private void checkTopicRepeats(PamConnection con, EmptyTableDefinition lutTableDef2, String topic) {
		LookupList lutList = getLookupList(topic);
		Vector<LookupItem> list = lutList.getList();
		int n = list.size();
		boolean[] isRepeat = new boolean[n];
		int nRepeat = 0;
		// search for repeats.
		for (int i = 0; i < n-1; i++) {
			String code = list.get(i).getCode().trim();
			String topic1 = list.get(i).getTopic().trim();
			for (int j = i+1; j < n; j++) {
				String code2 = list.get(j).getCode().trim();
				String topic2 = list.get(j).getTopic();
				if (topic2 != null) {
				topic2 = topic2.trim();
				if (code.equals(code2) && topic1.equals(topic2)) {
					isRepeat[j] = true;
					nRepeat++;
				}
				}
			}
		}
		if (nRepeat == 0) {
			return;
		}
		// make a clause to delete the repeats. 
		String sql = null;
		for (int i = 0; i < n; i++) {
			if (isRepeat[i] == false) {
				continue;
			}
			if (sql == null) {
				sql = String.format("DELETE FROM %s WHERE Id IN (%d", lutTableDef.getTableName(), list.get(i).getDatabaseId());
			}
			else {
				sql = sql + String.format(",%d", list.get(i).getDatabaseId());
			}
		}
		sql += ")";
		boolean ok = false;
		try {
			Statement stmt = con.getConnection().createStatement();
			ok = stmt.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> getTopicList(PamConnection con, EmptyTableDefinition lutTableDef) {
		if (con == null) {
			return null;
		}
		ArrayList<String> topics = new ArrayList<>();
		String qStr = "SELECT DISTINCT Topic FROM  " + lutTableDef.getTableName() + " WHERE TOPIC IS NOT NULL";
		try {
			Statement stmt = con.getConnection().createStatement();
			boolean ok = stmt.execute(qStr);
			if (ok == false) {
				return null;
			}
			ResultSet results = stmt.getResultSet();
			while (results.next()) {
				String topic = results.getString(1);
				topics.add(topic);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return topics;
	}

	public LookupList createLookupList(PamCursor resultSet, String topic) {
		LookupList lookupList = new LookupList(topic);
		LookupItem lutItem;
		try {
			while (resultSet.next()) {
				for (int i = 0; i < lutTableDef.getTableItemCount(); i++) {
					lutTableDef.getTableItem(i).setValue(resultSet.getObject(i+1));
				}
				lookupList.addItem(lutItem = new LookupItem(lutTableDef.getIndexItem().getIntegerValue(),
						resultSet.getRow(),
						topicItem.getDeblankedStringValue(), orderItem.getIntegerValue(), codeItem.getDeblankedStringValue(),
						textItem.getDeblankedStringValue(), selectableItem.getBooleanValue(),
						getColour(fillcolourItem.getDeblankedStringValue()),
						getColour(borderColourItem.getDeblankedStringValue()),
						symbolItem.getDeblankedStringValue()));
				//				System.out.println("createLookupList New Item " + lutItem);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return lookupList;
	}

	/**
	 * <p>Adds the passed list to the end of the Lookup table in the current database.</p>
	 * <p>This method DOES NOT CHECK if the list topic already exists in the table, and
	 * try to merge the rows.  It simply adds the list to the end of the table.</p>
	 * 
	 * @param newList
	 * @return
	 */
	public boolean addListToDB(LookupList newList) {
		DBControlUnit dbControlUnit = DBControlUnit.findDatabaseControl();
		if (dbControlUnit == null) {	// if there is no database, exit immediately
			return false;
		}

		// if this table already exists in the database, warn the user that it's about to
		// be modified and give them the option of skipping
		boolean exists = dbControlUnit.getDbProcess().tableExists(lutTableDef);
		if (exists) {
			String title = "Modify existing Lookup table";
			String msg = "PAMGuard is about to modify the Lookup table in the database.  It will be adding new rows to the end of the table, possibly " +
					"duplicating existing topics if you are switching from a database that already had Lookup information.<br><br>  " +
					"If this is correct, press Ok.  If you do not want to change the database table, press Cancel.<br><br>";
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans==WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}

		// copy the information to the end of the table.  If the table doesn't exist, create it first
		checkTable(); 
		PamCursor cursor = createPamCursor(newList.getListTopic());
		if (cursor == null) {
			System.out.println("Unable to access database lookup table");
			return false;
		}

		ListIterator<LookupItem> lutList = newList.getList().listIterator();
		while (lutList.hasNext()) {
			LookupItem item = lutList.next();
			cursor.moveToInsertRow();
			setTableData(cursor, item);
			cursor.insertRow(false);
		}
		cursor.updateDatabase();
		return true;
	}

	/**
	 * write back to database
	 * @param newList
	 * @param resultSet
	 * @return
	 */
	private boolean reWriteList(LookupList newList, PamCursor resultSet) {
		//		newList.sortItemsById();
		LookupItem item;
		//		Connection con = DBControlUnit.findConnection();
		ListIterator<LookupItem> lutList = newList.getList().listIterator();
		while (lutList.hasNext()) {
			item = lutList.next();
			//			setTableData(item);
			if (item.getResultSetRow() == 0) {
				resultSet.moveToInsertRow();
				setTableData(resultSet, item);
				//					resultSet.updateRow();
				resultSet.insertRow(false);
			}
			else {
				try {
					boolean b = resultSet.absolute(item.getResultSetRow());
					if (b) {
						setTableData(resultSet, item);
						resultSet.updateRow();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		// now check for deleted items. 
		if (newList.getDeletedItems() != null) {
			lutList = newList.getDeletedItems().listIterator();
			while (lutList.hasNext()) {
				item = lutList.next();
				/**
				 * System below doesn't seem to be deleting properly from sqlite, so 
				 * do a more specific delete scheme ...
				 */
				if (item.getResultSetRow() > 0 && item.getDatabaseId() > 0) {
					resultSet.absolute(item.getResultSetRow());
					resultSet.deleteRow();
				}
			}
		}
		resultSet.updateDatabase();
		DBControlUnit.findDatabaseControl().commitChanges();

		return true;
	}


	/**
	 * Set data in the result set. 
	 * It's slightly easier for this to be done by copying the data back into 
	 * the table items since they are accessible by named objects, these 
	 * then get copied in the correct order into the PamCursor.<p>
	 * they could be copied straight into the cursor, but this would have to
	 * be done in the exact right order. 
	 * @param resultSet Pamguard cursor object
	 * @param lookupItem lookup item
	 */
	private void setTableData(PamCursor resultSet, LookupItem lookupItem) {
		if (lookupItem.getDatabaseId() <= 0) {
			lutTableDef.getIndexItem().setValue(null);
		}
		else {
			lutTableDef.getIndexItem().setValue(lookupItem.getDatabaseId());
		}
		topicItem.setValue(lookupItem.getTopic());
		codeItem.setValue(lookupItem.getCode());
		textItem.setValue(lookupItem.getText());
		selectableItem.setValue(lookupItem.isSelectable() ? 1 : 0);
		borderColourItem.setValue(getColourString(lookupItem.getBorderColour()));
		fillcolourItem.setValue(getColourString(lookupItem.getFillColour()));
		orderItem.setValue(lookupItem.getOrder());
		symbolItem.setValue(lookupItem.getSymbolType());
		try {
			for (int i = 0; i < lutTableDef.getTableItemCount(); i++) {
				resultSet.updateObject(i+1, lutTableDef.getTableItem(i).getValue());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Interpret colour strings from the lookup table
	 * @param deblankedStringValue
	 * @return a color, or null if the string cannot be interpreted.
	 */
	private Color getColour(String colString) {
		if (colString == null) {
			return null;
		}
		if (colString.equalsIgnoreCase("red")) {
			return Color.red;
		}else if (colString.equalsIgnoreCase("green")) {
			return Color.green;
		}else if (colString.equalsIgnoreCase("blue")) {
			return Color.blue;
		}else if (colString.equalsIgnoreCase("white")) {
			return Color.white;
		}else if (colString.equalsIgnoreCase("black")) {
			return Color.black;
		}else if (colString.equalsIgnoreCase("gray")) {
			return Color.gray;
		}else if (colString.equalsIgnoreCase("darkgray")) {
			return Color.darkGray;
		}else if (colString.equalsIgnoreCase("orange")) {
			return Color.orange;
		}else if (colString.equalsIgnoreCase("cyan")) {
			return Color.cyan;
		}else if (colString.equalsIgnoreCase("magenta")) {
			return Color.magenta;
		}else if (colString.equalsIgnoreCase("lightgray")) {
			return Color.lightGray;
		}else if (colString.equalsIgnoreCase("pink")) {
			return Color.pink;
		}else if (colString.equalsIgnoreCase("yellow")) {
			return Color.yellow;
		}
		else if (colString.substring(0, 3).equalsIgnoreCase("RGB")) {
			int[] rgb = new int[3];
			int[] divPos = new int[4];
			divPos[0] = colString.indexOf('(');
			divPos[1] = colString.indexOf(',');
			if (divPos[1] < 0) {
				return null;
			}
			divPos[2] = colString.indexOf( ',', divPos[1]+1);
			if (divPos[2] < 0) {
				return null;
			}
			divPos[3] = colString.indexOf(')');
			if (divPos[3] < 0) {
				return null;
			}
			try {
				for (int i = 0; i < 3; i++) {
					rgb[i] = Integer.valueOf(colString.substring(divPos[i]+1, divPos[i+1]));
				}
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
			return new Color(rgb[0], rgb[1], rgb[2]);
		}
		return null;
	}

	/**
	 * Convert a colour into a string that can be written to the table
	 * @param colour Colour
	 * @return String representation in the format RGB(%d,%d,%d).
	 */
	private String getColourString(Color colour) {
		if (colour == null) {
			return null;
		}
		return String.format("RGB(%d,%d,%d)", colour.getRed(), colour.getGreen(), colour.getBlue());
	}

	/**
	 * Query all LUT items with the given topic name. 
	 * display these in a table / list (which might be empty)
	 * and provide facilities for the user to add to and remove 
	 * items from this list
	 * @param window 
	 * @param topic LUT topic
	 * @return a new list, or null if no new list created.
	 */
	public LookupList editLookupTopic(Window window, String topic) {

		PamCursor cursor = createPamCursor(topic);
		if (cursor == null) {
			System.out.println("Unable to access database lookup table");
			return null;
		}
		LookupList lookupList = createLookupList(cursor, topic);
		//		lookupList.sortItemsByOrder();
		LookupList newList = LookupEditDialog.showDialog(window, lookupList);
		if (newList != null) {
			reWriteList(newList, cursor);
			updateComponents(topic);
			return newList;
		}
		return null;
	}

	public LookupList getLookupList(String topic) {
		PamCursor cursor = createPamCursor(topic); 
		if (cursor == null) {
			//			System.out.println("Unable to access database lookup table for topic " + topic);
			if (topic == null) {
				topic = "all row";
			}
			lutWarning.setWarningMessage("Can't read " + topic + "'s from database lookup table");
			lutWarning.setWarnignLevel(1);
			WarningSystem.getWarningSystem().addWarning(lutWarning);
			return null;
		}
		if (lutWarning.getWarnignLevel() > 0) {
			// ok if it gets here so cancel the warning
			lutWarning.setWarnignLevel(0);
			WarningSystem.getWarningSystem().removeWarning(lutWarning);
		}
		LookupList lookupList = createLookupList(cursor, topic);
		cursor.closeScrollableCursor();
		return lookupList;
	}

	/**
	 * create a PamCursor for the topic specified.  Note that if the topic is
	 * null, the PamCursor will access all rows of the Lookup table
	 * 
	 * @param topic
	 * @return
	 */
	private PamCursor createPamCursor(String topic) {
		PamCursor c = PamCursorManager.createCursor(lutTableDef);
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return null;
		}
		if (topic == null) {
			c.openScrollableCursor(con, true, true, "ORDER BY DisplayOrder");
		}
		else {
			c.openScrollableCursor(con, true, true, String.format("WHERE RTRIM(%s) = '%s' ORDER BY DisplayOrder", "Topic", topic));
		}

		return c;
	}
	
	/**
	 * Add a lookup component which can be updated automatically should there be any changes to 
	 * the components topic.  
	 * @param lookupComponent
	 */
	public void addUpdatableComponent(LookupComponent lookupComponent) {
		updatableComponents.add(lookupComponent);
	}
	/**
	 * Remove a lookup component which can be updated automatically should there be any changes to 
	 * the components topic.  
	 * @param lookupComponent
	 * @return true if the component existed in the update list. 
	 */
	public boolean removeUpdatableComponent(LookupComponent lookupComponent) {
		return updatableComponents.remove(lookupComponent);
	}
	
	/**
	 * Tell all lookup components with the given topic to update their lists, restoring 
	 * currently selected values if possible. 
	 * @param lookupTopic
	 */
	public void updateComponents(String lookupTopic) {
		for (LookupComponent lc:updatableComponents) {
			LookupList luList = lc.getLookupList();
			if (luList == null) {
				continue;
			}
			String topic = luList.getListTopic();
			if (topic.equalsIgnoreCase(lookupTopic) == false) {
				continue;
			}
			LookupItem currVal = lc.getSelectedItem();
			lc.setLookupList(getLookupList(lookupTopic));
			if (currVal != null) {
				lc.setSelectedCode(currVal.getCode());
			}
		}
	}

}
