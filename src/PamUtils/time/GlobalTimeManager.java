package PamUtils.time;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.time.nmea.PamNMEATime;
import PamUtils.time.ntp.PamNTPTime;
import PamguardMVC.debug.Debug;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;

/**
 * Class owned by PamController which manages time corrections from NMEA sources and / or NTP servers, etc. 
 * @author Doug Gillespie
 *
 */
public class GlobalTimeManager implements PamSettings {

	private PamController pamController;

	private ArrayList<PCTimeCorrector> pcTimeCorrectors;

	private int runMode;

	private PCTimeCorrector currentSystem;

	private GlobalTimeParameters globalTimeParameters = new GlobalTimeParameters();

	private GlobalTimeLogging globalTimeLogging;

	private List<TimeCorrection> offsetList = Collections.synchronizedList(new LinkedList<TimeCorrection>());

	private volatile long lastOuputTime;

	public boolean checkPCClock;

	private Thread clockCheckThread;

	public GlobalTimeManager(PamController pamController) {
		super();

		this.pamController = pamController;

		pcTimeCorrectors = new ArrayList<>();
		pcTimeCorrectors.add(new NullTimeCorrector(this));
		pcTimeCorrectors.add(new PamNMEATime(this));
		pcTimeCorrectors.add(new PamNTPTime(this));
		runMode = pamController.getRunMode();
		PamSettingManager.getInstance().registerSettings(this);
	}

