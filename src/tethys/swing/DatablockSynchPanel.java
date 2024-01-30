package tethys.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;


import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysStateObserver;
import tethys.niluswraps.PDeployment;
import tethys.output.DatablockSynchInfo;
import tethys.species.DataBlockSpeciesManager;

public class DatablockSynchPanel extends TethysGUIPanel {
	
	public JPanel mainPanel;
	
	private JTable synchTable;
	
	private SynchTableModel synchTableModel;
	
	private ArrayList<DatablockSynchInfo> dataBlockSynchInfo;
	
	private ArrayList<StreamTableObserver> tableObservers = new ArrayList<>();
	
	private JButton exportButton;

	public DatablockSynchPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("PAMGuard data blocks"));
		synchTableModel = new SynchTableModel();
		synchTable = new JTable(synchTableModel);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"SynchTable", synchTable);
		JScrollPane scrollPane = new JScrollPane(synchTable);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		PamPanel ctrlPanel = new PamPanel(new BorderLayout());
		exportButton = new JButton("Export ...");
		ctrlPanel.add(BorderLayout.WEST, exportButton);
		mainPanel.add(BorderLayout.NORTH, ctrlPanel);

		
		synchTable.addMouseListener(new MouseActions());
		synchTable.addKeyListener(new KeyActions());
		
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportData();
			}
		});
		enableExportButton();
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}
	
	private class KeyActions extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) { 
			if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
				selectRow();
			}
		}
		
	}
	private class MouseActions extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			int row = selectRow();
			if (e.isPopupTrigger() && row >= 0) {
				showPopup(e, row);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int row = selectRow();
			if (e.isPopupTrigger() && row >= 0) {
				showPopup(e, row);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			int row = selectRow();
			if (e.isPopupTrigger() && row >= 0) {
				showPopup(e, row);
			}
		}
		
	}
	
	private int selectRow() {
		int row = synchTable.getSelectedRow();
		if (row < 0) {
			return row;
		}
		DatablockSynchInfo synchInfo = dataBlockSynchInfo.get(row);
//		datablockDetectionsPanel.setDataBlock(synchInfo.getDataBlock());
		notifyObservers(synchInfo.getDataBlock());
		enableExportButton();
		return row;
	}
	
	protected void exportData() {
		int[] rows = synchTable.getSelectedRows();
		if (rows == null || rows.length != 1) {
			WarnOnce.showWarning("Data selection", "you must select a single data block for export", WarnOnce.WARNING_MESSAGE);
			return;
		}
		PamDataBlock dataBlock = dataBlockSynchInfo.get(rows[0]).getDataBlock();
		getTethysControl().getDetectionsHandler().exportDataBlock(dataBlock);
	}

	private void enableExportButton() {
		int[] rows = synchTable.getSelectedRows();
		boolean en = rows != null && rows.length == 1;
		ArrayList<PDeployment> deployments = getTethysControl().getDeploymentHandler().getMatchedDeployments();
		if (deployments == null || deployments.size() == 0) {
			en = false;
		}
		exportButton.setEnabled(getTethysControl().isServerOk() & en);
	}

	public void showPopup(MouseEvent e, int row) {
		DatablockSynchInfo synchInfo = dataBlockSynchInfo.get(row);
		if (synchInfo == null) {
			return;
		}
		PamDataBlock dataBlock = synchInfo.getDataBlock();
		DataBlockSpeciesManager speciesManager = dataBlock.getDatablockSpeciesManager();
		if (speciesManager == null) {
			return;
		}
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Species info ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				speciesManager.showSpeciesDialog();
			}
		});
		popMenu.add(menuItem);
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch (tethysState.stateType) {
		case DELETEDATA:
		case EXPORTRDATA:
		case NEWPROJECTSELECTION:
//			dataBlockSynchInfo = null;
//			getSychInfos();
//			getTethysControl().coun
			break;
		case UPDATESERVER:
			enableExportButton();
		}
		
		synchTableModel.fireTableDataChanged();
		selectRow();
	}
	
	public void addTableObserver(StreamTableObserver observer) {
		tableObservers.add(observer);
	}
	
	public void notifyObservers(PamDataBlock dataBlock) {
		for (StreamTableObserver obs : tableObservers) {
			obs.selectDataBlock(dataBlock);
		}
	}

	private ArrayList<DatablockSynchInfo> getSychInfos() {
		if (dataBlockSynchInfo == null) {
			dataBlockSynchInfo = getTethysControl().getSynchronisationInfos();
		}
		return dataBlockSynchInfo;
	}

	private class SynchTableModel extends AbstractTableModel {

		String[] columnNames = {"Data Stream", "N PAM Datas", "PAMGuard Time", "Tethys Documents"};//, "Tethys Time", "Options"};
		
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
				return synchInfo.getDataBlock().getLongDataName();
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
				return synchInfo.getDetectionDocumentCount();
			}
			return null;
		}
		
	}
}
