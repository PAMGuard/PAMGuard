package generalDatabase.dataExport;

import generalDatabase.SQLTypes;

import java.awt.event.MouseEvent;

public abstract class DataFilter {
	
	private DataFilterChangeListener dataFilterChangeListener;

	/**
	 * @param dataFilterChangeListener
	 */
	public DataFilter(DataFilterChangeListener dataFilterChangeListener) {
		super();
		this.dataFilterChangeListener = dataFilterChangeListener;
	}
	
	/**
	 * Should be called by any filter whenever it's data have changed 
	 * so that any necessary actions can be taken. 
	 */
	public void filterChanged() {
		dataFilterChangeListener.filterChanged(this);
	}

	/**
	 * Display some kind of action which will enable users to set
	 * the filter - be it a drop down menu, a dialog, etc. 
	 * @return true if filter settings are changed.
	 */
	public abstract boolean filterSelectAction(MouseEvent e);
	
	/**
	 * Get the column name associated with this filter. 
	 * @return the column name associated with this filter. 
	 */
	public abstract String getColumnName();
	
	/**
	 * Get a filter clause which can be incorporated into an SQL string
	 * @return SQL clause (without the WHERE). 
	 */
	public abstract String getFilterClause(SQLTypes sqlTypes);
	
}
