package clickDetector.offlineFuncs.rcImport;

public interface DBConvertProgress {

	void setProgress(int state, int totalEvents, int totalClicks, int processedClicks);
	
	boolean stop();
	
}
