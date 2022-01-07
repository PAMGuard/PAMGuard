package PamController.memory;

public class PamMemory {

	private long free, total, max, available;
	boolean critical;
	
	public PamMemory() {
		Runtime r = Runtime.getRuntime();
		free = r.freeMemory();
		total = r.totalMemory();
		max = r.maxMemory();
		available = max - (total-free);
		critical = available < 10000000L || available < max / 20;
	}

	/**
	 * @return the free
	 */
	public long getFree() {
		return free;
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return the max
	 */
	public long getMax() {
		return max;
	}

	/**
	 * @return the available
	 */
	public long getAvailable() {
		return available;
	}

	/**
	 * @return the critical
	 */
	public boolean isCritical() {
		return critical;
	}
	static final private long[] scales = {1, 1024, 1024*1024};
	static final private String[] scaleUnits = {"B", "KB", "MB"};
	
	
	public String formatMemory(long membytes) {
		int scaleInd = getScaleIndex(membytes);
		if (scaleInd == 0) {
			return String.format("%d%s", membytes, scaleUnits[0]);
		}
		else {
			return String.format("%3.1f%s", (double) membytes / (double) scales[scaleInd], scaleUnits[scaleInd]);
		}
	}

	private int getScaleIndex(long membytes) {
		for (int i = 0; i < scales.length; i++) {
			if (membytes < scales[i]) {
				return i;
			}
		}
		return scales.length-1;
	}

}
