package tethys.niluswraps;

import nilus.Detections;

public class PDetections {
	
	public Detections detections;
	
	public Integer count;
	
	public PDeployment deployment;

	public PDetections(Detections detections, PDeployment deployment, Integer count) {
		super();
		this.detections = detections;
		this.deployment = deployment;
		this.count = count;
	}
	
	
	

}
