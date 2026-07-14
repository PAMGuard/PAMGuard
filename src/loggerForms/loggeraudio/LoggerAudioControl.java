package loggerForms.loggeraudio;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import loggerForms.actions.ActionOwner;
import loggerForms.actions.LoggerActions;
import loggerForms.loggeraudio.swing.LoggerAudioDialog;
import loggerForms.network.LoggerNetworkObserver;

public class LoggerAudioControl extends PamControlledUnit implements LoggerNetworkObserver, PamSettings, ActionOwner {
	
	public static final String unitTupe = "Logger Audio";
	
	private LoggerAudioProcess loggerAudioProcess;
	
	private LoggerAudioSettings loggerAudioSettings = new LoggerAudioSettings();
	
	private HashMap<String, LoggerAudioAction> localActionsMap = new HashMap<>();

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
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsMenu(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showSettingsMenu(Frame parentFrame) {
		LoggerAudioSettings newSettings = LoggerAudioDialog.showDialog(parentFrame, loggerAudioSettings);
		if (newSettings != null) {
			loggerAudioSettings = newSettings;
			loggerAudioProcess.notifyModelChanged(PamController.INITIALIZATION_COMPLETE);
		}
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
	
	/**
	 * Check all the logger actions are correctly available and registered.
	 * this gets called at startup and after a new platform has been added. 
	 * A bit tricky since the action map won't build until data have been received
	 * at least once, and old actions may get left in, so could get a bit weird.  
	 */
	public void checkActionsMap() {
		Set<String> platNames = loggerAudioSettings.getPlatformNames();
		for (String name : platNames) {
			checkActionsMap(name);
		}
	}
	
	public void checkActionsMap(String name) {

		LoggerAudioAction action = localActionsMap.get(name);
		if (action == null) {
			action = new LoggerAudioAction(this, name);
			localActionsMap.put(name, action);
			LoggerActions loggerActions = LoggerActions.getInstance();
			loggerActions.registerAction(action);
		}
	}

	/**
	 * @return the loggerAudioProcess
	 */
	public LoggerAudioProcess getLoggerAudioProcess() {
		return loggerAudioProcess;
	}
	


}
