package loggerForms.loggeraudio;

import java.io.Serializable;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import loggerForms.network.LoggerNetworkObserver;

public class LoggerAudioControl extends PamControlledUnit implements LoggerNetworkObserver, PamSettings {
	
	public static final String unitTupe = "Logger Audio";
	
	private LoggerAudioProcess loggerAudioProcess;
	
	private LoggerAudioSettings loggerAudioSettings = new LoggerAudioSettings();

	public LoggerAudioControl(String unitName) {
		super(unitTupe, unitName);
		loggerAudioProcess = new LoggerAudioProcess(this);
		addPamProcess(loggerAudioProcess);
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * @return the loggerAudioSettings
	 */
	public LoggerAudioSettings getLoggerAudioSettings() {
		return loggerAudioSettings;
	}

	@Override
	public void updateState(boolean connected, int nClient) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Serializable getSettingsReference() {
		return loggerAudioSettings;
	}

	@Override
	public long getSettingsVersion() {
		return LoggerAudioSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		loggerAudioSettings = (LoggerAudioSettings) pamControlledUnitSettings.getSettings();
		return true;
	}
	
	


}
