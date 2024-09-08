package PamController;

import java.io.Serializable;

import generalDatabase.SQLTypes;
import generalDatabase.clauses.PAMSelectClause;

/**
 * A set of variables for loading a section of data from the viewer mode database.
 * 
 * @author Doug Gillespie
 *
 */
public class PamViewParameters extends PAMSelectClause implements Serializable, Cloneable {

	/*
	 * 99% sure this is never actually serialised any more. 
	 */
	static final long serialVersionUID = 1;

	/**
	 * start and end times for Pamguard viewer
	 * These are the times of the data (UTC in most
	 * database tables)
	 */
	public long viewStartTime, viewEndTime;
	
	/**
	 * Analysis offline may have gone through the data multiple times.
	 * IF this is the case, you may want to select by analysis time 
	 * as well as the data time. (LocalTime in most database tables)
	 */
	public boolean useAnalysisTime;

	/**
	 * Analysis offline may have gone through the data multiple times.
	 * IF this is the case, you may want to select by analysis time 
	 * as well as the data time. (LocalTime in most database tables)
	 */
	public long analStartTime, analEndTime;
	
	/**
	 * List of modules to use. This is sent directly into all modules
	 * from the view times dialog. 
	 */
	public boolean[] useModules;

	/**
	 * Make an empty view parameters clause. This will generate an 
	 * empty string when getSelectClause(...) is called.
	 */
	public PamViewParameters() {
		
	}
	
	/**
	 * @param viewStartTime
	 * @param viewEndTime
	 */
	public PamViewParameters(long viewStartTime, long viewEndTime) {
		super();
		this.viewStartTime = viewStartTime;
		this.viewEndTime = viewEndTime;
	}

