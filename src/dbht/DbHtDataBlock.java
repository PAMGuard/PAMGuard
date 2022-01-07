package dbht;

import dbht.alarm.DbHtAlarmCounter;
import dbht.alarm.DbHtAlarmProvider;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class DbHtDataBlock extends PamDataBlock<DbHtDataUnit> implements AlarmDataSource {

	private DbHtAlarmProvider dbHtAlarmProvider;
	private DbHtControl dbHtControl;
	
	public DbHtDataBlock(String dataName,
			DbHtProcess parentProcess, int channelMap) {
		super(DbHtDataUnit.class, dataName, parentProcess, channelMap);
		dbHtControl = parentProcess.dbHtControl;
	}

	@Override
	public AlarmCounterProvider getAlarmCounterProvider() {
		if (dbHtAlarmProvider == null) {
			dbHtAlarmProvider = new DbHtAlarmProvider(dbHtControl);
		}
		return dbHtAlarmProvider;
	}

}
