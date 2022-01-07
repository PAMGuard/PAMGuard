/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector.ClickClassifiers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import clickDetector.BasicClickIdParameters;
import clickDetector.ClickTypeParams;
import clickDetector.ClickClassifiers.basic.ClickTypeDialog;
import PamView.PamTable;
import PamView.dialog.PamDialog;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Dialog for displaying a list of multiple click types. This dialog
 *         does not do much apart from display a list of types, but it can
 *         create a ClickTypeDialog to add or modify click types in the list.
 * @see clickDetector.ClickClassifiers.basic.ClickTypeDialog ClickTypeDialog
 */
public class ClickTypesDialog extends PamDialog implements ActionListener,
		ListSelectionListener {
	
	static ClickTypesDialog clickTypesDialog;

	float sampleRate;

	private BasicClickIdParameters basicClickIdParameters;

	private JButton upButton, downButton;

	private JButton newButton, editButton, deleteButton;

	private JCheckBox runOnline;

	private JTable typesTable;

	private ClickTypesTableData tableData;

	private String[] columnNames = { "Symbol", "Type", "Code" };
	

	private ClickTypesDialog(Frame parentFrame) {
		
		super(parentFrame, "Click Classification", false);

		tableData = new ClickTypesTableData();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setOpaque(true);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout());

		JPanel pc = new JPanel();
		typesTable = new JTable(tableData);
		typesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		typesTable.getSelectionModel().addListSelectionListener(this);
		tableData.addTableModelListener(typesTable);
		TableColumn column = null;
		for (int i = 0; i < typesTable.getColumnCount(); i++) {
			column = typesTable.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(22); // sport column is bigger
			} else if (i == 2) {
				column.setPreferredWidth(20);
			}
		}
		//
		// // check to see that there is an unassigned type ...
		// if ()

		JScrollPane scrollPanel = new JScrollPane(typesTable);
		scrollPanel.setPreferredSize(new Dimension(290, 150));
		// pc.add(typesTable);
		panel.add(BorderLayout.CENTER, scrollPanel);

		JPanel ps = new JPanel();
		ps.setLayout(new BoxLayout(ps, BoxLayout.X_AXIS));
		ps.add(newButton = new JButton("New"));
		ps.add(editButton = new JButton("Edit"));
		ps.add(deleteButton = new JButton("Delete"));
		newButton.addActionListener(this);
		editButton.addActionListener(this);
		deleteButton.addActionListener(this);
		panel.add(BorderLayout.SOUTH, ps);

		JPanel n = new JPanel();
		n.setLayout(new BorderLayout());
		n.setBorder(new EmptyBorder(10, 10, 10, 10));
		n.add(BorderLayout.WEST, runOnline = new JCheckBox("Run Online"));

		JPanel e = new JPanel();
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		e.add(upButton = new JButton(" Up "));
		e.add(downButton = new JButton("Down"));
		upButton.addActionListener(this);
		downButton.addActionListener(this);

		mainPanel.add(BorderLayout.NORTH, n);
		panel.add(BorderLayout.EAST, e);
		mainPanel.add(BorderLayout.CENTER, panel);

		setDialogComponent(mainPanel);
		
		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickClassification");

	}

	public static BasicClickIdParameters showDialog(Frame parentFrame,
			BasicClickIdParameters clickParameters, float sampleRate) {
		if (clickTypesDialog == null || clickTypesDialog.getOwner() != parentFrame) {
			clickTypesDialog = new ClickTypesDialog(parentFrame);
		}
		clickTypesDialog.setParameters(clickParameters, sampleRate);

		clickTypesDialog.setVisible(true);

		return clickTypesDialog.basicClickIdParameters;
	}

	void setParameters(BasicClickIdParameters clickParmeters, float sampleRate) {

		this.basicClickIdParameters = clickParmeters.clone();

//		runOnline.setSelected(basicClickIdParameters.runClickIdOnline
//				&& basicClickIdParameters.clickTypeParams.size() > 0);

		updateTable();

		enableButtons();
	}

	@Override
	public boolean getParams() {
//		basicClickIdParameters.runClickIdOnline = runOnline.isSelected();
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		basicClickIdParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == upButton) {
			int r = typesTable.getSelectedRow();
			ClickTypeParams p;
			if (r >= 1) {
				p = basicClickIdParameters.clickTypeParams.get(r);
				basicClickIdParameters.clickTypeParams.remove(r);
				basicClickIdParameters.clickTypeParams.add(--r, p);
				tableData.fireTableDataChanged();
				typesTable.setRowSelectionInterval(r, r);
			}
		} else if (e.getSource() == downButton) {
			int r = typesTable.getSelectedRow();
			ClickTypeParams p;
			if (r < typesTable.getRowCount() - 1) {
				p = basicClickIdParameters.clickTypeParams.get(r);
				basicClickIdParameters.clickTypeParams.remove(r);
				basicClickIdParameters.clickTypeParams.add(++r, p);
				tableData.fireTableDataChanged();
				typesTable.setRowSelectionInterval(r, r);
			}
		} else if (e.getSource() == newButton) {
			ClickTypeParams p = new ClickTypeParams(basicClickIdParameters
					.getFirstFreeClickIdentifier());
			if ((p = ClickTypeDialog.showDialog((Frame) getOwner(), basicClickIdParameters, p)) != null) {
				basicClickIdParameters.clickTypeParams.add(p);
				tableData.fireTableDataChanged();
				updateTable();
			}
		} else if (e.getSource() == editButton) {
			int r = typesTable.getSelectedRow();
			if (r >= 0) {
				ClickTypeParams p;
				if ((p = ClickTypeDialog.showDialog((Frame) getOwner(), basicClickIdParameters, 
						basicClickIdParameters.clickTypeParams.get(r))) != null) {
					basicClickIdParameters.clickTypeParams.remove(r);
					basicClickIdParameters.clickTypeParams.add(r, p);
					tableData.fireTableDataChanged();
					updateTable();
				}
			}
		} else if (e.getSource() == deleteButton) {
			int r = typesTable.getSelectedRow();
			if (r >= 0) {
				basicClickIdParameters.clickTypeParams.remove(r);
				tableData.fireTableDataChanged();
			}
		}

		enableButtons();

	}

	public void valueChanged(ListSelectionEvent e) {
		// ListSelectionModel lsm =
		// (ListSelectionModel)e.getSource();
		enableButtons();
	}

	private void enableButtons() {
		int row = typesTable.getSelectedRow();
		int rows = typesTable.getRowCount();
		runOnline.setEnabled(rows > 0);
		deleteButton.setEnabled(row >= 0);
		editButton.setEnabled(row >= 0);
		upButton.setEnabled(row >= 1);
		downButton.setEnabled(row >= 0 && row < rows - 1);

	}

	public void updateTable() {
		// typesTable.setModel(tableData);
		tableData.fireTableRowsUpdated(0, tableData.getRowCount() - 1);
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

		public synchronized int getRowCount() {
			if (basicClickIdParameters == null)
				return 0;
			return basicClickIdParameters.clickTypeParams.size();
		}

		public synchronized int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return ImageIcon.class;
			return getValueAt(0, columnIndex).getClass();

		}

		public synchronized Object getValueAt(int row, int col) {
			/*
			 * need to find the right process by going through and seeing which
			 * one our block is in for the given row
			 */
			if (row < basicClickIdParameters.clickTypeParams.size()) {
				switch (col) {
				case 0:
					return basicClickIdParameters.clickTypeParams.get(row).symbol;
				case 1:
					return basicClickIdParameters.clickTypeParams.get(row).getName();
				case 2:
					return basicClickIdParameters.clickTypeParams.get(row).getSpeciesCode();
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
}
