package dbht.alarm;

import java.awt.Window;
import java.io.Serializable;

import dbht.DbHtControl;
import dbht.DbHtDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmDecibelCounter;

public class DbHtAlarmCounter extends AlarmDecibelCounter implements PamSettings {

	protected DbHtAlarmParameters dbHtAlarmParameters = new DbHtAlarmParameters();
	private DbHtControl dbHtControl;
	
	public DbHtAlarmCounter(AlarmControl alarmControl, DbHtControl dbHtControl) {
		super(alarmControl);
		this.dbHtControl = dbHtControl;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		DbHtDataUnit dbHtDataUnit = (DbHtDataUnit) dataUnit;
		return dbHtDataUnit.getMeasure(dbHtAlarmParameters.returnedMeasure);
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
		DbHtAlarmParameters newParams = DbHtAlarmParamsDialog.showDialog(parent, this);
		if (newParams != null) {
			dbHtAlarmParameters = newParams.clone();
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
		return "DBHT ALarm Counter";
	}

	@Override
	public Serializable getSettingsReference() {
		return dbHtAlarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DbHtAlarmParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		dbHtAlarmParameters = ((DbHtAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return (dbHtAlarmParameters != null);
	}

}
