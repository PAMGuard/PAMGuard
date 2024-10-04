package alarm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Timer;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

public class AlarmProcess extends PamProcess {

	private AlarmControl alarmControl;
	private AlarmDataBlock alarmDataBlock;
	private AlarmCounter alarmCounter;

	List<AlarmDataPoint> alarmPoints = new LinkedList<AlarmDataPoint>();
	private double alarmCount;
	private int alarmStatus;
	private PamDataBlock dataSource;
	private AlarmDataUnit currentDataUnit;
	private Timer nrTimer;
	private boolean hasMasterClockData;


	public AlarmProcess(AlarmControl alarmControl) {
		super(alarmControl, null, "Alarm Process");
		this.alarmControl = alarmControl;
		alarmDataBlock = new AlarmDataBlock(this, alarmControl.getUnitName());
		alarmDataBlock.SetLogging(new AlarmLogging(alarmDataBlock));
		addOutputDataBlock(alarmDataBlock);

		// start the timer anyway, but don't use it if it's not needed. 
		nrTimer = new Timer(1000, new NRTimerAction());
		nrTimer.start();
	}

	/**
	 *  If there is no acquisition of if we're running in network receive mode
	 *  then we need a timer to update alarms every second or so. 
	 * @return true if a time is needed to reset alarms. 
	 */
	private boolean needAlarmTimer() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			return true;
		}
