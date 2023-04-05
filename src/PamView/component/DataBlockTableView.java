package PamView.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import PamController.PamController;
import PamView.PamTable;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.debug.Debug;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.ScrollPaneAddon;
import qa.QATestDataUnit;

public abstract class DataBlockTableView<T extends PamDataUnit> {

	private PamDataBlock<T> pamDataBlock;
	
	private PamPanel tablePanel;

	private boolean isViewer;

	private String displayName;

	private DataBlockTableView<T>.BlockTableModel blockTableModel;

	private PamTable testTable;

	private PamPanel topPanel;

	private SwingTableColumnWidths columnWidths;

	/**
	 * Most work will run off a copy of the data. 
	 * Makes it easier to include data selectors, etc. 
	 */
	private ArrayList<T> dataUnitCopy;
	
	private Object copySynch = new Object();

	public DataBlockTableView(PamDataBlock<T> pamDataBlock, String displayName) {
		this.pamDataBlock = pamDataBlock;
		this.displayName = displayName;
		tablePanel = new PamPanel(new BorderLayout());
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		blockTableModel = new BlockTableModel();
		pamDataBlock.addObserver(new DataObs());
		testTable = new DataBlockTable(blockTableModel);
		JScrollPane scrollPane = new PamScrollPane(testTable);
		tablePanel.add(BorderLayout.CENTER, scrollPane);
		testTable.addMouseListener(new MouseAction());

		columnWidths = new SwingTableColumnWidths(displayName, testTable);

		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		if (isViewer) {
			topPanel = new PamPanel(new BorderLayout());
			ScrollPaneAddon sco = new ScrollPaneAddon(scrollPane, displayName,
					AbstractPamScrollerAWT.HORIZONTAL, 1000, 2*3600*1000, true);
			sco.addDataBlock(pamDataBlock);
			topPanel.add(BorderLayout.EAST, sco.getButtonPanel());
			tablePanel.add(BorderLayout.NORTH, topPanel);
			sco.addObserver(new ViewScrollObserver());
		}

		SwingUtilities.invokeLater(new Runnable() {
			/*
			 * this needs to run a little later - after the consstructor for the superclass
			 * has run, so that the column widths have initialised correclty.  
			 */
			@Override
			public void run() {
				blockTableModel.fireTableStructureChanged();
				columnWidths.setColumnWidths();
			}
		});
	}
	
	/**
	 * Call the fireTableStructureChanged function for the table. 
	 */
	public void fireTableStructureChanged() {
		blockTableModel.fireTableStructureChanged();
		if (columnWidths != null) {
			columnWidths.setColumnWidths();
		}
	}
	
	public JComponent getComponent() {
		return tablePanel;
	}
	
	/**
	 * Show the scroll bar controls at the top of the display in viewer mode. 
	 * The are on by default, so if you call this, it will probably be with 'false'
	 * @param show show controls
	 */
	public void showViewerScrollControls(boolean show) {
		if (topPanel != null) {
			topPanel.setVisible(show);
		}
	}
	
