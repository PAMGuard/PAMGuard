package dbht.alarm;

import dbht.DbHtControl;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class DbHtAlarmProvider extends AlarmCounterProvider {

	private DbHtControl dbHtControl;

	public DbHtAlarmProvider(DbHtControl dbHtControl) {
		this.dbHtControl = dbHtControl;
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new DbHtAlarmCounter(alarmControl, dbHtControl);
	}

}
