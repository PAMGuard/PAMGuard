package tethys.detection;

import java.util.ArrayList;

import tethys.niluswraps.NilusDataWrapper;
import tethys.niluswraps.PDetections;

/**
 * Summary information on all Detections documents for a Stream for this 
 * PAMGuard dataset. 
 * @author dg50
 *
 */
public class StreamDetectionsSummary<T extends NilusDataWrapper> {

	public ArrayList<T> detectionsDocs;

	public StreamDetectionsSummary(ArrayList<T> detectionsDocs) {
		this.detectionsDocs = detectionsDocs;
	}

}
