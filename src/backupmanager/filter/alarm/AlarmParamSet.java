package backupmanager.filter.alarm;

import java.io.Serializable;

/**
 * Set of params for an individual alarm output. There is a table
 * of these in AlarmFilterParams. 
 * @author dg50
 *
 */
public class AlarmParamSet implements Serializable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * fire data actions on this alarm
	 */
	public boolean useAlarm;
	
	/**
	 * Period before the alarm that's to be kept (millis)
	 */
	public long prePeriod;
	
	/**
	 * Period after the alarm that's to be kept (millis)
	 */
	public long postPeriod;

}
