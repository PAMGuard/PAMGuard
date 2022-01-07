package qa.monitor;

import PamguardMVC.PamDataUnit;
import qa.QASoundDataUnit;

public class TimeFreqDetectionMatcher extends TimeDetectionMatcher {

	/* (non-Javadoc)
	 * @see qa.monitor.TimeDetectionMatcher#getOverlap(PamguardMVC.PamDataUnit, qa.QASoundDataUnit)
	 */
	@Override
	public double getOverlap(PamDataUnit detection, QASoundDataUnit qaSound) {
		double timeOverlap = super.getOverlap(detection, qaSound);
		if (timeOverlap < 0) {
			return timeOverlap;
		}
		double[] qaFreq = qaSound.getFrequency();
		double[] detFreq = detection.getFrequency();
		if (detFreq == null) { // aFreq should never be null
			return timeOverlap;
		}
		// otherwise work out the frequency overlap. 
//		double tOverlap = Math.min(soundEnd, detEnd) - Math.max(soundStart, detStart) + 1;
		double fOverlap = Math.min(qaFreq[1], detFreq[1]) - Math.max(qaFreq[0], detFreq[0]);
		if (qaFreq[1]-qaFreq[0] == 0) {
			return timeOverlap; // infinite 
		}
		fOverlap /= (qaFreq[1]-qaFreq[0]);
		return Math.min(timeOverlap*fOverlap, 1.);
	}

	@Override
	public String getName() {
		return "Time-Frequency Match";
	}

	@Override
	public String getDescription() {
		return "Overlap in time and frequency";
	}


}
