package alarm;

import java.awt.Window;

import PamguardMVC.PamDataUnit;

public interface AlarmDataSource {

	/**
	 * Get an alarmCounterProvider which can then generate
	 * any number of alarm counters for a datablock. 
	 * @return alarm Counter provider for that control. 
	 * This more complicated system enables multiple alarms to be
	 * hung off the same data block. 
	 */
	public AlarmCounterProvider getAlarmCounterProvider();
	
}
