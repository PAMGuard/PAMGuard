package generalDatabase.lookupTables;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import PamView.PamSymbol;
import PamView.PamSymbolDialog;
import PamView.dialog.PamDialog;

/**
 * dialog for editing the contents of the lookup table.
 * @author Doug Gillespie
 *
 */
public class LookupEditDialog extends PamDialog {

	private LookUpTables lookUpTables;

	private static LookupEditDialog singleInstance;

	private JTable lutTable;

	private LUTTableDataModel lutTableDataModel;

	private JButton addButton, deleteButton, upButton, downButton;

	private LookupList lookupList;

	private String[] colNames = {"Code", "Text", "Selectable", "Symbol"}; 

	private LookupEditDialog(Window parentFrame) {
		super(parentFrame, "Lookup Editor", false);
		lookUpTables = LookUpTables.getLookUpTables();
		lutTableDataModel = new LUTTableDataModel();
		lutTable = new LUTTable(lutTableDataModel);
		JPanel mainPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(lutTable);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setMinimumSize(new Dimension(300, 200));
		mainPanel.add(BorderLayout.WEST, scrollPane);

		JPanel eastPanel = new JPanel(new BorderLayout());
		JPanel eastTop = new JPanel();
		eastTop.setBorder(new EmptyBorder(5, 5, 5, 5));
//		eastTop.setLayout(new BoxLayout(eastTop, BoxLayout.Y_AXIS));
		eastTop.setLayout(new GridLayout(4, 1));
		eastTop.add(addButton = new JButton("Add item"));
		addButton.addActionListener(new AddItem());
		eastTop.add(deleteButton = new JButton("Delete item"));
		deleteButton.addActionListener(new DeleteItem());
		eastTop.add(upButton = new JButton("Move up"));
		upButton.addActionListener(new MoveUp());
		eastTop.add(downButton = new JButton("Move down"));
		downButton.addActionListener(new MoveDown());
		eastPanel.add(BorderLayout.NORTH, eastTop);
		mainPanel.add(BorderLayout.EAST, eastPanel);

		setDialogComponent(mainPanel);
	}

	public static LookupList showDialog(Window parentFrame, LookupList lookupList) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new LookupEditDialog(parentFrame);
		}
		singleInstance.setTitle(String.format("Lookup Editor (%s)", lookupList.getListTopic()));
		singleInstance.lookupList = lookupList.clone();
		singleInstance.lutTableDataModel.fireTableDataChanged();
		singleInstance.setVisible(true);
		return singleInstance.lookupList;
	}

	private void setTopic(String topic) {
		//		ResultSet r = lookupTables.createResultSet(topic);
	}

	@Override
	public void cancelButtonPressed() {
		lookupList = null;
	}

	@Override
	public boolean getParams() {
		/**
		 * Try to force table to accept half edited text. 
		 */
		TableCellEditor ce = lutTable.getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
			int eRow = lutTable.getEditingRow();
			int eCol = lutTable.getEditingColumn();
			lutTable.setValueAt(ce.getCellEditorValue(), eRow, eCol);
			lutTableDataModel.fireTableDataChanged();
		}
