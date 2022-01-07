package AIS;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;

public class AISStringsTable {

	static AISStringsTable singleInstance;
	
	AISDataBlock aisDataBlock;

	private int[] colWidths = {250, 100, 250, 200, 80, 200, 120, 120};
	private String[] tableColumns = {"Last update", "mmsi", "Name", "Type", 
			"LOA", "Destination", "Lat", "Long", "CSE", "SPD"};
	
	JFrame frame;
	
	JPanel mainPanel;
	
	JTable nmeaTable;
	
	Timer timer;
	
	AISTableData aisTableData;
	
	private AISStringsTable() {

		frame = new AISFrame("AIS Vessel List");
//		frame.setSize(800, 200);
		frame.setLocation(100, 300);
		frame.setAlwaysOnTop(true);
		// frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//		frame.addWindowListener(this);

		mainPanel = new JPanel(new GridLayout(1, 0));
		mainPanel.setOpaque(true);

		aisTableData = new AISTableData();
		nmeaTable = new JTable(aisTableData);
		nmeaTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		int nCol = Math.min(aisTableData.getColumnCount(), colWidths.length);
		for (int i = 0; i < nCol; i++) {
			nmeaTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
		}
//		nmeaTable.getColumnModel().getColumn(0).setPreferredWidth(250);
//		nmeaTable.getColumnModel().getColumn(1).setPreferredWidth(100);
//		nmeaTable.getColumnModel().getColumn(2).setPreferredWidth(250);
//		nmeaTable.getColumnModel().getColumn(3).setPreferredWidth(200);
//		nmeaTable.getColumnModel().getColumn(4).setPreferredWidth(80);
//		nmeaTable.getColumnModel().getColumn(5).setPreferredWidth(200);
//		nmeaTable.getColumnModel().getColumn(6).setPreferredWidth(120);
//		nmeaTable.getColumnModel().getColumn(7).setPreferredWidth(120);
		

		JScrollPane scrollPanel = new JScrollPane(nmeaTable);
		mainPanel.add(scrollPanel);

		mainPanel.setPreferredSize(new Dimension(900, 200));

		frame.setContentPane(mainPanel);

		frame.pack();

		timer = new Timer(500, new TimerListener());
		timer.setInitialDelay(10);
	}
	
	class AISFrame extends JFrame {
		// so I can override set visible !
		public AISFrame(String title) throws HeadlessException {
			super(title);
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#setVisible(boolean)
		 */
		@Override
		public void setVisible(boolean b) {
			if (b) timer.start();
			else timer.stop();
			super.setVisible(b);
		}
		
	}
	
	static public void show(AISControl aisControl) {
		if (singleInstance == null) {
			singleInstance = new AISStringsTable();
		}
		
		singleInstance.aisDataBlock = aisControl.aisProcess.getOutputDataBlock();
		
		singleInstance.frame.setVisible(true);
	}

	class TimerListener implements ActionListener {
		boolean doneLayout;
		public void actionPerformed(ActionEvent ev) {
			// table.
//			nmeaTableData.fireTableRowsUpdated(0, 10);
			aisTableData.fireTableDataChanged();
			
			if (doneLayout == false && aisTableData.getRowCount() > 0) {
				doneLayout = true;
			}
		}
	}

	class AISTableData extends AbstractTableModel {

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			// TODO Auto-generated method stub
			return tableColumns[column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return tableColumns.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			if (aisDataBlock == null) return 0;
			//System.out.println(aisDataBlock.getUnitsCount() + " rows in AIS data");
			return aisDataBlock.getUnitsCount();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			AISDataUnit aisDataUnit = aisDataBlock.getDataUnit(rowIndex, PamDataBlock.REFERENCE_CURRENT);
			
			AISStaticData staticData = aisDataUnit.getStaticData();
			AISPositionReport positionReport = aisDataUnit.getPositionReport();
			String str = null;
			switch(columnIndex) {
			case 0: // time
				return PamCalendar.formatDateTime(aisDataUnit.getTimeMilliseconds());
			case 1:
				return aisDataUnit.mmsiNumber;
				//return ((StringBuffer) pamDataUnit.data).substring(1,6);
			case 2:
				if (staticData != null) return staticData.shipName;
				break;
			case 3:
				
				if (staticData != null) str = staticData.getStationTypeString(aisDataUnit.stationType, staticData.shipType);
				if (str == null) str = aisDataUnit.stationType.toString();
				return str;
			case 4:
				if (staticData != null) return staticData.dimA + staticData.dimB + "m";
				break;
			case 5:
				if (staticData != null) return staticData.destination;
				break;
			case 6:
				if (positionReport != null) return LatLong.formatLatitude(positionReport.getLatitude());
				break;
			case 7:
				if (positionReport != null) return LatLong.formatLongitude(positionReport.getLongitude());
				break;
			case 8:
				if (positionReport != null) return String.format("%.1f", positionReport.courseOverGround);
				break;
			case 9:
				if (positionReport != null) return String.format("%.1f", positionReport.speedOverGround);
				break;
			}
			return null;
		}
		
	}

}
