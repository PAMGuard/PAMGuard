package qa.analyser;

import Stats.LogRegWeka;
import pamMaths.PamHistogram;
import pamMaths.PamLogHistogram;

/**
 * Class to conduct and hold results of an analysis of test data. Will probably 
 * be created both for single sounds and for cluster detection, unless
 * of course cluster size is 1, in which case there is no point. 
 * @author Doug Gillespie
 *
 */
public class QATestAnalysis {
	
	private LogRegWeka logRegWeka;
	private PamHistogram hitHistogram, missHistogram;
	private double hitMean;

	public QATestAnalysis(PamLogHistogram hitHist, PamLogHistogram missHist, double hitMean, LogRegWeka lrw) {
		this.hitHistogram = hitHist;
		this.missHistogram = missHist;
		this.hitMean = hitMean;
		this.logRegWeka = lrw;
	}

	/**
	 * @return the logRegWeka
	 */
	public LogRegWeka getLogRegWeka() {
		return logRegWeka;
	}

	/**
	 * @return the hitHistogram
	 */
	public PamHistogram getHitHistogram() {
		return hitHistogram;
	}

	/**
	 * @return the missHistogram
	 */
	public PamHistogram getMissHistogram() {
		return missHistogram;
	}

	/**
	 * @return the hitMean
	 */
	public double getHitMean() {
		return hitMean;
	}

	
}
