package PamguardMVC.dataSelector;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.SQLTypes;
import generalDatabase.clauses.PAMSelectClause;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class SuperDetDataSelector extends DataSelector {

	private SuperDetDataBlock superDataBlock;
	private DataSelector dataSelector;
	
	private SuperDataSelectParams superDataSelectParams = new SuperDataSelectParams();

	public SuperDetDataSelector(SuperDetDataBlock superDataBlock, DataSelector dataSelector) {
		super(superDataBlock, dataSelector.getSelectorName(), dataSelector.isAllowScores());
		this.superDataBlock = superDataBlock;
		this.dataSelector = dataSelector;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof SuperDataSelectParams) {
			this.superDataSelectParams = (SuperDataSelectParams) dataSelectParams;
			dataSelectParams = superDataSelectParams.getDataSelectParams(); // to pass on to superdetection!
		}
		
		try {
			dataSelector.setParams(dataSelectParams);				
		}
		catch (ClassCastException e) {
			System.out.printf("Cannot cast parameter type for data selector %s: %s", dataSelector.getLongSelectorName(), e.getMessage());
		}
	}

	@Override
	public SuperDataSelectParams getParams() {
		if (superDataSelectParams == null) {
			superDataSelectParams = new SuperDataSelectParams();
		}
		superDataSelectParams.setDataSelectParams(dataSelector.getParams());
		return superDataSelectParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new SuperDataSelectorPanel(this, new DataSelectorDialogPanel(dataSelector, dataSelector.getDialogPanel(), -1));
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return dataSelector.getDialogPaneFX();
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
			return 1;
		}
		SuperDetection superDetection = pamDataUnit.getSuperDetection(superDataBlock);
		if (superDetection == null) {
			return superDataSelectParams.isUseUnassigned() ? 1 : 0;
		}
		return dataSelector.scoreData(superDetection);
	}

	@Override
	public String getSelectorName() {
		return "Sup'det: " + dataSelector.getSelectorName();
	}

	@Override
	public PAMSelectClause getSQLSelectClause(SQLTypes sqlTypes) {
		return dataSelector.getSQLSelectClause(sqlTypes);
	}

	@Override
	public boolean isAllowScores() {
		return dataSelector.isAllowScores();
	}

	@Override
	public String getSelectorTitle() {
		return dataSelector.getSelectorTitle();
	}

}
