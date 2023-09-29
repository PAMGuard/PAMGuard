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
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.warn.WarnOnce;
import PamView.tables.SwingTableColumnWidths;
import tethys.Collection;
import tethys.DocumentInfo;
import tethys.TethysControl;
import tethys.dbxml.TethysException;

/**
 * Table view of a collection of Tethys documents. 
 * @author dg50
 *
 */
public class TethysDocumentTable implements PamDialogPanel {

	private TethysControl tethysControl;
	
	private Collection collection;
	
	private JTable mainTable;
	
	private TableModel tableModel;
	
	private ArrayList<DocumentInfo> documentInfos;
	
	private JPanel mainPanel;
	
	private JScrollPane scrollPane;

	/**
	 * @param tethysControl
	 * @param collectionName
	 */
	public TethysDocumentTable(TethysControl tethysControl, Collection collection) {
		this.tethysControl = tethysControl;
		this.collection = collection;
		mainPanel = new JPanel(new BorderLayout());
		tableModel = new TableModel();
		mainTable = new JTable(tableModel);
		scrollPane = new JScrollPane(mainTable);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"TethysDocumentsTable", mainTable);
		mainTable.addMouseListener(new TableMouse());
		mainTable.setRowSelectionAllowed(true);
		mainTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	public void updateTableData() {
		documentInfos = tethysControl.getDbxmlQueries().getCollectionDocumentList(collection);
		if (documentInfos != null) {
			Collections.sort(documentInfos);
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
		if (documentInfos == null) {
			return;
		}
		int row = mainTable.getSelectedRow();
		if (row < 0|| row >= documentInfos.size()) {
			return;
		}
		
		DocumentInfo docInfo = documentInfos.get(row);
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Show document " + docInfo);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDocument(docInfo);
			}
		});
		popMenu.add(menuItem);
		

		int[] rows = mainTable.getSelectedRows();
		if (rows != null && rows.length == 1) {
//			docName = documentNames.get(rows[0]);
			menuItem = new JMenuItem("Delete document " + docInfo);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteDocument(docInfo);
				}
			});
			popMenu.add(menuItem);
		}
		else if (rows != null && rows.length > 1) {
			String mt = String.format("Delete multiple (%d) documents", rows.length);
			menuItem = new JMenuItem(mt);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteDocuments(rows);
				}
			});
			popMenu.add(menuItem);
		}
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void showDocument(DocumentInfo docInfo) {
		tethysControl.displayDocument(docInfo);
	}

	private void deleteDocument(DocumentInfo docInfo) {
		int ans = WarnOnce.showNamedWarning("deletedoc "+ collection.collectionName(), PamController.getMainFrame(), "Delete document", 
				"Are you sure you want to delete the document " + docInfo, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			try {
				tethysControl.getDbxmlConnect().removeDocument(docInfo.getCollection().collectionName(), docInfo.getDocumentId());
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docInfo);
				System.out.println(e.getMessage());
			}
		}
		updateTableData();
	}
	
	private void deleteDocuments(int[] rows) {
		int ans = WarnOnce.showNamedWarning("deletedoc "+collection.collectionName(), PamController.getMainFrame(), "Delete documents", 
				"Are you sure you want to delete multiple documents ", WarnOnce.OK_CANCEL_OPTION);
		if (ans != WarnOnce.OK_OPTION) {
			return;
		}
		/*
		 *  make a new list before anything is deleted since the
		 *  man list will get updated during deletion and be out of date.  
		 */
		DocumentInfo[] docInfos = new DocumentInfo[rows.length];
		for (int i = 0; i < rows.length; i++) {
			 docInfos[i] = documentInfos.get(rows[i]);
		}
		// now it's safe to delete them. 
		for (int i = 0; i < docInfos.length; i++) {
			try {
				tethysControl.getDbxmlConnect().removeDocument(docInfos[i].getCollection().collectionName(), docInfos[i].getDocumentId());
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docInfos[i]);
				System.out.println(e.getMessage());
			}
		}
		updateTableData();
	}

	private class TableModel extends AbstractTableModel {
		
		private String[] columnNames = {"", "Document Name", "Document Id"};

		@Override
		public int getRowCount() {
			if (documentInfos == null) {
				return 0;
			}
			return documentInfos.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (documentInfos == null) {
				return null;
			}
			DocumentInfo docInfo = documentInfos.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return rowIndex;
			case 1:
				return docInfo.getDocumentName();
			case 2:
				return docInfo.getDocumentId();
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
	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
		updateTableData();
	}

}
