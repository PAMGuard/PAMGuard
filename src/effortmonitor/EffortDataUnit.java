package effortmonitor;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import pamScrollSystem.AbstractPamScroller;

public class EffortDataUnit extends PamDataUnit {

	private AbstractPamScroller scroller;
	
	private String observer;
	
	private String objective;
	
	private boolean isActive;
	
	private String displayName;
	
	private String runMode;
	
	/*
	 * Actual times that the scrolling too place. 
	 */
	private long sessionStartTime, sessionEndTime; 

	/**
	 * Constructor for new scroll effort data
	 * @param scroller
	 * @param observer
	 * @param objective
	 */
	public EffortDataUnit(AbstractPamScroller scroller, String observer, String objective) {
		super(scroller.getValueMillis());
		this.scroller = scroller;
		displayName = scroller.getScrollerData().getName();
		this.observer = observer;
		this.objective = objective;
		runMode = PamController.getInstance().getRunModeName();
		this.setActive(true);
		sessionStartTime = sessionEndTime = System.currentTimeMillis();
		getBasicData().setEndTime(scroller.getValueMillis()+scroller.getVisibleAmount());
	}
	
	
	/**
	 * Constructor to use when reloading from database. 
	 * @param databaseIndex
	 * @param scroller
	 * @param displayName
	 * @param timeMilliseconds
	 * @param endTime
	 * @param observer
	 * @param objective
	 * @param sessStart
	 * @param sessEnd
	 */
	public EffortDataUnit(int databaseIndex, AbstractPamScroller scroller, String displayName, long timeMilliseconds,
			long endTime, String observer, String objective, long sessStart, long sessEnd, String runMode) {
		super(timeMilliseconds);
		setDatabaseIndex(databaseIndex);
		this.scroller = scroller;
		this.displayName = displayName;
		this.observer = observer;
		this.objective = objective;
		this.setActive(false);
		getBasicData().setEndTime(endTime);
		this.sessionStartTime = sessStart;
		this.sessionEndTime = sessEnd;
		this.runMode = runMode;
	}

	/**
	 * Say if the current scroll position is continuous with the existing recorded time or 
	 * if there is a gap of any sort. 
	 * @return true if no gap or an overlap, false if a gap
	 */
	public boolean isContinuous() {
		if (scroller.getValueMillis() > getBasicData().getEndTime()) {
			long scrV = scroller.getValueMillis();
			long dE = getBasicData().getEndTime();
			long diff = scrV-dE;
			return false;
		}
		if (scroller.getValueMillis()+scroller.getVisibleAmount() < getTimeMilliseconds()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Extend the recorded range of this scroller. 
	 * @return true if the range has changed
	 */
	public boolean extendRecordedRange() {
		boolean changed = false;
		if (scroller.getValueMillis() < getTimeMilliseconds()) {
			/*
			 * Careful. Data unit actually stored duratin not end time, do 
			 * if we just shift the start, it will mess up the end!!!
			 */
			long currEnd = getBasicData().getEndTime();
			this.setTimeMilliseconds(scroller.getValueMillis());
			getBasicData().setEndTime(currEnd);
			
			changed = true;
		}
		if (scroller.getValueMillis()+scroller.getVisibleAmount() > getBasicData().getEndTime()) {
			getBasicData().setEndTime(scroller.getValueMillis()+scroller.getVisibleAmount());
			changed = true;
		}
		if (changed) {
			sessionEndTime = System.currentTimeMillis();
		}
		return changed;
	}

	/**
	 * @return the scroller
	 */
	public AbstractPamScroller getScroller() {
		return scroller;
	}

	/**
	 * @return the observer
	 */
	public String getObserver() {
		return observer;
	}

	/**
	 * Is the data active, i.e. is being scrolled and no gaps have been made in the data
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Set to false once there has been a gap in scrolling and this data unit 
	 * is closed but held in memory. There should only be one active unit for each scroller. 
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * The time the observer did the actual viewing, i.e. the 
	 * System.currenttime()
	 * @return the startTime
	 */
	public long getSessionStartTime() {
		return sessionStartTime;
	}

	/**
	 * The time the observer did the actual viewing, i.e. the 
	 * System.currenttime()
	 * @return the endTime
	 */
	public long getSessionEndTime() {
		return sessionEndTime;
	}
	
	/**
	 * Get the scroller (display) name
	 * @return display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return the objective
	 */
	public String getObjective() {
		return objective;
	}

	/**
	 * @param objective the objective to set
	 */
	public void setObjective(String objective) {
		this.objective = objective;
	}

	/**
	 * @param observer the observer to set
	 */
	public void setObserver(String observer) {
		this.observer = observer;
	}

	@Override
	public String getSummaryString() {
		String str = String.format("<html>Id %d, UID %d<br>%s to %s<p>Observer: %s<p>Objective: %s<p>"+
				"Display: %s<p>Analysed: %s - %s<br>Running PAMGuard %s</html>", 
				getDatabaseIndex(), getUID(),
				PamCalendar.formatDBDateTime(getTimeMilliseconds()), PamCalendar.formatDBDateTime(getEndTimeInMilliseconds()), 
				getObserver(), getObjective(), getDisplayName(),
				PamCalendar.formatDBDateTime(getSessionStartTime()), PamCalendar.formatDBDateTime(getSessionEndTime()), getRunMode());
		return str;
	}

	/**
	 * @return the runMode
	 */
	public String getRunMode() {
		return runMode;
	}


	/**
	 * Set the limits as the outer limits of the scroller. 
	 */
	public void setOuterLimits() {
		setTimeMilliseconds(scroller.getMinimumMillis());
		getBasicData().setEndTime(scroller.getMaximumMillis());
	}

}
