package binaryFileStorage.checker;

import java.util.ArrayList;

import PamController.DataIntegrityChecker;
import PamController.PamController;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryStore;
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
		long overlaps = dataMap.checkOverlaps();
		if (overlaps <= 0) {
			return true;
		}
		String warn = String.format("<html>Binary data %s has overlapping data files, with a maximum overlap of %3.1fs<br>"
				+ "This can occur when data have been reprocessed multiple times offline into "
				+ "the same folders.<br>Since files sometimes get slightly different names, old files are"
				+ "not always overwritten. <br>"
				+ "You should determine which files are 'old' by looking at file creation dates and delete them.",
				aBlock.getLongDataName(), (double) overlaps / 1000.);
		WarnOnce.showNamedWarning("BINOVERLAPWARNING", PamController.getMainFrame(), "Data Integrity Warning", warn, WarnOnce.WARNING_MESSAGE);
		return false;
	}
}
