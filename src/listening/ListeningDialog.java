package listening;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import PamView.PamSymbol;
import PamView.PamSymbolDialog;
import PamView.dialog.PamDialog;

public class ListeningDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private static ListeningDialog singleInstance;
	
	private ListeningParameters listeningParameters;
	
	private LTableData tableData;
	
	private JTable table;
	
	private ListeningControl listeningControl;
	
	private JButton addButton, removeButton, editButton;
	
	private JButton upButton, downButton;
	
	private ListeningDialog(Window parentFrame) {
		super(parentFrame, "Aural Monitoring", false);
		tableData = new LTableData();
		table = new JTable(tableData);
		table.getSelectionModel().addListSelectionListener(new TableSelection());
		table.addMouseListener(new MouseEvents());
//		tableData.addTableModelListener(new TableMoused());
//		table.addMouseListener(new TableMoused());
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(300,300));
		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.CENTER, scrollPane);
		p.setBorder(new TitledBorder("Species / Sound Types"));
		
		JPanel b = new JPanel();
		b.add(addButton = new JButton("Add ..."));
		b.add(removeButton = new JButton("Remove"));
		b.add(editButton = new JButton("Edit ..."));
		addButton.addActionListener(new AddButton());
		removeButton.addActionListener(new RemoveButton());
		editButton.addActionListener(new EditButton());
		p.add(BorderLayout.SOUTH, b);
		
		JPanel e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		e.add(upButton = new JButton("Move up"));
		e.add(downButton = new JButton("Move Down"));
		upButton.addActionListener(new UpButton());
		downButton.addActionListener(new DownButton());
		p.add(BorderLayout.EAST, e);
		
		for (int i = 0; i < tableData.tableWidths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(tableData.tableWidths[i]);
		}
		
		this.setResizable(true);
		
		setHelpPoint("utilities.listening.docs.Listening_Configuration");
		
		setDialogComponent(p);
	}
	
	public static ListeningParameters showDialog(ListeningControl listeningControl, Frame parentFrame, ListeningParameters listeningParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ListeningDialog(parentFrame);
		}
		singleInstance.listeningControl = listeningControl;
		singleInstance.listeningParameters = listeningParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.listeningParameters;
	}

	@Override
	public void cancelButtonPressed() {
		listeningParameters = null;
	}

	private void setParams() {

		enableControls();
		tableData.fireTableDataChanged();
	}
	
	@Override
	public boolean getParams() {
		// always return true, since changes are made in the 
		// main parameter list. 
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	private void enableControls() {
		int r = getSelectedRow();
		int n = listeningParameters.speciesList.size();
		editButton.setEnabled(r>=0);
		removeButton.setEnabled(r>=0);
		downButton.setEnabled(r<n-1 & r>0);
		upButton.setEnabled(r>0);
	}
	
	private int getSelectedRow() {
		return table.getSelectedRow();
	}
	
	private void tableSelectionChange() {
		enableControls();
	}
	
	private void addAction() {
		String newSp = JOptionPane.showInputDialog(getOwner(), "Type name of new species");
		if (newSp == null) {
			return;
		}
		if (checkSpeciesLength(newSp) == false) {
			return;
		}
		listeningParameters.speciesList.add(new SpeciesItem(newSp));
		tableData.fireTableDataChanged();
	}
	
	private void removeAction() {
		int r = getSelectedRow();
		if (r < 0) {
			return;
		}
		SpeciesItem sp = listeningParameters.speciesList.get(r);
		String str = String.format("Are you sure you want to remove %s from the list ?" , sp);
		int ans = JOptionPane.showConfirmDialog(getOwner(), str, "Remove Item", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return;
		}
		listeningParameters.speciesList.remove(r);
		tableData.fireTableDataChanged();
	}

	private void editAction() {
		int r = getSelectedRow();
		if (r < 0) {
			return;
		}
		SpeciesItem sp = listeningParameters.speciesList.get(r);
		String str = String.format("Edit species %s", sp);

		String newSp = JOptionPane.showInputDialog(getOwner(), str, sp);
		if (newSp == null) {
			return;
		}
		if (checkSpeciesLength(newSp) == false) {
			return;
		}
		sp.setName(newSp);
//		listeningParameters.speciesList.remove(r);
//		listeningParameters.speciesList.add(r, newSp);
		tableData.fireTableDataChanged();
	}
	private void symbolAction() {
		int r = getSelectedRow();
		if (r < 0) {
			return;
		}
		SpeciesItem sp = listeningParameters.speciesList.get(r);
		PamSymbol ps = sp.getSymbol();
		if (ps == null) {
			ps = getDefaultSymbol();
		}
		PamSymbol newSymbol = PamSymbolDialog.show(this, ps);
		if (newSymbol != null) {
			sp.setSymbol(newSymbol);
		}
		tableData.fireTableDataChanged();
	}
	
	private boolean checkSpeciesLength(String speciesStr) {
		if (lengthOK(speciesStr)) {
			return true;
		}
		String msg = String.format("Species name \"%s\" is either zero or longer than the maximum of 50 characters", speciesStr);
		JOptionPane.showMessageDialog(getOwner(), msg, "Species String", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	private boolean lengthOK(String speciesStr) {
		return (speciesStr.length() <= ListeningControl.SPECIES_LENGTH &
				speciesStr.length() > 0);
	}
	
	private void upAction() {
		int r = getSelectedRow();
		if (r < 1) {
			return;
		}
		if (listeningParameters.speciesList == null) {
			return;
		}
		SpeciesItem str = listeningParameters.speciesList.remove(r);
		listeningParameters.speciesList.add(r-1, str);
		table.changeSelection(r-1, 0, false, false);

	}
	private void downAction() {
		int r = getSelectedRow();
		if (listeningParameters.speciesList == null) {
			return;
		}
		int n = listeningParameters.speciesList.size();
		if (r < 0 || r >= n-1) {
			return;
		}
		SpeciesItem str = listeningParameters.speciesList.remove(r);
		listeningParameters.speciesList.add(r+1, str);
		table.changeSelection(r+1, 0, false, false);
	}
	private class TableSelection implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			tableSelectionChange();
		}
	}
	private class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addAction();
		}
	}
	private class RemoveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			removeAction();
		}
	}
	private class EditButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			editAction();
		}
	}
	private class UpButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			upAction();
		}
	}
	private class DownButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			downAction();
		}
	}
	
	private void mouseDblClick(MouseEvent e) {
		int r = table.getSelectedRow();
		int c = table.getSelectedColumn();
		switch(c) {
		case 1:
			editAction();
		case 2:
			symbolAction();
		}
	}
	
	private PamSymbol getDefaultSymbol() {
		return listeningControl.listeningProcess.thingHeardGraphics.getPamSymbol();
	}
	
	private class MouseEvents extends MouseAdapter {
		 @Override
		public void mouseClicked(MouseEvent e){
		      if (e.getClickCount() == 2){
		         	mouseDblClick(e);
		         }
		      }
	}
	
	private class LTableData extends AbstractTableModel {

		private String colNames[] = {"", "Type", "Sym"};
		int[] tableWidths = {2, 150, 3};
		
		@Override
		public String getColumnName(int iCol) {
			return colNames[iCol];
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			if (listeningParameters == null || listeningParameters.speciesList == null) {
				return 0;
			}
			
			return listeningParameters.speciesList.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			PamSymbol pamSymbol;
			switch (iCol) {
			case 0:
				return iRow+1;
			case 1:
				return listeningParameters.speciesList.get(iRow);
			case 2:
				pamSymbol = listeningParameters.speciesList.get(iRow).getSymbol();
				if (pamSymbol == null) {
					pamSymbol = getDefaultSymbol();
				}
				return pamSymbol;
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 2)
				return ImageIcon.class;
			return String.class;
		}

		@Override
		public void fireTableStructureChanged() {
			// TODO Auto-generated method stub
			super.fireTableStructureChanged();
//			System.out.println("Table structure changed fire");
		}

		
	}

}
