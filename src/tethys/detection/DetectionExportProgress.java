package tethys.detection;

import nilus.Detections;
import tethys.niluswraps.PDeployment;

public class DetectionExportProgress {
	
	public static final int STATE_GATHERING = 1;
	public static final int STATE_COUNTING = 2;
	public static final int STATE_WRITING = 3;
	public static final int STATE_CANCELED = 4;
	public static final int STATE_COMPLETE = 5;
	
	public PDeployment currentDeployment;
	public Detections currentDetections;
	public long lastUnitTime;
	public long totalUnits;
	public int exportCount;
	public int skipCount;
	public int state;
	public int totalDeployments, deploymentsDone;
	public int nMapPoints;
	public int doneMapPoints;
	
	public DetectionExportProgress(PDeployment currentDeployment, Detections currentDetections, int nMapPoints, int doneMapPoints, 
			long lastUnitTime,
			long totalUnits, int exportCount, int skipCount, int state) {
		super();
		this.currentDeployment = currentDeployment;
		this.currentDetections = currentDetections;
		this.nMapPoints = nMapPoints;
		this.doneMapPoints = doneMapPoints;
		this.lastUnitTime = lastUnitTime;
		this.totalUnits = totalUnits;
		this.exportCount = exportCount;
		this.skipCount = skipCount;
		this.state = state;
	}
}
