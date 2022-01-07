package generalDatabase.pamCursor;

import java.sql.Connection;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;

public class PamCursorManager {

	/**
	 * Underlying database does not support updateable cursors, so 
	 * the functionality is implemented using more simple 
	 * SQL update statements
	 */
	static public final int NON_SCROLLABLE = 1;
	/**
	 * The underlying database does support updateable cursors, 
	 * so most functionality can be simply passed straight through 
	 * to the underlying database.  
	 */
	static public final int SCROLLABLE = 2;
	
	private static int cursorType = NON_SCROLLABLE;

	/**
	 * Set the cursor type, SCROLLABLE or NON_SCROLLABLE
	 * @param cursorType cursor type
	 */
	public static void setCursorType(int cursorType) {
		PamCursorManager.cursorType = cursorType;
	}

	/**
	 * 
	 * @return the cursor type of the underlying database
	 */
	public static int getCursorType() {
		return cursorType;
	}
	
	/**
	 * Create a cursor using the connection to the main database
	 * @param tableDefinition table definition for cursor
	 * @return PamCursor object.
	 */
	public static PamCursor createCursor(EmptyTableDefinition tableDefinition) {
		PamConnection con = DBControlUnit.findConnection();
//		if (con == null) {
//			return null;
//		}
		return createCursor(con, tableDefinition);
	}
	
	/**
	 * Create a cursor using any connection to a database. 
	 * @param connection database connection
	 * @param tableDefinition table definition for cursor
	 * @return PamCursor object
	 */
	public static PamCursor createCursor(PamConnection connection, EmptyTableDefinition tableDefinition) {
		PamCursor cursor = null;
		switch(cursorType) {
		case NON_SCROLLABLE:
			cursor = new NonScrollablePamCursor(tableDefinition);
			break;
		case SCROLLABLE:
			cursor = new ScrollablePamCursor(tableDefinition);
			break;
		}
//		if (cursor != null) {
//			if (connection != null) {
//				cursor.setCurrentConnection(connection);
//			}
//			if (tableDefinition != null) {
//				cursor.setTableDefinition(tableDefinition);
//			}
//		}
		return cursor;
	}
}
