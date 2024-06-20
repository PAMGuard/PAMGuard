package binaryFileStorage.checker;

import java.util.ArrayList;

import PamController.DataIntegrityChecker;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
import dataMap.MapOverlap;
import dataMap.OfflineDataMap;

public class BinaryIntegrityChecker implements DataIntegrityChecker {

	private BinaryStore binaryStore;

	public BinaryIntegrityChecker(BinaryStore binaryStore) {
		this.binaryStore = binaryStore;
	}

	public boolean checkDataStore() {
		checkMapOverlaps();
		
		
		return true;
	}

	private boolean checkMapOverlaps() {
		boolean ok = true;
		ArrayList<PamDataBlock> dataBlocks = binaryStore.getStreamingDataBlocks(false);
		for (PamDataBlock aBlock : dataBlocks) {
			ok &= checkMapOverlaps(aBlock);
		}
		return ok;
	}

	private boolean checkMapOverlaps(PamDataBlock aBlock) {
		OfflineDataMap dataMap = aBlock.getOfflineDataMap(binaryStore);
		if (dataMap == null) {
			return true;
		}
		ArrayList<MapOverlap> overlaps = dataMap.checkOverlaps();
		if (overlaps == null || overlaps.size() == 0) {
			return true;
		}
		String warn = String.format("<html>Binary data %s has %d overlapping data files, the first one at %s to %s<br>"
				+ "This can occur when data have been reprocessed multiple times offline into "
				+ "the same folders.<br>Since files sometimes get slightly different names, old files are"
				+ "not always overwritten. <br>"
				+ "You should determine which files are 'old' by looking at file creation dates and delete them.",
				aBlock.getLongDataName(), overlaps.size(), PamCalendar.formatDBDateTime(overlaps.get(0).getFile1End()), PamCalendar.formatDBDateTime(overlaps.get(0).getFile2Start()));
		WarnOnce.showNamedWarning("BINOVERLAPWARNING", PamController.getMainFrame(), "Data Integrity Warning", warn, WarnOnce.WARNING_MESSAGE);
		return false;
	}
}