	@Override
	public PamViewParameters clone() {
		try {
			return (PamViewParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get a clause to add to a query. The clause includes the 
	 * word WHERE so that it's possible to return an empty string
	 * and still get a valid SQL statement. <br>Care will be needed
	 * if two clauses are added (with AND) to remove one WHERE.
	 * @param sqlTypes SQL types for any bespoke formatting
	 * @return results query. 
	 */
	@Override
	public String getSelectClause(SQLTypes sqlTypes) {
		/**
		 * Moved this here so that it can be overridden. It used 
		 * to be called getBetweenString in PamTableDefinition. 
		 */
		if (analEndTime == 0 && analStartTime == 0 && 
				viewStartTime == 0 && viewEndTime == 0) {
			return "";
		}
	
		String str = null;
		String wrappedTableName = "";//getTableName();
		if (viewStartTime > 1 && viewEndTime != 0) {
			str = String.format(" WHERE UTC BETWEEN %s AND %s", 
					sqlTypes.formatDBDateTimeQueryString(getRoundedViewStartTime()),
					sqlTypes.formatDBDateTimeQueryString(getRoundedViewEndTime()));
		}
		else if (viewStartTime != 0) {
			str = String.format(" WHERE UTC > %s ", 
					sqlTypes.formatDBDateTimeQueryString(getRoundedViewStartTime()));
		}
		else if (viewEndTime != 0) {
			str = String.format(" WHERE UTC < %s ", 
					sqlTypes.formatDBDateTimeQueryString(getRoundedViewEndTime()));
		}
		if (useAnalysisTime) {
			if (str != null) {
				str += " AND ";
			}
			else {
				str = " WHERE ";
			}
	
			if (isValidTime(analStartTime) && isValidTime(analEndTime)) {
				str += String.format(" (LocalTime BETWEEN %s AND %s OR LocalTime BETWEEN %s AND %s)", 
						sqlTypes.formatDBDateTimeQueryString(getRoundedAnalStartTime()),
						sqlTypes.formatDBDateTimeQueryString(getRoundedAnalEndTime()),
						sqlTypes.formatDBDateTimeQueryString(getRoundedViewStartTime()),
						sqlTypes.formatDBDateTimeQueryString(getRoundedViewEndTime()));
			}
			else if (isValidTime(analStartTime)) {
				str += String.format(" LocalTime > %s ", 
						sqlTypes.formatDBDateTimeQueryString(getRoundedAnalStartTime()));
			}
			else if (isValidTime(analEndTime)) {
				str += String.format(" LocalTime < %s ", 
						sqlTypes.formatDBDateTimeQueryString(getRoundedAnalEndTime()));
			}
		}
	
		//Debug.out.println("PamViewParameters: " + str);
		return str;
	}
	
	/**
	 * Check a time is valid. If its <= 1 or near maxint, then it isn't. 
	 * @param millis
	 * @return
	 */
	private boolean isValidTime(long millis) {
		return millis > 1 && millis < Long.MAX_VALUE-1;
	}

	public boolean equals(PamViewParameters otherParameters) {
		if (otherParameters == null) {
			return false;
		}
		if (otherParameters.viewStartTime != viewStartTime) {
			return false;
		}
		if (otherParameters.viewEndTime != viewEndTime) {
			return false;
		}
		if (otherParameters.useAnalysisTime != useAnalysisTime) {
			return false;
		}
		if (!useAnalysisTime) {
			return true; // no need to shcek the other parameters in this instance
		}
		if (otherParameters.analStartTime != analStartTime) {
			return false;
		}
		if (otherParameters.analEndTime != analEndTime) {
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * @return the viewStartTime
	 */
	public long getRoundedViewStartTime() {
		return roundDown(viewStartTime);
	}

	/**
	 * @param viewStartTime the viewStartTime to set
	 */
	public void setViewStartTime(long viewStartTime) {
		this.viewStartTime = viewStartTime;
	}

	/**
	 * @return the viewEndTime
	 */
	public long getRoundedViewEndTime() {
		return roundUp(viewEndTime);
	}

	/**
	 * @param viewEndTime the viewEndTime to set
	 */
	public void setViewEndTime(long viewEndTime) {
		this.viewEndTime = viewEndTime;
	}

	/**
	 * @return the useAnalysisTime
	 */
	public boolean isUseAnalysisTime() {
		return useAnalysisTime;
	}

	/**
	 * @param useAnalysisTime the useAnalysisTime to set
	 */
	public void setUseAnalysisTime(boolean useAnalysisTime) {
		this.useAnalysisTime = useAnalysisTime;
	}

	/**
	 * @return the analStartTime
	 */
	public long getRoundedAnalStartTime() {
		return roundDown(analStartTime);
	}

	/**
	 * @param analStartTime the analStartTime to set
	 */
	public void setAnalStartTime(long analStartTime) {
		this.analStartTime = analStartTime;
	}

	/**
	 * @return the analEndTime
	 */
	public long getRoundedAnalEndTime() {
		return roundUp(analEndTime);
	}

	/**
	 * @param analEndTime the analEndTime to set
	 */
	public void setAnalEndTime(long analEndTime) {
		this.analEndTime = analEndTime;
	}

	/**
	 * Round a time down to nearest second
	 * @param time
	 * @return time rounded down to nearest second
	 */
	public long roundDown(long time) {
		long r = time%1000;
		return time-r;
	}
	/**
	 * Round a time down up nearest second
	 * @param time
	 * @return time rounded down to nearest second
	 */
	public long roundUp(long time) {
		long r = time%1000;
		if (r == 0) {
			return time;
		}
		return time-r+1000;
	}

	/**
	 * @return the viewStartTime
	 */
	public long getViewStartTime() {
		return viewStartTime;
	}

	/**
	 * @return the viewEndTime
	 */
	public long getViewEndTime() {
		return viewEndTime;
	}

	/**
	 * @return the analStartTime
	 */
	public long getAnalStartTime() {
		return analStartTime;
	}

	/**
	 * @return the analEndTime
	 */
	public long getAnalEndTime() {
		return analEndTime;
	}

	/**
	 * @return the useModules
	 */
	public boolean[] getUseModules() {
		return useModules;
	}

	/**
	 * @param useModules the useModules to set
	 */
	public void setUseModules(boolean[] useModules) {
		this.useModules = useModules;
	}
}
