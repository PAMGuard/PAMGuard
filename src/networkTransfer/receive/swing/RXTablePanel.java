package networkTransfer.receive.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import Acquisition.DaqStatusDataUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamTable;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import networkTransfer.receive.BuoyStatusDataBlock;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.BuoyStatusValue;
import networkTransfer.receive.NetworkReceiver;
import networkTransfer.receive.PairedValueInfo;

@Deprecated // Use RXTablePanel 2.  
public class RXTablePanel {

	private NetworkReceiver networkReceiver; 
	private BuoyStatusDataBlock buoyStatusDataBlock;

	private JPanel mainPanel;

	private TablePanel tablePanel;

	private JTable buoyTable;

	private TableDataModel buoyTableData;
	private SwingTableColumnWidths columnWidths;

	private RXTablePanel(NetworkReceiver networkReceiver, BuoyStatusDataBlock buoyStatusDataBlock) {
		super();
		this.networkReceiver = networkReceiver;
		this.buoyStatusDataBlock = buoyStatusDataBlock;

		buoyTableData = new TableDataModel();
		buoyTableData.findColumn("RSSI");
		buoyTable = new RXTable(buoyTableData);

		buoyTable.setAutoCreateRowSorter(true);

		//		buoyTable.getColumnModel().  getColumn(0).setCellRenderer();
		mainPanel = new JPanel(new BorderLayout());
		tablePanel = new TablePanel();
		mainPanel.add(BorderLayout.CENTER, tablePanel);
		//		setColumnWidths();
		columnWidths = new SwingTableColumnWidths(networkReceiver.getUnitName()+"maintable", buoyTable);

		//		new SwingTableColumnWidths(networkReceiver.getUnitName(), buoyTable);

		//		Timer t = new Timer(2000, new TimerAction());
		//		t.start();
		buoyStatusDataBlock.addObserver(new DataObs());
	}

