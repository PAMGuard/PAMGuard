package generalDatabase.pamCursor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ListIterator;

import PamView.dialog.warn.WarnOnce;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

/**
 * A wrapper around non scrollable cursors to make them behave in the same
 * way as a scrollable cursor. 
 * <p>
 * When the cursor is opened, all data are fetched into memory buffers and
 * the statement used to fetch the data is closed.
 * <p> 
 * The data consist of arrays of type Object. These can be fetched and scrolled
 * through as though we were using a scrollable cursor working on the database. 
 * <p>
 * Whenever a row is updated, a simpel query which updates that single row 
 * get's called. 
 * @author Doug
 *
 */
public class NonScrollablePamCursor extends PamCursor {

	/**
	 * linked list of data rows. 
	 */
	private LinkedList<PamDataRow> dataRows;

	/**
	 * Iterator over all items in dataRows;
	 */
	private ListIterator<PamDataRow> dataIterator;

	/**
	 * total number of rows read in
	 */
	private int nRows;

	/**
	 * current row position, zero indexed.<p>
	 * N.B. The commands coming in to things like absolute
	 * will be using 1 indexed (SQL standard) row numbers.
	 */
	private int currentRow;

	/**
	 * reference to current data row - will have to be 
	 * updated every time the iterator is moved 
	 */
	private PamDataRow currentData;
	/**
	 * ACtions such as delete and update take place
	 * immediately. If not, then they all occurr 
	 * in a final call to ??? later on (which may be quicker)
	 */
	private boolean immediate = false;

	private int oldCurrentRow;

	private int nCol;

	private PamDataRow insertRowData;

	public NonScrollablePamCursor(EmptyTableDefinition tableDefinition) {
		super(tableDefinition);
		if (tableDefinition == null) {
			return;
		}
		dataRows = new LinkedList<PamDataRow>();
		nCol = tableDefinition.getTableItemCount();
	}
//
//	@Override
//	public void setTableDefinition(EmptyTableDefinition tableDefinition) {
//		super.setTableDefinition(tableDefinition);
//	}

