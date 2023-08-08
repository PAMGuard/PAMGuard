package tethys.swing.documents;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import PamView.dialog.PamDialogPanel;
import PamView.tables.SwingTableColumnWidths;
import tethys.TethysControl;

/**
 * Table view of a collection of Tethys documents. 
 * @author dg50
 *
 */
public class TethysDocumentTable implements PamDialogPanel {

	private TethysControl tethysControl;
	
	private String collectionName;
	
	private JTable mainTable;
	
	private TableModel tableModel;
	
	private ArrayList<String> documentNames;
	
	private JPanel mainPanel;
	
	private JScrollPane scrollPane;

	/**
	 * @param tethysControl
	 * @param collectionName
	 */
	public TethysDocumentTable(TethysControl tethysControl, String collectionName) {
		this.tethysControl = tethysControl;
		mainPanel = new JPanel(new BorderLayout());
		tableModel = new TableModel();
		mainTable = new JTable(tableModel);
		scrollPane = new JScrollPane(mainTable);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"TethysDocumentsTable", mainTable);
		this.setCollectionName(collectionName);
		mainTable.addMouseListener(new TableMouse());		
	}
	
	public void updateTableData() {
		documentNames = tethysControl.getDbxmlQueries().getCollectionDocumentList(collectionName);
		if (documentNames != null) {
			Collections.sort(documentNames);
		}
		tableModel.fireTableDataChanged();
	}
	
	private class TableMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}
		
	}

	public void showPopupMenu(MouseEvent e) {
		if (documentNames == null) {
			return;
		}
		int row = mainTable.getSelectedRow();
		if (row < 0|| row >= documentNames.size()) {
			return;
		}
		String docName = documentNames.get(row);
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Show document " + docName);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDocument(docName);
			}
		});
		popMenu.add(menuItem);
		menuItem = new JMenuItem("Delete document " + docName);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteDocument(docName);
			}
		});
		popMenu.add(menuItem);
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void showDocument(String docName) {
		tethysControl.displayDocument(collectionName, docName);
	}

	private void deleteDocument(String docName) {
		// TODO Auto-generated method stub
		
	}
	
	private class TableModel extends AbstractTableModel {
		
		private String[] columnNames = {"", "Document Id/Name"};

		@Override
		public int getRowCount() {
			if (documentNames == null) {
				return 0;
			}
			return documentNames.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (documentNames == null) {
				return null;
			}
			switch (columnIndex) {
			case 0:
				return rowIndex+1;
			case 1:
				return documentNames.get(rowIndex);
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}
		
	}



	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @param collectionName the collectionName to set
	 */
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
		updateTableData();
	}
}
