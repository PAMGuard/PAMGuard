package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import PamUtils.LatLong;
import videoRangePanel.VRControl;
import videoRangePanel.vrmethods.landMarkMethod.LandMark;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroup;

@SuppressWarnings("serial")
class LandMarkPanel extends AbstractVRTabelPanel{

	protected AbstractTableModel tableData;
	private LandMarkGroup landMarkGroup;
	private Frame frame;
	private VRControl vrControl;

	
	public LandMarkPanel(VRControl vrControl){
		super();
		this.vrControl=vrControl;
		this.add(BorderLayout.NORTH, new JLabel("Select and manage landmarks"));
		this.tableData=new LandMarkTableData();
		super.createPanel(tableData);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		list.setBounds(new Rectangle(10, 10, 600, 100));
		//list.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		addButton.addActionListener(new AddButton());
		editbutton.addActionListener(new EditButton());
		deleteButton.addActionListener(new DeleteButton());
	}
	
	/**
	 * If using landmarks which are angles, rather than GPS co-ordinates then it is vital that each group has an origin lat,long, height (where the angle was taken from) which are the same. This function checks other Landmarks in the group and if any are angle landmarks autiomaticaly sets the latlong for the user. 
	 * @param landMark
	 * @return
	 */
	private LatLong checkforLMOrigin(){
		for (int i=0; i<landMarkGroup.size(); i++){
			if (landMarkGroup.get(i).getLatLongOrigin()!=null){
				return landMarkGroup.get(i).getLatLongOrigin();
			}
		} 
		return null;
	}
	
	
	
	protected LandMark getDialog(LandMark landMark){
		return LandMarkAddDialog.showDialog(frame, vrControl, landMark, checkforLMOrigin());
	}

	class AddButton implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			LandMark newLandMark = getDialog(new LandMark());
			if (getLandMarkList() == null) {
				setLandMarkList(new LandMarkGroup());
			}
			if (newLandMark != null) {
				getLandMarkList().add(newLandMark);
				tableData.fireTableDataChanged();
				int lastRow = getLandMarkList().size()-1;
				list.setRowSelectionInterval(lastRow, lastRow);
			}
		}
		
	}
	
	class EditButton implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int row = list.getSelectedRow();
			if (row >= 0&& row < getLandMarkList().size()) {
				
				LandMark lndmrk = getLandMarkList().get(row);
				LandMark newLandMark = getDialog(lndmrk);
				if (newLandMark != null) {
					lndmrk.update(newLandMark);
					tableData.fireTableDataChanged();
					list.setRowSelectionInterval(row, row);
				}
			}
		}
	}
	
	class DeleteButton implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int row = list.getSelectedRow();
			if (getLandMarkList() == null) return;
			if (row < 0 || row >= getLandMarkList().size()) {
				return;
			}
			getLandMarkList().remove(row);
			tableData.fireTableDataChanged();
		}
	}
	
	
	protected  LandMarkGroup getLandMarkList(){
		return landMarkGroup;
	}
	
	
	protected  void  setLandMarkList(LandMarkGroup landMarkGroup){
		this. landMarkGroup=landMarkGroup;
	}
	
	
	public void setParams() {
		tableData.fireTableDataChanged();
	}
	
	class LandMarkTableData extends AbstractTableModel {

		public int getColumnCount() {
			return 9;
		}

		public int getRowCount() {
			if (landMarkGroup == null) {
				return 0;
			}
			return landMarkGroup.size();
		}
		
		private String[] columnNames = {"Name", "Lat", "Long","Height","Bearing","Pitch","Origin Lat","Origin Long","Origin Height"};
		
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (landMarkGroup == null) {
				return null;
			}
			LandMark cd = landMarkGroup.get(rowIndex);
			if (cd == null) {
				return "Unknown";
			}
			switch (columnIndex) {
			case 0:
				return cd.getName();
			case 1:
				if (cd.getPosition()==null) return null;
				return cd.getPosition().getLatitude();
			case 2:
				if (cd.getPosition()==null) return null;
				return cd.getPosition().getLongitude();
			case 3:
				if (cd.getPosition()==null) return null;
				return cd.getPosition().getHeight();
			case 4:
				return cd.getBearing();
			case 5:
				return cd.getPitch();
			case 6:
				if (cd.getLatLongOrigin()==null) return null;
				return cd.getLatLongOrigin().getLatitude();
			case 7:
				if (cd.getLatLongOrigin()==null) return null;
				return cd.getLatLongOrigin().getLongitude();
			case 8:
				if (cd.getLatLongOrigin()==null) return null;
				return cd.getLatLongOrigin().getHeight();
			}
			return null;
		}

	}
	
}