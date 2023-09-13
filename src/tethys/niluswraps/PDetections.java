package tethys.niluswraps;

import PamguardMVC.PamDataBlock;
import nilus.Detections;

public class PDetections {
	
	public Detections detections;
	
	public Integer count;
	
	public PDeployment deployment;

	public PamDataBlock dataBlock;

	public PDetections(Detections detections, PamDataBlock dataBlock, PDeployment deployment, Integer count) {
		super();
		this.dataBlock = dataBlock;
		this.detections = detections;
		this.deployment = deployment;
		this.count = count;
	}
	
	
	

}
