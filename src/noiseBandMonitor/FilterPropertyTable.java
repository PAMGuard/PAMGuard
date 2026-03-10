package noiseBandMonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import Filters.FilterParams;
import PamUtils.FrequencyFormat;

/**
 * A panel that can be included in various places which displays a list 
 * of filter properties. 
 * @author Doug Gillespie
 *
 */
public class FilterPropertyTable {

	private BandPerformance[] bandPerformances;
	private JPanel mainPanel;
	private FilterTableData filterTableData;
	private JTable filterTable;
	private NoiseBandControl noiseBandControl;
	private NoiseBandDialog noiseBandDialog;
	
	public FilterPropertyTable(NoiseBandControl noiseBandControl, NoiseBandDialog noiseBandDialog) {
		this.noiseBandControl = noiseBandControl;
		this.noiseBandDialog = noiseBandDialog;
		filterTableData = new FilterTableData();
		filterTable = new JTable(filterTableData);
		JScrollPane scrollPane = new JScrollPane(filterTable);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		TableMouse tm = new TableMouse();
		filterTable.addMouseListener(tm);
		filterTable.addMouseMotionListener(tm);
		filterTable.setRowSelectionAllowed(true);
		filterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setPreferredSize(new Dimension(20,200));
		for (int i = 0; i < filterTableData.getColumnCount(); i++) {
			filterTable.getColumnModel().getColumn(i).setPreferredWidth(filterTableData.preferredWidth[i]);
		}
	}


	/**
	 * @param bandPerformances the bandPerformances to set
	 */
	public void setBandPerformances(NoiseBandSettings noiseBandSettings, BandPerformance[] bandPerformances) {
		this.bandPerformances = bandPerformances;
		filterTableData.fireTableDataChanged();
	}


	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	private class FilterTableData extends AbstractTableModel {

		private final String[] colNames = {"Lo Freq", "Centre", "Hi Freq", "Response"};
		protected final int[] preferredWidth = {20, 20, 20, 15};
		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public int getRowCount() {
			if (bandPerformances == null) {
				return 0;
			}
			return bandPerformances.length;
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (bandPerformances == null) {
				return null;
			}
			BandPerformance bf = bandPerformances[iRow];
			FilterParams fParams = bf.getFilterMethod().getFilterParams();
			switch(iCol) {
			case 0:
				return FrequencyFormat.formatFrequency(fParams.highPassFreq, true);
			case 1:
				return FrequencyFormat.formatFrequency(fParams.getCenterFreq(), true);
			case 2:
				return FrequencyFormat.formatFrequency(fParams.lowPassFreq, true);
			case 3:
				return String.format("%3.2f dB", bf.getFilterIntegratedResponse());
			}
			return null;
		}
		
	}
	
	private class TableMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			selMouseRow();
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			selMouseRow();
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			selMouseRow();
		}
	}
	
	private void selMouseRow() {
		int iRow = filterTable.getSelectedRow();
		noiseBandDialog.setSelectedBand(iRow);
	}
}
