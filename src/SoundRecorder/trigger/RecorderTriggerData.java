package SoundRecorder.trigger;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Information for triggered recordings to tell each recorder how long
 * a recording it should make. 
 * <p><p> 
 * If a recording is already running when a RecorderTrigger is sent, then 
 * the recording will continue, ending at the later of the existing stop time 
 * (if recoring on a timer or on a different trigger) or the time indicated in 
 * RecorderTriggerData. If a continuous recording is being made, then that 
 * recording will simply continue.
 * <p><p> 
 * If no recording is being made, then the recorder will take data from the
 * buffer for secondsBeforeTrigger seconds to add to the start of the recording. Note that 
 * recordings NEVER overlap, so if secondsBeforeTrigger were set to say, 30s, and the previous 
 * recoridng had only ended 10 seconds earlier, then only 10s of data will be taken from
 * the buffer. 
 * <p><p> 
 * Each recorder will ensure that adequate raw audio data is stored in the 
 * source data block to satisfy secondsBeforeTrigger in every trigger. Therefore, if
 * secondsBeforeTrigger is set to a large value, excessive amounts of memory may
 * be required to store the data, particularly at high frequencies. 
 * <p><p>
 * Information from RecorderTriggerData is read when the recording is made, so it is
 * possible to update the fields secondsBeforeTrigger and secondsAfterTrigger after
 * the RecorderTrigger has been registered with the recorders, although time may be required 
 * for the buffer to fill if secondsBeforeTrigger is increased.
 * @author Doug Gillespie
 * @see SoundRecorder.trigger.RecorderTrigger
 * @see SoundRecorder.RecorderControl
 */
