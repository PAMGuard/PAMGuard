package tethys.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import nilus.Detections;
import tethys.TethysControl;
import tethys.dbxml.TethysException;
import tethys.detection.StreamDetectionsSummary;
import tethys.niluswraps.PDetections;

/**
 * Table of Detections documents for a single PAMGuard datablock. 
 * Generally, this should only have a smallish number of entries in it
 * so may not need too much real estate on the display. 
 * @author dg50
 *
 */
public class DatablockDetectionsPanel extends TethysGUIPanel implements StreamTableObserver {
	
	private JPanel mainPanel;
	
	private JLabel dataBlockName;

	private TableModel tableModel;
	
	private JTable table;

	private PamDataBlock dataBlock;
	
	private StreamDetectionsSummary streamDetectionsSummary;
	
	public DatablockDetectionsPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, dataBlockName = new JLabel("PAMGUard data stream", JLabel.LEFT));
		mainPanel.setBorder(new TitledBorder("Data stream Tethys Detections documents"));
		
		tableModel = new TableModel();
		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		
		new SwingTableColumnWidths(tethysControl.getUnitName() + getClass().getName(), table);
		
		table.addMouseListener(new MouseActions());
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void selectDataBlock(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		dataBlockName.setText(dataBlock.getLongDataName());
		streamDetectionsSummary = getTethysControl().getDetectionsHandler().getStreamDetections(dataBlock);
		tableModel.fireTableDataChanged();
	}
	
	private class MouseActions extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		
	}
	
	public void showPopupMenu(MouseEvent e) {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}
		
		PDetections pDets = detectionsForRow(row);
		if (pDets == null) {
			return;
		}
		
		JMenuItem menuItem = new JMenuItem("Delete " + pDets.detections.getId());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteDocument(pDets);
			}
		});
		JPopupMenu popMenu = new JPopupMenu();
		popMenu.add(menuItem);
		popMenu.show(e.getComponent(), e.getX(), e.getY());
		
	}
	
	protected void deleteDocument(PDetections pDets) {
		try {
			getTethysControl().getDbxmlConnect().deleteDocument(pDets.detections);
		} catch (TethysException e) {
			getTethysControl().showException(e);
		}
		selectDataBlock(dataBlock); // force table update. 
	}

	private PDetections detectionsForRow(int iRow) {
		if (streamDetectionsSummary == null || streamDetectionsSummary.detectionsDocs == null) {
			return null;
		}
		if (iRow < 0 || iRow >= streamDetectionsSummary.detectionsDocs.size()) {
			return null;
		}
		return streamDetectionsSummary.detectionsDocs.get(iRow);
	}

	private class TableModel extends AbstractTableModel {
		
		private String[] colNames = {"Document", "Count", "Abstract"};

		@Override
		public int getRowCount() {
			if (streamDetectionsSummary == null || streamDetectionsSummary.detectionsDocs == null) {
				return 0;
			}
			return streamDetectionsSummary.detectionsDocs.size();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			PDetections pDets = detectionsForRow(rowIndex);
			return getValueAt(pDets, columnIndex);
		}

		private Object getValueAt(PDetections pDets, int columnIndex) {
			if (pDets == null) {
				return null;
			}
			Detections dets = pDets.detections;
			if (dets == null) {
				return "Error in doc";
			}
			switch (columnIndex) {
			case 0:
				return dets.getId();
			case 1:
				return pDets.count;
			case 2:
				return dets.getDescription().getAbstract();
			}
			return null;
		}
		
	}
}
