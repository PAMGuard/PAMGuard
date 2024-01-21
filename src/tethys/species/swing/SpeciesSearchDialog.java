package tethys.species.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.tables.SwingTableColumnWidths;
import PamView.tables.TableColumnWidthData;
import tethys.TethysControl;
import tethys.species.ITISFunctions;
import tethys.species.SpeciesMapItem;

public class SpeciesSearchDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private TethysControl tethysControl;
	
	private SpeciesMapItem selectedItem;

	private static SpeciesSearchDialog singleInstance;
	
	private JTextField searchText;
	
	private JButton searchButton;
	
	private JTable resultTable;

	private ArrayList<SpeciesMapItem> speciesMapItems;

	private DataModel tableModel;
	
	private int selectedRow = -1;
	

	private SpeciesSearchDialog(Window parentFrame, TethysControl tethysControl) {
		super(parentFrame, "Species search", false);
		this.tethysControl = tethysControl;
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Search Term"));
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		topPanel.add(new JLabel("Latin or common name ", JLabel.RIGHT), c);
		c.gridx++;
		topPanel.add(searchText = new JTextField(12), c);
		c.gridx++;
		topPanel.add(searchButton = new JButton("search"), c);
		mainPanel.add(BorderLayout.NORTH, topPanel);
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchTethys();
			}

		});
		
		tableModel = new DataModel();
		resultTable = new JTable(tableModel);
		JPanel centPanel = new JPanel(new BorderLayout());
		centPanel.add(BorderLayout.NORTH, new JLabel("Possible matches", JLabel.LEFT));
		JScrollPane scrollPane = new JScrollPane(resultTable);
		centPanel.add(BorderLayout.CENTER, scrollPane);
		mainPanel.add(BorderLayout.CENTER, centPanel);
		
		resultTable.addMouseListener(new TableMouse());
		new SwingTableColumnWidths("Species Search Dialog Table", resultTable);
		
		setResizable(true);
		setDialogComponent(mainPanel);
	}
	public static SpeciesMapItem showDialog(Window parentFrame, TethysControl tethysControl) {
		singleInstance = new SpeciesSearchDialog(parentFrame, tethysControl);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.selectedItem;
	}

	
	private void searchTethys() {
		clearResults();
		String str = searchText.getText();
		if (str == null || str.length() == 0) {
			return;
		}
		ITISFunctions itisFunctions = tethysControl.getItisFunctions();
		speciesMapItems = itisFunctions.searchSpecies(str);
		tableModel.fireTableDataChanged();
	}
	
	private void setParams() {
		searchText.setText(null);
		clearResults();
	}

	private void clearResults() {
		selectedRow = -1;
		speciesMapItems = null;
		selectedItem = null;
	}
	@Override
	public boolean getParams() {
		if (selectedItem == null) {
			return showWarning("You must select a row from the table of species");
		}
		return selectedItem != null;
	}

	@Override
	public void cancelButtonPressed() {
		clearResults();
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class TableMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			selectedRow = resultTable.getSelectedRow();
			if (selectedRow >= 0) {
				selectedItem = speciesMapItems.get(selectedRow);
			}
			tableModel.fireTableDataChanged();
		}
		
	}
	private class DataModel extends AbstractTableModel {
		
		private String[] colNames = {"Select", "TSN", "Name", "Common Name"};

		@Override
		public int getRowCount() {
			if (speciesMapItems == null) {
				return 0;
			}
			return speciesMapItems.size();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SpeciesMapItem mapItem = speciesMapItems.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return rowIndex == selectedRow;
			case 1:
				return mapItem.getItisCode();
			case 2:
				return mapItem.getLatinName();
			case 3:
				return mapItem.getCommonName();
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
	}

}
