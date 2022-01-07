package alarm;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class AlarmParameters implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	
	public static final int COUNT_SIMPLE = 0;
	public static final int COUNT_SCORES = 1;
	public static final int COUNT_SINGLES = 2;
	
	public static final int COUNT_LEVELS = 2;
	public static final String[] levelNames = {"Amber", "Red"};
	public static final Color alarmColours[] = {new Color(255, 128, 0), new Color(255, 0, 0)};
	
	public String dataSourceName;
	public int countType = COUNT_SIMPLE;
	public long countIntervalMillis = 10000L;
	public long minAlarmIntervalMillis = 2000L;
	private double[] triggerCounts = new double[COUNT_LEVELS];
	private boolean[] enabledActions;
	
	/**
	 * Default time to hold data for. 
	 */
	private int holdSeconds = 3600;
	private boolean hadHold = false; // flag to initialise when deserialised. 

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected AlarmParameters clone() {
		try {
			AlarmParameters np = (AlarmParameters) super.clone();
			if (np.triggerCounts == null || np.triggerCounts.length != COUNT_LEVELS) {
				np.triggerCounts = new double[COUNT_LEVELS];
			}
			if (np.hadHold == false) {
				np.setHoldSeconds(3600);
				np.hadHold = true;
			}
			return np;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public double getTriggerCount(int countLevel) {
		return triggerCounts[countLevel];
	}
	
	public void setTriggerCount(int countLevel, double value) {
		triggerCounts[countLevel] = value;
	}

	public static String sayLevel(int level) {
		switch(level) {
		case 0:
			return "Off";
		default:
			return AlarmParameters.levelNames[level-1];
		}
	}

	public boolean[] getEnabledActions() {
		if (enabledActions == null) {
			enabledActions = new boolean[0];
		}
		return enabledActions;
	}

	public void setEnabledActions(boolean[] enabledActions) {
		this.enabledActions = enabledActions;
	}

	/**
	 * Number of seconds to hold data for before deleting
	 * @return the holdSeconds
	 */
	public int getHoldSeconds() {
		return holdSeconds;
	}

	/**
	 * Number of seconds to hold data for before deleting
	 * @param holdSeconds the holdSeconds to set
	 */
	public void setHoldSeconds(int holdSeconds) {
		this.holdSeconds = holdSeconds;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("hadHold");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return hadHold;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("triggerCounts");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return triggerCounts;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
