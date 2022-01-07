package clickDetector.dataSelector;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;

import PamView.dialog.PamDialogPanel;
import PamView.paneloverlay.OverlaySwingPanel;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.PAMSelectClause;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Data selector which makes it possible to select events based on their type. 
 * @author Doug
 *
 */
public class ClickTrainDataSelector2 extends DataSelector {

	private ClickControl clickControl;
	private OfflineEventDataBlock offlineEventDataBlock;
	private ClickTrainDataSelect2Params params = new ClickTrainDataSelect2Params();

	public ClickTrainDataSelector2(ClickControl clickControl, OfflineEventDataBlock offlineEventDataBlock, String selectorName, boolean allowScores) {
		super(offlineEventDataBlock, selectorName, allowScores);
		this.clickControl = clickControl;
		this.offlineEventDataBlock = offlineEventDataBlock;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof ClickTrainDataSelect2Params) {
			params = (ClickTrainDataSelect2Params) dataSelectParams;
		}
	}

	@Override
	public ClickTrainDataSelect2Params getParams() {
		return params;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new ClickTrainSelectPanel2(clickControl, this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		// the data unit should be an offlineeventdataunit
		if (pamDataUnit == null) {
			return params.isIncludeUnclassified() ? 1 : 0;
		}
		OfflineEventDataUnit oedu = (OfflineEventDataUnit) pamDataUnit;
		return params.isWantType(oedu.getEventType()) ? 1. : 0.;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#getSQLSelectClause()
	 */
	@Override
	public PAMSelectClause getSQLSelectClause(SQLTypes sqlTypes) {
		if (getPamDataBlock() == null) {
			return null;
		}
		SQLLogging logging = getPamDataBlock().getLogging();
		if (logging == null) return null; //cannot happen!
		PamTableDefinition tableDef = logging.getTableDefinition();
		if (params.isIncludeUnclassified()) {
			return null;
		}
		ArrayList<String> selKeys = params.getSelectedList();
		if (selKeys.size() == 0) {
			return null;
		}
		String keyList = sqlTypes.makeInList(selKeys.toArray(new String[selKeys.size()]));
		String qStr = String.format("WHERE TRIM(%s.eventType) %s", 
				tableDef.getTableName(), keyList);
		
		return new FixedClause(qStr);
	}


}