	public JMenuItem getSwingMenuItem(Window frame) {
		JMenuItem menuItem = new JMenuItem("Global time settings ...");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(frame);
			}
		});
		menuItem.setToolTipText("Manage how the PC clock is updates from GPS or NTP servers");
		return menuItem;
	}

	public boolean showSettingsDialog(Window frame) {
		GlobalTimeParameters newSettings = GlobalTimeDialog.showDialog(frame, this);
		if (newSettings != null) {
			globalTimeParameters = newSettings;
			startSystem();
			return true;
		}
		else {
			return false;
		}
	}

	private class PCClockCheckThread implements Runnable {
		public void run() {
			long lastPCTime = System.currentTimeMillis();
			long checkTime = 1000;
			while (checkPCClock) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				long now = System.currentTimeMillis();
				long diff = Math.abs(now-lastPCTime-1000);
				lastPCTime = now;
				if (diff > 500) {
					System.out.printf("PC clock Jumped %d millis at %s\n", diff, PamCalendar.formatDateTime(now));
					handleClockJump();
					// the handler restarts the monitor system for some reason which relaunches this 
					// thread, so end up with ever increasing numbers of this thread running which is really stupid.
					// so kill this one by breaking now. 
					break;
				}
			}
		}
	}
	/*
	 * Call from PamController when everything is initialised. 
	 */
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			startSystem();
		}
		//		if (changeType == PamController.) {
		//			stopSystem();
		//		}
	}
	
	/**
	 * Work out if we're running a real time DAQ system. If we're not then there is 
	 * no need to do time checks. 
	 * @return true if it's a realtime system
	 */
	public boolean isRealTime() {
		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
			return false;
		}
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daqControl == null) {
			return true;
		}
		DaqSystem daqSystem = daqControl.findDaqSystem(null);
		if (daqSystem == null) {
			return false;
		}
		return daqSystem.isRealTime();
	}

	/**
	 * Called when the PC clock jumps suddenly - for instance when Windows updates from a time server. 
	 */
	private void handleClockJump() {
		startSystem();
	}

	private void startSystem() {
		if (runMode == PamController.RUN_MIXEDMODE || runMode == PamController.RUN_PAMVIEW) {
			return;
		}
		stopSystem();
		
		if (isRealTime() == false) {
			return;
		}

		lastOuputTime = 0;

		currentSystem = findSystem(globalTimeParameters.getSelectedTimeSource());
		if (currentSystem == null) {
			return;
		}
		currentSystem.start();

		clockCheckThread = new Thread(new PCClockCheckThread());
		checkPCClock = true;
		clockCheckThread.start();
	}

	private void stopSystem() {
		if (clockCheckThread != null) {
			clockCheckThread.interrupt();
			checkPCClock = false;
			try {
				clockCheckThread.join(1000);
			} catch (InterruptedException e) {
			}
		}

		if (currentSystem == null) {
			return;
		}
		currentSystem.stop();
		synchronized (offsetList) {
			offsetList.clear();
		}
	}

	private PCTimeCorrector findSystem(String selectedTimeSource) {
		for (PCTimeCorrector tc:pcTimeCorrectors) {
			if (tc.getClass().getName().equals(selectedTimeSource)) {
				return tc;
			}
		}
		return null;
	}

	public void updateUTCOffset(TimeCorrection timeCorrection) {
		//		if (timeCorrection != null) {
		//			System.out.printf("Current time offset from %s-%s = %d millis\n", currentSystem.getName(), currentSystem.getSource(), timeCorrection.getCorrection());
		//		}
		smoothTimeOffset(timeCorrection);
	}

	/**
	 * Smooth the data over a period of time. Also only log at those time
	 * intervals. More important with NMEA data than with NTP data since NMEA data
	 * are less accurate and also arrive very frequency. 
	 * @param timeCorrection latest time correction. 
	 */
	private void smoothTimeOffset(TimeCorrection timeCorrection) {
		long sumOffset = 0;
		long nOffset = 0;
		synchronized (offsetList) {
			offsetList.add(timeCorrection);
			if (timeCorrection.getSystemTime() - lastOuputTime < globalTimeParameters.getSmoothingTimeSeconds()*1000) {
				return; // wait a bit longer !
			}
			Iterator<TimeCorrection> it = offsetList.iterator();
			long minTime = timeCorrection.getSystemTime() - globalTimeParameters.getSmoothingTimeSeconds()*1000;
			int nStored = offsetList.size();
			while (it.hasNext()) {
				TimeCorrection tc = it.next();
				if (tc.getSystemTime() < minTime && nStored > 5) {
					it.remove();
					nStored--;
				}
				else {
					sumOffset += tc.getCorrection();
					nOffset++;
				}
			}
		}
		if (nOffset == 0) {
			return;
		}
		TimeCorrection meanCorrection = new TimeCorrection(timeCorrection.getSystemTime(), timeCorrection.getSystemTime()+sumOffset/nOffset, timeCorrection.getSource());
		setPCTimeCorrection(meanCorrection);
	}

	private void setPCTimeCorrection(TimeCorrection timeCorrection) {		
		if (globalTimeParameters.isUpdateUTC()) {
			PamCalendar.setTimeCorrection(timeCorrection.getCorrection());
			pamController.notifyModelChanged(PamController.GLOBAL_TIME_UPDATE);
		}
		logData(timeCorrection);
		lastOuputTime = timeCorrection.getSystemTime();
	}

	private void logData(TimeCorrection timeCorrection) {
		GlobalTimeLogging timeLogging = checkTimeLogging();
		if (timeLogging == null) return;
		GlobalTimeDataUnit gtdu = new GlobalTimeDataUnit(PamCalendar.getTimeInMillis(), currentSystem, timeCorrection);
		timeLogging.logData(gtdu);
	}

	private GlobalTimeLogging checkTimeLogging() {
		DBControl dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			globalTimeLogging = null;
			return null;
		}
		if (globalTimeLogging == null || globalTimeLogging.getDbControl().getConnection() != dbControl.getConnection()) {
			globalTimeLogging = new GlobalTimeLogging(dbControl, this);
			dbControl.getDbProcess().checkTable(globalTimeLogging.getTableDefinition());
		}
		return globalTimeLogging;
	}

	/**
	 * @return the globalTimeParameters
	 */
	public GlobalTimeParameters getGlobalTimeParameters() {
		return globalTimeParameters;
	}

	/**
	 * @return the pcTimeCorrectors
	 */
	public ArrayList<PCTimeCorrector> getPcTimeCorrectors() {
		return pcTimeCorrectors;
	}

	@Override
	public String getUnitName() {
		return "Global Time Settings";
	}

	@Override
	public String getUnitType() {
		return "Global Time Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return globalTimeParameters;
	}

	@Override
	public long getSettingsVersion() {
		return GlobalTimeParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		globalTimeParameters = ((GlobalTimeParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * Called by Controller at startup to wait for a global time measurement. Will wait for up to a certain
	 * amount of time before giving up. 
	 * @param mainFrame main frame for dialog display
	 * @param waitTimeMillis maximum wait time. 
	 * @return true if global time acquired, false otherwise. 
	 */
	public boolean waitForGlobalTime(Frame mainFrame, int waitTimeMillis) {
		long tStart = System.currentTimeMillis();
		long now ;
		if (currentSystem == null) {
			return false;
		}
		if (currentSystem.getClass() == NullTimeCorrector.class) {
			return true;
		}
		while ((now=System.currentTimeMillis()) < tStart+waitTimeMillis) {
			if (lastOuputTime > 0) {
				Debug.out.printf("Global time correction set after %d milliseconds\n", now-tStart);
				return true;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.printf("Global time correction unavailable after %d milliseconds\n", now-tStart);
		return true;
	}


}
