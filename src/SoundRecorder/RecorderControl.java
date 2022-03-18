package SoundRecorder;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.command.CommandManager;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.MenuItemEnabler;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import SoundRecorder.backup.RecorderBackupStream;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;
import SoundRecorder.trigger.TriggerDecisionMaker;
import backupmanager.BackupInformation;

/**
 * Control a sound file recorder. The sound file recorder has two
 * view panels - a main tab panel and also a smaller side panel which 
 * replicates some of the functionality of the main panel.
 * <p>
 * @author Doug Gillespie
 *@see SoundRecorder.RecorderProcess
 *@see SoundRecorder.RecorderSettings
 *@see SoundRecorder.RecorderTabPanel
 *@see SoundRecorder.RecorderSidePanel
 *@see SoundRecorder.RecorderStorage
 *@see SoundRecorder.RecorderView
 */
public class RecorderControl extends PamControlledUnit implements PamSettings {

	RecorderTabPanel recorderTabPanel;

	RecorderSidePanel recorderSidePanel;

	RecorderProcess recorderProcess;

	RecorderSettings recorderSettings;

	RecorderStorage recorderStorage;

	String actionTrigger;

	protected MenuItemEnabler menuEnabler = new MenuItemEnabler();

	int recorderStatus = IDLE;

	int pressedButton, lastPressedButton = -1;

	static public final int RECORDING = 1;
	static public final int IDLE      = 2;
	
	// add an X3 file type, for use with Decimus ONLY.  This is a cludge - the correct place would be in the X3 library as a real AudioFileFormat object
    public static final Type X3 = new Type("X3", "x3");


	ArrayList<RecorderView> recorderViews = new ArrayList<RecorderView>();

	protected static ArrayList<RecorderTrigger> recorderTriggers = new ArrayList<RecorderTrigger>();

	static ArrayList<RecorderControl> recorderControllers = new ArrayList<RecorderControl>();

	//	private long triggeredRecordingStart, 
	private long triggeredRecordingEnd = 0;

	private BackupInformation backupInformation;

	public static final String recorderUnitType = "Sound Recorder";

	public RecorderControl(String name) {

		super(recorderUnitType, name);

		RecorderControl.registerRecorders(this);

		recorderSettings = new RecorderSettings();

		addPamProcess(recorderProcess = new RecorderProcess(this));

		setTabPanel(recorderTabPanel = new RecorderTabPanel(this));

		setSidePanel(recorderSidePanel = new RecorderSidePanel(this));

		recorderViews.add(recorderTabPanel);
		recorderViews.add(recorderSidePanel);

		recorderStorage = new PamAudioFileStorage(this);

		PamSettingManager.getInstance().registerSettings(this);

		recorderTimer.start();

		buttonCommand(RecorderView.BUTTON_OFF);

		/*
		 * for any recorder trigger that is already registered globally, 
		 * make sure that it is added to this recorderControl. 
		 */
		for (int i = 0; i < recorderTriggers.size(); i++) {
			addRecorderTrigger(recorderTriggers.get(i));
		}

		//		if (recorderProcess.getParentDataBlock() == null) {
		//			recordSettingsDialog();
		//		}

		enableRecording();
		
		backupInformation = new BackupInformation(new RecorderBackupStream(this));
	}

