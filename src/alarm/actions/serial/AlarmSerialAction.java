package alarm.actions.serial;

import java.awt.Window;

import PamUtils.PamCalendar;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmDataUnit;
import alarm.actions.AlarmAction;

/**
 * Alarm action for serial port output. Since all alarms will share 
 * a serial port, will need to wrap most of the functionality up in a 
 * singleton class that does the actual work of writing to the 
 * serial port.  
 * @author Doug Gillespie
 *
 */
public class AlarmSerialAction extends AlarmAction {

	public AlarmSerialAction(AlarmControl alarmControl) {
		super(alarmControl);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionName() {
		return "Serial Port Output";
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean setSettings(Window window) {
		return AlarmSerialInterface.getInstance(alarmControl).setSettings(window);
	}

	@Override
	public boolean actOnAlarm(AlarmDataUnit alarmDataUnit) {
		String alarmString  = createAlarmString(alarmDataUnit);
		if (alarmString != null) {
			return AlarmSerialInterface.getInstance(alarmControl).writeString(alarmString);
		}
		else {
			return false;
		}
	}

	@Override
	public int canDo() {
		// should probably return 0 if serial port an't be opened. 
		return AlarmSerialInterface.getInstance(alarmControl).isPortOk() ? 1:0;
	}

	/* (non-Javadoc)
	 * @see alarm.actions.AlarmAction#prepareAction()
	 */
	@Override
	public boolean prepareAction() {
		/*
		 * Since the serial interface is shared between alarms, 
		 * all we need to do is to call getInstance which will 
		 * call the constructor, loading settings if necessary. 
		 * It will then all isPortOk. 
		 * If the instance is already there, it won't call the constructor
		 * so it just calls isPortOk. 
		 */
		return AlarmSerialInterface.getInstance(alarmControl).isPortOk();
	}

}