//		int cRow = lutTableDataModel.
//		if (cRow >= 0) {
//			lutTable.setEditingColumn(2);
//		}
		
		
		LookupItem lutItem;
		// check all items have necessary fields filled in
		for (int i = 0; i < lookupList.getList().size(); i++) {
			lutItem = lookupList.getList().get(i);
			if (lutItem.checkItem() == false) {
				return this.showWarning("One or more items do not have a Code or Text field filled in");
			}
			// and set it's order number, so they increase in 10's
			lutItem.setOrder((i+1)*10);
		}
		String repeatCode = lookupList.checkRepeatCodes();
		if (repeatCode != null) {
			return this.showWarning(String.format("The code string %s is repeated in more than one item",
					repeatCode));
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private void addItem() {
		lookupList.addItem(new LookupItem(0, 0, lookupList.getListTopic(), 0, null, null, true, null, null, null));
		lutTableDataModel.fireTableDataChanged();
	}

	private void deleteItem() {
//		showItems();
		int row = lutTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		LookupItem lutItem = lookupList.getList().get(row);
//		System.out.println(String.format("Delete row %d: %s", row, lutItem));
		lookupList.removeItem(lutItem);
		lutTableDataModel.fireTableDataChanged();
	}
	
//	private void showItems() {
//		Vector<LookupItem> lst = lookupList.getList();
//		LookupItem item;
//		System.out.println(String.format("***** There are currently %d items in lookupList %s ******", 
//				lst.size(), lookupList.getListTopic()));
//		for (int i = 0; i < lst.size(); i++) {
//			item = lst.get(i);
//			System.out.println(String.format("Item %d = %s", i, item));
//		}
//	}
	
	private void moveDown() {
		int row = lutTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		int nRows = lookupList.getList().size();
		if (row >= nRows-1) {
			return;
		}
		LookupItem lutItem = lookupList.getList().remove(row);
		lookupList.getList().insertElementAt(lutItem, row+1);
		lutTableDataModel.fireTableDataChanged();
	}
	
	private void moveUp() {
		int row = lutTable.getSelectedRow();
		if (row < 1) {
			return;
		}
		int nRows = lookupList.getList().size();
		if (row > nRows-1) {
			return;
		}
		LookupItem lutItem = lookupList.getList().remove(row);
		lookupList.getList().insertElementAt(lutItem, row-1);
		lutTableDataModel.fireTableDataChanged();
	}

	public void doubleClick(MouseEvent e) {
		int col = lutTable.getSelectedColumn();
		int row = lutTable.getSelectedRow();
		if (row < 0) {
			return;
		}
		LookupItem lutItem = lookupList.getList().get(row);
		if (col == 3) {
			PamSymbol newSymbol = PamSymbolDialog.show(getOwner(), lutItem.getSymbol());
			if (newSymbol != null) {
				lutItem.setSymbolType(String.valueOf(newSymbol.getTextCode()));
				lutItem.setFillColour(newSymbol.getFillColor());
				lutItem.setBorderColour(newSymbol.getLineColor());
				lutTableDataModel.fireTableDataChanged();
			}
		}
	}

	class AddItem implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addItem();
		}
	}
	
	class DeleteItem implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			deleteItem();
		}
	}
	
	class MoveDown implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			moveDown();
		}
	}

	class MoveUp implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			moveUp();
		}
	}
	private class MouseActions extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				doubleClick(e);
			}
		}	
	}

	private class LUTTable extends JTable {


		public LUTTable(LUTTableDataModel lutTableData) {
			super(lutTableData);
			addMouseListener(new MouseActions());
			setRowSelectionAllowed(false);
			setColumnSelectionAllowed(false);
		}

		@Override
		public String getToolTipText(MouseEvent e) {        
			Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);
			int colIndex = columnAtPoint(p);
			int realColumnIndex = convertColumnIndexToModel(colIndex);
			switch(realColumnIndex) {
			case 0:
				return "Click to edit code";
			case 1:
				return "Click to edit item text";
			case 2:
				return "Check to make item selectable";
			case 3:
				return "Double click to edit symbol";
			}

			return super.getToolTipText(e);
		}

	}
	private class LUTTableDataModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			if (lookupList == null) {
				return 0;
			}
			return lookupList.getList().size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (lookupList == null) {
				return 0;
			}
			LookupItem lutItem = lookupList.getList().get(iRow);
			switch(iCol) {
			//			case 0:
			//				return lutItem.getOrder();
			case 0:
				return lutItem.getCode();
			case 1:
				return lutItem.getText();
			case 2:
				return lutItem.isSelectable();
			case 3:
				return lutItem.getSymbol();

			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 2:
				return Boolean.class;
			case 3:
				return ImageIcon.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex <= 2) {
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (lookupList == null) {
				return;
			}
			if (rowIndex < 0) {
				return;
			}
			LookupItem lutItem = lookupList.getList().get(rowIndex);
			switch(columnIndex) {
			case 0:
				lutItem.setCode((String) value);
				break;
			case 1:
				lutItem.setText((String) value);
				break;
			case 2:
				lutItem.setSelectable((Boolean) value);
				break;

			}
		}

	}
}