	@Override
	public boolean openScrollableCursor(PamConnection connection, boolean includeKeys,
			boolean includeCounters, String clause) {
		setCurrentConnection(connection);
		String sqlString = this.getSelectString(connection.getSqlTypes(), includeKeys, includeCounters, clause);
		PamDataRow newDataRow;
		ResultSet cursor;
		if (connection == null) {
			return false;
		}
//		int iRow = 0;
		try {
			Statement stmt = connection.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY);
			cursor = stmt.executeQuery(sqlString);

			dataRows.clear();

			while (cursor.next()) {
				newDataRow = new PamDataRow(cursor.getInt(1), nCol);
				newDataRow.data[0] = newDataRow.iD;
				for (int i = 1; i < nCol; i++) {
					newDataRow.data[i] = cursor.getObject(i+1);
//					System.out.println(String.format("Row %d, Column %d, value = ", iRow, i) + newDataRow.data[i]);
				}
				dataRows.add(newDataRow);
//				iRow++;
			}
			nRows = dataRows.size();
			currentRow = -1;
			beforeFirst();

			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in NonScrollablePamCursor.openScrollableCursor: " + sqlString);
			e.printStackTrace();
			String title = "Error Accessing Database Table";
			String msg = "Pamguard is not able to access the database table " + getTableDefinition().getTableName() +
					" using the following SQL command:<br><br>" +
					sqlString + ".<br><br>" +
					"This may be caused by an issue with the database (it does not exist, is corrupt, or is currently " +
					"locked by another process) or an issue with the specific table (it does not exist or is corrupt).<p>" +
					"Depending on the exact nature of the problem, PAMGuard may or may not be able to recover on its own.  It is " +
					"recommended that you check the database for problems, especially if this warning reoccurs.";
			String help = null;
			int ans = WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE, help, e);
			return false;
		}
		return true;
	}

	@Override
	int getLastDatabaseIndex(PamConnection connection,
			PreparedStatement preparedInsertStatement) {

		if (connection == null) {
			return -1;
		}
		
		
		// tic2 = SystemTiming.getProcessCPUTime();
		// use scrollable cursor to get the last result out - shouldn't be
		// too slow I hope
		String indexName = getTableDefinition().getIndexItem().getName();
		String qStr = String.format("SELECT %s FROM %s ORDER BY %s DESC", 
				indexName, getTableDefinition().getTableName(), indexName);

		try {
			Statement  stmt = connection.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(qStr);
			rs.next();
			int ind = rs.getInt(1);
			stmt.close();
			return ind;
		}
		catch (SQLException e) {
			System.out.println("Unable to extract last Id code using: " + qStr);
			return -1;
		}
		
	}

	@Override
	public boolean closeScrollableCursor() {
		//		if (cursor == null) {
		//			return true;
		//		}
		//		try {
		//			cursor.close();
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//			return false;
		//		}
		//		cursor = null;
		return true;
	}


	@Override
	public boolean updateDatabase() {
		if (!deleteUnwanted()) {
			return false;
		}
		if (!updateExisting()) {
			return false;
		}
		if (!addNew()) {
			return false;
		}
		return true;
	}

	/**
	 * Update existing data in the database, i.e. data where
	 * the current Id is > 0 and the data row is flagged
	 * as updated. 
	 */
	private boolean updateExisting() {

		PamConnection connection = getCurrentConnection();

		String updateString = getUpdateString(connection.getSqlTypes());

		int iCol;
		int iUsedCol = 0;
		Object obj;
		PamTableItem tableItem;
		try {
			PreparedStatement stmt = connection.getConnection().prepareStatement(updateString);
			beforeFirst();
			while (next()) {
				if (currentData.updated == false) {
					continue;
				}
				if (currentData.iD < 0) {
					continue;
				}
				iUsedCol = 0;
				for (iCol = 0; iCol < nCol; iCol++) {
					tableItem = getTableDefinition().getTableItem(iCol);
					if (tableItem.isCounter()) {
						continue;
					}
					if (tableItem.isPrimaryKey()) {
						continue;
					}
					// crashing here if set null data !!!
					// seems you have to use setNull instead. 
					obj = getObject(iCol+1);
					if (obj == null) {
						stmt.setNull(++iUsedCol, tableItem.getSqlType());
					}
					else {
						stmt.setObject(++iUsedCol, getObject(iCol+1));
					}
				}
				// crashing here if set null data !!!
				stmt.setInt(++iUsedCol, currentData.iD);
				stmt.execute();
				currentData.updated = false;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Add new data with a single prepared statement. 
	 * @return true if Ok. 
	 */
	private boolean addNew() {
		PamConnection connection = getCurrentConnection();
		if (connection == null) {
			return false;
		}
		String insertString = getInsertString(connection.getSqlTypes());

		int iCol;
		int iUsedCol = 0;
		boolean ans;
		PamTableItem tableItem;
		Object obj;
		try {
			PreparedStatement stmt = connection.getConnection().prepareStatement(insertString);
			beforeFirst();
			while (next()) {
				if (currentData.iD >= 0) {
					continue;
				}
				iUsedCol = 0;
				for (iCol = 0; iCol < nCol; iCol++) {
					tableItem = getTableDefinition().getTableItem(iCol);
					if (tableItem.isCounter()) {
						continue;
					}
					if (tableItem.isPrimaryKey()) {
						continue;
					}
					obj = getObject(iCol+1);
					if (obj == null) {
						stmt.setNull(++iUsedCol, tableItem.getSqlType());
					}
					else {
						stmt.setObject(++iUsedCol, getObject(iCol+1));
					}
				}
				//				stmt.setInt(nCol, currentData.iD);
				ans = stmt.execute();
//				System.out.println("Database insert result = " + ans);
				if (ans == false) {
					
				}
				currentData.updated = false;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Delete unwanted data with a single statement
	 * for all unwanted ID's. 
	 * @return true if Ok. 
	 */
	private boolean deleteUnwanted() {
		PamConnection connection = getCurrentConnection();
		if (connection == null) {
			return false;
		}

		String delString = String.format("DELETE FROM %s WHERE Id IN (", 
				getTableDefinition().getTableName());
		int nDel = 0;
		beforeFirst();
		while (next()) {
			if (currentData.deleted == false) {
				continue;
			}
			if (currentData.iD < 0) {
				continue; // no need to delete - it's not saved yet !
			}
			if (nDel++ > 0) {
				delString += ", ";
			}
			delString += currentData.iD;
		}
		if (nDel == 0) {
			return true;
		}
		delString += ")";
		try {
			PreparedStatement pStmt = connection.getConnection().prepareStatement(delString);
			pStmt.execute();
			pStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean absolute(int row) {
		oldCurrentRow = -1;
		currentRow = row-1;
		if (row > 0) {
			dataIterator = dataRows.listIterator(row-1);
			return next();
		}
		else if (row < 0) {
			dataIterator = dataRows.listIterator(nRows+row);
			return previous();
		}
		return false;
	}

	@Override
	public void beforeFirst() {
		dataIterator = dataRows.listIterator(0);
		currentRow = -1;
		currentData = null;
		oldCurrentRow = -1;
	}

	@Override
	public void afterLast() {
		if (nRows == 0) {
			beforeFirst();
			return;
		}
		currentRow = nRows;
		currentData = null;
		dataIterator = null;
		oldCurrentRow = 1;
		dataIterator = dataRows.listIterator(nRows-1);
		if (dataIterator.hasNext()) {
			dataIterator.next();
		}
	}

	@Override
	public boolean first() {
		oldCurrentRow = -1;
		dataIterator = dataRows.listIterator(0);
		if (dataIterator.hasNext() == false) {
			currentData = null;
			return false;
		}
		currentData = dataIterator.next();
		currentRow = 0;
		return true;
	}

	@Override
	public boolean last() {
		oldCurrentRow = -1;
		dataIterator = dataRows.listIterator(nRows-1);
		if (dataIterator.hasNext() == false) {
			currentData = null;
			return false;
		}
		currentData = dataIterator.next();
		currentRow = nRows-1;
		return true;
	}

	@Override
	public boolean next() {
		oldCurrentRow = -1;
		if (dataIterator == null) {
			if (currentRow < 0) {
				return first();
			}
			else {
				return false;
			}
		}
		if (dataIterator.hasNext() == false) {
			return false;
		}
		currentData = dataIterator.next();
		currentRow++;
		return true;
	}

	@Override
	public boolean previous() {
		oldCurrentRow = -1;
		if (dataIterator == null) {
			if (currentRow >= nRows) {
				return last();
			}
			else {
				return false;
			}
		}
		if (dataIterator.hasPrevious() == false) {
			return false;
		}
		currentData = dataIterator.previous();
		currentRow--;
		return true;
	}

	/**
	 * checks the current row is valid, 
	 * i.e. currentRow >= 0 && currentRow < nRows
	 */
	private boolean validRow() {
		return (currentRow > 0 && currentRow <= nRows);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteRow() {
		if (!validRow() || currentData == null) {
			return;
		}
		currentData.deleted = true;
		if (immediate) {
			// TODO need to implement immediate SQL delete here. 
		}
	}

	@Override
	public int findColumn(String columnLabel) {
		for (int i = 0; i < getTableDefinition().getTableItemCount(); i++) {
			if (getTableDefinition().getTableItem(i).getName().equalsIgnoreCase(columnLabel)) {
				return i+1; //  need to change this to i+1 for compatibility with real SQL !
			}
		}
		return -1;
	}

	@Override
	public int getRow() throws SQLException {
		return currentRow+1;
	}

	@Override
	public Object getObject(int columnIndex) {
		return currentData.data[columnIndex-1];
	}

	@Override
	public boolean getBoolean(int columnIndex) {
		return (Boolean) currentData.data[columnIndex-1];
	}

	@Override
	public byte getByte(int columnIndex) {
		return (Byte) currentData.data[columnIndex-1];
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDouble(int columnIndex) {
		return (Double) currentData.data[columnIndex-1];
	}

	@Override
	public float getFloat(int columnIndex) {
		return (Float) currentData.data[columnIndex-1];
	}

	@Override
	public int getInt(int columnIndex) {
		return (Integer) currentData.data[columnIndex-1];
	}

	@Override
	public long getLong(int columnIndex) {
		return (Long) currentData.data[columnIndex-1];
	}
	
	@Override
	public RowId getRowId(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(int columnIndex) {
		return (String) currentData.data[columnIndex-1];
	}

	@Override
	public Time getTime(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getTimestampMillis(int columnIndex) {
		SQLTypes sqlTypes = getSqlTypes();
		if (sqlTypes == null) return null;
		return sqlTypes.millisFromTimeStamp(currentData.data[columnIndex-1]);
	}

	@Override
	public boolean isAfterLast() {
		return currentRow == nRows;
	}

	@Override
	public boolean isBeforeFirst() {
		return currentRow < 0;
	}

	@Override
	public boolean isClosed() {
		return dataRows == null;
	}

	@Override
	public boolean isFirst() {
		return currentRow == 0;
	}

	@Override
	public boolean isLast() {
		return currentRow == nRows-1;
	}

	@Override
	public void moveToCurrentRow() {
		if (oldCurrentRow < 0) {
			return;
		}
		dataIterator = dataRows.listIterator(oldCurrentRow);
		currentRow = oldCurrentRow;
		next();
	}

	@Override
	public void moveToInsertRow() {
		oldCurrentRow = currentRow;
		afterLast();
		insertRowData = currentData = new PamDataRow(-1, nCol);
	}

	@Override
	public int insertRow(boolean getIndex) {
		int newIndex = 0;
		if (insertRowData != currentData) {
			return -1;
		}
		afterLast(); // this set current data null, but doesn't matter 
		dataIterator.add(insertRowData);
		insertRowData.updated = true;
		if (getIndex) {
			newIndex = immediateInsert(getCurrentConnection());
			insertRowData.iD = newIndex;
			insertRowData.updated = false;
		}
		nRows++;
		currentRow = nRows;
		return newIndex;
	}

	@Override
	public void refreshRow() {
		/**
		 * Needs to re-get data from the database for that row
		 */
		// TODO Auto-generated method stub

	}

	@Override
	public boolean rowDeleted() {		
		return currentData.deleted;
	}

	@Override
	public boolean rowInserted() {
		return currentData.iD < 0;
	}

	@Override
	public boolean rowUpdated() {
		return currentData.updated;
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}


	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		currentData.data[columnIndex-1] = null;
	}


//	private void updateSingleRow(PamDataRow currentData2) {
//		Statement updateStatment = getUpdateStatement();
//
//	}

//	private Statement getUpdateStatement() {
//		String str = getUpdateString(getSQLTypes(connection));
//		return null;
//	}
	
	@Override
	public void updateRow() throws SQLException {
		if (immediate) {
//			updateSingleRow(currentData);

			System.out.println("Function NonScrollablePamCursor.updateRow() is not implemented. No data changed. Contact developer team.");
		}
		else {
			currentData.updated = true;
		}
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		currentData.data[columnIndex-1] = x;
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
	throws SQLException {
		// TODO Auto-generated method stub

	}


}
