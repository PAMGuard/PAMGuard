package loggerForms.network;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import pamguard.CommandLine;

public class LoggerNetworkSystem implements PamSettings {

	private static LoggerNetworkSystem singleInstance;
	
	private LoggerNetworkSettings loggerNetworkSettings = new LoggerNetworkSettings();
	
	private LoggerNetworkManager currentManager;
	
	private LoggerMQTTManager mqttManager;
	
	private LoggerMulticastManager udpManager;
	
	private LoggerDummyNetwork dummy;

	private LoggerNetworkSystem() {
		PamSettingManager.getInstance().registerSettings(this);
		setupNetworkManager();
	}

	public static LoggerNetworkSystem getInstance() {
		if (singleInstance == null) {
			singleInstance = new LoggerNetworkSystem();
		}
		return singleInstance;
	}

	public JMenuItem getMenuItem(Window window) { 
		if (CommandLine.getCommandLine().hasCommand("-SMRU") == false) {
			return null;
		}
		JMenuItem item = new JMenuItem("Logger network options ...");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showNetworkOptions(window);
			}

		});
		return item;
	}

	private void showNetworkOptions(Window window) {
		LoggerNetworkSettings newSettings = LoggerNetworkDialog.showDialog(window, loggerNetworkSettings);
		if (newSettings != null && newSettings.loggerNetworkType != loggerNetworkSettings.loggerNetworkType) {
			loggerNetworkSettings = newSettings;
			setupNetworkManager();
		}
	}

	private void setupNetworkManager() {
		LoggerNetworkManager man = selectMamager(loggerNetworkSettings.loggerNetworkType);
		if (man == currentManager) {
			return;
		}
		if (currentManager != null) {
			currentManager.closeListener();
		}
		currentManager = man;
		if (currentManager != null) {
			currentManager.setupListener();
		}
	}
	
	private LoggerNetworkManager selectMamager(int type) {
		switch (type) {
		case LoggerNetworkSettings.LOG_NET_NONE:
			if (dummy == null) {
				dummy = new LoggerDummyNetwork();
			}
			return dummy;
		case LoggerNetworkSettings.LOG_NET_MQTT:
			if (mqttManager == null) {
				mqttManager = new LoggerMQTTManager();
			}
			return mqttManager;
		case LoggerNetworkSettings.LOG_NET_UDP:
			if (udpManager == null) {
				udpManager = new LoggerMulticastManager();
			}
			return udpManager;
		}
		return null;
	}

	public static LoggerNetworkManager getManager() {
		return getInstance().currentManager;
	}

	@Override
	public String getUnitName() {
		return "Logger Network Manager";
	}

	@Override
	public String getUnitType() {
		return "Logger Network Manager";
	}

	@Override
	public Serializable getSettingsReference() {
		return loggerNetworkSettings;
	}

	@Override
	public long getSettingsVersion() {
		return LoggerNetworkSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.loggerNetworkSettings = (LoggerNetworkSettings) pamControlledUnitSettings.getSettings();
		return true;
	}
}
