package difar.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import Array.ArrayManager;
import Array.StreamerDataUnit;
import difar.DifarControl;
import difar.DifarProcess;
import difar.SonobuoyManager;
import difar.calibration.CalibrationDataUnit;
import difar.dialogs.SonobuoyDialog;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamSplitPane;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamTable;
import PamView.PamColors.PamColor;
import PamView.component.DataBlockTableView;
import PamView.component.PamScrollPane;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataUnit;


/**
 * Contains table of sonobuoy deployments and calibrations
 * @author Brian Miller
 *
 */
public class SonobuoyManagerPanel extends PamPanel {

	DifarControl difarControl;
	SonobuoyManager buoyManager;
	private PamPanel mainPanel;
	PamTable sonobuoyTable;
	CalibrationTableView calibrationTable;
	PamPanel calibrationPanel;
	
	private PamScrollPane scrollPane;
	
	
	public SonobuoyManagerPanel(DifarControl difarControl) {
		super();
		this.difarControl = difarControl;
		this.buoyManager = difarControl.sonobuoyManager;
		this.setLayout(new BorderLayout());
		boolean isViewer = difarControl.isViewer();
		if (isViewer == false) {
//			this.add(BorderLayout.NORTH, difarControl.getInternalActionsPanel().getComponent());
		}
		
//		this.add(BorderLayout.CENTER, makeMainPanel());
////		calibrationPanel = new PamPanel();
////		calibrationPanel.add(new CalibrationTableView(difarControl.getDifarProcess()).getComponent())
////		this.add(BorderLayout.EAST, new CalibrationTableView(difarControl.getDifarProcess()).getComponent()); //calibrationPanel);
		calibrationTable = new CalibrationTableView(difarControl.getDifarProcess());
		calibrationPanel = (PamPanel) calibrationTable.getComponent();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			makeMainPanel(),
			calibrationPanel
		);	
		splitPane.setResizeWeight(1);
		splitPane.setDividerLocation(600);
		this.add(splitPane);

	}
	
	private JPanel makeMainPanel(){
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setOpaque(true);
		
		sonobuoyTable = new PamTable(buoyManager.tableDataModel){
			  @Override
		       public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		           Component component = super.prepareRenderer(renderer, row, column);
		           int rendererWidth = component.getPreferredSize().width;
		           javax.swing.table.TableColumn tableColumn = getColumnModel().getColumn(column);
		           tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
		           return component;
		        }
		    };
		sonobuoyTable.setAutoCreateRowSorter(true);
		sonobuoyTable.getRowSorter().toggleSortOrder(0);
		sonobuoyTable.getSelectionModel().addListSelectionListener(new TableListListener());
		sonobuoyTable.addMouseListener(new TableMouseListener());
		resizeColumnWidth(sonobuoyTable);
		sonobuoyTable.setFillsViewportHeight(true);
//		sonobuoyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//		sonobuoyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ON);
		sonobuoyTable.setRowSelectionAllowed(true);
		
		scrollPane = new PamScrollPane(sonobuoyTable);
		
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		new SwingTableColumnWidths(buoyManager.getProcessName(), sonobuoyTable);

		return mainPanel;
	}
	
	
	class TableMouseListener extends MouseAdapter {
//		int selectedDataUnitIndex = 0;
		@Override
		public void mousePressed(MouseEvent me) {
	        JTable table =(JTable) me.getSource();
	        Point p = me.getPoint();
	        int row = table.rowAtPoint(p);
	        if (me.getClickCount() == 2) {
				row = sonobuoyTable.getSelectedRow();
				int rc = sonobuoyTable.getRowCount();
				if (row < 0 || row > rc){
					return;
				}
				PamDataUnit pamDataUnit = null;
				int index = sonobuoyTable.convertRowIndexToModel(row);
					pamDataUnit = findDataUnitForRow(index);
					if (pamDataUnit==null){
						return;
					}
					editDataUnit((StreamerDataUnit) pamDataUnit);
//				}
				if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
					viewDataUnit(pamDataUnit);
				}
	        }
	    }
	}
	
	class TableListListener implements ListSelectionListener {
		int selectedDataUnitIndex = 0; 
		
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
		}
		
	
	}

	public PamDataUnit findDataUnitForRow(int row) {
		if (row < 0){
			return null;
		}
		int tsCol = SonobuoyManager.COLUMN_TIMESTAMP;
		int chanCol = SonobuoyManager.COLUMN_CHANNEL;
		long timestamp = PamCalendar.millisFromDateString((String) buoyManager.tableData[row][tsCol], false);

		int channel = 1<<(int) buoyManager.tableData[row][chanCol];
		
		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getClosestUnitMillis(timestamp, channel);

		return sdu;

	}

	public void editDataUnit(StreamerDataUnit sdu) {
		Double heading = sdu.getStreamerData().getHeading();
		long startTime = sdu.getTimeMilliseconds();
		StreamerDataUnit modifiedSdu = SonobuoyDialog.showDialog(difarControl.getGuiFrame(), 
				ArrayManager.getArrayManager().getCurrentArray(), sdu, difarControl);
		if (modifiedSdu != null){
			Double newHeading = modifiedSdu.getStreamerData().getHeading();
			if (newHeading != heading  || 
					modifiedSdu.getTimeMilliseconds() != startTime) {
				buoyManager.updateCorrection(modifiedSdu.getStreamerData(), PamCalendar.getTimeInMillis(), 
											 newHeading, 0.0, 0);
				}
			ArrayManager.getArrayManager().getStreamerDatabBlock().replaceStreamerDataUnit(sdu, modifiedSdu);
			buoyManager.overwriteSonobuoyData(modifiedSdu);				
			buoyManager.updateSonobuoyTableData();
		}
	}
	
	public void viewDataUnit(PamDataUnit formsDataUnit) {
		// TODO Auto-generated method stub
		
	}
	
	public void resizeColumnWidth(JTable table) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        int width = 1; // Min width
	        for (int row = 0; row < table.getRowCount(); row++) {
	            TableCellRenderer renderer = table.getCellRenderer(row, column);
	            Component comp = table.prepareRenderer(renderer, row, column);
	            width = Math.max(comp.getPreferredSize().width +1 , width);
	        }
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	}

}
