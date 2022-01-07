package clickDetector;

import Localiser.detectionGroupLocaliser.GroupDetection;
import PamView.GeneralProjector;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamProcess;

/**
 * Click Train data block deletes old data in a slightly
 * different way to PamDataBlock. Where PamDataBlock
 * starts at the beginning of the list and deletes items
 * until one of them starts after the set time at which point
 * it stops, ClickTrainDataBlock examins all units and deletes
 * all of those which end before the set time. This is necessary since
 * some click train data units last for hours, whereas others 
 * can be deleted after a couple of seconds. 
 * @author Doug Gillespie
 *
 */
public class ClickGroupDataBlock<t extends GroupDetection> extends AcousticDataBlock<t> {

	public ClickGroupDataBlock(Class pduClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(pduClass, dataName, parentProcess, channelMap);
	}

	@Override
	synchronized protected int removeOldUnitsT(long currentTimeMS) {
		int unitsRemoved = 0;
		if (pamDataUnits.isEmpty())
			return 0;
		GroupDetection clickTrain;
		long firstWantedTime = currentTimeMS - this.naturalLifetime * 1000;
		firstWantedTime = Math.min(firstWantedTime, currentTimeMS - getRequiredHistory());
		
		int i = 0;

		while (i < pamDataUnits.size()) {
			clickTrain = pamDataUnits.get(i);
			if (clickTrain.getEndTimeInMilliseconds() < firstWantedTime) {
				pamDataUnits.remove(clickTrain);
			}
			else if ((clickTrain).getStatus() == ClickTrainDetection.STATUS_BINME) {
				pamDataUnits.remove(clickTrain);
			}
			else {
				i++;
			}
		}
		return unitsRemoved;
	}

}
