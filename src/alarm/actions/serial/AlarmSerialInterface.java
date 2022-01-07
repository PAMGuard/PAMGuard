package alarm.actions.serial;

import java.awt.Window;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JOptionPane;

import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialException;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import alarm.AlarmControl;

/**
 * Singleton class for all alarm actions to share a single serial port, it's settings
 * etc. 
 * @author Doug Gillespie
 *
 */
public class AlarmSerialInterface implements PamSettings {

	private AlarmControl alarmControl;
	private static AlarmSerialInterface singleInstance;
	private AlarmSerialSettings alarmSerialSettings = new AlarmSerialSettings();
	
	private PJSerialComm serialCom;
//	private OutputStream outputStream; removed - use PJSerialComm write method instead
	private static final String portIdName = "Alarm output";
	
	private AlarmSerialInterface(AlarmControl alarmControl) {
		this.alarmControl = alarmControl;
		PamSettingManager.getInstance().registerSettings(this);
		configureSerialPort();
	}
	
	synchronized static public final AlarmSerialInterface getInstance(AlarmControl alarmControl) {
		if (singleInstance == null) {
			singleInstance = new AlarmSerialInterface(alarmControl);
		}
		return singleInstance;
	}

	public synchronized boolean setSettings(Window window) {
		AlarmSerialSettings newSettings = AlarmSerialDialog.showDialog(window, alarmSerialSettings);
		if (newSettings != null) {
			alarmSerialSettings = newSettings.clone();
			configureSerialPort();
			return true;
		}
		else {
			return false;
		}
	}

	private synchronized void configureSerialPort() {
		closeSerialPort();
		try {
			serialCom = PJSerialComm.openSerialPort(alarmSerialSettings.portName, alarmSerialSettings.bitsPerSecond, 
					 alarmSerialSettings.dataBits, alarmSerialSettings.stopBits, 
					 alarmSerialSettings.parity, alarmSerialSettings.flowControl);
		} catch (PJSerialException e) {
			System.err.println("Alarm comm port unavailable: " + e.getMessage());
			String str = String.format("Serial Port %s is unavailable: %s",
					alarmSerialSettings.portName, e.getMessage());
			JOptionPane.showMessageDialog(null, str, "Alarm Com port error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (serialCom == null) {
			return;
		}

//		outputStream = serialCom.getOutputStream();
		System.out.println("Comm port for alarm output successfull opened on " + alarmSerialSettings.portName);
	}
	
	private void closeSerialPort() {
		if (serialCom != null) {
			serialCom.closePort();
			serialCom = null;
		}
//		outputStream = null;
	}

	public boolean isPortOk() {
		return (serialCom != null && serialCom.checkPortStatus());
	}
	
	/**
	 * Write a string to the serial port. 
	 * @param outputString string to write to serial port. 
	 * @return true if string written successfully.
	 */
	public synchronized boolean writeString(String outputString) {
		if (serialCom==null) {
			System.out.println(String.format("Serial Port %s is unavailable",
					alarmSerialSettings.portName));
			return false;
		}
//		if (alarmSerialSettings.getMakeLineFeed()) {
//			outputString = checkLineFeed(outputString);
//		}
		if (alarmSerialSettings.printStrings) {
			System.out.print("Output alarm string " +  outputString);
		}
//		if (outputStream == null) {
//			System.out.println("Error writing alarm string because com port is not open ");
//			return false;
//		}
		try {
			serialCom.writeToPort(outputString, alarmSerialSettings.getMakeLineFeed());
//			outputStream.write(outputString.getBytes());
		} catch (IOException e) {
			System.out.println("Error writing alarm string to com port: " + e.getMessage());
			return false;
		}
		return true;
	}
	
//	private String checkLineFeed(String outputString) {
//		if (outputString == null || outputString.length() == 0) {
//			return outputString;
//		}
//		char lastCh = outputString.charAt(outputString.length()-1);
//		if (lastCh == '\n' || lastCh == '\r') {
//			return outputString;
//		}
//		else {
//			return outputString + "\n";
//		}
//	}

	@Override
	public String getUnitName() {
		// changed to return AlarmControl class name, so that the serial settings would be included
		// automatically in the XML output.  Also had to pass alarmControl into the getInstance method
//		return "Alarm Serial Settings";
		return alarmControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Alarm Serial Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return alarmSerialSettings;
	}

	@Override
	public long getSettingsVersion() {
		return AlarmSerialSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		alarmSerialSettings = ((AlarmSerialSettings) pamControlledUnitSettings.getSettings()).clone();
		return alarmSerialSettings != null;
	}

}
