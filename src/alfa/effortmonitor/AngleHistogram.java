package alfa.effortmonitor;

import pamMaths.PamHistogram;

public class AngleHistogram extends PamHistogram implements Cloneable {
	
	private long startTime;

	public AngleHistogram(long startTime, double minVal, double maxVal, int nBins) {
		super(minVal, maxVal, nBins);
		this.startTime = startTime;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	protected AngleHistogram clone() {
		AngleHistogram angleHist = (AngleHistogram) super.clone();
		return angleHist;
	}

}