	private boolean modelComplete = false;
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			modelComplete = true;
			newParams();
			break;
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (modelComplete) {
				newParams();
			}
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			if (modelComplete) {
				newParams();
			}
			break;
		}
	}

	/**
	 * Nothing to do with the automatic recordings - just a general timer
	 * that triggers all sorts of things.
	 */
	Timer recorderTimer = new Timer(500, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {

			recorderTabPanel.timerActions();

			setRecorderStatus();

			sayRecorderStatus();

		}
	});

	private boolean folderStatus;

	public int getRecorderStatus() {
		return recorderStatus;
	}

	public void sayRecorderStatus() {
		String str = "Unknown";
		switch (recorderStatus) {
		case IDLE:
			str = "Recorder idle";
			break;
		case RECORDING:
			str = "Recording to " + recorderStorage.getFileName();
			break;
		}
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).sayStatus(str);
		}
	}

	/**
	 * Enables and disables the main control buttons for starting / stopping
	 * depending on whether or not the ADC is running.
	 */
	public void enableRecording() {
		boolean enable = shouldEnableRecording();
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).enableRecording(enable);
		}
	}
	private boolean shouldEnableRecording() {
		boolean enable = recorderProcess.isDataComing();
		enable &= (recorderSettings.getChannelBitmap(0xFFFFFFFF) != 0);
		return enable;
	}
	/**
	 * Enables and disables controls on the views such as the channel selection
	 * buttons, the main settings button and also the menus..
	 * @param enable
	 */
	public void enableRecordingControl(boolean enable) {
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).enableRecordingControl(enable);
			menuEnabler.enableItems(enable);
		}
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {

		JMenuItem menu = new JMenuItem(getUnitName() + " settings ...");
		menu.addActionListener(new MenuRecordSettings(parentFrame));
		menuEnabler.addMenuItem(menu);
		return menu;
	}

	/**
	 * Sends data to various views of the recorder (generally level meters)
	 * @param dataBlock
	 * @param dataUnit
	 */
	public void newData(PamDataBlock dataBlock, PamDataUnit dataUnit) {
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).newData(dataBlock, dataUnit);
		}
	}

	protected void buttonCommand(int command) {
		pressedButton = command;

		/*
		 * don't repeat a command unless it's the stop button - 
		 * it may be necessary to press this to stop a recording
		 * triggered by one of the detectors. 
		 * Actually - an easier bodge is ...
		 */
		if (pressedButton == RecorderView.BUTTON_OFF) {
			triggeredRecordingEnd = 0;
		}
		if (pressedButton == lastPressedButton) return;
		lastPressedButton = pressedButton;

		//System.out.println("Button " + command);
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).setButtonStates(command);
		}
		/*
		 * The only button that needs definitive action is the 
		 * start_buffered button since this needs us to grab the buffer
		 * Other buttons instructions will need to be combined with 
		 * information from other places before we know if recording should 
		 * be started or stopped
		 */
		if (pressedButton == RecorderView.BUTTON_START_BUFFERED) {
			if (recorderSettings.enableBuffer) {
				recorderProcess.grabBuffer = recorderSettings.bufferLength;
			}
			else {
				recorderProcess.grabBuffer = 0;
			}
		}
		// then call this to handle everything else. 
		setRecorderStatus();

		if (PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING) {
			recorderSettings.oldStatus = pressedButton;
		}
	}

	public void setRecorderStatus(int recorderStatus) {

		if (this.recorderStatus != recorderStatus){
			this.recorderStatus = recorderStatus;
			recorderProcess.setRecordStatus(recorderStatus, actionTrigger);
			sayRecorderStatus();
			enableRecordingControl(recorderStatus == IDLE);
		}

	}

	protected void setFolderStatus(boolean folderOk) {
		folderStatus = folderOk;
	}

	/**
	 * Works out what the record status should be - the buttons only apply to local
	 * control, even if the off button is presses, it may be that a recording is being
	 * initiated by some other part of the system. Recording is effectively a 
	 * logical OR of the local control from buttons and timers in this module and
	 * instructions from other detectors. 
	 *
	 */
	private void setRecorderStatus() {
		// works out automatically what the record status should be.
		int recordStatus = IDLE;
		switch(pressedButton) {
		case RecorderView.BUTTON_OFF:
			recordStatus = IDLE;
			break;
		case RecorderView.BUTTON_AUTO:
			recordStatus = shouldAutoRecord();
			actionTrigger = "Auto Recording";
			break;
		case RecorderView.BUTTON_START:
		case RecorderView.BUTTON_START_BUFFERED:
			recordStatus = RECORDING;
			actionTrigger = "Manual Recording";
			break;
		}
		if (recordStatus == IDLE) {
			// check against the triggered recording stuff. 
			long now = PamCalendar.getTimeInMillis();
			if (now < triggeredRecordingEnd) {
				recordStatus = RECORDING;
			}
		}
		setRecorderStatus(recordStatus);
	}

	private int shouldAutoRecord() {
		// check the clock and work out if we should be auto recording or not. 
		long now = PamCalendar.getTimeInMillis();
		/*
		 * I'm not sure that just taking the modulus of this against the 
		 * number of seconds per day will give a number of seconds within 
		 * today, since leap seconds will shift things a bit, so need to subtract off
		 * the time at the start of today ...
		 */ 
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE), 0, 0, 0);
		long todayMilliSeconds = (now - cal.getTimeInMillis());
		long cyclePos = todayMilliSeconds % (recorderSettings.autoInterval*1000);
		if (cyclePos <= recorderSettings.autoDuration*1000) {
			return RECORDING;
		}
		else {
			return IDLE;
		}
	}

	class MenuRecordSettings implements ActionListener {
		private Frame parentFrame;

		public MenuRecordSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {
			recordSettingsDialog(parentFrame);
		}
	}

	protected void recordSettingsDialog(Frame parentFrame) {
		RecorderSettings newSettings = RecorderSettingsDialog.showDialog(parentFrame, recorderSettings);
		if (newSettings != null) {
			recorderSettings = newSettings.clone();
			newParams();
		}
	}

	protected void newParams() {
		// set the data source for recording
		recorderSettings.createTriggerDataList(recorderTriggers);
		PamRawDataBlock rawDataBlock = PamController.getInstance().getRawDataBlock(recorderSettings.rawDataSource);// = clickParameters.rawDataSource;
		if (rawDataBlock == null) {
			return;
		}
		recorderProcess.setParentDataBlock(rawDataBlock);
		recorderSettings.setChannelBitmap(recorderSettings.getChannelBitmap(rawDataBlock.getChannelMap()));

		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).newParams();
		}

		selectRecorderStorage();
	}

	/**
	 * Select the type of recorder storage. 
	 */
	void selectRecorderStorage() {
		//		AudioFileFormat.Type.
		if (recorderSettings.getFileType() == AudioFileFormat.Type.WAVE) {
			recorderStorage = new WavFileStorage(this); 
		}
		else {
			recorderStorage = new PamAudioFileStorage(this);
		}
	}

	/**
	 * Called mainly so that the samplerate notification from
	 * Acquisition can be used to check the number of channels
	 * is correctly displayed. 
	 * @param sampleRate
	 */
	protected void setSampleRate(float sampleRate) {
		if (recorderTabPanel == null) {
			return;
		}
		for (int i = 0; i < recorderViews.size(); i++) {
			recorderViews.get(i).newParams();
		}
	}

	public Serializable getSettingsReference() {
		//		if (recorderTriggers.size() > 0) {
		//			for (int i = recorderTriggers.size()-1; i >= 0; i--) {
		//				recorderSettings.setEnableTrigger(i, recorderTabPanel.isTriggerEnabled(i));
		//			}
		//		}
		recorderSettings.cleanTriggerDataList(recorderTriggers);
		return recorderSettings;
	}

	public long getSettingsVersion() {
		return RecorderSettings.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		recorderSettings = ((RecorderSettings) pamControlledUnitSettings.getSettings()).clone();
		newParams();
		return true;
	}


	/**
	 * Adds a recorder trigger to this recorderControl. This can either be called
	 * from the constructor as the recordercontrol is created or from the static
	 * registerRecorderTrigger function. 
	 * @param recorderTrigger
	 */
	private void addRecorderTrigger(RecorderTrigger recorderTrigger) {
		recorderTabPanel.addRecorderTrigger();
	}


	/**
	 * static version of actionRecordTrigger can be called
	 * from anywhere in PamGuard. It checks the index of the
	 * recordTrigger in the static list and then passes this 
	 * out to individual recorders which can each check whether
	 * or not that trigger is enabled for that particular recorder
	 * @param recorderTrigger
	 */
	public static void actionRecorderTrigger(RecorderTrigger recorderTrigger, PamDataUnit dataUnit, long timeNow) {
		// first find the index of this trigger. 
		for (int i = 0; i < recorderControllers.size(); i++) {
			recorderControllers.get(i).actionRecordTrigger(recorderTrigger, dataUnit, timeNow);
		}
	}

	/**
	 * Called when an event triggers to start a recording (or
	 * continue a recording if already active).
	 * @param recorderTrigger
	 */
	private void actionRecordTrigger(RecorderTrigger recorderTrigger, PamDataUnit dataUnit, long timeNow) {

		// now find out if it's enabled or not
		RecorderTriggerData rtData = recorderSettings.findTriggerData(recorderTrigger);

		boolean enabled = rtData.isEnabled();
		if (enabled == false) return;

		if (recorderTrigger.triggerDataUnit(dataUnit, rtData) == false) {
			return;
		}
		/*
		 * rtData now contains loads more information about budgets, etc which need to be examined before we can 
		 * decide whether or not to go ahead with the recording. 
		 */		
		// find out that the recorder can actually record at the moment
		if (shouldEnableRecording() == false) return;

		TriggerDecisionMaker tdm = rtData.getDecisionMaker();

		int availChanns = 0xFFFFFFFF;
		if (recorderProcess.getParentDataBlock() != null) {
			availChanns = recorderProcess.getParentDataBlock().getChannelMap();
		}
		int nChan = PamUtils.getNumChannels(recorderSettings.getChannelBitmap(availChanns));
		double wantedSecs = tdm.canRecord(rtData, timeNow, nChan, recorderProcess.getSampleRate(), 2);
		String message = tdm.getMessage();
		if (message != null) {
			recorderTabPanel.setTriggerMessage(rtData.getTriggerName() + " " + message);
		}
		if (wantedSecs <= 0) {
			return;
		}

		//triggeredRecordingStart = now - recorderTrigger.getTriggerData().getSecondsBeforeTrigger() * 1000;
		recorderProcess.grabBuffer = rtData.getSecondsBeforeTrigger();
		triggeredRecordingEnd = Math.max(triggeredRecordingEnd, timeNow + (long) (wantedSecs * 1000.));

		actionTrigger = "Triggered by " + recorderTrigger.getDefaultTriggerData().getTriggerName();
		setRecorderStatus();
	}

	/**
	 * Register recorder triggers with all recorders. If the triggers were created and
	 * registered before any recorders were created, then they will get added to each recorder
	 * as it is constructed. If recorders are created before the triggers, then the new triggers are 
	 * added to the controller immediately on registration. 
	 * @param recorderTrigger
	 */
	public static void registerRecorderTrigger(RecorderTrigger recorderTrigger) {
		recorderTriggers.add(recorderTrigger);
		for (int i = 0; i < recorderControllers.size(); i++) {
			recorderControllers.get(i).addRecorderTrigger(recorderTrigger);
		}
	}

	/**
	 * Keep a static list of all recorders (primarily so that trigger information
	 * can be added to them later).
	 * @param recorderControl
	 */
	private static void registerRecorders(RecorderControl recorderControl) {
		recorderControllers.add(recorderControl);
	}

	/**
	 * Find details of a recorder trigger by name. 
	 * @param triggerName
	 * @return RecorderTrigger object or null if none found
	 */
	public static RecorderTrigger findRecorderTrigger(String triggerName) {
		for (RecorderTrigger rt:recorderTriggers) {
			if (rt.getName().equals(triggerName)) {
				return rt;
			}
		}
		return null;
	}

	public RecorderSettings getRecorderSettings() {
		return recorderSettings;
	}

	public RecorderProcess getRecorderProcess() {
		return recorderProcess;
	}

	public static ArrayList<RecorderTrigger> getRecorderTriggers() {
		return recorderTriggers;
	}

	@Override
	public BackupInformation getBackupInformation() {
		return backupInformation;
	}

	@Override
	public String tellModule(String command) {
		String[] parts = command.split(" ");
		if (parts.length < 1) {
			return "Unknown command to sound recorder: " + command;
		}
		switch(parts[0]) {
		case "start":
			buttonCommand(RecorderView.BUTTON_START);
			break;
		case "startbuffered":
			buttonCommand(RecorderView.BUTTON_START_BUFFERED);
			break;
		case "stop":
			buttonCommand(RecorderView.BUTTON_OFF);
			break;
		case "cycle":
			buttonCommand(RecorderView.BUTTON_AUTO);
			break;
		case "outputfolder":
			return setOutputFolder(command);
		default:
			return "Unknown command to sound recorder: " + command;
		}
		return getUnitName() + " executed " + command;
	}

	/**
	 * set output folder. Trim off the command first. 
	 * @param command
	 * @return 
	 */
	private String setOutputFolder(String command) {
		String[] parts = CommandManager.splitCommandLine(command);
		if (parts.length < 2) {
			return "Unspecified output folder for sound recorder";
		}
		File path = new File(parts[1].trim());
		if (path.exists() == false) {
			path.mkdirs();
		}
		if (path.isDirectory()) {
			recorderSettings.outputFolder = path.getAbsolutePath();
			return getUnitName() + "storing recordings in " + path.getAbsolutePath();
		}
		else {
			return getUnitName() + "unable to switch to storage folder " + path.getAbsolutePath();
		}
	}

	@Override
	public String getModuleSummary(boolean clear) {
		File path = new File(recorderSettings.outputFolder);
		long space = -1;
		double freeSpace = -1;
		try {
			space = path.getFreeSpace();
			freeSpace = (double) space / 1048576.;
		}
		catch (SecurityException e) {
			freeSpace = -9999;
		}
		int currButton = pressedButton;
		int currState = recorderStatus;
		return String.format("%d,%d,%3.1f", currButton, currState, freeSpace);
	}

}