	private class DataObs extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Network Receiver display";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			buoyTableData.fireTableDataChanged();
		}

	}

	public JComponent getComponent() {
		return mainPanel;
	}



	//	private void setColumnWidths() {
	//		for (int i = 0; i < buoyTableData.getColumnCount(); i++) {
	//			buoyTable.getColumnModel().getColumn(i).setPreferredWidth(buoyTableData.getColWidth(i));
	//		}		
	//	}

	private class RXTable extends PamTable {

		public RXTable(TableModel dm) {
			super(dm);
			setDefaultRenderer(Object.class	, new RXTableRenderer());
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			java.awt.Point p = e.getPoint();
			int rowIndex = rowAtPoint(p);
			int colIndex = columnAtPoint(p);
			if (colIndex < 0) {
				return null;
			}
			else if (rowIndex < 0) {
				return buoyTableData.getColumnTipText(colIndex);
			}
			else {
				return buoyTableData.getToolTipText(rowIndex, colIndex);
			}
		}

		//		@Override
		//		public TableCellRenderer getCellRenderer(int row, int column) {
		//			Integer col = buoyTableData.getExtraInfoIndex(column);
		//			if (col != null) {
		//				Class colClass = networkReceiver.getExtraTableInfo().get(col).getTableClass();
		//				if (colClass != null) {
		//					return getDefaultRenderer(colClass);
		//				}
		//			}
		//			return super.getCellRenderer(row, column);
		//		}

	}

	private class RXTableRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Integer col = buoyTableData.getExtraInfoIndex(column);
			if (col != null) {
				Class colClass = networkReceiver.getExtraTableInfo().get(col).getTableClass();
				if (Component.class.isAssignableFrom(colClass)) {
					return (Component) value;
				}
			}

			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}

	}


	private class TablePanel extends JPanel {
		private TablePanel(){
			setLayout(new BorderLayout());
			JScrollPane scrollPane = new JScrollPane(buoyTable);
			this.add(BorderLayout.CENTER, scrollPane);
		}
	}

	//	private class TimerAction implements ActionListener {
	//		String [] genericDataList;
	//		@Override
	//		public void actionPerformed(ActionEvent arg0) {
	//			
	//			if (networkReceiver.getBuoyStatusDataBlock().getBuoyGenericDataChanged()){
	//				//genericDataList = networkReceiver.getBuoyStatusDataBlock().getBuoyGenericDataList();
	//				//buoyTableData.fireTableStructureChanged();
	//				notifyModelChanged(0);
	//				//buoyTable.columnAdded(e);
	//				int rI = buoyTableData.findColumn("RSSI");
	//				//buoyTable.getColumnModel().getColumn(rI);
	//				TableColumn col = null;
	//				//System.out.println("RSSI Column Index = "+rI);
	//				
	//				if (rI!=-1) {
	//					//col= buoyTable.getColumn(rI);
	//					col=buoyTable.getColumnModel().getColumn(rI);
	//				}
	////				if (col!=null) col.setCellRenderer(new MyNumberRenderer(-100.0, .0));
	//				
	//			}else{
	//				buoyTableData.fireTableDataChanged();
	//			}
	//			
	//		}
	//	}

	class MyMinMaxRenderer<T extends Comparable> extends DefaultTableCellRenderer {

		//		private DecimalFormat formatter = new DecimalFormat( "#0.00" );
		private Comparable min;
		private Comparable max;

		/**
		 * 
		 */
		public MyMinMaxRenderer(Comparable d, Comparable e) {
			this.min=d;
			this.max=e;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			//	        if(column==1) value = formatter.format((Number)value);        
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			//		    if (isSelected) {
			//		    	cell.setBackground(Color.green);
			//		    } else {
			Double myVal=null;
			try{
				myVal = Double.valueOf(value.toString());
			}catch (NumberFormatException e){
				return cell;
			}
			boolean comparable = Comparable.class.isAssignableFrom(value.getClass()); //have if statement that if not object try < and > ??
			if ( (min!=null && comparable &&  min.compareTo(value) > 0 ) |    //(value).compare(min)  #or#  myVal<min) | ( max!=null && myVal>max)){
					(max!=null && comparable &&  max.compareTo(value) < 0 )  ){
				Color curCol = cell.getBackground();
				//cell.setBackground(new Color(curCol.getRed(),curCol.getGreen()/2,curCol.getBlue()/2)); // redder
				if (isSelected){
					cell.setBackground(new Color(255,128,128));
				}else{
					//	        		default selected = 51,153,255
					cell.setBackground(new Color(224,158,160)); // 210,117,119

				}
			}
			return cell; 
		}
	}

	private class TableDataModel extends AbstractTableModel {

		TableDataModel() {		

		}

		String[] colNames1 = {"Station Id","IP Addr", "Channel", "Status", };
		String[] colNames2 = {"Last Data", "Position", "Tot' Packets"};
		//		int[] colWidths = {   50,          70,        50,        50,       50,     50,       50,      100,        200,         40};
		//		int nStandardCols = colNames1.length;
		//		private String[] genericDataList;

		@Override
		public int getColumnCount() {

			int n = colNames1.length + colNames2.length;
			//			genericDataList = rxStats.getBuoyGenericDataList();
			n += networkReceiver.getExtraTableInfo().size();

			if (networkReceiver.getRxDataBlocks() != null) {
				n += networkReceiver.getRxDataBlocks().size();
			}
			return n;
		}

		@Override
		public int getRowCount() {
			return buoyStatusDataBlock.getUnitsCount();
		}

		//		public int getColWidth(int iCol) {
		//			if (iCol < nStandardCols) {
		//				return colWidths[iCol];
		//			}
		//			else return 40;
		//		}

		public String getColumnTipText(int colIndex) {
			//			if (colIndex < nStandardCols) {
			//				return null;
			//			}
			//			try {
			//			if (networkReceiver.getRxDataBlocks() != null) {
			//				PamDataBlock dataBlock = networkReceiver.getRxDataBlocks().get(colIndex-nStandardCols);
			//				return dataBlock.getLongDataName();
			//			}
			//			}
			//			catch (Exception e) {		
			//			}
			return null;
		}

		public String getToolTipText(int rowIndex, int column) {
			BuoyStatusDataUnit b = buoyStatusDataBlock.getDataUnit(rowIndex, PamDataBlock.REFERENCE_ABSOLUTE);
			if (b == null) {
				return null;
			}

			//			int baseCols = nStandardCols;
			//			if (genericDataList != null) {
			//				baseCols += genericDataList.length;
			//			}
			if (column < colNames1.length) {
				// basic information about the sender. 
				String str = String.format("<html>Device %d(%d)<br>Last data %s<br>Recieving since %s", b.getBuoyId1(), b.getBuoyId2(),
						PamCalendar.formatDateTime(b.getLastDataTime()), PamCalendar.formatDateTime(b.getCreationTime()));
				DaqStatusDataUnit daqStatus = (DaqStatusDataUnit) b.findLastDataUnit(DaqStatusDataUnit.class);
				if (daqStatus != null) {
					Long ppsMillis = daqStatus.getGpsPPSMilliseconds();
					if (ppsMillis != null) {
						str += String.format("<br>Clock offset from GPS PPS = %5.3fs", (double)(daqStatus.getTimeMilliseconds()-ppsMillis)/1000.);
					}
				}
				str += "</html>";
				return str;
			}
			Integer bCol = getDatablockIndex(column);
			if (bCol != null) {
				String str = String.format("<html>Device %d(%d)", b.getBuoyId1(), b.getBuoyId2());

				int nPackets = 0;//b.getBlockPacketCount(bCol);
				PamDataBlock dataBlock = networkReceiver.getRxDataBlocks().get(bCol);
				str += String.format("<br>Module: %s<br>Process: %s<br>Stream: %s<br>%d packets received", 
						dataBlock.getParentProcess().getPamControlledUnit().getUnitName(),
						dataBlock.getParentProcess().getProcessName(), dataBlock.getDataName(), nPackets);
				if (nPackets > 0) {
					PamDataUnit lastData = b.findLastDataUnit(dataBlock.getUnitClass());
					if (lastData != null) {
						str += "<br>Last data: " + lastData.getSummaryString();
						//							str += String.format("<br>Last packet %s", PamCalendar.formatDateTime(lastData.getLastChangeTime()));
					}
				}
				str += "</html>";
				return str;
			}
			bCol = getExtraInfoIndex(column);
			if (bCol != null) {
				PairedValueInfo pairInfo = networkReceiver.getExtraTableInfo().get(bCol);
				BuoyStatusValue data = b.getPairVal(pairInfo.getPairName());
				if (data != null) {
					return String.format("%s: %s updated at %s", pairInfo.getPairName(), pairInfo.formatTableData(b, data), 
							PamCalendar.formatDateTime(data.getTimemillis()));
				}
				else {
					// make a list of available value pairs. 
					Set<String> pairKeys = b.getPairKeys();
					if (pairKeys == null || pairKeys.size() == 0) {
						return String.format("No paired value data available for station %d(%d)", b.getBuoyId1(), b.getBuoyId2());
					}
					else {
						String str = String.format("<html>Station %d(%d) has pair data values:", b.getBuoyId1(), b.getBuoyId2());
						Iterator<String> it = pairKeys.iterator();
						while (it.hasNext()) {
							String key = it.next();
							Object val = b.getPairVal(key);
							if (val == null) {
								val = "null";
							}
							str += String.format("<br>%s: %s", key, val.toString());
						}
						str += "</html>";
					}
				}

			}
			return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			BuoyStatusDataUnit b = buoyStatusDataBlock.getDataUnit(row, PamDataBlock.REFERENCE_ABSOLUTE);
			long t;

			Integer col = getCols1Index(column);
			if (col != null){
				switch(col) {
				case 0:
					return String.format("%d(%d)", b.getBuoyId1(), b.getBuoyId2());
				case 1:
					return b.getIPAddr();
				case 2:
					return b.getLowestChannel();
				case 3:
					return NetworkReceiver.getPamCommandString(b.getCommandStatus());
				}
			}
			col = getCols2Index(column);
			if (col != null) {
				switch (col) {
				case 0:
					t = b.getLastDataTime();
					if (t == 0) {
						return "no data";
					}
					else {
						return PamCalendar.formatDateTime2(t);
					}
				case 1:
					return b.getPositionString();
				case 2:
					int unk = b.getUnknownPackets();
					int tot = b.getTotalPackets();
					if (unk == 0) {
						return tot;
					}
					else {
						return String.format("%d(+%d unk)", tot, unk);
					}
				}
			}
			col = getExtraInfoIndex(column);
			if (col != null) {
				PairedValueInfo pairInfo = networkReceiver.getExtraTableInfo().get(col);
				Object data = b.getPairVal(pairInfo.getPairName());
//				return pairInfo.formatTableData(b, data);
			}

			col = getDatablockIndex(column);
			if (col != null) {
				return 0;//b.getBlockPacketCount(col);
			}
			return null;
		}

		//		@Override
		//		public void fireTableStructureChanged() {
		//			super.fireTableStructureChanged();
		//			
		//		};

		//		@Override
		//		public java.lang.Class<?> getColumnClass(int columnIndex) {
		//			
		//		};		


		@Override
		public String getColumnName(int iCol) {
			Integer c = getCols1Index(iCol);
			if (c != null) {
				return colNames1[c];
			}
			c = getExtraInfoIndex(iCol);
			if (c != null) {
				return networkReceiver.getExtraTableInfo().get(c).getPairName();
			}
			c = getCols2Index(iCol);
			if (c != null) {
				return colNames2[c];
			}
			c = getDatablockIndex(iCol);
			if (c != null) {
				return networkReceiver.getRxDataBlocks().get(c).getDataName();
			}
			return null;
		}

		private Integer getCols1Index(int column) {
			return (column < colNames1.length ? column : null); 
		}

		private Integer getExtraInfoIndex(int column) {
			column -= (colNames1.length);
			return (column >= 0 & column < networkReceiver.getExtraTableInfo().size() ? column : null);
		}

		private Integer getCols2Index(int column) {
			column -= (colNames1.length + networkReceiver.getExtraTableInfo().size());
			return (column >= 0 & column < colNames2.length ? column : null);
		}

		private Integer getDatablockIndex(int column) {
			if (networkReceiver.getRxDataBlocks() == null) {
				return null;
			}
			column -= (colNames1.length + networkReceiver.getExtraTableInfo().size() + colNames2.length);
			return (column >= 0 & column < networkReceiver.getRxDataBlocks().size() ? column : null);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Integer col = getExtraInfoIndex(columnIndex);
			if (col != null) {
				Class colClass = networkReceiver.getExtraTableInfo().get(col).getTableClass();
				if (colClass != null) {
					return colClass;
				}
			}
			return super.getColumnClass(columnIndex);
		}


		//		@Override
		//		public void fireTableStructureChanged() {
		//			super.fireTableStructureChanged();
		//			
		//		};

		//		@Override
		//		public java.lang.Class<?> getColumnClass(int columnIndex) {
		//			
		//		};		


	}





	public void notifyModelChanged(int changeType) {
		buoyTableData.fireTableStructureChanged();
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			//			setColumnWidths();
			// need to call this again here because the widths reset when the extra columns are added
			columnWidths.setColumnWidths();
			System.out.println("iit comp");
		}
		//		notifyModelChanged
		int rI = buoyTableData.findColumn("RSSI");
		if (rI!=-1){
			//			buoyTable.getColumn(rI).setCellRenderer();
		}

	}

	/**
	 * @param setPairData
	 */
	public void configurationChange() {
		buoyTableData.fireTableStructureChanged();
		columnWidths.setColumnWidths();
	}

}
