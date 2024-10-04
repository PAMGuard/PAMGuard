/**
 * 
 */
package loggerForms;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamTable;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;

/**
 * @author GrahamWeatherup
 *
 */
public class FormsDataDisplayTable {

	private FormDescription formDescription;
	
	private FormsTableDataModel formsTableDataModel;
	
	private JPanel mainPanel;
	private JTable formsTable;

	private JScrollPane scrollPane;

	private FormsControl formsControl;

	/**
	 * @param formDescription
	 */
	public FormsDataDisplayTable(FormsControl formsControl, FormDescription formDescription) {
		this.formsControl = formsControl;
		this.formDescription=formDescription;
		
//		GridLayout(rows,columns)
		mainPanel = new JPanel(new GridLayout(1, 0));
		mainPanel.setOpaque(true);

		formsTableDataModel = new FormsTableDataModel();
		formsTable = new PamTable(formsTableDataModel);
//		TableColumnModel colomnModel = columnModel=new FormsTableColumnModel();
//		formsTableDataModel.addTableModelListener(new TableListener());
		formsTable.getSelectionModel().addListSelectionListener(new TableListListener());
		
//		formsTable.setColumnModel(columnModel);
		
//		new JT
//		System.out.println
//		formsTable.getRowSelectionAllowed();
		
		formsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		// for now, don't allow edits in viewer mode. 
//		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			formsTable.addMouseListener(new EditDataListener());
//		}

		scrollPane = new JScrollPane(formsTable);
		
		mainPanel.add(scrollPane);
		
	}

	class TableListListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int row = formsTable.getSelectedRow();
			int rc=formsTable.getRowCount();
			int duIndex = rc-row-1;
			FormsDataUnit formsDataUnit = findDataUnitForRow(row);
			if (formsDataUnit==null){
				return;
			}
			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				formDescription.viewDataUnit(formsDataUnit);
			}
		}
	}
	
	private boolean canEdit() {
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			return true;
		}
		if (formsControl == null) return false;
		return formsControl.getFormsParameters().allowViewerChanges;
	}
	/**
	 * Find the data unit for a particular row in the table. 
	 * @param iRow
	 * @return data unit or null if none found. 
	 */
	FormsDataUnit findDataUnitForRow(int iRow) {
		int rc=formsTable.getRowCount();
		int duIndex = rc-iRow-1;
		return formDescription.getFormsDataBlock().getDataUnit(duIndex, PamDataBlock.REFERENCE_CURRENT);
	}
	
	class EditDataListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!canEdit()) {
				return;
			}
			if (e.getClickCount() == 2) {
				
				int row = formsTable.getSelectedRow();
				
				FormsDataUnit formsDataUnit = findDataUnitForRow(row);
				if (formsDataUnit==null){
					PamDialog.showWarning(null, "WARNING", "could not find formsDataUnit");
					return;
				}
				editDataUnit(formsDataUnit);			
				
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybePopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybePopup(e);
		}

		private void maybePopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return;
			}
			if (!canEdit()) {
				return;
			}
			int row = formsTable.getSelectedRow();
			
			showPopupMenu(e.getPoint());
		}
	}
	
	
	
	class FormsTableDataModel extends AbstractTableModel {
//		FormsDataBlock formsDataBlock;
		
		private final String[] extraColumns = {"Id", "Time Saved (UTC)"};
		/**
		 * 
		 */
		public FormsTableDataModel() {
			// TODO Auto-generated constructor stub
			super();
//			formsDataBlock = formDescription.getFormsDataBlock();
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			if (column<extraColumns.length){
				return extraColumns[column];
			}else{
				return formDescription.getInputControlDescriptions().get(column-extraColumns.length).getTitle();
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return formDescription.getInputControlDescriptions().size()+extraColumns.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			FormsDataBlock formsDataBlock = formDescription.getFormsDataBlock();
			if (formsDataBlock == null) return 0;
			return formsDataBlock.getUnitsCount();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			FormsDataBlock formsDataBlock = formDescription.getFormsDataBlock();
			FormsDataUnit pamDataUnit = formsDataBlock.getDataUnit(getRowCount()-rowIndex-1 //so the newest data is at the top
					, PamDataBlock.REFERENCE_CURRENT);
			switch(columnIndex) {
			case 0: // time
				return pamDataUnit.getDatabaseIndex();
			case 1:
				return PamCalendar.formatDateTime(pamDataUnit.getTimeMilliseconds());
			default:
				try {
					Object[] fd = pamDataUnit.getFormData();
					int ctIndex = columnIndex-extraColumns.length;
					ControlDescription ctrlDescription = formDescription.getInputControlDescriptions().get(ctIndex);
					Object value = ctrlDescription.formatDataItem(fd[ctIndex]);
					if (value == null) return null;
					if (getColumnClass(columnIndex) == Boolean.class) {
						if (!(value instanceof Boolean)) {
//							System.out.println("Bad boolean value: " + value);
							return FormsControl.checkBadBoolean(value);
						}
					}
					return value;
//					return fd[ctIndex];
				}
				catch (Exception e) {
					return "Error!";
				}
			}
//			return "";
			
			
		}
		
		@Override
		public Class getColumnClass(int c){
			if (c<extraColumns.length){
				return super.getColumnClass(c);
			}
			else if (formDescription.getInputControlDescriptions().get(c-extraColumns.length).getEType()==ControlTypes.CHECKBOX){
				return Boolean.class;
			}
			return super.getColumnClass(c-extraColumns.length);
//			int sqlType = formDescription.getInputControlDescriptions().get(c-1).;
			
		}
		
		
		@Override
		public boolean isCellEditable(int row, int col){
			return false;
		}
		
		
	}


	/**
	 * @return
	 */
	public JPanel getMainPanel() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	public void showPopupMenu(Point pt) {
		
		FormsDataUnit dataUnit = findDataUnitForRow(formsTable.getSelectedRow());

		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		if (dataUnit != null) {
			menuItem = new JMenuItem("Edit row ...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editDataUnit(dataUnit);
				}
			});
			menu.add(menuItem);
		}

		menuItem = new JMenuItem("Create row ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDataUnit();
			}
		});
		menu.add(menuItem);
		
		if (dataUnit != null) {
			menu.addSeparator();
			menuItem = new JMenuItem("Delete row ...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteDataUnit(dataUnit);
				}
			});
			menu.add(menuItem);
		}

		menu.show(formsTable, pt.x, pt.y);
	}
	
	private boolean editDataUnit(FormsDataUnit dataUnit) {
		FormsDataUnitEditor due = new FormsDataUnitEditor(formDescription, dataUnit);
		return true;
	}

	private boolean deleteDataUnit(FormsDataUnit dataUnit) {
		if (dataUnit == null) {
			return false;
		}
		int ans = WarnOnce.showWarning("Delete logger form data", "Are you sure you want to delete data Id " + dataUnit.getDatabaseIndex(), WarnOnce.OK_CANCEL_OPTION);
		if (ans != WarnOnce.OK_OPTION) {
			return false;
		}
		formDescription.deleteDataUnit(dataUnit);
		return true;
	}
	
	private boolean addDataUnit() {
		formDescription.viewDataUnit(null);
		return false;
	}
	
	/**
	 * Called when data have changed in the datablock. 
	 */
	public void dataChanged() {
		formsTableDataModel.fireTableDataChanged();
	}


	/**
	 * @return the scrollPane
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * Some optins have changed. 
	 */
	public void optionsChange() {
		// TODO Auto-generated method stub
		
	}
}
