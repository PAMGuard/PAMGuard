package analoginput.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import PamUtils.PamCalendar;
import PamView.PamTable;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogDevicesManager;
import analoginput.AnalogInputObserver;
import analoginput.AnalogRangeData;
import analoginput.AnalogSensorUser;
import analoginput.ItemAllData;
import analoginput.SensorChannelInfo;
import userDisplay.UserDisplayComponentAdapter;
import userDisplay.UserDisplayControl;

public class AnalogDiagnosticsDisplay extends UserDisplayComponentAdapter implements AnalogInputObserver {

	private AnalogDevicesManager analogDevicesManager;
	private String uniqueDisplayName;
	private UserDisplayControl userDisplayControl;

	private PamPanel maiPanel, componentPanel;

	private AnalogSensorUser sensorUser;
	private SensorChannelInfo[] channelInfo;
	private AnalogDeviceParams deviceParams;

	private PamTable table;

	private SensTableModel sensTableModel;

	private long[] lastUpdate;

	private ItemAllData[] allItemData;

	private  String[] tableCols = {"Item", "Channel", "Range", "Updated", "Int", "Raw", "Calibrated"};

	public AnalogDiagnosticsDisplay(AnalogDevicesManager analogDevicesManager, UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		this.analogDevicesManager = analogDevicesManager;
		this.userDisplayControl = userDisplayControl;
		this.uniqueDisplayName = uniqueDisplayName;
		this.sensorUser = analogDevicesManager.getSensorUser();
		
		sensTableModel = new SensTableModel();
		table = new SensTable(sensTableModel);
		SwingTableColumnWidths tcw = new SwingTableColumnWidths(analogDevicesManager.getUnitName() + "Table", table);


//		mainPanel = new PamPanel();
//		mainPanel.setBorder(new TitledBorder(sensorUser.getUserName()));
		JScrollPane scrollPane = new JScrollPane(table);
		componentPanel = new PamPanel(new BorderLayout());
		componentPanel.add(BorderLayout.CENTER, scrollPane);

		analogDevicesManager.addInputObserver(this);

//		mainPanel.setLayout(new BorderLayout());
//		mainPanel.add(BorderLayout.CENTER, table);
		layoutPanel();

	}

	private synchronized void layoutPanel() {
		deviceParams = analogDevicesManager.getAnalogDeviceParams();
		channelInfo = sensorUser.getChannelNames();
		int nItem = channelInfo.length;
		lastUpdate = new long[nItem];
		allItemData = new ItemAllData[nItem];
		sensTableModel.fireTableDataChanged();
	}

	@Override
	public Component getComponent() {
		return componentPanel;
	}

	@Override
	public void changedConfiguration() {
		layoutPanel();
	}

	@Override
	public void changedData(ItemAllData itemData) {
		if (itemData == null || allItemData == null) {
			return;
		}
		allItemData[itemData.getItem()] = itemData;
		int item = itemData.getItem();
		if (item < 0 || item >= allItemData.length) {
			return;
		}
		lastUpdate[itemData.getItem()] = System.currentTimeMillis();
		sensTableModel.fireTableDataChanged();
	}
	
	private class SensTable extends PamTable {

	    private DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();

	    private DefaultTableCellRenderer renderCentre = new DefaultTableCellRenderer();

	    private String defaultTip = "Sensor Data diagnostic information";
		/**
		 * @param dm
		 */
		public SensTable(TableModel dm) {
			super(dm);
	        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
	        renderRight.setBorder(new EmptyBorder(0, 3, 0, 5));
	        renderCentre.setHorizontalAlignment(SwingConstants.CENTER);
	        setToolTipText(defaultTip);
		}

		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			switch (column) {
			case 0:
				return renderRight;
			case 1:
			case 2:
			case 3:
				return renderCentre;
			}
			return super.getCellRenderer(row, column);
		}

		@Override
		public String getToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            return getTooTipText(rowIndex, realColumnIndex);
		}

		private String getTooTipText(int rowIndex, int colIndex) {
			if (channelInfo == null) {
				return defaultTip;
			}
			String rowInfo = "";
			switch(colIndex) {
			case 0:
				return "Data item under control";
			case 1:
				return "Hardware channel number";
			case 2:
				return "Hardware input range";
			case 3:
				return "Last update time";
			case 4:
				return "Raw data value from hardware";
			case 5:
				return "Data value after hardware calibration";
			case 6:
				return "Data vlaue after sensor calibration";
			}
			return defaultTip;
		}
		
	}

	private class SensTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			if (channelInfo == null) {
				return 0;
			}
			return channelInfo.length;
		}

		@Override
		public int getColumnCount() {
			return tableCols.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SensorChannelInfo chanInfo = channelInfo[rowIndex];
			switch (columnIndex) {
			case 0:
				return chanInfo.getName();
			case 1:
				if (deviceParams == null) {
					return null;
				}
				Integer chan = deviceParams.getItemChannel(rowIndex);
				if (chan == null || chan < 0) {
					return "-";
				}
				else {
					return chan;
				}
			case 2:
				if (deviceParams == null) {
					return null;
				}
				AnalogRangeData range = deviceParams.getItemRange(rowIndex);
				if (range != null) {
					return range.toString();
				}
				break;
			case 3:
				if (lastUpdate[rowIndex] > 0) {
					return PamCalendar.formatTime(lastUpdate[rowIndex]);
				}
				break;
			case 4:
				if (allItemData[rowIndex] != null) {
					return allItemData[rowIndex].getIntValue();
				}
				break;
			case 5:
				if (allItemData[rowIndex] != null) {
					return allItemData[rowIndex].getScaledValue();
				}
				break;
			case 6:
				if (allItemData[rowIndex] != null) {
					return allItemData[rowIndex].getParameterValue();
				}
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return tableCols[column];
		}

	}

}
