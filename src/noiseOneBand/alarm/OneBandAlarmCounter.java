package noiseOneBand.alarm;

import java.awt.Window;
import java.io.Serializable;

import noiseOneBand.OneBandAlarmParameters;
import noiseOneBand.OneBandControl;
import noiseOneBand.OneBandDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmDecibelCounter;

public class OneBandAlarmCounter extends AlarmDecibelCounter implements PamSettings {

	protected OneBandAlarmParameters oneBandAlarmParameters = new OneBandAlarmParameters();
	private OneBandControl oneBandControl;
	
	public OneBandAlarmCounter(AlarmControl alarmControl, OneBandControl oneBandControl) {
		super(alarmControl);
		this.oneBandControl = oneBandControl;
		PamSettingManager.getInstance().registerSettings(this);
	}
	

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		OneBandDataUnit oneBandDataUnit = (OneBandDataUnit) dataUnit;
		return oneBandDataUnit.getMeasure(oneBandAlarmParameters.getReturnedMeasure());
	}

	@Override
	public void resetCounter() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window parent) {
		OneBandAlarmParameters newParams = OneBandAlarmParamsDialog.showDialog(parent, this);
		if (newParams != null) {
			oneBandAlarmParameters = newParams.clone();
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
		return "Noise Alarm Counter";
	}

	@Override
	public Serializable getSettingsReference() {
		return oneBandAlarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return OneBandAlarmParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		oneBandAlarmParameters = ((OneBandAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return (oneBandAlarmParameters != null);
	}

}
