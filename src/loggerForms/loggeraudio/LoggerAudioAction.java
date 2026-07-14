package loggerForms.loggeraudio;

import loggerForms.LoggerForm;
import loggerForms.actions.LoggerAction;
import loggerForms.controls.LoggerControl;

public class LoggerAudioAction extends LoggerAction {

	private String platformName;
	private LoggerAudioControl loggerAudioControl;

	public LoggerAudioAction(LoggerAudioControl actionOwner, String platformName) {
		super(actionOwner, "Record with buffer: " + platformName, "Record audio data from logger app with buffer");
		this.loggerAudioControl = actionOwner;
		this.platformName = platformName;
	}

	@Override
	public boolean runAction(LoggerForm loggerForm, LoggerControl loggerControl) {
		PlatformAudio platformAudio = loggerAudioControl.getLoggerAudioProcess().findPlatformAudio(platformName);
		if (platformAudio == null) {
			return false;
		}
		
		return platformAudio.makeRecording();
	}
	

}
