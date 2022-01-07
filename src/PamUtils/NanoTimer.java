package PamUtils;

import java.io.PrintStream;


/**
 * Utility to measure execution times of various parts of PAMGuard. 
 * All based on nanosecond times from the system clock so does not
 * really represent CPU usage of individual threads. 
 * @author dg50
 *
 */
public class NanoTimer {

	private String timerName;
	
	private int averageCount;
	
	private int nMeasures;
	
	private long totalTimeNanos;
	
	private long lastTimeNanos;
	
	private long lastStart, lastStop;
	
	private long[] runningCounts;
	
	private int iRuningCount;

	/**
	 * 
	 * @param timerName Name for the timer
	 */
	public NanoTimer(String timerName) {
		this.timerName = timerName;
	}
	

	/**
	 * Start the timer
	 */
	public void start() {
		lastStart = System.nanoTime();
	}
	
	/**
	 * Stop the timer
	 * @param os name of a print stream if you want quick terminal output (e.g. System.out)
	 * @return time in nanos since start() was called. 
	 */
	public long Stop(PrintStream os) {
		lastStop = System.nanoTime();
		lastTimeNanos = lastStop-lastStart;
		nMeasures++;
		if (runningCounts != null) {
			runningCounts[iRuningCount++] = lastTimeNanos;
			if (iRuningCount >= runningCounts.length) {
				iRuningCount = 0;
			}
		}
		if (os != null) {
			os.println(this.toString());
		}
		return lastTimeNanos;
	}


	/**
	 * @return the iRuningCount
	 */
	private int getRuningCount() {
		if (runningCounts == null) {
			return 0;
		}
		else {
			return runningCounts.length;
		}
	}
	
	/**
	 * Mean time over n calls if setRunningCount(n) was previously called. 
	 * @return mean time over n calls to start() / stop(). 
	 */
	public long getMeanTime() {
		if (runningCounts == null) {
			return lastTimeNanos;
		}
		long tot = 0;
		int n = Math.min(runningCounts.length, nMeasures);
		for (int i = 0; i < n; i++) {
			tot += runningCounts[i];
		}
		return tot/n;
	}


	/**
	 * 
	 * Set the number of calls to make average measurements over. 
	 * @param iRuningCount the iRuningCount to set
	 */
	public void setRuningCount(int nRunningCounts) {
		if (runningCounts == null || runningCounts.length != nRunningCounts) {
			runningCounts = new long[nRunningCounts];
			iRuningCount = 0;
			nMeasures = 0;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = String.format("%s took %3.1f microseconds", timerName, (double) lastTimeNanos / 1000.);
		if (runningCounts != null) {
			int n = Math.min(nMeasures, runningCounts.length);
			str += String.format(", Mean of %d = %3.1f mics", n, (double)getMeanTime()/1000.);
		}
		return str;
	}
	
}
