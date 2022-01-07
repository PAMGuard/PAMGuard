package SoundRecorder.trigger;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import SoundRecorder.RecorderControl;

/**
 * Basic recorder trigger which can be re-implemented in many guises.<p> 
 * Contains basic code for<p> 
 * a) requiring a minimum number of detections in a set time
 * b) can limit total length of a recording 
 * c) can force a minimum gap between recordings. 
 * @author Doug Gillespie
 *
 */
public class TriggerDecisionMaker {
	


	private long[] trigTimeArray;
	private int trigTimeArrayPos;
	
	private String message;
					
	public TriggerDecisionMaker(RecorderTriggerData recorderTriggerData) {
//		this.rtData = recorderTriggerData;
	}
	
	/**
	 * Work out whether or not a recording can be made. If
	 * it can, the maximum length of the recording in seconds is 
	 * returned, otherwise, 0 or a negative number.
	 * @param timeNow current data time in milliseconds 
	 * @param nChan number of channels
	 * @param sampleRate sample rate for the recording
	 * @return +ve number of seconds to record, or <=0 for no recording. 
	 */
	public double canRecord(RecorderTriggerData rtData, long timeNow, int nChan, float sampleRate, int nBytes) {
		/**
		 * Decision is based on a number of options in the rtData class
		 */
		if (rtData.enabled == false) {
			setMessage("disabled");
			return 0;
		}
		if (checkCount(rtData, timeNow) == false) {
			clearMessage();
			return 0;
		}
		double maxLen1 = checkRecordingTimes(rtData, timeNow);
		double maxLen2 = checkDataBudget(rtData, timeNow, nChan, sampleRate, nBytes);
		double recordLength = Math.min(maxLen1, maxLen2);
		/*
		 * Now take off from the budget the appropriate amount of data. 
		 * This will depend on whether or not we're already running. 
		 * Also set the appropriate start and end times for total length book keeping. 
		 */
		if (recordLength > 0) {
			long newEndTime = timeNow + (long) (recordLength * 1000.); 
			long recordStartTime = timeNow - (long)(rtData.getSecondsBeforeTrigger() * 1000.);
			recordStartTime = Math.max(recordStartTime, rtData.lastTriggerEnd);
			long bytesPerSec = (long) (nChan * sampleRate * nBytes);
			long usedBudget = (newEndTime - recordStartTime) * bytesPerSec / 1000;
			rtData.usedDayBudget += usedBudget;
			if (!probRunning(rtData, timeNow)) {
				rtData.lastTriggerStart = recordStartTime;
			}
			rtData.lastTriggerEnd = newEndTime;
			setMessage("Triggered");
		}
			
		return recordLength;
	}

	private double checkDataBudget(RecorderTriggerData rtData, long timeNow, int nChan, float sampleRate, int nBytes) {
		double recordTime = rtData.getSecondsAfterTrigger();
		if (rtData.dayBudgetMB == 0) {
			return recordTime;
		}
		if (!PamCalendar.isSameDay(rtData.lastTriggerStart, timeNow)) {
			rtData.usedDayBudget = 0;
		}
		double bytesPerSec = nChan * sampleRate * nBytes;
		long availBytes = rtData.dayBudgetMB*1024*1024 - rtData.usedDayBudget;
		double availSecs = availBytes / bytesPerSec;
		availSecs = Math.min(recordTime, availSecs);
		if (availSecs < 0) {
			setMessage("budget exceeded");
		}
		return availSecs;
	}

	private double checkRecordingTimes(RecorderTriggerData rtData, long timeNow) {
		double recordTime = rtData.getSecondsAfterTrigger();
		// check we're not going to overrun the maximum recording length. 
		if (probRunning(rtData, timeNow) && rtData.maxTotalTriggerLength > 0) {
			double availableSecs = rtData.maxTotalTriggerLength - (timeNow - rtData.lastTriggerStart)/1000.;
			recordTime = Math.min(recordTime, availableSecs);
			if (recordTime <= 0) {
				setMessage("recording too long");
			}
		}
		// if it's not running, check there has been a reasonable gap since the last trigger
		if (!probRunning(rtData, timeNow) && rtData.minGapBetweenTriggers > 0) {
			if ((timeNow - rtData.lastTriggerEnd) / 1000. < rtData.minGapBetweenTriggers) {
				setMessage("recording too close");
				return 0;
			}
		}
		return recordTime;
	}
	
	private boolean probRunning(RecorderTriggerData rtData, long timeNow) {
		return timeNow < rtData.lastTriggerEnd;
	}

	private boolean checkCount(RecorderTriggerData rtData, long timeNow) {
		if (rtData.minDetectionCount <= 1) {
			return true;
		}
		checkTrigArray(rtData);
		/*
		 * nextPos is the next position in the array, which is therefore
		 * the OLDEST entry in the array, so compare that value with the
		 * current one. 
		 */
		int nextPos = trigTimeArrayPos + 1;
		if (nextPos >= trigTimeArray.length) {
			nextPos = 0;
		}
		trigTimeArray[nextPos] = timeNow;
		boolean fired = false;
		if (trigTimeArray[nextPos] > 0 &&
				(timeNow - trigTimeArray[nextPos]) <= rtData.countSeconds * 1000) {
			fired = true;
		}
		trigTimeArrayPos = nextPos;
		return fired;
	}

	private final void checkTrigArray(RecorderTriggerData rtData) {
		if (trigTimeArray == null || trigTimeArray.length != rtData.minDetectionCount) {
			trigTimeArray = new long[rtData.minDetectionCount];
		}
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	private void clearMessage() {
		setMessage(null);
	}
	

}
