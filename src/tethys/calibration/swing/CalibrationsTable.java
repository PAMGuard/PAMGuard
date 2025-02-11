package tethys.calibration.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.xml.datatype.XMLGregorianCalendar;

import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import PamController.soundMedium.GlobalMediumManager;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import nilus.Calibration;
import tethys.Collection;
import tethys.DocumentNilusObject;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.TethysTimeFuncs;
import tethys.calibration.CalibrationHandler;
import tethys.dbxml.TethysException;
import tethys.swing.TethysGUIPanel;

public class CalibrationsTable extends TethysGUIPanel {

	private CalibrationHandler calibrationHandler;
	
	private CalibrationsTableModel calTableModel;
	
	private JPanel mainPanel;
	
	private JTable calTable;

	private TethysControl tethysControl;
	
	/**
	 * @param calibrationHandler
	 */
	public CalibrationsTable(TethysControl tethysControl, CalibrationHandler calibrationHandler) {
		super(tethysControl);
		this.tethysControl = tethysControl;
		this.calibrationHandler = calibrationHandler;
		calTableModel = new CalibrationsTableModel();
		calTable = new JTable(calTableModel);
		calTable.setRowSelectionAllowed(true);
		calTable.addMouseListener(new TableMouse());
		
		JScrollPane scrollPane = new JScrollPane(calTable);
		
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, scrollPane);

		calTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"CalibrationsTable", calTable);

	}
	

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}


	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		calTableModel.fireTableDataChanged();
	}

	private class TableMouse extends MouseAdapter {

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
		int[] rows = calTable.getSelectedRows();
		if (rows == null || rows.length == 0) {
			return;
		}
		int n = rows.length;
		DocumentNilusObject<Calibration> doc = calibrationHandler.getCalibrationDataList().get(rows[0]);
		
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem;
		if (n == 1) {
			menuItem = new JMenuItem("Display document " + doc.getDocumentName());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showCalibration(doc);
				}
			});
			popMenu.add(menuItem);
		}
		popMenu.addSeparator();
		if (n > 1) {
			menuItem = new JMenuItem("Delete selected documents");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteCalibrations(rows);
				}
			});
			popMenu.add(menuItem);
		}
		else {
			menuItem = new JMenuItem("Delete document " + doc.getDocumentName());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteCalibration(doc);
				}
			});
			popMenu.add(menuItem);
		}
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void deleteCalibration(DocumentNilusObject<Calibration> doc) {
		String docName = doc.getDocumentName();
		int ans = WarnOnce.showNamedWarning("delete doc " + Collection.Calibrations.collectionName(),
				PamController.getMainFrame(), "Delete document",
				"Are you sure you want to delete the document " + docName, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			try {
				tethysControl.getDbxmlConnect().removeDocument(Collection.Calibrations.collectionName(), docName);
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docName);
				System.out.println(e.getMessage());
			}
		}
		updateEverything();
		calTableModel.fireTableDataChanged();
	}


	protected void showCalibration(DocumentNilusObject<Calibration> docInfo) {
		tethysControl.displayDocument(docInfo);
		
	}


	protected void deleteCalibrations(int[] rows) {
		String msg = String.format("Are you sure you want to delete %d calibrations documents ?", rows.length);
		int ans = WarnOnce.showNamedWarning("Deletemanycalibrations", PamController.getMainFrame(), "Delete multiple documents", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans != WarnOnce.OK_OPTION) {
			return;
		}
		for (int i = 0; i < rows.length; i++) {
			String docName = null;
			try {
				DocumentNilusObject<Calibration> doc = calibrationHandler.getCalibrationDataList().get(rows[i]);
				docName = doc.getDocumentName();
				tethysControl.getDbxmlConnect().removeDocument(Collection.Calibrations, docName);
			} catch (TethysException e) {
				System.out.println("Failed to delete " + docName);
				System.out.println(e.getMessage());
			}
		}
		
		updateEverything();
		
	}

	private void updateEverything() {
		getTethysControl().sendStateUpdate(new TethysState(StateType.DELETEDATA, Collection.Calibrations));
	}

	class CalibrationsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Document", "Id", "Date", "End to End", "Hydrophone"};

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DocumentNilusObject<Calibration> dataUnit = null;
			try {
				dataUnit = calibrationHandler.getCalibrationDataList().get(rowIndex);
			}
			catch (Exception e) {
				return null;
			}
			if (dataUnit == null) {
				return null;
			}
			Calibration cal = dataUnit.getNilusObject();
			switch (columnIndex) {
			case 0:
				return dataUnit.getDocumentName();
			case 1:
				return cal.getId();
			case 2:
				XMLGregorianCalendar ts = cal.getTimeStamp();
				if (ts == null) {
					return null;
				}
				long ms = TethysTimeFuncs.millisFromGregorianXML(ts);
				return PamCalendar.formatDBDate(ms);
			case 3:
				return getFSString(cal);
			case 4:
				return getPhoneString(cal);
//				return String.format("%3.1fdB %s", cal.getSensitivityV(), cal.getType());
			}
			return null;
		}

		@Override
		public int getRowCount() {
			return calibrationHandler.getCalibrationDataList().size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 4) {
				return  PamController.getInstance().getGlobalMediumManager().getRecieverString();
			}
			else {
				return columnNames[column];
			}
		}
	}

	public String getFSString(Calibration cal) {
		Double fs = cal.getSensitivityDBFS();
		if (fs == null) {
			return null;
		}
		double ir = cal.getIntensityReferenceUPa();
		String str = String.format("%3.1fdB", fs);
		if (ir != 0) {
			str += String.format(" re%.0f\u00B5Pa", ir); 
		}
		return str;
	}

	public Object getPhoneString(Calibration cal) {
		Double dbV =  cal.getSensitivityDBV();
		if (dbV == null) {
			return null;
		}
		double ir = cal.getIntensityReferenceUPa();
		String str = String.format("%3.1fdB", dbV);
		if (ir != 0) {
			str += String.format(" re%.0fV/\u00B5Pa", ir); 
		}
		return str;
	}
}
