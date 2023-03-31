package tethys.detection;

import nilus.Detections;
import tethys.niluswraps.PDeployment;

public class DetectionExportProgress {
	
	public static final int STATE_GATHERING = 1;
	public static final int STATE_CANCELED = 2;
	public static final int STATE_COMPLETE = 3;
	public static final int STATE_WRITING = 4;
	public PDeployment currentDeployment;
	public Detections currentDetections;
	public long lastUnitTime;
	public long totalUnits;
	public int exportCount;
	public int skipCount;
	public int state;
	
	public DetectionExportProgress(PDeployment currentDeployment, Detections currentDetections, long lastUnitTime,
			long totalUnits, int exportCount, int skipCount, int state) {
		super();
		this.currentDeployment = currentDeployment;
		this.currentDetections = currentDetections;
		this.lastUnitTime = lastUnitTime;
		this.totalUnits = totalUnits;
		this.exportCount = exportCount;
		this.skipCount = skipCount;
		this.state = state;
	}
}
