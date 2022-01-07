package effortmonitor.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import effortmonitor.EffortControl;
import effortmonitor.EffortDataUnit;
import pamScrollSystem.AbstractPamScroller;

public class EffortTableView extends DataBlockTableView<EffortDataUnit> {

	private String[] colNames = {"UID", "Active", "Analysis Date", "Observer", "Display", "Data Start", "Data End"};
	private EffortControl effortControl;

	public EffortTableView(EffortControl effortControl, PamDataBlock<EffortDataUnit> pamDataBlock) {
		super(pamDataBlock, "Scrolling Effort");
		this.effortControl = effortControl;
		showViewerScrollControls(false);
		setAllowMultipleRowSelection(true);
	}

	@Override
	public String[] getColumnNames() {
		return colNames;
	}

	@Override
	public Object getColumnData(EffortDataUnit effortData, int column) {
		if (effortData == null) {
			return null;
		}
		switch (column) {
		case 0:
			return effortData.getUID();
		case 1:
			return effortData.isActive() ? "*" : null;
		case 2:
			return PamCalendar.formatDBDateTime(effortData.getSessionStartTime());
		case 3:
			return effortData.getObserver();
		case 4:
			return effortData.getDisplayName();
		case 5:
			return PamCalendar.formatDBDateTime(effortData.getTimeMilliseconds());
		case 6:
			return PamCalendar.formatDBDateTime(effortData.getEndTimeInMilliseconds());
		}
		return null;
	}

	@Override
	public void popupMenuAction(MouseEvent e, EffortDataUnit dataUnit, String colName) {
		if (dataUnit == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		String str = String.format("Go to %s", PamCalendar.formatDateTime(dataUnit.getEndTimeInMilliseconds()));
		JMenuItem menuItem = new JMenuItem(str);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				goTo(dataUnit.getScroller(), dataUnit.getEndTimeInMilliseconds());
			}
		});
		menu.add(menuItem);

		if (dataUnit.isActive()) {
			menuItem = new JMenuItem("Update info ...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					effortControl.showSettingsDialog(effortControl.getGuiFrame(), dataUnit);
				}
			});
			menu.add(menuItem);
		}
		
		PamDataUnit[] multiRows = getMultipleSelectedRows();
		if (multiRows != null && multiRows.length > 1) {
			menuItem = new JMenuItem(String.format("Delete %d effort entries", multiRows.length));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteMultipleRows(multiRows);
				}
			});
			menu.add(menuItem);
		}
		else {
			menuItem = new JMenuItem(String.format("Delete effort entry UID %d", dataUnit.getUID()));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteRow(dataUnit, true);
				}
			});
			menu.add(menuItem);
		}
		
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void deleteRow(EffortDataUnit dataUnit, boolean askFirst) {
		if (askFirst) {
			String message = String.format("Are you sure you want to permanently delete effort row UID %d?", dataUnit.getUID());
			int answ = WarnOnce.showWarning(PamController.getMainFrame(), "Delete effort data", message, WarnOnce.OK_CANCEL_OPTION);
			if (answ == WarnOnce.CANCEL_OPTION) {
				return;
			}
		}
		effortControl.getEffortDataBlock().remove(dataUnit, true);
		fireTableStructureChanged();
	}

	protected void deleteMultipleRows(PamDataUnit[] multiRows) {
		String message = String.format("Are you sure you want to permanently delete %d effort rows?", multiRows.length);
		int answ = WarnOnce.showWarning(PamController.getMainFrame(), "Delete effort data", message, WarnOnce.OK_CANCEL_OPTION);
		if (answ == WarnOnce.OK_OPTION) {
			for (int i = 0; i < multiRows.length; i++) {
				deleteRow((EffortDataUnit) multiRows[i], false);
			}
		}
	}

	/**
	 * Go to a time (usually the end of an existing scroll period)
	 * @param scroller 
	 * @param endTimeInMilliseconds
	 */
	protected void goTo(AbstractPamScroller scroller, long timeInMilliseconds) {
		effortControl.goToTime(scroller, timeInMilliseconds);
	}

}
