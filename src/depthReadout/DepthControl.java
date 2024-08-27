package depthReadout;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.Timer;

import Array.ArrayManager;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSidePanel;
import PamView.dialog.warn.WarnOnce;

public class DepthControl extends PamControlledUnit implements PamSettings {

	private DepthSystem depthSystem;

	private Timer timer;
	
	private DepthProcess depthProcess;
	
	protected DepthParameters depthParameters = new DepthParameters();
	
	private DepthSidePanel depthSidePanel;
	
	int[] sensorMap;
	
	public DepthControl(String unitName) {
		
		super("Hydrophone Depth Readout", unitName);
		
		PamSettingManager.getInstance().registerSettings(this);
		
		warnObsolete();

		createDepthSystem(depthParameters.systemNumber);
		
		addPamProcess(depthProcess = new DepthProcess(this));
		
//		ArrayManager.getArrayManager().setDepthControl(this);

		timer = new Timer((int) (depthParameters.pollTime * 1000), new TimerListener());
		
		newSettings();
		
		depthSidePanel = new DepthSidePanel(this);
		
		startTimer();
		
	}
	
	private void warnObsolete() {
		String msg = "The depth measurement module has changed and you should now use the analog array sensors module." + 
	"<p>If you continue to use this module, you should change the sensor readout settings for each array streamer to use data from this module";
		WarnOnce.showWarning(getUnitName(), msg, WarnOnce.WARNING_MESSAGE);		
	}

	@Override
	public boolean removeUnit() {
		super.removeUnit();
//		ArrayManager.getArrayManager().setDepthControl(null);
		return true;
	}

	class TimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			depthProcess.readDepthData();
			
		}
		
	}
	
	private void newSettings() {
		makeSensorMap();
		startTimer();
	}

	private void startTimer() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			timer.setDelay((int)(depthParameters.pollTime * 1000));
			timer.stop();
			timer.setInitialDelay(0);	
			timer.start();
		}
	}
	
	private boolean makeSensorMap() {
		/*
		 * Make a map of shich sensor is connected with which hydrophone.
		 * basically reversing the hydrophone lookup tables in DepthParameters.
		 * 
		 */
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		int nPhones = arrayManager.getCurrentArray().getHydrophoneCount();
		sensorMap = new int[nPhones];
		for (int iSensor = 0; iSensor < depthParameters.nSensors; iSensor++) {
			sensorMap[iSensor] = -1;
		}
		
		int hMap;
		if (depthParameters.hydrophoneMaps == null) {
			return false;
		}
		for (int iSensor = 0; iSensor < depthParameters.nSensors; iSensor++) {
			if (iSensor >= depthParameters.hydrophoneMaps.length ) {
				return false;
			}
			hMap = depthParameters.hydrophoneMaps[iSensor];
			for (int iP = 0; iP < nPhones; iP++) {
				if ((hMap & 1<< iP) != 0) {
					sensorMap[iP] = iSensor;
				}
			}
		}
		return true;
	}
	/**
	 * Return the sensor number for a particular hydrophone or -1 if this
	 * hydrophone is not associated with a sensor. 
	 * @param iPhone
	 * @return sensor number for a given hydrophone
	 */
	public int getSensorForHydrophone(int iPhone) {
		if (iPhone >= sensorMap.length) {
			return -1;
		}
		return sensorMap[iPhone];
	}
	
	public DepthDataBlock getDepthDataBlock() {
		return depthProcess.getDepthDataBlock();
	}

	public int getNumDepthSystems() {
		return 1;
	}
	public String getDepthSystemName(int iSystem) {
		return "Measurement Computing analog readout";
	}
	
	private boolean createDepthSystem(int iSystem) {
		switch(iSystem) {
		case 0:
			setDepthSystem(new MccDepthSystem(this));
			return true;
		default:
			setDepthSystem(null);
			return false;
		}
	}
	private void setDepthSystem(DepthSystem depthSystem) {
		this.depthSystem = depthSystem;
	}
	
	public DepthSystem getDepthSystem() {
		return depthSystem;
	}
	

	@Override
	public PamSidePanel getSidePanel() {
		if (depthSidePanel == null) {
			depthSidePanel = new DepthSidePanel(this);
		}
		return depthSidePanel;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
//		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new DetectionMenuAction(parentFrame));
		return menuItem;
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.NEW_SCROLL_TIME) {
			newViewTimes();
		}
	}
	
	private void newViewTimes() {
		depthSidePanel.newViewTimes();
	}

	class DetectionMenuAction implements ActionListener {
		
		private Frame parentFrame;
		
		public DetectionMenuAction(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (depthSystem != null && depthSystem.canConfigure()) {
				depthSystem.configureSensor(parentFrame);
			}
			newSettings();
			
		}
		
	}

	@Override
	public Serializable getSettingsReference() {
		return depthParameters;
	}
	@Override
	public long getSettingsVersion() {
		return DepthParameters.serialVersionUID;
	}
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		depthParameters = ((DepthParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
}
