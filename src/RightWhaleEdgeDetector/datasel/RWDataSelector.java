package RightWhaleEdgeDetector.datasel;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import RightWhaleEdgeDetector.RWEDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class RWDataSelector extends DataSelector {
	
	private RWDataSelParams dataSelParams;


	public RWDataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		dataSelParams = new RWDataSelParams();
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof RWDataSelParams) {
			dataSelParams = (RWDataSelParams) dataSelectParams;
		}
	}

	@Override
	public DataSelectParams getParams() {
		return dataSelParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new RSDataSelDialog(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		RWEDataUnit rweDataUnit = (RWEDataUnit) pamDataUnit;
		if (isAllowScores()) {
			return rweDataUnit.rweSound.soundType;
		}
		return (rweDataUnit.rweSound.soundType >= dataSelParams.minType ? 1. : 0.);
	}

	/**
	 * @return the dataSelParams
	 */
	public RWDataSelParams getDataSelParams() {
		return dataSelParams;
	}

	/**
	 * @param dataSelParams the dataSelParams to set
	 */
	public void setDataSelParams(RWDataSelParams dataSelParams) {
		this.dataSelParams = dataSelParams;
	}
}
