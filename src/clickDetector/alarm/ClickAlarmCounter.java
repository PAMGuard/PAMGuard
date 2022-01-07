package clickDetector.alarm;

import java.awt.Window;
import java.io.Serializable;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.ClickIdentifier;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmDecibelCounter;
import alarm.AlarmParameters;

public class ClickAlarmCounter extends AlarmDecibelCounter implements PamSettings {

	private ClickControl clickControl;

	private ClickAlarmParameters clickAlarmParameters = new ClickAlarmParameters();

	private int lutLength;
	
	private long lastClickMilliseconds = 0;

	public ClickAlarmCounter(AlarmControl alarmControl, ClickControl clickControl) {
		super(alarmControl);
		this.clickControl = clickControl;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		ClickDetection click = (ClickDetection) dataUnit;
		if (clickAlarmParameters.useEchoes == false && click.isEcho()) {
			return 0;
		}
		ClickIdentifier clickIdentifier = clickControl.getClickIdentifier();
		int code = click.getClickType();
		if (code > 0 && clickIdentifier != null) {
			code = clickIdentifier.codeToListIndex(code) + 1;
		}
		boolean enabled = clickAlarmParameters.getUseSpecies(code);
		if (enabled == false) {
			return 0;
		}
		if (countType == AlarmParameters.COUNT_SIMPLE) {
			return 1;
		}
		if (clickAlarmParameters.scoreByAmplitude) {
			return click.getAmplitudeDB();
		}
		else {
			return clickAlarmParameters.getSpeciesWeight(code);
		}
	}

	/* (non-Javadoc)
	 * @see alarm.AlarmCounter#hasOptions()
	 */
	@Override
	public boolean hasOptions() {
		return true;
	}


	/* (non-Javadoc)
	 * @see alarm.AlarmCounter#showOptions(java.awt.Window)
	 */
	@Override
	public boolean showOptions(Window parent) {
		ClickAlarmParameters newParams = ClickAlarmDialog.showDialog(parent, clickControl, clickAlarmParameters);
		if (newParams == null) {
			return false;
		}
		clickAlarmParameters = newParams.clone();
		return true;
	}

	@Override
	public Serializable getSettingsReference() {
		return clickAlarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ClickAlarmParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return getAlarmControl().getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Click Alarm Parameters";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		clickAlarmParameters = ((ClickAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/* (non-Javadoc)
	 * @see alarm.AlarmDecibelCounter#addCount(double, double, int)
	 */
	@Override
	public double addCount(double currentValue, double countToAdd, int countType) {
		if (clickAlarmParameters.scoreByAmplitude) {
			return super.addCount(currentValue, countToAdd, countType);
		}
		else if (countType == AlarmParameters.COUNT_SIMPLE) {
			return currentValue + 1;
		}
		else {
			return currentValue + countToAdd;
		}
	}

	/* (non-Javadoc)
	 * @see alarm.AlarmDecibelCounter#subtractCount(double, double, int)
	 */
	@Override
	public double subtractCount(double currentValue, double countToSubtract,
			int countType) {
		if (clickAlarmParameters.scoreByAmplitude) {
			return super.subtractCount(currentValue, countToSubtract, countType);
		}
		else if (countType == AlarmParameters.COUNT_SIMPLE) {
			return currentValue - 1;
		}
		else {
			return currentValue - countToSubtract;
		}
	}

	@Override
	public void resetCounter() {
		// TODO Auto-generated method stub
	}

}
