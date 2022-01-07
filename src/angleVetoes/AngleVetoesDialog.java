package angleVetoes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import PamView.PamTable;
import PamView.dialog.PamDialog;

/**
 * Dialog to manage multiple angle vetoes.
 * 
 * @author Douglas Gillespie
 * @see AngleVetoDialog
 * @see AngleVetoes
 *
 */
public class AngleVetoesDialog extends PamDialog {

	private AngleVetoes angleVetoes;
	
	private AngleVetoParameters angleVetoParameters;
	
	static private AngleVetoesDialog singleInstance;
	
	private AbstractTableModel tableData;
	private PamTable list;
	private JButton addButton, editbutton, deleteButton;
	
	private AngleVetoesDialog(Frame parentFrame, AngleVetoes angleVetoes) {
		super(parentFrame, angleVetoes.getUnitName() + " Angle Vetoes", false);
		this.angleVetoes = angleVetoes;
		angleVetoParameters = angleVetoes.getAngleVetoParameters();
		tableData = new TableModel();
		
		JPanel p = new JPanel();

		p.setLayout(new BorderLayout());
		list = new PamTable(tableData);
		list.setRowSelectionAllowed(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(1, 130));
		p.add(BorderLayout.CENTER, scrollPane);
		
		p.add(BorderLayout.NORTH, new JLabel("Create and manage angle vetoes"));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(addButton = new JButton("Add"));
		buttonPanel.add(editbutton = new JButton("Edit"));
		buttonPanel.add(deleteButton = new JButton("Delete"));
		addButton.addActionListener(new AddButton());
		editbutton.addActionListener(new EditButton());
		deleteButton.addActionListener(new DeleteButton());
		p.add(BorderLayout.SOUTH, buttonPanel);
		
		this.setResizable(true);
		
		setDialogComponent(p);
		
	}
	
	static public AngleVetoParameters showDialog(Frame parentFrame, AngleVetoes angleVetoes) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || angleVetoes != singleInstance.angleVetoes) {
			singleInstance = new AngleVetoesDialog(parentFrame, angleVetoes);
		}
		singleInstance.angleVetoParameters = angleVetoes.getAngleVetoParameters();
		
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.angleVetoParameters;
	}
	
	@Override
	public void cancelButtonPressed() {
		angleVetoParameters = null;
	}

	private void setParams() {
		tableData.fireTableDataChanged();
	}
	
	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
	class AddButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			AngleVeto newVeto = AngleVetoDialog.showDialog((Frame)getOwner(), null);
			if (newVeto != null) {
				angleVetoParameters.addVeto(newVeto);
				tableData.fireTableDataChanged();
			}
		}
	}
	class EditButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int row = list.getSelectedRow();
			if (row >= 0 && row < angleVetoParameters.getVetoCount()) {
				AngleVeto angleVeto = angleVetoParameters.getVeto(row);
				angleVeto = AngleVetoDialog.showDialog((Frame)getOwner(), angleVeto);
				if (angleVeto != null) {
					angleVetoParameters.replaceVeto(row, angleVeto);
					tableData.fireTableDataChanged();
				}
			}
		}
	}
	class DeleteButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int row = list.getSelectedRow();
			if (row >= 0 && row < angleVetoParameters.getVetoCount()) {
				angleVetoParameters.removeVeto(row);
				tableData.fireTableDataChanged();
			}
		}
	}

	class TableModel extends AbstractTableModel {
		
		String[] columnNames = {"Start", "End"};//, "Channels"};

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			if (angleVetoParameters == null) {
				return 0;
			}
			return angleVetoParameters.getVetoCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			AngleVeto angleVeto = angleVetoParameters.getVeto(rowIndex);
			if (angleVeto == null) {
				return null;
			}
			switch (columnIndex) {
			case 0:
				return String.format("%.1f\u00B0", angleVeto.startAngle);
			case 1:
				return String.format("%.1f\u00B0", angleVeto.endAngle);
			case 2:
				return "chans";
			}
			return null;
		}
		
	}
}
