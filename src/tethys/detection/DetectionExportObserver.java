package tethys.detection;

public interface DetectionExportObserver {

	/**
	 * Update message and state of export 
	 * @param progress
	 */
	public void update(DetectionExportProgress progress);
	
}
