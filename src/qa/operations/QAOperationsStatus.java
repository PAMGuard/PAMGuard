package qa.operations;

import java.awt.Color;

import javax.swing.SwingUtilities;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import qa.QAControl;
import qa.QANotifyable;
import qa.swing.QAOperationsDisplays;

/**
 * Handle QA Operations status information including possibility 
 * of editing ops data times and values. 
 * @author dg50
 *
 */
public class QAOperationsStatus implements QANotifyable {

	private static final String[] defaultStatuss = {"Critical Monitoring", "Non Critical Monitoring"};
	private static final String[] defaultCodes = {"CM", "NCM"};

	public static final String qaStatusTopic = "Monitoring Status";

	private LookUpTables lookUpTables;

	private QAControl qaControl;
	private QAOpsDataUnit currentOpsDataUnit;

	private QAOperationsDisplays qaOpsDisplays;

	public QAOperationsStatus(QAControl qaControl) {
		this.qaControl = qaControl;
		lookUpTables = LookUpTables.getLookUpTables();
		qaControl.addNotifyable(this);
		qaOpsDisplays = new QAOperationsDisplays(qaControl, this);
	}

	public LookupList getLookupList() {
		LookupList newList = LookUpTables.getLookUpTables().getLookupList(qaStatusTopic);
		if (newList == null) {
			newList = new LookupList(qaStatusTopic);
		}
		if (newList.getList().isEmpty()) {
			setDefaultList(newList);
		}
		return newList;
	}
	
	private void setDefaultList(LookupList statusLUT) {
		statusLUT.getList().clear();
		for (int i = 0; i < defaultStatuss.length; i++) {
			statusLUT.addItem(new LookupItem(0, 0, qaStatusTopic, i, defaultCodes[i], defaultStatuss[i], true, Color.BLACK, Color.BLACK, "o"));
		}
		LookUpTables.getLookUpTables().addListToDB(statusLUT);
//		statusLUT.
	}



	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		switch (noteCode) {
		case PamController.INITIALIZATION_COMPLETE:
			if (qaControl.isViewer() == false) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						loadEarlyierData();
					}
				});
			}
			break;
		}

	}

	/**
	 * Load data from previous runs in case PAMguard got restarted. ...
	 */
	private void loadEarlyierData() {
		QAOpsDataBlock dataBlock = qaControl.getQaGeneratorProcess().getOpsDataBlock();
//		dataBlock.loadViewerData(PamCalendar.getTimeInMillis() - PamCalendar.millisPerDay, PamCalendar.getTimeInMillis() + PamCalendar.millisPerDay, null);
		QAOpsDataUnit du = (QAOpsDataUnit) dataBlock.getLogging().loadLastDataUnit();
		if (du != null) {
//			dataBlock.addPamData(du);
			qaControl.tellNotifyables(QANotifyable.OPS_STATUS_CHANGE, du);
		}
	}

	/**
	 * 
	 * @return the current operations status.
	 */
	public QAOpsDataUnit getCurrentStatus() {
		return currentOpsDataUnit;
	}

	public void setStatus(LookupItem selectedState) {
		QAOpsDataUnit currentStatus = getCurrentStatus();
		if (needStatusUpdate(selectedState, currentStatus) == false) {
			return;
		}
		if (selectedState == null) {
			currentOpsDataUnit = new QAOpsDataUnit(PamCalendar.getTimeInMillis(), null, null);
		}
		else {
			currentOpsDataUnit = new QAOpsDataUnit(PamCalendar.getTimeInMillis(), selectedState);
		}
		qaControl.getQaGeneratorProcess().getOpsDataBlock().addPamData(currentOpsDataUnit);
		qaControl.tellNotifyables(QANotifyable.OPS_STATUS_CHANGE, currentOpsDataUnit);
	}
	
	private boolean needStatusUpdate(LookupItem selectedStatus, QAOpsDataUnit currentStatus) {
		if (currentOpsDataUnit == null) {
			return true;
		}
		if (selectedStatus == null && currentStatus == null) {
			return false;
		}
		if (selectedStatus == null || currentStatus == null) {
			return true;
		}
		String selCode = selectedStatus.getCode();
		String oldCode = currentStatus.getOpsStatusCode();
		if (selCode == null || oldCode == null) {
			return true;
		}
		return (selCode.equals(oldCode) == false);
	}

	/**
	 * @return the qaOpsDisplays
	 */
	public QAOperationsDisplays getQaOpsDisplays() {
		return qaOpsDisplays;
	}

}
