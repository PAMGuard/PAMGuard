package tethys.niluswraps;

import PamguardMVC.PamDataBlock;
import nilus.Detections;

public class PDetections extends NilusDataWrapper<Detections> {

	public PDetections(Detections detections, PamDataBlock dataBlock, PDeployment deployment, Integer count) {
		super(detections, dataBlock, deployment, count);
	}
	
	

}
