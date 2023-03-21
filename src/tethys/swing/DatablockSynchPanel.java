package tethys.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import dataMap.OfflineDataMap;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.output.DatablockSynchInfo;

public class DatablockSynchPanel extends TethysGUIPanel  {
	
	public JPanel mainPanel;
	
	private JTable synchTable;
	
	private SynchTableModel synchTableModel;
	
	private ArrayList<DatablockSynchInfo> dataBlockSynchInfo;

	public DatablockSynchPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("PAMGuard data blocks"));
		synchTableModel = new SynchTableModel();
		synchTable = new JTable(synchTableModel);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"SynchTable", synchTable);
		JScrollPane scrollPane = new JScrollPane(synchTable);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}
	
	
	@Override
	public void updateState(TethysState tethysState) {
		synchTableModel.fireTableDataChanged();
	}

	private ArrayList<DatablockSynchInfo> getSychInfos() {
		if (dataBlockSynchInfo == null) {
			dataBlockSynchInfo = getTethysControl().getSynchronisationInfos();
		}
		return dataBlockSynchInfo;
	}

	private class SynchTableModel extends AbstractTableModel {

		String[] columnNames = {"Data Stream", "N PAM Datas", "PAMGuard Time", "N Tethys Datas", "Tethys Time", "Options"};
		
		@Override
		public int getRowCount() {
			return getSychInfos().size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DatablockSynchInfo synchInfo = getSychInfos().get(rowIndex);
			return getValueAt(synchInfo, columnIndex);
		}

		private Object getValueAt(DatablockSynchInfo synchInfo, int columnIndex) {
			OfflineDataMap dataMap = synchInfo.getDataBlock().getPrimaryDataMap();
			switch (columnIndex) {
			case 0:
				return synchInfo.getDataBlock().getDataName();
			case 1:
				if (dataMap == null) {
					return null;
				}
				return synchInfo.getDataBlock().getPrimaryDataMap().getDataCount();
			case 2:
				if (dataMap == null) {
					return null;
				}
				if (dataMap.getDataCount() == 0) {
					return "No data";
				}
				long start = synchInfo.getDataBlock().getPrimaryDataMap().getFirstDataTime();
				long stop = synchInfo.getDataBlock().getPrimaryDataMap().getLastDataTime();
				return String.format("%s - %s", PamCalendar.formatDBDateTime(start), PamCalendar.formatDBDateTime(stop));
			case 3:
				return synchInfo.getDataCount();
			}
			return null;
		}
		
	}
}
