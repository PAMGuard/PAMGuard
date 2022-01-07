package whistlesAndMoans.dataSelector;

import java.io.Serializable;

import alarm.AlarmParameters;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;
import whistlesAndMoans.alarm.WMAlarmParameters;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;

public class WMDDataSelector extends DataSelector {

	private WhistleMoanControl wmControl;
	
	private WMDSelectPanel selectPanel;
	
	private WMAlarmParameters wmAlarmParameters = new WMAlarmParameters();

	public WMDDataSelector(WhistleMoanControl wmControl, PamDataBlock pamDataBlock, String selectorName,
			boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.wmControl = wmControl;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (selectPanel == null) {
			selectPanel = new WMDSelectPanel(this);
		}
		return selectPanel;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {		
		ConnectedRegionDataUnit crDataUnit = (ConnectedRegionDataUnit) pamDataUnit;
		return (wantWhistle(crDataUnit) ? 1: 0);
	}	
	
	private boolean wantWhistle(ConnectedRegionDataUnit crDataUnit) {
		double[] f = crDataUnit.getFrequency();
		if (wmAlarmParameters.minFreq > 0 && f[0] < wmAlarmParameters.minFreq) {
			return false;
		}
		if (wmAlarmParameters.maxFreq > 0 && f[1] > wmAlarmParameters.maxFreq) {
			return false;
		}
		if (crDataUnit.getAmplitudeDB() < wmAlarmParameters.minAmplitude) {
			return false;
		}
		float sampleRate = crDataUnit.getParentDataBlock().getParentProcess().getSampleRate();
		if (crDataUnit.getSampleDuration() * 1000. / sampleRate < wmAlarmParameters.minLengthMillis) {
			return false;
		}
		PamDataUnit superDet = crDataUnit.getSuperDetection(0); 
		if (wmAlarmParameters.superDetOnly && superDet==null) {
			return false;
		}
		return true;
	}

	/**
	 * @return the wmAlarmParameters
	 */
	public WMAlarmParameters getWmAlarmParameters() {
		return wmAlarmParameters;
	}

	/**
	 * @param wmAlarmParameters the wmAlarmParameters to set
	 */
	public void setWmAlarmParameters(WMAlarmParameters wmAlarmParameters) {
		this.wmAlarmParameters = wmAlarmParameters;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof WMAlarmParameters) {
			wmAlarmParameters = (WMAlarmParameters) dataSelectParams;
		}
	}

	@Override
	public DataSelectParams getParams() {
		return wmAlarmParameters;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

}