	/**
	 * Set allowing of multiple row selection. 
	 * @param allow
	 */
	public void setAllowMultipleRowSelection(boolean allow) {
		if (allow) {
			testTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		else {
			testTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
	
	private class DataBlockTable extends PamTable {
		
		/**
		 * @param dm
		 */
		public DataBlockTable(TableModel dm) {
			super(dm);
			setToolTipText(pamDataBlock.getDataName());
			setFillsViewportHeight(true);
		}

		@Override
        public String getToolTipText(MouseEvent e) {
            String tip = null;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            if (rowIndex < 0) {
            	return null;
            }
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            T dataUnit = getDataUnit(rowIndex);
        	return DataBlockTableView.this.getToolTipText(dataUnit, realColumnIndex);
        }
	}
	
	/**
	 * Get a tooltip for the data unit. 
	 * @param dataUnit Data unit for the row the mouse is over - note that this might be null
	 * @param columnIndex column the mouse is over. 
	 * @return tool tip text or null
	 */
	public String getToolTipText(T dataUnit, int columnIndex) {
		if (dataUnit == null) {
			String[] colNames = getColumnNames();
			if (colNames != null && colNames.length > columnIndex) {
				return colNames[columnIndex];
			}
			else {
				return null;
			}
		}
		else {
			return dataUnit.getSummaryString();
		}
	}
	
	/**
	 * 
	 * @return a list of column names. 
	 */
	public abstract String[] getColumnNames();
	
	/**
	 * Data for a particular column extracted from the data unit. 
	 * @param dataUnit
	 * @param column
	 * @return
	 */
	public abstract Object getColumnData(T dataUnit, int column);
	
	/**
	 * The class of the table column. 
	 * Defaults to 'Object' can be overridden. 
	 * @param columnIndex
	 * @return
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return null;
	}

	/**
	 * The data unit for a given row. Note that in some circumstances this 
	 * may return null since data may get removed in a different thread
	 * between asking for the number of rows and trying to get the data. 
	 * @param tableRow
	 * @return data unit for the table row. 
	 */
	private final T getDataUnit(int tableRow) {
		synchronized (copySynch) {
			int rowIndex = getDataIndexForRow(tableRow);
			if (rowIndex < 0) return null;
			if (dataUnitCopy == null) {
				return null;
			}
			return dataUnitCopy.get(tableRow);
//			return pamDataBlock.getDataUnit(rowIndex, PamDataBlock.REFERENCE_CURRENT);
		}
	}
	
	/**
	 * Get the number of rows in the table - default behaviour is the 
	 * number of rows in the datablock, but this may be overridded if
	 * data are being selected in a different way. 
	 * @return number of table rows to show. 
	 */
	public int getRowCount() {
		if (dataUnitCopy == null) {
			return 0;
		}
		return dataUnitCopy.size();
	}
	
	/**
	 * Get the absolute index of the data within the PAMDataBlock for the row in the table. 
	 * Default behaviour is that in viewer mode, there is 1:1 correspondence, so data are 
	 * displayed in order. In normal mode, data are displayed in reverse oder, with newest data
	 * in row 0.
	 * @param tableRow
	 * @return
	 */
	public int getDataIndexForRow(int tableRow) {
		if (dataUnitCopy == null) {
			return tableRow;
		}
		int nRow = dataUnitCopy.size();
		if (!isViewer) {
			tableRow = nRow-tableRow-1;
		}		
		return tableRow;
	}
	
	/**
	 * Data observer. Will cause table update whenever anything in the 
	 * table is added or updated. 
	 * @author dg50
	 *
	 */
	private class DataObs extends PamObserverAdapter {

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			DataBlockTableView.this.updatePamData();
//			blockTableModel.fireTableDataChanged();
		}
		
		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			DataBlockTableView.this.updatePamData();
//			blockTableModel.fireTableDataChanged();
		}

		@Override
		public String getObserverName() {
			return displayName;
		}

	}
	
	private void updatePamData() {
		synchronized (copySynch) {
			dataUnitCopy = pamDataBlock.getDataCopy();
		}
		blockTableModel.fireTableDataChanged();
	}
	
	
	private class MouseAction extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			checkRowSelection(e);
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}
	}
	
	public void popupMenuAction(MouseEvent e, T dataUnit, String colName) {
		
	}

	private void showPopupMenu(MouseEvent e) {
		checkRowSelection(e);
		T dataUnit = getDataUnit(testTable.rowAtPoint(e.getPoint()));
		int selColumn = testTable.columnAtPoint(e.getPoint());
		String colName = null;
		String[] colNames = getColumnNames();
		if (selColumn >= 0 && selColumn < colNames.length) {
			colName = getColumnNames()[selColumn];
		}
		popupMenuAction(e, dataUnit, colName);
	}
	
	/**
	 * Check for multiple row selection. <p>
	 * If wanted, this should be called back from implementations of popupMenuAction
	 * @return Array of multiple rows selected. 
	 */
	public T[] getMultipleSelectedRows() {
		if (dataUnitCopy == null) {
			return null;
		}
//		synchronized(pamDataBlock.getSynchLock()) { // synch not needed with data copy. 
		synchronized (copySynch) {
			int[] selRows = testTable.getSelectedRows();
			if (selRows == null) {
				return null;
			}
			T[] selUnits = (T[]) new PamDataUnit[selRows.length];
			for (int i = 0; i < selRows.length; i++) {
				selUnits[i] = getDataUnit(selRows[i]);
			}
			return selUnits;
		}
	}
	/**
	 * If right clicking, may have right clicked on a different row, 
	 * so consider changing the row selection
	 * @param e
	 */
	private void checkRowSelection(MouseEvent e) {
		int tableRow = testTable.rowAtPoint(e.getPoint());
		int currentRow = testTable.getSelectedRow();
		if (tableRow != currentRow) {
//			Debug.out.printf("Right click on row %d, but row %d is selected\n", tableRow, currentRow);
			// check range. 
			if (tableRow >= 0 && tableRow < blockTableModel.getRowCount()) {
				testTable.setRowSelectionInterval(tableRow, tableRow);
			}
		}
	}

	private class ViewScrollObserver implements PamScrollObserver {

		@Override
		public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
//			blockTableModel.fireTableDataChanged();
			updatePamData();
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
//			blockTableModel.fireTableDataChanged();
			updatePamData();
		}
		
	}
	
	private class BlockTableModel extends AbstractTableModel {

		private T lastDataUnit;
		private int lastRowIndex = -1;
		
		@Override
		public int getRowCount() {
			return DataBlockTableView.this.getRowCount();
		}

		@Override
		public int getColumnCount() {
			String[] names = getColumnNames();
			if (names == null) {
				return 0;
			}
			else {
				return names.length;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
//			System.out.printf("Get data for row %d, column %d\n ", rowIndex, columnIndex);
			T dataUnit;
			if (lastDataUnit == null || columnIndex == 0 || rowIndex != lastRowIndex) {
				lastDataUnit = dataUnit = getDataUnit(rowIndex);
				lastRowIndex = rowIndex;
			}
			else { // should be the same data unit. 
				dataUnit = lastDataUnit;
			}
			if (dataUnit == null) {
				return null;
			}
			return getColumnData(dataUnit, columnIndex);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return getColumnNames()[column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> colClass = DataBlockTableView.this.getColumnClass(columnIndex);
			if (colClass == null) {
				return super.getColumnClass(columnIndex);
			}
			else {
				return colClass;
			}
		}
		
	}

}
