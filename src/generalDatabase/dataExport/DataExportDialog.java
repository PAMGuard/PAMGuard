package generalDatabase.dataExport;

import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import PamView.dialog.PamDialog;

public class DataExportDialog extends PamDialog implements DataFilterChangeListener {

	private JTable dataTable;
	
	private JLabel filterClause;
	
	private EmptyTableDefinition tableDefinition;

	private ExportTableModel tableModel;
	
	private PamCursor tableCursor;

	private boolean cancelled;
	
//	private JButton filterSelectionButton;

	private int tableCursorRows;
	
	private DataExportSystem exportSystem;
	
	private ArrayList<PamTableItem> visibleTableItems = new ArrayList<PamTableItem>();
	
	private ArrayList<DataFilter> dataFilters = new ArrayList<DataFilter>();
	
	private PamConnection pamConnection;
	
	public DataExportDialog(Window parentFrame, EmptyTableDefinition tableDefinition, String title) {
		super(parentFrame, title, false);
		this.tableDefinition = tableDefinition;
		
		exportSystem = new FileExportsystem(this);
		pamConnection = DBControlUnit.findConnection();
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		for (int i = 0; i < tableDefinition.getTableItemCount();i++) {
			visibleTableItems.add(tableDefinition.getTableItem(i));
		}
		
		JScrollPane scrollPane = new JScrollPane(dataTable = new JTable(tableModel = new ExportTableModel()));
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//		dataTable.get
//		getButtonPanel().add(filterSelectionButton = new JButton("Data Filter(s)"));
		getOkButton().setText("Export");
		JTableHeader tableHeader = dataTable.getTableHeader();
		tableHeader.setReorderingAllowed(false);
		tableHeader.addMouseListener(new HeadingMouseAdapter());
		tableHeader.addMouseMotionListener(new HeadingMouseAdapter());
//		tableHeader.setr
//		filterSelectionButton.addActionListener(new FiltersButton());
		mainPanel.add(BorderLayout.SOUTH, filterClause = new JLabel("No filters"));
		
		mainPanel.setPreferredSize(new Dimension(1200, 300));
		setDialogComponent(mainPanel);
		setResizable(true);
	}
	
	public boolean showDialog() {
		cancelled = false;
		queryData();
		sayQueryClause();
		setVisible(true);
		
		return (cancelled == false);
	}
	
	/**
	 * Remove a specific column from the visible table item list. 
	 * @param columnName Name of the column to remove
	 * @return true if found and removed
	 */
	public boolean excludeColumn(String columnName) {
		PamTableItem tableItem = tableDefinition.findTableItem(columnName);
		return excludeColumn(tableItem);
	}
	/**
	 * Removes a specific table item from the visible list. 
	 * @param tableItem Table item to remove
	 * @return true if found and removed.
	 */
	public boolean excludeColumn(PamTableItem tableItem) {
		if (tableItem == null) {
			return false;
		}
		boolean removed = visibleTableItems.remove(tableItem);
		tableModel.fireTableStructureChanged();
		return removed;
	}
	
	public void addDataFilter(DataFilter dataFilter) {
		dataFilters.add(dataFilter);
//		filterSelectionButton.setEnabled(true);
	}
	
	/**
	 * Activate any filters associated with this control. 
	 * @param e
	 */
	public void headingMouseClick(MouseEvent e) {
		JTableHeader tableHeader = dataTable.getTableHeader();
		int iCol = tableHeader.columnAtPoint(e.getPoint());
//		TableColumn tableColumn = tableHeader.getColumnModel().getColumn(icol);
		DataFilter dataFilter = findDataFilter(tableModel.getColumnName(iCol));
		if (dataFilter != null) {
			dataFilter.filterSelectAction(e);
		}
	}
	
