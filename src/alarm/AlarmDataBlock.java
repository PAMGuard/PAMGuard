package alarm;

import PamguardMVC.PamDataBlock;

public class AlarmDataBlock extends PamDataBlock<AlarmDataUnit> {

	public AlarmDataBlock(AlarmProcess alarmProcess, String name) {
		super(AlarmDataUnit.class, name, alarmProcess, 0);
		setClearAtStart(false);
	}

}
