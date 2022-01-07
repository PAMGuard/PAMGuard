package whistlesAndMoans;

import Localiser.detectionGroupLocaliser.DetectionGrouper;
import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class WhistleDetectionGrouper extends DetectionGrouper<ConnectedRegionDataUnit> {

	public WhistleDetectionGrouper(PamControlledUnit pamControlledUnit,
			PamDataBlock sourceDataBlock) {
		super(pamControlledUnit, sourceDataBlock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean match(PamDataUnit currentData, PamDataUnit olderData) {
		// see if there is a 50% overlap in time and frequency.
//		if (currentData.getChannelBitmap() == olderData.getChannelBitmap()) {	// use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
		if (currentData.getSequenceBitmap() == olderData.getSequenceBitmap()) {
			return false;
		}
		double oT = getTOverlap(currentData, olderData);
		double oF = getFOverlap(currentData, olderData);
		if (oF > 0.5 && oT > 0.5) {
			return true;
		}
		
		return false;
	}

	double getTOverlap(PamDataUnit w1, PamDataUnit w2) {
		return Math.max(w1.getTimeOverlap(w2), w2.getTimeOverlap(w1));
	}
	
	double getFOverlap(PamDataUnit w1, PamDataUnit w2) {
		return Math.max(w1.getFrequencyOverlap(w2), w2.getFrequencyOverlap(w1));
	}
	


}
