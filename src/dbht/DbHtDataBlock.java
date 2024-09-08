package dbht;

import PamguardMVC.PamDataBlock;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import dbht.alarm.DbHtAlarmProvider;

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
