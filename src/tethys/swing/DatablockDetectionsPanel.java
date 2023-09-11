package tethys.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.PamGui;
import PamView.dialog.warn.WarnOnce;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import nilus.DetectionEffortKind;
import nilus.Detections;
import nilus.GranularityType;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.dbxml.TethysException;
import tethys.detection.StreamDetectionsSummary;
import tethys.niluswraps.PDeployment;
import tethys.niluswraps.PDetections;
import tethys.niluswraps.TethysCollections;

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
	
	@Override
	public void updateState(TethysState tethysState) {
		if (dataBlock != null) {
			selectDataBlock(dataBlock);
		}
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

		JPopupMenu popMenu = new JPopupMenu();
		
		JMenuItem menuItem = new JMenuItem("Delete document " + pDets.detections.getId());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteDocument(pDets);
			}
		});
		popMenu.add(menuItem);
		
		menuItem = new JMenuItem("Display document " + pDets.detections.getId());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayDocument(pDets);
			}
		});
		popMenu.add(menuItem);
		
		menuItem = new JMenuItem("Export document " + pDets.detections.getId());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportDocument(pDets);
			}
		});
		popMenu.add(menuItem);
		
		
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
		
	}
	
	protected void deleteDocument(PDetections pDets) {
		String msg = String.format("Are you sure you want to delete the Detections document %s ?", pDets.detections.getId());
		int ans = WarnOnce.showWarning(PamGui.findComponentWindow(mainPanel), "Delete Document", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans != WarnOnce.OK_OPTION) {
			return;
		}
		try {
			getTethysControl().getDbxmlConnect().deleteDocument(pDets.detections);
		} catch (TethysException e) {
			getTethysControl().showException(e);
		}
		getTethysControl().exportedDetections(dataBlock);
		selectDataBlock(dataBlock); // force table update. 
	}

	private void displayDocument(PDetections pDets) {
		getTethysControl().displayDocument(TethysCollections.Detections.toString(), pDets.detections.getId());
		
	}

	private void exportDocument(PDetections pDets) {
		getTethysControl().exportDocument(TethysCollections.Detections.toString(), pDets.detections.getId());
		
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
		
		private String[] colNames = {"Document", "Granularity", "Count", "Abstract"};

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
				List<DetectionEffortKind> kinds = dets.getEffort().getKind();
				if (kinds == null) {
					return null;
				}
				for (DetectionEffortKind kind : kinds) {
					if (kind.getGranularity() != null) {
						GranularityType granularity = kind.getGranularity();
						return PDeployment.formatGranularity(granularity);
//						if (granularity != null) {
//							return granularity.getValue();
//						}
					}
				}
				break;
			case 2:
				return pDets.count;
			case 3:
				return dets.getDescription().getAbstract();
			}
			return null;
		}
		
	}
}
