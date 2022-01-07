package qa.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamTable;
import PamView.component.DataBlockTableView;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.ScrollPaneAddon;
import qa.QAControl;
import qa.QATestDataBlock;
import qa.QATestDataUnit;
import qa.generator.testset.QATestSet;
import qa.operations.QAOpsDataUnit;

public class QATestTable extends DataBlockTableView<QATestDataUnit>{

	private QAControl qaControl;

	private static String[] colNames = {"UID", "UTC", "Type", "Sound Type", "Ops", "Version", "Sequences", "Status"};

	private QATestDataBlock testDataBlock;

	public QATestTable(QAControl qaControl, QATestDataBlock pamDataBlock) {
		super(pamDataBlock, "QA Tests");
		this.qaControl = qaControl;
		this.testDataBlock = pamDataBlock;
	}

	@Override
	public String[] getColumnNames() {
		return colNames;
	}

	@Override
	public Object getColumnData(QATestDataUnit dataUnit, int columnIndex) {
		if (dataUnit == null) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			return dataUnit.getUID();
		case 1:
			return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds());
		case 2:
			return dataUnit.getTestType();
		case 3:
			return dataUnit.getQaTestSet().getTestName();
		case 4:
			QAOpsDataUnit ops = dataUnit.getQaOpsDataUnit();
			if (ops == null) {
				return "-";
			}
			else {
				return ops.getOpsStatusCode();
			}
		case 5:
			return dataUnit.getQaTestSet().getVersion();
		case 6:
			return dataUnit.getQaTestSet().getNumSequences();
		case 7:
			String status = dataUnit.getQaTestSet().getStatus();
			Long endT = dataUnit.getQaTestSet().getEndTime();
			if (endT != null && endT != null) {
				return status + " " + PamCalendar.formatTodaysTime(endT);
			}
			else if (status != null) {
				return status;
			}
			else {
				return "unknown";
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see PamView.component.DataBlockTableView#popupMenuAction(java.awt.event.MouseEvent, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void popupMenuAction(MouseEvent e, QATestDataUnit dataUnit, String selColumn) {
		if (dataUnit == null) {
			return;
		}
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem;
		
		if (QATestSet.STATUS_ACTIVE.equals(dataUnit.getQaTestSet().getStatus())){
			menuItem = new JMenuItem("Cancel Test " + dataUnit.getUID());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					qaControl.getQaGeneratorProcess().cancelTest(dataUnit);
				}
			});
			popMenu.add(menuItem);
		}

		PamDataUnit[] multiUnits = getMultipleSelectedRows();
		String menuTxt = "Process Test " + dataUnit.getUID();
		if (multiUnits != null && multiUnits.length > 1) {
			menuTxt = "Process multiple tests";
		}
		menuItem = new JMenuItem(menuTxt);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check for multiple selected tests
				if (multiUnits != null && multiUnits.length > 1) {
					/*
					 * Need to individually cast the data units ...
					 */
					QATestDataUnit[] t = new QATestDataUnit[multiUnits.length];
					for (int i = 0; i < t.length; i++) {
						t[i] = (QATestDataUnit) multiUnits[i];
					}
					qaControl.getQaAnalyser().analyseTests(t);
				}
				else {
					qaControl.getQaAnalyser().analyseTest(dataUnit);
				}
			}
		});
		popMenu.add(menuItem);
		
		
		popMenu.show(e.getComponent(), e.getX(), e.getY());
	}

}