	/**
	 * Display appropriate tooltip text depending on the column. 
	 * @param e
	 */
	private void headingMouseMoved(MouseEvent e) {
		JTableHeader tableHeader = dataTable.getTableHeader();
		int iCol = tableHeader.columnAtPoint(e.getPoint());
//		TableColumn tableColumn = tableHeader.getColumnModel().getColumn(icol);
		DataFilter dataFilter = findDataFilter(tableModel.getColumnName(iCol));
		if (pamConnection == null) {
			System.out.println("No database connection");
			return;
		}
		if (dataFilter != null) {
			String colQuery = dataFilter.getFilterClause(pamConnection.getSqlTypes());
			if (colQuery == null || colQuery.length() == 0) {
				tableHeader.setToolTipText(" click to activate a data filter on this column");
			}
			else {
				tableHeader.setToolTipText(colQuery + " (click to change data filter on this column)");
			}
		}
		else {
			tableHeader.setToolTipText(null);
		}
	}
	private DataFilter findDataFilter(String columnName) {
		for (DataFilter dataFilter:dataFilters) {
			if (dataFilter.getColumnName().equals(columnName)){
				return dataFilter;
			}
		}
		return null;
	}

	@Override
	public void filterChanged(DataFilter dataFilter) {
		queryData();
		sayQueryClause();
	}

	private void sayQueryClause() {
		String clause = getQueryClause();
		if (clause == null || clause.length() == 0) {
			filterClause.setText("No Filters");
		}
		else {
			filterClause.setText(clause);
		}
	}

	private String getQueryClause() {
		String clause = "WHERE ";
		boolean needAnd = false;
		String newClause;
		if (pamConnection == null) {
			System.out.println("No database connection");
			return null;
		}
		for (DataFilter dataFilter:dataFilters) {
			newClause = dataFilter.getFilterClause(pamConnection.getSqlTypes());
			if (newClause != null && newClause.length() > 0) {
				if (needAnd) {
					clause += "AND ";
				}
				clause += "(" + newClause + ") ";
				needAnd = true;
			}
		}
		if (needAnd == false) {
			return "";
		}
		return clause;
	}

	public boolean queryData() {
		if (tableCursor == null) {
			tableCursor = PamCursorManager.createCursor(tableDefinition); 
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		tableCursor.openScrollableCursor(con, true, true, getQueryClause());
		tableCursorRows = 0;
		try {
			tableCursor.last();
		}
		catch (Exception e) {
			// can happen naturally if there are no data. 
			tableModel.fireTableDataChanged();
			return false;
		}
		try {
			tableCursorRows = tableCursor.getRow();
//			System.out.println("Table cursor rows = " + tableCursorRows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		tableModel.fireTableDataChanged();
		return true;
	}
	
	@Override
	public void cancelButtonPressed() {
		cancelled = true;
	}

	@Override
	protected void okButtonPressed() {
		boolean ok = exportSystem.exportData(visibleTableItems, tableCursor);
		if (ok) {
			getCancelButton().setText("Close");
		}
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	private class FiltersButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			filterSelectionPressed(e);
		}
		
	}
	private class HeadingMouseAdapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			headingMouseClick(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			headingMouseMoved(e);
		}
		
	}

	private class ExportTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		
		private int lastRow = 0;
		
		private boolean moveToRow(int iRow) {
			if (tableCursor == null) {
				return false;
			}
			if (iRow == lastRow) {
				return true;
			}
			if (tableCursor.absolute(iRow)) {
				try {
					tableCursor.moveDataToTableDef(true);
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				lastRow = iRow;
				return true;
			}
			else {
				lastRow = 0;
				return false;
			}
			
		}

		@Override
		public int getColumnCount() {
			return visibleTableItems.size();
		}

		@Override
		public int getRowCount() {
			if (tableCursor == null) {
				return 0;
			}
			return tableCursorRows;
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (moveToRow(iRow+1) == false) {
				return null;
			}
			PamTableItem tableItem = visibleTableItems.get(iCol);
			return tableItem.getValue();			
		}

		@Override
		public String getColumnName(int iCol) {
			return visibleTableItems.get(iCol).getName();
		}
		
	}

	public void filterSelectionPressed(ActionEvent e) {
		if (dataFilters.size() < 0) {
			return;
		}
//		dataFilters.get(0).filterSelectAction(null);
	}
	
}