public class RecorderTriggerData implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * Name used to identify the trigger
	 */
	protected String triggerName;
	
	/**
	 * Trigger state.
	 */
	protected boolean enabled;
	/**
	 * Number of seconds of data to add to the start of the recording prior
	 * to the trigger event.
	 */
	protected double secondsBeforeTrigger = 0;
	
	/**
	 * Number of seconds of data to record after the trigger event.
	 */
	protected double secondsAfterTrigger = 10;
	
	/**
	 * min number of detections in time period countSeconds
	 */
	protected int minDetectionCount = 1;
	
	/**
	 * time period for minDetectionCount
	 */
	protected int countSeconds = 0;      
	
	/**
	 * minimum gap between triggers
	 */
	protected int minGapBetweenTriggers = 0; 
	
	/**
	 * max total length in seconds of a recording when multiple trigs run together.
	 */
	protected int maxTotalTriggerLength = 0; 
	
	/**
	 * Daily budget in megabytes. 
	 */
	protected int dayBudgetMB = 0;
	
	/*
	 * Also put some bookkeeping stuff into this class since it gets stored and 
	 * the budget should be persistent even if PAMGuard exits and restarts. 
	 */
	protected long lastTriggerStart; // start of last fired trigger
	protected long lastTriggerEnd;   // end of last fired trigger
	/**
	 * Used day budget is recorded in bytes to deal with low frequency data. 
	 */
	protected long usedDayBudget;
	
	private transient TriggerDecisionMaker decisionMaker;

	public RecorderTriggerData(String triggerName, double secondsBeforeTrigger, double secondsAfterTrigger) {
		
		this.triggerName = triggerName;
		this.secondsBeforeTrigger = secondsBeforeTrigger;
		this.secondsAfterTrigger = secondsAfterTrigger;
	}

	public TriggerDecisionMaker getDecisionMaker() {
		if (decisionMaker == null) {
			decisionMaker = new TriggerDecisionMaker(this);
		}
		return decisionMaker;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public RecorderTriggerData clone() {
		try {
			return (RecorderTriggerData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the secondsBeforeTrigger
	 */
	public double getSecondsBeforeTrigger() {
		return secondsBeforeTrigger;
	}

	/**
	 * @param secondsBeforeTrigger the secondsBeforeTrigger to set
	 */
	public void setSecondsBeforeTrigger(double secondsBeforeTrigger) {
		this.secondsBeforeTrigger = secondsBeforeTrigger;
	}

	/**
	 * @return the secondsAfterTrigger
	 */
	public double getSecondsAfterTrigger() {
		return secondsAfterTrigger;
	}

	/**
	 * @param secondsAfterTrigger the secondsAfterTrigger to set
	 */
	public void setSecondsAfterTrigger(double secondsAfterTrigger) {
		this.secondsAfterTrigger = secondsAfterTrigger;
	}

	/**
	 * @return the minDetectionCount
	 */
	public int getMinDetectionCount() {
		return minDetectionCount;
	}

	/**
	 * @param minDetectionCount the minDetectionCount to set
	 */
	public void setMinDetectionCount(int minDetectionCount) {
		this.minDetectionCount = minDetectionCount;
	}

	/**
	 * @return the countSeconds
	 */
	public int getCountSeconds() {
		return countSeconds;
	}

	/**
	 * @param countSeconds the countSeconds to set
	 */
	public void setCountSeconds(int countSeconds) {
		this.countSeconds = countSeconds;
	}

	/**
	 * @return the minGapBetweenTriggers
	 */
	public int getMinGapBetweenTriggers() {
		return minGapBetweenTriggers;
	}

	/**
	 * @param minGapBetweenTriggers the minGapBetweenTriggers to set
	 */
	public void setMinGapBetweenTriggers(int minGapBetweenTriggers) {
		this.minGapBetweenTriggers = minGapBetweenTriggers;
	}

	/**
	 * @return the maxTotalTriggerLength
	 */
	public int getMaxTotalTriggerLength() {
		return maxTotalTriggerLength;
	}

	/**
	 * @param maxTotalTriggerLength the maxTotalTriggerLength to set
	 */
	public void setMaxTotalTriggerLength(int maxTotalTriggerLength) {
		this.maxTotalTriggerLength = maxTotalTriggerLength;
	}

	/**
	 * @return the triggerName
	 */
	public String getTriggerName() {
		return triggerName;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param dayBudgetMB the dayBudgetMB to set
	 */
	public void setDayBudgetMB(int dayBudgetMB) {
		this.dayBudgetMB = dayBudgetMB;
	}

	/**
	 * @return the dayBudgetMB
	 */
	public int getDayBudgetMB() {
		return dayBudgetMB;
	}

	public String getSummaryString() {
		String txt = String.format("<html>Minimum %d detections in %d seconds", minDetectionCount, countSeconds);
		txt += String.format("<p>Presample: %3.1fs; Post Sample: %3.1fs", 
				getSecondsBeforeTrigger(), getSecondsAfterTrigger());
		if (getDayBudgetMB() > 0) {
			txt += String.format("<p>Daily data budget %d MBytes (%d remaining)", 
					getDayBudgetMB(), getDayBudgetMB() - usedDayBudget / 1024/1024);
		}
		if (getMaxTotalTriggerLength() > 0) {
			txt += String.format("<p>Maximum single recording length %d s", getMaxTotalTriggerLength());
		}
		if (getMinGapBetweenTriggers() > 0) {
			txt += String.format("<p>Minimum gap between  recordings %d s", getMinGapBetweenTriggers());
		}
		txt += "</html>";
		return txt;
	}

	/**
	 * Get the auto-generated parameter set, and then add in the fields that are not included
	 * because they are not public and do not have getters.
	 * Note: for each field, we search the current class and (if that fails) the superclass.  It's
	 * done this way because RecorderTriggerData might be used directly (and thus the field would
	 * be found in the class) and it also might be used as a superclass to something else
	 * (e.g. WMRecorderTriggerData) in which case the field would only be found in the superclass.
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("lastTriggerStart");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lastTriggerStart;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("lastTriggerStart");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return lastTriggerStart;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
			
		try {
			Field field = this.getClass().getDeclaredField("usedDayBudget");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return usedDayBudget;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("usedDayBudget");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return usedDayBudget;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
			
		try {
			Field field = this.getClass().getDeclaredField("lastTriggerEnd");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lastTriggerEnd;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("lastTriggerEnd");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return lastTriggerEnd;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
			
		return ps;
	}

}
