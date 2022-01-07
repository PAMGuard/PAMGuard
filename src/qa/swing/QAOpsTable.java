package qa.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamguardMVC.PamDataBlock;
import qa.QAControl;
import qa.QANotifyable;
import qa.operations.QAOpsDataUnit;

public class QAOpsTable extends DataBlockTableView<QAOpsDataUnit> implements QANotifyable {
	
	private String[] columnNames = {"UTC", "Local", "Code", "State"};
	private QAControl qaControl; 

	public QAOpsTable(QAControl qaControl, PamDataBlock<QAOpsDataUnit> pamDataBlock) {
		super(pamDataBlock, "Operations Data");
		this.qaControl = qaControl;
		showViewerScrollControls(false);
	}

	@Override
	public String[] getColumnNames() {
		return columnNames;
	}

	@Override
	public Object getColumnData(QAOpsDataUnit dataUnit, int column) {
		switch(column) {
		case 0:
			return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds());
		case 1:
			return PamCalendar.formatLocalDateTime(dataUnit.getTimeMilliseconds());
		case 2:
			return dataUnit.getOpsStatusCode();
		case 3:
			return dataUnit.getOpsStatusName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see PamView.component.DataBlockTableView#popupMenuAction(java.awt.event.MouseEvent, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void popupMenuAction(MouseEvent e, QAOpsDataUnit dataUnit, String selColumn) {
		if (dataUnit == null) {
			return;
		}
		JPopupMenu pm = new JPopupMenu();
		JMenuItem edit = new JMenuItem("Edit...");
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editItem(e, dataUnit);
			}
		});
		pm.add(edit);
		pm.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void editItem(ActionEvent e, QAOpsDataUnit dataUnit) {
		QAOpsDataUnit updatedUnit = OpsEditDialog.showDialog(qaControl.getGuiFrame(), qaControl.getQaOperationsStatus(), dataUnit);
		if (updatedUnit != null) {
			updatedUnit.getParentDataBlock().updatePamData(updatedUnit, PamCalendar.getTimeInMillis());
		}
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		switch (noteCode) {
		case PamController.INITIALIZATION_COMPLETE:
		case QANotifyable.OPS_STATUS_CHANGE:
//			this.
			// not needed - handled automatically by datablock observer
		}
		
	}


}
