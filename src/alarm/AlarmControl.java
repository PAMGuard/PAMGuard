package alarm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSidePanel;
//import alarm.actions.ActionSendEmail;
import alarm.actions.AlarmAction;
import alarm.actions.email.SendEmailAction;
import alarm.actions.serial.AlarmSerialAction;
import alarm.actions.sound.PlaySound;
import alarm.actions.udp.AlarmUDPAction;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class AlarmControl extends PamControlledUnit implements PamSettings {

	private AlarmProcess alarmProcess;
	
	protected AlarmSidePanel alarmSidePanel;
	
	protected AlarmParameters alarmParameters = new AlarmParameters();
	
	private AlarmDisplayProvider alarmDisplayProvider;
	
	protected ArrayList<AlarmAction> alarmActions = new ArrayList<AlarmAction>();

	private OLProcessDialog offlineDialog;

	private OfflineTaskGroup offlineTaskGroup;

	protected AlarmOfflineTask alarmOfflineTask;

	public AlarmControl(String unitName) {
		super("Alarm", unitName);
		addPamProcess(alarmProcess = new AlarmProcess(this));
		alarmSidePanel = new AlarmSidePanel(this);
		PamSettingManager.getInstance().registerSettings(this);
		alarmDisplayProvider = new AlarmDisplayProvider();
		UserDisplayControl.addUserDisplayProvider(alarmDisplayProvider);
		
		alarmActions.add(new PlaySound(this));
		alarmActions.add(new AlarmSerialAction(this));
		alarmActions.add(new SendEmailAction(this));
		alarmActions.add(new AlarmUDPAction(this));
//		alarmActions.add(new TastAction(this)); // uncomment when alarm action string ready
		
		createOfflineTasks();
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getSidePanel()
	 */
	@Override
	public PamSidePanel getSidePanel() {
		return alarmSidePanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new AlarmMenu(parentFrame));
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
		return menuItem;
		}
		else {
			JMenu menu = new JMenu(getUnitName());
			menu.add(menuItem);
			menuItem = new JMenuItem("Offline Processing ...");
			menuItem.addActionListener(new OfflineMenu(parentFrame));
			menu.add(menuItem);
			return menu;
		}
	}

	private class AlarmMenu implements ActionListener {

		private Frame parentFrame;
		
		public AlarmMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showAlarmDialog(parentFrame);
		}
		
	}
	private class OfflineMenu implements ActionListener {

		private Frame parentFrame;
		
		public OfflineMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			offlineProcessing(parentFrame);
		}
		
	}
	
	private void offlineProcessing(Frame parentFrame) {
		if (offlineDialog == null) {
			offlineDialog = new OLProcessDialog(parentFrame, offlineTaskGroup, getUnitName());
		}
		offlineDialog.setVisible(true);
	}
	
	private void createOfflineTasks() {
		offlineTaskGroup = new OfflineTaskGroup(this, "Noise Processing");
		offlineTaskGroup.addTask(alarmOfflineTask = new AlarmOfflineTask(this));
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			setupAlarm();
			break;
		}
	}

	protected boolean showAlarmDialog(Frame parentFrame) {
		AlarmParameters newParams = AlarmDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			alarmParameters = newParams.clone();
			setupAlarm();
			return true;
		}
		else {
			return false;
		}
	}

	private void setupAlarm() {
		alarmProcess.setupAlarm();

		boolean[] enabledActions = alarmParameters.getEnabledActions();
		int nAct = Math.min(alarmActions.size(), enabledActions.length);
		for (int i = 0; i < nAct; i++) {
			if (!enabledActions[i]) {
				continue;
			}
			alarmActions.get(i).prepareAction();
		}
	}

	protected int fireAlarmActions(AlarmDataUnit alarmDataUnit) {
		int fired = 0;
		boolean[] enabledActions = alarmParameters.getEnabledActions();
		int nAct = Math.min(alarmActions.size(), enabledActions.length);
		for (int i = 0; i < nAct; i++) {
			if (!enabledActions[i]) {
				continue;
			}
			alarmActions.get(i).actOnAlarm(alarmDataUnit);
		}
		return fired;
	}

	@Override
	public Serializable getSettingsReference() {
		return alarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return AlarmParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		alarmParameters = ((AlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	public int getAlarmStatus() {
		return alarmProcess.getAlarmStatus();
	}
	
	private class AlarmDisplayProvider implements UserDisplayProvider {

		@Override
		public String getName() {
			return getUnitName() + " history";
		}

		@Override
		public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
			return getDisplayPanelComponent();
		}

		@Override
		public Class getComponentClass() {
			return AlarmDisplayTable.class;
		}

		@Override
		public int getMaxDisplays() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean canCreate() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void removeDisplay(UserDisplayComponent component) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private AlarmDisplayTable getDisplayPanelComponent() {
		AlarmDisplayTable dt = new AlarmDisplayTable(this);
		return dt;
	}

	public AlarmProcess getAlarmProcess() {
		return alarmProcess;
	}
}
