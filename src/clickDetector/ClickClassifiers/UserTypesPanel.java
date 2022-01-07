package clickDetector.ClickClassifiers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import clickDetector.ClickControl;

public abstract class UserTypesPanel implements ClassifyDialogPanel {


	private String[] columnNames = {"Enable", "Symbol", "Type", "Code", "Discard", "Alarm" };

	private JButton upButton, downButton;

	private JButton newButton, editButton, deleteButton;

	private JTable typesTable;
	
	private ClickTypesTableData tableData;
	
	private JPanel mainPanel;
	
	private TableColumn discardColumn;
	
	private Window pWindow;
	
	protected ClickControl clickControl;

	public UserTypesPanel(Window pWindow, ClickControl clickControl) {
		
		this.clickControl = clickControl;
		
		this.pWindow = pWindow;

		tableData = new ClickTypesTableData();

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Click Types"));
		mainPanel.setOpaque(true);

		JPanel panel = new JPanel();
//		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout());

		JPanel pc = new JPanel();
		typesTable = new JTable(tableData);
		typesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		typesTable.getSelectionModel().addListSelectionListener(new ListSelection());
		typesTable.addMouseListener(new MouseEvents());
		tableData.addTableModelListener(typesTable);
		TableColumn column = null;
		for (int i = 0; i < typesTable.getColumnCount(); i++) {
			column = typesTable.getColumnModel().getColumn(i);
//			if (i == 2) {
//				column.setPreferredWidth(132); // Name column is bigger
//			} 
//			else if (i == 2) {
//				column.setPreferredWidth(20);
//			}
		}
		discardColumn = typesTable.getColumnModel().getColumn(3);
//		discardColumn.set
//		JCheckBox discardBox = new JCheckBox();
//		discardBox.addActionListener(new DiscardHit());
//		discardColumn.setCellEditor(new DefaultCellEditor(discardBox));
		//
		// // check to see that there is an unassigned type ...
		// if ()

		JScrollPane scrollPanel = new JScrollPane(typesTable);
		scrollPanel.setPreferredSize(new Dimension(340, 150));
		// pc.add(typesTable);
		panel.add(BorderLayout.CENTER, scrollPanel);
		
		JPanel ps = new JPanel();
		ps.setLayout(new BoxLayout(ps, BoxLayout.X_AXIS));
		ps.add(newButton = new JButton("New"));
		ps.add(editButton = new JButton("Edit"));
		ps.add(deleteButton = new JButton("Delete"));
		newButton.addActionListener(new AddButton());
		editButton.addActionListener(new EditButton());
		deleteButton.addActionListener(new DeleteButton());
		panel.add(BorderLayout.SOUTH, ps);

		JPanel n = new JPanel();
		n.setLayout(new BorderLayout());
//		n.setBorder(new EmptyBorder(10, 10, 10, 10));
//		n.add(BorderLayout.WEST, runOnline = new JCheckBox("Run Online"));

		JPanel e = new JPanel();
		e.setLayout(new GridLayout(2,1));
		e.add(upButton = new JButton(" Up "));
		e.add(downButton = new JButton("Down"));
		upButton.addActionListener(new UpButton());
		downButton.addActionListener(new DownButton());
		JPanel f = new JPanel(new BorderLayout());
		f.add(BorderLayout.NORTH, e);

		mainPanel.add(BorderLayout.NORTH, n);
		panel.add(BorderLayout.EAST, f);
		mainPanel.add(BorderLayout.CENTER, panel);
		
		enableButtons();
	}
	
	@Override
	public Component getComponent() {
		return mainPanel;
	}
	
	abstract public int getNumSpecies();
	
	abstract public Icon getSymbol(int iSpecies);
	
	abstract public String getSpeciesName(int iSpecies);
	
	abstract public String getSpeciesCode(int iSpecies);
	
	abstract public Boolean getSpeciesDiscard(int iSpecies);
	
	abstract public void setSpeciesDiscard(int species, Boolean discard);
	
	abstract public boolean getSpeciesCanProcess(int iSpecies, double sampleRate);
	
	abstract public Boolean getSpeciesEnable(int iSpecies);
	
	abstract public void setSpeciesEnable(int species, Boolean enable); 
	
    abstract public Boolean getAlarm(int iSpecies);

