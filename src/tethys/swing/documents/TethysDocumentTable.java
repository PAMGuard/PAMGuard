package tethys.swing.documents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.WestAlignedPanel;
import PamView.tables.SwingTableColumnWidths;
import tethys.Collection;
import tethys.DocumentInfo;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
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

	private JComboBox<Collection> collectionSelector;
	
	private JCheckBox projectOnly, datasetOnly;
	
	/**
	 * @param tethysControl
	 * @param collectionName
	 */
	public TethysDocumentTable(TethysControl tethysControl, Collection collection) {
		this.tethysControl = tethysControl;
		this.collection = collection;
		mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new GridBagLayout());
		JPanel northPanel = new WestAlignedPanel(topPanel);
		GridBagConstraints c = new PamGridBagContraints();
		northPanel.setBorder(new TitledBorder("Options"));
		collectionSelector = new JComboBox<Collection>();
		Collection[] items = Collection.mainList();
		for (int i = 0; i < items.length; i++) {
			collectionSelector.addItem(items[i]);
		}
//		projectOnly = new JCheckBox("Current project only");
		topPanel.add(new JLabel("Collection "),c);
		c.gridx++;
		topPanel.add(collectionSelector,c);
		c.gridx++;
		topPanel.add(projectOnly = new JCheckBox("Current Tethys Project"));
		c.gridx++;
		topPanel.add(datasetOnly = new JCheckBox("Current PAMGuard dataset"));
//		topPanel.add(projectOnly);
		mainPanel.add(BorderLayout.NORTH, northPanel);
		collectionSelector.setSelectedItem(collection);
//		projectOnly.setSelected(tethysControl.getTethysExportParams().projectOnlyDocs);
		collectionSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				collectionChanged();
			}
		});

		projectOnly.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				collectionChanged();
			}
		});
		datasetOnly.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				collectionChanged();
			}
		});
		
		tableModel = new TableModel();
		mainTable = new JTable(tableModel) {
            public String getToolTipText(MouseEvent e) {
            	return tableModel.getToolTipText(e);
            }
		};
		scrollPane = new JScrollPane(mainTable);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"TethysDocumentsTable", mainTable);
		mainTable.addMouseListener(new TableMouse());
		mainTable.setRowSelectionAllowed(true);
		mainTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"docstableview", mainTable);
		projectOnly.setToolTipText("Show only documents that are associated with this Tethys project name");
		datasetOnly.setToolTipText("Show only documents that are associated with this PAMGuard dataset");
	}

	/**
	 * Called on any options change. 
	 */
	protected void collectionChanged() {
		Collection newItem = (Collection) collectionSelector.getSelectedItem();
		if (newItem == null) {
			return;
		}
		this.collection = newItem;
		
		updateTableData();
	}

	public void updateTableData() {
//		documentInfos = tethysControl.getDbxmlQueries().getCollectionDocumentList(collection);
		ArrayList<DocumentInfo> allInfos = tethysControl.getDocumentMap().getCollection(collection);
		if (allInfos != null) {
			Collections.sort(allInfos);
			// and filter the data. 
			documentInfos = filterData(allInfos);
		}
		else {
			documentInfos = allInfos;
		}
		tableModel.fireTableDataChanged();
	}
	
	private ArrayList<DocumentInfo> filterData(ArrayList<DocumentInfo> allDocs) {
		boolean t = projectOnly.isSelected();
		boolean p = datasetOnly.isSelected();
		boolean f = canFilter(collection);
		if (f == false) {
			return allDocs;
		}
		if (t == false && p == false) {
			return allDocs;
		}
		ArrayList<DocumentInfo> filtered = new ArrayList<>();
		for (DocumentInfo aDoc : allDocs) {
			if (t && aDoc.isThisTethysProject() == false) {
				continue;
			}
			if (p && aDoc.isThisPAMGuardDataSet() == false) {
				continue;
			}
			filtered.add(aDoc);
		}
		return filtered;
	}
	
	private boolean canFilter(Collection collection) {
		if (collection == null) {
			return false;
		}
		switch (collection) {
		case Calibrations:
		case Deployments:
		case Detections:
		case Localizations:
			return true;
		case Ensembles:
			break;
		case ITIS:
			break;
		case ITIS_ranks:
			break;
		case OTHER:
			break;
		case SourceMaps:
			break;
		case SpeciesAbbreviations:
			break;
		default:
			break;
		
		}
		return false;
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

		@Override
		public void mouseClicked(MouseEvent e) {
			int row = mainTable.getSelectedRow();
			if (row < 0|| row >= documentInfos.size()) {
				return;
			}
			DocumentInfo docInfo = documentInfos.get(row);
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				showDocument(docInfo);
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
		String docName = docInfo.getDocumentName();
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Display document " + docName);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDocument(docInfo);
			}
		});
		popMenu.add(menuItem);
	
		// can only delete when making a sub selection. 
		boolean haveSelection = projectOnly.isSelected() || datasetOnly.isSelected();
//		String tipText = "Deleting is only possible when you select only the current project or dataset";
		String tipText = "You can only delete documents associated with the current Tethys Project: \""+ tethysControl.getGlobalDeplopymentData().getProject() + "\"";

		int[] rows = mainTable.getSelectedRows();
		boolean nonProject = false;
		if (rows != null) {
			for (int i = 0; i < rows.length; i++) {
				DocumentInfo dInf = documentInfos.get(rows[i]);
				if (dInf.isThisTethysProject() == false) {
					nonProject = true;
					break;
				}
			}
		}
		
		if (rows != null && rows.length == 1) {
			popMenu.addSeparator();
//			docName = documentNames.get(rows[0]);
			menuItem = new JMenuItem("Delete document " + docName);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteDocument(docInfo);
				}
			});
			popMenu.add(menuItem);
			if (nonProject) {
				menuItem.setEnabled(false);
				menuItem.setToolTipText(tipText);
			}
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
			if (nonProject) {
				menuItem.setEnabled(false);
				menuItem.setToolTipText(tipText);
			}
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
				tethysControl.getDbxmlConnect().removeDocument(docInfo.getCollection().collectionName(), docInfo.getDocumentName());
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docInfo);
				System.out.println(e.getMessage());
			}
		}
		tethysControl.sendStateUpdate(new TethysState(StateType.DELETEDATA, collection));
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
				tethysControl.getDbxmlConnect().removeDocument(docInfos[i].getCollection().collectionName(), docInfos[i].getDocumentName());
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docInfos[i]);
				System.out.println(e.getMessage());
			}
		}
		tethysControl.sendStateUpdate(new TethysState(StateType.DELETEDATA, collection));
		updateTableData();
	}

	private class TableModel extends AbstractTableModel {
		
		private String[] columnNames = {"Ind", "Document Name", "Document Id", "Project", "Dataset"};
		private String[] toolTip = {"", "Name of Tethys Document", "Document Id", "Part of this Tethys Project", "Part of this PAMGuard dataset"};

		@Override
		public int getRowCount() {
			if (documentInfos == null) {
				return 0;
			}
			return documentInfos.size();
		}

		public String getToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int rowIndex = mainTable.rowAtPoint(p);
            int colIndex = mainTable.columnAtPoint(p);
            if (colIndex < 0 || colIndex >= toolTip.length) {
            	return null;
            }
            return toolTip[colIndex];
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
			case 3:
				return docInfo.isThisTethysProject() ? "  Y" : null;
			case 4:
				return docInfo.isThisPAMGuardDataSet() ? "  Y" : null;
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
		this.collectionSelector.setSelectedItem(collection);
		updateTableData();
	}

}
