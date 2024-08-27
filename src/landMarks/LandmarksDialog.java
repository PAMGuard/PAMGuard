package landMarks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import generalDatabase.DBControlUnit;

public class LandmarksDialog extends PamDialog {

	private static LandmarksDialog singleInstance;
	private LandmarkControl landmarkControl;
	private LandmarkDatas landmarkDatas;
	
	Window parentFrame;
	private JTable table;
	private LandmarkDataModel landmarkDataModel;
	private JButton deleteButton, addButton, editbutton;
	private JTextField tableName;
	private JButton dbImport;
//	private JTextField dbLink;
	
	private LandmarksDialog(Window parentFrame, LandmarkControl landmarkControl) {
		super(parentFrame, landmarkControl.getUnitName(), false);
		this.landmarkControl = landmarkControl;
		this.parentFrame = parentFrame;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		table = new JTable(landmarkDataModel = new LandmarkDataModel());
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(230, 130));
		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.setBorder(new TitledBorder("Objects"));
		tablePanel.add(BorderLayout.CENTER, scrollPane);
		panel.add(BorderLayout.CENTER, tablePanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(addButton = new JButton("Add"));
		addButton.addActionListener(new AddButton());
		buttonPanel.add(editbutton = new JButton("Edit"));
		editbutton.addActionListener(new EditButton());
		buttonPanel.add(deleteButton = new JButton("Delete"));
		deleteButton.addActionListener(new DeleteButton());
		panel.add(BorderLayout.SOUTH, buttonPanel);
		
		JPanel databasePanel = new JPanel();
		databasePanel.setBorder(new TitledBorder("Database"));
		panel.add(BorderLayout.NORTH, databasePanel);
		databasePanel.setLayout(new BorderLayout());
//		GridBagConstraints c = new PamGridBagContraints();
		databasePanel.add(tableName = new JTextField(40), BorderLayout.CENTER);
//		c.gridx++;
		databasePanel.add(dbImport = new JButton("Import"), BorderLayout.EAST);
		dbImport.addActionListener(new DBImport());
		
		setDialogComponent(panel);
		setResizable(true);
	}

	public static LandmarkDatas showDialog(Window parentFrame, LandmarkControl landmarkControl) {
		if (singleInstance == null || singleInstance.landmarkControl != landmarkControl || 
				singleInstance.getOwner() != parentFrame) {
			singleInstance = new LandmarksDialog(parentFrame, landmarkControl);
		}
		
		singleInstance.landmarkDatas = landmarkControl.landmarkDatas.clone();
		singleInstance.landmarkDataModel.fireTableDataChanged();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.landmarkDatas;
	}
	
	private void setParams() {
		tableName.setText(this.landmarkDatas.tableName);
		enableControls();
	}

	private void enableControls() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		tableName.setEnabled(dbControl != null);
		String tN = tableName.getText();
		dbImport.setEnabled(dbControl != null);
	}

	@Override
	public void cancelButtonPressed() {
		landmarkDatas = null;
	}

	@Override
	public boolean getParams() {
		landmarkDatas.tableName = tableName.getText();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	class DeleteButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			if (row < 0 || row >= landmarkDatas.size()) {
				return;
			}
			landmarkDatas.remove(row);
			landmarkDataModel.fireTableDataChanged();
		}
	}
	
	class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			LandmarkData newLandmark = LandmarkDialog.showDialog(parentFrame, landmarkControl, null);
			if (newLandmark != null) {
				if (landmarkDatas == null) {
					landmarkDatas = new LandmarkDatas();
				}
				landmarkDatas.add(newLandmark);
				landmarkDataModel.fireTableDataChanged();
			}
		}
	}
	
	class EditButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			if (row < 0 || row >= landmarkDatas.size()) {
				return;
			}
			LandmarkData landmarkData = landmarkDatas.get(row);
			LandmarkData modifiedData = LandmarkDialog.showDialog(parentFrame, landmarkControl, landmarkData);
			if (modifiedData != null) {
				landmarkDatas.replace(landmarkData, modifiedData);
				landmarkDataModel.fireTableDataChanged();
			}
//			VRHeightData heightData = localHeightList.get(row);
//			VRHeightData newData = HeightDialog.showDialog(null, heightData);
//			if (newData != null) {
//				heightData.update(newData);
//				tableData.fireTableDataChanged();
//			}
			
		}
	}
	
	class DBImport implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			databaseImport();
		}
		
	}
	
	class LandmarkDataModel extends AbstractTableModel {

		String[] colNames = {"Symbol", "Name", "Latitude", "Longitude", "Height"};
		
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			if (landmarkDatas == null) {
				return 0;
			}
			return landmarkDatas.size();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return ImageIcon.class;
			}
			else {
				return String.class;
			}
//			return getValueAt(0, columnIndex).getClass();

		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (landmarkDatas == null) {
				return null;
			}

			LandmarkData ld = landmarkDatas.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return ld.symbol;
			case 1:
				return ld.name;
			case 2:
				return LatLong.formatLatitude(ld.latLong.getLatitude());
			case 3:
				return LatLong.formatLongitude(ld.latLong.getLongitude());
			case 4:
				return ld.height;
			}
			return null;
		}
		
	}

	public void databaseImport() {
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}
		DatabaseImport dbi = new DatabaseImport(landmarkControl);
		String tN = tableName.getText();
		if (tN == null || tN.length() == 0) {
			return;
		}
		if (!dbi.checkTable(tN)) {
			return;
		}
		ArrayList<LandmarkData> newData = dbi.readDatas(tN);
		if (newData != null) {
			this.landmarkDatas.setData(newData);
			landmarkDataModel.fireTableDataChanged();
		}
	}

}
