package alarm.actions.udp;

import java.awt.Window;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import alarm.AlarmControl;
import alarm.AlarmDataUnit;
import alarm.actions.AlarmAction;
import warnings.PamWarning;
import warnings.QuickWarning;

public class AlarmUDPAction extends AlarmAction implements PamSettings {
	
	private static final String actionName = "UDP Message";
	
	private AlarmUDPParams alarmUDPParams = new AlarmUDPParams();
	
	private QuickWarning quickWarning;

	public AlarmUDPAction(AlarmControl alarmControl) {
		super(alarmControl);
		PamSettingManager.getInstance().registerSettings(this);
		quickWarning = new QuickWarning(alarmControl.getUnitName() + " UDP Messages");
	}
	
	@Override
	public String getActionName() {
		return actionName;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean setSettings(Window window) {
		AlarmUDPParams newParams = AlarmUDPDialog.showDialog(window, alarmUDPParams);
		if (newParams != null) {
			alarmUDPParams = newParams;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean actOnAlarm(AlarmDataUnit alarmDataUnit) {
		String alarmString = createAlarmString(alarmDataUnit);
		if (alarmString == null) {
			return false;
		}
//		alarmString += "\n";
		byte[] data = alarmString.getBytes();
		InetAddress address;
		try {
			address = InetAddress.getByName(alarmUDPParams.destAddr);
			DatagramPacket dp = new DatagramPacket(data, data.length, address, alarmUDPParams.destPort);
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(dp);
			dsocket.close();
		} catch (UnknownHostException e) {
			quickWarning.setWarning(e.getMessage(), 2);
			return false;
		} catch (IOException e) {
			quickWarning.setWarning(e.getMessage(), 2);
			return false;
		}
		quickWarning.setWarning("Alarm sent:" + alarmString, 1);
		return true;
	}
	
	PamWarning getit() {
		return null;
	}

	@Override
	public int canDo() {
		return -1;
	}

	@Override
	public String getUnitName() {
		return alarmControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "UDP Alarm Action";
	}

	@Override
	public Serializable getSettingsReference() {
		return alarmUDPParams;
	}

	@Override
	public long getSettingsVersion() {
		return AlarmUDPParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		alarmUDPParams = (AlarmUDPParams) pamControlledUnitSettings.getSettings();
		return true;
	}

}
