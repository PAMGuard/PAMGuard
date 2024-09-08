package PamController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import PamView.dialog.PamDialog;

public class ModuleOrderDialog extends PamDialog implements ActionListener,
ListSelectionListener{

	private static PamController pamController;
	private static Frame parentFrame;
	private static ModuleOrderDialog singleInstance;
	
	TableData tableData;
	
	JTable moduleTable;
	JButton upButton, downButton;
	
	String[] columnNames = { "Module Type", "Module Name", "Tab"};
	
	static int[] localLUTTable;

	
	private ModuleOrderDialog(Frame parentFrame) {
		super(parentFrame, "Module Order", false);
		ModuleOrderDialog.parentFrame = parentFrame;

		tableData = new TableData();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout());

		moduleTable = new JTable(tableData);
		moduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moduleTable.getSelectionModel().addListSelectionListener(this);
		tableData.addTableModelListener(moduleTable);
		TableColumn column = null;
		column = moduleTable.getColumnModel().getColumn(2);
		column.setPreferredWidth(10); // sport column is

		JScrollPane scrollPanel = new JScrollPane(moduleTable);
		scrollPanel.setPreferredSize(new Dimension(290, 150));
		panel.add(BorderLayout.CENTER, scrollPanel);

		JPanel e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		e.add(upButton = new JButton(" Up "));
		e.add(downButton = new JButton("Down"));
		upButton.addActionListener(this);
		downButton.addActionListener(this);

		panel.add(BorderLayout.EAST, e);
		mainPanel.add(BorderLayout.CENTER, panel);

		setDialogComponent(mainPanel);
		setResizable(true);
	}
	
	public static int[] showDialog(PamController pamController, Frame parentFrame)
	{
		ModuleOrderDialog.pamController = pamController;
		if (singleInstance == null || ModuleOrderDialog.parentFrame != parentFrame) {
			singleInstance = new ModuleOrderDialog(parentFrame);
		}
		singleInstance.makeLocalLUTTable();
		singleInstance.enableButtons();
		singleInstance.setVisible(true);
		return ModuleOrderDialog.localLUTTable;
	}
	
	public void makeLocalLUTTable()
	{
		localLUTTable = new int[pamController.getNumControlledUnits()];
		for (int i = 0; i < localLUTTable.length; i++) {
			localLUTTable[i] = i;
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		int r = moduleTable.getSelectedRow();
		int dum;
		if (e.getSource() == upButton) {
			if (r >= 1) {
				dum = localLUTTable[r];
				localLUTTable[r] = localLUTTable[r-1];
				localLUTTable[r-1] = dum;
				r--;
			}
		} else if (e.getSource() == downButton) {
			if (r < moduleTable.getRowCount() - 1) {
				dum = localLUTTable[r];
				localLUTTable[r] = localLUTTable[r+1];
				localLUTTable[r+1] = dum;
				r++;
			}
		} 
		tableData.fireTableDataChanged();
		moduleTable.setRowSelectionInterval(r, r);
		enableButtons();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		enableButtons();
		
	}

	private void enableButtons() {
		int row = moduleTable.getSelectedRow();
		int rows = moduleTable.getRowCount();
		upButton.setEnabled(row >= 1);
		downButton.setEnabled(row >= 0 && row < rows - 1);

	}
	
	class TableData extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return pamController.getNumControlledUnits();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			if (rowIndex >= pamController.getNumControlledUnits()) return null;
			PamControlledUnit pcu = pamController.getControlledUnit(localLUTTable[rowIndex]);
			
			switch (columnIndex) {
			case 0:
				return pcu.getUnitType();
			case 1:
				return pcu.getUnitName();
			case 2:
				if (pcu.getTabPanel() != null) {
					return "Y";
				}
			}
			return null;
		}


		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}
		
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {

		localLUTTable = null;
		
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		return (localLUTTable != null);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	
}
