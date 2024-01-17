package tethys.detection;

import java.util.ArrayList;

import tethys.niluswraps.PDetections;

/**
 * Summary information on all Detections documents for a Stream for this 
 * PAMGuard dataset. 
 * @author dg50
 *
 */
public class StreamDetectionsSummary {

	public ArrayList<PDetections> detectionsDocs;

	public StreamDetectionsSummary(ArrayList<PDetections> detectionsDocs) {
		this.detectionsDocs = detectionsDocs;
	}

}
