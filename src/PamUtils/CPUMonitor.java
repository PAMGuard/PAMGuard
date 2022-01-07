package PamUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Class to monitor Thread CPU usage over multiple calls.  
 */
public class CPUMonitor {

	private long totalProcessTime;

	private long processCalls;
	
	private long totalRealNanos;

	private ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
	private long start;
	private long startR;
	
	public CPUMonitor() {
	}
	
	public void reset() {
		totalProcessTime = 0;
		processCalls = 0;
		totalRealNanos = 0;
	}
	
	public void start() {
		start = tmxb.getCurrentThreadCpuTime();
		startR = System.nanoTime();
	}
	
	public long stop() {
		long len = tmxb.getCurrentThreadCpuTime() - start;
		totalProcessTime += len;
		len = System.nanoTime()-startR;
		totalRealNanos += len;
		processCalls++;
		return len;
	}

	/**
	 * @return the totalProcessTime
	 */
	public long getTotalProcessTime() {
		return totalProcessTime;
	}

	/**
	 * @return the processCalls
	 */
	public long getProcessCalls() {
		return processCalls;
	}
	
	public String getSummary(String prefix) {
		return prefix + " " + getSummary();
	}
	
	public String getSummary() {
		return String.format("calls %d total CPU %s, CPU per call %s, Total Time, %s, Time Per Call %s", processCalls, 
				formatNanoString(totalProcessTime), formatNanoString((double) totalProcessTime/processCalls),
				formatNanoString(totalRealNanos), formatNanoString((double) totalRealNanos/processCalls));
	}
	
	private String formatNanoString(double nanosecs) {
		String unit = "nanos";
		double scale = 1000;
		if (nanosecs > 1.e9) {
			scale = 1.e9;
			unit = "s";
		}
		else if (nanosecs > 1.e6) {
			scale = 1.e6;
			unit = "ms";
		}
		else if (nanosecs > 1.e3) {
			scale = 1.e3;
			unit = "us";
		}
		return String.format("%3.3f%s", nanosecs/scale, unit);
	}
}
