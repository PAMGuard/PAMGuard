package qa.monitor;

import PamguardMVC.PamDataUnit;
import qa.QASoundDataUnit;

public class TimeDetectionMatcher implements DetectionMatcher<PamDataUnit> {

	private long preWindowMillis = 0;
	
	private long postWindowMillis = 0;
	
	@Override
	public double getOverlap(PamDataUnit detection, QASoundDataUnit qaSound) {
		long soundStart = qaSound.getArrivalStartMillis();
		long soundEnd = qaSound.getArrivalEndMillis();
		long detStart = detection.getTimeMilliseconds() - preWindowMillis;
		long detEnd = detection.getEndTimeInMilliseconds() + postWindowMillis;
		double tOverlap = Math.min(soundEnd, detEnd) - Math.max(soundStart, detStart) + 1;
		double soundLen = soundEnd-soundStart+1; 
		return Math.min(tOverlap/soundLen, 1.);
	}

	@Override
	public boolean hasSettings() {
		return false;
	}

	@Override
	public boolean showSettings(Object parentWindow) {
		return false;
	}

	@Override
	public String getName() {
		return "Time Match";
	}

	@Override
	public String getDescription() {
		return "Overlap in time only";
	}

	/**
	 * @return the preWindowMillis
	 */
	public long getPreWindowMillis() {
		return preWindowMillis;
	}

	/**
	 * @param preWindowMillis the preWindowMillis to set
	 */
	public void setPreWindowMillis(long preWindowMillis) {
		this.preWindowMillis = preWindowMillis;
	}

	/**
	 * @return the postWindowMillis
	 */
	public long getPostWindowMillis() {
		return postWindowMillis;
	}

	/**
	 * @param postWindowMillis the postWindowMillis to set
	 */
	public void setPostWindowMillis(long postWindowMillis) {
		this.postWindowMillis = postWindowMillis;
	}

}
