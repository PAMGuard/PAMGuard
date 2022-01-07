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

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;

public class ScrollablePamCursor extends PamCursor {

	private ResultSet scrollableCursor;

	public ScrollablePamCursor(EmptyTableDefinition tableDefinition) {
		super(tableDefinition);
	}


	@Override
	public boolean openScrollableCursor(PamConnection connection, 
			boolean includeKeys, boolean includeCounters, String clause) {
		setCurrentConnection(connection);
		String sqlString = this.getSelectString(connection.getSqlTypes(),  includeKeys, includeCounters, clause);
		try {
			/*
			 * Probably need to leave this one as SCROLL_INSENSITIVE ? 
			 */
			Statement stmt = connection.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			scrollableCursor = stmt.executeQuery(sqlString);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean closeScrollableCursor() {
		if (scrollableCursor == null) {
			return true;
		}
		try {
			scrollableCursor.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		scrollableCursor = null;
		return true;
	}

	@Override
	int getLastDatabaseIndex(PamConnection connection,
			PreparedStatement preparedInsertStatement) {	
		int ans = -1;
		if (connection == null) {
			return -1;
		}
		
		String indexName = getTableDefinition().getIndexItem().getName();
//		System.out.println(indexName);
		
//		String qStr = "SELECT "/*@@IDENTITY*/+indexName+" FROM " + getTableDefinition().getTableName();
		String qStr = String.format("SELECT %s FROM %s ORDER BY %s DESC", 
				indexName, getTableDefinition().getTableName(), indexName);
		
		try {
			Statement s = connection.getConnection().createStatement();
			ResultSet rs = s.executeQuery(qStr);
			rs.next();
			ans = rs.getInt(1);
			
//			System.out.println(ans);
			rs.close();
			s.close();
		}
		catch (SQLException e) {
			System.out.println("Unable to extract last Id code using" + qStr);
			return -1;
		}
		return ans;
	}


	@Override
	public boolean updateDatabase() {
		/*
		 * Nothing to do, since this database will be up to date. 
		 */
		return true;
	}

	@Override
	public boolean absolute(int row) {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			scrollableCursor.absolute(row);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	@Override
	public void afterLast() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.afterLast();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		return;

	}


	@Override
	public void beforeFirst() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.beforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		return;

	}

	@Override
	public void close() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.close();
			scrollableCursor = null;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		return;

	}


	@Override
	public void deleteRow() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.deleteRow();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		return;
	}


	@Override
	public int findColumn(String columnLabel) {
		if (scrollableCursor == null) {
			return -1;
		}
		try {
			return scrollableCursor.findColumn(columnLabel);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}


	@Override
	public boolean first() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public int getRow() throws SQLException {
		return scrollableCursor.getRow();
	}


	@Override
	public Object getObject(int columnIndex) {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.getObject(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean getBoolean(int columnIndex) {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.getBoolean(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public byte getByte(int columnIndex) {
		if (scrollableCursor == null) {
			return Byte.MIN_VALUE;
		}
		try {
			return scrollableCursor.getByte(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return Byte.MIN_VALUE;
		}
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) {
		if (scrollableCursor == null) {
			return null;
		}
		try {
			return scrollableCursor.getDate(columnIndex, cal);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public Date getDate(int columnIndex) {
		if (scrollableCursor == null) {
			return null;
		}
		try {
			return scrollableCursor.getDate(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public double getDouble(int columnIndex) {
		if (scrollableCursor == null) {
			return Double.NaN;
		}
		try {
			return scrollableCursor.getDouble(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return Double.NaN;
		}
	}

	@Override
	public float getFloat(int columnIndex) {
		if (scrollableCursor == null) {
			return Float.NaN;
		}
		try {
			return scrollableCursor.getFloat(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return Float.NaN;
		}
	}

	@Override
	public int getInt(int columnIndex) {
		if (scrollableCursor == null) {
			return Integer.MIN_VALUE;
		}
		try {
			return scrollableCursor.getInt(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return Integer.MIN_VALUE;
		}
	}

	@Override
	public long getLong(int columnIndex) {
		if (scrollableCursor == null) {
			return Long.MIN_VALUE;
		}
		try {
			return scrollableCursor.getLong(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return Long.MIN_VALUE;
		}
	}


	@Override
	public RowId getRowId(int columnIndex) {
		if (scrollableCursor == null) {
			return null;
		}
		try {
			return scrollableCursor.getRowId(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getString(int columnIndex) {
		if (scrollableCursor == null) {
			return null;
		}
		try {
			return scrollableCursor.getString(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public Time getTime(int columnIndex){ 
		if (scrollableCursor == null) {
			return null;
		}
		try {
			return scrollableCursor.getTime(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Long getTimestampMillis(int columnIndex){
		if (scrollableCursor == null) {
			return null;
		}
		Object o;
		try {
			o = scrollableCursor.getObject(columnIndex);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return SQLTypes.millisFromTimeStamp(o);
//		try {
//			return scrollableCursor.getTimestamp(columnIndex);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return null;
//		}
	}

	@Override
	public boolean isAfterLast() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.isAfterLast();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isBeforeFirst() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isClosed() {
		if (scrollableCursor == null) {
			return true;
		}
		try {
			return scrollableCursor.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	public boolean isFirst() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.isFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public boolean isLast() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.isLast();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean last(){
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.last();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void moveToCurrentRow() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.moveToCurrentRow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moveToInsertRow() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.moveToInsertRow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int insertRow(boolean getIndex) {
		if (scrollableCursor == null) {
			return -1;
		}
		try {
			scrollableCursor.insertRow();
			if (getIndex) {
				return getLastDatabaseIndex(getCurrentConnection(), null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}


	@Override
	public boolean next() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean previous() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.previous();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void refreshRow() {
		if (scrollableCursor == null) {
			return;
		}
		try {
			scrollableCursor.refreshRow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean rowDeleted() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.rowDeleted();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public boolean rowInserted() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.rowInserted();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean rowUpdated() {
		if (scrollableCursor == null) {
			return false;
		}
		try {
			return scrollableCursor.rowUpdated();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateObject(columnIndex, x);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateBoolean(columnIndex, x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateByte(columnIndex, x);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateInt(columnIndex, x);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateLong(columnIndex, x);
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateNull(columnIndex);
	}


	@Override
	public void updateRow() throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateRow();
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateString(columnIndex, x);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateShort(columnIndex, x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x)
	throws SQLException {
		if (scrollableCursor == null) {
			return;
		}
		scrollableCursor.updateTimestamp(columnIndex, x);
	}


}