//		if (PamController.getInstance().findControlledUnit(AcquisitionControl.class, null) == null) {
//			return true;
//		}
//		// still return true if we're not actually running yet. 
//		if (PamController.getInstance().)
		return !hasMasterClockData;
	}

	@Override
	public void pamStart() {
		setupAlarm();
	}

	private class NRTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (needAlarmTimer()) {
				updateAlarm(PamCalendar.getTimeInMillis());
			}
		}
	}
	@Override
	public void pamStop() {
		if (currentDataUnit != null) {
			currentDataUnit.setActive(false);
			alarmDataBlock.updatePamData(currentDataUnit, PamCalendar.getTimeInMillis());
			currentDataUnit = null;
		}
	}

	/**
	 * @return the alarmStatus
	 */
	public int getAlarmStatus() {
		return alarmStatus;
	}		

	/**
	 * Received an updated alarm score. 
	 * @param alarmCount
	 */
	public void updateAlarmScore(double alarmCount, long timeMillis) {
		int alarmState = 0;
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			if (alarmCount < alarmControl.alarmParameters.getTriggerCount(i)) {
				break;
			}
			alarmState++;
		}
		setAlarmStatus(alarmState, alarmCount, timeMillis);

		alarmControl.alarmSidePanel.updateAlarmScore(alarmCount);
	}

	/**
	 * @param alarmStatus the alarmStatus to set
	 */
	public void setAlarmStatus(int alarmStatus, double score, long timeMillis) {
		// only do anything if the state changes. 
		boolean fireEvents = false;
		if (alarmStatus == 0 && currentDataUnit != null && currentDataUnit.getCurrentStatus() > 0) {
			currentDataUnit.setAlarmStatus(0, score, timeMillis);
			alarmDataBlock.updatePamData(currentDataUnit, timeMillis);
			fireEvents = true;
		}
		if (checkCurrentEvent(timeMillis)) { // see if it's necessary to terminate it.
			fireEvents = true;
		}
		
		this.alarmStatus = alarmStatus;
		if (alarmStatus > 0) {
			if (currentDataUnit != null) {
				/*
				 * If the status has changed, always update. If the status
				 * is the same, then only send updates after a reasonable gap
				 */
				if (currentDataUnit.getCurrentStatus() == alarmStatus) {
					if (timeMillis-currentDataUnit.getLastUpdateTime() > 
						alarmControl.alarmParameters.minAlarmIntervalMillis) {
						currentDataUnit.setAlarmStatus(alarmStatus, score, timeMillis);
						alarmDataBlock.updatePamData(currentDataUnit, timeMillis);
						fireEvents = true;
					}
				}
				else {
					currentDataUnit.setAlarmStatus(alarmStatus, score, timeMillis);
					alarmDataBlock.updatePamData(currentDataUnit, timeMillis);
					fireEvents = true;
				}
			}
			else {
				currentDataUnit = new AlarmDataUnit(timeMillis);
				currentDataUnit.setAlarmStatus(alarmStatus, score, timeMillis);
				if (alarmCounter != null) {
					currentDataUnit.setExtraFieldData(alarmCounter.getExtraFieldData());
				}
				alarmDataBlock.addPamData(currentDataUnit);
				fireEvents = true;
			}
			if (fireEvents) {
				alarmControl.fireAlarmActions(currentDataUnit);
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#masterClockUpdate(long, long)
	 */
	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		hasMasterClockData = true;
		updateAlarm(timeMilliseconds);
	}
	
	private synchronized void updateAlarm(long timeMilliseconds) {
//		removeOldPoints(timeMilliseconds);
		if (alarmControl.alarmParameters.countType != AlarmParameters.COUNT_SINGLES) {
			removeOldPoints(System.currentTimeMillis()-2000);
			updateAlarmScore(alarmCount, timeMilliseconds);
		}
	}
	
	/**
	 * Consider closing the current alarm data unit. When doing this, 
	 * a closed flag is set true and the datablock is updated with this
	 * event for a final time. It will then be written to storage. 
	 * 
	 * @param timeMilliseconds
	 * @return true if the event has been terminated. 
	 */
	private synchronized boolean checkCurrentEvent(long timeMilliseconds) {
		if (currentDataUnit == null) {
			return false;
		}
		if (currentDataUnit.getCurrentStatus() == 0 && 
				timeMilliseconds-currentDataUnit.getLastUpdate() > 
				alarmControl.alarmParameters.minAlarmIntervalMillis)  {
			currentDataUnit.setActive(false);
			alarmDataBlock.updatePamData(currentDataUnit, timeMilliseconds);
			currentDataUnit = null;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public synchronized void newData(PamObservable o, PamDataUnit dataUnit) {
		if (alarmCounter == null) {
			return;
		}
		double score = alarmCounter.getValue(alarmControl.alarmParameters.countType, dataUnit);
		
		if (alarmControl.alarmParameters.countType == AlarmParameters.COUNT_SINGLES) {
			alarmCount = score;
		}
		else{
			removeOldPoints(dataUnit.getTimeMilliseconds());
			if (score > 0) {
				alarmPoints.add(new AlarmDataPoint(dataUnit.getTimeMilliseconds(), score));
				alarmCount = alarmCounter.addCount(alarmCount, score, alarmControl.alarmParameters.countType);
			}
		}

		updateAlarmScore(alarmCount, dataUnit.getTimeMilliseconds());
	}


	@Override
	public void updateData(PamObservable o, PamDataUnit arg) {
		newData(o, arg);
	}

	public boolean setupAlarm() {
		dataSource = PamController.getInstance().getDataBlock(PamDataUnit.class, alarmControl.alarmParameters.dataSourceName);
		if (dataSource == null) {
			return false;
		}
		setParentDataBlock(dataSource, true);
		alarmCounter = null;
		if (AlarmDataSource.class.isAssignableFrom(dataSource.getClass())) {
			AlarmCounterProvider alarmProvider = ((AlarmDataSource) dataSource).getAlarmCounterProvider();
			if (alarmProvider != null) {
				alarmCounter = alarmProvider.getAlarmCounter(alarmControl);
			}
		}
		if (alarmCounter == null) {
			alarmCounter = new SimpleAlarmCounter(alarmControl, dataSource);
		}
		resetCount();
		alarmDataBlock.setNaturalLifetime(alarmControl.alarmParameters.getHoldSeconds());

		return true;
	}

	private synchronized void resetCount() {
		alarmPoints.clear();
		alarmCount = 0;
		updateAlarmScore(alarmCount, 0);
	}

	private synchronized void removeOldPoints(long currentTime) {
		ListIterator<AlarmDataPoint> it = alarmPoints.listIterator();
		long minTime = currentTime - alarmControl.alarmParameters.countIntervalMillis;
		AlarmDataPoint adp;
		while (it.hasNext()) {
			adp = it.next();
			if (adp.timeMilliseconds < minTime) {
				it.remove();
				alarmCount = alarmCounter.subtractCount(alarmCount, adp.score, alarmControl.alarmParameters.countType);
			}
		}
	}

	public AlarmDataBlock getAlarmDataBlock() {
		return alarmDataBlock;
	}

	/**
	 * @return the alarmCounter
	 */
	public AlarmCounter getAlarmCounter() {
		return alarmCounter;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#destroyProcess()
	 */
	@Override
	public void destroyProcess() {
		super.destroyProcess();
		if (nrTimer != null) {
			nrTimer.stop();
		}
	}

}
