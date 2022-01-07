package whistlesAndMoans.alarm;

import java.awt.Window;
import java.io.Serializable;

import whistlesAndMoans.ConnectedRegionDataUnit;
import whistlesAndMoans.WhistleMoanControl;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmDecibelCounter;
import alarm.AlarmParameters;

public class WMAlarmCounter extends AlarmDecibelCounter implements PamSettings {

	private WhistleMoanControl whistleMoanControl;
	private WMAlarmParameters wmAlarmParameters = new WMAlarmParameters();

	public WMAlarmCounter(AlarmControl alarmControl, WhistleMoanControl whistleMoanControl) {
		super(alarmControl);
		this.whistleMoanControl = whistleMoanControl;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		ConnectedRegionDataUnit crDataUnit = (ConnectedRegionDataUnit) dataUnit;
		if (wantWhistle(crDataUnit) == false) {
			return 0;
		}
		if (countType == AlarmParameters.COUNT_SCORES) {
			return crDataUnit.getAmplitudeDB();
		}
		else {
			return 1;
		}
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
		return true;
	}

	@Override
	public void resetCounter() {

	}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window parent) {
		WMAlarmParameters newParams = WMAlarmDialog.showDialog(parent, wmAlarmParameters);
		if (newParams != null) {
			wmAlarmParameters = newParams.clone();
			return true;
		}
		return false;
	}

	@Override
	public String getUnitName() {
		return getAlarmControl().getUnitName();
	}

	@Override
	public String getUnitType() {
		return "WhistleMoanAlarmParameters";
	}

	@Override
	public Serializable getSettingsReference() {
		return wmAlarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return WMAlarmParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.wmAlarmParameters = ((WMAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return wmAlarmParameters != null;
	}

}
