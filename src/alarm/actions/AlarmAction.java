package alarm.actions;

import java.awt.Window;

import PamUtils.PamCalendar;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmDataUnit;

public abstract class AlarmAction {
	
	public static final int ALARM_CANT_DO = 0;
	public static final int ALARM_CAN_DO = 1;
	public static final int ALARM_DONT_KNOW = -1;
	
	protected AlarmControl alarmControl;
	
	public AlarmAction(AlarmControl alarmControl) {
		super();
		this.alarmControl = alarmControl;
	}

	/**
	 * 
	 * @return the name of the alarm action
	 */
	abstract public String getActionName();
	
	/**
	 * 
	 * @return true if the action has configurable settings
	 */
	abstract public boolean hasSettings();
	
	/**
	 * Open an action specific dialog to configure the action
	 * @param window parent window
	 * @return true if settings changed. 
	 */
	abstract public boolean setSettings(Window window);
	
	/**
	 * Act on the alarm - called every time the data unit is updated. 
	 * @param alarmDataUnit alarm data unit that has changed. 
	 * @return true if action completed ok
	 */
	abstract public boolean actOnAlarm(AlarmDataUnit alarmDataUnit);
	
	/**
	 * Alarm action can complete (i.e. some tests have been conducted) 
	 * @return 0 = no, 1 = yes, -1 = don't know. 
	 */
	abstract public int canDo();
	
	/**
	 * Called when PAMGuard initialises to prepare any alarm actions, e.g. 
	 * open a serial port, find a file, etc. 
	 * @return true if preparation completed Ok. 
	 */
	public boolean prepareAction() {
		return true;
	}

	/**
	 * Create the alarm string according to standard pgxxx/v2.34.700
	 * @param alarmDataUnit
	 * @return a string to write to the serial port. 
	 */
	protected String createAlarmString(AlarmDataUnit alarmDataUnit) {
		if (alarmDataUnit == null) return null;
		long alarmTime = alarmDataUnit.getLastUpdate();
		if (alarmTime <= 0) {
			alarmTime = alarmDataUnit.getTimeMilliseconds();
		}
		String str = String.format("$PGALM,%s,%s,%s,%d,%3.1f,", alarmControl.getUnitName(), // alarm name
				PamCalendar.formatCompactDate(alarmTime, false), // date stamp
				PamCalendar.formatTime2(alarmTime,0, false), // date stamp
				alarmDataUnit.getCurrentStatus(), // current alarm status
				alarmDataUnit.getCurrentScore());
		String[] extraData = alarmDataUnit.getExtraFieldData();
		if (extraData == null) {
			str += "0";
		}
		else {
			str += extraData.length;
			AlarmCounter alarmCounter = alarmControl.getAlarmProcess().getAlarmCounter();
			String[] fieldNames = alarmCounter.getExtraFieldNames();
			for (int i = 0; i < extraData.length; i++) {
				str += String.format(",%s,%s", fieldNames[i], extraData[i]); 
			}
		}
		byte checkSum = calcCheckSum(str, 1);
		str += String.format("*%02X\r\n", checkSum);
		return str;
	}
	/**
	 * Calculate a checksum for the string, starting at the i'th character
	 * @param str string to calculate checksum for
	 * @param startIndex index of start character for sum 
	 * @return checksum. 
	 */
	private byte calcCheckSum(String str, int startIndex) {
		byte s = 0;
		byte[] strBytes = str.getBytes();
		for (int i = startIndex; i < strBytes.length; i++) {
			s ^= strBytes[i];
		}
		return s;
	}

	
}