    abstract public void setAlarm(int species, Boolean alarmEnabled);

	abstract public void upButton();
	abstract public void downButton();
	abstract public void addButton();
	abstract public void editButton();
	abstract public void deleteButton();
	
	private class UpButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			upButton();
		}
	}
	
	private class DownButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			downButton();
		}
	}
	private class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			addButton();
		}
	}
	private class EditButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			editButton();
		}
	}
	private class DeleteButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			deleteButton();
		}
	}
	
	class ListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			enableButtons();
		}
	}

	private class MouseEvents extends MouseAdapter {
		 @Override
		public void mouseClicked(MouseEvent e){
		      if (e.getClickCount() == 2){
		         	mouseDblClick(e);
		         }
		      }
	}

	private void mouseDblClick(MouseEvent e) {
		int r = typesTable.getSelectedRow();
		int c = typesTable.getSelectedColumn();
		editButton();
//		switch(c) {
//		case 1:
//			editAction();
//		case 2:
//			symbolAction();
//		}
	}
	
	private void enableButtons() {
		int row = typesTable.getSelectedRow();
		int rows = typesTable.getRowCount();
//		runOnline.setEnabled(rows > 0);
		deleteButton.setEnabled(row >= 0);
		editButton.setEnabled(row >= 0);
		upButton.setEnabled(row >= 1);
		downButton.setEnabled(row >= 0 && row < rows - 1);

	}
	
	protected int getSelectedRow() {
		return typesTable.getSelectedRow();
	}
	
	protected void setSelectedRow(int iRow) {
		typesTable.setRowSelectionInterval(iRow, iRow);
//		fireTableDataChanged();
	}
	
	protected void fireTableDataChanged() {
		tableData.fireTableDataChanged();
	}
	
	protected void fireTableDataChangedLater() {
		SwingUtilities.invokeLater(new FireChangedLater());
	}
	
	class FireChangedLater implements Runnable {
		@Override
		public void run() {
			fireTableDataChanged();
		}
	}
	
	private class DiscardHit implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Discard hit row " + getSelectedRow());
			
		}
		
	}
	
	class ClickTypesTableData extends AbstractTableModel {

		// ArrayList<PamProcess> processList;

		public ClickTypesTableData() {
			// processList= pamModelInterface.GetModelProcessList();
			// for (int i = 0; i < processList.size(); i++){
			// //processList.get(i).GetOutputDataBlock().addObserver(this);
			// }
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getRowCount() {
			int rowCount= getNumSpecies();
//			System.out.println("Row Count = " + rowCount + " in " + this.toString());
			return rowCount;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			switch (col) {
			case 0:
				return getSpeciesCanProcess(row, clickControl.getClickDetector().getSampleRate());
			case 1:
				return false;
			case 2:
				return false;
			case 3:
				return false;
			case 4:
				return true;
            case 5:
                return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				setSpeciesEnable(rowIndex, (Boolean) value);
			}
			if (columnIndex == 4) {
				setSpeciesDiscard(rowIndex, (Boolean) value);
			}
			if (columnIndex == 5) {
				setAlarm(rowIndex, (Boolean) value);
            }
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 1) {
				return ImageIcon.class;
			}
//			else if (columnIndex == 3) {
//				return boolean.class;
//			}
			else {
				Object obj = getValueAt(0, columnIndex);
				if (obj == null) {
					return super.getColumnClass(columnIndex);
				}
				return obj.getClass();
			}
		}

		public Object getValueAt(int row, int col) {
			/*
			 * need to find the right process by going through and seeing which
			 * one our block is in for the given row
			 */
			if (row < getNumSpecies()) {
				switch (col) {
				case 0:
					if (getSpeciesCanProcess(row, clickControl.getClickDetector().getSampleRate()) == false) {
						return false;
					}
					return getSpeciesEnable(row);
				case 1:
					return getSymbol(row);
				case 2:
					return getSpeciesName(row);
				case 3:
					return getSpeciesCode(row);
				case 4:
					return getSpeciesDiscard(row);
				case 5:
					return getAlarm(row);
				}
			} else {
				switch (col) {
				case 0:
					break;
				case 1:
					return col;
				case 2:
					return row;
				}
			}
			return null;

		}

	}

	public Window getPWindow() {
		return pWindow;
	}
}
