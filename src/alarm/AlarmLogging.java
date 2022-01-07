package alarm;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class AlarmLogging extends SQLLogging {

	private AlarmDataBlock alarmDataBlock;
	private PamTableItem[] levelStart = new PamTableItem[AlarmParameters.COUNT_LEVELS+1];
	private PamTableItem[] levelEnd = new PamTableItem[AlarmParameters.COUNT_LEVELS+1];
	private PamTableItem highestState, highestScore;

	public AlarmLogging(AlarmDataBlock alarmDataBlock) {
		super(alarmDataBlock);
		this.alarmDataBlock = alarmDataBlock;
		PamTableDefinition tableDef = new PamTableDefinition(alarmDataBlock.getDataName(), UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(highestState = new PamTableItem("Highest_State", Types.SMALLINT));
		tableDef.addTableItem(highestScore = new PamTableItem("Highest_Score", Types.REAL));
		for (int i = 1; i <= AlarmParameters.COUNT_LEVELS; i++) {
			levelStart[i] = new PamTableItem(String.format("State_%d_Start", i), Types.TIMESTAMP);
			levelEnd[i] = new PamTableItem(String.format("State_%d_End", i), Types.TIMESTAMP);
			tableDef.addTableItem(levelStart[i]);
			tableDef.addTableItem(levelEnd[i]);
		}
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		AlarmDataUnit adu = (AlarmDataUnit) pamDataUnit;
		highestState.setValue(new Short((short) adu.getHighestStatus()));
		highestScore.setValue(new Float(adu.getHighestScore()));
		long firstTimes[] = adu.getFirstStateTime();
		long lastTimes[] = adu.getLastStateTime();
		for (int i = 1; i <= AlarmParameters.COUNT_LEVELS; i++) {
			if (firstTimes[i] == 0) {
				levelStart[i].setValue(null);
			}
			else {
				levelStart[i].setValue(sqlTypes.getTimeStamp(firstTimes[i]));
			}
			if (lastTimes[i] == 0) {
				levelEnd[i].setValue(null);
			}
			else {
				levelEnd[i].setValue(sqlTypes.getTimeStamp(lastTimes[i]));
			}
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		// 1 indxed !
		long[] levStart = new long[AlarmParameters.COUNT_LEVELS+1];
		long[] levEnd = new long[AlarmParameters.COUNT_LEVELS+1];
		for (int i = 1; i <= AlarmParameters.COUNT_LEVELS; i++) {
			Long val = SQLTypes.millisFromTimeStamp(levelStart[i].getValue());
			if (val != null) {
				levStart[i] = val;
			}
			val = SQLTypes.millisFromTimeStamp(levelEnd[i].getValue());
			if (val != null) {
				levEnd[i] = val;
			}
		}
		double highScore = highestScore.getDoubleValue();
		int highState = highestState.getIntegerValue();
		AlarmDataUnit adu = new AlarmDataUnit(timeMilliseconds, levStart, levEnd, highState, highScore);
		return adu;
	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		AlarmDataUnit adu = (AlarmDataUnit) dataUnit;
		if (adu.isActive()) {
			return true;
		}
		return super.logData(con, dataUnit);
	}

	@Override
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit) {
		AlarmDataUnit adu = (AlarmDataUnit) dataUnit;
		if (adu.isActive() == false) {
			return logData(con, dataUnit);
		}
		else {
			return true;
		}
	}

}
